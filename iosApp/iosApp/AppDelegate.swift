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
    let rootHolder: RootHolder = RootHolder()
}


class RootHolder : ObservableObject {
    let lifecycle: LifecycleRegistry
    let root: AppComponent

    init() {
        KoinKt.doInitKoin()
        
        lifecycle = LifecycleRegistryKt.LifecycleRegistry()

        root = DefaultAppComponent(
            componentContext: DefaultComponentContext(lifecycle: lifecycle),
            onSignOut: {}
        )

        LifecycleRegistryExtKt.resume(lifecycle)
    }

    deinit {
        // Destroy the root component before it is deallocated
        LifecycleRegistryExtKt.destroy(lifecycle)
    }
}
