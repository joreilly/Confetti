// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK (do not edit)
let remoteKotlinUrl = "https://repo.repsy.io/mvn/joreilly/confetti/Confetti/shared-kmmbridge/0.8.10/shared-kmmbridge-0.8.10.zip"
let remoteKotlinChecksum = "4674c38f87f15cad4a6118211417b48db2e8bbd12294f30739583a02ebc3fc25"
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