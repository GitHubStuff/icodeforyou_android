// MainActivity.kt
// CatsCarsCoins — spec 24.3.7. Complete file.
// Change from 24.2.52: Coins rail icon is Icons.Default.CurrencyBitcoin —
// material-icons-extended adopted by user decision (24.3.7), superseding
// the 24.2.38 core-set ShoppingCart workaround.
// Change from 24.2.38: safe-area insets applied at the shell. The app is
// edge-to-edge (enableEdgeToEdge) and NavigationSuiteScaffold insets only
// its own chrome — content needs safeDrawing Top+Horizontal, applied ONCE
// here so screens stay inset-ignorant. Bottom excluded: the bar/rail owns
// that edge.
// Corrections folded in (splash integrity): insets are conditional — the
// splash stays full-bleed (no padded strip showing window background);
// NotifierHost is disabled during splash so an early poll's toast waits
// until the splash ends.
// Change from previous version: the duplicated splash rendering is gone.
// NavDisplay is now the single source of truth for ALL destinations; the
// navigation chrome is hidden on splash via layoutType (data), not by
// tearing down the spec 0.5 tree (structure). The locked tree
// AppTheme { NavigationSuiteScaffold { NotifierHost { NavDisplay } } }
// now holds on every frame of the app's lifetime, splash included.
package com.icodeforyou.catscarscoins

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.icodeforyou.catscarscoins.coins.ui.CoinsScreen
import com.icodeforyou.catscarscoins.nav.AppNavKey
import com.icodeforyou.catscarscoins.nav.CoinsKey
import com.icodeforyou.catscarscoins.nav.MainKey
import com.icodeforyou.catscarscoins.nav.SettingsKey
import com.icodeforyou.catscarscoins.nav.SplashKey
import com.icodeforyou.catscarscoins.notifier.NotifierHost
import com.icodeforyou.catscarscoins.preferences.ui.SettingsScreen
import com.icodeforyou.catscarscoins.preferences.ui.ThemeViewModel
import com.icodeforyou.catscarscoins.ui.theme.AppTheme
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** How long the in-app splash entry holds before advancing to Main. */
private val SPLASH_HOLD: Duration = 4000.milliseconds

/** A rail/bar destination: key + chrome presentation. */
private data class TopLevelDestination(
    val key: AppNavKey,
    val label: String,
    val icon: ImageVector,
)

/**
 * The navigation chrome's contents, in display order. Adding a destination
 * here is the only chrome change a new feature needs.
 */
private val TOP_LEVEL_DESTINATIONS = listOf(
    TopLevelDestination(key = MainKey, label = "Main", icon = Icons.Default.Home),
    TopLevelDestination(key = CoinsKey, label = "Coins", icon = Icons.Default.CurrencyBitcoin),
    TopLevelDestination(key = SettingsKey, label = "Settings", icon = Icons.Default.Settings),
)

/**
 * Single Activity (spec §5). Owns the spec 0.5 tree:
 * AppTheme { NavigationSuiteScaffold { NotifierHost { NavDisplay } } }.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = koinViewModel()
            val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()

            AppTheme(themeMode = themeMode) {
                AppShell()
            }
        }
    }
}

/**
 * The one and only app shell. NavDisplay owns every destination (single
 * source of truth); navigation chrome visibility is derived data —
 * [NavigationSuiteType.None] while Splash is current, the adaptive
 * bar/rail/drawer default otherwise. No destination is ever rendered
 * outside NavDisplay, so the spec 0.5 tree never changes shape.
 */
@Composable
private fun AppShell() {
    val backStack = rememberNavBackStack(SplashKey)
    val currentKey: NavKey? = backStack.lastOrNull()

    val layoutType = if (currentKey == SplashKey) {
        NavigationSuiteType.None
    } else {
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
            currentWindowAdaptiveInfoV2(),
        )
    }

    NavigationSuiteScaffold(
        layoutType = layoutType,
        navigationSuiteItems = {
            TOP_LEVEL_DESTINATIONS.forEach { destination ->
                item(
                    selected = currentKey == destination.key,
                    onClick = {
                        if (currentKey != destination.key) {
                            backStack.clear()
                            backStack.add(destination.key)
                        }
                    },
                    icon = {
                        Icon(destination.icon, contentDescription = destination.label)
                    },
                    label = { Text(destination.label) },
                )
            }
        },
    ) {
        val isSplash = currentKey == SplashKey
        val safeAreaModifier = if (isSplash) {
            Modifier
        } else {
            Modifier.windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                ),
            )
        }

        Box(modifier = safeAreaModifier) {
            NotifierHost(enabled = !isSplash) {
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = entryProvider {
                        entry<SplashKey> {
                            SplashScreenEntry(
                                onFinished = {
                                    backStack.clear()
                                    backStack.add(MainKey)
                                },
                            )
                        }
                        entry<MainKey> {
                            MainScreenEntry()
                        }
                        entry<CoinsKey> {
                            CoinsScreen()
                        }
                        entry<SettingsKey> {
                            SettingsScreen()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun SplashScreenEntry(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(SPLASH_HOLD)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_logo),
            contentDescription = "Splash Logo",
        )
    }
}

@Composable
private fun MainScreenEntry() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "Main")
    }
}