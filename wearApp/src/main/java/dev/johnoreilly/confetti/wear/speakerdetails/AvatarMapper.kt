package dev.johnoreilly.confetti.wear.speakerdetails

import coil.map.Mapper
import coil.request.Options

class AvatarMapper: Mapper<SpeakerAvatar, String> {
    override fun map(data: SpeakerAvatar, options: Options): String? {
        // TODO change to check A/B config from firebase
        return if (false) {
            return data.speaker.photoUrl
        } else {
//            "http://10.0.2.2:8080/images/avatar/${data.conference}/${data.speaker.id}/Watch"
            "https://confetti-app.dev/images/avatar/${data.conference}/${data.speaker.id}/Watch"
        }
    }
}