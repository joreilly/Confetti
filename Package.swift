// swift-tools-version:5.3
import PackageDescription

let remoteKotlinUrl = "https://api.github.com/repos/joreilly/Confetti/releases/assets/81446643.zip"
let remoteKotlinChecksum = "86b6465121bbdbcf2f100fc1f8e2a01acc2ba15e21c19b58ffc717a89ccc6e3c"
let packageName = "shared"

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