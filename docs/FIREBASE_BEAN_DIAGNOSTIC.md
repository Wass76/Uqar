# Firebase Bean Creation Diagnostic Guide

## Problem
Firebase initializes successfully, but `FirebaseMessagingService` is not available.

## Root Cause
The `FirebaseMessaging` bean is not being created, which prevents `FirebaseMessagingService` from being created (it has `@ConditionalOnBean(FirebaseMessaging.class)`).

## Diagnostic Steps

### 1. Check All Firebase-Related Logs

```bash
docker-compose logs uqar-app | grep -i firebase
```

**Look for these messages in order:**

1. ✅ `Firebase initialized SUCCESSFULLY` - Should appear
2. ✅ `FirebaseApp bean created successfully` - **MUST appear**
3. ✅ `FirebaseMessaging bean created successfully` - **MUST appear**
4. ✅ `FirebaseMessagingService initialized successfully` - **MUST appear**

### 2. Check for Warnings/Errors

```bash
docker-compose logs uqar-app | grep -i "firebase\|warn\|error" | grep -i firebase
```

**Common issues:**
- `FirebaseApp is null` - Bean method returned null
- `FirebaseApp is not initialized` - FirebaseApp.getApps() is empty
- `Error creating FirebaseMessaging bean` - Exception during creation

### 3. Verify Environment Variables

```bash
docker-compose exec uqar-app env | grep FIREBASE
```

**Should show:**
```
FIREBASE_CREDENTIALS_PATH=/app/firebase/serviceAccountKey.json
FIREBASE_PROJECT_ID=uqar-project-7c843
FIREBASE_MESSAGING_ENABLED=true
```

### 4. Check Application Properties

```bash
docker-compose exec uqar-app sh -c "cat /app/BOOT-INF/classes/application.yml | grep -A 5 firebase"
```

**Should show:**
```yaml
firebase:
  credentials:
    path: ${FIREBASE_CREDENTIALS_PATH:classpath:firebase/serviceAccountKey.json}
  project-id: ${FIREBASE_PROJECT_ID:uqar-project-7c843}
  messaging:
    enabled: ${FIREBASE_MESSAGING_ENABLED:true}
```

## Solutions

### Solution 1: Check Bean Creation Order

The issue might be that `FirebaseApp` bean is created but returns `null`. Check logs:

```bash
docker-compose logs uqar-app | grep "FirebaseApp bean"
```

If you see: `FirebaseApp is not initialized` or `FirebaseApp is null`, then:

1. **Check if Firebase actually initialized:**
   ```bash
   docker-compose logs uqar-app | grep "Firebase initialized"
   ```

2. **If Firebase initialized but bean returns null:**
   - This means `FirebaseApp.getApps().isEmpty()` is returning true
   - This can happen if there's a timing issue

### Solution 2: Force Bean Creation

If the bean methods aren't being called, try:

1. **Restart the container:**
   ```bash
   docker-compose restart uqar-app
   ```

2. **Check startup logs:**
   ```bash
   docker-compose logs -f uqar-app
   ```

### Solution 3: Verify Firebase File

```bash
# Check file exists and is readable
docker-compose exec uqar-app ls -la /app/firebase/serviceAccountKey.json

# Check file content (first few lines)
docker-compose exec uqar-app head -5 /app/firebase/serviceAccountKey.json

# Should show valid JSON starting with {
```

### Solution 4: Check Spring Bean Context

Add this temporary endpoint to check beans:

```java
@GetMapping("/debug/firebase-beans")
public ResponseEntity<Map<String, Boolean>> checkFirebaseBeans() {
    Map<String, Boolean> beans = new HashMap<>();
    try {
        beans.put("FirebaseApp", applicationContext.getBean(FirebaseApp.class) != null);
    } catch (Exception e) {
        beans.put("FirebaseApp", false);
    }
    try {
        beans.put("FirebaseMessaging", applicationContext.getBean(FirebaseMessaging.class) != null);
    } catch (Exception e) {
        beans.put("FirebaseMessaging", false);
    }
    try {
        beans.put("FirebaseMessagingService", applicationContext.getBean(FirebaseMessagingService.class) != null);
    } catch (Exception e) {
        beans.put("FirebaseMessagingService", false);
    }
    return ResponseEntity.ok(beans);
}
```

Then check:
```bash
curl http://localhost:13000/api/v1/debug/firebase-beans
```

## Expected Log Sequence

When everything works correctly, you should see this sequence:

```
INFO  c.U.n.config.FirebaseConfig : ✅ Firebase initialized SUCCESSFULLY
INFO  c.U.n.config.FirebaseConfig : ✅ FirebaseApp bean created successfully: [DEFAULT]
INFO  c.U.n.config.FirebaseConfig : ✅ FirebaseMessaging bean created successfully
INFO  c.U.n.service.FirebaseMessagingService : FirebaseMessagingService initialized successfully
INFO  c.U.n.service.NotificationQueueProcessor : NotificationQueueProcessor initialized with FirebaseMessagingService
```

## Quick Fix

If you see "Firebase initialized SUCCESSFULLY" but not "FirebaseApp bean created successfully", try:

1. **Rebuild with the latest code:**
   ```bash
   docker-compose down
   docker-compose build --no-cache
   docker-compose up -d
   ```

2. **Check logs immediately after startup:**
   ```bash
   docker-compose logs -f uqar-app | grep -i firebase
   ```

3. **If still not working, check the actual issue:**
   ```bash
   docker-compose logs uqar-app | grep -A 5 -B 5 "FirebaseApp\|FirebaseMessaging"
   ```

## Common Issues

### Issue 1: Bean Method Not Called
**Symptom:** No "FirebaseApp bean created" log
**Cause:** `@ConditionalOnProperty` condition not met
**Fix:** Check `FIREBASE_MESSAGING_ENABLED` environment variable

### Issue 2: Bean Returns Null
**Symptom:** "FirebaseApp is not initialized" log
**Cause:** `FirebaseApp.getApps().isEmpty()` returns true
**Fix:** Check if Firebase actually initialized in @PostConstruct

### Issue 3: FirebaseMessaging Bean Not Created
**Symptom:** "FirebaseMessaging bean created" log missing
**Cause:** `FirebaseApp` bean is null or `@ConditionalOnBean` fails
**Fix:** Ensure `FirebaseApp` bean is created first

### Issue 4: Service Not Available
**Symptom:** "FirebaseMessagingService is not available"
**Cause:** `FirebaseMessaging` bean not created
**Fix:** Fix `FirebaseMessaging` bean creation first

