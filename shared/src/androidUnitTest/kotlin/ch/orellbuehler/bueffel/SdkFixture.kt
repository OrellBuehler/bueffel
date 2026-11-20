package ch.orellbuehler.bueffel

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import ch.orellbuehler.bueffel.db.BueffelDatabase
import ch.orellbuehler.bueffel.store.ImportResult
import ch.orellbuehler.bueffel.study.Answer
import ch.orellbuehler.bueffel.study.ChoiceAnswer
import ch.orellbuehler.bueffel.study.ChoiceExercise
import ch.orellbuehler.bueffel.study.ClozeAnswer
import ch.orellbuehler.bueffel.study.ClozeExercise
import ch.orellbuehler.bueffel.study.Exercise
import ch.orellbuehler.bueffel.study.FlipExercise
import ch.orellbuehler.bueffel.study.Grade
import ch.orellbuehler.bueffel.study.OpenExercise
import ch.orellbuehler.bueffel.study.OrderAnswer
import ch.orellbuehler.bueffel.study.OrderExercise
import ch.orellbuehler.bueffel.study.SessionConfig
import ch.orellbuehler.bueffel.study.SessionState
import ch.orellbuehler.bueffel.study.SessionSummary
import ch.orellbuehler.bueffel.study.StudySession
import ch.orellbuehler.bueffel.study.TextAnswer
import ch.orellbuehler.bueffel.study.TrueFalseAnswer
import ch.orellbuehler.bueffel.study.TrueFalseExercise
import ch.orellbuehler.bueffel.study.TypeExercise
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.TimeZone
import kotlin.random.Random
import kotlin.test.assertIs
import kotlin.test.fail
import kotlin.time.Instant

internal class SdkFixture(
    val timeZone: TimeZone = TimeZone.UTC,
    start: String = "2026-11-20T12:00:00Z",
    val driver: SqlDriver = newDriver(),
) {
    val clock = FakeClock(Instant.parse(start).toEpochMilliseconds())
    val sdk = BueffelSdk(driver, clock, { timeZone }, Random(42), Dispatchers.Unconfined)

    fun restart() = BueffelSdk(driver, clock, { timeZone }, Random(42), Dispatchers.Unconfined)

    suspend fun importOk(json: String): ImportResult.Success = assertIs<ImportResult.Success>(sdk.importPack(json))

    suspend fun studyFlipCards(
        packId: String,
        grade: Grade = Grade.GOOD,
        config: SessionConfig = SessionConfig.learn(),
        sdk: BueffelSdk = this.sdk,
    ): SessionSummary {
        val session = sdk.startSession(packId, config)
        var retry = false
        while (true) {
            when (val state = session.state.value) {
                is SessionState.Question -> {
                    retry = state.isRetry
                    session.reveal()
                }
                is SessionState.Feedback -> session.selfGrade(if (retry) Grade.GOOD else grade)
                is SessionState.Finished -> return state.summary
            }
        }
    }

    companion object {
        fun newDriver(): SqlDriver {
            val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            BueffelDatabase.Schema.create(driver)
            return driver
        }
    }
}

internal fun flipPack(
    id: String = "test.flip",
    cards: Int = 3,
    revision: Int? = null,
    title: String = "Flip Pack",
    reversible: Boolean = false,
    extraItems: List<String> = emptyList(),
): String {
    val items = (1..cards).map { """{"id":"c$it","type":"card","front":"front $it","back":"back $it"}""" } + extraItems
    val rev = if (revision != null) """"revision":$revision,""" else ""
    return """{"format":"bueffel-pack","version":1,"id":"$id","title":"$title",$rev
        "study":{"exercises":{"card":["flip"]},"reversible":$reversible,"newPerSession":50},"items":[${items.joinToString(",")}]}"""
}

internal class ScriptedLearner {
    val asked = ArrayList<Exercise>()
    private val known = HashMap<String, (Exercise) -> Answer>()

    suspend fun run(session: StudySession): SessionSummary {
        repeat(MAX_STEPS) {
            when (val state = session.state.value) {
                is SessionState.Question -> {
                    asked += state.exercise
                    act(session, state.exercise)
                }
                is SessionState.Feedback ->
                    if (state.needsSelfGrade) {
                        session.selfGrade(Grade.GOOD)
                    } else {
                        if (!state.correct) learn(state)
                        session.next()
                    }
                is SessionState.Finished -> return state.summary
            }
        }
        fail("session did not finish")
    }

    private suspend fun act(
        session: StudySession,
        exercise: Exercise,
    ) {
        when (exercise) {
            is FlipExercise, is OpenExercise -> session.reveal()
            else -> session.submit(known[exercise.itemId]?.invoke(exercise) ?: firstGuess(exercise))
        }
    }

    private fun firstGuess(exercise: Exercise): Answer =
        when (exercise) {
            is TrueFalseExercise -> TrueFalseAnswer(true)
            is ChoiceExercise -> ChoiceAnswer(emptyList())
            is TypeExercise -> TextAnswer("")
            is ClozeExercise -> ClozeAnswer(emptyList())
            is OrderExercise -> OrderAnswer(emptyList())
            else -> fail("no guess for $exercise")
        }

    private fun learn(feedback: SessionState.Feedback) {
        val id = feedback.exercise.itemId
        when (feedback.exercise) {
            is ChoiceExercise -> {
                val texts = feedback.choiceResults.filter { it.correct }.map { it.text.plain }
                known[id] = { e -> ChoiceAnswer((e as ChoiceExercise).options.filter { it.text.plain in texts }.map { it.id }) }
            }
            is TypeExercise -> known[id] = { TextAnswer(feedback.correctAnswer.plain) }
            is ClozeExercise -> known[id] = { ClozeAnswer(feedback.gapResults.map { it.canonical }) }
            is TrueFalseExercise -> known[id] = { TrueFalseAnswer(feedback.statementIsTrue) }
            is OrderExercise -> {
                val expected = feedback.orderResults.map { it.expected.plain }
                known[id] = { e -> OrderAnswer(expected.map { text -> (e as OrderExercise).entries.first { it.text.plain == text }.id }) }
            }
            else -> Unit
        }
    }

    private companion object {
        const val MAX_STEPS = 2000
    }
}
