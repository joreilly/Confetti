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
    
    let lifecycle: LifecycleRegistry
    let root: AppComponent
    
    override init() {
        KoinKt.doInitKoin()

        lifecycle = LifecycleRegistryKt.LifecycleRegistry()
        
        root = DefaultAppComponent(
            componentContext: DefaultComponentContext(lifecycle: lifecycle)
        )
        
        LifecycleRegistryExtKt.create(lifecycle)
    }
    
    func applicationDidBecomeActive(_ application: UIApplication) {
        LifecycleRegistryExtKt.resume(lifecycle)
    }
    
    func applicationWillResignActive(_ application: UIApplication) {
        LifecycleRegistryExtKt.pause(lifecycle)
    }
    
    func applicationDidEnterBackground(_ application: UIApplication) {
        LifecycleRegistryExtKt.stop(lifecycle)
    }
    
    func applicationWillTerminate(_ application: UIApplication) {
        LifecycleRegistryExtKt.destroy(lifecycle)
    }
}
