# Full deployment steps – api.uqarsoft.com with SSL

Do these steps **in order** on your VPS.

---

## Step 1: Create certbot webroot and use HTTP-only Nginx config

Certbot needs Nginx to serve `/.well-known/acme-challenge/` over **HTTP**. We first use a config **without** HTTPS so the certificate files don’t need to exist yet.

```bash
sudo mkdir -p /var/www/certbot
```

Copy the **HTTP-only** config over the current site config (this replaces whatever is there now):

```bash
sudo cp ~/Uqar/deployment/api.uqarsoft.com.nginx.conf.http-only /etc/nginx/sites-available/api.uqarsoft.com
```

Test and reload Nginx:

```bash
sudo nginx -t && sudo systemctl reload nginx
```

If `nginx -t` fails, fix the reported errors before continuing.

---

## Step 2: Get the SSL certificate

```bash
sudo certbot certonly --webroot -w /var/www/certbot -d api.uqarsoft.com
```

- Use a valid email when asked.
- Agree to the terms.
- You should see: “Successfully received certificate.”

---

## Step 3: Switch to the full Nginx config (with SSL)

Replace the site config with the **full** one that enables HTTPS:

```bash
sudo cp ~/Uqar/deployment/api.uqarsoft.com.nginx.conf /etc/nginx/sites-available/api.uqarsoft.com
```

Test and reload:

```bash
sudo nginx -t && sudo systemctl reload nginx
```

If you see **“protocol options redefined”** and your Nginx is older, you can ignore that warning if `nginx -t` still reports “syntax is ok” and “test is successful”. If the test **fails**, say so and we can adjust the config.

---

## Step 4: Ensure the app is listening

Your Spring Boot app must be running and listening on port **3000** (or **13000** if you use Docker).

If using the JAR:

```bash
# Example – run in background or use systemd
java -jar ~/Uqar/target/Uqar-0.0.1-SNAPSHOT.jar --server.port=3000
```

If using Docker:

```bash
cd ~/Uqar && docker compose up -d
```

If the app is on port **13000**, edit the Nginx config:

```bash
sudo nano /etc/nginx/sites-available/api.uqarsoft.com
```

Change `proxy_pass http://127.0.0.1:3000;` to `proxy_pass http://127.0.0.1:13000;`, then:

```bash
sudo nginx -t && sudo systemctl reload nginx
```

---

## Step 5: Verify

- HTTP: `http://api.uqarsoft.com` → should redirect to HTTPS.
- HTTPS: `https://api.uqarsoft.com` → should show your API (e.g. Swagger or your base path).

---

## Summary

| Step | What to do |
|------|------------|
| 1 | `sudo cp .../api.uqarsoft.com.nginx.conf.http-only /etc/nginx/sites-available/api.uqarsoft.com` then `nginx -t` and `reload nginx` |
| 2 | `sudo certbot certonly --webroot -w /var/www/certbot -d api.uqarsoft.com` |
| 3 | `sudo cp .../api.uqarsoft.com.nginx.conf /etc/nginx/sites-available/api.uqarsoft.com` then `nginx -t` and `reload nginx` |
| 4 | Start your app on 3000 (or 13000) and fix port in Nginx if needed |
| 5 | Test http and https in the browser |

The Certbot 404 happened because the previous Nginx config had errors and never reloaded, so `/.well-known/acme-challenge/` was never served. Using the HTTP-only config first fixes that.
