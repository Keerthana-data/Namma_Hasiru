package com.nammahasiru.app.data

data class SurvivalSnapshot(
    val villageLabel: String,
    val checkedCount: Int,
    val sproutedCount: Int,
    val diedCount: Int,
    val survivalPercent: Int?,
)

data class SpeciesGuideRow(
    val villageTag: String,
    val speciesName: String,
    val sprouted: Int,
    val died: Int,
    val successPercent: Int?,
    val scientificName: String,
    val preferredSoil: String,
    val waterNeed: String,
    val benefits: String,
)
