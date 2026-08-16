package com.adferdv.ktile.core.hotkey

import com.adferdv.ktile.core.hotkey.KtileHotkeyNative.KtileHotkeyCallback
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.sun.jna.Pointer
import javax.swing.SwingUtilities

/**
 * [GlobalHotkeyProvider] backed by the Rust `kbd-global` / evdev library.
 *
 * Works on Linux regardless of the display server (Wayland, X11, TTY).
 * Requires the user to have access to `/dev/input/event*` devices.
 */
class LinuxEvdevHotkeyProvider : GlobalHotkeyProvider {
    private var manager: Pointer? = null
    private var callback: KtileHotkeyCallback? = null

    init {
        val handle = KtileHotkeyNative.INSTANCE.ktile_hotkey_init()
        check(handle != Pointer.NULL) { "Failed to initialize ktile_hotkey native library" }
        manager = handle
    }

    override fun register(
        hotkey: Hotkey,
        callback: () -> Unit,
    ) {
        val currentManager = manager ?: error("Hotkey provider has been shut down")
        val nativeCallback =
            object : KtileHotkeyCallback {
                override fun invoke() {
                    System.err.println("LinuxEvdevHotkeyProvider: native callback fired")
                    SwingUtilities.invokeLater { callback.invoke() }
                }
            }
        this.callback = nativeCallback

        val combo = hotkey.toKbdGlobalString()
        val result = KtileHotkeyNative.INSTANCE.ktile_hotkey_register(currentManager, combo, nativeCallback)
        check(result == 0) { "Failed to register hotkey '$combo' (error $result)" }
    }

    override fun unregister(hotkey: Hotkey) {
        shutdown()
    }

    override fun dispose() {
        shutdown()
    }

    private fun shutdown() {
        manager?.let { KtileHotkeyNative.INSTANCE.ktile_hotkey_shutdown(it) }
        manager = null
        callback = null
    }
}

internal fun Hotkey.toKbdGlobalString(): String {
    val modifierNames =
        modifiers
            .map { modifier ->
                when (modifier) {
                    ModifierKey.SHIFT -> "Shift"
                    ModifierKey.CTRL -> "Ctrl"
                    ModifierKey.ALT -> "Alt"
                    ModifierKey.SUPER -> "Super"
                }
            }.sorted()
    val keyName = keyCode.toKbdGlobalKeyName()
    return if (modifierNames.isEmpty()) keyName else (modifierNames + keyName).joinToString("+")
}

internal val JNA_TO_KBD_KEY_NAME: Map<Int, String> =
    mapOf(
        NativeKeyEvent.VC_A to "A",
        NativeKeyEvent.VC_B to "B",
        NativeKeyEvent.VC_C to "C",
        NativeKeyEvent.VC_D to "D",
        NativeKeyEvent.VC_E to "E",
        NativeKeyEvent.VC_F to "F",
        NativeKeyEvent.VC_G to "G",
        NativeKeyEvent.VC_H to "H",
        NativeKeyEvent.VC_I to "I",
        NativeKeyEvent.VC_J to "J",
        NativeKeyEvent.VC_K to "K",
        NativeKeyEvent.VC_L to "L",
        NativeKeyEvent.VC_M to "M",
        NativeKeyEvent.VC_N to "N",
        NativeKeyEvent.VC_O to "O",
        NativeKeyEvent.VC_P to "P",
        NativeKeyEvent.VC_Q to "Q",
        NativeKeyEvent.VC_R to "R",
        NativeKeyEvent.VC_S to "S",
        NativeKeyEvent.VC_T to "T",
        NativeKeyEvent.VC_U to "U",
        NativeKeyEvent.VC_V to "V",
        NativeKeyEvent.VC_W to "W",
        NativeKeyEvent.VC_X to "X",
        NativeKeyEvent.VC_Y to "Y",
        NativeKeyEvent.VC_Z to "Z",
        NativeKeyEvent.VC_1 to "1",
        NativeKeyEvent.VC_2 to "2",
        NativeKeyEvent.VC_3 to "3",
        NativeKeyEvent.VC_4 to "4",
        NativeKeyEvent.VC_5 to "5",
        NativeKeyEvent.VC_6 to "6",
        NativeKeyEvent.VC_7 to "7",
        NativeKeyEvent.VC_8 to "8",
        NativeKeyEvent.VC_9 to "9",
        NativeKeyEvent.VC_0 to "0",
        NativeKeyEvent.VC_F1 to "F1",
        NativeKeyEvent.VC_F2 to "F2",
        NativeKeyEvent.VC_F3 to "F3",
        NativeKeyEvent.VC_F4 to "F4",
        NativeKeyEvent.VC_F5 to "F5",
        NativeKeyEvent.VC_F6 to "F6",
        NativeKeyEvent.VC_F7 to "F7",
        NativeKeyEvent.VC_F8 to "F8",
        NativeKeyEvent.VC_F9 to "F9",
        NativeKeyEvent.VC_F10 to "F10",
        NativeKeyEvent.VC_F11 to "F11",
        NativeKeyEvent.VC_F12 to "F12",
        NativeKeyEvent.VC_SPACE to "Space",
        NativeKeyEvent.VC_ENTER to "Enter",
        NativeKeyEvent.VC_BACKSPACE to "Backspace",
        NativeKeyEvent.VC_TAB to "Tab",
        NativeKeyEvent.VC_SLASH to "Slash",
    )

private fun Int.toKbdGlobalKeyName(): String = JNA_TO_KBD_KEY_NAME[this] ?: "Key$this"
