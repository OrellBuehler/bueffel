import SwiftUI

struct EmptyLibraryView: View {
    @Environment(AppModel.self) private var model

    var body: some View {
        VStack(spacing: 24) {
            Mascot(size: 150, animated: true)
                .padding(.top, 28)
            VStack(spacing: 8) {
                Text("empty.title")
                    .font(.brand(.title, weight: .bold))
                    .foregroundStyle(Brand.text)
                    .multilineTextAlignment(.center)
                Text("empty.message")
                    .font(.brand(.body))
                    .foregroundStyle(Brand.textSecondary)
                    .multilineTextAlignment(.center)
            }
            VStack(spacing: 12) {
                SectionTitle("empty.samples")
                ForEach(model.samples) { sample in
                    SamplePackRow(sample: sample)
                }
            }
            Button {
                model.presentAddPack()
            } label: {
                Label("empty.import", systemImage: "square.and.arrow.down")
            }
            .buttonStyle(SoftButtonStyle())
        }
        .padding(.horizontal, 20)
        .padding(.bottom, 40)
    }
}
