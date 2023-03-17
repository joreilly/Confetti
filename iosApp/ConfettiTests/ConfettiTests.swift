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
        
        let confUIStateSequence = asyncSequence(for: viewModel.uiStateFlow)
        let uiState = try await confUIStateSequence.first(where: { $0 is ConferencesViewModel.Success })
        let conferences = (uiState as! ConferencesViewModel.Success).conferences
        XCTAssert(!conferences.isEmpty)
    }

}
