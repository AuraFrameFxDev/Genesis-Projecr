Testing notes for build-logic-tests:

- Testing library and framework: JUnit Jupiter (JUnit 5) + Gradle TestKit.
- Tests live under `src/test/kotlin/dev/aurakai/auraframefx/buildlogic`.
- The suite validates plugin application, Android configuration, dependency declarations, packaging exclusions, externalNativeBuild CMake version, and Kotlin toolchain settings in `build-logic-tests/build.gradle.kts`.
- One test performs a minimal Gradle configuration dry run using TestKit; it is disabled on CI via `CI=true`.
- Ensure the module (or root project) provides:

    testImplementation(gradleTestKit())
    testImplementation(<Jupiter BOM or junit-jupiter-api + engine>)

- Ensure that the test task uses the JUnit Platform:

    tasks.test {
        useJUnitPlatform()
    }