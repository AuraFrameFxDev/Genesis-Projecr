/*
  Test Suite: BuildScriptIntegrationTest
  Purpose: Validate critical aspects of the Gradle build script(s) introduced/changed in the PR.
  Focus Areas (from the diff snippet):
    - Required plugin IDs are present (Android, Kotlin, KSP, Hilt, Dokka, Spotless, Kover, OpenAPI Generator, Compose, Serialization)
    - Android configuration values present (namespace, compileSdk, ndkVersion, defaultConfig with minSdk/runner, ndk abiFilters)
    - Build features compose/buildConfig enabled
    - Sacred rule: no manual compiler configuration blocks (no kotlinOptions, no composeOptions)
    - Packaging excludes configured
    - externalNativeBuild.cmake.version wired to versions catalog
    - Dependencies include expected bundles and ksp lines; androidTest + debug implementations included
    - Local JARs reference in Libs directory
    - repositories { mavenCentral() } present
    - kotlin { jvmToolchain(8) } present

  Testing framework: JUnit 5 (JUnit Jupiter). Assertions use Kotlin test assertions for readability.
*/

@file:Suppress("MemberVisibilityCanBePrivate", "SpellCheckingInspection")

package buildlogic.tests

import org.junit.jupiter.api.*
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.io.File
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertEquals

@TestInstance(Lifecycle.PER_CLASS)
class BuildScriptIntegrationTest {

    private lateinit var gradleFiles: List<File>

    @BeforeAll
    fun locateGradleScripts() {
        // Heuristic: find build.gradle.kts files that contain unique markers from the snippet
        val root = File(System.getProperty("user.dir"))
        assertTrue(root.exists(), "Repository root should exist")

        gradleFiles = root.walkTopDown()
            .filter { it.isFile && it.name == "build.gradle.kts" && !it.inBuildDir() }
            .filter { file ->
                val text = file.readText()
                listOf(
                    "SACRED RULE #3",
                    "org.openapi.generator",
                    "org.jetbrains.kotlinx.kover",
                    "com.diffplug.spotless",
                    "kotlin(\"jvm\") version \"2.2.10\"",
                    "namespace = \"dev.aurakai.auraframefx.coremodule\"",
                    "ndkVersion = libs.versions.ndkVersion.get()",
                    "jvmToolchain(8)"
                ).count { text.contains(it) } >= 2 // match at least 2 markers to be conservative
            }
            .toList()

        assertTrue(
            gradleFiles.isNotEmpty(),
            "Expected to find at least one Gradle script matching the PR snippet markers."
        )
    }

    @Test
    fun `plugins block contains required plugin IDs`() {
        val requiredIds = listOf(
            "com.android.library",
            "org.jetbrains.kotlin.android",
            "org.jetbrains.kotlin.plugin.compose",
            "org.jetbrains.kotlin.plugin.serialization",
            "com.google.devtools.ksp",
            "com.google.dagger.hilt.android",
            "org.jetbrains.dokka",
            "com.diffplug.spotless",
            "org.jetbrains.kotlinx.kover",
            "org.openapi.generator"
        )

        expectAnyFileMatches("plugins block contains all required plugin IDs") { text ->
            requiredIds.all { id -> text.contains("id(\"$id\")") || text.contains("id('$id')") }
        }
    }

    @Test
    fun `kotlin jvm plugin uses explicit 2_2_10 version in plugins block`() {
        expectAnyFileMatches("kotlin(\"jvm\") version \"2.2.10\" is present") { text ->
            text.contains("kotlin(\"jvm\") version \"2.2.10\"")
        }
    }

    @Test
    fun `android namespace and sdks configured via versions catalog`() {
        expectAnyFileMatches("android { namespace and compileSdk/minSdk from libs versions }") { text ->
            text.contains("android {") &&
            text.contains("namespace = \"dev.aurakai.auraframefx.coremodule\"") &&
            text.contains("compileSdk = libs.versions.compileSdk.get().toInt()") &&
            text.contains("minSdk = libs.versions.minSdk.get().toInt()")
        }
    }

    @Test
    fun `ndk version and abi filters present`() {
        expectAnyFileMatches("ndkVersion set and abi filters defined") { text ->
            text.contains("ndkVersion = libs.versions.ndkVersion.get()") &&
            text.contains("ndk {") &&
            (text.contains("abiFilters.addAll(listOf(\"arm64-v8a\", \"armeabi-v7a\", \"x86_64")) ||
             text.contains("abiFilters") && text.contains("arm64-v8a") && text.contains("armeabi-v7a") && text.contains("x86_64"))
        }
    }

    @Test
    fun `buildTypes release is minified with proper proguard files`() {
        expectAnyFileMatches("release buildType with minify and proguard files") { text ->
            text.contains("buildTypes {") &&
            text.contains("release {") &&
            text.contains("isMinifyEnabled = true") &&
            text.contains("getDefaultProguardFile(\"proguard-android-optimize.txt\")") &&
            text.contains("\"proguard-rules.pro\"")
        }
    }

    @Test
    fun `compose and buildConfig enabled in buildFeatures`() {
        expectAnyFileMatches("buildFeatures compose=true and buildConfig=true") { text ->
            text.contains("buildFeatures {") &&
            text.contains("compose = true") &&
            text.contains("buildConfig = true")
        }
    }

    @Test
    fun `sacred rule enforcements - no kotlinOptions or composeOptions blocks`() {
        expectAllMatchingFiles("no kotlinOptions blocks present") { text ->
            assertFalse(
                Regex("""\bkotlinOptions\s*\{""").containsMatchIn(text),
                "kotlinOptions block should not exist per Sacred Rule #3"
            )
        }
        expectAllMatchingFiles("no composeOptions blocks present") { text ->
            assertFalse(
                Regex("""\bcomposeOptions\s*\{""").containsMatchIn(text),
                "composeOptions block should not exist per Sacred Rule #3"
            )
        }
    }

    @Test
    fun `packaging excludes are configured`() {
        expectAnyFileMatches("packaging excludes META-INF AL2.0, LGPL2.1") { text ->
            text.contains("packaging {") &&
            text.contains("resources {") &&
            text.contains("excludes += \"/META-INF/{AL2.0,LGPL2.1}\"")
        }
    }

    @Test
    fun `externalNativeBuild cmake uses version from versions catalog`() {
        expectAnyFileMatches("externalNativeBuild.cmake.version = libs.versions.cmakeVersion.get()") { text ->
            text.contains("externalNativeBuild {") &&
            text.contains("cmake {") &&
            text.contains("version = libs.versions.cmakeVersion.get()")
        }
    }

    @Test
    fun `dependencies include expected bundles and ksp lines`() {
        val requiredDependencySnippets = listOf(
            "implementation(libs.bundles.androidx.core)",
            "implementation(libs.bundles.compose)",
            "implementation(libs.bundles.coroutines)",
            "implementation(libs.bundles.network)",
            "implementation(libs.androidx.navigation.compose)",
            "implementation(libs.hilt.android)",
            "ksp(libs.hilt.compiler)",
            "implementation(libs.hilt.navigation.compose)",
            "implementation(libs.bundles.room)",
            "ksp(libs.room.compiler)",
            "coreLibraryDesugaring(libs.coreLibraryDesugaring)",
            "testImplementation(libs.bundles.testing)",
            "testImplementation(libs.junit.engine)",
            "androidTestImplementation(libs.bundles.testing)",
            "androidTestImplementation(platform(libs.androidx.compose.bom))",
            "androidTestImplementation(libs.androidx.compose.ui.test.junit4)",
            "androidTestImplementation(libs.hilt.android.testing)",
            "kspAndroidTest(libs.hilt.compiler)",
            "debugImplementation(libs.androidx.compose.ui.tooling)",
            "debugImplementation(libs.androidx.compose.ui.test.manifest)"
        )

        expectAnyFileMatches("dependencies contain all required entries") { text ->
            requiredDependencySnippets.all { snippet -> text.contains(snippet) }
        }
    }

    @Test
    fun `local Xposed framework jars are referenced from Libs directory`() {
        expectAnyFileMatches("implementation(files(\"\${project.rootDir}/Libs/api-82.jar\")) and sources jar") { text ->
            text.contains("implementation(files(\"\\${'$'}{project.rootDir}/Libs/api-82.jar\"))") &&
            text.contains("implementation(files(\"\\${'$'}{project.rootDir}/Libs/api-82-sources.jar\"))")
        }
    }

    @Test
    fun `stdlib jdk8 is included`() {
        expectAnyFileMatches("implementation(kotlin(\"stdlib-jdk8\")) present") { text ->
            text.contains("implementation(kotlin(\"stdlib-jdk8\"))")
        }
    }

    @Test
    fun `repositories include mavenCentral`() {
        expectAnyFileMatches("repositories { mavenCentral() } block exists") { text ->
            text.contains("repositories {") && text.contains("mavenCentral()")
        }
    }

    @Test
    fun `kotlin jvmToolchain set to 8`() {
        expectAnyFileMatches("kotlin { jvmToolchain(8) } present") { text ->
            text.contains("kotlin {") && text.contains("jvmToolchain(8)")
        }
    }

    // Utility expectations

    private fun expectAnyFileMatches(message: String, predicate: (String) -> Boolean) {
        val failures = mutableListOf<String>()
        val matched = gradleFiles.any { file ->
            val text = file.readText()
            val ok = try { predicate(text) } catch (t: Throwable) {
                failures += "${file.relativeToOrSelf(File(System.getProperty(\"user.dir\")))}: ${t.message}"
                false
            }
            if (!ok) failures += file.relativeToOrSelf(File(System.getProperty("user.dir"))).path
            ok
        }
        assertTrue(
            matched,
            buildString {
                appendLine("$message failed for all candidate build.gradle.kts files.")
                if (failures.isNotEmpty()) {
                    appendLine("Checked files:")
                    failures.forEach { appendLine(" - $it") }
                }
            }
        )
    }

    private fun expectAllMatchingFiles(message: String, assertion: (String) -> Unit) {
        assertTrue(gradleFiles.isNotEmpty(), "No candidate Gradle scripts to validate for: $message")
        gradleFiles.forEach { file ->
            val text = file.readText()
            assertion(text)
        }
    }

    private fun File.inBuildDir(): Boolean {
        val parts = generateSequence(this) { it.parentFile }.map { it.name }.toList()
        return parts.any { it == "build" }
    }
}