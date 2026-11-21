package ch.orellbuehler.jackdaw.store

import ch.orellbuehler.jackdaw.study.DueCutoffs
import ch.orellbuehler.jackdaw.study.Scheduler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Instant

internal class Env(
    val clock: Clock,
    val timeZone: () -> TimeZone,
    val random: Random,
    val dispatcher: CoroutineDispatcher,
) {
    private val ticks = MutableStateFlow(0L)
    private val idRandom = Random.Default

    var syncTracking: Boolean = false

    fun newId(): String = hex(idRandom.nextBytes(ID_BYTES))

    fun now(): Long = clock.now().toEpochMilliseconds()

    fun today(now: Long): LocalDate = Instant.fromEpochMilliseconds(now).toLocalDateTime(timeZone()).date

    fun epochDay(now: Long): Int = today(now).toEpochDays().toInt()

    fun cutoffs(now: Long): DueCutoffs {
        val tomorrow = today(now).plus(1, DateTimeUnit.DAY)
        return DueCutoffs(now + Scheduler.LEARN_AHEAD_MS, tomorrow.atStartOfDayIn(timeZone()).toEpochMilliseconds())
    }

    fun changed() {
        ticks.update { it + 1 }
    }

    private fun hex(bytes: ByteArray): String = bytes.joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }

    fun <T> observe(compute: () -> T): Flow<T> = ticks.map { compute() }.distinctUntilChanged().flowOn(dispatcher)

    private companion object {
        const val ID_BYTES = 16
    }
}
