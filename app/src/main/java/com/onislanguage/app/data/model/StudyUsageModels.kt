package com.onislanguage.app.data.model

data class StudyUsageEvent(
    val featureKey: String,
    val featureLabel: String,
    val createdAt: Long
)

data class StudyUsageFeatureCount(
    val featureKey: String,
    val featureLabel: String,
    val count: Int
)

data class StudyUsageDaySummary(
    val dateKey: String,
    val displayDate: String,
    val totalCount: Int,
    val featureCounts: List<StudyUsageFeatureCount>
)

data class StudyUsageChartPoint(
    val label: String,
    val totalCount: Int
)

data class StudyUsageOverview(
    val thisWeekTotal: Int = 0,
    val lastWeekTotal: Int = 0,
    val weeklyDeltaPercent: Int? = null,
    val chartPoints: List<StudyUsageChartPoint> = emptyList(),
    val days: List<StudyUsageDaySummary> = emptyList()
)
