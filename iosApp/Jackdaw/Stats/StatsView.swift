import SwiftUI

struct StatsView: View {
    @Environment(AppModel.self) private var model

    private var stats: StatsInfo { model.stats }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    StreakCard(current: stats.currentStreak, longest: stats.longestStreak)
                    TodayCard(stats: stats)
                    tiles
                    ActivityBarsCard(days: Array(stats.activity.suffix(14)), goal: stats.dailyGoal)
                    HeatmapCard(days: stats.activity, goal: stats.dailyGoal)
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)
                .padding(.bottom, 32)
            }
            .background(Brand.background.ignoresSafeArea())
            .navigationTitle("stats.title")
        }
    }

    private var tiles: some View {
        let columns = [GridItem(.flexible(), spacing: 12), GridItem(.flexible(), spacing: 12)]
        return LazyVGrid(columns: columns, spacing: 12) {
            StatTile(
                symbol: "scope",
                tint: Brand.accent,
                value: L10n.format("pack.progress.percent", Int((stats.accuracy * 100).rounded())),
                key: "stats.accuracy"
            )
            StatTile(symbol: "graduationcap.fill", tint: Brand.success, value: String(stats.facetsMastered), key: "stats.mastered")
            StatTile(symbol: "brain.head.profile", tint: Brand.flame, value: String(stats.facetsLearned), key: "stats.learned")
            StatTile(symbol: "checkmark.circle.fill", tint: Brand.accent, value: String(stats.totalReviews), key: "stats.reviews")
        }
    }
}

private struct StatTile: View {
    let symbol: String
    let tint: Color
    let value: String
    let key: LocalizedStringKey

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Image(systemName: symbol)
                .font(.system(size: 17, weight: .semibold))
                .foregroundStyle(tint)
                .frame(width: 36, height: 36)
                .background(tint.opacity(0.14), in: RoundedRectangle(cornerRadius: 11, style: .continuous))
            Text(value)
                .font(.brand(.title, weight: .bold))
                .foregroundStyle(Brand.text)
                .monospacedDigit()
                .contentTransition(.numericText())
            Text(key)
                .font(.brand(.footnote))
                .foregroundStyle(Brand.textSecondary)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .card(radius: 22)
        .accessibilityElement(children: .combine)
    }
}

private struct StreakCard: View {
    let current: Int
    let longest: Int

    var body: some View {
        HStack(spacing: 16) {
            Image(systemName: "flame.fill")
                .font(.system(size: 52))
                .foregroundStyle(Brand.ink.opacity(current > 0 ? 0.9 : 0.4))
                .symbolEffect(.bounce, value: current)
            VStack(alignment: .leading, spacing: 2) {
                Text(String(current))
                    .font(.system(size: 44, weight: .heavy, design: .rounded))
                    .foregroundStyle(Brand.ink)
                    .contentTransition(.numericText())
                Text(L10n.plural("stats.streak.days", current))
                    .font(.brand(.subheadline, weight: .semibold))
                    .foregroundStyle(Brand.ink.opacity(0.75))
            }
            Spacer(minLength: 0)
            VStack(alignment: .trailing, spacing: 2) {
                Text("stats.streak.best")
                    .font(.brand(.caption, weight: .semibold))
                    .foregroundStyle(Brand.ink.opacity(0.7))
                Text(L10n.plural("stats.streak.days", longest))
                    .font(.brand(.subheadline, weight: .bold))
                    .foregroundStyle(Brand.ink)
            }
        }
        .padding(20)
        .background(
            LinearGradient(
                colors: [Brand.amber, Color(hex: 0xFF8A3D)],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            ),
            in: RoundedRectangle(cornerRadius: 28, style: .continuous)
        )
        .shadow(color: Brand.amber.opacity(0.35), radius: 16, x: 0, y: 8)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(
            L10n.format("stats.streak.a11y", L10n.plural("stats.streak.days", current), L10n.plural("stats.streak.days", longest))
        )
    }
}

private struct TodayCard: View {
    let stats: StatsInfo

    private var progress: Double {
        guard stats.dailyGoal > 0 else { return 0 }
        return Double(stats.reviewsToday) / Double(stats.dailyGoal)
    }

    var body: some View {
        HStack(spacing: 20) {
            ZStack {
                ProgressRing(progress: progress, lineWidth: 12, tint: stats.goalReached ? Brand.success : Brand.accent)
                VStack(spacing: 0) {
                    Text(String(stats.reviewsToday))
                        .font(.brand(.title, weight: .bold))
                        .foregroundStyle(Brand.text)
                        .contentTransition(.numericText())
                    Text(L10n.format("stats.today.of", stats.dailyGoal))
                        .font(.brand(.caption))
                        .foregroundStyle(Brand.textSecondary)
                }
            }
            .frame(width: 112, height: 112)
            VStack(alignment: .leading, spacing: 6) {
                Text("stats.today")
                    .font(.brand(.headline, weight: .bold))
                    .foregroundStyle(Brand.text)
                if stats.goalReached {
                    Label("stats.today.reached", systemImage: "checkmark.seal.fill")
                        .font(.brand(.subheadline, weight: .semibold))
                        .foregroundStyle(Brand.success)
                } else {
                    Text(L10n.plural("stats.today.togo", max(stats.dailyGoal - stats.reviewsToday, 0)))
                        .font(.brand(.subheadline))
                        .foregroundStyle(Brand.textSecondary)
                }
                Text(L10n.format("stats.today.correct", stats.correctToday))
                    .font(.brand(.footnote))
                    .foregroundStyle(Brand.textSecondary)
                if stats.dueToday > 0 {
                    Text(L10n.plural("library.due.total", stats.dueToday))
                        .font(.brand(.footnote, weight: .semibold))
                        .foregroundStyle(Brand.flame)
                }
            }
            Spacer(minLength: 0)
        }
        .padding(20)
        .frame(maxWidth: .infinity, alignment: .leading)
        .card(radius: 28)
        .accessibilityElement(children: .combine)
    }
}

private struct ActivityBarsCard: View {
    let days: [DayActivityInfo]
    let goal: Int

    private var peak: Int {
        max(days.map { $0.reviews }.max() ?? 0, goal, 1)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("stats.activity.recent")
                .font(.brand(.headline, weight: .bold))
                .foregroundStyle(Brand.text)
            HStack(alignment: .bottom, spacing: 6) {
                ForEach(days) { day in
                    bar(day)
                }
            }
            .frame(height: 150)
        }
        .padding(20)
        .frame(maxWidth: .infinity, alignment: .leading)
        .card(radius: 28)
    }

    private func bar(_ day: DayActivityInfo) -> some View {
        let ratio = CGFloat(day.reviews) / CGFloat(peak)
        let reached = goal > 0 && day.reviews >= goal
        let weekday = day.date.formatted(.dateTime.weekday(.wide).day().month())
        let accessibilityText = weekday + ": " + L10n.plural("stats.reviews.count", day.reviews)
        return VStack(spacing: 6) {
            Spacer(minLength: 0)
            if day.reviews > 0 {
                Text(String(day.reviews))
                    .font(.system(size: 9, weight: .semibold, design: .rounded))
                    .foregroundStyle(Brand.textSecondary)
                    .minimumScaleFactor(0.6)
                    .lineLimit(1)
            }
            RoundedRectangle(cornerRadius: 5, style: .continuous)
                .fill(reached ? Brand.success : Brand.accent)
                .opacity(day.reviews > 0 ? 1 : 0.15)
                .frame(height: max(4, 100 * ratio))
            Text(day.date.formatted(.dateTime.weekday(.narrow)))
                .font(.system(size: 10, weight: .medium, design: .rounded))
                .foregroundStyle(Brand.textSecondary)
        }
        .frame(maxWidth: .infinity)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(accessibilityText)
    }
}

private struct HeatmapCard: View {
    let days: [DayActivityInfo]
    let goal: Int

    private var weeks: [[DayActivityInfo?]] {
        guard let first = days.first else { return [] }
        let calendar = Calendar.current
        let offset = (calendar.component(.weekday, from: first.date) - calendar.firstWeekday + 7) % 7
        var cells: [DayActivityInfo?] = []
        for _ in 0 ..< offset { cells.append(nil) }
        for day in days { cells.append(day) }
        while cells.count % 7 != 0 { cells.append(nil) }
        var result: [[DayActivityInfo?]] = []
        var index = 0
        while index < cells.count {
            result.append(Array(cells[index ..< index + 7]))
            index += 7
        }
        return result
    }

    private var activeDays: Int { days.filter { $0.reviews > 0 }.count }

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                Text("stats.activity.weeks")
                    .font(.brand(.headline, weight: .bold))
                    .foregroundStyle(Brand.text)
                Spacer(minLength: 8)
                Text(L10n.plural("stats.activity.active", activeDays))
                    .font(.brand(.caption))
                    .foregroundStyle(Brand.textSecondary)
            }
            HStack(alignment: .top, spacing: 4) {
                ForEach(Array(weeks.enumerated()), id: \.offset) { _, week in
                    VStack(spacing: 4) {
                        ForEach(Array(week.enumerated()), id: \.offset) { _, day in
                            cell(day)
                        }
                    }
                    .frame(maxWidth: .infinity)
                }
            }
            legend
        }
        .padding(20)
        .frame(maxWidth: .infinity, alignment: .leading)
        .card(radius: 28)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(L10n.plural("stats.activity.active", activeDays))
    }

    @ViewBuilder
    private func cell(_ day: DayActivityInfo?) -> some View {
        if let day {
            RoundedRectangle(cornerRadius: 4, style: .continuous)
                .fill(color(level(day)))
                .aspectRatio(1, contentMode: .fit)
        } else {
            Color.clear
                .aspectRatio(1, contentMode: .fit)
        }
    }

    private var legend: some View {
        HStack(spacing: 4) {
            Text("stats.legend.less")
                .font(.brand(.caption2))
                .foregroundStyle(Brand.textSecondary)
            ForEach(0 ..< 5, id: \.self) { level in
                RoundedRectangle(cornerRadius: 3, style: .continuous)
                    .fill(color(level))
                    .frame(width: 12, height: 12)
            }
            Text("stats.legend.more")
                .font(.brand(.caption2))
                .foregroundStyle(Brand.textSecondary)
        }
        .frame(maxWidth: .infinity, alignment: .trailing)
    }

    private func level(_ day: DayActivityInfo) -> Int {
        if day.reviews <= 0 { return 0 }
        let ratio = Double(day.reviews) / Double(max(goal, 1))
        if ratio < 0.34 { return 1 }
        if ratio < 0.67 { return 2 }
        if ratio < 1 { return 3 }
        return 4
    }

    private func color(_ level: Int) -> Color {
        switch level {
        case 0: return Brand.hairline.opacity(0.7)
        case 1: return Brand.accent.opacity(0.28)
        case 2: return Brand.accent.opacity(0.5)
        case 3: return Brand.accent.opacity(0.75)
        default: return Brand.accent
        }
    }
}
