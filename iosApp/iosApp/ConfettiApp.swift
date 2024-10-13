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
            case let child as AppComponentChild.Conference: ConferenceView(child.component)
            default: EmptyView()
            }
        }.onOpenURL{ url in
            //Handle Google Oauth URL
            GIDSignIn.sharedInstance.handle(url)
        }.onAppear{
            //Firebase state change listeneer
            Auth.auth().addStateDidChangeListener{ auth, user in
                if let user {
                    print("user logged in")
                    let confettiUser = NativeTokenProvider(user: user)
                    component.setUser(user: confettiUser)
                } else {
                    print("user logged out")
                    component.setUser(user: nil)
                }
            }
        }
    }
}


class NativeTokenProvider: ConfettiKit.User {
    private let user: FirebaseAuth.User

    var email: String?
    var name: String
    var photoUrl: String?
    var uid: String
    
    init(user: FirebaseAuth.User) {
        self.user = user
        self.email = user.email
        self.name = user.displayName ?? ""
        self.photoUrl = user.photoURL?.description
        self.uid = user.uid
    }
    
    func token(forceRefresh: Bool) async throws -> String? {
        return try await user.getIDToken()
    }
}
