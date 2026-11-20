@preconcurrency import shared
import Foundation

enum ImportAdapter {
    static func outcome(_ result: ImportResult) -> ImportOutcome {
        switch onEnum(of: result) {
        case .success(let ok):
            return .success(
                ImportDoneInfo(
                    packId: ok.packId,
                    title: ok.title,
                    itemCount: Int(ok.itemCount),
                    skippedCount: Int(ok.skippedCount),
                    isUpdate: ok.isUpdate,
                    counts: counts(ok.counts),
                    warnings: warnings(ok.warnings)
                )
            )
        case .failure(let bad):
            return .failure(issue(bad.error))
        default:
            return .failure(ImportIssue(kind: .other, message: ""))
        }
    }

    static func previewOutcome(_ preview: ImportPreview) -> PreviewOutcome {
        switch onEnum(of: preview) {
        case .valid(let ok):
            return .valid(
                ImportPreviewInfo(
                    packId: ok.packId,
                    title: ok.title,
                    blurb: ok.descriptionText,
                    icon: ok.icon,
                    colorHex: ok.color,
                    language: ok.language,
                    revision: Int(ok.revision),
                    itemCount: Int(ok.itemCount),
                    sectionCount: Int(ok.sectionCount),
                    typeCounts: Mapping.typeCounts(ok.typeCounts),
                    skippedCount: Int(ok.skippedCount),
                    warnings: warnings(ok.warnings),
                    isUpdate: ok.isUpdate,
                    installedTitle: ok.installedTitle,
                    installedRevision: Int(ok.installedRevision),
                    isDowngrade: ok.isDowngrade,
                    counts: counts(ok.counts)
                )
            )
        case .invalid(let bad):
            return .invalid(issue(bad.error))
        default:
            return .invalid(ImportIssue(kind: .other, message: ""))
        }
    }

    static func counts(_ c: ImportCounts) -> ImportCountsInfo {
        ImportCountsInfo(
            added: Int(c.addedItems),
            kept: Int(c.keptItems),
            reset: Int(c.resetItems),
            removed: Int(c.removedItems)
        )
    }

    static func warnings(_ list: [ImportWarning]) -> [ImportWarningInfo] {
        list.enumerated().map { index, warning in
            ImportWarningInfo(
                id: index,
                kind: warningKind(warning.code),
                message: warning.message,
                itemId: warning.itemId,
                itemIndex: Int(warning.itemIndex),
                detail: warning.detail,
                count: Int(warning.count)
            )
        }
    }

    static func issue(_ error: ImportError) -> ImportIssue {
        ImportIssue(kind: issueKind(error.code), message: error.message)
    }

    static func issueKind(_ code: ImportErrorCode) -> ImportIssueKind {
        switch code {
        case .notJson, .notAnObject, .notAPack: return .notAPack
        case .versionTooNew: return .versionTooNew
        case .versionInvalid, .idInvalid, .titleInvalid: return .invalidPack
        case .itemsMissing, .noValidItems: return .noItems
        case .tooLarge, .tooManyItems: return .tooLarge
        case .revisionDowngrade: return .revisionDowngrade
        case .unknownSample: return .unknownSample
        case .storageError: return .storage
        default: return .other
        }
    }

    static func warningKind(_ code: ImportWarningCode) -> ImportWarningKind {
        switch code {
        case .itemUnknownType: return .unknownType
        case .moreWarnings: return .more
        default: return .other
        }
    }
}
