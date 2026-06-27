package com.adferdv.ktile.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.adferdv.ktile.service.LayoutService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class LayoutViewModel(
    private val service: LayoutService,
    private val coroutineScope: CoroutineScope,
) {
    private val count = mutableStateOf("")
    val layout: State<String> = count

    fun loadLayout() {
        coroutineScope.launch {
            val newValue = service.getLayout()
            count.value = newValue
        }
    }
}
