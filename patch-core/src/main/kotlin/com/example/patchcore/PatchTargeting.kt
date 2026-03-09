package com.example.patchcore

data class InstalledApp(
    val packageName: String,
    val appName: String,
    val dex: DexIr
)

class PatchTargetSelector {
    fun selectByPackageName(apps: List<InstalledApp>, packageName: String): InstalledApp {
        require(packageName.isNotBlank()) { "packageName must not be blank" }
        return apps.firstOrNull { it.packageName == packageName }
            ?: throw IllegalArgumentException("Installed app not found for package: $packageName")
    }
}

class PatchWorkflow(
    private val targetSelector: PatchTargetSelector,
    private val patchEngine: PatchEngine
) {
    fun patchSelectedApp(
        installedApps: List<InstalledApp>,
        packageName: String,
        module: PatchModule
    ): PatchExecutionResult {
        val selectedApp = targetSelector.selectByPackageName(installedApps, packageName)
        val patchResult = patchEngine.applyModule(selectedApp.dex, module)
        return PatchExecutionResult(selectedApp, patchResult)
    }
}

data class PatchExecutionResult(
    val targetApp: InstalledApp,
    val patchResult: PatchResult
)
