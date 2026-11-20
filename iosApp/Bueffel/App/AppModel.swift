import Foundation
import Observation
import SwiftUI

enum AppTab: Hashable {
    case library
    case stats
    case settings
}

struct PackRoute: Hashable {
    let id: String
}

struct AddPackRoute: Identifiable {
    let id = UUID()
    var incomingURL: URL?
}

struct ContinueTarget {
    let pack: PackInfo
    let count: Int
    let isDue: Bool
}

@MainActor
@Observable
final class AppModel {
    private(set) var packs: [PackInfo] = []
    private(set) var packsLoaded = false
    private(set) var stats: StatsInfo = .empty
    private(set) var settings: SettingsInfo = .initial
    private(set) var settingsLoaded = false
    private(set) var samples: [SamplePackInfo] = []

    var selectedTab: AppTab = .library
    var studyRequest: StudyRequest?
    var addPackRoute: AddPackRoute?

    private let client = SDKClient.shared
    @ObservationIgnored private var watchers: [Task<Void, Never>] = []

    var showOnboarding: Bool {
        settingsLoaded && !settings.onboardingSeen
    }

    var preferredScheme: ColorScheme? {
        switch settings.theme {
        case .system: return nil
        case .light: return .light
        case .dark: return .dark
        }
    }

    var dailyGoalSelection: Int {
        get { settings.dailyGoal }
        set { setDailyGoal(newValue) }
    }

    var sessionSizeSelection: Int {
        get { settings.defaultSessionSize }
        set { setSessionSize(newValue) }
    }

    var themeSelection: ThemeChoice {
        get { settings.theme }
        set { setTheme(newValue) }
    }

    var continueTarget: ContinueTarget? {
        let due = packs.filter { $0.dueCount > 0 }
        if let best = due.max(by: { $0.dueCount < $1.dueCount }) {
            return ContinueTarget(pack: best, count: best.dueCount, isDue: true)
        }
        if let fresh = packs.first(where: { $0.newCount > 0 }) {
            return ContinueTarget(pack: fresh, count: fresh.newCount, isDue: false)
        }
        return nil
    }

    func pack(withId id: String) -> PackInfo? {
        packs.first { $0.id == id }
    }

    func start() {
        guard watchers.isEmpty else { return }
        let client = self.client
        samples = client.samplePacks()
        let packsTask = Task {
            await client.watchPacks { [weak self] packs in
                self?.applyPacks(packs)
            }
        }
        let statsTask = Task {
            await client.watchStats { [weak self] stats in
                self?.applyStats(stats)
            }
        }
        let settingsTask = Task {
            await client.watchSettings { [weak self] settings in
                self?.applySettings(settings)
            }
        }
        watchers = [packsTask, statsTask, settingsTask]
    }

    func refresh() {
        client.refresh()
    }

    func beginStudy(_ request: StudyRequest) {
        studyRequest = request
    }

    func continueLearning() {
        guard let target = continueTarget else { return }
        studyRequest = .learn(pack: target.pack)
    }

    func presentAddPack() {
        addPackRoute = AddPackRoute()
    }

    func handleIncoming(_ url: URL) {
        guard url.isFileURL else { return }
        addPackRoute = AddPackRoute(incomingURL: url)
    }

    func completeOnboarding() {
        settings.onboardingSeen = true
        Task { [client = self.client] in
            await client.setOnboardingSeen(true)
        }
    }

    func setDailyGoal(_ goal: Int) {
        let value = min(max(goal, 1), 1000)
        guard value != settings.dailyGoal else { return }
        settings.dailyGoal = value
        Task { [client = self.client] in
            await client.setDailyGoal(value)
        }
    }

    func setSessionSize(_ size: Int) {
        let value = min(max(size, 1), 500)
        guard value != settings.defaultSessionSize else { return }
        settings.defaultSessionSize = value
        Task { [client = self.client] in
            await client.setDefaultSessionSize(value)
        }
    }

    func setTheme(_ theme: ThemeChoice) {
        guard theme != settings.theme else { return }
        settings.theme = theme
        Task { [client = self.client] in
            await client.setTheme(theme)
        }
    }

    func importSample(_ sample: SamplePackInfo) async -> ImportOutcome {
        await client.importSample(id: sample.id)
    }

    func authoringPrompt() -> String {
        client.authoringPrompt()
    }

    private func applyPacks(_ value: [PackInfo]) {
        packs = value
        packsLoaded = true
    }

    private func applyStats(_ value: StatsInfo) {
        stats = value
    }

    private func applySettings(_ value: SettingsInfo) {
        settings = value
        settingsLoaded = true
    }
}
