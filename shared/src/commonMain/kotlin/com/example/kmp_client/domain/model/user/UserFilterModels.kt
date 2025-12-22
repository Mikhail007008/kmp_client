package com.example.kmp_client.domain.model.user

import com.example.kmp_client.domain.model.filter.FieldFilterState
import com.example.kmp_client.domain.model.filter.FilterOperator

data class UserFilterModels(
    val code: FieldFilterState = FieldFilterState(
        fieldName = "code",
        displayName = "Табельный номер",
        availableOperators = FilterOperator.stringComparison()
    ),
    val name: FieldFilterState = FieldFilterState(
        fieldName = "name",
        displayName = "Имя",
        availableOperators = FilterOperator.stringOperators()
    ),
    val surname: FieldFilterState = FieldFilterState(
        fieldName = "surname",
        displayName = "Фамилия",
        availableOperators = FilterOperator.stringOperators()
    ),
    val patronymic: FieldFilterState = FieldFilterState(
        fieldName = "patronymic",
        displayName = "Отчество",
        availableOperators = FilterOperator.stringOperators()
    ),
    val jobTitleId: FieldFilterState = FieldFilterState(
        fieldName = "jobTitleId",
        displayName = "Должность",
        selectedOperator = FilterOperator.EQ,
        availableOperators = FilterOperator.idOperators()
    ),
    val departmentId: FieldFilterState = FieldFilterState(
        fieldName = "departmentId",
        displayName = "Подразделение",
        selectedOperator = FilterOperator.EQ,
        availableOperators = FilterOperator.idOperators()
    ),
    val phone: FieldFilterState = FieldFilterState(
        fieldName = "phone",
        displayName = "Телефон"
    ),
    val email: FieldFilterState = FieldFilterState(
        fieldName = "email",
        displayName = "Email",
        availableOperators = FilterOperator.stringOperators()
    ),
    val balance: FieldFilterState = FieldFilterState(
        fieldName = "balance",
        displayName = "Баланс",
        selectedOperator = FilterOperator.EQ,
        availableOperators = FilterOperator.comparableOperators()
    ),
    val guest: FieldFilterState = FieldFilterState(
        fieldName = "guest",
        displayName = "Гость",
        selectedOperator = FilterOperator.EQ,
        availableOperators = listOf(FilterOperator.EQ)
    ),
    val photo: FieldFilterState = FieldFilterState(
        fieldName = "photo",
        displayName = "Фото",
        selectedOperator = FilterOperator.EQ,
        availableOperators = listOf(FilterOperator.EQ)
    ),
    val creationTime: FieldFilterState = FieldFilterState(
        fieldName = "creationTime",
        displayName = "Дата создания",
        selectedOperator = FilterOperator.LESS_THAN_OR_EQUAL,
        availableOperators = FilterOperator.dateOperators()
    ),
    val featuresFilters: List<FieldFilterState> = emptyList(),
) {
    val allStandardFieldsStates: List<FieldFilterState>
        get() = listOf(
            code, name, surname, patronymic, jobTitleId, departmentId, phone, email, balance,
            guest, photo, creationTime
        ) + featuresFilters

    fun toQueryMap(): Map<String, String> {
        val queryMap = mutableMapOf<String, String>()

        allStandardFieldsStates.forEach { fieldState ->
            if (fieldState.isEnabled && fieldState.value.isNotBlank() && fieldState.errorMessage == null) {
                val isNumericField =
                    fieldState.availableOperators == FilterOperator.comparableOperators() || fieldState.availableOperators == FilterOperator.idOperators()
                val isValidNumericValue =
                    fieldState.value.toDoubleOrNull() != null || fieldState.value.toLongOrNull() != null
                val alwaysIncludedFields = setOf("guest", "photo", "creationTime")

                if (!isNumericField || isValidNumericValue || fieldState.fieldName in alwaysIncludedFields) {
                    val key =
                        "filters[${fieldState.fieldName}][${fieldState.selectedOperator.operator}]"
                    val value = fieldState.value
                    queryMap[key] = value
                }
            }
        }
        return queryMap
    }

    fun hasActiveFilters(): Boolean {
        return allStandardFieldsStates.any { it.isEnabled && it.value.isNotBlank() && it.errorMessage == null }
    }
}