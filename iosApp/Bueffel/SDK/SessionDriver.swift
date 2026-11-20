@preconcurrency import shared
import Foundation

final class SessionDriver: @unchecked Sendable {
    private let session: StudySession

    init(session: StudySession) {
        self.session = session
    }

    func watch(deliver: @MainActor @Sendable (SessionSnapshot) -> Void) async {
        for await state in session.state {
            guard let snapshot = SessionAdapter.snapshot(state) else { continue }
            await deliver(snapshot)
        }
    }

    func revealHint() {
        session.revealHint()
    }

    func reveal() {
        session.reveal()
    }

    func next() {
        session.next()
    }

    func finish() {
        session.finish()
    }

    func submit(_ answer: AnswerInput) async {
        do {
            try await session.submit(answer: SessionAdapter.kotlinAnswer(answer))
        } catch {}
    }

    func selfGrade(_ grade: GradeChoice) async {
        do {
            try await session.selfGrade(grade: SessionAdapter.kotlinGrade(grade))
        } catch {}
    }
}
