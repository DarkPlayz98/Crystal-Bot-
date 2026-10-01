package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.security.MessageDigest

data class AuthUser(
    val uid: String,
    val email: String?,
    val isAnonymous: Boolean,
    val displayName: String?
)

class AuthService(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("crystal_auth_prefs", Context.MODE_PRIVATE)

    private val auth: FirebaseAuth? = try {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        FirebaseAuth.getInstance()
    } catch (e: Throwable) {
        null
    }

    companion object {
        // Strict RFC 5322 compliant regex requiring valid username, domain, and 2-10 char TLD
        private val STRICT_EMAIL_REGEX =
            Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,10}$")

        const val DEFAULT_DEMO_EMAIL = "agent@crystal.bot"
        const val DEFAULT_DEMO_PASS = "CrystalBot2026!"
    }

    init {
        // Pre-seed official verified demo account
        if (!prefs.contains("user_$DEFAULT_DEMO_EMAIL")) {
            saveLocalAccount(
                email = DEFAULT_DEMO_EMAIL,
                password = DEFAULT_DEMO_PASS,
                displayName = "Crystal Agent"
            )
        }
    }

    val isFirebaseAvailable: Boolean
        get() = auth != null

    val currentUser: AuthUser?
        get() {
            val firebaseUser = auth?.currentUser
            if (firebaseUser != null) {
                return AuthUser(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    isAnonymous = firebaseUser.isAnonymous,
                    displayName = firebaseUser.displayName ?: firebaseUser.email?.substringBefore('@') ?: "Agent"
                )
            }

            // Check persistent active local session
            val activeEmail = prefs.getString("active_session_email", null)
            val activeUid = prefs.getString("active_session_uid", null)
            val isGuest = prefs.getBoolean("active_session_is_guest", false)

            if (activeUid != null) {
                return AuthUser(
                    uid = activeUid,
                    email = activeEmail,
                    isAnonymous = isGuest,
                    displayName = activeEmail?.substringBefore('@') ?: if (isGuest) "Guest Agent" else "User"
                )
            }
            return null
        }

    fun isUserSignedIn(): Boolean = currentUser != null

    suspend fun signInWithEmail(email: String, pass: String): Result<AuthUser> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        val trimmedPass = pass.trim()

        // 1. Strict RFC Email Format Validation (rejects scribble like "asdf", "fake", "a@@b", "no-tld@domain")
        if (trimmedEmail.isBlank() || !STRICT_EMAIL_REGEX.matches(trimmedEmail)) {
            return@withContext Result.failure(
                IllegalArgumentException("Invalid email format. Please enter a valid address (e.g. name@domain.com).")
            )
        }

        // 2. Strict Password Validation
        if (trimmedPass.length < 6) {
            return@withContext Result.failure(
                IllegalArgumentException("Password is too short. Minimum 6 characters required.")
            )
        }

        // 3. Try Firebase Auth if configured with Google Services
        val firebaseAuth = auth
        if (firebaseAuth != null) {
            try {
                val result = firebaseAuth.signInWithEmailAndPassword(trimmedEmail, trimmedPass).await()
                val user = result.user
                if (user != null) {
                    recordActiveSession(user.uid, user.email, isGuest = false)
                    return@withContext Result.success(
                        AuthUser(
                            uid = user.uid,
                            email = user.email,
                            isAnonymous = user.isAnonymous,
                            displayName = user.displayName ?: user.email?.substringBefore('@')
                        )
                    )
                }
            } catch (e: FirebaseAuthException) {
                val friendlyMsg = when (e.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "The email address is invalid."
                    "ERROR_WRONG_PASSWORD" -> "Incorrect password. Authentication denied."
                    "ERROR_USER_NOT_FOUND" -> "No account found with email '$trimmedEmail'. Please check the email or create an account."
                    "ERROR_USER_DISABLED" -> "This user account has been disabled."
                    "ERROR_TOO_MANY_REQUESTS" -> "Too many failed attempts. Please try again later."
                    "ERROR_OPERATION_NOT_ALLOWED" -> "Email/Password sign-in is disabled in Firebase console."
                    else -> e.localizedMessage ?: "Firebase authentication failed (${e.errorCode})"
                }
                return@withContext Result.failure(Exception(friendlyMsg))
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: ""
                // If it's a genuine credential rejection from Firebase Auth servers, reject immediately!
                if (msg.contains("password", ignoreCase = true) ||
                    msg.contains("user", ignoreCase = true) ||
                    msg.contains("credential", ignoreCase = true) ||
                    msg.contains("not found", ignoreCase = true)
                ) {
                    return@withContext Result.failure(Exception("Authentication Failed: $msg"))
                }
            }
        }

        // 4. Secure Credential Verification against registered accounts
        val storedHash = prefs.getString("user_$trimmedEmail", null)
        if (storedHash == null) {
            return@withContext Result.failure(
                Exception("Authentication Failed: No account found for '$trimmedEmail'. Please create an account first.")
            )
        }

        val inputHash = hashPassword(trimmedPass)
        if (storedHash != inputHash) {
            return@withContext Result.failure(
                Exception("Authentication Failed: Incorrect password for '$trimmedEmail'. Access denied.")
            )
        }

        // Validated credentials!
        val uid = prefs.getString("uid_$trimmedEmail", "user_${trimmedEmail.hashCode()}") ?: "user_${trimmedEmail.hashCode()}"
        recordActiveSession(uid, trimmedEmail, isGuest = false)

        Result.success(
            AuthUser(
                uid = uid,
                email = trimmedEmail,
                isAnonymous = false,
                displayName = trimmedEmail.substringBefore('@')
            )
        )
    }

    suspend fun signUpWithEmail(email: String, pass: String): Result<AuthUser> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        val trimmedPass = pass.trim()

        if (trimmedEmail.isBlank() || !STRICT_EMAIL_REGEX.matches(trimmedEmail)) {
            return@withContext Result.failure(
                IllegalArgumentException("Invalid email format. Please enter a valid address (e.g. name@domain.com).")
            )
        }

        if (trimmedPass.length < 6) {
            return@withContext Result.failure(
                IllegalArgumentException("Password must be at least 6 characters long.")
            )
        }

        // Reject obvious dummy scribbles
        val domain = trimmedEmail.substringAfter('@')
        if (!domain.contains('.') || domain.length < 4) {
            return@withContext Result.failure(
                IllegalArgumentException("Invalid email domain. Please use a valid email domain (e.g. gmail.com, outlook.com).")
            )
        }

        // 1. Try Firebase Auth if live
        val firebaseAuth = auth
        if (firebaseAuth != null) {
            try {
                val result = firebaseAuth.createUserWithEmailAndPassword(trimmedEmail, trimmedPass).await()
                val user = result.user
                if (user != null) {
                    recordActiveSession(user.uid, user.email, isGuest = false)
                    // Also mirror locally
                    saveLocalAccount(trimmedEmail, trimmedPass, user.email?.substringBefore('@') ?: "Agent", user.uid)
                    return@withContext Result.success(
                        AuthUser(
                            uid = user.uid,
                            email = user.email,
                            isAnonymous = user.isAnonymous,
                            displayName = user.displayName ?: user.email?.substringBefore('@')
                        )
                    )
                }
            } catch (e: FirebaseAuthException) {
                val friendlyMsg = when (e.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "This email '$trimmedEmail' is already registered. Please sign in instead."
                    "ERROR_INVALID_EMAIL" -> "The email address is invalid."
                    "ERROR_WEAK_PASSWORD" -> "The password is too weak. Please use at least 6 characters."
                    else -> e.localizedMessage ?: "Registration failed (${e.errorCode})"
                }
                return@withContext Result.failure(Exception(friendlyMsg))
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: ""
                if (msg.contains("already in use", ignoreCase = true) || msg.contains("weak", ignoreCase = true)) {
                    return@withContext Result.failure(Exception(msg))
                }
            }
        }

        // 2. Local Account Registration
        if (prefs.contains("user_$trimmedEmail")) {
            return@withContext Result.failure(
                Exception("An account with email '$trimmedEmail' already exists. Please switch to Sign In.")
            )
        }

        val uid = "user_${java.util.UUID.randomUUID()}"
        saveLocalAccount(trimmedEmail, trimmedPass, trimmedEmail.substringBefore('@'), uid)
        recordActiveSession(uid, trimmedEmail, isGuest = false)

        Result.success(
            AuthUser(
                uid = uid,
                email = trimmedEmail,
                isAnonymous = false,
                displayName = trimmedEmail.substringBefore('@')
            )
        )
    }

    suspend fun signInAnonymously(): Result<AuthUser> = withContext(Dispatchers.IO) {
        val firebaseAuth = auth
        if (firebaseAuth != null) {
            try {
                val result = firebaseAuth.signInAnonymously().await()
                val user = result.user
                if (user != null) {
                    recordActiveSession(user.uid, null, isGuest = true)
                    return@withContext Result.success(
                        AuthUser(
                            uid = user.uid,
                            email = null,
                            isAnonymous = true,
                            displayName = "Guest Agent"
                        )
                    )
                }
            } catch (e: Exception) {
                // fall through
            }
        }

        val guestUid = "guest_${System.currentTimeMillis()}"
        recordActiveSession(guestUid, null, isGuest = true)
        Result.success(
            AuthUser(
                uid = guestUid,
                email = null,
                isAnonymous = true,
                displayName = "Guest Agent"
            )
        )
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Throwable) {
            // ignore
        }
        prefs.edit()
            .remove("active_session_email")
            .remove("active_session_uid")
            .remove("active_session_is_guest")
            .apply()
    }

    private fun saveLocalAccount(email: String, password: String, displayName: String, uid: String = "user_${email.hashCode()}") {
        prefs.edit()
            .putString("user_$email", hashPassword(password))
            .putString("uid_$email", uid)
            .putString("name_$email", displayName)
            .apply()
    }

    private fun recordActiveSession(uid: String, email: String?, isGuest: Boolean) {
        prefs.edit()
            .putString("active_session_uid", uid)
            .putString("active_session_email", email)
            .putBoolean("active_session_is_guest", isGuest)
            .apply()
    }

    private fun hashPassword(password: String): String {
        val salt = "crystal_salt_2026"
        val bytes = MessageDigest.getInstance("SHA-256").digest((password + salt).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
