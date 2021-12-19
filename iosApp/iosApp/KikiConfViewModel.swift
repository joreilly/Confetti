import Foundation
import SwiftUI
import shared
import KMPNativeCoroutinesAsync

@MainActor
class KikiConfViewModel: ObservableObject {
    let repository = KikiConfRepository()
    @Published public var sessions: [Session] = []
    @Published public var speakers: [Speaker] = []
    @Published public var rooms: [Room] = []
    
    private var sessionsTask: Task<(), Never>? = nil
    private var speakeresTask: Task<(), Never>? = nil
    private var roomsTask: Task<(), Never>? = nil

    
    func startObservingSessions() {
        sessionsTask = Task {
            do {
                let stream = asyncStream(for: repository.sessionsNative)
                for try await data in stream {
                    self.sessions = data
                }
            } catch {
                print("Failed with error: \(error)")
            }
        }
    }
    
    func stopObservingSessions() {
        sessionsTask?.cancel()
    }

    
    func startObservingSpeakers() {
        speakeresTask = Task {
            do {
                let stream = asyncStream(for: repository.speakersNative)
                for try await data in stream {
                    self.speakers = data
                }
            } catch {
                print("Failed with error: \(error)")
            }
        }
    }
    
    func stopObservingSpeakers() {
        speakeresTask?.cancel()
    }

    
    func startObservingRooms() {
        roomsTask = Task {
            do {
                let stream = asyncStream(for: repository.roomsNative)
                for try await data in stream {
                    self.rooms = data
                }
            } catch {
                print("Failed with error: \(error)")
            }
        }
    }
    
    func stopObservingRooms() {
        roomsTask?.cancel()
    }

}

