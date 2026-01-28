# Google Maps API Key Security

## ⚠️ IMPORTANT: API Key Protection

The Google Maps API key is stored in `local.properties` which is **gitignored** and will **NEVER** be committed to version control.

## Setup Instructions

### For Development

1. Open `local.properties` in the project root
2. Add your Google Maps API key:
   ```properties
   MAPS_API_KEY=YOUR_ACTUAL_API_KEY_HERE
   ```
3. The Secrets Gradle Plugin will automatically inject it into the app during build

### For CI/CD

1. Set the `MAPS_API_KEY` as an environment variable or secret in your CI/CD platform
2. The `secrets_defaults.properties` file provides a placeholder for builds without the real key

## Files Involved

- **`local.properties`** - Contains the actual API key (gitignored, never commit!)
- **`secrets_defaults.properties`** - Default/placeholder for CI/CD builds (safe to commit)
- **`app/src/main/AndroidManifest.xml`** - Uses `${MAPS_API_KEY}` placeholder
- **`.gitignore`** - Ensures `local.properties` is never committed

## Verify Protection

To verify your API key is protected:

```bash
# Check that local.properties is gitignored
git check-ignore -v local.properties

# Make sure local.properties is not tracked
git status local.properties
```

## The Build Process

1. Secrets Gradle Plugin reads `local.properties`
2. Replaces `${MAPS_API_KEY}` in AndroidManifest with actual key
3. API key is embedded in APK but NOT in source code

## ✅ Security Checklist

- [x] `local.properties` is in `.gitignore`
- [x] API key is NOT hardcoded in source files
- [x] Secrets Gradle Plugin is properly configured
- [x] Default properties file exists for CI/CD
