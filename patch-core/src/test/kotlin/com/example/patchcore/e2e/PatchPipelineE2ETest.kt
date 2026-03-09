package com.example.patchcore.e2e

import com.example.patchcore.DexIr
import com.example.patchcore.DexParser
import com.example.patchcore.GraphMatcher
import com.example.patchcore.Instruction
import com.example.patchcore.MethodIr
import com.example.patchcore.PatchEngine
import com.example.patchcore.PatternScanner
import com.example.patchcore.modules.YouTubeSkipAdsModule
import kotlin.test.Test
import kotlin.test.assertEquals

class PatchPipelineE2ETest {

    @Test
    fun fullPipelineRewritesAdGateBranchWithoutMocks() {
        val dex = DexIr(
            methods = listOf(
                MethodIr(
                    className = "Lcom/google/android/apps/youtube/player/Controller;",
                    methodName = "play",
                    strings = listOf("ad", "skip", "player_response"),
                    calls = listOf("Player.isAd:()Z", "UiController.showSkipButton:()V"),
                    instructions = mutableListOf(
                        Instruction("CONST_STRING", listOf("ad")),
                        Instruction("IF_NEZ", listOf("v1", "label_ad")),
                        Instruction("INVOKE_VIRTUAL", listOf("Player.isAd:()Z")),
                        Instruction("RETURN")
                    )
                )
            )
        )

        val engine = PatchEngine(DexParser(), PatternScanner(), GraphMatcher())
        val result = engine.applyModule(dex, YouTubeSkipAdsModule.build())

        assertEquals(1, result.applied)
        assertEquals("IF_EQZ", dex.methods.first().instructions[1].opcode)
    }

    @Test
    fun fullPipelineDoesNotPatchWhenRequiredStringIsMissing() {
        val dex = DexIr(
            methods = listOf(
                MethodIr(
                    className = "Lcom/google/android/apps/youtube/player/Controller;",
                    methodName = "play",
                    strings = listOf("player_response"),
                    calls = listOf("Player.isAd:()Z", "UiController.showSkipButton:()V"),
                    instructions = mutableListOf(
                        Instruction("CONST_STRING", listOf("player_response")),
                        Instruction("IF_NEZ", listOf("v1", "label_ad")),
                        Instruction("INVOKE_VIRTUAL", listOf("Player.isAd:()Z")),
                        Instruction("RETURN")
                    )
                )
            )
        )

        val engine = PatchEngine(DexParser(), PatternScanner(), GraphMatcher())
        val result = engine.applyModule(dex, YouTubeSkipAdsModule.build())

        assertEquals(0, result.applied)
        assertEquals("IF_NEZ", dex.methods.first().instructions[1].opcode)
    }
}
