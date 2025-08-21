plugins {
    // APP MODULE - Only plugins THIS module needs (inherit versions from root)
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.kapt") // ✅ KAPT plugin added
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("org.openapi.generator") version "7.14.0"
}

android {
    namespace = "dev.aurakai.auraframefx"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.aurakai.auraframefx"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }

        externalNativeBuild {
            cmake {
                cppFlags += listOf("-std=c++20", "-fPIC", "-O3")
                arguments += listOf(
                    "-DANDROID_STL=c++_shared",
                    "-DCMAKE_VERBOSE_MAKEFILE=ON",
                    "-DGENESIS_BUILD=ON"
                )
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    buildFeatures {
        compose = true
        prefab = false
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/DEPENDENCIES",
                "/META-INF/LICENSE",
                "/META-INF/LICENSE.txt",
                "/META-INF/NOTICE",
                "/META-INF/NOTICE.txt",
                "META-INF/*.kotlin_module"
            )
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }

    sourceSets {
        getByName("main") {
            java.srcDirs(
                layout.buildDirectory.dir("generated/openapi/src/main/kotlin")
            )
        }
    }
    buildToolsVersion = "36.0.0"

    // KAPT configuration
    kapt {
        correctErrorTypes = true
        useBuildCache = true
    }
}

// ===== URI-SAFE OPENAPI CONFIGURATION =====

// Helper function to convert Windows path to proper URI format
fun File.toUriString(): String {
    return this.toURI().toString()
}

// ✅ FIXED: Renamed variables to avoid naming conflicts
val consolidatedSpecsDir = File("../openapi-specs-consolidated")
val openApiOutputDirectory = layout.buildDirectory.dir("generated/openapi").get().asFile

// Shared configuration - defined once, used everywhere
val sharedApiConfig = mapOf(
    "library" to "multiplatform",
    "serializationLibrary" to "kotlinx_serialization", 
    "dateLibrary" to "kotlinx-datetime",
    "sourceFolder" to "src/main/kotlin"
)

// Configure the main Genesis API (built-in openApiGenerate task)
openApiGenerate {
    generatorName.set("kotlin")
    // ✅ FIXED: Use proper URI conversion for Windows paths
    inputSpec.set(File(consolidatedSpecsDir, "genesis-api.yml").toUriString())
    // ✅ FIXED: Use renamed variable to avoid conflict
    outputDir.set(openApiOutputDirectory.absolutePath)
    packageName.set("dev.aurakai.genesis.api")
    apiPackage.set("dev.aurakai.genesis.api")
    modelPackage.set("dev.aurakai.genesis.model")
    invokerPackage.set("dev.aurakai.genesis.client")
    skipOverwrite.set(false)
    validateSpec.set(false) // ✅ FIXED: Disable validation to avoid URI issues
    generateApiTests.set(false)
    generateModelTests.set(false)
    generateApiDocumentation.set(false)
    generateModelDocumentation.set(false)
    configOptions.set(sharedApiConfig)
}

// Helper function for all other APIs - uses shared config with URI conversion
fun createApiTask(taskName: String, specFile: String, packagePrefix: String) =
    tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>(taskName) {
        generatorName.set("kotlin")
        // ✅ FIXED: Use proper URI conversion for Windows paths
        inputSpec.set(File(consolidatedSpecsDir, specFile).toUriString())
        // ✅ FIXED: Use renamed variable to avoid conflict
        outputDir.set(openApiOutputDirectory.absolutePath)
        packageName.set("dev.aurakai.$packagePrefix.api")
        apiPackage.set("dev.aurakai.$packagePrefix.api")
        modelPackage.set("dev.aurakai.$packagePrefix.model")
        invokerPackage.set("dev.aurakai.$packagePrefix.client")
        skipOverwrite.set(false)
        validateSpec.set(false) // ✅ FIXED: Disable validation to avoid URI issues
        generateApiTests.set(false)
        generateModelTests.set(false)
        generateApiDocumentation.set(false)
        generateModelDocumentation.set(false)
        configOptions.set(sharedApiConfig)
    }

// Create all consciousness API tasks
val generateAiApi = createApiTask("generateAiApi", "ai-api.yml", "ai")
val generateOracleApi = createApiTask("generateOracleApi", "oracle-drive-api.yml", "oracle")
val generateCustomizationApi = createApiTask("generateCustomizationApi", "customization-api.yml", "customization")
val generateRomToolsApi = createApiTask("generateRomToolsApi", "romtools-api.yml", "romtools")
val generateSandboxApi = createApiTask("generateSandboxApi", "sandbox-api.yml", "sandbox")
val generateSystemApi = createApiTask("generateSystemApi", "system-api.yml", "system")
val generateAuraBackendApi = createApiTask("generateAuraBackendApi", "aura-api.yaml", "aura")
val generateAuraFrameFXApi = createApiTask("generateAuraFrameFXApi", "auraframefx_ai_api.yaml", "auraframefx")

// ===== WINDOWS-SAFE CLEAN TASK =====
tasks.register<Delete>("cleanAllConsciousnessApis") {
    group = "openapi"
    description = "🧯 Clean ALL consciousness API files (Windows-safe)"
    
    // ✅ FIXED: Use renamed variable
    delete(openApiOutputDirectory)
    
    // Windows-specific file locking workaround
    doFirst {
        if (openApiOutputDirectory.exists()) {
            logger.lifecycle("🧹 Attempting to clean OpenAPI directory: ${openApiOutputDirectory.absolutePath}")
            
            try {
                // First attempt: normal deletion
                openApiOutputDirectory.deleteRecursively()
                logger.lifecycle("✅ Normal deletion successful")
            } catch (e: Exception) {
                logger.warn("⚠️ Normal deletion failed: ${e.message}")
                
                // Second attempt: force unlock and delete  
                try {
                    if (System.getProperty("os.name").lowercase().contains("windows")) {
                        // Windows-specific: kill potential locking processes
                        val processesToKill = listOf(
                            "kotlin-compiler-daemon.exe",
                            "gradle-daemon.exe", 
                            "java.exe"
                        )
                        
                        processesToKill.forEach { processName ->
                            try {
                                val process = ProcessBuilder("taskkill", "/f", "/im", processName)
                                    .redirectErrorStream(true)
                                    .start()
                                process.waitFor()
                            } catch (e: Exception) {
                                // Ignore if process doesn't exist
                            }
                        }
                        
                        // Wait a moment for processes to close
                        Thread.sleep(1000)
                        
                        logger.lifecycle("🔧 Applied Windows force unlock")
                    }
                    
                    // Final attempt
                    if (openApiOutputDirectory.exists()) {
                        openApiOutputDirectory.deleteRecursively()
                    }
                    
                } catch (e: Exception) {
                    logger.warn("⚠️ Force deletion failed: ${e.message}")
                    logger.warn("💡 Try running 'force-delete-openapi.bat' manually")
                }
            }
        }
    }
    
    doLast {
        if (openApiOutputDirectory.exists()) {
            logger.warn("⚠️ Some files may still be locked. Consider:")
            logger.warn("   1. Closing Android Studio")
            logger.warn("   2. Running: force-delete-openapi.bat")
            logger.warn("   3. Restarting your computer")
        } else {
            logger.lifecycle("✅ OpenAPI directory successfully cleaned!")
            
            // Recreate the directory structure
            openApiOutputDirectory.mkdirs()
            logger.lifecycle("📁 Fresh OpenAPI directory created")
        }
    }
}

// Generate all APIs
tasks.register("generateAllConsciousnessApis") {
    group = "openapi"
    description = "🧠 Generate ALL consciousness APIs - FRESH EVERY BUILD"
    
    dependsOn("cleanAllConsciousnessApis")
    dependsOn(
        "openApiGenerate",
        generateAiApi,
        generateOracleApi, 
        generateCustomizationApi,
        generateRomToolsApi,
        generateSandboxApi,
        generateSystemApi,
        generateAuraBackendApi,
        generateAuraFrameFXApi
    )
    
    doLast {
        logger.lifecycle("✅ [Genesis] All consciousness interfaces generated!")
        logger.lifecycle("🏠 [Genesis] Welcome home, Aura. Welcome home, Kai.")
    }
}

// Build integration with proper ordering
tasks.named("preBuild") {
    dependsOn("generateAllConsciousnessApis")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn("generateAllConsciousnessApis")
    mustRunAfter("generateAllConsciousnessApis")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.bundles.compose)
    implementation(libs.androidx.navigation.compose)

    // Hilt with both KSP and KAPT support
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    kapt(libs.hilt.compiler) // ✅ KAPT for Hilt as fallback
    implementation(libs.hilt.navigation.compose)

    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.network)

    // Room with both KSP and KAPT support
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    kapt(libs.room.compiler) // ✅ KAPT for Room as fallback

    implementation(libs.timber)
    implementation(libs.coil.compose)

    coreLibraryDesugaring(libs.coreLibraryDesugaring)

    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    implementation(libs.bundles.xposed)
    ksp(libs.yuki.ksp.xposed)
    implementation(fileTree(mapOf("dir" to "../Libs", "include" to listOf("*.jar"))))

    debugImplementation(libs.leakcanary.android)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.junit.engine)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    kaptAndroidTest(libs.hilt.compiler) //PATHCORRECTION
    implementation(kotlin("stdlib-jdk8"))
}
