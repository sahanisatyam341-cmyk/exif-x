# EXIF.X — OSINT Lab (v2.0)

A local-first Android/WebView photo metadata & forensics viewer.

## Features
- **Bulletproof Mobile Uploads:** Touch-friendly gallery, camera capture, clipboard paste, and batch photo cleaning.
- **Full EXIF & GPS Forensics:** Extracts camera specs, GPS coordinates with interactive OpenStreetMap preview & address lookup, and hidden trailing bytes.
- **ELA Tamper Detection:** Visual Error Level Analysis heatmap to spot edited areas.
- **Flex Report Card:** Generates a shareable Privacy Risk Score card with one-tap report copying.
- **Privacy Lab & Diagnostics:** Local browser/device diagnostics and session log viewer.

## Project Structure
- `app/src/main/assets/index.html` — Complete EXIF.X single-file web application
- `app/src/main/java/com/satyam/exifx/MainActivity.java` — Enhanced Android WebView activity with multi-file chooser
- `app/src/main/AndroidManifest.xml` — Android manifest permissions & configuration
- `app/build.gradle` — App module Gradle configuration
