import CloudKit
import Foundation

// CloudKit record layout. The names here are the schema in iosApp/cloudkit/schema.ckdb: change both together.
enum CloudRecords {
    static let containerID = "iCloud.ch.orellbuehler.jackdaw"
    static let zoneID = CKRecordZone.ID(zoneName: "JackdawZone", ownerName: CKCurrentUserDefaultName)

    enum RecordType {
        static let pack = "Pack"
        static let review = "ReviewLog"
        static let setting = "Setting"
    }

    static func recordID(_ name: String) -> CKRecord.ID {
        CKRecord.ID(recordName: name, zoneID: zoneID)
    }

    static func recordType(for kind: SyncKind) -> String {
        switch kind {
        case .pack: return RecordType.pack
        case .review: return RecordType.review
        case .setting: return RecordType.setting
        }
    }

    // Change tags are only needed for records that are saved more than once.
    static func keepsSystemFields(_ type: String) -> Bool {
        type != RecordType.review
    }

    static func encodeSystemFields(_ record: CKRecord) -> String {
        let coder = NSKeyedArchiver(requiringSecureCoding: true)
        record.encodeSystemFields(with: coder)
        coder.finishEncoding()
        return coder.encodedData.base64EncodedString()
    }

    static func decodeSystemFields(_ text: String) -> CKRecord? {
        guard let data = Data(base64Encoded: text), let coder = try? NSKeyedUnarchiver(forReadingFrom: data) else { return nil }
        coder.requiresSecureCoding = true
        let record = CKRecord(coder: coder)
        coder.finishDecoding()
        return record
    }

    static func baseRecord(type: String, name: String, systemFields: String?) -> CKRecord {
        if let systemFields, let record = decodeSystemFields(systemFields), record.recordType == type, record.recordID.recordName == name {
            return record
        }
        return CKRecord(recordType: type, recordID: recordID(name))
    }

    static func fill(_ record: CKRecord, pack: PackWire, assetURL: URL?) {
        record.setString("packId", pack.packId)
        record.setInt("importedAt", pack.importedAt)
        record.setInt("contentAt", pack.contentAt)
        record.setInt("resetAt", pack.resetAt)
        record.setInt("deletedAt", pack.deletedAt)
        if let assetURL {
            record["source"] = CKAsset(fileURL: assetURL)
        } else {
            record["source"] = nil
        }
    }

    static func fill(_ record: CKRecord, review: ReviewWire) {
        record.setString("logId", review.id)
        record.setInt("ts", review.ts)
        record.setInt("day", review.day)
        record.setString("packId", review.packId)
        record.setString("itemId", review.itemId)
        record.setInt("facet", review.facet)
        record.setInt("exercise", review.exercise)
        record.setInt("grade", review.grade)
        record.setInt("correct", review.correct ? 1 : 0)
        record.setInt("practice", review.practice ? 1 : 0)
        record.setInt("reschedule", review.reschedule ? 1 : 0)
        record.setString("itemType", review.itemType)
    }

    static func fill(_ record: CKRecord, setting: SettingWire) {
        record.setString("name", setting.name)
        record.setString("value", setting.value)
        record.setInt("updatedAt", setting.updatedAt)
    }

    static func pack(from record: CKRecord, source: String?) -> PackWire? {
        guard let packId = record.string("packId") else { return nil }
        return PackWire(
            recordName: record.recordID.recordName,
            packId: packId,
            source: source,
            importedAt: record.int("importedAt"),
            contentAt: record.int("contentAt"),
            resetAt: record.int("resetAt"),
            deletedAt: record.int("deletedAt"),
            version: 0
        )
    }

    static func review(from record: CKRecord) -> ReviewWire? {
        guard let id = record.string("logId"), let packId = record.string("packId"), let itemId = record.string("itemId") else {
            return nil
        }
        return ReviewWire(
            recordName: record.recordID.recordName,
            id: id,
            ts: record.int("ts"),
            day: record.int("day"),
            packId: packId,
            itemId: itemId,
            facet: record.int("facet"),
            exercise: record.int("exercise"),
            grade: record.int("grade"),
            correct: record.int("correct") != 0,
            practice: record.int("practice") != 0,
            reschedule: record.int("reschedule") != 0,
            itemType: record.string("itemType") ?? "",
            version: 0
        )
    }

    static func setting(from record: CKRecord) -> SettingWire? {
        guard let name = record.string("name"), let value = record.string("value") else { return nil }
        return SettingWire(
            recordName: record.recordID.recordName,
            name: name,
            value: value,
            updatedAt: record.int("updatedAt"),
            version: 0
        )
    }
}

extension CKRecord {
    func setString(_ key: String, _ value: String) {
        self[key] = value as NSString
    }

    func setInt(_ key: String, _ value: Int64) {
        self[key] = NSNumber(value: value)
    }

    func string(_ key: String) -> String? {
        self[key] as? String
    }

    func int(_ key: String) -> Int64 {
        (self[key] as? NSNumber)?.int64Value ?? 0
    }
}

enum SyncErrorText {
    static func describe(_ error: Error) -> String {
        let ns = error as NSError
        var lines: [String] = []
        if let ck = error as? CKError {
            lines.append("CKError \(ck.code.rawValue) (\(name(of: ck.code))): \(ck.localizedDescription)")
            if let server = ns.userInfo["ServerErrorDescription"] as? String {
                lines.append("server: \(server)")
            }
            if let retry = ck.retryAfterSeconds {
                lines.append("retry after \(Int(retry)) s")
            }
            if let partial = ck.partialErrorsByItemID, !partial.isEmpty {
                for (key, value) in partial.prefix(5) {
                    lines.append("  \(key): \(describe(value))")
                }
                if partial.count > 5 {
                    lines.append("  and \(partial.count - 5) more")
                }
            }
        } else {
            lines.append("\(ns.domain) \(ns.code): \(ns.localizedDescription)")
        }
        if let underlying = ns.userInfo[NSUnderlyingErrorKey] as? NSError {
            lines.append("underlying: \(underlying.domain) \(underlying.code): \(underlying.localizedDescription)")
        }
        return lines.joined(separator: "\n")
    }

    static func name(of code: CKError.Code) -> String {
        switch code {
        case .internalError: return "internalError"
        case .partialFailure: return "partialFailure"
        case .networkUnavailable: return "networkUnavailable"
        case .networkFailure: return "networkFailure"
        case .badContainer: return "badContainer"
        case .serviceUnavailable: return "serviceUnavailable"
        case .requestRateLimited: return "requestRateLimited"
        case .missingEntitlement: return "missingEntitlement"
        case .notAuthenticated: return "notAuthenticated"
        case .permissionFailure: return "permissionFailure"
        case .unknownItem: return "unknownItem"
        case .invalidArguments: return "invalidArguments"
        case .serverRecordChanged: return "serverRecordChanged"
        case .serverRejectedRequest: return "serverRejectedRequest"
        case .assetFileNotFound: return "assetFileNotFound"
        case .assetFileModified: return "assetFileModified"
        case .incompatibleVersion: return "incompatibleVersion"
        case .constraintViolation: return "constraintViolation"
        case .operationCancelled: return "operationCancelled"
        case .changeTokenExpired: return "changeTokenExpired"
        case .batchRequestFailed: return "batchRequestFailed"
        case .zoneBusy: return "zoneBusy"
        case .badDatabase: return "badDatabase"
        case .quotaExceeded: return "quotaExceeded"
        case .zoneNotFound: return "zoneNotFound"
        case .limitExceeded: return "limitExceeded"
        case .userDeletedZone: return "userDeletedZone"
        case .accountTemporarilyUnavailable: return "accountTemporarilyUnavailable"
        default: return "code \(code.rawValue)"
        }
    }
}
