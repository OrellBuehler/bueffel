import SwiftUI

struct LibraryView: View {
    @Environment(AppModel.self) private var model

    var body: some View {
        NavigationStack {
            ScrollView {
                if !model.packsLoaded {
                    Color.clear.frame(height: 1)
                } else if model.packs.isEmpty {
                    EmptyLibraryView()
                } else {
                    LibraryContent()
                }
            }
            .background(Brand.background.ignoresSafeArea())
            .navigationTitle("library.title")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        model.presentAddPack()
                    } label: {
                        Image(systemName: "plus")
                    }
                    .accessibilityLabel("library.add")
                }
            }
            .navigationDestination(for: PackRoute.self) { route in
                PackDetailView(packId: route.id)
            }
        }
    }
}

private struct LibraryContent: View {
    @Environment(AppModel.self) private var model

    var body: some View {
        LazyVStack(spacing: 16) {
            LibraryHeaderView()
            SectionTitle("library.packs")
                .padding(.top, 8)
            ForEach(model.packs) { pack in
                NavigationLink(value: PackRoute(id: pack.id)) {
                    PackCardView(pack: pack)
                }
                .buttonStyle(PressableStyle())
            }
        }
        .padding(.horizontal, 16)
        .padding(.top, 8)
        .padding(.bottom, 32)
    }
}
