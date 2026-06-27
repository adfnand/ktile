package com.adferdv.ktile.service

import kotlinx.coroutines.delay

class LayoutService {
    suspend fun getLayout(): String {
        delay(DELAY)
        return "layout"
    }

    companion object {
        private const val DELAY = 300L
    }
}
