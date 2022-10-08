import SwiftUI
import ConfettiKit

struct SessionDetailsView: View {
    var session: SessionDetails

    var body: some View {
        
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text(session.title).font(.title).foregroundColor(.blue)
                Divider()
                
                Text(session.sessionDescription() ?? "")
                ScrollView(.horizontal, showsIndicators: false) {
                    LazyHStack(alignment: .center) {
                        ForEach(session.tags, id: \.self) { tag in
                            Text(tag)
                                .padding(.vertical, 10)
                                .padding(.horizontal)
                                .background(.blue)
                                .foregroundColor(.white)
                                .background(Capsule().stroke())
                                .clipShape(Capsule())
                        }
                    }
                    .padding(.vertical)
                }
                
                ForEach(session.speakers, id: \.self) { speaker in
                    Text(speaker.name).bold()
                    Text(speaker.bio ?? "")
                }
                Spacer()
            }
            .padding()
        }
    }
}
