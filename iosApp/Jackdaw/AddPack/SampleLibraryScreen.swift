import SwiftUI

struct SampleLibraryScreen: View {
    @Environment(AppModel.self) private var model

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                Text("samples.intro")
                    .font(.brand(.subheadline))
                    .foregroundStyle(Brand.textSecondary)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 4)
                ForEach(model.samples) { sample in
                    SamplePackRow(sample: sample)
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 8)
            .padding(.bottom, 32)
        }
        .background(Brand.background.ignoresSafeArea())
        .navigationTitle("samples.title")
        .navigationBarTitleDisplayMode(.inline)
    }
}
