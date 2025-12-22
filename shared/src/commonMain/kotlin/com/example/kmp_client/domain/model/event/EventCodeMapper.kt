package com.example.kmp_client.domain.model.event

import kotlin.math.roundToInt

object EventCodeMapper {
    private val eventDescriptions = mutableMapOf<EventCodePair, EventDescriptionInfo>()

    init {
        addMapping("43", "04", "Дверь не заперта")
        /* Коды событий */
    }

    private fun addMapping(codeOneHex: String, codeTwoHex: String, mainDesc: String, readerInfo: String? = null) {
        try {
            val codeOne = codeOneHex.toInt(16)
            val codeTwo = codeTwoHex.toInt(16)
            val key = EventCodePair(codeOne, codeTwo)
            val newInfo = EventDescriptionInfo(mainDesc, readerInfo)

            eventDescriptions[key] = newInfo
        } catch (_: NumberFormatException) {
            println("Некорректный hex code: code1=$codeOneHex, code2=$codeTwoHex")
        }
    }

    fun getEventInfo(codeOne: Int, codeTwo: Int): EventDescriptionInfo {
        val pair = EventCodePair(codeOne, codeTwo)
        var info = eventDescriptions[pair]

        if (info == null) {
            val mainDesc: String
            var reader: String? = null
            when (codeOne) {
                0x77 -> { mainDesc = "Осталось проходов $codeTwo"; reader = "Вход" }
                0x66 -> { mainDesc = "Осталось проходов $codeTwo"; reader = "Выход" }
                0x9F -> { mainDesc = "Подтверждение доступа (разрешено по термодатчику, T=${formatTemperature(codeTwo)})"; reader = "Вход" }
                0xAF -> { mainDesc = "Подтверждение доступа (разрешено по термодатчику, T=${formatTemperature(codeTwo)})"; reader = "Выход" }
                0xBF -> { mainDesc = "Подтверждение доступа (отказ по термодатчику, T=${formatTemperature(codeTwo)})"; reader = "Вход" }
                0xDF -> { mainDesc = "Подтверждение доступа (отказ по термодатчику, T=${formatTemperature(codeTwo)})"; reader = "Выход" }
                else -> {
                    mainDesc = "Неизвестное событие (Коды: $codeOne [0x${codeOne.toString(16).uppercase()}], $codeTwo [0x${codeTwo.toString(16).uppercase()}])"
                }
            }
            info = EventDescriptionInfo(mainDesc, reader)
        }

        return info
    }

    private fun formatTemperature(rawValue: Int): String {
        val temperature = 25.0 + (rawValue.toDouble() / 10.0)
        return ((temperature * 10).roundToInt() / 10.0)
            .toString().let { if (it.endsWith(".0")) it.dropLast(2) else it }
    }
}