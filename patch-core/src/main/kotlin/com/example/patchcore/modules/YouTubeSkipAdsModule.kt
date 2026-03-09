package com.example.patchcore.modules

import com.example.patchcore.Instruction
import com.example.patchcore.Match
import com.example.patchcore.MethodIr
import com.example.patchcore.PatchModule
import com.example.patchcore.PatternSignature

object YouTubeSkipAdsModule {
    fun build(): PatchModule = PatchModule(
        name = "youtube-skip-ads",
        description = "Flip ad gate branch from IF_NEZ to IF_EQZ",
        versionCompatibility = "android/youtube/*",
        signature = PatternSignature(
            opcodePattern = listOf("CONST_STRING", "IF_NEZ", "INVOKE_VIRTUAL"),
            requiredStrings = listOf("ad"),
            requiredCalls = listOf("Player.isAd:()Z")
        ),
        transform = ::flipBranch
    )

    private fun flipBranch(method: MethodIr, match: Match): Boolean {
        for (index in match.startIndex until method.instructions.size) {
            val current = method.instructions[index]
            if (current.opcode == "IF_NEZ") {
                method.instructions[index] = Instruction("IF_EQZ", current.operands)
                return true
            }
        }
        return false
    }
}
