import SwiftUI

struct FlipExerciseView: View {
    let store: StudyStore
    let current: StudyCurrent
    let style: PackStyle
    @State private var angle: Double = 0
    @State private var showsBack = false

    private var isFlipped: Bool { current.feedback != nil }

    var body: some View {
        ExerciseScaffold(current: current, style: style) {
            VStack(spacing: 16) {
                card
                    .rotation3DEffect(.degrees(angle), axis: (x: 0, y: 1, z: 0), perspective: 0.55)
                    .onTapGesture {
                        if !isFlipped { store.reveal() }
                    }
                    .accessibilityAddTraits(.isButton)
                if let explanation = current.feedback?.explanation, !explanation.isEmpty {
                    ExplanationCard(text: explanation)
                        .transition(.opacity)
                }
            }
            .onChange(of: isFlipped) { _, flipped in
                flip(to: flipped)
            }
            .onAppear {
                if isFlipped { showsBack = true }
            }
        } bar: {
            if current.feedback != nil {
                GradeBar(store: store)
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

    private var card: some View {
        let text = showsBack ? (current.feedback?.correctAnswer ?? StyledText.empty) : current.exercise.prompt
        let key = showsBack ? "study.flip.back" : "study.flip.front"
        return VStack(spacing: 18) {
            Text(L10n.tr(key))
                .font(.brand(.caption, weight: .bold))
                .foregroundStyle(style.accentText)
                .tracking(0.6)
                .textCase(.uppercase)
            RichTextView(text: text, font: .brand(.title2, weight: .semibold), alignment: .center)
            if !showsBack, let hint = current.hint, !hint.isEmpty {
                RichTextView(text: hint, font: .brand(.subheadline), color: Brand.textSecondary, alignment: .center)
            }
            if !showsBack {
                Label("study.flip.tap", systemImage: "hand.tap.fill")
                    .font(.brand(.caption))
                    .foregroundStyle(Brand.textSecondary)
            }
        }
        .padding(24)
        .frame(maxWidth: .infinity, minHeight: 300)
        .background(Brand.surface, in: RoundedRectangle(cornerRadius: 28, style: .continuous))
        .overlay {
            RoundedRectangle(cornerRadius: 28, style: .continuous)
                .strokeBorder(style.base.opacity(showsBack ? 0.5 : 0.2), lineWidth: showsBack ? 2 : 1)
        }
        .shadow(color: style.base.opacity(0.18), radius: 18, x: 0, y: 10)
    }

    private func flip(to flipped: Bool) {
        guard flipped != showsBack else { return }
        withAnimation(.easeIn(duration: 0.16)) {
            angle = 90
        }
        Task {
            _ = try? await Task.sleep(nanoseconds: 160_000_000)
            showsBack = flipped
            angle = -90
            withAnimation(.easeOut(duration: 0.22)) {
                angle = 0
            }
        }
    }
}
