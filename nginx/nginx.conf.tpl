upstream user-management {
{{ range service "user-management" }}
    server {{ .Address }}:{{ .Port }};
{{ else }}
    server 127.0.0.1:65535; #will fail -> error.html
{{ end }}
}

upstream document-management {
{{ range service "document-management" }}
    server {{ .Address }}:{{ .Port }};
{{ else }}
    server 127.0.0.1:65535; #will fail -> error.html
{{ end }}
}

server {
    listen 80;

    location /user/ {
        proxy_pass http://user-management/;
    }

    location /document/ {
        proxy_pass http://document-management/;
    }

    error_page 500 502 503 504 /error.html;
    location = /error.html {
        root /usr/share/nginx/html;
    }
}
