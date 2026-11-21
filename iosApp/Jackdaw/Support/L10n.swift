import Foundation

enum L10n {
    static func tr(_ key: String) -> String {
        NSLocalizedString(key, comment: "")
    }

    static func format(_ key: String, _ arguments: CVarArg...) -> String {
        String(format: NSLocalizedString(key, comment: ""), arguments: arguments)
    }

    static func plural(_ key: String, _ count: Int) -> String {
        let resolved = count == 1 ? key + ".one" : key + ".other"
        return String(format: NSLocalizedString(resolved, comment: ""), count)
    }
}
