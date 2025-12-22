package com.example.kmp_client.domain.model.event

data class EventCodePair(val code1: Int, val code2: Int)

enum class EventTypeFilter(val displayName: String, val codes: List<EventCodePair>) {
    ALL("Все события", emptyList()),
    ACCESS_GRANTED(
        "Доступ разрешен",
        listOf(EventCodePair(code1 = 0, code2 = 2), EventCodePair(code1 = 0, code2 = 4))
    ),
    PASSAGE(
        "Проход",
        listOf(EventCodePair(code1 = 0, code2 = 3), EventCodePair(code1 = 0, code2 = 5))
    ),
    UNKNOWN_KEY(
        "Неизвестный ключ",
        listOf(EventCodePair(code1 = 2, code2 = 2), EventCodePair(code1 = 2, code2 = 4))
    ),
    ACCESS_DENIED(
        "Отказ в доступе", listOf(
            EventCodePair(code1 = 1, code2 = 2), EventCodePair(code1 = 8, code2 = 2),
            EventCodePair(code1 = 5, code2 = 2), EventCodePair(code1 = 6, code2 = 2),
            EventCodePair(code1 = 7, code2 = 2), EventCodePair(code1 = 1, code2 = 4),
            EventCodePair(code1 = 8, code2 = 4), EventCodePair(code1 = 5, code2 = 4),
            EventCodePair(code1 = 6, code2 = 4), EventCodePair(code1 = 7, code2 = 4),
            EventCodePair(code1 = 111, code2 = 111),
            EventCodePair(code1 = 32, code2 = 2),
            EventCodePair(code1 = 32, code2 = 4)
        )
    ),
    HACK_ATTEMPT(
        "ВЗЛОМ", listOf(
            EventCodePair(code1 = 0, code2 = 1),
            EventCodePair(code1 = 11, code2 = 2),
            EventCodePair(code1 = 11, code2 = 4)
        )
    ),
    BUTTON_PRESSED(
        "Нажата кнопка", listOf(
            EventCodePair(code1 = 0, code2 = 8),
            EventCodePair(code1 = 10, code2 = 8),
            EventCodePair(code1 = 12, code2 = 2),
            EventCodePair(code1 = 12, code2 = 4),
            EventCodePair(code1 = 177, code2 = 2),
            EventCodePair(code1 = 177, code2 = 4)
        )
    ),
    REMOTE_OPEN(
        "Открытие с пульта", listOf(
            EventCodePair(code1 = 9, code2 = 9),
            EventCodePair(code1 = 9, code2 = 1),
            EventCodePair(code1 = 9, code2 = 2)
        )
    );

    companion object {
        fun fromDisplayName(displayName: String): EventTypeFilter? {
            return entries.find { it.displayName == displayName }
        }
    }
}