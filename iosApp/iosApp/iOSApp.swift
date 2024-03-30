import SwiftUI
import ConfettiKit
import BackgroundTasks


@main
struct iOSApp: App {
    @Environment(\.scenePhase) private var phase

    @UIApplicationDelegateAdaptor(AppDelegate.self)
    var appDelegate: AppDelegate
    
    var body: some Scene {
        WindowGroup {
            ConfettiApp(appDelegate.root)
                .onOpenURL(perform: { url in
                    let pathComponents = url.pathComponents
                    if pathComponents.count != 3 { return }
                    if pathComponents[1] != "conference" { return }
                    let conferenceId = pathComponents[2]
                    for char in conferenceId {
                        if !char.isLetter && !char.isNumber { return }
                    }
                    appDelegate.root.onConferenceDeepLink(conferenceId: conferenceId)
                })
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
