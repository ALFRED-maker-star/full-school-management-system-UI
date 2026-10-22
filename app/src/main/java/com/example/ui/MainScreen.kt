package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Role
import com.example.model.Screen
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    viewModel: EduAdminViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val teachers by viewModel.teachers.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val feeRecords by viewModel.feeRecords.collectAsStateWithLifecycle()
    val timetableSlots by viewModel.timetableSlots.collectAsStateWithLifecycle()
    val announcements by viewModel.announcements.collectAsStateWithLifecycle()

    val attendanceClassId by viewModel.attendanceSelectedClassId.collectAsStateWithLifecycle()
    val attendanceDate by viewModel.attendanceSelectedDate.collectAsStateWithLifecycle()
    val draftAttendance by viewModel.draftAttendance.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Handle snackbar notifications
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    // Handle system back navigation
    BackHandler(enabled = currentUser != null && currentScreen != Screen.DASHBOARD) {
        viewModel.navigateBack()
    }

    // Unread count for announcements
    val unreadCount = remember(announcements, currentUser) {
        val user = currentUser
        if (user == null) 0 else announcements.count { !it.readByUsers.contains(user.id) }
    }

    // If no user is logged in, show LoginScreen
    if (currentUser == null) {
        LoginScreen(
            demoAccounts = viewModel.demoAccounts,
            onLoginSuccess = { role -> viewModel.loginWithRole(role) },
            modifier = modifier
        )
        return
    }

    val user = currentUser!!

    // Responsive Desktop-first Layout:
    // If width >= 720dp: Left persistent DesktopSidebar + Content
    // If width < 720dp: ModalNavigationDrawer with top app bar + Bottom Navigation Bar
    BoxWithConstraints(modifier = modifier.fillMaxSize().background(Slate50)) {
        val isDesktopOrTablet = maxWidth >= 720.dp

        if (isDesktopOrTablet) {
            // Desktop-First Persistent Sidebar Layout
            Row(modifier = Modifier.fillMaxSize()) {
                DesktopSidebar(
                    user = user,
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) },
                    onSwitchRole = { viewModel.loginWithRole(it) },
                    onLogout = { viewModel.logout() },
                    unreadAnnouncementsCount = unreadCount,
                    modifier = Modifier.testTag("desktop_sidebar")
                )

                // Main Content Workspace
                Scaffold(
                    modifier = Modifier.weight(1f),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        EduTopBar(
                            title = currentScreen.title,
                            user = user,
                            onOpenDrawer = null, // Sidebar is already visible
                            onSwitchRole = { viewModel.loginWithRole(it) },
                            onLogout = { viewModel.logout() },
                            unreadCount = unreadCount,
                            onNotificationsClick = { viewModel.navigateTo(Screen.ANNOUNCEMENTS) }
                        )
                    },
                    containerColor = Slate50
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        ScreenRouter(
                            currentScreen = currentScreen,
                            user = user,
                            viewModel = viewModel,
                            classes = classes,
                            students = students,
                            teachers = teachers,
                            results = results,
                            feeRecords = feeRecords,
                            timetableSlots = timetableSlots,
                            announcements = announcements,
                            attendanceClassId = attendanceClassId,
                            attendanceDate = attendanceDate,
                            draftAttendance = draftAttendance
                        )
                    }
                }
            }
        } else {
            // Mobile / Narrow Screen: Drawer + TopAppBar + Bottom Navigation Bar
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = Navy800,
                        modifier = Modifier.width(280.dp)
                    ) {
                        DesktopSidebar(
                            user = user,
                            currentScreen = currentScreen,
                            onNavigate = { screen ->
                                viewModel.navigateTo(screen)
                                coroutineScope.launch { drawerState.close() }
                            },
                            onSwitchRole = { role ->
                                viewModel.loginWithRole(role)
                                coroutineScope.launch { drawerState.close() }
                            },
                            onLogout = {
                                viewModel.logout()
                                coroutineScope.launch { drawerState.close() }
                            },
                            unreadAnnouncementsCount = unreadCount
                        )
                    }
                }
            ) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        EduTopBar(
                            title = currentScreen.title,
                            user = user,
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                            onSwitchRole = { viewModel.loginWithRole(it) },
                            onLogout = { viewModel.logout() },
                            unreadCount = unreadCount,
                            onNotificationsClick = { viewModel.navigateTo(Screen.ANNOUNCEMENTS) }
                        )
                    },
                    bottomBar = {
                        val bottomScreens = getScreensForRole(user.role).take(4)
                        NavigationBar(
                            containerColor = Color.White,
                            tonalElevation = 6.dp,
                            windowInsets = WindowInsets.navigationBars,
                            modifier = Modifier.testTag("mobile_bottom_bar")
                        ) {
                            bottomScreens.forEach { screen ->
                                val isSelected = currentScreen == screen
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { viewModel.navigateTo(screen) },
                                    icon = {
                                        Icon(
                                            imageVector = getScreenIcon(screen),
                                            contentDescription = screen.title
                                        )
                                    },
                                    label = { Text(screen.title, fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Navy800,
                                        selectedTextColor = Navy800,
                                        indicatorColor = Navy100
                                    )
                                )
                            }
                        }
                    },
                    containerColor = Slate50
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        ScreenRouter(
                            currentScreen = currentScreen,
                            user = user,
                            viewModel = viewModel,
                            classes = classes,
                            students = students,
                            teachers = teachers,
                            results = results,
                            feeRecords = feeRecords,
                            timetableSlots = timetableSlots,
                            announcements = announcements,
                            attendanceClassId = attendanceClassId,
                            attendanceDate = attendanceDate,
                            draftAttendance = draftAttendance
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenRouter(
    currentScreen: Screen,
    user: com.example.model.UserAccount,
    viewModel: EduAdminViewModel,
    classes: List<com.example.model.ClassRoom>,
    students: List<com.example.model.Student>,
    teachers: List<com.example.model.Teacher>,
    results: List<com.example.model.ResultRecord>,
    feeRecords: List<com.example.model.FeeRecord>,
    timetableSlots: List<com.example.model.TimetableSlot>,
    announcements: List<com.example.model.Announcement>,
    attendanceClassId: String,
    attendanceDate: String,
    draftAttendance: Map<String, com.example.model.AttendanceStatus>
) {
    when (currentScreen) {
        Screen.DASHBOARD -> {
            when (user.role) {
                Role.ADMIN -> AdminDashboardScreen(
                    classes = classes,
                    students = students,
                    teachers = teachers,
                    feeRecords = feeRecords,
                    announcements = announcements,
                    onNavigate = { viewModel.navigateTo(it) },
                    onPostAnnouncement = { t, c, a, r, cat, tr, pin ->
                        viewModel.postAnnouncement(t, c, a, r, cat, tr, pin)
                    }
                )
                Role.TEACHER -> TeacherDashboardScreen(
                    user = user,
                    timetableSlots = timetableSlots,
                    announcements = announcements,
                    classes = classes,
                    onNavigate = { viewModel.navigateTo(it) },
                    onSelectAttendanceClass = { classId ->
                        viewModel.setAttendanceClass(classId)
                    }
                )
                Role.STUDENT_PARENT -> StudentDashboardScreen(
                    user = user,
                    students = students,
                    results = results,
                    feeRecords = feeRecords,
                    timetableSlots = timetableSlots,
                    announcements = announcements,
                    onNavigate = { viewModel.navigateTo(it) },
                    onPayFee = { feeId, amount, method ->
                        viewModel.recordFeePayment(feeId, amount, method, "WEB-PAY")
                    }
                )
            }
        }
        Screen.CLASSES -> ClassesScreen(
            user = user,
            classes = classes,
            onAddClass = { name, grade, sec, teacher, room, cap, subj ->
                viewModel.addClass(name, grade, sec, teacher, room, cap, subj)
            },
            onNavigate = { viewModel.navigateTo(it) },
            onSelectClassForAttendance = { classId ->
                viewModel.setAttendanceClass(classId)
            }
        )
        Screen.STUDENTS -> StudentsScreen(
            user = user,
            students = students,
            classes = classes,
            onAddStudent = { n, r, c, g, p, e ->
                viewModel.addStudent(n, r, c, g, p, e)
            },
            onUpdateStudent = { updated ->
                viewModel.updateStudent(updated)
            }
        )
        Screen.ATTENDANCE -> AttendanceScreen(
            user = user,
            classes = classes,
            students = students,
            selectedClassId = attendanceClassId,
            selectedDate = attendanceDate,
            draftAttendance = draftAttendance,
            onSelectClass = { viewModel.setAttendanceClass(it) },
            onSelectDate = { viewModel.setAttendanceDate(it) },
            onSetStatus = { stuId, st -> viewModel.setStudentAttendanceStatus(stuId, st) },
            onMarkAllPresent = { ids -> viewModel.markAllPresent(ids) },
            onSaveAttendance = { viewModel.saveCurrentAttendance() }
        )
        Screen.RESULTS -> ResultsScreen(
            user = user,
            classes = classes,
            students = students,
            results = results,
            onSubmitBatchResults = { cId, subj, exam, max, scores, remarks ->
                viewModel.submitBatchResults(cId, subj, exam, max, scores, remarks)
            },
            onShowMessage = { viewModel.showMessage(it) }
        )
        Screen.FEES -> FeesScreen(
            user = user,
            students = students,
            feeRecords = feeRecords,
            onRecordPayment = { feeId, amt, method, ref ->
                viewModel.recordFeePayment(feeId, amt, method, ref)
            }
        )
        Screen.TIMETABLE -> TimetableScreen(
            user = user,
            classes = classes,
            teachers = teachers,
            timetableSlots = timetableSlots,
            onAddSlot = { slot -> viewModel.addTimetableSlot(slot) },
            onRemoveSlot = { slotId -> viewModel.removeTimetableSlot(slotId) }
        )
        Screen.ANNOUNCEMENTS -> AnnouncementsScreen(
            user = user,
            announcements = announcements,
            onPostAnnouncement = { t, c, a, r, cat, tr, pin ->
                viewModel.postAnnouncement(t, c, a, r, cat, tr, pin)
            },
            onToggleRead = { ancId, uId ->
                viewModel.toggleAnnouncementRead(ancId, uId)
            }
        )
    }
}
