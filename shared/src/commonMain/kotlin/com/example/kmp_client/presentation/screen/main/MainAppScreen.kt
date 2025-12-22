package com.example.kmp_client.presentation.screen.main


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color.Companion.Transparent
import com.example.kmp_client.presentation.navigation.NavRoutes
import com.example.kmp_client.presentation.screen.accessscheme.AccessSchemeScreen
import com.example.kmp_client.presentation.screen.loading.LoadingScreen
import com.example.kmp_client.presentation.screen.auth.AuthViewModel
import com.example.kmp_client.presentation.screen.devices.DevicesScreen
import com.example.kmp_client.presentation.screen.eventLog.EventLogScreen
import com.example.kmp_client.presentation.screen.key.KeysScreen
import com.example.kmp_client.presentation.screen.users.UsersScreen
import com.example.kmp_client.presentation.screen.users.host.ExpectedUserDetailScreenHost
import com.example.kmp_client.core.getAppVersion
import com.example.kmp_client.presentation.ui.component.MenuItem
import com.example.kmp_client.presentation.ui.component.getMenuItems
import kmp_eraclient.shared.generated.resources.Res
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.PopUpTo
import moe.tlaster.precompose.navigation.rememberNavigator
import org.jetbrains.compose.resources.painterResource
import com.example.kmp_client.presentation.ui.theme.DarkBackground
import com.example.kmp_client.presentation.ui.theme.LightBackground
import com.example.kmp_client.presentation.ui.theme.LightPrimary
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kmp_eraclient.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    appNavigator: Navigator,
    authViewModel: AuthViewModel,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val authUIState by authViewModel.uiState.collectAsState()
    val internalNavigator = rememberNavigator()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentSelectedRoute by rememberSaveable { mutableStateOf(NavRoutes.MENU_APP_CONFIG_DEV) }
    var expandedSection by rememberSaveable { mutableStateOf("Конфигурация") }
    var showAboutDialog by remember { mutableStateOf(false) }

    val getUpdatedMenuItems = remember {
        { route: String, expanded: String, onAboutClick: () -> Unit ->
            getMenuItems(onAboutClick).map { menuItem ->
                if (menuItem is MenuItem.Parent) {
                    menuItem.copy(isExpanded = menuItem.parentTitle == expanded)
                } else menuItem
            }
        }
    }

    val getSectionForRoute = remember {
        { route: String ->
            when (route) {
                NavRoutes.MENU_APP_CONFIG_DEV, NavRoutes.MENU_APP_CONFIG_SHEM -> "Конфигурация"
                NavRoutes.MENU_APP_BUREAU_USR, NavRoutes.MENU_APP_BUREAU_KEYS -> "Бюро Пропусков"
                NavRoutes.MENU_APP_EVENTS_EVENT_LOG -> "События"
                else -> "Конфигурация"
            }
        }
    }

    val menuItems = getUpdatedMenuItems(currentSelectedRoute, expandedSection, { showAboutDialog = true })
    val currentBackStackEntry by internalNavigator.currentEntry.collectAsState(null)

    LaunchedEffect(internalNavigator.currentEntry) {
        currentBackStackEntry?.route?.route?.let { route ->
            if (NavRoutes.isMainScreenRoute(route) && currentSelectedRoute != route) {
                currentSelectedRoute = route
                expandedSection = getSectionForRoute(route)
            }
        }
    }

    LaunchedEffect(authUIState.isAuthenticated) {
        if (!authUIState.isAuthenticated && drawerState.isOpen) {
            drawerState.close()
        }
    }

    if (!authUIState.isAuthenticated) {
        LoadingScreen(message = "Загрузка")
        return
    }

    val drawerWidth = 320.dp

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.width(drawerWidth)) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 12.dp)
                    ) {
                        itemsIndexed(menuItems.filter { it !is MenuItem.Divider && it !is MenuItem.ActionItem }) { index, item ->
                            when (item) {
                                is MenuItem.Parent -> {
                                    ParentMenuItemContent(
                                        item = item,
                                        currentSelectedRoute = currentSelectedRoute,
                                        isSelected = item.children.any { it.childRoute == currentSelectedRoute },
                                        onParentClick = {
                                            expandedSection =
                                                if (expandedSection == item.parentTitle) {
                                                    ""
                                                } else {
                                                    item.parentTitle
                                                }
                                        },
                                        onChildClick = { childRoute ->
                                            currentSelectedRoute = childRoute
                                            expandedSection = getSectionForRoute(childRoute)
                                            internalNavigator.navigate(
                                                childRoute,
                                                NavOptions(
                                                    popUpTo = PopUpTo.First(inclusive = true),
                                                    launchSingleTop = true
                                                )
                                            )
                                            scope.launch { drawerState.close() }
                                        }
                                    )
                                }

                                else -> {}
                            }
                        }
                    }

                    val actionItems = menuItems.filterIsInstance<MenuItem.ActionItem>()
                    if (actionItems.isNotEmpty()) {
                        menuItems.find { it is MenuItem.Divider }?.let {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }

                        actionItems.forEach { item ->
                            NavigationDrawerItem(
                                icon = {
                                    item.actionIcon?.let {
                                        Icon(
                                            it,
                                            contentDescription = item.actionTitle
                                        )
                                    }
                                },
                                label = { Text(item.actionTitle) },
                                selected = false,
                                onClick = {
                                    scope.launch {
                                        drawerState.close()
                                        item.onClickAction()
                                    }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(Res.drawable.background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Scaffold(
                containerColor = Transparent,
                snackbarHost = {
                    SnackbarHost(
                        hostState = snackbarHostState
                    ) { snackbarData ->
                        Snackbar(
                            snackbarData = snackbarData,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            actionColor = MaterialTheme.colorScheme.onPrimary,
                            dismissActionContentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                },
                topBar = {
                    TopAppBar(
                        title = { Text(getTitleForRoute(currentSelectedRoute)) },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = Res.string.menu.toString()
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = if (MaterialTheme.colorScheme.primary == LightPrimary) LightBackground else DarkBackground
                        )
                    )
                }
            ) { paddingValues ->
                NavHost(
                    navigator = internalNavigator,
                    initialRoute = NavRoutes.MENU_APP_CONFIG_DEV,
                    modifier = Modifier.padding(paddingValues)
                ) {
                    scene(NavRoutes.MENU_APP_CONFIG_DEV) {
                        DevicesScreen(snackbarHostState = snackbarHostState)
                    }
                    scene(NavRoutes.MENU_APP_CONFIG_SHEM) {
                        AccessSchemeScreen(snackbarHostState = snackbarHostState)
                    }
                    scene(NavRoutes.MENU_APP_BUREAU_USR) {
                        UsersScreen(
                            snackbarHostState = snackbarHostState,
                            onUserClick = { userId ->
                                internalNavigator.navigate(NavRoutes.userDetailScreen(userId))
                            }
                        )
                    }
                    scene(
                        route = NavRoutes.USER_DETAIL
                    ) { backStackEntry ->
                        val userIdString = backStackEntry.pathMap[NavRoutes.USER_ID_ARG]
                        val userId = userIdString?.toLongOrNull()

                        if (userId != null) {
                            ExpectedUserDetailScreenHost(
                                userId = userId,
                                navController = internalNavigator,
                                snackbarHostState = snackbarHostState
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                snackbarHostState.showSnackbar("Ошибка: ID пользователя не найден.")
                                internalNavigator.popBackStack()
                            }
                        }
                    }
                    scene(NavRoutes.MENU_APP_BUREAU_KEYS) {
                        KeysScreen(snackbarHostState = snackbarHostState)
                    }
                    scene(NavRoutes.MENU_APP_EVENTS_EVENT_LOG) {
                        EventLogScreen(snackbarHostState = snackbarHostState)
                    }
                }
            }
        }
    
        if (showAboutDialog) {
            val version = getAppVersion()
            val year = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .year
            val message = stringResource(Res.string.version_info)
    
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text(stringResource(Res.string.app_title_main)) },
                text = { Text(message) },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        stringResource(Res.string.ok)
                    }
                }
            )
        }
    }
}

@Composable
private fun ParentMenuItemContent(
    item: MenuItem.Parent,
    currentSelectedRoute: String,
    isSelected: Boolean,
    onParentClick: () -> Unit,
    onChildClick: (route: String) -> Unit,
) {
    val arrowRotationDegree by animateFloatAsState(
        targetValue = if (item.isExpanded) 100f else 0f,
        label = "arrowRotation"
    )

    Column {
        NavigationDrawerItem(
            icon = { item.icon?.let { Icon(it, contentDescription = item.title) } },
            label = { Text(item.title) },
            selected = isSelected && !item.isExpanded,
            onClick = onParentClick,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            badge = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = if (item.isExpanded) stringResource(Res.string.collapse) else stringResource(Res.string.expand),
                    modifier = Modifier.rotate(arrowRotationDegree)
                )
            }
        )

        AnimatedVisibility(visible = item.isExpanded) {
            Column(modifier = Modifier.padding(start = 16.dp)) {
                item.children.forEach { child ->
                    NavigationDrawerItem(
                        icon = { child.icon?.let { Icon(it, contentDescription = child.title) } },
                        label = { Text(child.title) },
                        selected = child.route == currentSelectedRoute,
                        onClick = { onChildClick(child.childRoute) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    }
}

fun getTitleForRoute(route: String): String {
    return when (route) {
        NavRoutes.MENU_APP_CONFIG_DEV -> Res.string.devices
        NavRoutes.MENU_APP_CONFIG_SHEM -> Res.string.access_schemes
        NavRoutes.MENU_APP_BUREAU_USR -> Res.string.users
        NavRoutes.MENU_APP_BUREAU_KEYS -> Res.string.keys
        NavRoutes.MENU_APP_EVENTS_EVENT_LOG -> Res.string.event_log
        else -> Res.string.app_name
    }.toString()
}