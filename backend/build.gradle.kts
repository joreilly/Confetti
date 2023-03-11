tasks.register("setupCredentials") {
    doLast {
        file("gcp_service_account_key.json").writeText(System.getenv("GOOGLE_SERVICES_JSON"))
        file("firebase_service_account_key.json").writeText(System.getenv("GOOGLE_SERVICES_JSON"))
    }
}