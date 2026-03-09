package com.example.androidpatchmodule

import android.os.Bundle
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apps = loadInstalledAppsWithPatchIr().sortedBy { it.appName.lowercase() }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(48, 48, 48, 48)
        }

        val title = TextView(this).apply {
            text = "Select installed app to patch"
            textSize = 20f
        }

        val appSpinner = Spinner(this)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            apps.map { "${it.appName} (${it.packageName})" }
        )
        appSpinner.adapter = adapter

        val patchButton = Button(this).apply {
            text = "Patch selected app"
        }

        val resultView = TextView(this).apply {
            text = "Ready"
            textSize = 16f
        }

        patchButton.setOnClickListener {
            if (apps.isEmpty()) {
                resultView.text = "No installed apps available"
                return@setOnClickListener
            }

            val selected = apps[appSpinner.selectedItemPosition]
            val workflow = PatchWorkflow(
                PatchTargetSelector(),
                PatchEngine(DexParser(), PatternScanner(), GraphMatcher())
            )
            val execution = workflow.patchSelectedApp(
                installedApps = apps,
                packageName = selected.packageName,
                module = YouTubeSkipAdsModule.build()
            )

            resultView.text = "Patched ${execution.targetApp.appName}: ${execution.patchResult.applied} change(s)"
        }

        root.addView(title)
        root.addView(appSpinner)
        root.addView(patchButton)
        root.addView(resultView)
        setContentView(root)
    }

    private fun loadInstalledAppsWithPatchIr(): List<InstalledApp> {
        val pm = packageManager
        return pm.getInstalledApplications(0).map { appInfo ->
            InstalledApp(
                packageName = appInfo.packageName,
                appName = pm.getApplicationLabel(appInfo).toString(),
                dex = createSampleDex(appInfo.packageName)
            )
        }
    }

    private fun createSampleDex(packageName: String): DexIr {
        val looksLikeYoutubeTarget = packageName.contains("youtube", ignoreCase = true)

        val strings = if (looksLikeYoutubeTarget) {
            listOf("ad", "skip", "player_response")
        } else {
            listOf("home", "feed", "player_response")
        }

        return DexIr(
            methods = listOf(
                MethodIr(
                    className = "L$packageName/MainController;",
                    methodName = "play",
                    strings = strings,
                    calls = listOf("Player.isAd:()Z", "UiController.showSkipButton:()V"),
                    instructions = mutableListOf(
                        Instruction("CONST_STRING", listOf(strings.first())),
                        Instruction("IF_NEZ", listOf("v0", "label_gate")),
                        Instruction("INVOKE_VIRTUAL", listOf("Player.isAd:()Z")),
                        Instruction("RETURN")
                    )
                )
            )
        )
    }
}
