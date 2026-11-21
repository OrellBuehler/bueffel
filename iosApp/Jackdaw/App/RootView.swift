import SwiftUI

struct RootView: View {
    @Environment(AppModel.self) private var model
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        ZStack {
            Brand.background.ignoresSafeArea()
            if !model.settingsLoaded {
                Mascot(size: 96, animated: true)
            } else if model.showOnboarding {
                OnboardingView()
                    .transition(.opacity)
            } else {
                tabs
                    .transition(.opacity)
            }
        }
        .animation(.smooth(duration: 0.4), value: model.showOnboarding)
        .animation(.smooth(duration: 0.4), value: model.settingsLoaded)
        .preferredColorScheme(model.preferredScheme)
        .tint(Brand.accent)
        .onAppear {
            model.start()
        }
        .onOpenURL { url in
            model.handleIncoming(url)
        }
        .onChange(of: scenePhase) { _, phase in
            if phase == .active {
                model.refresh()
                model.sync.foreground()
            }
        }
    }

    private var tabs: some View {
        @Bindable var model = model
        return TabView(selection: $model.selectedTab) {
            LibraryView()
                .tabItem { Label("tab.library", systemImage: "books.vertical.fill") }
                .tag(AppTab.library)
            StatsView()
                .tabItem { Label("tab.stats", systemImage: "chart.bar.xaxis") }
                .tag(AppTab.stats)
            SettingsView()
                .tabItem { Label("tab.settings", systemImage: "gearshape.fill") }
                .tag(AppTab.settings)
        }
        .sheet(item: $model.addPackRoute) { route in
            AddPackSheet(route: route)
        }
        .fullScreenCover(
            item: $model.studyRequest,
            onDismiss: { model.refresh() },
            content: { request in StudyView(request: request) }
        )
    }
}
