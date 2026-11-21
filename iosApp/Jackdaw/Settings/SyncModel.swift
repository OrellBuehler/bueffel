import Foundation
import Observation

@MainActor
@Observable
final class SyncModel {
    private(set) var status = SyncStatus.initial
    @ObservationIgnored private var updates: Task<Void, Never>?

    var isEnabled: Bool {
        get { status.isEnabled }
        set { setEnabled(newValue) }
    }

    func start() {
        guard updates == nil else { return }
        updates = Task { [weak self] in
            let stream = await CloudSync.shared.updates()
            for await value in stream {
                self?.status = value
            }
        }
        Task {
            await CloudSync.shared.start()
        }
    }

    func setEnabled(_ on: Bool) {
        guard on != status.isEnabled else { return }
        status.isEnabled = on
        Task {
            await CloudSync.shared.setEnabled(on)
        }
    }

    func syncNow() {
        Task {
            await CloudSync.shared.syncNow(userInitiated: true)
        }
    }

    func foreground() {
        Task {
            await CloudSync.shared.syncNow(userInitiated: false)
        }
    }
}
