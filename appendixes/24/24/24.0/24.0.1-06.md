## Step 6 — Gradle JVM = 17 (brew-installed JDK)

Verify macOS sees the brew JDK:

```zsh
/usr/libexec/java_home -v 17
```

If it errors, register it (one-time; brew's `openjdk@17` is keg-only), then verify again:

```zsh
sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
```

From `CatsCarsCoins/`:

```zsh
./gradlew updateDaemonJvm --jvm-version=17
```

Creates one file — `gradle/gradle-daemon-jvm.properties`:

```properties
toolchainVersion=17
```

Sanity check the daemon picked up 17:

```zsh
./gradlew -q javaToolchains | head -20
```

Expect a `Daemon JVM: Compatible with Java 17` line (and the brew 17 listed among detected toolchains).

**File → Sync Project with Gradle Files.** Green = done.

**Why:** Spec 0.1 requires JDK 17 as the Gradle JVM. The daemon-JVM properties file is the committed, file-based pin — every machine and CI runs Gradle on 17 automatically, versus the IDE's Gradle-JDK dropdown, which is a per-machine `.idea/` setting invisible to git. Brew's only wrinkle is discovery: keg-only JDKs aren't registered in `/Library/Java/JavaVirtualMachines/` until symlinked, which the first two commands verify/fix.
