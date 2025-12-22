package com.example.kmp_client.data.remote.dto.mapper

import com.example.kmp_client.data.remote.dto.response.AccessEntryDto
import com.example.kmp_client.data.remote.dto.response.AccessSchemeDTO
import com.example.kmp_client.data.remote.dto.response.AccessSchemeInUserAccessDto
import com.example.kmp_client.data.remote.dto.response.ScheduleDto
import com.example.kmp_client.data.remote.dto.response.UserAccessInfoResponse
import com.example.kmp_client.domain.model.accesssheme.AccessScheme
import com.example.kmp_client.domain.model.useraccess.UserAccessDetails
import com.example.kmp_client.domain.model.useraccess.UserAccessKeyDetail
import com.example.kmp_client.domain.model.useraccess.UserAccessSchemeDetail
import com.example.kmp_client.presentation.screen.users.EditableSchemeUiState
import com.example.kmp_client.domain.model.accesssheme.AccessScheme as DomainAccessScheme

fun AccessSchemeDTO.toDomain(): AccessScheme {
    return AccessScheme(
        id = this.id,
        name = this.name,
        devices = this.devices,
        defaultForNewEmployees = this.defaultForNewEmployees,
        defaultForNewGuests = this.defaultForNewGuests
    )
}

fun DomainAccessScheme.toDTO(): AccessSchemeDTO {
    return AccessSchemeDTO(
        id = this.id,
        name = this.name,
        devices = this.devices,
        defaultForNewEmployees = this.defaultForNewEmployees,
        defaultForNewGuests = this.defaultForNewGuests
    )
}

fun UserAccessInfoResponse.toDomain(allSchedules: List<ScheduleDto>): UserAccessDetails {
    return UserAccessDetails(
        keys = this.accessEntries.map { it.toDomain(allSchedules) }
    )
}

fun AccessEntryDto.toDomain(allSchedules: List<ScheduleDto>): UserAccessKeyDetail {
    return UserAccessKeyDetail(
        key = this.key,
        controlKey = this.controlKey,
        accessSchemes = this.accessSchemes.map { it.toDomain(allSchedules) }
    )
}

fun AccessSchemeInUserAccessDto.toDomain(allSchedules: List<ScheduleDto>): UserAccessSchemeDetail {

    val currentSchedule = if (this.scheduleCurrentYear == 0L || this.scheduleCurrentYear == null) null else this.scheduleCurrentYear.let { csId ->
        allSchedules.find { it.id == csId }
    }
    val nextSchedule = if (this.scheduleNextYear == 0L || this.scheduleNextYear == null) null else this.scheduleNextYear.let { nsId ->
        allSchedules.find { it.id == nsId }
    }

    val isCurrentAnyOrZeroOrNull = this.anytime || this.scheduleCurrentYear == 0L || this.scheduleCurrentYear == null
    val finalCurrentScheduleId = if (isCurrentAnyOrZeroOrNull) null else this.scheduleCurrentYear


    val isNextAnyOrZeroOrNull = this.anytime || this.scheduleNextYear == 0L || this.scheduleNextYear == null
    val finalNextScheduleId = if (isNextAnyOrZeroOrNull) null else this.scheduleNextYear

    val result = UserAccessSchemeDetail(
        id = this.id,
        name = this.name,
        anytime = this.anytime,
        currentYearScheduleId = finalCurrentScheduleId,
        currentYearScheduleName = if (this.anytime || finalCurrentScheduleId == null) null else currentSchedule?.let { "[${it.year}] ${it.name}" },
        nextYearScheduleId = finalNextScheduleId,
        nextYearScheduleName = if (this.anytime || finalCurrentScheduleId == null || finalNextScheduleId == null) null else nextSchedule?.let { "[${it.year}] ${it.name}" }
    )
    return result
}

fun EditableSchemeUiState.toDomain(allSchedulesFromDialog: List<ScheduleDto>): UserAccessSchemeDetail? {
    if (isNewlyAdded && selectedSchemeId == null) return null
    if (selectedSchemeId == null || schemeName.isNullOrBlank()) return null

    val domainCurrentScheduleId = if (isAnytime) null else this.selectedCurrentScheduleId
    val domainCurrentScheduleName = if (isAnytime || domainCurrentScheduleId == null) {
        null
    } else {
        allSchedulesFromDialog.find { it.id == domainCurrentScheduleId }?.let { schedule ->
            "[${schedule.year}] ${schedule.name}"
        }
    }

    val domainNextScheduleId = if (isAnytime || domainCurrentScheduleId == null) null else this.selectedNextScheduleId
    val domainNextScheduleName = if (isAnytime || domainCurrentScheduleId == null || domainNextScheduleId == null) {
        null
    } else {
        allSchedulesFromDialog.find { it.id == domainNextScheduleId }?.let { schedule ->
            "[${schedule.year}] ${schedule.name}"
        }
    }

    return UserAccessSchemeDetail(
        id = selectedSchemeId!!,
        name = schemeName!!,
        anytime = isAnytime,
        currentYearScheduleId = domainCurrentScheduleId,
        currentYearScheduleName = domainCurrentScheduleName,
        nextYearScheduleId = domainNextScheduleId,
        nextYearScheduleName = domainNextScheduleName
    )
}