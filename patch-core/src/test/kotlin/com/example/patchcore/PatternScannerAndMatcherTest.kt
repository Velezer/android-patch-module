package com.example.patchcore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PatternScannerAndMatcherTest {

    @Test
    fun scannerFindsMultiplePatternMatchesInSingleMethod() {
        val method = MethodIr(
            className = "Lcom/example/player/Controller;",
            methodName = "play",
            strings = listOf("ad"),
            calls = listOf("Player.isAd:()Z"),
            instructions = mutableListOf(
                Instruction("CONST_STRING"),
                Instruction("IF_NEZ"),
                Instruction("INVOKE_VIRTUAL"),
                Instruction("NOP"),
                Instruction("CONST_STRING"),
                Instruction("IF_NEZ"),
                Instruction("INVOKE_VIRTUAL"),
                Instruction("RETURN")
            )
        )

        val signature = PatternSignature(
            opcodePattern = listOf("CONST_STRING", "IF_NEZ", "INVOKE_VIRTUAL"),
            requiredStrings = listOf("ad"),
            requiredCalls = listOf("Player.isAd:()Z")
        )

        val matches = PatternScanner().scan(method, signature)

        assertEquals(2, matches.size)
        assertEquals(listOf(0, 4), matches.map { it.startIndex })
    }

    @Test
    fun graphMatcherRejectsMethodWithoutReturnOpcode() {
        val method = MethodIr(
            className = "Lcom/example/player/Controller;",
            methodName = "play",
            strings = emptyList(),
            calls = emptyList(),
            instructions = mutableListOf(
                Instruction("IF_NEZ"),
                Instruction("INVOKE_VIRTUAL")
            )
        )

        assertFalse(GraphMatcher().hasTargetShape(method))
    }

    @Test
    fun graphMatcherAcceptsMethodWithBranchInvokeAndReturn() {
        val method = MethodIr(
            className = "Lcom/example/player/Controller;",
            methodName = "play",
            strings = emptyList(),
            calls = emptyList(),
            instructions = mutableListOf(
                Instruction("IF_EQZ"),
                Instruction("INVOKE_VIRTUAL"),
                Instruction("RETURN")
            )
        )

        assertTrue(GraphMatcher().hasTargetShape(method))
    }
}
