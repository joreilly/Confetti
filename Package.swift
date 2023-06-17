// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK (do not edit)
let remoteKotlinUrl = "https://repo.repsy.io/mvn/joreilly/confetti/Confetti/shared-kmmbridge/0.8.13/shared-kmmbridge-0.8.13.zip"
let remoteKotlinChecksum = "381b6f2e7a63ac1ea84de35f6ad55f21b6fe7ccfd6f54e1ca56497aa6abcb3e3"
let packageName = "ConfettiKit"
// END KMMBRIDGE BLOCK

let package = Package(
    name: packageName,
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(
            name: packageName,
            targets: [packageName]
        ),
    ],
    targets: [
        .binaryTarget(
            name: packageName,
            url: remoteKotlinUrl,
            checksum: remoteKotlinChecksum
        )
        ,
    ]
)