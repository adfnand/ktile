package com.adferdv.ktile.core.instance

import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path

private const val TOGGLE_COMMAND = "toggle"
private const val COMMAND_BUFFER_SIZE = 64
private const val STOP_TIMEOUT_MS = 100L

class SingleInstanceToggle(
    private var onToggle: () -> Unit,
) {
    private val socketPath: Path = Path(System.getProperty("java.io.tmpdir"), "ktile-${ownerId()}.sock")
    private var server: ServerSocketChannel? = null
    private var thread: Thread? = null

    fun setCallback(callback: () -> Unit) {
        onToggle = callback
    }

    fun trySendToggleAndExit(): Boolean =
        Files.exists(socketPath) &&
            try {
                SocketChannel.open(UnixDomainSocketAddress.of(socketPath)).use { channel ->
                    channel.write(ByteBuffer.wrap(TOGGLE_COMMAND.toByteArray(StandardCharsets.UTF_8)))
                }
                true
            } catch (_: Exception) {
                Files.deleteIfExists(socketPath)
                false
            }

    fun startServer() {
        Files.deleteIfExists(socketPath)
        val channel =
            ServerSocketChannel.open(StandardProtocolFamily.UNIX).apply {
                bind(UnixDomainSocketAddress.of(socketPath))
            }
        server = channel
        thread =
            Thread({ listen(channel) }, "ktile-single-instance").apply {
                isDaemon = true
                start()
            }
    }

    fun stopServer() {
        thread?.interrupt()
        server?.close()
        thread?.join(STOP_TIMEOUT_MS)
        Files.deleteIfExists(socketPath)
    }

    private fun listen(channel: ServerSocketChannel) {
        while (!Thread.currentThread().isInterrupted) {
            try {
                channel.accept().use { client -> handleClient(client) }
            } catch (_: Exception) {
                break
            }
        }
    }

    private fun handleClient(client: SocketChannel) {
        val buffer = ByteBuffer.allocate(COMMAND_BUFFER_SIZE)
        client.read(buffer)
        buffer.flip()
        val command =
            StandardCharsets.UTF_8
                .decode(buffer)
                .toString()
                .trim()
        if (command == TOGGLE_COMMAND) {
            onToggle()
        }
    }

    private fun ownerId(): String = System.getProperty("user.name", "unknown")
}
