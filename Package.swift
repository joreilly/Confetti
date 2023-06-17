// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK (do not edit)
let remoteKotlinUrl = "https://repo.repsy.io/mvn/joreilly/confetti/Confetti/shared-kmmbridge/0.8.12/shared-kmmbridge-0.8.12.zip"
let remoteKotlinChecksum = "70bde00884fc682fceb7a8c63c2f4ea5cb90a8f1d348143ea5f300cecb2b496b"
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