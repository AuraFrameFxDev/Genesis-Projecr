package dev.aurakai.auraframefx.buildlogic

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import java.io.File
import kotlin.io.path.createTempDirectory

/**
 * Testing library and framework: JUnit Jupiter (JUnit 5) + Gradle TestKit.
 * These tests validate critical configuration present in build-logic-tests/build.gradle.kts,
 * focusing on plugin application, android block, dependencies, and Kotlin toolchain.
 *
 * We bias for action by providing both file-content assertions and a minimal TestKit run that
 * checks Gradle configuration on a synthetic project including the target module when feasible.
 */
class BuildLogicGradleKtsTest {

    private val modulePath = "build-logic-tests/build.gradle.kts"

    @Test
    fun `build file applies required plugins`() {
        val script = File(modulePath).readText()

        GradleBuildAssertions.assertContainsAll(
            script,
            // Plugin IDs
            "id(\"com.android.library\")",
            "id(\"org.jetbrains.kotlin.android\")",
            "id(\"org.jetbrains.kotlin.plugin.compose\")",
            "id(\"org.jetbrains.kotlin.plugin.serialization\")",
            "id(\"com.google.devtools.ksp\")",
            "id(\"com.google.dagger.hilt.android\")",
            "id(\"org.jetbrains.dokka\")",
            "id(\"com.diffplug.spotless\")",
            "id(\"org.jetbrains.kotlinx.kover\")",
            "id(\"org.openapi.generator\")",
            // Kotlin JVM plugin line with version
            "kotlin(\"jvm\") version \"2.2.10\""
        )
    }

    @Test
    fun `android configuration specifies namespace sdks runner and features`() {
        val script = File(modulePath).readText()

        GradleBuildAssertions.assertContainsAll(
            script,
            "android {",
            "namespace = \"dev.aurakai.auraframefx.coremodule\"",
            "compileSdk = libs.versions.compileSdk.get().toInt()",
            "ndkVersion = libs.versions.ndkVersion.get()",
            "defaultConfig {",
            "minSdk = libs.versions.minSdk.get().toInt()",
            "testInstrumentationRunner = \"androidx.test.runner.AndroidJUnitRunner\"",
            "buildFeatures {",
            "compose = true",
            "buildConfig = true"
        )
    }

    @Test
    fun `packaging excludes and externalNativeBuild cmake version are configured`() {
        val script = File(modulePath).readText()

        GradleBuildAssertions.assertContainsAll(
            script,
            "packaging {",
            "resources {",
            "excludes += \"/META-INF/{AL2.0,LGPL2.1}\"",
            "externalNativeBuild {",
            "cmake {",
            "version = libs.versions.cmakeVersion.get()"
        )
    }

    @Test
    fun `dependencies include expected bundles ksp androidTest and debug deps`() {
        val script = File(modulePath).readText()

        GradleBuildAssertions.assertContainsAll(
            script,
            "dependencies {",
            // Core impl bundles
            "implementation(libs.bundles.androidx.core)",
            "implementation(libs.bundles.compose)",
            "implementation(libs.bundles.coroutines)",
            "implementation(libs.bundles.network)",
            // Navigation
            "implementation(libs.androidx.navigation.compose)",
            // Hilt + KSP
            "implementation(libs.hilt.android)",
            "ksp(libs.hilt.compiler)",
            "implementation(libs.hilt.navigation.compose)",
            // Room + KSP
            "implementation(libs.bundles.room)",
            "ksp(libs.room.compiler)",
            // Desugaring
            "coreLibraryDesugaring(libs.coreLibraryDesugaring)",
            // Testing deps
            "testImplementation(libs.bundles.testing)",
            "testImplementation(libs.junit.engine)",
            "androidTestImplementation(libs.bundles.testing)",
            "androidTestImplementation(platform(libs.androidx.compose.bom))",
            "androidTestImplementation(libs.androidx.compose.ui.test.junit4)",
            "androidTestImplementation(libs.hilt.android.testing)",
            "kspAndroidTest(libs.hilt.compiler)",
            // Debug deps
            "debugImplementation(libs.androidx.compose.ui.tooling)",
            "debugImplementation(libs.androidx.compose.ui.test.manifest)",
            // Local file dependencies for Xposed
            "implementation(files(\"${'$'}{project.rootDir}/Libs/api-82.jar\"))",
            "implementation(files(\"${'$'}{project.rootDir}/Libs/api-82-sources.jar\"))",
            // Kotlin stdlib
            "implementation(kotlin(\"stdlib-jdk8\"))"
        )
    }

    @Test
    fun `repositories and kotlin toolchain are configured`() {
        val script = File(modulePath).readText()

        GradleBuildAssertions.assertContainsAll(
            script,
            "repositories {",
            "mavenCentral()",
            "kotlin {",
            "jvmToolchain(8)"
        )
    }

    /**
     * Attempt a minimal Gradle configuration run via TestKit on a synthetic project that:
     *  - Uses version catalogs if present at repo root
     *  - Applies the same build script content under test
     * We disable this in CI environments that restrict process execution, but leaving the test
     * enables local verification where supported.
     */
    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    fun `gradle can configure a project with the given build file (dry run)`() {
        val tmpDir = createTempDirectory("blt-config-test").toFile()
        tmpDir.resolve("settings.gradle.kts").writeText(
            """
            rootProject.name = "blt-config-test"
            """.trimIndent()
        )

        // Create a minimalist version catalog if none will be inherited; many references in the script rely on libs.versions.*
        val gradleDir = tmpDir.resolve("gradle").apply { mkdirs() }
        gradleDir.resolve("libs.versions.toml").writeText(
            """
            [versions]
            compileSdk = "34"
            minSdk = "24"
            ndkVersion = "26.1.10909125"
            cmakeVersion = "3.22.1"
            
            [libraries]
            # Minimal placeholders to allow resolution in dependency notations during configuration.
            # Actual resolution is not required for a configuration dry run.
            androidx-core = { module = "androidx.core:core-ktx", version = "1.12.0" }
            
            [bundles]
            androidx-core = ["androidx-core"]
            compose = []
            coroutines = []
            network = []
            room = []
            testing = []
            
            [plugins]
            # Plugins expected in the script; versions are intentionally omitted because they may be included via settings pluginManagement
            """.trimIndent()
        )

        // Copy the build logic script under test into tmp project module build file
        tmpDir.resolve("build.gradle.kts").writeText(File(modulePath).readText())

        // Perform a dry-run of help task to force configuration phase
        val result = GradleRunner.create()
            .withProjectDir(tmpDir)
            .withArguments("help", "--dry-run", "--stacktrace")
            .withPluginClasspath()
            .forwardOutput()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":help")?.outcome)
    }
}