variable "region" {
  default = "us-west1"
}

provider google-beta {
  project = "confetti-349319"
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

  path_matcher {
    name = "default"
    default_service = google_compute_backend_bucket.landing_page.id

    path_rule {
      paths = [
        "/graphiql",
        "/graphql"
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
    name = "router"
    default_service = google_compute_backend_service.router.id

    path_rule {
      paths = [
        "/graphiql",
        "/graphql"
      ]
      service = google_compute_backend_service.router.id
    }
  }
}

resource "google_compute_backend_bucket" "landing_page" {
  provider              = google-beta
  name        = "landing-page"
  bucket_name = "confetti-landing-page"
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
    enable = true
    sample_rate = 1
  }
  backend {
    group = google_compute_global_network_endpoint_group.router.id
  }
  cdn_policy {
    cache_mode = "USE_ORIGIN_HEADERS"

    cache_key_policy {
      include_protocol = true
      include_host = true
      include_query_string = true
      include_http_headers = ["conference"]
    }
  }
  compression_mode = "DISABLED"
  protocol = "HTTPS"
}

resource "google_compute_backend_service" "graphql" {
  provider                        = google-beta
  name                            = "graphql"
  enable_cdn                      = true

  custom_response_headers = ["X-Cache-Hit: {cdn_cache_status}"]

  log_config {
    enable = true
    sample_rate = 1
  }

  backend {
    group = google_compute_region_network_endpoint_group.graphql.id
  }

  cdn_policy {
    cache_mode = "USE_ORIGIN_HEADERS"

    cache_key_policy {
      include_protocol = true
      include_host = true
      include_query_string = true
      include_http_headers = ["conference"]
    }
  }
  compression_mode = "DISABLED"
}

resource "google_compute_backend_service" "import" {
  provider                        = google-beta
  name                            = "import"
  enable_cdn                      = true

  custom_response_headers = ["X-Cache-Hit: {cdn_cache_status}"]

  log_config {
    enable = true
    sample_rate = 1
  }

  backend {
    group = google_compute_region_network_endpoint_group.import.id
  }

  cdn_policy {
    cache_mode = "USE_ORIGIN_HEADERS"

    cache_key_policy {
      include_protocol = true
      include_host = true
      include_query_string = true
      include_http_headers = ["conference"]
    }
  }
  compression_mode = "DISABLED"
}

resource "google_compute_region_network_endpoint_group" "graphql" {
  provider = google-beta
  name = "graphql"
  region  = var.region
  network_endpoint_type = "SERVERLESS"

  app_engine {
    service = "graphql"
  }
}

resource "google_compute_region_network_endpoint_group" "import" {
  provider = google-beta
  name = "import"
  region  = var.region
  network_endpoint_type = "SERVERLESS"

  app_engine {
    service = "import"
  }
}

resource "google_compute_managed_ssl_certificate" "default" {
  name     = "default"
  provider = google-beta

  managed {
    domains = ["confetti-app.dev", "router.confetti-app.dev"]
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
  ssl_certificates = [google_compute_managed_ssl_certificate.default.id]
}

resource "google_compute_target_http_proxy" "default" {
  provider         = google-beta
  name             = "default"
  url_map          = google_compute_url_map.default.id
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

output "ip_addr" {
  value = google_compute_global_address.default.address
}
