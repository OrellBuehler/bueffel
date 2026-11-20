package ch.orellbuehler.bueffel.study

import ch.orellbuehler.bueffel.format.Item
import ch.orellbuehler.bueffel.format.StudyOrder
import kotlin.random.Random

internal data class FacetKey(
    val itemId: String,
    val facet: Facet,
)

internal class LoadedPack(
    val packId: String,
    val context: PackContext,
    val schedules: Map<FacetKey, Schedule>,
) {
    fun scheduleOf(
        item: Item,
        facet: Facet,
    ): Schedule = schedules[FacetKey(item.id, facet)] ?: Schedule.fresh()
}

internal class ResolvedConfig(
    val mode: SessionMode,
    val sectionId: String?,
    val size: Int,
    val newLimit: Int,
    val filter: PracticeFilter,
    val reschedule: Boolean,
) {
    companion object {
        fun resolve(
            config: SessionConfig,
            defaultSize: Int,
            packNewPerSession: Int,
        ): ResolvedConfig =
            ResolvedConfig(
                mode = config.mode,
                sectionId = config.sectionId,
                size = if (config.size > 0) config.size else defaultSize,
                newLimit = if (config.newLimit >= 0) config.newLimit else packNewPerSession,
                filter = if (config.mode == SessionMode.PRACTICE) config.filter else PracticeFilter.ALL,
                reschedule = config.mode == SessionMode.LEARN || config.rescheduleInPractice,
            )
    }
}

internal class PlanEntry(
    val item: Item,
    val facet: Facet,
    val schedule: Schedule,
    val type: ExerciseType,
)

internal class Plan(
    val entries: List<PlanEntry>,
    val dueCount: Int,
    val newCount: Int,
)

internal object Planner {
    fun plan(
        pack: LoadedPack,
        config: ResolvedConfig,
        cutoffs: DueCutoffs,
        random: Random,
    ): Plan {
        val eligible = pack.context.items.filter { config.sectionId == null || (it.section ?: "") == config.sectionId }
        return if (config.mode ==
            SessionMode.LEARN
        ) {
            planLearn(pack, eligible, config, cutoffs, random)
        } else {
            planPractice(pack, eligible, config, random)
        }
    }

    private fun entry(
        pack: LoadedPack,
        item: Item,
        facet: Facet,
        filter: PracticeFilter,
    ): PlanEntry? {
        val schedule = pack.scheduleOf(item, facet)
        val type = ExerciseFactory.chooseType(pack.context, item, facet, schedule, filter) ?: return null
        return PlanEntry(item, facet, schedule, type)
    }

    private fun planLearn(
        pack: LoadedPack,
        eligible: List<Item>,
        config: ResolvedConfig,
        cutoffs: DueCutoffs,
        random: Random,
    ): Plan {
        val used = HashSet<String>()
        val entries = ArrayList<PlanEntry>()
        val due =
            eligible
                .flatMapIndexed { position, item ->
                    pack.context.facetsOf(item).mapNotNull { facet ->
                        val schedule = pack.scheduleOf(item, facet)
                        if (cutoffs.isDue(schedule)) Triple(position, item to facet, schedule.dueAt) else null
                    }
                }.sortedWith(compareBy({ it.third }, { it.first }))
        for ((_, itemFacet, _) in due) {
            if (entries.size >= config.size) break
            val (item, facet) = itemFacet
            if (!used.add(item.id)) continue
            entry(pack, item, facet, PracticeFilter.ALL)?.let { entries += it }
        }
        val dueCount = entries.size
        val newCandidates = if (pack.context.study.order == StudyOrder.RANDOM) eligible.shuffled(random) else eligible
        for (item in newCandidates) {
            if (entries.size - dueCount >= config.newLimit || entries.size >= config.size) break
            if (item.id in used) continue
            val facets = pack.context.facetsOf(item)
            val facet =
                facets.firstOrNull { f ->
                    pack.scheduleOf(item, f).state == LearningState.NEW &&
                        (f == Facet.FORWARD || pack.scheduleOf(item, Facet.FORWARD).state != LearningState.NEW)
                } ?: continue
            used += item.id
            entry(pack, item, facet, PracticeFilter.ALL)?.let { entries += it }
        }
        return Plan(entries, dueCount, entries.size - dueCount)
    }

    private fun planPractice(
        pack: LoadedPack,
        eligible: List<Item>,
        config: ResolvedConfig,
        random: Random,
    ): Plan {
        val entries = ArrayList<PlanEntry>()
        for (item in eligible.shuffled(random)) {
            if (entries.size >= config.size) break
            for (facet in pack.context.facetsOf(item).shuffled(random)) {
                val planned = entry(pack, item, facet, config.filter)
                if (planned != null) {
                    entries += planned
                    break
                }
            }
        }
        return Plan(entries, 0, 0)
    }
}
