tasks.register("setupGoogleServices") {
  doLast {
    file("service_account_key.json").writeText(System.getenv("GOOGLE_SERVICES_JSON"))
  }
}