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
    
    var root: AppComponent
    
    override init() {
        KoinKt.doInitKoin()

        root = DefaultAppComponent(
            componentContext: DefaultComponentContext(lifecycle: ApplicationLifecycle()),
            onSignOut: {},
            onSignIn: {},
            isMultiPane: UIDevice.current.userInterfaceIdiom != UIUserInterfaceIdiom.phone,
            initialConferenceId: nil
        )
    }

    func onConferenceDeepLink(conferenceId: String) {
        root = DefaultAppComponent(
            componentContext: DefaultComponentContext(lifecycle: ApplicationLifecycle()),
            onSignOut: {},
            onSignIn: {},
            isMultiPane: UIDevice.current.userInterfaceIdiom != UIUserInterfaceIdiom.phone,
            initialConferenceId: conferenceId
        )
    }
}
