import SwiftUI
import ConfettiKit


struct SpeakerDetailsView: View {
    var speaker: SpeakerDetails
    
    var body: some View {
        ScrollView {
            VStack(alignment: .center) {
                Text(speaker.name).font(.title)
                
                AsyncImage(url: URL(string: speaker.photoUrl ?? "")) { image in
                     image.resizable()
                        .aspectRatio(contentMode: .fit)
                } placeholder: {
                    ProgressView()
                }
                .frame(width: 240, height: 240)

                Text(speaker.bio ?? "").font(.body)
                Spacer()
                
                VStack {
                    ForEach(speaker.socials, id: \.self) { social in
                        HStack {
                            Text("\(social.name): ")
                            Button(action: {
                                guard let url = URL(string: social.link) else { return }
                                UIApplication.shared.open(url)
                               }) {
                                Text(social.link)
                            }
                        }
                    }
                }
            }
            .padding()
        }
    }
}

