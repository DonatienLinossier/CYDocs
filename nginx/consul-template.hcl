consul {
  address = "consul:8500"
}

template {
  source      = "/etc/consul-template/nginx.conf.tpl"
  destination = "/etc/nginx/conf.d/default.conf"
  # Commande simplifiée : si nginx tourne on reload, sinon on le lance
  command     = "pgrep nginx > /dev/null && nginx -s reload || nginx"
}