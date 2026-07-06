## Step 4 — Commit and Push the compileSdk Fix

```zsh
git add CatsCarsCoins/app/build.gradle.kts
git status
```

Staged list must show exactly one file: `CatsCarsCoins/app/build.gradle.kts`.

```zsh
git commit -m "fix(CatsCarsCoins): compileSdk 37 (AAR metadata requires API 37)"
git push
```

**Why:** One fix, one commit — the diff is a single reviewable line against the pristine baseline, and the commit message records the reason (AAR metadata requirement) so the history explains itself.
