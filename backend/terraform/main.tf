terraform {
  backend "gcs" {
    bucket = "confetti-tfstate"
    prefix = "terraform/state"
  }
}

variable "region" {
  default = "us-west1"
}

provider google-beta {
  project = "confetti-349319"
  region  = var.region
}

resource "google_compute_url_map" "default" {
  name            = "default"
  provider        = google-beta
  default_service = google_compute_backend_bucket.landing_page.id

  host_rule {
    hosts        = ["confetti-app.dev"]
    path_matcher = "default"
  }

  host_rule {
    hosts        = ["router.confetti-app.dev"]
    path_matcher = "router"
  }

  host_rule {
    hosts        = ["wasm.confetti-app.dev"]
    path_matcher = "wasm"
  }

  path_matcher {
    name            = "default"
    default_service = google_compute_backend_bucket.landing_page.id

    path_rule {
      paths = [
        "/graphiql",
        "/graphql",
        "/images/*"
      ]
      service = google_compute_backend_service.graphql.id
    }
    path_rule {
      paths = [
        "/update/*"
      ]
      service = google_compute_backend_service.import.id
    }
  }
  path_matcher {
    name            = "router"
    default_service = google_compute_backend_service.router.id

    path_rule {
      paths = [
        "/graphiql",
        "/graphql"
      ]
      service = google_compute_backend_service.router.id
    }
  }
  path_matcher {
    name            = "wasm"
    default_service = google_compute_backend_bucket.wasm.id
  }
}

resource "google_compute_backend_bucket" "landing_page" {
  provider    = google-beta
  name        = "landing-page"
  bucket_name = "confetti-landing-page"
  enable_cdn  = true
}

resource "google_compute_backend_bucket" "wasm" {
  provider    = google-beta
  name        = "wasm"
  bucket_name = "confetti-wasm"
  enable_cdn  = true
}

resource "google_compute_global_network_endpoint" "router" {
  provider                      = google-beta
  global_network_endpoint_group = google_compute_global_network_endpoint_group.router.name
  fqdn                          = "main--confetti-supergraph-uv5cdu.apollographos.net"
  port                          = 443
}

resource "google_compute_global_network_endpoint_group" "router" {
  name                  = "default"
  provider              = google-beta
  default_port          = "443"
  network_endpoint_type = "INTERNET_FQDN_PORT"
}

resource "google_compute_backend_service" "router" {
  provider                        = google-beta
  name                            = "router"
  enable_cdn                      = true
  timeout_sec                     = 10
  connection_draining_timeout_sec = 10

  custom_request_headers  = ["Host: ${google_compute_global_network_endpoint.router.fqdn}"]
  custom_response_headers = ["X-Cache-Hit: {cdn_cache_status}"]

  log_config {
    enable      = true
    sample_rate = 1
  }
  backend {
    group = google_compute_global_network_endpoint_group.router.id
  }
  cdn_policy {
    cache_mode = "USE_ORIGIN_HEADERS"

    cache_key_policy {
      include_protocol     = true
      include_host         = true
      include_query_string = true
      include_http_headers = ["conference"]
    }
  }
  compression_mode = "DISABLED"
  protocol         = "HTTPS"
}

resource "google_compute_backend_service" "graphql" {
  provider   = google-beta
  name       = "graphql"
  enable_cdn = true

  custom_response_headers = ["X-Cache-Hit: {cdn_cache_status}"]

  log_config {
    enable      = true
    sample_rate = 1
  }

  backend {
    group = google_compute_region_network_endpoint_group.cloudrungraphql.id
  }

  cdn_policy {
    cache_mode = "USE_ORIGIN_HEADERS"

    cache_key_policy {
      include_protocol     = true
      include_host         = true
      include_query_string = true
      include_http_headers = ["conference"]
    }
  }
  compression_mode = "DISABLED"
}

resource "google_compute_backend_service" "import" {
  provider   = google-beta
  name       = "import"
  enable_cdn = true

  custom_response_headers = ["X-Cache-Hit: {cdn_cache_status}"]

  log_config {
    enable      = true
    sample_rate = 1
  }

  backend {
    group = google_compute_region_network_endpoint_group.cloudrunimport.id
  }

  cdn_policy {
    cache_mode = "USE_ORIGIN_HEADERS"

    cache_key_policy {
      include_protocol     = true
      include_host         = true
      include_query_string = true
      include_http_headers = ["conference"]
    }
  }
  compression_mode = "DISABLED"
}

resource "google_compute_region_network_endpoint_group" "cloudrungraphql" {
  provider              = google-beta
  name                  = "cloudrungraphql"
  region                = var.region
  network_endpoint_type = "SERVERLESS"

  cloud_run {
    service = "graphql"
  }
}

resource "google_compute_region_network_endpoint_group" "cloudrunimport" {
  provider              = google-beta
  name                  = "cloudrunimport"
  region                = var.region
  network_endpoint_type = "SERVERLESS"

  cloud_run {
    service = "import"
  }
}

resource "google_compute_managed_ssl_certificate" "default2" {
  name     = "default2"
  provider = google-beta

  managed {
    domains = ["confetti-app.dev", "router.confetti-app.dev", "wasm.confetti-app.dev"]
  }
}

resource "google_compute_global_address" "default" {
  provider = google-beta
  name     = "default"
}

resource "google_compute_target_https_proxy" "default" {
  provider         = google-beta
  name             = "default"
  url_map          = google_compute_url_map.default.id
  ssl_certificates = [google_compute_managed_ssl_certificate.default2.id]
}

resource "google_compute_target_http_proxy" "default" {
  provider = google-beta
  name     = "default"
  url_map  = google_compute_url_map.default.id
}

resource "google_compute_global_forwarding_rule" "https" {
  name                  = "https"
  provider              = google-beta
  ip_protocol           = "TCP"
  load_balancing_scheme = "EXTERNAL"
  port_range            = "443"
  target                = google_compute_target_https_proxy.default.id
  ip_address            = google_compute_global_address.default.id
}

resource "google_compute_global_forwarding_rule" "http" {
  name                  = "http"
  provider              = google-beta
  ip_protocol           = "TCP"
  load_balancing_scheme = "EXTERNAL"
  port_range            = "80"
  target                = google_compute_target_http_proxy.default.id
  ip_address            = google_compute_global_address.default.id
}

resource "google_artifact_registry_repository" "graphql-images" {
  repository_id = "graphql-images"
  provider      = google-beta
  description   = "images for the GraphQL API"
  format        = "DOCKER"
  cleanup_policies {
    id     = "keep-minimum-versions"
    action = "KEEP"
    most_recent_versions {
      # Delete old images automatically
      keep_count = 5
    }
  }
}

resource "google_cloud_run_v2_service" "graphql" {
  name     = "graphql"
  provider = google-beta
  ingress  = "INGRESS_TRAFFIC_ALL"
  location = var.region


  template {
    containers {
      image = "us-west1-docker.pkg.dev/confetti-349319/graphql-images/graphql"
      resources {
        cpu_idle = true
        startup_cpu_boost = true
      }
      env {
        name = "APOLLO_KEY"
        value_source {
          secret_key_ref {
            secret = google_secret_manager_secret.apollo-key.secret_id
            version = "latest"
          }
        }
      }
    }
  }
}

resource "google_secret_manager_secret" "apollo-key" {
  provider = google-beta
  secret_id = "apollo-key"

  replication {
    auto {}
  }
}

resource "google_cloud_run_service_iam_binding" "graphql" {
  provider = google-beta
  location = google_cloud_run_v2_service.graphql.location
  service  = google_cloud_run_v2_service.graphql.name
  role     = "roles/run.invoker"
  members  = [
    "allUsers"
  ]
}

resource "google_artifact_registry_repository" "import-images" {
  repository_id = "import-images"
  provider      = google-beta
  description   = "images for the Import API"
  format        = "DOCKER"
  cleanup_policies {
    id     = "keep-minimum-versions"
    action = "KEEP"
    most_recent_versions {
      # Delete old images automatically
      keep_count = 5
    }
  }
}

resource "google_cloud_run_v2_service" "import" {
  name     = "import"
  provider = google-beta
  ingress  = "INGRESS_TRAFFIC_ALL"
  location = var.region

  template {
    containers {
      image = "us-west1-docker.pkg.dev/confetti-349319/import-images/import"
      resources {
        cpu_idle = true
        startup_cpu_boost = true
      }
    }
  }
}

resource "google_cloud_run_service_iam_binding" "import" {
  provider = google-beta
  location = google_cloud_run_v2_service.import.location
  service  = google_cloud_run_v2_service.import.name
  role     = "roles/run.invoker"
  members  = [
    "allUsers"
  ]
}

resource "google_storage_bucket" "landing_page" {
  provider      = google-beta
  name          = "confetti-landing-page"
  # This bucket was created before everything was in terraform and uses a multi-region instead of var.region
  location      = "US"
  storage_class = "STANDARD"

  website {
    main_page_suffix = "index.html"
    not_found_page   = "404.html"
  }
}

resource "google_storage_bucket_iam_member" "member" {
  provider = google-beta
  bucket   = google_storage_bucket.landing_page.name
  role     = "roles/storage.objectViewer"
  member   = "allUsers"
}

output "ip_addr" {
  value = google_compute_global_address.default.address
}

# This is for enabling APIs
resource "google_project_service" "project" {
  provider = google-beta
  service = "secretmanager.googleapis.com"
}