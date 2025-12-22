package com.example.kmp_client.domain.model.filter

enum class FilterOperator(val operator: String, val displayName: String, val symbol: String) {
    EQ("\$eq", "Равно", "= "),
    NOT_EQ("\$ne", "Не равно", "!= "),
    EQUALS("\$equals", "Совпадает", "= "),
    NOT_EQUALS("\$notEquals", "Не совпадает", "!= "),
    CONTAINS("\$contains", "Содержит", "<> "),
    NOT_CONTAINS("\$notContains", "Не содержит", "<!> "),
    STARTS_WITH("\$startsWith", "Начинается с", "_* "),
    ENDS_WITH("\$endsWith", "Заканчивается на", "*_ "),
    GREATER_THAN("\$gt", "Больше", "> "),
    GREATER_THAN_OR_EQUAL("\$gte", "Больше или равно", ">= "),
    LESS_THAN("\$lt", "Меньше", "< "),
    LESS_THAN_OR_EQUAL("\$lte", "Меньше или равно", "<= ");

    companion object {
        fun stringOperators() =
            listOf(EQUALS, NOT_EQUALS, CONTAINS, NOT_CONTAINS, STARTS_WITH, ENDS_WITH)

        fun stringComparison() = listOf(EQUALS, NOT_EQUALS)

        fun comparableOperators() = listOf(
            EQ,
            NOT_EQ,
            GREATER_THAN,
            GREATER_THAN_OR_EQUAL,
            LESS_THAN,
            LESS_THAN_OR_EQUAL
        )

        fun dateOperators() = listOf(
            GREATER_THAN,
            GREATER_THAN_OR_EQUAL,
            LESS_THAN,
            LESS_THAN_OR_EQUAL
        )

        fun idOperators() = listOf(EQ, NOT_EQ)
    }
}

data class FieldFilterState(
    val fieldName: String,
    val displayName: String,
    val isEnabled: Boolean = false,
    val selectedOperator: FilterOperator = FilterOperator.EQUALS,
    val value: String = "",
    val availableOperators: List<FilterOperator> = FilterOperator.stringOperators(),
    val errorMessage: String? = null
)