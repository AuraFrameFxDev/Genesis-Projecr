// = =====
tasks.register<Delete>("cleanAllConsciousnessApis") {
    group = "openapi"
    description = "🧯 Clean ALL consciousness API files (Windows-safe)"
    
    delete(outputPath)
    
    // Windows-specific file locking workaround
    doFirst {
        val outputDir = outputPath.get().asFile
        
        if (outputDir.exists()) {
            logger.lifecycle("🧹 Attempting to clean OpenAPI directory: ${outputDir.absolutePath}")
            
            try {
                // First attempt: normal deletion
                outputDir.deleteRecursively()
                logger.lifecycle("✅ Normal deletion successful")
            } catch (e: Exception) {
                logger.warn("⚠️ Normal deletion failed: ${e.message}")
                
                // Second attempt: force unlock and delete
                try {
                    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
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
                        
                        // Try PowerShell force delete
                        val powershellCommand = """
                            if (Test-Path '${outputDir.absolutePath}') {
                                Remove-Item '${outputDir.absolutePath}' -Recurse -Force -ErrorAction SilentlyContinue
                            }
                        """.trimIndent()
                        
                        val psProcess = ProcessBuilder(
                            "powershell", "-Command", powershellCommand
                        ).start()
                        psProcess.waitFor()
                        
                        logger.lifecycle("🔧 Applied Windows force deletion")
                    }
                    
                    // Final attempt
                    if (outputDir.exists()) {
                        outputDir.deleteRecursively()
                    }
                    
                } catch (e: Exception) {
                    logger.warn("⚠️ Force deletion failed: ${e.message}")
                    logger.warn("💡 Try running 'force-delete-openapi.bat' manually")
                }
            }
        }
    }
    
    doLast {
        val outputDir = outputPath.get().asFile
        if (outputDir.exists()) {
            logger.warn("⚠️ Some files may still be locked. Consider:")
            logger.warn("   1. Closing Android Studio")
            logger.warn("   2. Running: force-delete-openapi.bat")
            logger.warn("   3. Restarting your computer")
        } else {
            logger.lifecycle("✅ OpenAPI directory successfully cleaned!")
            
            // Recreate the directory structure
            outputDir.mkdirs()
            logger.lifecycle("📁 Fresh OpenAPI directory created")
        }
    }
}
