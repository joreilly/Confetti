import XCTest

import ConfettiKit

final class ConfettiTests: XCTestCase {
    
    private let lifecycle = LifecycleRegistryKt.LifecycleRegistry(initialState: .resumed)
    
    private lazy var context: ComponentContext = {
        DefaultComponentContext(lifecycle: lifecycle)
    }()
    
    override func setUp() {
        executionTimeAllowance = 60
        continueAfterFailure = true
    }
    
    override func setUpWithError() throws {
    }

    override func tearDownWithError() throws {
    }

    func testGetConferences() async throws {
        let viewModel = DefaultConferencesComponent(
            componentContext: context,
            onConferenceSelected: { _ in }
        )
        
        let uiState = await awaitForState(viewModel.uiState) { $0 as? ConferencesComponentSuccess }
        
        XCTAssert(!uiState.conferences.isEmpty)
    }
    
    func testGetSessions() async throws {
        let viewModel = DefaultSessionsComponent(
            componentContext: context,
            conference: "test",
            user: nil,
            onSessionSelected: { _ in },
            onSignIn: {}
        )
        
        let uiState = await awaitForState(viewModel.uiState) { $0 as? SessionsUiStateSuccess }

        XCTAssert(!uiState.sessionsByStartTimeList.isEmpty)
    }

}
