package com.adferdv.ktile.core.instance

import io.kotest.matchers.shouldBe
import org.junit.Test

class SingleInstanceLockTest {
    @Test
    fun `acquire succeeds when no other lock exists`() {
        val lock = SingleInstanceLock()
        lock.tryAcquire() shouldBe true
        lock.release()
    }

    @Test
    fun `second acquire fails while first lock is held`() {
        val first = SingleInstanceLock()
        val second = SingleInstanceLock()

        first.tryAcquire() shouldBe true
        second.tryAcquire() shouldBe false

        first.release()
    }

    @Test
    fun `reacquire succeeds after release`() {
        val lock = SingleInstanceLock()
        lock.tryAcquire() shouldBe true
        lock.release()
        lock.tryAcquire() shouldBe true
        lock.release()
    }
}
