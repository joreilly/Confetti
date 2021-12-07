import SwiftUI
import shared

struct ContentView: View {
    @State var repository = KikiConfRepository()
    @State var sessionList: [Session] = []

    var body: some View {
        NavigationView {
            List(sessionList, id: \.id) { session in
                VStack(alignment: .leading) {
                    Text(session.title).font(.headline)
                }
            }
            .navigationTitle("Sessions")
            .onAppear {
                repository.getSessions { data, error  in
                    if let sessionList = data {
                        self.sessionList = sessionList
                    }
                }
            }
        }
    }
}


