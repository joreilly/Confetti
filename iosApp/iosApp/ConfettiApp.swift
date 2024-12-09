import SwiftUI
import FirebaseAuth
import GoogleSignIn
import Combine
import ConfettiKit
import FirebaseCore


struct ComposeView: UIViewControllerRepresentable {
    let component: DefaultAppComponent
    let backDispatcher: BackDispatcher
    
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(component: component, backDispatcher: backDispatcher)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}


struct ConfettiApp: View {
    let component: DefaultAppComponent
    let backDispatcher: BackDispatcher
    
    var body: some View {
        ComposeView(component: component, backDispatcher: backDispatcher)
            .ignoresSafeArea()
            .onOpenURL{ url in
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
