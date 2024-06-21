import Testing

import ConfettiKit

final class ConfettiTests {
    
    private let lifecycle = LifecycleRegistryKt.LifecycleRegistry(initialState: .resumed)
    
    private lazy var context: ComponentContext = {
        DefaultComponentContext(lifecycle: lifecycle)
    }()
    
    @Test func testGetConferences() async throws {
        let conferenceComponent = DefaultConferencesComponent(
            componentContext: context,
            onConferenceSelected: { _ in }
        )
        
        let uiState = await awaitForState(conferenceComponent.uiState) { $0 as? ConferencesComponentSuccess }
        
        #expect(!uiState.conferenceListByYear.isEmpty)
    }
    
    @Test func testGetSessions() async throws {
        let sessionsComponent = DefaultSessionsComponent(
            componentContext: context,
            conference: "test",
            user: nil,
            onSessionSelected: { _ in },
            onSignIn: {}
        )
        
        let uiState = await awaitForState(sessionsComponent.uiState) { $0 as? SessionsUiStateSuccess }

        #expect(!uiState.sessionsByStartTimeList.isEmpty)
    }

}
