import SwiftUI

struct PackCardView: View {
    let pack: PackInfo
    @Environment(\.colorScheme) private var scheme

    private var style: PackStyle { PackStyle(hex: pack.colorHex) }

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack(alignment: .top, spacing: 14) {
                EmojiTile(icon: pack.icon, style: style, size: 56)
                VStack(alignment: .leading, spacing: 4) {
                    Text(pack.title)
                        .font(.brand(.headline, weight: .bold))
                        .foregroundStyle(Brand.text)
                        .lineLimit(2)
                        .multilineTextAlignment(.leading)
                    Text(subtitle)
                        .font(.brand(.footnote))
                        .foregroundStyle(Brand.textSecondary)
                        .lineLimit(2)
                        .multilineTextAlignment(.leading)
                }
                Spacer(minLength: 0)
                Image(systemName: "chevron.right")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundStyle(Brand.textSecondary.opacity(0.6))
                    .padding(.top, 4)
            }
            HStack(spacing: 10) {
                ProgressBar(value: pack.progress, tint: style.accentText, track: style.base.opacity(0.18), height: 8)
                Text(percentText)
                    .font(.brand(.caption, weight: .bold))
                    .foregroundStyle(style.accentText)
                    .monospacedDigit()
            }
            badges
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background {
            RoundedRectangle(cornerRadius: 24, style: .continuous)
                .fill(Brand.surface)
                .overlay {
                    RoundedRectangle(cornerRadius: 24, style: .continuous)
                        .fill(style.base.opacity(scheme == .dark ? 0.14 : 0.07))
                }
        }
        .overlay {
            RoundedRectangle(cornerRadius: 24, style: .continuous)
                .strokeBorder(style.base.opacity(scheme == .dark ? 0.32 : 0.22), lineWidth: 1)
        }
        .shadow(color: Color.black.opacity(scheme == .dark ? 0 : 0.05), radius: 14, x: 0, y: 6)
        .accessibilityElement(children: .combine)
    }

    private var subtitle: String {
        if let blurb = pack.blurb, !blurb.isEmpty { return blurb }
        return L10n.plural("items", pack.itemCount)
    }

    private var percentText: String {
        L10n.format("pack.progress.percent", Int((pack.progress * 100).rounded()))
    }

    private var badges: some View {
        HStack(spacing: 8) {
            if pack.dueCount > 0 {
                Chip(text: L10n.plural("pack.badge.due", pack.dueCount), systemImage: "clock.fill", tint: Brand.flame)
            }
            if pack.newCount > 0 {
                Chip(text: L10n.plural("pack.badge.new", pack.newCount), systemImage: "sparkles", tint: style.accentText)
            }
            if pack.dueCount == 0 && pack.newCount == 0 {
                Chip(text: L10n.tr("pack.badge.done"), systemImage: "checkmark", tint: Brand.success)
            }
            Spacer(minLength: 0)
            Text(L10n.plural("items", pack.itemCount))
                .font(.brand(.caption))
                .foregroundStyle(Brand.textSecondary)
        }
    }
}
