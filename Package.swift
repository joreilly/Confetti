// swift-tools-version:5.3
import PackageDescription

let remoteKotlinUrl = "https://api.github.com/repos/joreilly/Confetti/releases/assets/81812486.zip"
let remoteKotlinChecksum = "5edc28cfe7ee4c1deb542819725bcf61f6dfbe87b42753c6bea6afbc545019c4"
let packageName = "ConfettiKit"

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