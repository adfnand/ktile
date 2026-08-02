package com.adferdv.ktile.core.hotkey

import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

/**
 * Checks whether the current process is likely able to read Linux input devices.
 *
 * This is a heuristic: it checks group membership and the readability of
 * `/dev/input/event*`. The actual check happens when the native library tries to open evdev.
 */
object InputDevicePermissionChecker {
    fun hasInputDeviceAccess(): Boolean {
        if (!System.getProperty("os.name").lowercase().contains("linux")) {
            return false
        }
        return (isInInputGroup() || canReadAnyInputDevice()) && canWriteUinput()
    }

    private fun canWriteUinput(): Boolean {
        val path = Paths.get("/dev/uinput")
        return Files.exists(path) && Files.isWritable(path)
    }

    private fun isInInputGroup(): Boolean {
        val groupNames = runIdGn() ?: return false
        return groupNames.splitToSequence(" ").any { it.trim() == "input" }
    }

    private fun runIdGn(): String? =
        try {
            ProcessBuilder("id", "-Gn")
                .redirectErrorStream(true)
                .start()
                .run {
                    waitFor(2, TimeUnit.SECONDS)
                    if (exitValue() == 0) inputStream.bufferedReader().readText().trim() else null
                }
        } catch (_: Exception) {
            null
        }

    private fun canReadAnyInputDevice(): Boolean =
        Files.newDirectoryStream(Paths.get("/dev/input"), "event*").use { stream ->
            stream.any { Files.isReadable(it) }
        }
}
