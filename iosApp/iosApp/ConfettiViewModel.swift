import Foundation
import SwiftUI
import ConfettiKit
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
    
    @Published var sessionMap = [Kotlinx_datetimeLocalDate: [SessionDetails]]()
    @Published public var sessionDates: [Kotlinx_datetimeLocalDate] = []
    
    
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
        
        
        Task {
            do {
                let stream = asyncStream(for: repository.sessionDatesNative)
                for try await data in stream {
                    self.sessionDates = data
                }
            } catch {
                print("Failed with error: \(error)")
            }
        }

    }

    func setSelectedDateIndex(index: Int) {
        if (index < sessionDates.count) {
            let selectedDate = sessionDates[index]
            sessions = sessionMap[selectedDate] ?? []
        }
    }
    
    func toggleLanguageChecked(language: String) {
        let checked = enabledLanguages.contains(language) ? false : true
        repository.updateEnableLanguageSetting(language: language, checked: checked)
    }

    
    func observeSessions() async {
        do {
            let stream = asyncStream(for: repository.sessionsMapNative)
            for try await data in stream {
                self.sessionMap = data
                setSelectedDateIndex(index: 0)
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
        // TODO need to figure out how we want to generally handle languages
        return  session.language == "fr-FR" ?  "🇫🇷" : "🇬🇧"
    }
    
    func getSessionSpeakerLocation(session: SessionDetails) -> String {
        var text = ""
        if (session.speakers.count > 0) {
            text = session.speakers.map { $0.name }.joined(separator: ",")
        }
        text += "\(session.room?.name ?? "") / \(getFlag(session: session))"
        return text
    }

    
    func getSessionTime(session: SessionDetails) -> String {
        return repository.getSessionTime(session: session)
    }
}

