# Confetti 🎊

![kotlin-version](https://img.shields.io/badge/kotlin-1.9.0-blue)

[<img src="https://upload.wikimedia.org/wikipedia/commons/7/78/Google_Play_Store_badge_EN.svg"
alt="Get it on Google Play Store"
height="80">](https://play.google.com/store/apps/details?id=dev.johnoreilly.confetti)
[<img src="https://developer.apple.com/assets/elements/badges/download-on-the-app-store.svg"
alt="Get it on App Store"
height="80">](https://apps.apple.com/us/app/confetti/id1660211390)

Kotlin Multiplatform GraphQL project (backend and clients) to allow viewing of conference information for a range of conferences.

Includes:

- SwiftUI iOS client
- Jetpack Compose Android client
- Compose for Desktop client (early version)
- Compose for Wear OS client (contributed by https://github.com/yschimke)
- Compose for iOS (session details screen shared across platforms)
- Android Auto and Automotive OS (contributed by https://github.com/cmota)
- [Apollo GraphQL](https://github.com/apollographql/apollo-kotlin) based Kotlin Multiplatform shared
  code
- GraphQL backend

### Contributors

<a href="https://github.com/joreilly/confetti/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=joreilly/confetti" />
</a>

### Related posts

* [Swift/Kotlin ViewModel alignment in a Kotlin Multiplatform project](https://johnoreilly.dev/posts/swift-kotlin-viewmodel-kmm-comparison/)
* [Using KMM-ViewModel library to share VM between iOS and Android](https://johnoreilly.dev/posts/kmm-viewmodel/)
* [Consuming Compose for iOS in a SwiftUI application](https://johnoreilly.dev/posts/swiftui-compose-ios/)

### Building

Use Android Studio/IntelliJ to build/run Android client.
Requires XCode 14 or later for iOS client.

### Screenshots

<img width="1203" alt="Screenshot 2023-04-02 at 14 28 02" src="https://user-images.githubusercontent.com/6302/229355816-b1f6b769-4c7f-49a1-9fba-4c9cb1f6955d.png">


![Tablet Screenshot of Confetti](https://user-images.githubusercontent.com/6302/227476725-edd577f5-2abd-4660-a777-decef84fbb9b.png)


<img width="500" alt="Wear Screenshots of Confetti screens" src="https://raw.githubusercontent.com/joreilly/Confetti/aaa91c53098754de5c568ec6611b7ab237d23bcb/wearApp/images/wearScreenshots.png">


<img width="1205" alt="Desktop Screenshot of Confetti screens" src="https://user-images.githubusercontent.com/6302/227615364-dd349253-483b-45a6-9090-cc8b932bef1f.png">

<img src="androidApp/snapshot/walkthroughAndroidAuto.gif" alt="Walkthrough of Confetti running on Android Auto" />

<img src="automotiveApp/snapshot/walkthroughAndroidAutomotive.gif" alt="Walkthrough of Confetti running on Android Automotive" />

### API

The API is available at https://confetti-app.dev/graphiql
You can query the list of conferences with `conferences.id` and chose what conference to query by
passing a `"conference"` HTTP header 


