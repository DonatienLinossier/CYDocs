consul {
  address = "consul:8500"
}

template {
  source      = "/etc/consul-template/nginx.conf.tpl"
  destination = "/etc/nginx/conf.d/default.conf"
  command     = "nginx -t && nginx -s reload || nginx"
}
