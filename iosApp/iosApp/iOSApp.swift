import SwiftUI
import ConfettiKit
import KMMViewModelCore
import KMMViewModelSwiftUI
import BackgroundTasks


@main
struct iOSApp: App {
    @Environment(\.scenePhase) private var phase
    @StateViewModel var viewModel = ConfettiViewModel()

    init() {
        KoinKt.doInitKoin()
    }
    
    var body: some Scene {
		WindowGroup {
            ContentView(viewModel: $viewModel)
		}
        .onChange(of: phase) { newPhase in
          switch newPhase {
          case .background: scheduleDataRefresh()
          default: break
          }
        }
        .backgroundTask(.appRefresh("refreshData")) {
            do {
                try await viewModel.refresh()
            } catch {
                print("Failed with error: \(error)")
            }
        }
    }
}

func scheduleDataRefresh() {
    let request = BGAppRefreshTaskRequest(identifier: "refreshData")
    request.earliestBeginDate = .now.addingTimeInterval(24 * 3600)
    try? BGTaskScheduler.shared.submit(request)
}
