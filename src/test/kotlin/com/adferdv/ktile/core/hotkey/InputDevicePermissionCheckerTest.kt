package com.adferdv.ktile.core.hotkey

import io.kotest.matchers.shouldBe
import org.junit.Assume
import org.junit.Test

class InputDevicePermissionCheckerTest {
    @Test
    fun `on Linux with input access returns true`() {
        Assume.assumeTrue(System.getProperty("os.name").lowercase().contains("linux"))
        // This test depends on the CI/user environment. It is informational.
        InputDevicePermissionChecker.hasInputDeviceAccess() shouldBe true
    }
}
