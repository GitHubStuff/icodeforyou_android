// MainActivity.kt
// CatsCarsCoins — spec 24.3.23. Complete file.
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
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.icodeforyou.catscarscoins.cats.ui.CatsScreen
import com.icodeforyou.catscarscoins.coins.ui.CoinsScreen
import com.icodeforyou.catscarscoins.nav.AppNavKey
import com.icodeforyou.catscarscoins.nav.CatsKey
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
    TopLevelDestination(key = CatsKey, label = "Cats", icon = Icons.Default.Pets),
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
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppShell()
                }
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                // The background MUST be painted before the windowInsetsPadding
                .background(MaterialTheme.colorScheme.background)
                .then(safeAreaModifier)
        ) {
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
                        entry<CatsKey> {
                            CatsScreen()
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
        contentAlignment = BiasAlignment(horizontalBias = 0f, verticalBias = -0.25f),
    ) {
        Image(
            painter = painterResource(id = R.drawable.hplogo),
            contentDescription = "Main Logo"
        )
    }
}