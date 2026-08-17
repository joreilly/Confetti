tasks.register("uploadLandingPage") {
    val baseDir = file("public")
    val serviceAccountJson = provider { gcpServiceAccountJson }
    doLast {
        uploadLandingPage(baseDir, serviceAccountJson.get())
    }
}