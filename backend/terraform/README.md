Terraform configuration for the backend. This is a work in progress and currently requires to be run from the same machine. Ultimately the goal is to deploy from CI

Notes:
* App Engine Versions, Cloud Storage buckets and Datastore are managed outside terraform for now
* DNS is managed outside terraform (in Google Domains)
* Call `terraform apply` from this directory to create/sync the resources. It'll output the IP address of the load balancer to configure in Google Domains

