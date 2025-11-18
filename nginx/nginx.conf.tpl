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

    # Set up variables for CORS policy
    set $cors_origin "http://localhost:5173";

    # Common CORS headers
    add_header 'Access-Control-Allow-Methods' 'GET, POST, OPTIONS, PUT, DELETE' always;
    add_header 'Access-Control-Allow-Headers' 'DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization' always;
    add_header 'Access-Control-Max-Age' 1728000 always;
    
    # Specific headers for credentials
    add_header 'Access-Control-Allow-Origin' $cors_origin always;
    add_header 'Access-Control-Allow-Credentials' 'true' always;

    location /user/ {
        if ($request_method = 'OPTIONS') {
            return 204; # No Content
        }

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
