package com.example.androidpatchmodule

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.patchcore.DexIr
import com.example.patchcore.DexParser
import com.example.patchcore.GraphMatcher
import com.example.patchcore.Instruction
import com.example.patchcore.MethodIr
import com.example.patchcore.PatchEngine
import com.example.patchcore.PatternScanner
import com.example.patchcore.modules.YouTubeSkipAdsModule

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dex = DexIr(
            methods = listOf(
                MethodIr(
                    className = "Lcom/example/Controller;",
                    methodName = "play",
                    strings = listOf("ad"),
                    calls = listOf("Player.isAd:()Z"),
                    instructions = mutableListOf(
                        Instruction("CONST_STRING", listOf("ad")),
                        Instruction("IF_NEZ", listOf("v0", "label_ad")),
                        Instruction("INVOKE_VIRTUAL", listOf("Player.isAd:()Z")),
                        Instruction("RETURN")
                    )
                )
            )
        )

        val result = PatchEngine(DexParser(), PatternScanner(), GraphMatcher())
            .applyModule(dex, YouTubeSkipAdsModule.build())

        val view = TextView(this)
        view.text = "Applied patches: ${result.applied}"
        setContentView(view)
    }
}
