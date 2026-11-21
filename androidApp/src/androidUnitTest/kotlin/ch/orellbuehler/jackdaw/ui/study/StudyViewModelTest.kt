package ch.orellbuehler.jackdaw.ui.study

import app.cash.turbine.test
import ch.orellbuehler.jackdaw.JackdawSdk
import ch.orellbuehler.jackdaw.MainDispatcherRule
import ch.orellbuehler.jackdaw.format.RichText
import ch.orellbuehler.jackdaw.statsOf
import ch.orellbuehler.jackdaw.study.Facet
import ch.orellbuehler.jackdaw.study.FlipExercise
import ch.orellbuehler.jackdaw.study.Grade
import ch.orellbuehler.jackdaw.study.SessionConfig
import ch.orellbuehler.jackdaw.study.SessionMode
import ch.orellbuehler.jackdaw.study.SessionState
import ch.orellbuehler.jackdaw.study.SessionSummary
import ch.orellbuehler.jackdaw.study.StudySession
import ch.orellbuehler.jackdaw.study.TextAnswer
import ch.orellbuehler.jackdaw.study.Verdict
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

class StudyViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val sdk = mockk<JackdawSdk>()
    private val session = mockk<StudySession>(relaxed = true)

    private fun exercise(id: String = "i1") = FlipExercise(id, Facet.FORWARD, RichText.fromPlain("front"))

    private fun question(
        position: Int,
        id: String = "i$position",
        hint: RichText? = null,
        hasHint: Boolean = false,
    ) = SessionState.Question(exercise(id), position, 3, isRetry = false, hasHint = hasHint, hint = hint)

    private fun feedback(position: Int) =
        SessionState.Feedback(
            exercise = exercise("i$position"),
            position = position,
            total = 3,
            verdict = Verdict.PENDING_SELF_GRADE,
            needsSelfGrade = true,
            willRepeat = false,
            correctAnswer = RichText.fromPlain("back"),
            userAnswer = "",
            explanation = null,
            choiceResults = emptyList(),
            gapResults = emptyList(),
            orderResults = emptyList(),
            keyPoints = emptyList(),
            statementIsTrue = false,
        )

    private fun summary(total: Int) = SessionSummary(SessionMode.LEARN, total, total, total, 0, 0, total, 1.0, 1000, false)

    private fun viewModel(
        state: MutableStateFlow<SessionState>,
        config: SessionConfig = SessionConfig.learn(),
    ): StudyViewModel {
        every { session.state } returns state
        every { session.mode } returns config.mode
        every { sdk.observePack(any()) } returns flowOf(null)
        every { sdk.observeStats(any()) } returns flowOf(statsOf(streak = 3))
        coEvery { sdk.startSession("pack", any()) } returns session
        return StudyViewModel(sdk, "pack", config)
    }

    @Test
    fun exposesTheFirstQuestionAsAnActiveStep() =
        runTest {
            val vm = viewModel(MutableStateFlow(question(1)))
            val active = assertIs<StudyUiState.Active>(vm.ui.value)
            assertEquals(1, active.step)
        }

    @Test
    fun stepOnlyAdvancesForANewQuestion() =
        runTest {
            val state = MutableStateFlow<SessionState>(question(1))
            val vm = viewModel(state)
            vm.ui.test {
                var current = assertIs<StudyUiState.Active>(awaitItem())
                val first = current.step
                state.value = question(1, hint = RichText.fromPlain("hint"), hasHint = true)
                current = assertIs<StudyUiState.Active>(awaitItem())
                assertEquals(first, current.step)
                state.value = feedback(1)
                current = assertIs<StudyUiState.Active>(awaitItem())
                assertEquals(first, current.step)
                state.value = question(2)
                current = assertIs<StudyUiState.Active>(awaitItem())
                assertNotEquals(first, current.step)
            }
        }

    @Test
    fun anEmptyPlanBecomesTheEmptyState() =
        runTest {
            val vm = viewModel(MutableStateFlow(SessionState.Finished(summary(0))))
            assertEquals(StudyUiState.Empty(SessionMode.LEARN), vm.ui.value)
        }

    @Test
    fun finishingLoadsTheStreak() =
        runTest {
            val state = MutableStateFlow<SessionState>(question(1))
            val vm = viewModel(state)
            state.value = SessionState.Finished(summary(3))
            val done = assertIs<StudyUiState.Done>(vm.ui.value)
            assertEquals(3, done.stats?.currentStreak)
            assertEquals(3, done.summary.total)
        }

    @Test
    fun abandoningASessionClosesTheScreenInsteadOfShowingASummary() =
        runTest {
            val state = MutableStateFlow<SessionState>(question(1))
            val vm = viewModel(state)
            state.value = SessionState.Finished(summary(3).copy(abandoned = true))
            assertEquals(StudyUiState.Closed, vm.ui.value)
        }

    @Test
    fun forwardsUserActionsToTheSession() =
        runTest {
            val vm = viewModel(MutableStateFlow(question(1)))
            vm.reveal()
            vm.revealHint()
            vm.next()
            vm.finish()
            vm.submit(TextAnswer("x"))
            vm.selfGrade(Grade.GOOD)
            verify { session.reveal() }
            verify { session.revealHint() }
            verify { session.next() }
            verify { session.finish() }
            coVerify { session.submit(TextAnswer("x")) }
            coVerify { session.selfGrade(Grade.GOOD) }
        }

    @Test
    fun practiceInsteadRestartsInPracticeMode() =
        runTest {
            val vm = viewModel(MutableStateFlow(SessionState.Finished(summary(0))))
            vm.practiceInstead()
            coVerify { sdk.startSession("pack", match { it.mode == SessionMode.PRACTICE }) }
            assertEquals(SessionMode.PRACTICE, vm.mode)
        }

    @Test
    fun restartStartsAnotherSessionWithTheSameConfig() =
        runTest {
            val config = SessionConfig.learn().withSection("s1")
            val vm = viewModel(MutableStateFlow(SessionState.Finished(summary(2))), config)
            vm.restart()
            coVerify(exactly = 2) { sdk.startSession("pack", config) }
        }
}
