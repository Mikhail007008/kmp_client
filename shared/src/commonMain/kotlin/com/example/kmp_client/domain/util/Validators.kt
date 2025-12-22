package com.example.kmp_client.domain.util

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

object Validators {
    fun maxLength(maxLength: Int): (String) -> String? = { value ->
        if (value.length > maxLength) "Макс. $maxLength симв" else null
    }

    fun phoneValidator(value: String): String? {
        if (value.isBlank()) return null
        if (value.length > 20) return "Макс. 20 симв"
        return null
    }

    fun balanceValidator(value: String): String? {
        if (value.isBlank()) return null
        val noSpacesValue = value.replace(" ", "")
        val regex = """^-?\d{1,6}(\.\d{1,2})?$""".toRegex()

        if (!regex.matches(noSpacesValue)) {
            val justDigits = noSpacesValue.replace(".", "").replace("-", "")
            if (justDigits.length > 8) return "Недопустимое значение"
            return "Недопустимое значение"
        }

        return null
    }
    fun dateDdMmYyValidator(value: String): String? {
        if (value.isBlank()) return null

        return try {
            val parsedDate = DateTimeFormatters.ddMMyyFormatter.parse(value)
            val today = Clock.System.todayIn(TimeZone.Companion.currentSystemDefault())

            if (parsedDate > today) {
                "Дата не может быть больше текущей"
            } else {
                null
            }
        } catch (_: Exception) {
            "Формат ДД.ММ.ГГГГ"
        }
    }
}

object DateTimeFormatters {
    val ddMMyyFormatter = LocalDate.Companion.Format {
        dayOfMonth()
        char('.')
        monthNumber()
        char('.')
        year()
    }

    @OptIn(FormatStringsInDatetimeFormats::class)
    val displayDateTimeFormatter by lazy {
        LocalDateTime.Format {
            byUnicodePattern("dd.MM.yyyy HH:mm")
        }
    }

    @OptIn(FormatStringsInDatetimeFormats::class)
    val displayDateTimeFormatterWithSeconds by lazy {
        LocalDateTime.Format {
            byUnicodePattern("dd.MM.yyyy HH:mm:ss")
        }
    }

    @OptIn(FormatStringsInDatetimeFormats::class)
    fun getCurrentDateForFilter(): String {
        val now = Clock.System.now()
        val todayAtStartOfDay = now.toLocalDateTime(TimeZone.currentSystemDefault())
            .date.atStartOfDayIn(TimeZone.currentSystemDefault())

        val localDateTime = todayAtStartOfDay.toLocalDateTime(TimeZone.UTC)

        val startOfDay = LocalDateTime(
            year = localDateTime.year,
            monthNumber = localDateTime.monthNumber,
            dayOfMonth = localDateTime.dayOfMonth,
            hour = 0,
            minute = 0,
            second = 0
        )

        val formatter = LocalDateTime.Format {
            byUnicodePattern("uuuu-MM-dd'T'HH:mm:ss.SSS")
        }
        return startOfDay.format(formatter)
    }
}