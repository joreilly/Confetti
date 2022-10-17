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
    case success(conferenceName: String, confDates: [Kotlinx_datetimeLocalDate], selectedDateIndex: Int, sessions: [SessionDetails])
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
            let conferenceNameAsyncSequence = asyncStream(for: repository.conferenceNameNative)
            let sessionsMapAsyncSequence = asyncStream(for: repository.sessionsMapNative)
            
            for try await (conferenceName, sessionsMap, selectedDateIndex)
                    in combineLatest(conferenceNameAsyncSequence, sessionsMapAsyncSequence, $selectedDateIndex.values) {
                let confDates = sessionsMap.map { $0.key }.sorted { e1, e2 in
                    e2.compareTo(other: e1) > 0
                }
                let selectedDate = confDates[selectedDateIndex]
                let sessions = sessionsMap[selectedDate] ?? []
                self.uiState = SessionsUiState.success(conferenceName: conferenceName, confDates: confDates, selectedDateIndex: selectedDateIndex, sessions: sessions)
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
            text = session.speakers.map { $0.speakerDetails.name }.joined(separator: ",")
        }
        text += " (\(session.room?.name ?? ""))"  // \(getFlag(session: session))"
        return text
    }

    
    func getSessionTime(session: SessionDetails) -> String {
        return repository.getSessionTime(session: session)
    }
    
    func refresh() async {
        do {
            try await asyncFunction(for: repository.refreshNative(networkOnly: true))
        } catch {
            print("Failed with error: \(error)")
        }
    }
}

