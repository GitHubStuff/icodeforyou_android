## Step002 — Commit and Push the Pristine Project

Repo: `GitHubStuff/icodeforyou_kotlin` (Kotlin monorepo). `CatsCarsCoins/` sits next to `SqlEditor/`. No `git init`, no remote setup — both already exist.

```zsh
git add CatsCarsCoins
git status
```

Check the staged list. Must NOT appear: `local.properties`, `.gradle/`, any `build/`. Should appear: `app/`, `gradle/`, `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `gradlew`, `gradlew.bat`, `.gitignore`.

```zsh
git commit -m "chore(CatsCarsCoins): pristine Android Studio project (Empty Activity, minSdk 31)"
git push
```

**Why:** The untouched wizard output is committed before any edit, so every change after it (starting with Step 3's compileSdk fix) is a small reviewable diff instead of noise. In a monorepo the project folder must never contain its own `.git` — a repo-inside-a-repo becomes a gitlink and the parent tracks an empty pointer instead of the files.
