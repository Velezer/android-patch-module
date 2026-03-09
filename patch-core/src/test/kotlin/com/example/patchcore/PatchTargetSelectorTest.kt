package com.example.patchcore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PatchTargetSelectorTest {

    @Test
    fun selectByPackageNameReturnsMatchingApp() {
        val apps = listOf(
            InstalledApp("com.example.music", "Music", DexIr(emptyList())),
            InstalledApp("com.google.android.youtube", "YouTube", DexIr(emptyList()))
        )

        val selected = PatchTargetSelector().selectByPackageName(apps, "com.google.android.youtube")

        assertEquals("com.google.android.youtube", selected.packageName)
        assertEquals("YouTube", selected.appName)
    }

    @Test
    fun selectByPackageNameRejectsBlankInput() {
        val apps = listOf(InstalledApp("com.example.music", "Music", DexIr(emptyList())))

        val error = assertFailsWith<IllegalArgumentException> {
            PatchTargetSelector().selectByPackageName(apps, "   ")
        }

        assertEquals("packageName must not be blank", error.message)
    }
}
