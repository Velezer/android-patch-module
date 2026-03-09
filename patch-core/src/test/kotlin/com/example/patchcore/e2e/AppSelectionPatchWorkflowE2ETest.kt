package com.example.patchcore.e2e

import com.example.patchcore.DexIr
import com.example.patchcore.DexParser
import com.example.patchcore.GraphMatcher
import com.example.patchcore.InstalledApp
import com.example.patchcore.Instruction
import com.example.patchcore.MethodIr
import com.example.patchcore.PatchEngine
import com.example.patchcore.PatchTargetSelector
import com.example.patchcore.PatchWorkflow
import com.example.patchcore.PatternScanner
import com.example.patchcore.modules.YouTubeSkipAdsModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AppSelectionPatchWorkflowE2ETest {

    @Test
    fun workflowPatchesOnlySelectedInstalledAppWithoutMocks() {
        val installedApps = listOf(
            InstalledApp(
                packageName = "com.example.music",
                appName = "Music",
                dex = makeDex(strings = listOf("home", "player_response"))
            ),
            InstalledApp(
                packageName = "com.google.android.youtube",
                appName = "YouTube",
                dex = makeDex(strings = listOf("ad", "skip", "player_response"))
            )
        )

        val workflow = PatchWorkflow(
            PatchTargetSelector(),
            PatchEngine(DexParser(), PatternScanner(), GraphMatcher())
        )

        val execution = workflow.patchSelectedApp(
            installedApps = installedApps,
            packageName = "com.google.android.youtube",
            module = YouTubeSkipAdsModule.build()
        )

        assertEquals("com.google.android.youtube", execution.targetApp.packageName)
        assertEquals(1, execution.patchResult.applied)
        assertEquals("IF_EQZ", execution.targetApp.dex.methods.first().instructions[1].opcode)
        assertEquals("IF_NEZ", installedApps.first().dex.methods.first().instructions[1].opcode)
    }

    @Test
    fun workflowFailsWhenSelectedAppIsNotInstalled() {
        val installedApps = listOf(
            InstalledApp(
                packageName = "com.example.music",
                appName = "Music",
                dex = makeDex(strings = listOf("home", "player_response"))
            )
        )

        val workflow = PatchWorkflow(
            PatchTargetSelector(),
            PatchEngine(DexParser(), PatternScanner(), GraphMatcher())
        )

        assertFailsWith<IllegalArgumentException> {
            workflow.patchSelectedApp(
                installedApps = installedApps,
                packageName = "com.google.android.youtube",
                module = YouTubeSkipAdsModule.build()
            )
        }
    }

    private fun makeDex(strings: List<String>): DexIr {
        return DexIr(
            methods = listOf(
                MethodIr(
                    className = "Lcom/example/player/Controller;",
                    methodName = "play",
                    strings = strings,
                    calls = listOf("Player.isAd:()Z", "UiController.showSkipButton:()V"),
                    instructions = mutableListOf(
                        Instruction("CONST_STRING", listOf(strings.first())),
                        Instruction("IF_NEZ", listOf("v1", "label_ad")),
                        Instruction("INVOKE_VIRTUAL", listOf("Player.isAd:()Z")),
                        Instruction("RETURN")
                    )
                )
            )
        )
    }
}
