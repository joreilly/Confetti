import SwiftUI
import ConfettiKit


struct SpeakerDetailsView: View {
    var speaker: SpeakerDetails
    
    var body: some View {
        ScrollView {
            VStack(alignment: .center) {
                Text(speaker.name).font(.title)
                Text(speaker.tagline ?? "").font(.subheadline)
                
                AsyncImage(url: URL(string: speaker.photoUrl ?? "")) { image in
                     image.resizable()
                        .aspectRatio(contentMode: .fit)
                } placeholder: {
                    ProgressView()
                }
                .frame(width: 240, height: 240)
                .clipShape(RoundedRectangle(cornerRadius: 16))

                Spacer().frame(height: 16)
                Text(speaker.bio ?? "").font(.body)
                Spacer()
                
                HStack {
                    SessionSpeakerSocialInfo(speaker: speaker)
                }
                Spacer()
                Text("Sessions").font(.headline)
                ForEach(speaker.sessions, id: \.self) { session in
                    Text(session.title).font(.body)
                }
            }
            .padding()
        }
    }
}

