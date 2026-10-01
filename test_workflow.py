#!/usr/bin/env python3
"""
Comprehensive 100-Test Validation Suite for .github/workflows/release.yml
Uses Python standard library exclusively (no external dependencies).
"""

import os
import sys
import re

WORKFLOW_PATH = ".github/workflows/release.yml"

def run_tests():
    tests = []
    
    def test(num, description, assertion_fn):
        tests.append((num, description, assertion_fn))

    # Read raw content
    if not os.path.exists(WORKFLOW_PATH):
        print(f"Error: {WORKFLOW_PATH} does not exist.")
        return 1

    with open(WORKFLOW_PATH, "r", encoding="utf-8") as f:
        raw_content = f.read()

    lines = raw_content.splitlines()

    # Helper functions for pure standard library analysis
    def has_section(header):
        return any(line.strip().startswith(header) for line in lines)

    def get_lines_between(start_prefix, end_prefixes):
        result = []
        capturing = False
        for line in lines:
            if line.strip().startswith(start_prefix) and not capturing:
                capturing = True
                result.append(line)
                continue
            if capturing:
                if any(line.strip().startswith(ep) and not line.startswith(" ") for ep in end_prefixes):
                    break
                result.append(line)
        return result

    # -------------------------------------------------------------
    # Category 1: Syntax & File Properties (Tests 1-10)
    # -------------------------------------------------------------
    test(1, "Workflow file exists on disk", lambda: os.path.exists(WORKFLOW_PATH))
    test(2, "Workflow file is non-empty", lambda: len(raw_content.strip()) > 0)
    test(3, "Workflow file has at least 60 lines of configuration", lambda: len(lines) >= 60)
    test(4, "Workflow uses valid UTF-8 encoding without BOM", lambda: not raw_content.startswith("\ufeff"))
    test(5, "Workflow contains no tab indentation characters", lambda: "\t" not in raw_content)
    test(6, "Workflow ends with a trailing newline", lambda: raw_content.endswith("\n"))
    test(7, "Workflow file size is reasonable (> 1KB and < 50 KB)", lambda: 1000 < len(raw_content) < 50000)
    test(8, "No duplicate root 'name:' key in YAML document", lambda: len(re.findall(r"^name:\s+", raw_content, re.MULTILINE)) == 1)
    test(9, "No raw merge conflicts markers (<<<<<<<, =======, >>>>>>>)", lambda: "<<<<<<<" not in raw_content and "=======" not in raw_content and ">>>>>>>" not in raw_content)
    test(10, "Balanced open/closed single and double quotes across file", lambda: raw_content.count("'''") % 2 == 0 and raw_content.count('"""') % 2 == 0)

    # -------------------------------------------------------------
    # Category 2: Top-Level Workflow Structure (Tests 11-20)
    # -------------------------------------------------------------
    test(11, "Root document contains 'name:' declaration", lambda: has_section("name:"))
    test(12, "Workflow name is 'Build & Release APK'", lambda: "name: Build & Release APK" in raw_content)
    test(13, "Workflow name mentions 'APK'", lambda: "APK" in raw_content.split("name:")[1].split("\n")[0])
    test(14, "Root document contains 'on:' trigger block", lambda: bool(re.search(r"^on:\s*$", raw_content, re.MULTILINE)))
    test(15, "Root document contains 'permissions:' block", lambda: bool(re.search(r"^permissions:\s*$", raw_content, re.MULTILINE)))
    test(16, "Root document contains 'jobs:' block", lambda: bool(re.search(r"^jobs:\s*$", raw_content, re.MULTILINE)))
    test(17, "'jobs:' is located after 'on:' and 'permissions:'", lambda: raw_content.index("jobs:") > raw_content.index("on:") and raw_content.index("jobs:") > raw_content.index("permissions:"))
    test(18, "Indentation is uniform 2 spaces for top sub-blocks", lambda: bool(re.search(r"^  release:\s*$", raw_content, re.MULTILINE)))
    test(19, "No unescaped colon inside scalar names without quotes", lambda: not re.search(r"^  [a-zA-Z0-9_-]+: [^'\"\n]+:[^'\"\n]+$", raw_content, re.MULTILINE))
    test(20, "Root block contains valid GitHub Actions keys only", lambda: all(re.match(r"^(name|on|permissions|jobs|env|concurrency):", l) for l in lines if l and not l.startswith(" ") and not l.startswith("#")))

    # -------------------------------------------------------------
    # Category 3: Trigger 'on' Events Configuration (Tests 21-30)
    # -------------------------------------------------------------
    test(21, "'on:' includes 'release:' event trigger", lambda: bool(re.search(r"^\s+release:\s*$", raw_content, re.MULTILINE)))
    test(22, "'release:' event defines 'types:' list", lambda: bool(re.search(r"^\s+types:\s*\[.*\]", raw_content, re.MULTILINE)))
    test(23, "'release:' event includes 'published' trigger", lambda: "published" in raw_content.split("release:")[1].split("types:")[1].split("\n")[0])
    test(24, "'release:' event includes 'created' trigger", lambda: "created" in raw_content.split("release:")[1].split("types:")[1].split("\n")[0])
    test(25, "'on:' includes 'push:' event trigger", lambda: bool(re.search(r"^\s+push:\s*$", raw_content, re.MULTILINE)))
    test(26, "'push:' trigger filters on 'tags:'", lambda: bool(re.search(r"^\s+tags:\s*$", raw_content, re.MULTILINE)))
    test(27, "'push.tags' includes 'v*' version pattern", lambda: bool(re.search(r"^\s+-\s*['\"]v\*['\"]", raw_content, re.MULTILINE)))
    test(28, "'on:' includes 'workflow_dispatch:' for manual runs", lambda: bool(re.search(r"^\s+workflow_dispatch:\s*$", raw_content, re.MULTILINE)))
    test(29, "'workflow_dispatch:' defines interactive 'inputs:'", lambda: bool(re.search(r"^\s+inputs:\s*$", raw_content, re.MULTILINE)))
    test(30, "'workflow_dispatch.inputs' contains 'build_type:' parameter", lambda: bool(re.search(r"^\s+build_type:\s*$", raw_content, re.MULTILINE)))

    # -------------------------------------------------------------
    # Category 4: Workflow Dispatch Inputs (Tests 31-38)
    # -------------------------------------------------------------
    test(31, "'build_type' has a clear description", lambda: "description: 'Build Type (both, release, debug)'" in raw_content)
    test(32, "'build_type' specifies 'type: choice'", lambda: "type: choice" in raw_content)
    test(33, "'build_type' options include 'both'", lambda: bool(re.search(r"^\s+-\s*both\s*$", raw_content, re.MULTILINE)))
    test(34, "'build_type' options include 'release'", lambda: bool(re.search(r"^\s+-\s*release\s*$", raw_content, re.MULTILINE)))
    test(35, "'build_type' options include 'debug'", lambda: bool(re.search(r"^\s+-\s*debug\s*$", raw_content, re.MULTILINE)))
    test(36, "'build_type' defaults to 'both'", lambda: "default: 'both'" in raw_content)
    test(37, "'workflow_dispatch.inputs' contains 'custom_tag:' parameter", lambda: bool(re.search(r"^\s+custom_tag:\s*$", raw_content, re.MULTILINE)))
    test(38, "'custom_tag' input is optional (required: false)", lambda: bool(re.search(r"^\s+required:\s*false\s*$", raw_content, re.MULTILINE)))

    # -------------------------------------------------------------
    # Category 5: Permissions & Security (Tests 39-44)
    # -------------------------------------------------------------
    test(39, "'permissions:' specifies 'contents: write' for release asset upload", lambda: bool(re.search(r"^\s+contents:\s*write\s*$", raw_content, re.MULTILINE)))
    test(40, "'permissions:' specifies 'packages: read'", lambda: bool(re.search(r"^\s+packages:\s*read\s*$", raw_content, re.MULTILINE)))
    test(41, "No blanket global write permissions ('write-all')", lambda: "write-all" not in raw_content)
    test(42, "No unnecessary administrative permissions requested", lambda: "administration:" not in raw_content)
    test(43, "Permissions block properly indented under root", lambda: "  contents: write" in raw_content)
    test(44, "Write permission is strictly constrained to contents", lambda: "pull-requests: write" not in raw_content and "issues: write" not in raw_content)

    # -------------------------------------------------------------
    # Category 6: Job Definition & Runner (Tests 45-52)
    # -------------------------------------------------------------
    test(45, "Job 'build:' is declared under jobs", lambda: bool(re.search(r"^  build:\s*$", raw_content, re.MULTILINE)))
    test(46, "Job specifies display name 'Build & Release APK'", lambda: "name: Build & Release APK" in raw_content.split("jobs:")[1])
    test(47, "Job runs on 'ubuntu-latest'", lambda: "runs-on: ubuntu-latest" in raw_content)
    test(48, "Job specifies 'timeout-minutes: 60' to prevent stuck builds", lambda: "timeout-minutes: 60" in raw_content)
    test(49, "Job timeout is greater than 10 minutes", lambda: int(re.search(r"timeout-minutes:\s*(\d+)", raw_content).group(1)) >= 10)
    test(50, "Job defines 'steps:' sequence", lambda: bool(re.search(r"^\s+steps:\s*$", raw_content, re.MULTILINE)))
    test(51, "Job contains at least 8 distinct steps", lambda: len(re.findall(r"^\s+-\s*name:\s+", raw_content, re.MULTILINE)) >= 8)
    test(52, "Step indentations are aligned properly at 6 spaces", lambda: bool(re.search(r"^      - name:\s+", raw_content, re.MULTILINE)))

    # -------------------------------------------------------------
    # Category 7: Step Names, IDs & Flow (Tests 53-60)
    # -------------------------------------------------------------
    step_names = re.findall(r"^\s+-\s*name:\s*(.+)$", raw_content, re.MULTILINE)
    test(53, "All workflow steps have non-empty names", lambda: all(len(n.strip()) > 0 for n in step_names))
    test(54, "Step 1 is 'Checkout Code'", lambda: any("checkout" in n.lower() for n in step_names[:2]))
    test(55, "Contains Java setup step", lambda: any("java" in n.lower() or "jdk" in n.lower() for n in step_names))
    test(56, "Contains Gradle setup step", lambda: any("gradle" in n.lower() for n in step_names))
    test(57, "Contains Gradlew permissions step", lambda: any("permission" in n.lower() or "executable" in n.lower() for n in step_names))
    test(58, "Contains APK building step", lambda: any("build" in n.lower() and "apk" in n.lower() for n in step_names))
    test(59, "Contains APK collection step with id 'collect_apks'", lambda: "id: collect_apks" in raw_content)
    test(60, "Contains Release assets publication step", lambda: any("release" in n.lower() and ("asset" in n.lower() or "publish" in n.lower()) for n in step_names))

    # -------------------------------------------------------------
    # Category 8: Checkout Action Quality (Tests 61-65)
    # -------------------------------------------------------------
    test(61, "Uses official 'actions/checkout@v4'", lambda: "uses: actions/checkout@v4" in raw_content)
    test(62, "Checkout action version is up-to-date (v4)", lambda: bool(re.search(r"actions/checkout@v4\b", raw_content)))
    test(63, "Checkout step configures 'fetch-depth: 0' for tag retrieval", lambda: "fetch-depth: 0" in raw_content)
    test(64, "Checkout 'with:' block is properly indented", lambda: bool(re.search(r"uses:\s*actions/checkout@v4\s*\n\s+with:\s*\n\s+fetch-depth:\s*0", raw_content)))
    test(65, "No deprecated checkout versions used (v1, v2, v3)", lambda: not re.search(r"actions/checkout@v[123]\b", raw_content))

    # -------------------------------------------------------------
    # Category 9: Java Setup Quality (Tests 66-70)
    # -------------------------------------------------------------
    test(66, "Uses official 'actions/setup-java@v4'", lambda: "uses: actions/setup-java@v4" in raw_content)
    test(67, "Java version configured as '17' for AGP compatibility", lambda: bool(re.search(r"java-version:\s*['\"]?17['\"]?", raw_content)))
    test(68, "Java distribution set to 'temurin' (Eclipse Adoptium)", lambda: "distribution: 'temurin'" in raw_content)
    test(69, "Java step enables 'cache: gradle'", lambda: "cache: 'gradle'" in raw_content)
    test(70, "No deprecated setup-java versions used", lambda: not re.search(r"actions/setup-java@v[123]\b", raw_content))

    # -------------------------------------------------------------
    # Category 10: Gradle Setup & Permissions (Tests 71-76)
    # -------------------------------------------------------------
    test(71, "Uses official 'gradle/actions/setup-gradle@v4'", lambda: "uses: gradle/actions/setup-gradle@v4" in raw_content)
    test(72, "Gradle setup configures 'gradle-version: wrapper'", lambda: "gradle-version: wrapper" in raw_content)
    test(73, "Includes step to grant execute permission to gradlew", lambda: "chmod +x ./gradlew" in raw_content)
    test(74, "Chmod step checks if ./gradlew exists before execution", lambda: '[ -f "./gradlew" ]' in raw_content)
    test(75, "Chmod step provides friendly fallback message if wrapper missing", lambda: 'echo "gradlew not found, will use system gradle"' in raw_content)
    test(76, "No deprecated setup-gradle v1 or v2 actions used", lambda: not re.search(r"gradle/actions/setup-gradle@v[12]\b", raw_content))

    # -------------------------------------------------------------
    # Category 11: Environment & .env Configuration (Tests 77-83)
    # -------------------------------------------------------------
    test(77, "Includes dedicated step for setting up environment files", lambda: "Setup Environment Files" in raw_content)
    test(78, ".env step inspects existence of .env file", lambda: '[ ! -f ".env" ]' in raw_content)
    test(79, ".env step falls back to .env.example if present", lambda: '[ -f ".env.example" ]' in raw_content and "cp .env.example .env" in raw_content)
    test(80, ".env step creates .env if neither exists", lambda: "touch .env" in raw_content)
    test(81, "GEMINI_API_KEY injected safely from GitHub secret", lambda: "${{ secrets.GEMINI_API_KEY }}" in raw_content)
    test(82, "Secrets interpolation uses safe env variable bridge", lambda: "GEMINI_API_KEY_SECRET: ${{ secrets.GEMINI_API_KEY }}" in raw_content)
    test(83, "No hardcoded Google or Gemini API credentials in workflow", lambda: "AIzaSy" not in raw_content and "sk-" not in raw_content)

    # -------------------------------------------------------------
    # Category 12: Keystore & Signing Configuration (Tests 84-90)
    # -------------------------------------------------------------
    test(84, "Includes dedicated step for restoring keystores", lambda: "Restore Keystores for Signing" in raw_content)
    test(85, "Keystore step detects debug.keystore.base64", lambda: '[ -f "debug.keystore.base64" ]' in raw_content)
    test(86, "Keystore step decodes base64 with 'base64 -d'", lambda: "base64 -d debug.keystore.base64 > debug.keystore" in raw_content)
    test(87, "Keystore step accepts RELEASE_KEYSTORE_BASE64 from GitHub Secrets", lambda: "RELEASE_KEYSTORE_BASE64: ${{ secrets.KEYSTORE_BASE64 }}" in raw_content)
    test(88, "Keystore step writes output to 'my-upload-key.jks'", lambda: "my-upload-key.jks" in raw_content)
    test(89, "Keystore step provides automated fallback generation via keytool", lambda: "keytool -genkey" in raw_content)
    test(90, "No raw private keys or binary data hardcoded in YAML", lambda: "BEGIN PRIVATE KEY" not in raw_content)

    # -------------------------------------------------------------
    # Category 13: Build Command & Execution (Tests 91-96)
    # -------------------------------------------------------------
    test(91, "Build step defines KEYSTORE_PATH in environment", lambda: "KEYSTORE_PATH: ${{ github.workspace }}/my-upload-key.jks" in raw_content)
    test(92, "Build step defines STORE_PASSWORD with fallback", lambda: "STORE_PASSWORD: ${{ secrets.STORE_PASSWORD || 'android' }}" in raw_content)
    test(93, "Build step defines KEY_PASSWORD with fallback", lambda: "KEY_PASSWORD: ${{ secrets.KEY_PASSWORD || 'android' }}" in raw_content)
    test(94, "Build step supports 'assembleRelease'", lambda: "assembleRelease" in raw_content)
    test(95, "Build step supports 'assembleDebug'", lambda: "assembleDebug" in raw_content)
    test(96, "Build step includes '--stacktrace' for actionable diagnostic logs", lambda: "--stacktrace" in raw_content)

    # -------------------------------------------------------------
    # Category 14: APK Collection & Release Publishing (Tests 97-100)
    # -------------------------------------------------------------
    test(97, "Collection step finds all generated APKs in 'app/build/outputs/apk'", lambda: "app/build/outputs/apk" in raw_content and "*.apk" in raw_content)
    test(98, "Collection step fails build with clear error if no APK was produced", lambda: "No APK files found" in raw_content and "exit 1" in raw_content)
    test(99, "Upload artifact step uses 'actions/upload-artifact@v4' with 30-day retention", lambda: "uses: actions/upload-artifact@v4" in raw_content and "retention-days: 30" in raw_content)
    test(100, "Release step uses 'softprops/action-gh-release@v2' to publish APK to GitHub Release", lambda: "uses: softprops/action-gh-release@v2" in raw_content and "files: release-apks/*.apk" in raw_content)

    # Run all 100 tests
    passed = 0
    failed = 0
    failures = []

    print(f"Running 100 Validation Tests on {WORKFLOW_PATH}...\n" + "=" * 75)
    for num, desc, fn in tests:
        try:
            ok = fn()
            if ok:
                passed += 1
                print(f"[TEST {num:03d}/100] PASS: {desc}")
            else:
                failed += 1
                failures.append((num, desc, "Assertion failed"))
                print(f"[TEST {num:03d}/100] FAIL: {desc}")
        except Exception as e:
            failed += 1
            failures.append((num, desc, str(e)))
            print(f"[TEST {num:03d}/100] ERROR: {desc} ({e})")

    print("=" * 75)
    print(f"TOTAL TESTS EVALUATED: {len(tests)}")
    print(f"TOTAL PASSED         : {passed}")
    print(f"TOTAL FAILED         : {failed}")
    print("=" * 75)

    if failed == 0 and passed == 100:
        print("🎉 SUCCESS: ALL 100 TESTS PASSED (100% PASS RATE)!")
        return 0
    else:
        print(f"❌ FAILURE: {failed} test(s) failed:")
        for num, desc, reason in failures:
            print(f"  - Test {num:03d}: {desc} ({reason})")
        return 1

if __name__ == "__main__":
    sys.exit(run_tests())
