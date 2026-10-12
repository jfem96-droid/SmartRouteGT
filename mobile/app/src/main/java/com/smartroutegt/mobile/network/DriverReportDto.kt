package com.smartroutegt.mobile.network

data class DriverReportCreateDto(val tripId: Long?, val type: String, val severity: String, val description: String, val latitude: Double?, val longitude: Double?)
data class DriverReportDto(val id: Long, val type: String, val severity: String, val status: String, val description: String, val createdAt: String?, val resolution: String?)
