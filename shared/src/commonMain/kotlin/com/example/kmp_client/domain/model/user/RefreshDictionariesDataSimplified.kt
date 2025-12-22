package com.example.kmp_client.domain.model.user

import com.example.kmp_client.data.remote.dto.response.Department
import com.example.kmp_client.data.remote.dto.response.JobTitle
import com.example.kmp_client.data.remote.dto.response.ScheduleDto

data class RefreshDictionariesDataSimplified(
    val jobTitles: List<JobTitle>,
    val departments: List<Department>,
    val schedules: List<ScheduleDto>
)