import SwiftUI
import ConfettiKit

struct SpeakerListView: View {
    var speakerList: [SpeakerDetails]
    
    var body: some View {
        NavigationView {
            List(speakerList) { speaker in
                NavigationLink(destination: SpeakerDetailsView(speaker: speaker)) {
                    SpeakerView(speaker: speaker)
                }
            }
            .navigationTitle("Speakers")
        }
    }
}


struct SpeakerView: View {
    var speaker: SpeakerDetails
    
    var body: some View {
        HStack {
            if let image = speaker.photoUrl,
               let url = URL(string: image) {
                AsyncImage(url: url) { image in
                    image.resizable()
                } placeholder: {
                    ProgressView()
                }
                .frame(width: 50, height: 50)
                .clipShape(RoundedRectangle(cornerRadius: 25))
            }
            VStack(alignment: .leading) {
                Text(speaker.name).font(.headline)
                Text(speaker.company ?? "").font(.subheadline)
            }
        }
    }
}

