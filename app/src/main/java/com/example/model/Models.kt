package com.example.model

enum class Role(val displayName: String, val badgeColor: Long) {
    ADMIN("Administrator", 0xFF1E3A8A),
    TEACHER("Faculty / Teacher", 0xFF0D9488),
    STUDENT_PARENT("Student & Parent", 0xFFF59E0B)
}

enum class Screen(val title: String, val iconName: String) {
    DASHBOARD("Dashboard", "dashboard"),
    CLASSES("Classes", "class"),
    STUDENTS("Students", "people"),
    ATTENDANCE("Attendance", "event_available"),
    RESULTS("Results", "grade"),
    FEES("Fees & Finance", "receipt_long"),
    TIMETABLE("Timetable", "calendar_today"),
    ANNOUNCEMENTS("Announcements", "campaign")
}

data class UserAccount(
    val id: String,
    val fullName: String,
    val role: Role,
    val email: String,
    val title: String,
    val avatarInitial: String,
    val assignedClassIds: List<String> = emptyList(),
    val studentId: String? = null
)

data class ClassRoom(
    val id: String,
    val name: String,
    val gradeLevel: String,
    val section: String,
    val classTeacherId: String,
    val classTeacherName: String,
    val roomNumber: String,
    val studentCount: Int,
    val capacity: Int,
    val subjects: List<String>
)

data class Student(
    val id: String,
    val rollNumber: String,
    val fullName: String,
    val gradeClassId: String,
    val gradeClassName: String,
    val guardianName: String,
    val guardianPhone: String,
    val email: String,
    val status: String = "Active", // Active, Leave, Suspended
    val attendancePercentage: Float = 94.5f,
    val feeBalance: Double = 0.0
)

data class Teacher(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val subjects: List<String>,
    val assignedClasses: List<String>,
    val roomNumber: String
)

enum class AttendanceStatus(val label: String, val color: Long) {
    PRESENT("Present", 0xFF10B981),
    ABSENT("Absent", 0xFFEF4444),
    LATE("Late", 0xFFF59E0B),
    EXCUSED("Excused", 0xFF3B82F6)
}

data class AttendanceEntry(
    val studentId: String,
    val studentName: String,
    val rollNumber: String,
    val status: AttendanceStatus
)

data class ResultRecord(
    val id: String,
    val studentId: String,
    val studentName: String,
    val classId: String,
    val className: String,
    val subject: String,
    val examType: String, // Midterm, Final, Unit Quiz
    val score: Double,
    val maxScore: Double = 100.0,
    val gradeLetter: String,
    val remarks: String,
    val term: String = "Term 1 (2026)"
)

data class FeeReceipt(
    val receiptNo: String,
    val date: String,
    val amount: Double,
    val paymentMethod: String,
    val referenceNo: String
)

data class FeeRecord(
    val id: String,
    val studentId: String,
    val studentName: String,
    val classId: String,
    val className: String,
    val title: String,
    val totalAmount: Double,
    val paidAmount: Double,
    val dueDate: String,
    val status: String, // "Paid", "Pending", "Overdue"
    val receipts: List<FeeReceipt> = emptyList()
)

data class TimetableSlot(
    val id: String,
    val dayOfWeek: String, // Monday, Tuesday, Wednesday, Thursday, Friday
    val periodNumber: Int,
    val timeRange: String,
    val classId: String,
    val className: String,
    val subject: String,
    val teacherName: String,
    val roomNumber: String
)

data class Announcement(
    val id: String,
    val title: String,
    val content: String,
    val authorName: String,
    val authorRole: String,
    val category: String, // Academic, Urgent, Event, Fee Notice
    val targetRole: String, // "ALL", "ADMIN", "TEACHER", "STUDENT_PARENT"
    val timestamp: String,
    val isPinned: Boolean = false,
    val readByUsers: Set<String> = emptySet()
)
