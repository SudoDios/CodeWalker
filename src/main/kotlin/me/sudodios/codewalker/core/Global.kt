package me.sudodios.codewalker.core

import androidx.compose.runtime.mutableStateOf

object Global {

    val userHome : String get() = System.getProperty("user.home")
    val LIB_CORE_PATH = "${userHome}/.codeWalker"
    val DB_CORE_PATH = "${userHome}/.codeWalker"

    object Alert {
        private var loadingScopes = ArrayList<String>()
        var openLoading = mutableStateOf(false)
        fun showLoading (key : String) {
            if (!loadingScopes.contains(key)) {
                loadingScopes.add(key)
                openLoading.value = true
            }
        }
        fun hideLoading (key: String) {
            loadingScopes.remove(key)
            if (loadingScopes.isEmpty()) {
                openLoading.value = false
            }
        }
    }

}