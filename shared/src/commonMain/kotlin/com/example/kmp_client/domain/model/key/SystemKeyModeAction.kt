package com.example.kmp_client.domain.model.key

enum class SystemKeyModeAction(val apiValue: Int, val displayName: String) {
    CONTROL_OPEN(1, "Контроль/Открыто"),
    CONTROL_CLOSED(2, "Контроль/Закрыто"),
    OPEN(3, "Открыто"),
    CLOSED(4, "Закрыто"),
    CONTROL(5, "Контроль"),
    ARM_DISARM(6, "Постановка/Снятие с охраны"),
    ARM(7, "Постановка на охрану"),
    DISARM(8, "Снятие с охраны"),
    ARM_CLOSED_DISARM_CONTROL(9, "Постановка (+Закрыто) / Снятие (+Контроль)"),
    ARM_CLOSED(10, "Постановка (+Закрыто) на охрану"),
    DISARM_CONTROL(11, "Снятие (+Контроль) с охраны"),
    NOT_SET(12, "");

    companion object {
        fun fromApiValue(value: Int?): SystemKeyModeAction {
            return entries.find { it.apiValue == value } ?: NOT_SET
        }
    }
}