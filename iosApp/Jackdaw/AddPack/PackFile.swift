import Foundation
import UniformTypeIdentifiers

enum PackFile {
    enum ReadError: Error {
        case notText
    }

    static let contentTypes: [UTType] = [.json, .plainText, .data]

    static func readText(from url: URL) async throws -> String {
        let scoped = url.startAccessingSecurityScopedResource()
        defer {
            if scoped { url.stopAccessingSecurityScopedResource() }
        }
        let data = try Data(contentsOf: url)
        guard let text = String(data: data, encoding: .utf8) else {
            throw ReadError.notText
        }
        return text
    }

    static func exportURL(title: String, text: String) throws -> URL {
        let allowed = CharacterSet.alphanumerics.union(CharacterSet(charactersIn: " -_."))
        let cleaned = String(title.unicodeScalars.map { allowed.contains($0) ? Character($0) : "-" })
            .trimmingCharacters(in: CharacterSet(charactersIn: " .-"))
        let name = cleaned.isEmpty ? "pack" : cleaned
        let directory = FileManager.default.temporaryDirectory.appendingPathComponent("exports", isDirectory: true)
        try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
        let url = directory.appendingPathComponent(name + ".jackdaw")
        try Data(text.utf8).write(to: url, options: .atomic)
        return url
    }
}
