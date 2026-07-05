## Step 3 — compileSdk 37 (edit + sync only)

View: either works — **Android view:** Gradle Scripts → `build.gradle.kts (Module :app)`. **Project view:** `CatsCarsCoins/app/build.gradle.kts`. Same file.

In the `android { ... }` block, find:

```kotlin
compileSdk {
    version = release(36, minorApiLevel = 1)
}
```

Replace with:

```kotlin
compileSdk {
    version = release(37)
}
```

**File → Sync Project with Gradle Files.** If prompted to install SDK Platform 37 — accept. Sync must end green, the 3 AAR errors gone.

**Why:** Wizard generated compileSdk 36.1; the androidx libs it pulled require API 37, and the spec pins 37 anyway (§2). compileSdk only affects compile-time API surface — minSdk 31 / targetSdk untouched, no runtime change. If the generated block differs from the "find" block above, paste the `android { }` block before editing.
