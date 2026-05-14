package com.nammahasiru.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TreeRepository(context: Context) {

    private val dao = TreeDatabase.build(context.applicationContext).treeDao()

    fun observeTrees(): Flow<List<TreeEntity>> = dao.observeAll()

    fun observeTree(id: Long): Flow<TreeEntity?> = dao.observeById(id)

    suspend fun insert(tree: TreeEntity): Long = dao.insert(tree)

    suspend fun update(tree: TreeEntity) = dao.update(tree)

    suspend fun getTree(id: Long): TreeEntity? = dao.getById(id)

    suspend fun getAllOnce(): List<TreeEntity> = dao.getAllOnce()

    fun observeSurvivalForVillage(villageTag: String): Flow<SurvivalSnapshot> {
        return dao.observeAll().map { trees ->
            aggregateSurvival(trees, villageTag)
        }
    }

    fun observeSpeciesGuide(): Flow<List<SpeciesGuideRow>> {
        return dao.observeAll().map { trees -> buildSpeciesGuide(trees) }
    }

    companion object {

        private data class SpeciesFact(
            val scientificName: String,
            val preferredSoil: String,
            val waterNeed: String,
            val benefits: String,
        )

        private val speciesFacts = mapOf(
            "neem" to SpeciesFact(
                scientificName = "Azadirachta indica",
                preferredSoil = "Well-drained loamy to sandy soil",
                waterNeed = "Low once established",
                benefits = "Shade, medicinal value, drought tolerant",
            ),
            "pongamia" to SpeciesFact(
                scientificName = "Millettia pinnata",
                preferredSoil = "Red soil and black cotton soil",
                waterNeed = "Moderate in first year",
                benefits = "Nitrogen fixation and soil restoration",
            ),
            "peepal" to SpeciesFact(
                scientificName = "Ficus religiosa",
                preferredSoil = "Moist, fertile, well-drained soil",
                waterNeed = "Moderate",
                benefits = "High canopy cover and biodiversity support",
            ),
            "banyan" to SpeciesFact(
                scientificName = "Ficus benghalensis",
                preferredSoil = "Deep loamy soil",
                waterNeed = "Moderate",
                benefits = "Long-lived shade tree with habitat value",
            ),
            "amla" to SpeciesFact(
                scientificName = "Phyllanthus emblica",
                preferredSoil = "Light to medium loamy soil",
                waterNeed = "Moderate",
                benefits = "Fruit, nutrition, and medicinal use",
            ),
            "mango" to SpeciesFact(
                scientificName = "Mangifera indica",
                preferredSoil = "Well-drained alluvial/loamy soil",
                waterNeed = "Moderate in growth phase",
                benefits = "Fruit yield and canopy cooling",
            ),
            "jamun" to SpeciesFact(
                scientificName = "Syzygium cumini",
                preferredSoil = "Moist soil near water bodies",
                waterNeed = "Moderate to high",
                benefits = "Fruit, pollinator support, erosion control",
            ),
            "gulmohar" to SpeciesFact(
                scientificName = "Delonix regia",
                preferredSoil = "Well-drained sandy loam",
                waterNeed = "Moderate",
                benefits = "Fast shade growth and urban aesthetics",
            ),
            "ashoka" to SpeciesFact(
                scientificName = "Saraca asoca",
                preferredSoil = "Moist fertile soil",
                waterNeed = "Moderate",
                benefits = "Ornamental, medicinal, avenue planting",
            ),
            "teak" to SpeciesFact(
                scientificName = "Tectona grandis",
                preferredSoil = "Deep, well-drained, slightly acidic soil",
                waterNeed = "Moderate",
                benefits = "Timber value and long-term carbon storage",
            ),
        )

        private fun normalizeSpeciesName(name: String): String {
            return name.trim().lowercase()
        }

        private fun speciesFactFor(name: String): SpeciesFact {
            return speciesFacts[normalizeSpeciesName(name)] ?: SpeciesFact(
                scientificName = "Scientific name not cataloged",
                preferredSoil = "Use locality survival data for soil suitability",
                waterNeed = "Regular watering in first 3 months",
                benefits = "Native tree plantations improve carbon sequestration",
            )
        }

        fun aggregateSurvival(trees: List<TreeEntity>, villageTag: String): SurvivalSnapshot {
            val normalized = villageTag.trim().lowercase()
            val subset = if (normalized.isBlank()) {
                trees
            } else {
                trees.filter { it.villageTag.trim().lowercase() == normalized }
            }
            val checked = subset.filter {
                val s = TreeStatus.fromStorage(it.status)
                s == TreeStatus.SPROUTED || s == TreeStatus.DIED
            }
            val sprouted = checked.count { TreeStatus.fromStorage(it.status) == TreeStatus.SPROUTED }
            val died = checked.count { TreeStatus.fromStorage(it.status) == TreeStatus.DIED }
            val pct = when {
                checked.isEmpty() -> null
                else -> ((sprouted * 100f) / checked.size).toInt().coerceIn(0, 100)
            }
            val label = if (normalized.isBlank()) "All communities" else villageTag.trim()
            return SurvivalSnapshot(label, checked.size, sprouted, died, pct)
        }

        fun buildSpeciesGuide(trees: List<TreeEntity>): List<SpeciesGuideRow> {
            data class Key(val village: String, val species: String)
            val map = mutableMapOf<Key, Pair<Int, Int>>()
            for (t in trees) {
                val status = TreeStatus.fromStorage(t.status)
                if (status != TreeStatus.SPROUTED && status != TreeStatus.DIED) continue
                val key = Key(t.villageTag.trim().lowercase(), t.speciesName.trim().lowercase())
                val p = map.getOrDefault(key, 0 to 0)
                val spr = p.first + if (status == TreeStatus.SPROUTED) 1 else 0
                val die = p.second + if (status == TreeStatus.DIED) 1 else 0
                map[key] = spr to die
            }
            return map.map { (k, v) ->
                val total = v.first + v.second
                val pct = if (total == 0) null else ((v.first * 100f) / total).toInt()
                val speciesLabel = k.species.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                val fact = speciesFactFor(speciesLabel)
                SpeciesGuideRow(
                    villageTag = k.village.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                    speciesName = speciesLabel,
                    sprouted = v.first,
                    died = v.second,
                    successPercent = pct,
                    scientificName = fact.scientificName,
                    preferredSoil = fact.preferredSoil,
                    waterNeed = fact.waterNeed,
                    benefits = fact.benefits,
                )
            }.sortedWith(
                compareByDescending<SpeciesGuideRow> { it.successPercent ?: -1 }
                    .thenByDescending { it.sprouted + it.died }
            )
        }
    }
}
