# Corrected Deployment Steps

## ✅ Your Steps (Mostly Correct!)

Your steps are **almost perfect**, but you're missing **one critical step**: mounting the `firebase` directory as a volume in `docker-compose.yml`.

---

## 📋 Complete Corrected Steps

### ✅ Step 1: Push Edits (DONE)
```bash
git add .
git commit -m "feat: Firebase improvements and database fixes"
git push origin main
```

### ✅ Step 2: Pull on Server (DONE)
```bash
ssh user@vps-ip
cd /path/to/Teryaq
git pull origin main
```

### ✅ Step 3: Create .env File (DONE)
```bash
nano .env
```
Add:
```env
FIREBASE_CREDENTIALS_PATH=/app/firebase/serviceAccountKey.json
FIREBASE_PROJECT_ID=uqar-project-7c843
FIREBASE_MESSAGING_ENABLED=true
SPRING_DATASOURCE_URL=jdbc:postgresql://uqar-db:5432/uqar
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your-secure-password
SPRING_DATASOURCE_DATABASE=uqar
JAVA_OPTS=-Xmx768m -Xms256m
```

### ✅ Step 4: Update docker-compose.yml (DONE)
Add to `uqar-app` service `environment` section:
```yaml
environment:
  # ... existing variables ...
  FIREBASE_CREDENTIALS_PATH: /app/firebase/serviceAccountKey.json
  FIREBASE_PROJECT_ID: uqar-project-7c843
  FIREBASE_MESSAGING_ENABLED: "true"
```

### ✅ Step 5: Create firebase Directory (DONE)
```bash
mkdir -p firebase
```

### ✅ Step 6: Copy serviceAccountKey.json (DONE)
```bash
# Copy from classpath to root firebase directory
cp src/main/resources/firebase/serviceAccountKey.json firebase/
# Or upload from local machine
```

### ⚠️ Step 7: ADD VOLUME MOUNT (MISSING!)
**This is the critical missing step!**

Update `docker-compose.yml` - add `volumes` section to `uqar-app` service:

```yaml
  uqar-app:
    build: .
    image: uqar-app
    # ... existing config ...
    environment:
      # ... your environment variables ...
    volumes:
      # Mount firebase directory so container can access the file
      - ./firebase:/app/firebase:ro
    networks:
      - uqar-net
```

**Why `:ro`?** 
- `:ro` means "read-only" - the container can read the file but not modify it
- This is more secure for credentials

### ✅ Step 8: Build Docker (DONE)
```bash
docker-compose down
docker-compose build --no-cache
```

### ✅ Step 9: Start Container (DONE)
```bash
docker-compose up -d
```

---

## 🔍 Complete docker-compose.yml Example

Here's what your `uqar-app` service should look like:

```yaml
  uqar-app:
    build: .
    image: uqar-app
    depends_on:
      uqar-db:
        condition: service_healthy
    container_name: uqar-backend
    ports:
      - "13000:3000"
    environment:
      # Database
      SPRING_DATASOURCE_URL: jdbc:postgresql://uqar-db:5432/uqar
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: password
      SPRING_DATASOURCE_DATABASE: uqar
      SPRING_FLYWAY_ENABLED: "false"
      JAVA_OPTS: "-Xmx768m -Xms256m"
      # Firebase
      FIREBASE_CREDENTIALS_PATH: /app/firebase/serviceAccountKey.json
      FIREBASE_PROJECT_ID: uqar-project-7c843
      FIREBASE_MESSAGING_ENABLED: "true"
    volumes:
      # ⚠️ THIS IS THE MISSING STEP!
      - ./firebase:/app/firebase:ro
    networks:
      - uqar-net
    restart: unless-stopped
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 1024M
        reservations:
          cpus: '0.25'
          memory: 512M
```

---

## 🎯 Alternative: Use Classpath (Simpler)

If you want to **avoid the volume mount**, you can use the classpath file that's already in your project:

### Option A: Use Classpath (No Volume Needed)

1. **Remove** the `firebase` directory you created in root
2. **Update** `.env` and `docker-compose.yml`:
   ```env
   FIREBASE_CREDENTIALS_PATH=classpath:firebase/serviceAccountKey.json
   ```
3. **Remove** the volume mount from `docker-compose.yml`
4. The file in `src/main/resources/firebase/serviceAccountKey.json` will be packaged into the JAR

**Pros:**
- Simpler setup
- No volume mount needed
- File is in the JAR

**Cons:**
- Credentials are in the JAR (less secure)
- Need to rebuild if credentials change

### Option B: Use File System Path (Current Approach)

1. Keep the `firebase` directory in root
2. **Add volume mount** (the missing step!)
3. Use file system path: `/app/firebase/serviceAccountKey.json`

**Pros:**
- Credentials not in JAR
- Can update credentials without rebuilding
- More secure

**Cons:**
- Need volume mount
- Need to manage file separately

---

## ✅ Verification After Deployment

### 1. Check Volume is Mounted
```bash
docker-compose exec uqar-app ls -la /app/firebase/
```

Should show:
```
-rw-r--r-- 1 root root XXXX serviceAccountKey.json
```

### 2. Check Environment Variables
```bash
docker-compose exec uqar-app env | grep FIREBASE
```

Should show:
```
FIREBASE_CREDENTIALS_PATH=/app/firebase/serviceAccountKey.json
FIREBASE_PROJECT_ID=uqar-project-7c843
FIREBASE_MESSAGING_ENABLED=true
```

### 3. Check Application Logs
```bash
docker-compose logs uqar-app | grep -i firebase
```

Should see:
```
✅ Firebase initialized SUCCESSFULLY
✅ FirebaseMessagingService initialized successfully
```

### 4. Check for Errors
```bash
docker-compose logs uqar-app | grep -i "credentials file not found"
```

Should be **empty** (no errors).

---

## 🚨 Common Issues

### Issue 1: "Firebase credentials file NOT FOUND"

**Cause:** Volume not mounted or wrong path

**Solution:**
```bash
# Check if volume is mounted
docker-compose exec uqar-app ls -la /app/firebase/

# If directory doesn't exist, add volume mount to docker-compose.yml
# Then rebuild:
docker-compose down
docker-compose up -d
```

### Issue 2: "Permission denied"

**Cause:** File permissions

**Solution:**
```bash
chmod 644 firebase/serviceAccountKey.json
docker-compose restart uqar-app
```

### Issue 3: File Not Found in Container

**Cause:** Volume mount path incorrect

**Solution:**
- Check `docker-compose.yml` has: `- ./firebase:/app/firebase:ro`
- Verify `firebase` directory exists in project root
- Rebuild: `docker-compose down && docker-compose up -d`

---

## 📝 Quick Fix Command

If you've already built and started, just add the volume and restart:

```bash
# 1. Edit docker-compose.yml - add volumes section
nano docker-compose.yml

# 2. Add this under uqar-app service:
volumes:
  - ./firebase:/app/firebase:ro

# 3. Restart (no need to rebuild)
docker-compose down
docker-compose up -d

# 4. Verify
docker-compose exec uqar-app ls -la /app/firebase/
docker-compose logs uqar-app | grep -i firebase
```

---

## ✅ Summary

**Your steps were 99% correct!** You just need to:

1. ✅ Add volume mount to `docker-compose.yml`:
   ```yaml
   volumes:
     - ./firebase:/app/firebase:ro
   ```

2. ✅ Rebuild/restart:
   ```bash
   docker-compose down
   docker-compose build --no-cache
   docker-compose up -d
   ```

That's it! 🎉

