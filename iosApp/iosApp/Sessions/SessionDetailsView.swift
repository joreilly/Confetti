import SwiftUI
import SwiftUIFlowLayout
import ConfettiKit

struct SessionDetailsView: View {
    var session: SessionDetails

    var body: some View {
        
        ScrollView {
            VStack(alignment: .leading, spacing: 8) {
                Text(session.title).font(.title).foregroundColor(.blue)
                Spacer()
                
                Text(session.sessionDescription ?? "").font(.body)
                                
                if session.tags.count > 0 {
                    FlowLayout(mode: .scrollable,
                               items: session.tags,
                               itemSpacing: 4) {
                        Text($0)
                            .padding(.vertical, 10)
                            .padding(.horizontal)
                            .background(.blue)
                            .foregroundColor(.white)
                            .background(Capsule().stroke())
                            .clipShape(Capsule())
                    }
                }
                
                Spacer()
                ForEach(session.speakers, id: \.self) { speaker in
                    SessionSpeakerInfo(speaker: speaker.speakerDetails)
                }
                Spacer()
            }
            .padding()
        }
        .navigationBarTitleDisplayMode(.inline)
        .background(Color(0xF0F0F0))
    }
}
