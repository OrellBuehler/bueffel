import shared
import SwiftUI

struct ContentView: View {
    var body: some View {
        Text(AppInfoKt.appName())
            .font(.largeTitle.bold())
            .foregroundStyle(.tint)
    }
}
