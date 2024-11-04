`main.tf` contains the Google Cloud infrastructure as code.

Call `./gradlew :backend:terraform:apply` to deploy changes.

For bootstrapping, it requires:
* A manually created Google cloud project with a manually created `confetti-tfstate` bucket.
* A GOOGLE_SERVICES_JSON service account with those roles:
  * `roles/secretmanager.secretAccessor`
  * more (TBD)
