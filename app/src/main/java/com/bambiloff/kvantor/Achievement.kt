package com.bambiloff.kvantor

import java.util.Date

data class Achievement(
    val id: String = "",
    val unlocked: Boolean = false,
    val unlockedAt: Date? = null
)
