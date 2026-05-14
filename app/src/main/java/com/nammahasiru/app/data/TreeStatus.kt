package com.nammahasiru.app.data

enum class TreeStatus(val storageKey: String) {
    PLANTED("PLANTED"),
    SPROUTED("SPROUTED"),
    DIED("DIED");

    companion object {
        fun fromStorage(value: String): TreeStatus =
            entries.firstOrNull { it.storageKey == value } ?: PLANTED
    }
}
