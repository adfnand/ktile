package com.adferdv.ktile.core.hotkey

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent

data class Hotkey(
    val keyCode: Int,
    val modifiers: Set<ModifierKey> = emptySet(),
) {
    companion object {
        val DEFAULT_TOGGLE =
            Hotkey(
                keyCode = NativeKeyEvent.VC_K,
                modifiers = setOf(ModifierKey.SUPER),
            )
    }
}
