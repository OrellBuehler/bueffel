import SwiftUI

struct SamplePackRow: View {
    let sample: SamplePackInfo
    @Environment(AppModel.self) private var model
    @State private var isAdding = false
    @State private var failure: String?

    private var isInstalled: Bool {
        model.packs.contains { $0.id == sample.id }
    }

    private var style: PackStyle {
        PackStyle(hex: sample.colorHex)
    }

    var body: some View {
        HStack(spacing: 14) {
            EmojiTile(icon: sample.icon, style: style, size: 52)
            VStack(alignment: .leading, spacing: 3) {
                Text(sample.title)
                    .font(.brand(.subheadline, weight: .bold))
                    .foregroundStyle(Brand.text)
                    .lineLimit(2)
                Text(sample.blurb)
                    .font(.brand(.caption))
                    .foregroundStyle(Brand.textSecondary)
                    .lineLimit(2)
                Text(L10n.plural("items", sample.itemCount))
                    .font(.brand(.caption2, weight: .semibold))
                    .foregroundStyle(style.accentText)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            trailing
        }
        .padding(14)
        .card(radius: 22)
        .alert(
            "import.failed.title",
            isPresented: Binding(
                get: { failure != nil },
                set: { if !$0 { failure = nil } }
            )
        ) {
            Button("common.ok", role: .cancel) {}
        } message: {
            Text(failure ?? "")
        }
    }

    @ViewBuilder
    private var trailing: some View {
        if isInstalled {
            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: 26))
                .foregroundStyle(Brand.success)
                .accessibilityLabel("sample.added")
        } else if isAdding {
            ProgressView()
        } else {
            Button {
                add()
            } label: {
                Text("sample.add")
                    .font(.brand(.subheadline, weight: .bold))
                    .padding(.horizontal, 14)
                    .padding(.vertical, 8)
                    .foregroundStyle(Color.white)
                    .background(Brand.accent, in: Capsule())
            }
            .buttonStyle(PressableStyle())
        }
    }

    private func add() {
        isAdding = true
        Task {
            let outcome = await model.importSample(sample)
            isAdding = false
            if case .failure(let issue) = outcome {
                failure = issue.localizedMessage
            }
        }
    }
}
