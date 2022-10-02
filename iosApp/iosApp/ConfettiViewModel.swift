import Foundation
import SwiftUI
import ConfettiKit
import AsyncAlgorithms
import KMPNativeCoroutinesAsync


extension SessionDetails: Identifiable { }
extension SpeakerDetails: Identifiable { }
extension RoomDetails: Identifiable { }

enum SessionsUiState {
    case loading
    case success([Kotlinx_datetimeLocalDate], Int, [SessionDetails])
}

@MainActor
class ConfettiViewModel: ObservableObject {
    let repository = ConfettiRepository()
    @Published public var speakers: [SpeakerDetails] = []
    @Published public var rooms: [RoomDetails] = []
    
    @Published public var enabledLanguages: Set<String> = []
    
    @Published public var selectedDateIndex: Int = 0
    
    @Published public var uiState: SessionsUiState = .loading
    
    
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
            let confDatesAsyncSequence = asyncStream(for: repository.confDatesNative)
            let sessionsMapAsyncSequence = asyncStream(for: repository.sessionsMapNative)
            
            for try await (confDates, sessionsMap, selectedDateIndex)
                    in combineLatest(confDatesAsyncSequence, sessionsMapAsyncSequence, $selectedDateIndex.values) {
                
                let selectedDate = confDates[selectedDateIndex]
                let sessions = sessionsMap[selectedDate] ?? []
                self.uiState = SessionsUiState.success(confDates, selectedDateIndex, sessions)
            }
        }
    }

    func toggleLanguageChecked(language: String) {
        let checked = enabledLanguages.contains(language) ? false : true
        repository.updateEnableLanguageSetting(language: language, checked: checked)
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

