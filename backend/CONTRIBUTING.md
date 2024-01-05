
# Components

The Confetti backend is composed of different components:

* A Spring Boot GraphQL server in [service-graphql](service-graphql). This server serves the main API.
* A KTor Import server in [service-import](service-import). This server updates the conference data when needed.
* Conference data is stored in Google Datastore.
* Bookmarks data is stored in Google Datastore.
* The static landing page is served from Google Cloud Storage.

You can run the backend locally with the following task:

# Developing locally

For the GraphQL server:

```
./gradle :backend:service-graphql:bootRun
```
Note that you will only be able to use the "test" conference as access to the real data requires additional GCP credentials.

For the Import server:

```
./gradle :backend:service-import:localRun
```

# Deploying to Google Cloud Platform

The GraphQL server, Import server as well as the static landing page are updated automatically when a PR is merged to main.

The rest of the configuration is still a work in progress. Most of it is now in [terraform/main.tf](terraform/main.tf) but this requires a state that is currently only on my machine. Ultimately the goal is to deploy from CI as well.

To update terraform resources:

```
terraform apply
```

This:
* Provisions a Google managed HTTPS certificate
* Allocates a static IP
* Configure a load balancer to listen to that static IP and redirect to `GraphQL`, `Import`, `Landing`, etc... 
* Configure Google Cloud CDN
* Creates Artifact registry & Cloud Run resources
* It'll output the IP address of the load balancer to configure in Google Domains

