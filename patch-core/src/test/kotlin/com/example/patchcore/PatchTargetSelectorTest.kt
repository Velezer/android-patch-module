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

    @Test
    fun selectByPackageNameAcceptsTrimmedInput() {
        val apps = listOf(
            InstalledApp("com.google.android.youtube", "YouTube", DexIr(emptyList()))
        )

        val selected = PatchTargetSelector().selectByPackageName(apps, "  com.google.android.youtube  ")

        assertEquals("com.google.android.youtube", selected.packageName)
    }

    @Test
    fun selectByPackageNameIncludesNormalizedPackageInNotFoundMessage() {
        val apps = listOf(InstalledApp("com.example.music", "Music", DexIr(emptyList())))

        val error = assertFailsWith<IllegalArgumentException> {
            PatchTargetSelector().selectByPackageName(apps, "  com.unknown.app  ")
        }

        assertEquals("Installed app not found for package: com.unknown.app", error.message)
    }
}
