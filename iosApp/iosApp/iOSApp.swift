import SwiftUI
import ConfettiKit
import BackgroundTasks


@main
struct iOSApp: App {
    @Environment(\.scenePhase) private var phase

    @UIApplicationDelegateAdaptor(AppDelegate.self)
    var appDelegate: AppDelegate
    
    @State
    private var reRenderIndex: Int = 0
    
    var body: some Scene {
        WindowGroup {
            EmptyView(reRenderIndex: reRenderIndex)
            ConfettiApp(appDelegate.root)
                .onOpenURL(perform: { url in
                    let pathComponents = url.pathComponents
                    if pathComponents.count != 3 { return }
                    if pathComponents[1] != "conference" { return }
                    let conferenceId = pathComponents[2]
                    for char in conferenceId {
                        if !char.isLetter && !char.isNumber { return }
                    }
                    appDelegate.onConferenceDeepLink(conferenceId: conferenceId)
                    reRenderIndex += 1
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

/**
 This view exists so that there is a way for us to read the change in `@State reloadIndex`, so that the view will try to re-render itself and pick up the latest value from appDelegate.root. This is done since `AppComponent` is not an object that can be observed by SwiftUI in some way, so this workaround was added.
 */
private struct EmptyView: View {
    var reRenderIndex: Int

    var body: some View {
        Group {}
    }
}
