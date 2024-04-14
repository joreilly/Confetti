//
//  AppDelegate.swift
//  iosApp
//
//  Created by Arkadii Ivanov on 11/05/2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import ConfettiKit

class AppDelegate : NSObject, UIApplicationDelegate {
    
    private var applicationLifecycle: ApplicationLifecycle
    @ObservedObject var appComponentHolder: AppComponentHolder
    
    override init() {
        KoinKt.doInitKoin()

        applicationLifecycle = ApplicationLifecycle()
        appComponentHolder = AppComponentHolder(
            appComponent: DefaultAppComponent(
                componentContext: DefaultComponentContext(lifecycle: applicationLifecycle),
                onSignOut: {},
                onSignIn: {},
                isMultiPane: UIDevice.current.userInterfaceIdiom != UIUserInterfaceIdiom.phone,
                initialConferenceId: nil
            )
        )
    }

    func onConferenceDeepLink(conferenceId: String) {
        applicationLifecycle.destroy()
        applicationLifecycle = ApplicationLifecycle()
        appComponentHolder.appComponent = DefaultAppComponent(
            componentContext: DefaultComponentContext(lifecycle: applicationLifecycle),
            onSignOut: {},
            onSignIn: {},
            isMultiPane: UIDevice.current.userInterfaceIdiom != UIUserInterfaceIdiom.phone,
            initialConferenceId: conferenceId
        )
    }
}

class AppComponentHolder: ObservableObject {
    @Published var appComponent: AppComponent
    
    init(appComponent: AppComponent) {
        self.appComponent = appComponent
    }
}
