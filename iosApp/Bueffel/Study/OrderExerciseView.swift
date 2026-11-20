import SwiftUI

struct OrderExerciseView: View {
    let store: StudyStore
    let current: StudyCurrent
    let style: PackStyle
    @State private var entries: [OptionInfo]
    @State private var moves = 0

    init(store: StudyStore, current: StudyCurrent, style: PackStyle) {
        self.store = store
        self.current = current
        self.style = style
        _entries = State(initialValue: current.exercise.options)
    }

    private var feedback: FeedbackInfo? { current.feedback }

    var body: some View {
        ExerciseScaffold(current: current, style: style) {
            VStack(spacing: 16) {
                QuestionCard(
                    labelKey: "study.label.order",
                    prompt: current.exercise.prompt,
                    hint: current.hint,
                    style: style
                )
                if let feedback {
                    resultList(feedback)
                    FeedbackBanner(feedback: feedback)
                } else {
                    editableList
                }
            }
            .sensoryFeedback(.selection, trigger: moves)
        } bar: {
            if feedback != nil {
                ContinueBar(store: store, style: style)
            } else {
                Button {
                    store.submit(.order(entries.map { $0.id }))
                } label: {
                    Text("study.check")
                }
                .buttonStyle(BrandButtonStyle(fill: style.base, foreground: style.onBase))
            }
        }
    }

    private var editableList: some View {
        VStack(spacing: 10) {
            Text("study.order.hint")
                .font(.brand(.footnote))
                .foregroundStyle(Brand.textSecondary)
                .frame(maxWidth: .infinity, alignment: .leading)
            ForEach(Array(entries.enumerated()), id: \.element.id) { index, entry in
                HStack(spacing: 12) {
                    Text(String(index + 1))
                        .font(.brand(.subheadline, weight: .bold))
                        .foregroundStyle(style.onBase)
                        .frame(width: 30, height: 30)
                        .background(style.base, in: Circle())
                    RichTextView(text: entry.text, font: .brand(.body, weight: .medium))
                        .frame(maxWidth: .infinity, alignment: .leading)
                    VStack(spacing: 2) {
                        moveButton(systemName: "chevron.up", enabled: index > 0) {
                            move(index, by: -1)
                        }
                        moveButton(systemName: "chevron.down", enabled: index < entries.count - 1) {
                            move(index, by: 1)
                        }
                    }
                }
                .padding(.vertical, 10)
                .padding(.horizontal, 12)
                .background(Brand.surface, in: RoundedRectangle(cornerRadius: 18, style: .continuous))
                .overlay {
                    RoundedRectangle(cornerRadius: 18, style: .continuous)
                        .strokeBorder(Brand.hairline, lineWidth: 1)
                }
            }
        }
    }

    private func moveButton(systemName: String, enabled: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: systemName)
                .font(.system(size: 14, weight: .bold))
                .foregroundStyle(enabled ? style.accentText : Brand.textSecondary.opacity(0.3))
                .frame(width: 36, height: 26)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .disabled(!enabled)
        .accessibilityLabel(L10n.tr(systemName == "chevron.up" ? "study.order.up" : "study.order.down"))
    }

    private func resultList(_ feedback: FeedbackInfo) -> some View {
        VStack(spacing: 10) {
            ForEach(feedback.orderResults) { result in
                HStack(alignment: .top, spacing: 12) {
                    Image(systemName: result.correct ? "checkmark.circle.fill" : "xmark.circle.fill")
                        .font(.system(size: 22))
                        .foregroundStyle(result.correct ? Brand.success : Brand.error)
                    VStack(alignment: .leading, spacing: 4) {
                        RichTextView(text: result.arranged, font: .brand(.body, weight: .medium))
                        if !result.correct {
                            HStack(alignment: .top, spacing: 6) {
                                Text("study.order.expected")
                                    .font(.brand(.footnote, weight: .bold))
                                    .foregroundStyle(Brand.textSecondary)
                                RichTextView(text: result.expected, font: .brand(.footnote), color: Brand.success)
                            }
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    Text(String(result.position))
                        .font(.brand(.caption, weight: .bold))
                        .foregroundStyle(Brand.textSecondary)
                }
                .padding(12)
                .background(
                    (result.correct ? Brand.success : Brand.error).opacity(0.1),
                    in: RoundedRectangle(cornerRadius: 18, style: .continuous)
                )
            }
        }
    }

    private func move(_ index: Int, by delta: Int) {
        let target = index + delta
        guard entries.indices.contains(index), entries.indices.contains(target) else { return }
        withAnimation(.snappy(duration: 0.25)) {
            entries.swapAt(index, target)
        }
        moves += 1
    }
}
