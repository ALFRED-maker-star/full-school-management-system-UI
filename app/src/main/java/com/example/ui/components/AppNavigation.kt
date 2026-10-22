package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Role
import com.example.model.Screen
import com.example.model.UserAccount
import com.example.ui.theme.*

fun getScreensForRole(role: Role): List<Screen> {
    return when (role) {
        Role.ADMIN -> listOf(
            Screen.DASHBOARD,
            Screen.CLASSES,
            Screen.STUDENTS,
            Screen.ATTENDANCE,
            Screen.RESULTS,
            Screen.FEES,
            Screen.TIMETABLE,
            Screen.ANNOUNCEMENTS
        )
        Role.TEACHER -> listOf(
            Screen.DASHBOARD,
            Screen.ATTENDANCE,
            Screen.RESULTS,
            Screen.CLASSES,
            Screen.TIMETABLE,
            Screen.ANNOUNCEMENTS
        )
        Role.STUDENT_PARENT -> listOf(
            Screen.DASHBOARD,
            Screen.TIMETABLE,
            Screen.RESULTS,
            Screen.ATTENDANCE,
            Screen.FEES,
            Screen.ANNOUNCEMENTS
        )
    }
}

fun getScreenIcon(screen: Screen): ImageVector {
    return when (screen) {
        Screen.DASHBOARD -> Icons.Default.Dashboard
        Screen.CLASSES -> Icons.Default.MeetingRoom
        Screen.STUDENTS -> Icons.Default.People
        Screen.ATTENDANCE -> Icons.Default.EventAvailable
        Screen.RESULTS -> Icons.Default.Grade
        Screen.FEES -> Icons.Default.AccountBalanceWallet
        Screen.TIMETABLE -> Icons.Default.CalendarMonth
        Screen.ANNOUNCEMENTS -> Icons.Default.Campaign
    }
}

@Composable
fun DesktopSidebar(
    user: UserAccount,
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    onSwitchRole: (Role) -> Unit,
    onLogout: () -> Unit,
    unreadAnnouncementsCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val navItems = getScreensForRole(user.role)
    var showRoleMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(260.dp),
        color = Navy800,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            // Academy Logo & Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Teal500),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "EduAdmin Logo",
                        tint = Navy900,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "EduAdmin",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "School Management",
                        style = MaterialTheme.typography.labelSmall,
                        color = Teal100.copy(alpha = 0.8f)
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(16.dp))

            // User Profile Card & Role Switcher
            Surface(
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showRoleMenu = true }
                    .testTag("sidebar_role_switch")
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EduAvatar(
                        initials = user.avatarInitial,
                        backgroundColor = Teal600,
                        textColor = Color.White,
                        size = 36
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.fullName,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Amber500)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = user.role.displayName,
                                color = Amber100,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.UnfoldMore,
                        contentDescription = "Switch Role",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showRoleMenu,
                    onDismissRequest = { showRoleMenu = false }
                ) {
                    Text(
                        text = "Switch Active Role",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                    Role.values().forEach { role ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = role.displayName,
                                        fontWeight = if (user.role == role) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (user.role == role) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Teal600,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                showRoleMenu = false
                                onSwitchRole(role)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "NAVIGATION",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            // Nav items list
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                navItems.forEach { screen ->
                    val isSelected = currentScreen == screen
                    val icon = getScreenIcon(screen)

                    Surface(
                        color = if (isSelected) Teal600 else Color.Transparent,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable { onNavigate(screen) }
                            .testTag("nav_item_${screen.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = screen.title,
                                tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.75f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = screen.title,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            if (screen == Screen.ANNOUNCEMENTS && unreadAnnouncementsCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Amber500),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$unreadAnnouncementsCount",
                                        color = Navy900,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Logout & System Info
            HorizontalDivider(color = Color.White.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogout() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("logout_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Logout",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Sign Out",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduTopBar(
    title: String,
    user: UserAccount?,
    onOpenDrawer: (() -> Unit)?,
    onSwitchRole: ((Role) -> Unit)?,
    onLogout: (() -> Unit)?,
    unreadCount: Int = 0,
    onNotificationsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier.testTag("app_top_bar"),
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                if (user != null) {
                    Text(
                        text = "${user.role.displayName} Portal",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                }
            }
        },
        navigationIcon = {
            if (onOpenDrawer != null) {
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier.testTag("menu_drawer_toggle")
                ) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Navy800)
                }
            }
        },
        actions = {
            if (user != null) {
                // Role badge pill
                Surface(
                    color = Teal100,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .clickable { showMenu = true }
                        .padding(end = 8.dp)
                        .testTag("topbar_role_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Teal700)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = user.role.displayName,
                            color = Teal700,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = Teal700,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    Text(
                        text = "Logged in as ${user.fullName}",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                    HorizontalDivider()
                    Role.values().forEach { role ->
                        DropdownMenuItem(
                            text = { Text("Switch to ${role.displayName}") },
                            onClick = {
                                showMenu = false
                                onSwitchRole?.invoke(role)
                            }
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Sign Out", color = DangerRed) },
                        onClick = {
                            showMenu = false
                            onLogout?.invoke()
                        }
                    )
                }

                // Notification Bell
                IconButton(
                    onClick = { onNotificationsClick?.invoke() },
                    modifier = Modifier.testTag("notifications_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge(
                                    containerColor = Amber500,
                                    contentColor = Navy900
                                ) {
                                    Text("$unreadCount")
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Navy800
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}
