import SwiftUI

struct SettingsView: View {
    @Environment(AppModel.self) private var model

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    StudySettingsCard(model: model)
                    AppearanceSettingsCard(model: model)
                    AboutCard()
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)
                .padding(.bottom, 32)
            }
            .background(Brand.background.ignoresSafeArea())
            .navigationTitle("settings.title")
        }
    }
}

private struct StudySettingsCard: View {
    @Bindable var model: AppModel

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            SettingsHeader(symbol: "target", tint: Brand.accent, key: "settings.study")
            Stepper(value: $model.dailyGoalSelection, in: 1 ... 1000, step: 5) {
                SettingsValueRow(key: "settings.goal", value: String(model.dailyGoalSelection))
            }
            Text("settings.goal.footer")
                .font(.brand(.footnote))
                .foregroundStyle(Brand.textSecondary)
            Divider()
            Stepper(value: $model.sessionSizeSelection, in: 1 ... 500, step: 5) {
                SettingsValueRow(key: "settings.session", value: String(model.sessionSizeSelection))
            }
            Text("settings.session.footer")
                .font(.brand(.footnote))
                .foregroundStyle(Brand.textSecondary)
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .card(radius: 24)
    }
}

private struct AppearanceSettingsCard: View {
    @Bindable var model: AppModel

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            SettingsHeader(symbol: "paintbrush.fill", tint: Brand.flame, key: "settings.appearance")
            Picker("settings.theme", selection: $model.themeSelection) {
                Text("settings.theme.system").tag(ThemeChoice.system)
                Text("settings.theme.light").tag(ThemeChoice.light)
                Text("settings.theme.dark").tag(ThemeChoice.dark)
            }
            .pickerStyle(.segmented)
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .card(radius: 24)
    }
}

private struct SettingsHeader: View {
    let symbol: String
    let tint: Color
    let key: LocalizedStringKey

    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: symbol)
                .font(.system(size: 15, weight: .semibold))
                .foregroundStyle(tint)
                .frame(width: 32, height: 32)
                .background(tint.opacity(0.14), in: RoundedRectangle(cornerRadius: 10, style: .continuous))
            Text(key)
                .font(.brand(.headline, weight: .bold))
                .foregroundStyle(Brand.text)
        }
    }
}

private struct SettingsValueRow: View {
    let key: LocalizedStringKey
    let value: String

    var body: some View {
        HStack {
            Text(key)
                .font(.brand(.body, weight: .medium))
                .foregroundStyle(Brand.text)
            Spacer(minLength: 8)
            Text(value)
                .font(.brand(.body, weight: .bold))
                .foregroundStyle(Brand.accent)
                .monospacedDigit()
        }
    }
}

private struct AboutCard: View {
    private var versionText: String {
        let info = Bundle.main.infoDictionary
        let version = info?["CFBundleShortVersionString"] as? String ?? "1.0"
        let build = info?["CFBundleVersion"] as? String ?? "1"
        return version + " (" + build + ")"
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack(spacing: 14) {
                Mascot(size: 56)
                VStack(alignment: .leading, spacing: 2) {
                    Text("settings.about.name")
                        .font(.brand(.title3, weight: .bold))
                        .foregroundStyle(Brand.text)
                    Text(L10n.format("settings.about.version", versionText))
                        .font(.brand(.footnote))
                        .foregroundStyle(Brand.textSecondary)
                }
            }
            Label("settings.about.local", systemImage: "lock.shield.fill")
                .font(.brand(.subheadline))
                .foregroundStyle(Brand.text)
                .labelStyle(AboutLabelStyle())
            Label("settings.about.format", systemImage: "doc.text.fill")
                .font(.brand(.subheadline))
                .foregroundStyle(Brand.text)
                .labelStyle(AboutLabelStyle())
            if let url = URL(string: "https://github.com/OrellBuehler/bueffel/tree/main/format") {
                Link(destination: url) {
                    Label("settings.about.spec", systemImage: "arrow.up.right.square")
                        .font(.brand(.subheadline, weight: .semibold))
                }
            }
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .card(radius: 24)
    }
}

private struct AboutLabelStyle: LabelStyle {
    func makeBody(configuration: Configuration) -> some View {
        HStack(alignment: .top, spacing: 10) {
            configuration.icon
                .foregroundStyle(Brand.accent)
                .frame(width: 22)
            configuration.title
        }
    }
}
