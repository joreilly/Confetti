import Foundation
import SwiftUI
import shared
import KMPNativeCoroutinesAsync


extension SessionDetails: Identifiable { }
extension SpeakerDetails: Identifiable { }
extension RoomDetails: Identifiable { }

@MainActor
class ConfettiViewModel: ObservableObject {
    let repository = ConfettiRepository()
    @Published public var sessions: [SessionDetails] = []
    @Published public var speakers: [SpeakerDetails] = []
    @Published public var rooms: [RoomDetails] = []
    
    @Published public var enabledLanguages: Set<String> = []
    
    
    init() {
        Task {
            do {
                let stream = asyncStream(for: repository.enabledLanguagesNative)
                for try await data in stream {
                    self.enabledLanguages = data
                }
            } catch {
                print("Failed with error: \(error)")
            }
        }
    }

        
    
    func toggleLanguageChecked(language: String) {
        let checked = enabledLanguages.contains(language) ? false : true
        repository.updateEnableLanguageSetting(language: language, checked: checked)
    }

    
    func observeSessions() async {
        do {
            let stream = asyncStream(for: repository.sessionsNative)
            for try await data in stream {
                self.sessions = data
            }
        } catch {
            print("Failed with error: \(error)")
        }
    }

    func observeSpeakers() async {
        do {
            let stream = asyncStream(for: repository.speakersNative)
            for try await data in stream {
                self.speakers = data
            }
        } catch {
            print("Failed with error: \(error)")
        }
    }
    

    
    func observeRooms() async {
        do {
            let stream = asyncStream(for: repository.roomsNative)
            for try await data in stream {
                self.rooms = data
            }
        } catch {
            print("Failed with error: \(error)")
        }
    }
    

    
    func getFlag(session: SessionDetails) -> String {
        return  session.language == "French" ?  "🇫🇷" : "🇬🇧"
    }
    
    func getSessionSpeakerLocation(session: SessionDetails) -> String {
        var text = session.speakers.map { $0.name }.joined(separator: ",")
        text += " / \(session.room.name) / \(getFlag(session: session))"
        return text
    }
    
    
    // TODO move this in to common code
    func getSessionTime(session: SessionDetails) -> String {
        // easier way to do this?
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss+SSSS"
        let date = dateFormatter.date(from: session.startDate)
        
        if let startDate = date {
            let calendar = Calendar.current
            let hour = calendar.component(.hour, from: startDate)
            let minute = calendar.component(.minute, from: startDate)
            return "\(hour):\(minute)"
        }
        
        return ""
    }

}

