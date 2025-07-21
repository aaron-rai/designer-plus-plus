plugins {
    id("io.ia.sdk.modl") version("0.4.0")
}

val sdk_version by extra("8.1.20")

allprojects {
    version = "0.0.1-SNAPSHOT"
}

ignitionModule {
    name.set("Designer++")
    fileName.set("Designer-Plus-Plus")
    id.set("DesignerPlusPlus")
    moduleVersion.set(findProperty("version")?.toString() ?: "0.0.0-SNAPSHOT")
    moduleDescription.set("Enhanced designer tools and utilities for Ignition development.")
    license.set("LICENSE.txt")
    requiredIgnitionVersion.set(sdk_version)
    projectScopes.putAll(mapOf(
        ":client" to "CD",
        ":common" to "GCD",
        ":designer" to "D",
        ":gateway" to "G"
    ))
    moduleDependencies.set(mapOf<String, String>())
    moduleDependencySpecs { }
    hooks.putAll(mapOf(
        "org.dev.arai.designerpp.gateway.DesignerPlusPlusGatewayHook" to "G",
        "org.dev.arai.designerpp.client.DesignerPlusPlusClientHook" to "C",
        "org.dev.arai.designerpp.designer.DesignerPlusPlusDesignerHook" to "D"
    ))
    skipModlSigning.set(!findProperty("signModule").toString().toBoolean())
}

tasks.withType<io.ia.sdk.gradle.modl.task.Deploy>().configureEach {
  hostGateway.set(project.findProperty("hostGateway")?.toString() ?: "")
}