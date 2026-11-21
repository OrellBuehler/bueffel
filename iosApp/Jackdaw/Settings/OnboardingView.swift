import SwiftUI

struct OnboardingView: View {
    @Environment(AppModel.self) private var model
    @State private var page = 0

    private let lastPage = 3

    var body: some View {
        ZStack {
            Brand.background.ignoresSafeArea()
            VStack(spacing: 0) {
                topBar
                TabView(selection: $page) {
                    OnboardingPage(
                        art: AnyView(Mascot(size: 190, animated: true)),
                        titleKey: "onboarding.welcome.title",
                        messageKey: "onboarding.welcome.message"
                    )
                    .tag(0)
                    OnboardingPage(
                        art: AnyView(OnboardingSymbol(name: "calendar.badge.clock", tint: Brand.accent)),
                        titleKey: "onboarding.srs.title",
                        messageKey: "onboarding.srs.message"
                    )
                    .tag(1)
                    OnboardingPage(
                        art: AnyView(OnboardingSymbol(name: "lock.shield.fill", tint: Brand.success)),
                        titleKey: "onboarding.private.title",
                        messageKey: "onboarding.private.message"
                    )
                    .tag(2)
                    OnboardingSamplesPage()
                        .tag(3)
                }
                .tabViewStyle(.page(indexDisplayMode: .never))
                .animation(.smooth, value: page)
                dots
                bottomButton
            }
        }
        .sensoryFeedback(.selection, trigger: page)
    }

    private var topBar: some View {
        HStack {
            Spacer()
            Button {
                model.completeOnboarding()
            } label: {
                Text("onboarding.skip")
                    .font(.brand(.subheadline, weight: .semibold))
                    .foregroundStyle(Brand.textSecondary)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 10)
            }
            .opacity(page < lastPage ? 1 : 0)
            .disabled(page >= lastPage)
        }
        .padding(.horizontal, 8)
        .frame(height: 48)
    }

    private var dots: some View {
        HStack(spacing: 8) {
            ForEach(0 ... lastPage, id: \.self) { index in
                Capsule()
                    .fill(index == page ? Brand.accent : Brand.hairline)
                    .frame(width: index == page ? 22 : 8, height: 8)
            }
        }
        .animation(.snappy, value: page)
        .padding(.vertical, 14)
        .accessibilityHidden(true)
    }

    private var bottomButton: some View {
        Button {
            if page < lastPage {
                page += 1
            } else {
                model.completeOnboarding()
            }
        } label: {
            Text(L10n.tr(page < lastPage ? "onboarding.next" : "onboarding.start"))
        }
        .buttonStyle(BrandButtonStyle())
        .padding(.horizontal, 24)
        .padding(.bottom, 20)
    }
}

private struct OnboardingSymbol: View {
    let name: String
    let tint: Color

    var body: some View {
        ZStack {
            Circle()
                .fill(tint.opacity(0.14))
                .frame(width: 190, height: 190)
            Circle()
                .fill(tint.opacity(0.2))
                .frame(width: 140, height: 140)
            Image(systemName: name)
                .font(.system(size: 64, weight: .semibold))
                .foregroundStyle(tint)
        }
        .accessibilityHidden(true)
    }
}

private struct OnboardingPage: View {
    let art: AnyView
    let titleKey: LocalizedStringKey
    let messageKey: LocalizedStringKey

    var body: some View {
        VStack(spacing: 28) {
            Spacer(minLength: 0)
            art
            VStack(spacing: 12) {
                Text(titleKey)
                    .font(.brand(.largeTitle, weight: .bold))
                    .foregroundStyle(Brand.text)
                    .multilineTextAlignment(.center)
                Text(messageKey)
                    .font(.brand(.body))
                    .foregroundStyle(Brand.textSecondary)
                    .multilineTextAlignment(.center)
            }
            Spacer(minLength: 0)
        }
        .padding(.horizontal, 32)
    }
}

private struct OnboardingSamplesPage: View {
    @Environment(AppModel.self) private var model

    var body: some View {
        ScrollView {
            VStack(spacing: 14) {
                VStack(spacing: 8) {
                    Text("onboarding.samples.title")
                        .font(.brand(.title, weight: .bold))
                        .foregroundStyle(Brand.text)
                        .multilineTextAlignment(.center)
                    Text("onboarding.samples.message")
                        .font(.brand(.subheadline))
                        .foregroundStyle(Brand.textSecondary)
                        .multilineTextAlignment(.center)
                }
                .padding(.bottom, 6)
                ForEach(model.samples) { sample in
                    SamplePackRow(sample: sample)
                }
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 16)
        }
    }
}
