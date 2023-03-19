import XCTest

import ConfettiKit
import KMPNativeCoroutinesCore
import KMPNativeCoroutinesAsync

final class ConfettiTests: XCTestCase {

    override func setUpWithError() throws {
    }

    override func tearDownWithError() throws {
    }

    func testGetConferences() async throws {
        let viewModel = ConferencesViewModel()
        
        let conferencesUIStateSequence = asyncSequence(for: viewModel.uiStateFlow)
        let uiState = try await conferencesUIStateSequence.first(where: { $0 is ConferencesViewModel.Success })
        let conferences = (uiState as! ConferencesViewModel.Success).conferences
        XCTAssert(!conferences.isEmpty)
    }

    
    func testGetSessions() async throws {
        let viewModel = SessionsViewModel()
        viewModel.configure(conference: "test")
        
        let sessionsUIStateSequence = asyncSequence(for: viewModel.uiStateFlow)
        let uiState = try await sessionsUIStateSequence.first(where: { $0 is SessionsUiStateSuccess })
        let sessions = (uiState as! SessionsUiStateSuccess).sessionsByStartTimeList
        XCTAssert(!sessions.isEmpty)
    }

}
