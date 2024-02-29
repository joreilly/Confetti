package dev.johnoreilly.confetti.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime

class JvmDateService: DateService {
    override fun now(): LocalDateTime = java.time.LocalDateTime.now().toKotlinLocalDateTime()
}