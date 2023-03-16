import SwiftUI
import ConfettiKit
import KMMViewModelCore
import KMMViewModelSwiftUI
import BackgroundTasks


@main
struct iOSApp: App {
    @Environment(\.scenePhase) private var phase

    init() {
        KoinKt.doInitKoin()
    }
    
    var body: some Scene {
		WindowGroup {
            ConfettiApp()
		}
        .onChange(of: phase) { newPhase in
          switch newPhase {
          case .background: scheduleDataRefresh()
          default: break
          }
        }
        .backgroundTask(.appRefresh("refreshData")) {
            JobConferenceRefresh().refresh()
        }
    }
}

func scheduleDataRefresh() {
    let request = BGAppRefreshTaskRequest(identifier: "refreshData")
    request.earliestBeginDate = .now.addingTimeInterval(24 * 3600)
    try? BGTaskScheduler.shared.submit(request)
}
