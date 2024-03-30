# Confetti 🎊

![kotlin-version](https://img.shields.io/badge/kotlin-1.9.20-blue)

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
Requires Xcode 14 or later for iOS client.

### Screenshots

<img width="1203" alt="Screenshot 2023-04-02 at 14 28 02" src="https://user-images.githubusercontent.com/6302/229355816-b1f6b769-4c7f-49a1-9fba-4c9cb1f6955d.png">


![Tablet Screenshot of Confetti](https://user-images.githubusercontent.com/6302/227476725-edd577f5-2abd-4660-a777-decef84fbb9b.png)


<img width="500" alt="Wear Screenshots of Confetti screens" src="https://raw.githubusercontent.com/joreilly/Confetti/aaa91c53098754de5c568ec6611b7ab237d23bcb/wearApp/images/wearScreenshots.png">

<img width="1356" alt="Screenshot 2024-03-30 at 19 46 48" src="https://github.com/joreilly/Confetti/assets/6302/0777a9d9-e620-4feb-9e28-a00f01978c1c">

<img src="androidApp/snapshot/walkthroughAndroidAuto.gif" alt="Walkthrough of Confetti running on Android Auto" />

<img src="automotiveApp/snapshot/walkthroughAndroidAutomotive.gif" alt="Walkthrough of Confetti running on Android Automotive" />


### Gemini functionality

Gemini based talk recommendations for particuar conference are included in the mobile clients (if `gemini_api_key` configured in `local.properties`).  Desktop client allows free entry of query.

<img width="1033" alt="Screenshot 2024-02-25 at 09 58 16" src="https://github.com/joreilly/Confetti/assets/6302/ac624a44-4b0b-4a1d-b58b-4ce172b6f3cd">


### API

The API is available at https://confetti-app.dev/graphiql
You can query the list of conferences with `conferences.id` and chose what conference to query by
passing a `"conference"` HTTP header 


