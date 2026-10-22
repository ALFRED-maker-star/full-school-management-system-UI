package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.EduAdminRepository
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EduAdminViewModel(
    private val repository: EduAdminRepository = EduAdminRepository()
) : ViewModel() {

    val demoAccounts = repository.demoAccounts

    val currentUser: StateFlow<UserAccount?> = repository.currentUser
    val classes: StateFlow<List<ClassRoom>> = repository.classes
    val students: StateFlow<List<Student>> = repository.students
    val teachers: StateFlow<List<Teacher>> = repository.teachers
    val attendanceRecords: StateFlow<Map<String, Map<String, AttendanceStatus>>> = repository.attendanceRecords
    val results: StateFlow<List<ResultRecord>> = repository.results
    val feeRecords: StateFlow<List<FeeRecord>> = repository.feeRecords
    val timetableSlots: StateFlow<List<TimetableSlot>> = repository.timetableSlots
    val announcements: StateFlow<List<Announcement>> = repository.announcements

    private val _currentScreen = MutableStateFlow(Screen.DASHBOARD)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Screen navigation history for BackHandler
    private val screenStack = mutableListOf(Screen.DASHBOARD)

    // Global snackbar / toast message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Active attendance draft state
    private val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private val _attendanceSelectedClassId = MutableStateFlow("cls_10a")
    val attendanceSelectedClassId: StateFlow<String> = _attendanceSelectedClassId.asStateFlow()

    private val _attendanceSelectedDate = MutableStateFlow(todayDateStr)
    val attendanceSelectedDate: StateFlow<String> = _attendanceSelectedDate.asStateFlow()

    private val _draftAttendance = MutableStateFlow<Map<String, AttendanceStatus>>(emptyMap())
    val draftAttendance: StateFlow<Map<String, AttendanceStatus>> = _draftAttendance.asStateFlow()

    init {
        // Initialize draft attendance for default class and date
        refreshDraftAttendance("cls_10a", todayDateStr)
    }

    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenStack.add(screen)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        return if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.lastIndex)
            _currentScreen.value = screenStack.last()
            true
        } else if (_currentScreen.value != Screen.DASHBOARD) {
            _currentScreen.value = Screen.DASHBOARD
            true
        } else {
            false
        }
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearMessage() {
        _snackbarMessage.value = null
    }

    fun loginWithRole(role: Role) {
        repository.loginWithRole(role)
        screenStack.clear()
        screenStack.add(Screen.DASHBOARD)
        _currentScreen.value = Screen.DASHBOARD
        showMessage("Logged in as ${role.displayName}")
    }

    fun loginDirect(user: UserAccount) {
        repository.setCurrentUser(user)
        screenStack.clear()
        screenStack.add(Screen.DASHBOARD)
        _currentScreen.value = Screen.DASHBOARD
        showMessage("Welcome back, ${user.fullName}!")
    }

    fun logout() {
        repository.logout()
        screenStack.clear()
        screenStack.add(Screen.DASHBOARD)
        _currentScreen.value = Screen.DASHBOARD
        showMessage("Logged out successfully")
    }

    // Attendance Methods
    fun setAttendanceClass(classId: String) {
        _attendanceSelectedClassId.value = classId
        refreshDraftAttendance(classId, _attendanceSelectedDate.value)
    }

    fun setAttendanceDate(date: String) {
        _attendanceSelectedDate.value = date
        refreshDraftAttendance(_attendanceSelectedClassId.value, date)
    }

    private fun refreshDraftAttendance(classId: String, date: String) {
        val key = "$classId-$date"
        val existing = attendanceRecords.value[key]
        if (existing != null) {
            _draftAttendance.value = existing
        } else {
            // Default all students in this class to PRESENT
            val studentsInClass = students.value.filter { it.gradeClassId == classId }
            val map = studentsInClass.associate { it.id to AttendanceStatus.PRESENT }
            _draftAttendance.value = map
        }
    }

    fun setStudentAttendanceStatus(studentId: String, status: AttendanceStatus) {
        val map = _draftAttendance.value.toMutableMap()
        map[studentId] = status
        _draftAttendance.value = map
    }

    fun markAllPresent(studentIds: List<String>) {
        val map = _draftAttendance.value.toMutableMap()
        studentIds.forEach { map[it] = AttendanceStatus.PRESENT }
        _draftAttendance.value = map
        showMessage("Marked all students as Present")
    }

    fun saveCurrentAttendance() {
        val classId = _attendanceSelectedClassId.value
        val date = _attendanceSelectedDate.value
        repository.saveAttendance(classId, date, _draftAttendance.value)
        showMessage("Attendance recorded for $date")
    }

    // Class Management
    fun addClass(name: String, gradeLevel: String, section: String, teacherName: String, roomNumber: String, capacity: Int, subjects: List<String>) {
        repository.addClass(name, gradeLevel, section, teacherName, roomNumber, capacity, subjects)
        showMessage("Class $name created successfully")
    }

    // Student Management
    fun addStudent(fullName: String, rollNumber: String, classId: String, guardianName: String, guardianPhone: String, email: String) {
        repository.addStudent(fullName, rollNumber, classId, guardianName, guardianPhone, email)
        showMessage("Student $fullName enrolled successfully")
    }

    fun updateStudent(updated: Student) {
        repository.updateStudent(updated)
        showMessage("Student ${updated.fullName} updated")
    }

    // Results / Scores
    fun submitBatchResults(classId: String, subject: String, examType: String, maxScore: Double, scores: Map<String, Double>, remarks: Map<String, String>) {
        repository.saveBatchResults(classId, subject, examType, maxScore, scores, remarks)
        showMessage("Grades published for $subject ($examType)")
    }

    // Fees
    fun recordFeePayment(feeRecordId: String, amount: Double, paymentMethod: String, referenceNo: String) {
        repository.recordFeePayment(feeRecordId, amount, paymentMethod, referenceNo)
        showMessage("Payment of $$amount recorded successfully")
    }

    // Timetable
    fun addTimetableSlot(slot: TimetableSlot) {
        repository.addTimetableSlot(slot)
        showMessage("Added timetable slot for ${slot.subject}")
    }

    fun removeTimetableSlot(slotId: String) {
        repository.removeTimetableSlot(slotId)
        showMessage("Timetable slot removed")
    }

    // Announcements
    fun postAnnouncement(title: String, content: String, authorName: String, authorRole: String, category: String, targetRole: String, isPinned: Boolean) {
        repository.addAnnouncement(title, content, authorName, authorRole, category, targetRole, isPinned)
        showMessage("Announcement posted to $targetRole")
    }

    fun toggleAnnouncementRead(announcementId: String, userId: String) {
        repository.toggleAnnouncementRead(announcementId, userId)
    }
}
