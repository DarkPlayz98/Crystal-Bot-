package com.example.data.integrations

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import java.net.URLEncoder

sealed class IntegrationActionResult {
    data class LaunchedIntent(val title: String, val detail: String) : IntegrationActionResult()
    data class OpenedUrl(val url: String, val detail: String) : IntegrationActionResult()
    data class Error(val message: String) : IntegrationActionResult()
}

object IntegrationExecutor {

    /**
     * Executes real Android Intents, system actions, or web endpoints for connected integrations.
     */
    fun executeRealAction(
        context: Context,
        appId: String,
        field1: String,
        field2: String,
        field3: String
    ): IntegrationActionResult {
        return try {
            when (appId.lowercase()) {
                "gmail" -> {
                    // Real Gmail / Email Intent
                    val recipient = field1.trim().ifBlank { "team@crystal.bot" }
                    val subject = field2.trim().ifBlank { "Autonomous Bot Task Update" }
                    val body = field3.trim().ifBlank { "Sent via Crystal Bot Workspace Integration" }

                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$recipient")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
                        putExtra(Intent.EXTRA_SUBJECT, subject)
                        putExtra(Intent.EXTRA_TEXT, body)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    if (emailIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(emailIntent)
                        IntegrationActionResult.LaunchedIntent(
                            "Gmail Intent Launched",
                            "Opened email composer for $recipient with subject '$subject'"
                        )
                    } else {
                        // Fallback to Web Gmail URL
                        val encTo = URLEncoder.encode(recipient, "UTF-8")
                        val encSub = URLEncoder.encode(subject, "UTF-8")
                        val encBody = URLEncoder.encode(body, "UTF-8")
                        val webUrl = "https://mail.google.com/mail/?view=cm&fs=1&to=$encTo&su=$encSub&body=$encBody"
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(webIntent)
                        IntegrationActionResult.OpenedUrl(
                            webUrl,
                            "Opened Gmail Web composer for $recipient"
                        )
                    }
                }

                "github" -> {
                    // Real GitHub Issue Creator
                    val repo = field1.trim().ifBlank { "crystal-ai/agent-workspace" }.removePrefix("https://github.com/")
                    val issueTitle = field2.trim().ifBlank { "Bot Autonomous Task Report" }
                    val issueBody = field3.trim().ifBlank { "Automated issue report generated from Crystal Bot Cloud Computer." }

                    val encTitle = URLEncoder.encode(issueTitle, "UTF-8")
                    val encBody = URLEncoder.encode(issueBody, "UTF-8")
                    val issueUrl = "https://github.com/$repo/issues/new?title=$encTitle&body=$encBody"

                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(issueUrl)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(browserIntent)
                    IntegrationActionResult.OpenedUrl(
                        issueUrl,
                        "Launched GitHub Issue Creator for repo '$repo'"
                    )
                }

                "slack" -> {
                    // Real Slack Message Sender / Share Intent
                    val channel = field1.trim().ifBlank { "#general" }
                    val author = field2.trim().ifBlank { "Crystal Bot" }
                    val message = field3.trim().ifBlank { "Hello from autonomous swarm!" }

                    val fullText = "[$channel] $author: $message"
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, fullText)
                        putExtra(Intent.EXTRA_TITLE, "Post to Slack ($channel)")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    val chooser = Intent.createChooser(shareIntent, "Post to Slack / Communication App").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(chooser)
                    IntegrationActionResult.LaunchedIntent(
                        "Slack Share Intent",
                        "Sharing '$fullText' to Slack channel"
                    )
                }

                "notion" -> {
                    // Real Notion / Notes Creator
                    val workspace = field1.trim().ifBlank { "Agent Notes" }
                    val pageTitle = field2.trim().ifBlank { "Crystal Bot Knowledge Note" }
                    val noteContent = field3.trim().ifBlank { "Autonomous bot routine insights and telemetry." }

                    val fullContent = "# $pageTitle\nWorkspace: $workspace\n\n$noteContent"
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, pageTitle)
                        putExtra(Intent.EXTRA_TEXT, fullContent)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    val chooser = Intent.createChooser(shareIntent, "Save Note to Notion or Keep").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(chooser)
                    IntegrationActionResult.LaunchedIntent(
                        "Notion Note Exported",
                        "Exporting page '$pageTitle' to Notion workspace"
                    )
                }

                else -> {
                    // Salesforce / HubSpot / CRM Contact
                    val contactName = field1.trim().ifBlank { "Alex Mercer" }
                    val company = field2.trim().ifBlank { "Crystal Labs" }
                    val dealValue = field3.trim().ifBlank { "$12,000" }

                    val contactIntent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI).apply {
                        putExtra(ContactsContract.Intents.Insert.NAME, contactName)
                        putExtra(ContactsContract.Intents.Insert.COMPANY, company)
                        putExtra(ContactsContract.Intents.Insert.NOTES, "CRM Lead Deal Value: $dealValue - Sourced via Crystal AI Swarm")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (contactIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(contactIntent)
                        IntegrationActionResult.LaunchedIntent(
                            "CRM Contact Added",
                            "Created CRM lead for $contactName at $company ($dealValue)"
                        )
                    } else {
                        val crmUrl = "https://app.hubspot.com/contacts"
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(crmUrl)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(browserIntent)
                        IntegrationActionResult.OpenedUrl(
                            crmUrl,
                            "Opened CRM Web Dashboard for $contactName"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            IntegrationActionResult.Error("Failed to trigger live action: ${e.localizedMessage}")
        }
    }
}
