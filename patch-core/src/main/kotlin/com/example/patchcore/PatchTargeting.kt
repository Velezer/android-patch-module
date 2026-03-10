package com.example.patchcore

data class InstalledApp(
    val packageName: String,
    val appName: String,
    val dex: DexIr
)

class PatchTargetSelector {
    fun selectByPackageName(apps: List<InstalledApp>, packageName: String): InstalledApp {
        val normalizedPackageName = packageName.trim()
        require(normalizedPackageName.isNotEmpty()) { "packageName must not be blank" }
        return apps.firstOrNull { it.packageName == normalizedPackageName }
            ?: throw IllegalArgumentException("Installed app not found for package: $normalizedPackageName")
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
