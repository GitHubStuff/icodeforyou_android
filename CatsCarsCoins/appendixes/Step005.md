## Step 5 — Verify the Gradle Wrapper Pin (no edit)

View: **Android view:** Gradle Scripts → `gradle-wrapper.properties (Gradle Version)`. **Project view:** `CatsCarsCoins/gradle/wrapper/gradle-wrapper.properties`. Same file.

Confirm the `distributionUrl` line reads exactly:

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-9.4.1-bin.zip
```

It does — the wizard generated the spec's pin. No edit, no commit.

The IDE banner "Gradle 9.6.1 is available" is an upgrade nag, not an error. **Ignore it.** Do not click upgrade.

**Why:** Spec §2 pins Gradle 9.4.1 — "latest but stable," pinned in one place so every machine builds identically. Chasing the newest version mid-standup unpins the toolchain and adds an AGP-compatibility variable. If the pin ever moves, it moves via a spec revision then a one-line wrapper diff — never via the IDE banner. `-bin` (not `-all`) is correct; sources aren't needed to build.
