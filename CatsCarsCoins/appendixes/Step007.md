## Step 7 — Commit and Push the Project Baseline

First commit of `CatsCarsCoins/` into `GitHubStuff/icodeforyou_android` — includes the Step 6 daemon-JVM pin. From the repo root:

```zsh
git add CatsCarsCoins
git status
```

Check the staged list. Must NOT appear: `local.properties`, `.gradle/`, `build/`, `.kotlin/`, any `.DS_Store`. Must appear: `app/`, `gradle/` (including `gradle/gradle-daemon-jvm.properties`), `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `gradlew`, `gradlew.bat`, `.gitignore`.

```zsh
git commit -m "feat(CatsCarsCoins): project standup (Empty Activity, minSdk 31, Gradle 9.4.1, daemon JVM 17)"
git push
```

**Why:** Everything from project creation through the JVM pin lands as the baseline commit; every change after it is a small reviewable diff. The daemon-JVM properties file rides along so any clone builds on JDK 17 without IDE configuration.
