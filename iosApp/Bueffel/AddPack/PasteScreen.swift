import SwiftUI
import UIKit

struct PasteScreen: View {
    let onContinue: (String) -> Void
    @State private var text = ""

    private var trimmed: String {
        text.trimmingCharacters(in: .whitespacesAndNewlines)
    }

    var body: some View {
        VStack(spacing: 14) {
            ZStack(alignment: .topLeading) {
                TextEditor(text: $text)
                    .font(.system(.footnote, design: .monospaced))
                    .scrollContentBackground(.hidden)
                    .autocorrectionDisabled()
                    .textInputAutocapitalization(.never)
                    .padding(10)
                if text.isEmpty {
                    Text("paste.placeholder")
                        .font(.system(.footnote, design: .monospaced))
                        .foregroundStyle(Brand.textSecondary.opacity(0.7))
                        .padding(.horizontal, 15)
                        .padding(.vertical, 18)
                        .allowsHitTesting(false)
                }
            }
            .frame(maxWidth: .infinity, minHeight: 240)
            .card(radius: 20)

            HStack(spacing: 12) {
                Button {
                    if let pasted = UIPasteboard.general.string {
                        text = pasted
                    }
                } label: {
                    Label("paste.clipboard", systemImage: "doc.on.clipboard")
                }
                .buttonStyle(SoftButtonStyle())
                Button {
                    onContinue(trimmed)
                } label: {
                    Text("paste.continue")
                }
                .buttonStyle(BrandButtonStyle())
                .disabled(trimmed.isEmpty)
            }
            Spacer(minLength: 0)
        }
        .padding(16)
        .background(Brand.background.ignoresSafeArea())
        .navigationTitle("paste.title")
        .navigationBarTitleDisplayMode(.inline)
    }
}
