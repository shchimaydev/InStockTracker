import Shared
import SwiftUI

@main
struct iOSApp: App {
    init() {
        if let apiKey = Bundle.main.object(forInfoDictionaryKey: "RevenueCatApiKey") as? String {
            RevenueCatManager.shared.initialize(apiKey: apiKey)
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
