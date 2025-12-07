# Deployment Guide - Firebase Improvements & Database Connection Fix

This guide covers committing changes, deploying to VPS, and rebuilding Docker containers.

---

## 📋 Prerequisites

- Git repository access
- SSH access to VPS
- Docker and Docker Compose installed on VPS
- Firebase credentials file (if not using environment variables)

---

## 🔄 Step 1: Commit Changes Locally

### 1.1 Check Modified Files

```bash
git status
```

You should see:
- `src/main/java/com/Uqar/notification/service/FirebaseMessagingService.java`
- `src/main/java/com/Uqar/notification/config/FirebaseConfig.java`
- `src/main/java/com/Uqar/notification/service/NotificationQueueProcessor.java`
- `src/main/resources/application.yml`
- `docs/FIREBASE_IMPROVEMENTS_IMPLEMENTED.md`
- `docs/DATABASE_CONNECTION_FIX.md`
- `docs/FIREBASE_NOTIFICATION_ANALYSIS.md`

### 1.2 Review Changes (Optional)

```bash
git diff
```

### 1.3 Stage All Changes

```bash
# Stage all modified files
git add .

# Or stage specific files
git add src/main/java/com/Uqar/notification/service/FirebaseMessagingService.java
git add src/main/java/com/Uqar/notification/config/FirebaseConfig.java
git add src/main/java/com/Uqar/notification/service/NotificationQueueProcessor.java
git add src/main/resources/application.yml
git add docs/
```

### 1.4 Commit Changes

```bash
git commit -m "feat: Implement Firebase notification improvements and database connection fixes

- Implement Firebase batch API (sendMulticast) for better performance
- Add rate limiting with Resilience4j (900 req/min)
- Support environment variables for Firebase credentials
- Add graceful degradation when Firebase is unavailable
- Add Micrometer metrics for notification monitoring
- Optimize transaction handling (remove long-running transactions)
- Add retry logic for transient database connection errors
- Configure HikariCP for Docker network resilience
- Add connection keepalive to prevent connection resets

Fixes:
- Database connection reset errors in Docker environment
- Long-running transaction issues
- Missing retry logic for transient errors"
```

### 1.5 Push to Remote Repository

```bash
# If working on main/master branch
git push origin main

# Or if working on a feature branch
git push origin feature/firebase-improvements

# If branch doesn't exist remotely
git push -u origin feature/firebase-improvements
```

---

## 🖥️ Step 2: Deploy to VPS

### 2.1 SSH into VPS

```bash
ssh user@your-vps-ip
# Or
ssh user@your-vps-domain
```

### 2.2 Navigate to Project Directory

```bash
cd /path/to/your/project
# Example: cd ~/Teryaq
# Or: cd /opt/teryaq
```

### 2.3 Pull Latest Changes

```bash
# If on main branch
git pull origin main

# Or if on feature branch
git pull origin feature/firebase-improvements

# If you have uncommitted changes, stash them first
git stash
git pull origin main
git stash pop
```

### 2.4 Verify Changes

```bash
# Check that files were updated
git log -1
git status
```

---

## 🔧 Step 3: Environment Configuration

### 3.1 Check if .env File Exists

```bash
ls -la | grep .env
```

### 3.2 Create/Update .env File (if needed)

If you want to use environment variables for Firebase credentials:

```bash
nano .env
# Or
vim .env
```

**Add/Update these variables:**

```env
# Firebase Configuration
FIREBASE_CREDENTIALS_PATH=/app/firebase/serviceAccountKey.json
FIREBASE_PROJECT_ID=uqar-project-7c843
FIREBASE_MESSAGING_ENABLED=true

# Database Configuration (if not already set)
SPRING_DATASOURCE_URL=jdbc:postgresql://uqar-db:5432/uqar
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your-secure-password
SPRING_DATASOURCE_DATABASE=uqar

# JVM Settings
JAVA_OPTS=-Xmx768m -Xms256m
```

**Save and exit:**
- Nano: `Ctrl+X`, then `Y`, then `Enter`
- Vim: `Esc`, then `:wq`, then `Enter`

### 3.3 Alternative: Update docker-compose.yml Directly

If you prefer to set environment variables in `docker-compose.yml`:

```bash
nano docker-compose.yml
```

Update the `uqar-app` service environment section:

```yaml
services:
  uqar-app:
    environment:
      # Database connection
      SPRING_DATASOURCE_URL: jdbc:postgresql://uqar-db:5432/uqar
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: password
      SPRING_DATASOURCE_DATABASE: uqar
      
      # Firebase Configuration
      FIREBASE_CREDENTIALS_PATH: /app/firebase/serviceAccountKey.json
      FIREBASE_PROJECT_ID: uqar-project-7c843
      FIREBASE_MESSAGING_ENABLED: "true"
      
      # JVM Settings
      JAVA_OPTS: "-Xmx768m -Xms256m"
      
      # Disable Flyway (if needed)
      SPRING_FLYWAY_ENABLED: "false"
```

---

## 🐳 Step 4: Prepare Firebase Credentials (if using file path)

### 4.1 Create Firebase Directory in Project

```bash
mkdir -p firebase
```

### 4.2 Copy Firebase Credentials File

**Option A: If file is on VPS**
```bash
cp /path/to/serviceAccountKey.json firebase/
```

**Option B: If file needs to be uploaded from local machine**

From your local machine:
```bash
scp src/main/resources/firebase/serviceAccountKey.json user@vps-ip:/path/to/project/firebase/
```

**Option C: If using Docker secrets (recommended for production)**

Update `docker-compose.yml`:
```yaml
services:
  uqar-app:
    secrets:
      - firebase_credentials
    volumes:
      - ./firebase:/app/firebase:ro

secrets:
  firebase_credentials:
    file: ./firebase/serviceAccountKey.json
```

### 4.3 Set Proper Permissions

```bash
chmod 600 firebase/serviceAccountKey.json
chown $USER:$USER firebase/serviceAccountKey.json
```

---

## 🏗️ Step 5: Rebuild Docker Container

### 5.1 Stop Running Containers

```bash
docker-compose down
# Or if you want to remove volumes too (be careful!)
# docker-compose down -v
```

### 5.2 Rebuild Docker Image

```bash
# Rebuild without cache (recommended for clean build)
docker-compose build --no-cache

# Or rebuild with cache (faster)
docker-compose build
```

### 5.3 Start Containers

```bash
# Start in detached mode (background)
docker-compose up -d

# Or start with logs visible
docker-compose up
```

### 5.4 Verify Containers are Running

```bash
docker-compose ps
```

You should see:
```
NAME           IMAGE      STATUS
uqar-db        postgres   Up X minutes
uqar-backend   uqar-app   Up X minutes
```

---

## ✅ Step 6: Verify Deployment

### 6.1 Check Application Logs

```bash
# View logs
docker-compose logs -f uqar-app

# Or view last 100 lines
docker-compose logs --tail=100 uqar-app
```

**Look for these success messages:**
- ✅ `Firebase initialized SUCCESSFULLY`
- ✅ `FirebaseMessagingService initialized successfully`
- ✅ `NotificationQueueProcessor initialized with FirebaseMessagingService`
- ✅ `HikariPool-1 - Starting...`
- ✅ `Started UqarApplication`

**Watch for errors:**
- ❌ `Firebase credentials file NOT FOUND` (if using file path)
- ❌ `Connection reset` (should be handled by retry logic now)
- ❌ `Unable to rollback` (should be fixed)

### 6.2 Check Database Connection

```bash
# Check database container logs
docker-compose logs uqar-db

# Test database connection from app container
docker-compose exec uqar-app sh -c "echo 'SELECT 1;' | psql -h uqar-db -U postgres -d uqar"
```

### 6.3 Test Notification Endpoint (Optional)

```bash
# Check if application is responding
curl http://localhost:13000/actuator/health

# Or from outside VPS
curl http://your-vps-ip:13000/actuator/health
```

### 6.4 Check Metrics (Optional)

```bash
# View Firebase notification metrics
curl http://localhost:13000/actuator/metrics/firebase.notifications.sent

# View connection pool metrics
curl http://localhost:13000/actuator/metrics/hikari.connections
```

---

## 🔍 Step 7: Troubleshooting

### 7.1 If Firebase Initialization Fails

**Check credentials file:**
```bash
docker-compose exec uqar-app ls -la /app/firebase/
docker-compose exec uqar-app cat /app/firebase/serviceAccountKey.json | head -5
```

**Check environment variables:**
```bash
docker-compose exec uqar-app env | grep FIREBASE
```

**Solution:**
- Verify file path is correct
- Check file permissions
- Verify JSON is valid
- Check environment variables are set

### 7.2 If Database Connection Fails

**Check database container:**
```bash
docker-compose logs uqar-db
docker-compose ps uqar-db
```

**Check connection string:**
```bash
docker-compose exec uqar-app env | grep DATASOURCE
```

**Solution:**
- Verify database container is running
- Check connection string uses service name `uqar-db`
- Verify database credentials
- Check network connectivity: `docker network ls`

### 7.3 If Build Fails

**Check build logs:**
```bash
docker-compose build --no-cache 2>&1 | tee build.log
```

**Common issues:**
- Maven dependencies not downloading
- Out of memory during build
- Docker daemon not running

**Solution:**
- Increase Docker memory limit
- Check internet connection
- Restart Docker daemon: `sudo systemctl restart docker`

### 7.4 If Container Crashes

**Check exit code:**
```bash
docker-compose ps
docker inspect uqar-backend | grep -A 10 State
```

**View crash logs:**
```bash
docker-compose logs --tail=200 uqar-app
```

**Solution:**
- Check application logs for errors
- Verify all environment variables are set
- Check file permissions
- Verify database is accessible

---

## 📝 Quick Reference Commands

### Git Commands
```bash
git status                    # Check modified files
git add .                     # Stage all changes
git commit -m "message"       # Commit changes
git push origin main          # Push to remote
```

### Docker Commands
```bash
docker-compose down           # Stop containers
docker-compose build          # Rebuild images
docker-compose up -d          # Start containers
docker-compose logs -f        # View logs
docker-compose ps             # Check status
docker-compose restart        # Restart containers
```

### VPS Commands
```bash
ssh user@vps-ip              # Connect to VPS
cd /path/to/project          # Navigate to project
git pull origin main         # Pull latest changes
nano .env                    # Edit environment file
```

---

## 🎯 Complete Deployment Checklist

- [ ] Committed all changes locally
- [ ] Pushed changes to remote repository
- [ ] SSH'd into VPS
- [ ] Pulled latest changes from repository
- [ ] Created/updated `.env` file (if needed)
- [ ] Copied Firebase credentials file (if using file path)
- [ ] Updated `docker-compose.yml` (if needed)
- [ ] Stopped running containers
- [ ] Rebuilt Docker images
- [ ] Started containers
- [ ] Verified containers are running
- [ ] Checked application logs for errors
- [ ] Tested application health endpoint
- [ ] Verified Firebase initialization
- [ ] Verified database connection
- [ ] Tested notification functionality (optional)

---

## 🚀 Production Deployment Best Practices

### 1. Use Docker Secrets for Credentials
```yaml
secrets:
  firebase_credentials:
    file: ./secrets/firebase-service-account.json
```

### 2. Use Environment Variables
- Never commit credentials to repository
- Use `.env` file (add to `.gitignore`)
- Or use Docker secrets

### 3. Health Checks
- Monitor application health: `/actuator/health`
- Set up alerts for connection pool exhaustion
- Monitor Firebase notification success rates

### 4. Backup
- Backup database before deployment
- Keep previous Docker image as fallback
- Document rollback procedure

### 5. Staging Environment
- Test changes in staging first
- Verify all functionality works
- Then deploy to production

---

## 📞 Support

If you encounter issues:
1. Check application logs: `docker-compose logs uqar-app`
2. Check database logs: `docker-compose logs uqar-db`
3. Review this guide's troubleshooting section
4. Check documentation in `docs/` directory

---

**Last Updated**: 2025-01-16  
**Version**: 1.0

