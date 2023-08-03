import SwiftUI
import ConfettiKit

struct SessionSpeakerInfo: View {
    var speaker: SpeakerDetails
    
    var body: some View {
        VStack(alignment: .leading) {
            HStack(alignment: .top) {
                if let photo = speaker.photoUrl {
                    AsyncImage(url: URL(string: photo)) { image in
                         image.resizable()
                            .aspectRatio(contentMode: .fill)
                            .clipShape(Circle())
                    } placeholder: {
                        ProgressView()
                    }
                    .frame(width: 60, height: 60)
                }
                Spacer().frame(width: 10)

                VStack(alignment: .leading) {
                    Text(speaker.name)
                        .bold()
                        .font(.title3)
                    Text(speaker.tagline ?? "")
                        .bold()
                        .font(.subheadline)
                    Spacer(minLength: 10)
                    Text(speaker.bio ?? "").font(.system(size: 14))
                    Spacer(minLength: 10)
                    HStack(alignment: .top, spacing: 20) {
                        SessionSpeakerSocialInfo(speaker: speaker)
                    }
                }.textSelection(.enabled)
                Spacer()
            }
            .padding(.vertical, 8)
        }
    }
}


struct SessionSpeakerSocialInfo: View {
    var speaker: SpeakerDetails
    
    var body: some View {
        ForEach(speaker.socials, id: \.self) { socialItem in
            if let url = URL(string: socialItem.url) {
                Link(destination: url) {
                    let name = socialItem.name.lowercased()
                    switch name {
                    case "twitter":
                        Image("ic_network_twitter")
                    case "github":
                        Image("ic_network_github")
                    case "linkedin":
                        Image("ic_network_linkedin")
                    case "facebook":
                        Image("ic_network_facebook")
                    default:
                        Image("ic_network_web")
                    }
                }
            }
        }
    }
}
