import Foundation
import FoundationModels
import ConfettiKit


class PromptApiIos: PromptApi {
    func generateContent(prompt: String, query: String) async throws -> PromptResponse {
        
        var responseText = ""
        
        if #available(iOS 26.0, *) {
            do {
                let session = LanguageModelSession()
                let response = try await session.respond(to: prompt)
                print(response)
                responseText = response.content
            } catch let error {
                responseText = error.localizedDescription
            }
        }

        return PromptResponse(text: responseText)
    }
    
    
}
