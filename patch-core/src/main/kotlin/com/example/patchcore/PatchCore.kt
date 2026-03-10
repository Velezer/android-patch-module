package com.example.patchcore

data class Instruction(val opcode: String, val operands: List<String> = emptyList())

data class MethodIr(
    val className: String,
    val methodName: String,
    val strings: List<String>,
    val calls: List<String>,
    val instructions: MutableList<Instruction>
)

data class DexIr(val methods: List<MethodIr>)

data class PatternSignature(
    val opcodePattern: List<String>,
    val requiredStrings: List<String> = emptyList(),
    val requiredCalls: List<String> = emptyList()
)

data class Match(val method: MethodIr, val startIndex: Int, val confidence: Double)

data class PatchResult(val moduleName: String, val applied: Int, val touchedMethods: List<String>)

class DexParser {
    fun iterateMethods(dex: DexIr): Sequence<MethodIr> = dex.methods.asSequence()
}

class PatternScanner {
    fun scan(method: MethodIr, signature: PatternSignature): List<Match> {
        if (signature.opcodePattern.isEmpty()) return emptyList()

        val methodStrings = method.strings.toSet()
        if (!signature.requiredStrings.all { it in methodStrings }) return emptyList()

        val methodCalls = method.calls.toSet()
        if (!signature.requiredCalls.all { it in methodCalls }) return emptyList()

        val hits = mutableListOf<Match>()
        val ops = method.instructions.map { it.opcode }
        val patternSize = signature.opcodePattern.size
        if (ops.size < patternSize) return emptyList()

        for (start in 0..<(ops.size - patternSize + 1)) {
            val window = ops.subList(start, start + patternSize)
            if (window == signature.opcodePattern) {
                hits += Match(method, start, 1.0)
            }
        }
        return hits
    }
}

class GraphMatcher {
    private val branchOps = setOf(
        "IF_EQZ", "IF_NEZ", "IF_EQ", "IF_NE",
        "IF_LTZ", "IF_GEZ", "IF_GTZ", "IF_LEZ"
    )

    fun hasTargetShape(method: MethodIr): Boolean {
        val ops = method.instructions.map { it.opcode }.toSet()
        return ops.any { it in branchOps } && "INVOKE_VIRTUAL" in ops && "RETURN" in ops
    }
}

data class PatchModule(
    val name: String,
    val description: String,
    val versionCompatibility: String,
    val signature: PatternSignature,
    val transform: (MethodIr, Match) -> Boolean
)

class PatchEngine(
    private val parser: DexParser,
    private val scanner: PatternScanner,
    private val graphMatcher: GraphMatcher
) {
    fun applyModule(dex: DexIr, module: PatchModule): PatchResult {
        var applied = 0
        val touched = mutableListOf<String>()

        parser.iterateMethods(dex).forEach { method ->
            val matches = scanner.scan(method, module.signature)
            if (matches.isEmpty() || !graphMatcher.hasTargetShape(method)) return@forEach

            for (match in matches) {
                if (module.transform(method, match)) {
                    applied += 1
                    touched += "${method.className}->${method.methodName}"
                    break
                }
            }
        }

        return PatchResult(module.name, applied, touched)
    }
}
