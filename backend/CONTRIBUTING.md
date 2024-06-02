
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

The backend is deployed in Google Cloud on each push to main.

Confetti uses [Terraform](https://www.terraform.io/) to manage Google Cloud infrastructure. The configuration is in [terraform/main.tf](terraform/main.tf) and the terraform state stored in Google Cloud Storage.

Terraform manages:
* Google managed HTTPS certificate
* The static IP
* The load balancer to listen to that static IP and redirect to `GraphQL`, `Import`, `Landing`, etc... 
* Google Cloud CDN
* Artifact registry & Cloud Run resources

The DNS is managed outside of Terraform. Once `terraform apply` is run, it'll output the IP address of the load balancer to configure in Google Domains.

