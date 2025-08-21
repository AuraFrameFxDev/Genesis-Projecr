plugins {
    id("org.openapi.generator")
}

val specDir = file("${rootDir}/api-spec")
val generatedDir = file("${projectDir}/generated")
val configFile = file("${rootDir}/openapi-generator-config.json")

val specFiles =
    specDir.listFiles { f -> f.extension == "yml" || f.extension == "yaml" } ?: emptyArray()

specFiles.forEach { specFile ->
    val specName = specFile.nameWithoutExtension
    tasks.register(
        "generateOpenApi_${specName}",
        org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class
    ) {
        generatorName.set("kotlin")
        inputSpec.set(specFile.absolutePath)
        outputDir.set("${generatedDir}/${specName}")
        configFile.set(configFile.absolutePath)
    }
}

tasks.register("generateAllApiClients") {
    dependsOn(specFiles.map { specFile -> "generateOpenApi_${specFile.nameWithoutExtension}" })
}

tasks.register("cleanOpenApiGenerated") {
    doLast {
        delete(generatedDir)
    }
}
