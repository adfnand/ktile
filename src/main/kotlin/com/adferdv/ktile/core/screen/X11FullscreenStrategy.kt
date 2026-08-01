package com.adferdv.ktile.core.screen

import com.sun.jna.NativeLong
import com.sun.jna.platform.unix.X11
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.NativeLongByReference
import com.sun.jna.ptr.PointerByReference
import kotlinx.coroutines.delay
import java.awt.Component
import java.awt.Window
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

object X11FullscreenStrategy : FullscreenStrategy {
    private const val NET_WM_STATE_ADD = 1L
    private const val NO_DATA = 0L
    private const val CLIENT_MESSAGE_FORMAT = 32
    private const val MAX_ATTEMPTS = 3
    private const val RETRY_DELAY_MS = 300L
    private const val FULLSCREEN_POLL_INTERVAL_MS = 50L
    private const val MAX_STATE_ATOMS = 1024L

    override suspend fun setFullscreen(window: Window) {
        val x11 = X11.INSTANCE
        val display = x11.XOpenDisplay(null)
        if (display == null) {
            AwtFullscreenStrategy.setFullscreen(window)
            return
        }
        try {
            val root = x11.XDefaultRootWindow(display)
            val windowId = x11WindowId(window) ?: findWindowByTitle(x11, display, root)
            if (windowId == null) {
                AwtFullscreenStrategy.setFullscreen(window)
                return
            }
            val netWmState = x11.XInternAtom(display, "_NET_WM_STATE", false)
            val fullscreenAtom = x11.XInternAtom(display, "_NET_WM_STATE_FULLSCREEN", false)

            repeat(MAX_ATTEMPTS) {
                requestFullscreen(x11, display, root, windowId, netWmState, fullscreenAtom)
                delay(RETRY_DELAY_MS.milliseconds)
            }
        } finally {
            x11.XCloseDisplay(display)
        }
    }

    override suspend fun waitForFullscreen(
        window: Window,
        timeoutMs: Long,
    ): Boolean {
        val x11 = X11.INSTANCE
        val display = x11.XOpenDisplay(null) ?: return false
        try {
            val windowId = x11WindowId(window)
            val netWmState = x11.XInternAtom(display, "_NET_WM_STATE", false)
            val fullscreenAtom = x11.XInternAtom(display, "_NET_WM_STATE_FULLSCREEN", false)
            val deadline = TimeSource.Monotonic.markNow() + timeoutMs.milliseconds
            var applied = false
            while (!applied && deadline.hasNotPassedNow()) {
                if (windowId != null && isFullscreen(x11, display, windowId, netWmState, fullscreenAtom)) {
                    applied = true
                } else {
                    delay(FULLSCREEN_POLL_INTERVAL_MS.milliseconds)
                }
            }
            return applied
        } finally {
            x11.XCloseDisplay(display)
        }
    }

    private fun isFullscreen(
        x11: X11,
        display: X11.Display,
        windowId: Long,
        netWmState: X11.Atom,
        fullscreenAtom: X11.Atom,
    ): Boolean {
        val actualType = X11.AtomByReference()
        val actualFormat = IntByReference()
        val itemsReturn = NativeLongByReference()
        val bytesAfter = NativeLongByReference()
        val propertyValue = PointerByReference()
        x11.XGetWindowProperty(
            display,
            X11.Window(windowId),
            netWmState,
            NativeLong(0),
            NativeLong(MAX_STATE_ATOMS),
            false,
            X11.XA_ATOM,
            actualType,
            actualFormat,
            itemsReturn,
            bytesAfter,
            propertyValue,
        )
        val value = propertyValue.value ?: return false
        val atomCount = itemsReturn.value.toInt()
        var found = false
        if (atomCount > 0) {
            found = value.getLongArray(0, atomCount).any { it == fullscreenAtom.toLong() }
        }
        x11.XFree(value)
        return found
    }

    private fun requestFullscreen(
        x11: X11,
        display: X11.Display,
        root: X11.Window,
        windowId: Long,
        netWmState: X11.Atom,
        fullscreenAtom: X11.Atom,
    ) {
        val event = X11.XEvent()
        event.setType(X11.XClientMessageEvent::class.java)
        event.xclient.type = X11.ClientMessage
        event.xclient.window = X11.Window(windowId)
        event.xclient.message_type = netWmState
        event.xclient.format = CLIENT_MESSAGE_FORMAT
        event.xclient.data.setType(Array<NativeLong>::class.java)
        val dataSlots = event.xclient.data.l
        dataSlots[0] = NativeLong(NET_WM_STATE_ADD)
        dataSlots[1] = NativeLong(fullscreenAtom.toLong())
        for (i in 2 until dataSlots.size) {
            dataSlots[i] = NativeLong(NO_DATA)
        }

        val mask = NativeLong((X11.SubstructureRedirectMask or X11.SubstructureNotifyMask).toLong())
        x11.XSendEvent(display, root, 0, mask, event)
        x11.XSendEvent(display, X11.Window(windowId), 0, NativeLong(0), event)
        x11.XFlush(display)
    }

    private fun x11WindowId(window: Window): Long? {
        return try {
            val getPeer = Component::class.java.getDeclaredMethod("getPeer")
            getPeer.isAccessible = true
            val peer = getPeer.invoke(window) ?: return null
            val getWindow = peer.javaClass.getMethod("getWindow")
            (getWindow.invoke(peer) as? Number)?.toLong()
        } catch (_: Exception) {
            null
        }
    }

    private fun findWindowByTitle(
        x11: X11,
        display: X11.Display,
        root: X11.Window,
    ): Long? {
        if (matchesTitle(x11, display, root)) {
            return root.toLong()
        }
        return findInChildren(x11, display, root)
    }

    private fun matchesTitle(
        x11: X11,
        display: X11.Display,
        window: X11.Window,
    ): Boolean {
        val namePointer = PointerByReference()
        if (x11.XFetchName(display, window, namePointer) == 0 || namePointer.value == null) {
            return false
        }
        val name = namePointer.value.getString(0)
        x11.XFree(namePointer.value)
        return name == FullscreenHelper.WINDOW_TITLE
    }

    private fun findInChildren(
        x11: X11,
        display: X11.Display,
        window: X11.Window,
    ): Long? {
        val root = X11.WindowByReference()
        val parent = X11.WindowByReference()
        val childrenPointer = PointerByReference()
        val childrenCount = IntByReference()
        if (x11.XQueryTree(display, window, root, parent, childrenPointer, childrenCount) == 0) {
            return null
        }
        var result: Long? = null
        if (childrenPointer.value != null && childrenCount.value > 0) {
            val children = childrenPointer.value.getLongArray(0, childrenCount.value)
            for (i in 0 until childrenCount.value) {
                val childWindow = X11.Window(children[i])
                result = findWindowByTitle(x11, display, childWindow)
                if (result != null) {
                    break
                }
            }
        }
        return result
    }
}
