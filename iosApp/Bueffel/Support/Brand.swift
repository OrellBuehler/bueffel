import Foundation
import SwiftUI
import UIKit

extension Color {
    init(hex: UInt32, opacity: Double = 1) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >> 8) & 0xFF) / 255,
            blue: Double(hex & 0xFF) / 255,
            opacity: opacity
        )
    }

    static func adaptive(light: UInt32, dark: UInt32) -> Color {
        Color(
            uiColor: UIColor { traits in
                let hex = traits.userInterfaceStyle == .dark ? dark : light
                return UIColor(
                    red: CGFloat((hex >> 16) & 0xFF) / 255,
                    green: CGFloat((hex >> 8) & 0xFF) / 255,
                    blue: CGFloat(hex & 0xFF) / 255,
                    alpha: 1
                )
            }
        )
    }
}

extension Font {
    static func brand(_ style: Font.TextStyle, weight: Font.Weight = .regular) -> Font {
        .system(style, design: .rounded, weight: weight)
    }
}

enum Brand {
    static let violet = Color(hex: 0x5B47E0)
    static let amber = Color(hex: 0xFFB238)
    static let ink = Color(hex: 0x17142A)
    static let cream = Color(hex: 0xFAF7F2)

    static let accent = Color.adaptive(light: 0x5B47E0, dark: 0x9C8CFF)
    static let success = Color.adaptive(light: 0x1F9D68, dark: 0x3DCB91)
    static let error = Color.adaptive(light: 0xE5484D, dark: 0xFF6B70)
    static let flame = Color.adaptive(light: 0xF08A00, dark: 0xFFB238)

    static let background = Color.adaptive(light: 0xFAF7F2, dark: 0x100E1D)
    static let surface = Color.adaptive(light: 0xFFFFFF, dark: 0x1C1930)
    static let surfaceMuted = Color.adaptive(light: 0xF1EDF8, dark: 0x27233F)
    static let hairline = Color.adaptive(light: 0xE6E0F0, dark: 0x2F2A4B)
    static let text = Color.adaptive(light: 0x17142A, dark: 0xF4F1FB)
    static let textSecondary = Color.adaptive(light: 0x625C7A, dark: 0xA9A3C2)
}

struct PackStyle {
    let base: Color
    let deep: Color
    let onBase: Color
    let accentText: Color
    let isLightBase: Bool

    init(hex: String?) {
        let value = PackStyle.parse(hex) ?? 0x5B47E0
        let deepValue = PackStyle.scale(value, by: 0.74)
        let lightValue = PackStyle.mix(value, with: 0xFFFFFF, amount: 0.4)
        let light = PackStyle.luminance(value) > 0.2
        base = Color(hex: value)
        deep = Color(hex: deepValue)
        onBase = light ? Brand.ink : Color.white
        accentText = Color.adaptive(light: deepValue, dark: lightValue)
        isLightBase = light
    }

    var gradient: LinearGradient {
        LinearGradient(colors: [base, deep], startPoint: .topLeading, endPoint: .bottomTrailing)
    }

    static func parse(_ hex: String?) -> UInt32? {
        guard var text = hex?.trimmingCharacters(in: .whitespacesAndNewlines) else { return nil }
        if text.hasPrefix("#") { text.removeFirst() }
        guard text.count == 6, let value = UInt32(text, radix: 16) else { return nil }
        return value
    }

    static func luminance(_ value: UInt32) -> Double {
        func channel(_ shift: UInt32) -> Double {
            let c = Double((value >> shift) & 0xFF) / 255
            return c <= 0.03928 ? c / 12.92 : pow((c + 0.055) / 1.055, 2.4)
        }
        return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
    }

    static func scale(_ value: UInt32, by factor: Double) -> UInt32 {
        func channel(_ shift: UInt32) -> UInt32 {
            let c = Double((value >> shift) & 0xFF) * factor
            return UInt32(min(max(c, 0), 255))
        }
        return (channel(16) << 16) | (channel(8) << 8) | channel(0)
    }

    static func mix(_ value: UInt32, with other: UInt32, amount: Double) -> UInt32 {
        func channel(_ shift: UInt32) -> UInt32 {
            let a = Double((value >> shift) & 0xFF)
            let b = Double((other >> shift) & 0xFF)
            return UInt32(min(max(a + (b - a) * amount, 0), 255))
        }
        return (channel(16) << 16) | (channel(8) << 8) | channel(0)
    }
}
