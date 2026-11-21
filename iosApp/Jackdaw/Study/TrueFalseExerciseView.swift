import SwiftUI

struct TrueFalseExerciseView: View {
    let store: StudyStore
    let current: StudyCurrent
    let style: PackStyle
    @State private var chosen: Bool?

    private var feedback: FeedbackInfo? { current.feedback }

    var body: some View {
        ExerciseScaffold(current: current, style: style, showsBar: feedback != nil) {
            VStack(spacing: 16) {
                QuestionCard(
                    labelKey: "study.label.truefalse",
                    prompt: current.exercise.prompt,
                    hint: current.hint,
                    style: style
                )
                HStack(spacing: 12) {
                    answerButton(value: true)
                    answerButton(value: false)
                }
                if let feedback {
                    FeedbackBanner(feedback: feedback)
                    Label(
                        L10n.tr(feedback.statementIsTrue ? "study.tf.wastrue" : "study.tf.wasfalse"),
                        systemImage: feedback.statementIsTrue ? "checkmark.seal.fill" : "xmark.seal.fill"
                    )
                    .font(.brand(.subheadline, weight: .semibold))
                    .foregroundStyle(Brand.textSecondary)
                }
            }
        } bar: {
            ContinueBar(store: store, style: style)
        }
    }

    private func answerButton(value: Bool) -> some View {
        let tint = value ? Brand.success : Brand.error
        let isChosen = chosen == value
        let showsResult = feedback != nil
        let isRight = showsResult && feedback?.statementIsTrue == value
        return Button {
            guard feedback == nil, chosen == nil else { return }
            chosen = value
            store.submit(.trueFalse(value))
        } label: {
            VStack(spacing: 8) {
                Image(systemName: value ? "checkmark" : "xmark")
                    .font(.system(size: 30, weight: .heavy))
                Text(L10n.tr(value ? "study.tf.true" : "study.tf.false"))
                    .font(.brand(.headline, weight: .bold))
            }
            .foregroundStyle(tint)
            .frame(maxWidth: .infinity, minHeight: 110)
            .background(
                tint.opacity(isChosen || isRight ? 0.22 : 0.12),
                in: RoundedRectangle(cornerRadius: 22, style: .continuous)
            )
            .overlay {
                RoundedRectangle(cornerRadius: 22, style: .continuous)
                    .strokeBorder(tint, lineWidth: isChosen || isRight ? 3 : 0)
            }
            .opacity(showsResult && !isChosen && !isRight ? 0.5 : 1)
        }
        .buttonStyle(PressableStyle())
        .disabled(showsResult)
    }
}
