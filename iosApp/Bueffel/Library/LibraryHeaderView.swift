import SwiftUI

struct StreakBadge: View {
    let days: Int

    var body: some View {
        VStack(spacing: 2) {
            Image(systemName: "flame.fill")
                .font(.system(size: 28))
                .foregroundStyle(days > 0 ? Brand.flame : Brand.textSecondary.opacity(0.45))
                .symbolEffect(.bounce, value: days)
            Text(String(days))
                .font(.brand(.title3, weight: .bold))
                .foregroundStyle(Brand.text)
                .contentTransition(.numericText())
            Text("library.streak")
                .font(.brand(.caption2, weight: .semibold))
                .foregroundStyle(Brand.textSecondary)
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(L10n.plural("library.streak.label", days))
    }
}

struct LibraryHeaderView: View {
    @Environment(AppModel.self) private var model

    private var stats: StatsInfo { model.stats }

    private var goalProgress: Double {
        guard stats.dailyGoal > 0 else { return 0 }
        return Double(stats.reviewsToday) / Double(stats.dailyGoal)
    }

    var body: some View {
        VStack(spacing: 18) {
            HStack(spacing: 16) {
                ring
                VStack(alignment: .leading, spacing: 4) {
                    Text("library.today")
                        .font(.brand(.headline, weight: .bold))
                        .foregroundStyle(Brand.text)
                    Text(L10n.format("library.goal.progress", stats.reviewsToday, stats.dailyGoal))
                        .font(.brand(.subheadline))
                        .foregroundStyle(Brand.textSecondary)
                    dueLine
                }
                Spacer(minLength: 8)
                StreakBadge(days: stats.currentStreak)
            }
            continueButton
        }
        .padding(18)
        .card(radius: 28)
    }

    private var ring: some View {
        ZStack {
            ProgressRing(
                progress: goalProgress,
                lineWidth: 9,
                tint: stats.goalReached ? Brand.success : Brand.accent
            )
            if stats.goalReached {
                Image(systemName: "checkmark")
                    .font(.system(size: 24, weight: .heavy))
                    .foregroundStyle(Brand.success)
            } else {
                Text(String(stats.reviewsToday))
                    .font(.brand(.title3, weight: .bold))
                    .foregroundStyle(Brand.text)
                    .contentTransition(.numericText())
            }
        }
        .frame(width: 76, height: 76)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(L10n.format("library.goal.progress", stats.reviewsToday, stats.dailyGoal))
    }

    @ViewBuilder
    private var dueLine: some View {
        if stats.dueToday > 0 {
            Label(L10n.plural("library.due.total", stats.dueToday), systemImage: "clock.badge.exclamationmark.fill")
                .font(.brand(.footnote, weight: .semibold))
                .foregroundStyle(Brand.flame)
        } else {
            Label("library.due.none", systemImage: "checkmark.circle.fill")
                .font(.brand(.footnote, weight: .semibold))
                .foregroundStyle(Brand.success)
        }
    }

    @ViewBuilder
    private var continueButton: some View {
        if let target = model.continueTarget {
            Button {
                model.continueLearning()
            } label: {
                HStack(spacing: 12) {
                    EmojiTile(icon: target.pack.icon, style: PackStyle(hex: target.pack.colorHex), size: 40)
                    VStack(alignment: .leading, spacing: 2) {
                        Text(L10n.tr(target.isDue ? "library.continue" : "library.start"))
                            .font(.brand(.headline, weight: .bold))
                        Text(subtitle(for: target))
                            .font(.brand(.caption))
                            .opacity(0.85)
                            .lineLimit(1)
                    }
                    Spacer(minLength: 8)
                    Image(systemName: "play.fill")
                        .font(.system(size: 16, weight: .bold))
                }
            }
            .buttonStyle(BrandButtonStyle())
        } else {
            HStack(spacing: 8) {
                Image(systemName: "checkmark.seal.fill")
                    .foregroundStyle(Brand.success)
                Text("library.caughtup")
                    .font(.brand(.subheadline, weight: .semibold))
                    .foregroundStyle(Brand.textSecondary)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 12)
            .background(Brand.surfaceMuted, in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        }
    }

    private func subtitle(for target: ContinueTarget) -> String {
        let count = target.isDue
            ? L10n.plural("pack.badge.due", target.count)
            : L10n.plural("pack.badge.new", target.count)
        return target.pack.title + " · " + count
    }
}
