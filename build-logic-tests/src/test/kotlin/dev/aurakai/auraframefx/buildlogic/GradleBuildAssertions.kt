package dev.aurakai.auraframefx.buildlogic

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertAll

/**
 * Lightweight text-level assertions tailored for validating Gradle Kotlin DSL content when
 * full resolution of catalogs/plugins is not feasible in isolated TestKit runs.
 * These assertions complement TestKit-based task checks and serve as guardrails on critical config.
 */
object GradleBuildAssertions {
    fun assertContainsAll(haystack: String, vararg needles: String) {
        assertAll(needles.map { needle ->
            { assertTrue(haystack.contains(needle), "Expected build file to contain: $needle") }
        })
    }
}