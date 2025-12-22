package com.example.kmp_client.data.remote.dto.mapper

import com.example.kmp_client.data.remote.dto.response.EventsDto
import com.example.kmp_client.domain.model.event.Event
import com.example.kmp_client.domain.model.event.EventCodeMapper
import com.example.kmp_client.domain.util.DateTimeFormatters
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime

fun EventsDto.toDomain(devicesMap: Map<String, String>): Event {
    val parsedDateTime = try {
        LocalDateTime.parse(this.dateTime)
    } catch (_: Exception) {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    }

    val formattedDateTime = try {
        parsedDateTime.format(DateTimeFormatters.displayDateTimeFormatterWithSeconds)
    } catch (_: Exception) {
        this.dateTime
    }

    val deviceName = devicesMap[this.device] ?: this.device
    val eventInfo = EventCodeMapper.getEventInfo(this.code1, this.code2)
    val eventDescription = eventInfo.mainDescription
    val readerDescription = eventInfo.readerInfo ?: ""

    return Event(
        id = this.id,
        originalDateTime = parsedDateTime,
        formattedDateTime = formattedDateTime,
        deviceName = deviceName,
        eventDescription = eventDescription,
        readerDescription = readerDescription,
        userFullName = this.fullName?.takeIf { it.isNotBlank() } ?: "",
        keyNumber = this.key.takeIf { it.isNotBlank() } ?: ""
    )
}