import SwiftUI
import ConfettiKit

@main
struct iOSApp: App {
    init() {
        KoinKt.doInitKoin()
    }
    
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
