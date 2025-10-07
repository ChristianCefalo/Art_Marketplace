# Build & Firebase Help

## Running Gradle
1. `./gradlew --version`
2. In Android Studio: **File ▸ Sync Project with Gradle Files**
3. After syncing, use **Run ▶** to launch on an emulator or device.

If you are behind a proxy, create `~/.gradle/gradle.properties` (Linux/macOS) or `%USERPROFILE%\.gradle\gradle.properties` (Windows) with:
```
systemProp.http.proxyHost=YOUR_PROXY_HOST
systemProp.http.proxyPort=YOUR_PROXY_PORT
systemProp.https.proxyHost=YOUR_PROXY_HOST
systemProp.https.proxyPort=YOUR_PROXY_PORT
```
Replace the placeholders with your network values. Do not commit credentials.

## Firebase Setup
Place your Firebase configuration file at `app/google-services.json`. The project builds without it, but Firebase services activate automatically once the file exists.
