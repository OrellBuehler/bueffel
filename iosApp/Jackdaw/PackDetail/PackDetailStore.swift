import Foundation
import Observation

@MainActor
@Observable
final class PackDetailStore {
    let packId: String
    private(set) var detail: PackDetailInfo?
    private(set) var isGone = false
    private(set) var preview: SessionPreviewInfo = .empty
    private(set) var previewLoaded = false

    private let client = SDKClient.shared

    init(packId: String) {
        self.packId = packId
    }

    var previewKey: String {
        guard let pack = detail?.pack else { return "none" }
        return "\(pack.dueCount)-\(pack.newCount)-\(pack.learningCount)-\(pack.masteredCount)"
    }

    func watch() async {
        await client.watchPack(packId) { [weak self] detail in
            self?.apply(detail)
        }
    }

    func loadPreview() async {
        guard let pack = detail?.pack else { return }
        let value = await client.previewSession(.learn(pack: pack))
        preview = value
        previewLoaded = true
    }

    func sessionPreview(for request: StudyRequest) async -> SessionPreviewInfo {
        await client.previewSession(request)
    }

    func resetProgress() async {
        await client.resetProgress(packId)
    }

    func delete() async {
        await client.deletePack(packId)
    }

    func exportFile() async -> URL? {
        guard let title = detail?.pack.title, let text = await client.exportPack(packId) else { return nil }
        return try? PackFile.exportURL(title: title, text: text)
    }

    private func apply(_ value: PackDetailInfo?) {
        detail = value
        if value == nil { isGone = true }
    }
}

@MainActor
@Observable
final class ItemsStore {
    private(set) var items: [ItemInfo] = []
    private(set) var isLoaded = false

    private let packId: String
    private let client = SDKClient.shared

    init(packId: String) {
        self.packId = packId
    }

    func watch() async {
        await client.watchItems(packId) { [weak self] items in
            self?.apply(items)
        }
    }

    private func apply(_ value: [ItemInfo]) {
        items = value
        isLoaded = true
    }
}
