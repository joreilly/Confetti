import Foundation
import SwiftUI
import shared

class KikiConfViewModel: ObservableObject {
    let repository = KikiConfRepository()
    @Published public var sessionList: [Session] = []
    @Published public var speakerList: [Speaker] = []
    @Published public var roomList: [Room] = []
    
    func fetchSessions() {
        repository.getSessions { data, error  in
            if let sessionList = data {
                self.sessionList = sessionList
            }
        }
    }
    
    func fetchSpeakers() {
        repository.getSpeakers { data, error  in
            if let speakerList = data {
                self.speakerList = speakerList
            }
        }
    }
    
    func fetchRooms() {
        repository.getRooms { data, error  in
            if let roomList = data {
                self.roomList = roomList
            }
        }
    }

}

