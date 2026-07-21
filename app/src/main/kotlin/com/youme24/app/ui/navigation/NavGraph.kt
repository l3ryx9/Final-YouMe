package com.youme24.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.youme24.app.R
import com.youme24.app.ui.auth.ForgotPasswordScreen
import com.youme24.app.ui.auth.AuthViewModel
import com.youme24.app.ui.auth.LoginScreen
import com.youme24.app.ui.auth.RegisterScreen
import com.youme24.app.ui.chat.ChatScreen
import com.youme24.app.ui.conversations.ConversationsScreen
import com.youme24.app.ui.flags.FlagsScreen
import com.youme24.app.ui.analysis.AnalysisScreen
import com.youme24.app.ui.location.LiveLocationScreen
import com.youme24.app.ui.partners.PartnersScreen
import com.youme24.app.ui.search.SearchScreen
import com.youme24.app.ui.settings.SettingsScreen
import com.youme24.app.ui.navigation.Screen.*

// ─────────────────────────────────────────────────────────────
//  Route definitions — équivalent au routeur Expo Router
// ─────────────────────────────────────────────────────────────

sealed class Screen(val route: String) {
    // Auth stack
    object Login         : Screen("login")
    object Register      : Screen("register")
    object ForgotPassword: Screen("forgot-password")

    // App stack — tabs
    object Conversations : Screen("conversations")
    object Partners      : Screen("partners")
    object Search        : Screen("search")
    object Settings      : Screen("settings")

    // App stack — detail
    object Chat          : Screen("chat/{conversationId}") {
        fun createRoute(id: String) = "chat/$id"
    }
    object Flags         : Screen("flags/{partnerId}") {
        fun createRoute(id: String) = "flags/$id"
    }
    object Analysis      : Screen("analysis/{conversationId}") {
        fun createRoute(id: String) = "analysis/$id"
    }
    object LiveLocation  : Screen("live-location/{conversationId}") {
        fun createRoute(id: String) = "live-location/$id"
    }
}

data class BottomTab(
    val screen: Screen,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

@Composable
fun YouMeNavGraph() {
    val rootNav   = rememberNavController()
    val authVm    = hiltViewModel<AuthViewModel>()
    val authState by authVm.uiState.collectAsState()

    val startDest = if (authState.isAuthenticated) Screen.Conversations.route
                    else Screen.Login.route

    NavHost(
        navController = rootNav,
        startDestination = startDest,
        enterTransition = { fadeIn(tween(250)) },
        exitTransition  = { fadeOut(tween(200)) },
    ) {
        // ── Auth stack ──────────────────────────────────────
        composable(Login.route) {
            LoginScreen(
                onLoginSuccess     = { rootNav.navigate(Conversations.route) { popUpTo(0) } },
                onNavigateRegister = { rootNav.navigate(Register.route) },
                onForgotPassword   = { rootNav.navigate(ForgotPassword.route) },
            )
        }
        composable(Register.route) {
            RegisterScreen(
                onRegisterSuccess = { rootNav.navigate(Conversations.route) { popUpTo(0) } },
                onNavigateLogin   = { rootNav.popBackStack() },
            )
        }
        composable(ForgotPassword.route) {
            ForgotPasswordScreen(onBack = { rootNav.popBackStack() })
        }

        // ── App stack with bottom bar ────────────────────────
        composable(Conversations.route) {
            AppScaffold(rootNav, currentRoute = Conversations.route) {
                ConversationsScreen(
                    onOpenChat = { id -> rootNav.navigate(Chat.createRoute(id)) }
                )
            }
        }
        composable(Partners.route) {
            AppScaffold(rootNav, currentRoute = Partners.route) {
                PartnersScreen(
                    onOpenFlags = { id -> rootNav.navigate(Flags.createRoute(id)) }
                )
            }
        }
        composable(Search.route) {
            AppScaffold(rootNav, currentRoute = Search.route) { SearchScreen() }
        }
        composable(Settings.route) {
            AppScaffold(rootNav, currentRoute = Settings.route) {
                SettingsScreen(onLogout = { rootNav.navigate(Login.route) { popUpTo(0) } })
            }
        }

        // ── Detail screens (no bottom bar) ───────────────────
        composable(
            route = Chat.route,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType }),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
            exitTransition  = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: return@composable
            ChatScreen(
                conversationId  = conversationId,
                onBack          = { rootNav.popBackStack() },
                onOpenLocation  = { rootNav.navigate(LiveLocation.createRoute(conversationId)) },
                onOpenAnalysis  = { rootNav.navigate(Analysis.createRoute(conversationId)) },
                onOpenFlags     = { rootNav.navigate(Flags.createRoute(conversationId)) },
            )
        }
        composable(
            route = Flags.route,
            arguments = listOf(navArgument("partnerId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val partnerId = backStackEntry.arguments?.getString("partnerId") ?: return@composable
            FlagsScreen(partnerId = partnerId, onBack = { rootNav.popBackStack() })
        }
        composable(
            route = Analysis.route,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("conversationId") ?: return@composable
            AnalysisScreen(conversationId = id, onBack = { rootNav.popBackStack() })
        }
        composable(
            route = LiveLocation.route,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("conversationId") ?: return@composable
            LiveLocationScreen(conversationId = id, onBack = { rootNav.popBackStack() })
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Bottom navigation scaffold
// ─────────────────────────────────────────────────────────────

@Composable
private fun AppScaffold(
    navController: androidx.navigation.NavHostController,
    currentRoute: String,
    content: @Composable () -> Unit,
) {
    val tabs = listOf(
        BottomTab(Conversations, "Discussions",
            androidx.compose.material.icons.Icons.Outlined.Chat),
        BottomTab(Partners,      "Partenaires",
            androidx.compose.material.icons.Icons.Outlined.Favorite),
        BottomTab(Search,        "Recherche",
            androidx.compose.material.icons.Icons.Outlined.Search),
        BottomTab(Settings,      "Paramètres",
            androidx.compose.material.icons.Icons.Outlined.Settings),
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == tab.screen.route
                    } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick  = {
                            navController.navigate(tab.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        },
                        icon  = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(Modifier.padding(innerPadding)) {
            content()
        }
    }
}
