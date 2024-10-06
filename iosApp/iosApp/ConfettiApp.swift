import SwiftUI
import FirebaseAuth
import GoogleSignIn
import Combine
import ConfettiKit
import FirebaseCore

struct ConfettiApp: View {
    private let component: AppComponent
    
    @StateValue
    private var stack: ChildStack<AnyObject, AppComponentChild>
    
    @State private var userLoggedIn = (Auth.auth().currentUser != nil)
    
    init(_ component: AppComponent) {
        self.component = component
        _stack = StateValue(component.stack)
        
        UITabBar.appearance().backgroundColor = UIColor.systemBackground
    }
    
    var body: some View {
        VStack {
            switch stack.active.instance {
            case is AppComponentChild.Loading: ProgressView()
            case let child as AppComponentChild.Conferences: ConferencesView(child.component)
            case let child as AppComponentChild.Conference: ConferenceView(child.component, userLoggedIn)
            default: EmptyView()
            }
        }.onOpenURL{ url in
            //Handle Google Oauth URL
            GIDSignIn.sharedInstance.handle(url)
        }.onAppear{
            //Firebase state change listeneer
            Auth.auth().addStateDidChangeListener{ auth, user in
                if (user != nil) {
                    userLoggedIn = true
                    print("user logged in")
                } else {
                    userLoggedIn = false
                    print("user logged out")
                }
            }
        }
        
    }
}
