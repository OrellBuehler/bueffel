import SwiftUI

struct ChoiceExerciseView: View {
    let store: StudyStore
    let current: StudyCurrent
    let style: PackStyle
    @State private var selected: Set<String> = []

    private var exercise: ExerciseInfo { current.exercise }
    private var feedback: FeedbackInfo? { current.feedback }

    var body: some View {
        ExerciseScaffold(current: current, style: style) {
            VStack(spacing: 16) {
                QuestionCard(
                    labelKey: exercise.multiSelect ? "study.label.multi" : "study.label.choice",
                    prompt: exercise.prompt,
                    hint: current.question?.hint,
                    style: style
                )
                VStack(spacing: 10) {
                    ForEach(exercise.options) { option in
                        optionButton(option)
                    }
                }
                if let feedback {
                    FeedbackBanner(feedback: feedback)
                }
            }
            .sensoryFeedback(.selection, trigger: selected)
        } bar: {
            if feedback != nil {
                ContinueBar(store: store, style: style)
            } else {
                Button {
                    submit()
                } label: {
                    Text("study.check")
                }
                .buttonStyle(BrandButtonStyle(fill: style.base, foreground: style.onBase))
                .disabled(selected.isEmpty)
            }
        }
    }

    private func optionButton(_ option: OptionInfo) -> some View {
        let result = feedback?.choiceResults.first { $0.id == option.id }
        return Button {
            toggle(option.id)
        } label: {
            ChoiceOptionRow(
                text: option.text,
                isSelected: selected.contains(option.id),
                isMulti: exercise.multiSelect,
                result: result,
                showsResult: feedback != nil,
                style: style
            )
        }
        .buttonStyle(PressableStyle())
        .disabled(feedback != nil)
    }

    private func toggle(_ id: String) {
        withAnimation(.snappy(duration: 0.2)) {
            if exercise.multiSelect {
                if selected.contains(id) {
                    selected.remove(id)
                } else {
                    selected.insert(id)
                }
            } else {
                selected = [id]
            }
        }
    }

    private func submit() {
        let ids = exercise.options.map { $0.id }.filter { selected.contains($0) }
        store.submit(.choice(ids))
    }
}

private struct ChoiceOptionRow: View {
    let text: StyledText
    let isSelected: Bool
    let isMulti: Bool
    let result: ChoiceResultInfo?
    let showsResult: Bool
    let style: PackStyle

    private var isCorrect: Bool { result?.correct == true }
    private var wasSelected: Bool { showsResult ? (result?.selected == true) : isSelected }

    private var tint: Color {
        if showsResult {
            if isCorrect { return Brand.success }
            if wasSelected { return Brand.error }
            return Brand.textSecondary
        }
        return isSelected ? style.accentText : Brand.textSecondary
    }

    private var symbol: String {
        if showsResult {
            if isCorrect { return "checkmark.circle.fill" }
            if wasSelected { return "xmark.circle.fill" }
            return isMulti ? "square" : "circle"
        }
        if isMulti {
            return isSelected ? "checkmark.square.fill" : "square"
        }
        return isSelected ? "largecircle.fill.circle" : "circle"
    }

    private var borderColor: Color {
        if showsResult {
            if isCorrect { return Brand.success }
            if wasSelected { return Brand.error }
            return Brand.hairline
        }
        return isSelected ? style.base : Brand.hairline
    }

    private var fillColor: Color {
        if showsResult {
            if isCorrect { return Brand.success.opacity(0.12) }
            if wasSelected { return Brand.error.opacity(0.12) }
            return Brand.surface
        }
        return isSelected ? style.base.opacity(0.12) : Brand.surface
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(alignment: .top, spacing: 12) {
                Image(systemName: symbol)
                    .font(.system(size: 22))
                    .foregroundStyle(tint)
                RichTextView(text: text, font: .brand(.body, weight: .medium))
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            if let explanation = result?.explanation, !explanation.isEmpty {
                RichTextView(text: explanation, font: .brand(.footnote), color: Brand.textSecondary)
                    .padding(.leading, 34)
            }
        }
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(fillColor, in: RoundedRectangle(cornerRadius: 18, style: .continuous))
        .overlay {
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .strokeBorder(borderColor, lineWidth: isSelected || (showsResult && (isCorrect || wasSelected)) ? 2 : 1)
        }
        .contentShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }
}
