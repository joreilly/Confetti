//
//  ConfettiMacApp.swift
//  ConfettiMac
//
//  Created by John O'Reilly on 16/06/2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import ConfettiKit

@main
struct ConfettiMacApp: App {
    init() {
        KoinKt.doInitKoin()
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
