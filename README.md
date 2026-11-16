# RabbiStream

Standalone Android prototype, built with AndroidX Media3 and a WebView shell.

## Build
Open this repository in Android Studio with JDK 17 and Android SDK Platform 35, or use the GitHub Actions workflow. The workflow creates an installable debug APK signed with the Android debug key and uploads it as an artifact.

## Prototype limitations
- This is not a finished YouTube replacement.
- XHamster supplies its own web UI and player; native playback works only if a standard HTML5 video URL is exposed to the page.
- Some sites use blob URLs, expiring URLs, encrypted or protected streams, custom player implementations, or required request headers. Those may not work.
- Queue and automatic next apply only to valid detected direct media URLs.
- Direct download only works where the site exposes a downloadable direct media URL and permits it.
- The blocklist is a small host denylist, not uBlock Origin. It is not equivalent to uBlock Origin filter parsing or cosmetic filtering.
- Ambient mode and several YouTube-specific features are not implemented.
- Release signing requires a private keystore controlled and backed up by the app owner. Never commit a keystore or passwords to this repository.
