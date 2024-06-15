//
//  AppDelegate.swift
//  iosApp
//
//  Created by Arkadii Ivanov on 11/05/2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import ConfettiKit

class AppDelegate : NSObject, UIApplicationDelegate, ObservableObject {
    
    private var applicationLifecycle: ApplicationLifecycle
    @Published var appComponent: AppComponent
    
    override init() {
        KoinKt.doInitKoin()

        applicationLifecycle = ApplicationLifecycle()
        appComponent = DefaultAppComponent(
            componentContext: DefaultComponentContext(lifecycle: applicationLifecycle),
            onSignOut: {},
            onSignIn: {},
            isMultiPane: UIDevice.current.userInterfaceIdiom != UIUserInterfaceIdiom.phone,
            initialConferenceId: nil,
            settingsComponent: nil
        )
    }

    func onConferenceDeepLink(conferenceId: String) {
        applicationLifecycle.destroy()
        applicationLifecycle = ApplicationLifecycle()
        appComponent = DefaultAppComponent(
            componentContext: DefaultComponentContext(lifecycle: applicationLifecycle),
            onSignOut: {},
            onSignIn: {},
            isMultiPane: UIDevice.current.userInterfaceIdiom != UIUserInterfaceIdiom.phone,
            initialConferenceId: conferenceId,
            settingsComponent: nil
        )
    }
}
