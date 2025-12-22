package com.example.kmp_client.domain.model.key

import com.example.kmp_client.domain.model.filter.FieldFilterState
import com.example.kmp_client.domain.model.filter.FilterOperator
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant

data class KeyFiltersModels(
    val uid: FieldFilterState = FieldFilterState(
        fieldName = "id",
        displayName = "UID ключа",
        selectedOperator = FilterOperator.EQ,
        availableOperators = FilterOperator.idOperators()
    ),
    val keyType: FieldFilterState = FieldFilterState(
        fieldName = "keyType",
        displayName = "Тип ключа",
        availableOperators = listOf(FilterOperator.EQ),
        selectedOperator = FilterOperator.EQ
    ),
    val expiration: FieldFilterState = FieldFilterState(
        fieldName = "expiration",
        displayName = "Срок действия до",
        availableOperators = FilterOperator.dateOperators(),
        selectedOperator = FilterOperator.LESS_THAN_OR_EQUAL
    ),
    val blocked: FieldFilterState = FieldFilterState(
        fieldName = "blocked",
        displayName = "Заблокирован",
        availableOperators = listOf(FilterOperator.EQ),
        selectedOperator = FilterOperator.EQ
    ),
    val description: FieldFilterState = FieldFilterState(
        fieldName = "description",
        displayName = "Комментарии",
        availableOperators = FilterOperator.stringOperators()
    )
) {
    val allFieldsStates: List<FieldFilterState>
        get() = listOf(uid, keyType, expiration, blocked, description)

    fun toQueryMap(): Map<String, String> {
        val queryMap = mutableMapOf<String, String>()

        if (uid.isEnabled && uid.value.isNotBlank() && uid.errorMessage == null) {
            queryMap["filters[${uid.fieldName}][${uid.selectedOperator.operator}]"] = uid.value
        }

        if (keyType.isEnabled && keyType.value.isNotBlank() && keyType.errorMessage == null) {
            when (KeyType.valueOf(keyType.value)) {
                KeyType.REGULAR -> {
                    queryMap["filters[guestKey][${FilterOperator.EQ.operator}]"] = "false"
                    queryMap["filters[systemKeyMode][${FilterOperator.EQ.operator}]"] = "0"
                }

                KeyType.GUEST -> {
                    queryMap["filters[guestKey][${FilterOperator.EQ.operator}]"] = "true"
                }

                KeyType.SYSTEM -> {
                    queryMap["filters[systemKeyMode][${FilterOperator.GREATER_THAN.operator}]"] =
                        "0"
                }
            }
        }

        if (expiration.isEnabled && expiration.value.isNotBlank() && expiration.errorMessage == null) {
            try {
                val selectedData = LocalDate.parse(expiration.value)
                val dateTimeWithAdjustedTime: LocalDateTime = when (expiration.selectedOperator) {
                    FilterOperator.GREATER_THAN -> selectedData.atTime(
                        LocalTime(
                            23,
                            59,
                            59,
                            999_000_000
                        )
                    )

                    FilterOperator.LESS_THAN -> selectedData.atTime(LocalTime(0, 0, 0, 0))
                    else -> selectedData.atTime(LocalTime(0, 0, 0, 0))
                }

                queryMap["filters[${expiration.fieldName}][${expiration.selectedOperator.operator}]"] =
                    dateTimeWithAdjustedTime.toInstant(TimeZone.UTC).toString()
            } catch (_: Exception) {
                null
            }
        }

        if (blocked.isEnabled && blocked.value.isNotBlank() && blocked.errorMessage == null) {
            queryMap["filters[${blocked.fieldName}][${blocked.selectedOperator.operator}]"] =
                blocked.value
        }

        if (description.isEnabled && description.value.isNotBlank() && description.errorMessage == null) {
            queryMap["filters[${description.fieldName}][${description.selectedOperator.operator}]"] = description.value
        }

        return queryMap
    }

    fun hasActiveFilters(): Boolean {
        return allFieldsStates.any { it.isEnabled && it.value.isNotBlank() && it.errorMessage == null }
    }
}