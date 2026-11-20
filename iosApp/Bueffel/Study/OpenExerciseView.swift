import SwiftUI

struct OpenExerciseView: View {
    let store: StudyStore
    let current: StudyCurrent
    let style: PackStyle
    @State private var ticked: Set<Int> = []

    private var feedback: FeedbackInfo? { current.feedback }

    var body: some View {
        ExerciseScaffold(current: current, style: style) {
            VStack(spacing: 16) {
                QuestionCard(
                    labelKey: "study.label.open",
                    prompt: current.exercise.prompt,
                    hint: current.question?.hint,
                    style: style
                )
                if let feedback {
                    answerCard(feedback)
                    if !feedback.keyPoints.isEmpty {
                        keyPointsCard(feedback)
                    }
                    if let explanation = feedback.explanation, !explanation.isEmpty {
                        ExplanationCard(text: explanation)
                    }
                } else {
                    Label("study.open.think", systemImage: "brain.head.profile")
                        .font(.brand(.subheadline))
                        .foregroundStyle(Brand.textSecondary)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal, 4)
                }
            }
            .sensoryFeedback(.selection, trigger: ticked)
        } bar: {
            if let feedback {
                VStack(spacing: 8) {
                    Text("study.open.rate")
                        .font(.brand(.caption, weight: .semibold))
                        .foregroundStyle(Brand.textSecondary)
                    GradeBar(store: store, suggested: feedback.suggestedGrade(ticked: ticked.count))
                }
            } else {
                Button {
                    store.reveal()
                } label: {
                    Text("study.reveal")
                }
                .buttonStyle(BrandButtonStyle(fill: style.base, foreground: style.onBase))
            }
        }
    }

    private func answerCard(_ feedback: FeedbackInfo) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("study.open.answer")
                .font(.brand(.caption, weight: .bold))
                .foregroundStyle(style.accentText)
                .tracking(0.6)
                .textCase(.uppercase)
            if feedback.correctAnswer.isEmpty {
                Text("study.open.noanswer")
                    .font(.brand(.subheadline))
                    .foregroundStyle(Brand.textSecondary)
            } else {
                RichTextView(text: feedback.correctAnswer, font: .brand(.body, weight: .medium))
            }
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .card(radius: 22)
    }

    private func keyPointsCard(_ feedback: FeedbackInfo) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("study.open.keypoints")
                .font(.brand(.caption, weight: .bold))
                .foregroundStyle(style.accentText)
                .tracking(0.6)
                .textCase(.uppercase)
            ForEach(Array(feedback.keyPoints.enumerated()), id: \.offset) { index, point in
                Button {
                    toggle(index)
                } label: {
                    HStack(alignment: .top, spacing: 12) {
                        Image(systemName: ticked.contains(index) ? "checkmark.square.fill" : "square")
                            .font(.system(size: 22))
                            .foregroundStyle(ticked.contains(index) ? Brand.success : Brand.textSecondary)
                        RichTextView(text: point, font: .brand(.subheadline))
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .accessibilityAddTraits(ticked.contains(index) ? .isSelected : [])
            }
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .card(radius: 22)
    }

    private func toggle(_ index: Int) {
        withAnimation(.snappy(duration: 0.2)) {
            if ticked.contains(index) {
                ticked.remove(index)
            } else {
                ticked.insert(index)
            }
        }
    }
}
