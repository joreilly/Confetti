import SwiftUI
import FirebaseCore
import ConfettiKit
import BackgroundTasks


@main
struct iOSApp: App {
    @Environment(\.scenePhase) private var phase

    @UIApplicationDelegateAdaptor(AppDelegate.self)
    var appDelegate: AppDelegate
    
    init() {
        // Firebase initialization
        FirebaseApp.configure()
    }
    
    var body: some Scene {
        WindowGroup {
            ConfettiIosApp(appDelegate: appDelegate)
                .onOpenURL(perform: { url in
                    let pathComponents = url.pathComponents
                    if pathComponents.count != 3 { return }
                    if pathComponents[1] != "conference" { return }
                    let conferenceId = pathComponents[2]
                    for char in conferenceId {
                        if !char.isLetter && !char.isNumber { return }
                    }
                    appDelegate.onConferenceDeepLink(conferenceId: conferenceId)
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

struct ConfettiIosApp : View {
    @ObservedObject
    var appDelegate: AppDelegate
    
    
    var body: some View {
        ConfettiApp(appDelegate.appComponent)
    }
}

func scheduleDataRefresh() {
    let request = BGAppRefreshTaskRequest(identifier: "refreshData")
    request.earliestBeginDate = .now.addingTimeInterval(24 * 3600)
    try? BGTaskScheduler.shared.submit(request)
}
