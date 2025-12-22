package com.example.kmp_client.domain.util

import com.example.kmp_client.domain.model.event.Event

object CsvGenerator {

    fun generateCsvBytes(events: List<Event>, maxRows: Int = 1000): ByteArray {
        return buildString {
            // Правильное отображение русского текста
            append(0xFEFF.toChar())
            val headers =
                listOf("Дата/Время", "Контроллер", "Событие", "Считыватель", "Пользователь", "Ключ")
            appendLine(createCsvRow(headers))

            val eventsToExport = events.take(maxRows - 1)
            for (event in eventsToExport) {
                val row = listOf(
                    event.formattedDateTime,
                    event.deviceName,
                    event.eventDescription,
                    event.readerDescription,
                    event.userFullName,
                    event.keyNumber
                )
                appendLine(createCsvRow(row))
            }
        }.encodeToByteArray()
    }

    private fun createCsvRow(values: List<String>): String {
        return values.joinToString(",") { value ->
            val cleanedValue = cleanValueForCsv(value)
            if (requiresCsvQuoting(cleanedValue)) {
                "\"${doubleQuoting(cleanedValue)}\""
            } else {
                cleanedValue
            }
        }
    }

    private fun cleanValueForCsv(value: String): String {
        return value.normalizeForCsv()
    }

    private fun requiresCsvQuoting(value: String): Boolean {
        return value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains(
            "\r"
        )
    }

    private fun doubleQuoting(value: String): String {
        return value.replace("\"", "\"\"")
    }

    private fun String.normalizeForCsv(): String {
        return this.replace("\r\n", "\n")
            .replace("\r", "\n")
            .trim()
    }
}