package ch.orellbuehler.bueffel

import app.cash.sqldelight.db.SqlDriver
import ch.orellbuehler.bueffel.db.BueffelDatabase
import ch.orellbuehler.bueffel.db.DatabaseDriverFactory
import ch.orellbuehler.bueffel.format.ImportError
import ch.orellbuehler.bueffel.format.ImportErrorCode
import ch.orellbuehler.bueffel.format.ItemType
import ch.orellbuehler.bueffel.format.PackReader
import ch.orellbuehler.bueffel.format.ReadResult
import ch.orellbuehler.bueffel.format.Study
import ch.orellbuehler.bueffel.samples.Samples
import ch.orellbuehler.bueffel.store.AppSettings
import ch.orellbuehler.bueffel.store.Env
import ch.orellbuehler.bueffel.store.ImportPreview
import ch.orellbuehler.bueffel.store.ImportResult
import ch.orellbuehler.bueffel.store.ItemSummary
import ch.orellbuehler.bueffel.store.PackDetail
import ch.orellbuehler.bueffel.store.PackStore
import ch.orellbuehler.bueffel.store.PackSummary
import ch.orellbuehler.bueffel.store.SamplePack
import ch.orellbuehler.bueffel.store.SettingsStore
import ch.orellbuehler.bueffel.store.Stats
import ch.orellbuehler.bueffel.store.StatsStore
import ch.orellbuehler.bueffel.store.ThemePreference
import ch.orellbuehler.bueffel.store.TypeCount
import ch.orellbuehler.bueffel.study.AttemptRecorder
import ch.orellbuehler.bueffel.study.LoadedPack
import ch.orellbuehler.bueffel.study.PackContext
import ch.orellbuehler.bueffel.study.Plan
import ch.orellbuehler.bueffel.study.Planner
import ch.orellbuehler.bueffel.study.ResolvedConfig
import ch.orellbuehler.bueffel.study.SessionConfig
import ch.orellbuehler.bueffel.study.SessionPreview
import ch.orellbuehler.bueffel.study.StudySession
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlin.random.Random
import kotlin.time.Clock

class BueffelSdk internal constructor(
    driver: SqlDriver,
    clock: Clock,
    timeZone: () -> TimeZone,
    random: Random,
    private val dispatcher: CoroutineDispatcher,
) {
    constructor(driverFactory: DatabaseDriverFactory) : this(
        driverFactory.createDriver(),
        Clock.System,
        { TimeZone.currentSystemDefault() },
        Random.Default,
        Dispatchers.Default,
    )

    private val env = Env(clock, timeZone, random, dispatcher)
    private val database = BueffelDatabase(driver)
    private val packStore = PackStore(database, env)
    private val settingsStore = SettingsStore(database, env)
    private val statsStore = StatsStore(database, env, packStore, settingsStore)

    fun observePacks(): Flow<List<PackSummary>> = env.observe { packStore.summaries() }

    fun observePack(packId: String): Flow<PackDetail?> = env.observe { packStore.detail(packId) }

    fun observeItems(packId: String): Flow<List<ItemSummary>> = env.observe { packStore.items(packId) }

    suspend fun items(packId: String): List<ItemSummary> = withContext(dispatcher) { packStore.items(packId) }

    suspend fun previewImport(text: String): ImportPreview =
        withContext(dispatcher) {
            when (val read = PackReader.read(text)) {
                is ReadResult.Rejected -> ImportPreview.Invalid(read.error)
                is ReadResult.Accepted -> {
                    val pack = read.pack
                    val diff = packStore.diff(pack)
                    ImportPreview.Valid(
                        packId = pack.id,
                        title = pack.title,
                        descriptionText = pack.description,
                        icon = pack.icon,
                        color = pack.color,
                        language = pack.language,
                        revision = pack.revision ?: 0,
                        itemCount = pack.items.size,
                        sectionCount = pack.sections.size,
                        typeCounts =
                            ItemType.entries.map { type -> TypeCount(type, pack.items.count { it.type == type }) }.filter {
                                it.count >
                                    0
                            },
                        skippedCount = read.skippedItems,
                        warnings = read.warnings,
                        isUpdate = diff.existing != null,
                        installedTitle = diff.existing?.title,
                        installedRevision = diff.existing?.revision ?: 0,
                        isDowngrade = diff.isDowngradeFor(pack),
                        counts = diff.counts,
                    )
                }
            }
        }

    suspend fun importPack(text: String): ImportResult = withContext(dispatcher) { install(text, allowDowngrade = false) }

    suspend fun importPackAllowingDowngrade(text: String): ImportResult = withContext(dispatcher) { install(text, allowDowngrade = true) }

    suspend fun deletePack(packId: String) {
        withContext(dispatcher) { packStore.delete(packId) }
    }

    suspend fun resetProgress(packId: String) {
        withContext(dispatcher) { packStore.reset(packId) }
    }

    suspend fun exportPack(packId: String): String? = withContext(dispatcher) { packStore.source(packId) }

    fun samplePacks(): List<SamplePack> =
        Samples.all.map {
            SamplePack(it.pack.id, it.pack.title, it.pack.description ?: "", it.pack.icon, it.pack.color, it.pack.items.size)
        }

    fun authoringPrompt(): String = GENERATED_AUTHORING_PROMPT

    suspend fun importSample(sampleId: String): ImportResult {
        val sample =
            Samples.all.firstOrNull { it.pack.id == sampleId }
                ?: return ImportResult.Failure(
                    ImportError(ImportErrorCode.UNKNOWN_SAMPLE, "There is no sample pack with the id '$sampleId'."),
                )
        return withContext(dispatcher) { install(sample.json, allowDowngrade = true) }
    }

    suspend fun previewSession(
        packId: String,
        config: SessionConfig,
    ): SessionPreview =
        withContext(dispatcher) {
            val plan = prepare(packId, config).plan
            SessionPreview(plan.dueCount, plan.newCount, plan.entries.size)
        }

    suspend fun startSession(
        packId: String,
        config: SessionConfig,
    ): StudySession {
        val prepared = withContext(dispatcher) { prepare(packId, config) }
        val recorder = AttemptRecorder { attempt -> withContext(dispatcher) { packStore.record(packId, attempt) } }
        return StudySession(
            packId,
            config.mode,
            prepared.pack,
            prepared.plan.entries,
            prepared.config,
            recorder,
            env.clock,
            env.random,
        )
    }

    fun observeStats(): Flow<Stats> = observeStats(StatsStore.DEFAULT_DAYS)

    fun observeStats(days: Int): Flow<Stats> = env.observe { statsStore.stats(days) }

    fun observeSettings(): Flow<AppSettings> = env.observe { settingsStore.get() }

    suspend fun settings(): AppSettings = withContext(dispatcher) { settingsStore.get() }

    suspend fun setDailyGoal(goal: Int) {
        withContext(dispatcher) { settingsStore.setDailyGoal(goal) }
    }

    suspend fun setDefaultSessionSize(size: Int) {
        withContext(dispatcher) { settingsStore.setDefaultSessionSize(size) }
    }

    suspend fun setTheme(theme: ThemePreference) {
        withContext(dispatcher) { settingsStore.setTheme(theme) }
    }

    suspend fun setOnboardingSeen(seen: Boolean) {
        withContext(dispatcher) { settingsStore.setOnboardingSeen(seen) }
    }

    fun refresh() {
        env.changed()
    }

    private class Prepared(
        val pack: LoadedPack,
        val config: ResolvedConfig,
        val plan: Plan,
    )

    private fun prepare(
        packId: String,
        config: SessionConfig,
    ): Prepared {
        val pack = packStore.load(packId) ?: LoadedPack(packId, PackContext(Study.DEFAULT, emptyList()), emptyMap())
        val resolved = ResolvedConfig.resolve(config, settingsStore.get().defaultSessionSize, pack.context.study.newPerSession)
        val plan = Planner.plan(pack, resolved, env.cutoffs(env.now()), env.random)
        return Prepared(pack, resolved, plan)
    }

    private fun install(
        text: String,
        allowDowngrade: Boolean,
    ): ImportResult {
        val read = PackReader.read(text)
        if (read !is ReadResult.Accepted) return ImportResult.Failure((read as ReadResult.Rejected).error)
        val pack = read.pack
        return try {
            val before = packStore.diff(pack)
            if (!allowDowngrade && before.isDowngradeFor(pack)) {
                return ImportResult.Failure(
                    ImportError(
                        ImportErrorCode.REVISION_DOWNGRADE,
                        "The installed pack has revision ${before.existing?.revision}, this file has revision ${pack.revision}.",
                    ),
                )
            }
            val diff = packStore.install(pack, read.payloads, text.removePrefix("\uFEFF"), env.now())
            ImportResult.Success(
                packId = pack.id,
                title = pack.title,
                itemCount = pack.items.size,
                skippedCount = read.skippedItems,
                isUpdate = before.existing != null,
                counts = diff.counts,
                warnings = read.warnings,
            )
        } catch (e: Exception) {
            ImportResult.Failure(ImportError(ImportErrorCode.STORAGE_ERROR, "The pack could not be saved: ${e.message ?: "unknown error"}"))
        }
    }
}
