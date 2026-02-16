# Deploy Teryaq API on api.uqarsoft.com with SSL

Follow these steps on your **Linux server** (Ubuntu/Debian assumed).

---

## Prerequisites

- A server with **api.uqarsoft.com** pointing to its IP (A record in DNS).
- SSH access and sudo.
- Java 17+ (for JAR) or Docker (for containerized deploy).

---

## 1. Prepare the application

### Option A: Run as JAR (recommended for a single server)

On your **local machine** or CI:

```bash
cd "path/to/Teryaq"
mvn clean package -DskipTests
```

Copy the JAR to the server (e.g. `scp target/Uqar-0.0.1-SNAPSHOT.jar user@api.uqarsoft.com:/opt/uqar/`).

On the **server**, run the app (example with port 3000):

```bash
java -jar /opt/uqar/Uqar-0.0.1-SNAPSHOT.jar --server.port=3000
```

For production, use a process manager (see step 4).

### Option B: Run with Docker Compose

On the server, clone/copy the project and run:

```bash
cd /opt/uqar   # or your project path
docker compose up -d
```

The app will be on port **13000** on the host. In the Nginx config, use `proxy_pass http://127.0.0.1:13000;` instead of `3000`.

---

## 2. Install Nginx and Certbot

```bash
sudo apt update
sudo apt install -y nginx certbot python3-certbot-nginx
```

---

## 3. Get SSL certificate (Do this before using the full Nginx SSL config)

First use a **temporary** Nginx config that only listens on port 80 and serves the ACME challenge, so Certbot can verify the domain.

Create a minimal config:

```bash
sudo nano /etc/nginx/sites-available/api.uqarsoft.com
```

Paste **only** this (no SSL yet):

```nginx
server {
    listen 80;
    server_name api.uqarsoft.com;
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
        allow all;
    }
    location / {
        proxy_pass http://127.0.0.1:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

If you use Docker and the app is on 13000, change `3000` to `13000`.

Enable and test:

```bash
sudo mkdir -p /var/www/certbot
sudo ln -sf /etc/nginx/sites-available/api.uqarsoft.com /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

Obtain the certificate:

```bash
sudo certbot certonly --webroot -w /var/www/certbot -d api.uqarsoft.com
```

Follow the prompts (email, agree to terms). Certbot will create the certificate under `/etc/letsencrypt/live/api.uqarsoft.com/`.

---

## 4. Use the full Nginx config with SSL

Replace the content of your site config with the full version that enables HTTPS:

```bash
sudo nano /etc/nginx/sites-available/api.uqarsoft.com
```

Paste the contents of `deployment/api.uqarsoft.com.nginx.conf` from this project.

- If the app runs via **Docker** on port **13000**, change:
  - `proxy_pass http://127.0.0.1:3000;` → `proxy_pass http://127.0.0.1:13000;`

Test and reload:

```bash
sudo nginx -t && sudo systemctl reload nginx
```

HTTP will redirect to HTTPS, and SSL will be served for **api.uqarsoft.com**.

---

## 5. Run the app as a service (Option A – JAR only)

Create a systemd unit so the JAR runs on boot and restarts on failure:

```bash
sudo nano /etc/systemd/system/uqar.service
```

Content (adjust paths and Java options if needed):

```ini
[Unit]
Description=Uqar Teryaq API
After=network.target postgresql.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/uqar
ExecStart=/usr/bin/java -Xmx768m -Xms256m -jar /opt/uqar/Uqar-0.0.1-SNAPSHOT.jar --server.port=3000
Restart=on-failure
RestartSec=10
Environment="SPRING_PROFILES_ACTIVE=prod"

[Install]
WantedBy=multi-user.target
```

Then:

```bash
sudo systemctl daemon-reload
sudo systemctl enable uqar
sudo systemctl start uqar
sudo systemctl status uqar
```

Ensure `application.yml` or `application-prod.yml` on the server has the correct database URL, username, and password for production.

---

## 6. Auto-renew SSL

Certbot installs a cron/systemd timer. Check renewal:

```bash
sudo certbot renew --dry-run
```

If it succeeds, real renewals will run automatically. Nginx will pick up renewed certificates after reload (Certbot’s hook often runs `nginx -s reload`).

---

## 7. Firewall (optional)

If you use UFW:

```bash
sudo ufw allow 'Nginx Full'
sudo ufw allow OpenSSH
sudo ufw enable
```

---

## Summary

| Step | Action |
|------|--------|
| 1 | Build JAR or run Docker; app listening on 3000 (or 13000 for Docker) |
| 2 | Install Nginx + Certbot |
| 3 | Minimal Nginx on :80 → get certificate with `certbot certonly --webroot` |
| 4 | Switch to full Nginx config with SSL (from `api.uqarsoft.com.nginx.conf`) |
| 5 | Run JAR with systemd (or keep Docker Compose running) |
| 6 | Rely on Certbot for SSL renewal |

Your API will be available at **https://api.uqarsoft.com** with a valid SSL certificate.
