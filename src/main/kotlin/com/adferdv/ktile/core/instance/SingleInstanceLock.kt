package com.adferdv.ktile.core.instance

import java.io.IOException
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.OverlappingFileLockException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path

class SingleInstanceLock {
    private val lockFile: Path = Path(System.getProperty("java.io.tmpdir"), "ktile.lock")
    private var channel: FileChannel? = null
    private var lock: FileLock? = null

    fun tryAcquire(): Boolean =
        try {
            Files.createDirectories(lockFile.parent)
            val ch = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
            val l = ch.tryLock() ?: return false.also { ch.close() }
            channel = ch
            lock = l
            true
        } catch (_: IOException) {
            false
        } catch (_: OverlappingFileLockException) {
            false
        }

    fun release() {
        try {
            lock?.release()
        } catch (_: IOException) {
        }
        try {
            channel?.close()
        } catch (_: IOException) {
        }
        try {
            Files.deleteIfExists(lockFile)
        } catch (_: IOException) {
        }
    }
}
