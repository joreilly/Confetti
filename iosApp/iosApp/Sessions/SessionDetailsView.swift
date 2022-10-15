import SwiftUI
import ConfettiKit

struct SessionDetailsView: View {
    var session: SessionDetails

    var body: some View {
        
        ScrollView {
            VStack(alignment: .leading, spacing: 8) {
                Text(session.title).font(.title).foregroundColor(.blue)
                Spacer()
                
                Text(session.sessionDescription() ?? "").font(.body)
                                
                if session.tags.count > 0 {
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
                }
                
                Spacer()
                ForEach(session.speakers, id: \.self) { speaker in
                    Text(speaker.name).bold()
                    Text(speaker.bio ?? "").font(.body)
                    Spacer()
                }
                Spacer()
            }
            .padding()
        }
        .navigationBarTitleDisplayMode(.inline)
        .scrollContentBackground(.hidden)
        .background(Color(0xF0F0F0))
    }
}
