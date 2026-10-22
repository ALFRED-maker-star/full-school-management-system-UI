package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class EduAdminRepository {

    // Default test accounts for instant role-based exploration
    val demoAccounts = listOf(
        UserAccount(
            id = "user_admin",
            fullName = "Dr. Eleanor Vance",
            role = Role.ADMIN,
            email = "admin@eduadmin.academy",
            title = "Principal & Superintendent",
            avatarInitial = "EV"
        ),
        UserAccount(
            id = "user_teacher",
            fullName = "Prof. Marcus Reed",
            role = Role.TEACHER,
            email = "m.reed@eduadmin.academy",
            title = "Senior STEM Faculty & Class Teacher",
            avatarInitial = "MR",
            assignedClassIds = listOf("cls_10a", "cls_10b")
        ),
        UserAccount(
            id = "user_parent",
            fullName = "Sarah Jenkins",
            role = Role.STUDENT_PARENT,
            email = "sarah.jenkins@mail.com",
            title = "Parent of Liam Jenkins (Grade 10-A)",
            avatarInitial = "SJ",
            studentId = "stu_10a_01"
        )
    )

    private val _currentUser = MutableStateFlow<UserAccount?>(demoAccounts[0])
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    private val _classes = MutableStateFlow<List<ClassRoom>>(initialClasses())
    val classes: StateFlow<List<ClassRoom>> = _classes.asStateFlow()

    private val _students = MutableStateFlow<List<Student>>(initialStudents())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _teachers = MutableStateFlow<List<Teacher>>(initialTeachers())
    val teachers: StateFlow<List<Teacher>> = _teachers.asStateFlow()

    // Keyed by "$classId-$date" -> Map<studentId, AttendanceStatus>
    private val _attendanceRecords = MutableStateFlow<Map<String, Map<String, AttendanceStatus>>>(initialAttendance())
    val attendanceRecords: StateFlow<Map<String, Map<String, AttendanceStatus>>> = _attendanceRecords.asStateFlow()

    private val _results = MutableStateFlow<List<ResultRecord>>(initialResults())
    val results: StateFlow<List<ResultRecord>> = _results.asStateFlow()

    private val _feeRecords = MutableStateFlow<List<FeeRecord>>(initialFeeRecords())
    val feeRecords: StateFlow<List<FeeRecord>> = _feeRecords.asStateFlow()

    private val _timetableSlots = MutableStateFlow<List<TimetableSlot>>(initialTimetable())
    val timetableSlots: StateFlow<List<TimetableSlot>> = _timetableSlots.asStateFlow()

    private val _announcements = MutableStateFlow<List<Announcement>>(initialAnnouncements())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    fun setCurrentUser(user: UserAccount?) {
        _currentUser.value = user
    }

    fun loginWithRole(role: Role) {
        val user = demoAccounts.find { it.role == role } ?: demoAccounts[0]
        _currentUser.value = user
    }

    fun logout() {
        _currentUser.value = null
    }

    // Class Management
    fun addClass(name: String, gradeLevel: String, section: String, teacherName: String, roomNumber: String, capacity: Int, subjects: List<String>) {
        val newClass = ClassRoom(
            id = "cls_${UUID.randomUUID().toString().take(6)}",
            name = name,
            gradeLevel = gradeLevel,
            section = section,
            classTeacherId = "tch_custom",
            classTeacherName = teacherName,
            roomNumber = roomNumber,
            studentCount = 0,
            capacity = capacity,
            subjects = subjects
        )
        _classes.value = _classes.value + newClass
    }

    fun updateClass(updated: ClassRoom) {
        _classes.value = _classes.value.map { if (it.id == updated.id) updated else it }
    }

    // Student Management
    fun addStudent(fullName: String, rollNumber: String, classId: String, guardianName: String, guardianPhone: String, email: String) {
        val classObj = _classes.value.find { it.id == classId }
        val newStudent = Student(
            id = "stu_${UUID.randomUUID().toString().take(6)}",
            rollNumber = rollNumber,
            fullName = fullName,
            gradeClassId = classId,
            gradeClassName = classObj?.name ?: "Grade 10-A",
            guardianName = guardianName,
            guardianPhone = guardianPhone,
            email = email,
            status = "Active",
            attendancePercentage = 100f,
            feeBalance = 450.0
        )
        _students.value = _students.value + newStudent
        // Increment class count
        classObj?.let { cls ->
            updateClass(cls.copy(studentCount = cls.studentCount + 1))
        }
    }

    fun updateStudent(updated: Student) {
        _students.value = _students.value.map { if (it.id == updated.id) updated else it }
    }

    // Attendance Management
    fun saveAttendance(classId: String, date: String, attendanceMap: Map<String, AttendanceStatus>) {
        val key = "$classId-$date"
        val current = _attendanceRecords.value.toMutableMap()
        current[key] = attendanceMap
        _attendanceRecords.value = current
    }

    // Results Management
    fun addResultRecord(record: ResultRecord) {
        _results.value = listOf(record) + _results.value
    }

    fun saveBatchResults(classId: String, subject: String, examType: String, maxScore: Double, scores: Map<String, Double>, remarks: Map<String, String>) {
        val current = _results.value.toMutableList()
        val classObj = _classes.value.find { it.id == classId }
        scores.forEach { (studentId, score) ->
            val student = _students.value.find { it.id == studentId }
            if (student != null) {
                val percentage = (score / maxScore) * 100
                val gradeLetter = when {
                    percentage >= 90 -> "A+"
                    percentage >= 80 -> "A"
                    percentage >= 70 -> "B"
                    percentage >= 60 -> "C"
                    percentage >= 50 -> "D"
                    else -> "F"
                }
                val existingIndex = current.indexOfFirst {
                    it.studentId == studentId && it.subject == subject && it.examType == examType
                }
                val newRecord = ResultRecord(
                    id = if (existingIndex >= 0) current[existingIndex].id else "res_${UUID.randomUUID().toString().take(6)}",
                    studentId = studentId,
                    studentName = student.fullName,
                    classId = classId,
                    className = classObj?.name ?: "Grade 10-A",
                    subject = subject,
                    examType = examType,
                    score = score,
                    maxScore = maxScore,
                    gradeLetter = gradeLetter,
                    remarks = remarks[studentId] ?: if (percentage >= 80) "Excellent comprehension" else "Good attempt",
                    term = "Term 1 (2026)"
                )
                if (existingIndex >= 0) {
                    current[existingIndex] = newRecord
                } else {
                    current.add(0, newRecord)
                }
            }
        }
        _results.value = current
    }

    // Fees Management
    fun recordFeePayment(feeRecordId: String, amount: Double, paymentMethod: String, referenceNo: String) {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dateStr = sdf.format(Date())
        val receipt = FeeReceipt(
            receiptNo = "REC-${System.currentTimeMillis().toString().takeLast(6)}",
            date = dateStr,
            amount = amount,
            paymentMethod = paymentMethod,
            referenceNo = referenceNo.ifBlank { "TXN-${UUID.randomUUID().toString().take(8).uppercase()}" }
        )

        _feeRecords.value = _feeRecords.value.map { fee ->
            if (fee.id == feeRecordId) {
                val newPaid = fee.paidAmount + amount
                val newStatus = if (newPaid >= fee.totalAmount) "Paid" else "Pending"
                fee.copy(
                    paidAmount = newPaid,
                    status = newStatus,
                    receipts = listOf(receipt) + fee.receipts
                )
            } else fee
        }

        // Update student fee balance
        val feeObj = _feeRecords.value.find { it.id == feeRecordId }
        feeObj?.let { f ->
            val student = _students.value.find { it.id == f.studentId }
            student?.let { s ->
                val newBalance = (s.feeBalance - amount).coerceAtLeast(0.0)
                updateStudent(s.copy(feeBalance = newBalance))
            }
        }
    }

    // Timetable
    fun addTimetableSlot(slot: TimetableSlot) {
        _timetableSlots.value = _timetableSlots.value + slot
    }

    fun removeTimetableSlot(slotId: String) {
        _timetableSlots.value = _timetableSlots.value.filter { it.id != slotId }
    }

    // Announcements
    fun addAnnouncement(title: String, content: String, authorName: String, authorRole: String, category: String, targetRole: String, isPinned: Boolean) {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        val newAnnouncement = Announcement(
            id = "anc_${UUID.randomUUID().toString().take(6)}",
            title = title,
            content = content,
            authorName = authorName,
            authorRole = authorRole,
            category = category,
            targetRole = targetRole,
            timestamp = sdf.format(Date()),
            isPinned = isPinned,
            readByUsers = emptySet()
        )
        _announcements.value = listOf(newAnnouncement) + _announcements.value
    }

    fun toggleAnnouncementRead(announcementId: String, userId: String) {
        _announcements.value = _announcements.value.map { anc ->
            if (anc.id == announcementId) {
                val updated = if (anc.readByUsers.contains(userId)) {
                    anc.readByUsers - userId
                } else {
                    anc.readByUsers + userId
                }
                anc.copy(readByUsers = updated)
            } else anc
        }
    }

    // Initial Mock Seed Data
    private fun initialClasses(): List<ClassRoom> = listOf(
        ClassRoom(
            id = "cls_10a",
            name = "Grade 10-A",
            gradeLevel = "Grade 10",
            section = "A",
            classTeacherId = "user_teacher",
            classTeacherName = "Prof. Marcus Reed",
            roomNumber = "Lab 204",
            studentCount = 28,
            capacity = 32,
            subjects = listOf("Mathematics", "Physics", "Chemistry", "English Lit", "Computer Science")
        ),
        ClassRoom(
            id = "cls_10b",
            name = "Grade 10-B",
            gradeLevel = "Grade 10",
            section = "B",
            classTeacherId = "tch_chen",
            classTeacherName = "Mr. David Chen",
            roomNumber = "Hall 108",
            studentCount = 30,
            capacity = 32,
            subjects = listOf("Mathematics", "Biology", "World History", "English Lit", "Visual Arts")
        ),
        ClassRoom(
            id = "cls_9a",
            name = "Grade 9-A",
            gradeLevel = "Grade 9",
            section = "A",
            classTeacherId = "tch_gomez",
            classTeacherName = "Ms. Linda Gomez",
            roomNumber = "Room 102",
            studentCount = 26,
            capacity = 30,
            subjects = listOf("General Science", "Algebra I", "Social Studies", "English", "Physical Ed")
        ),
        ClassRoom(
            id = "cls_11a",
            name = "Grade 11-A",
            gradeLevel = "Grade 11",
            section = "A",
            classTeacherId = "user_admin",
            classTeacherName = "Dr. Eleanor Vance",
            roomNumber = "Science Block B",
            studentCount = 24,
            capacity = 28,
            subjects = listOf("AP Physics", "Calculus BC", "Organic Chem", "Economics", "Literature")
        ),
        ClassRoom(
            id = "cls_12a",
            name = "Grade 12-A",
            gradeLevel = "Grade 12",
            section = "A",
            classTeacherId = "tch_thorne",
            classTeacherName = "Mrs. Rebecca Thorne",
            roomNumber = "Honors Wing 301",
            studentCount = 22,
            capacity = 25,
            subjects = listOf("Multivariable Calculus", "Advanced Biology", "World Politics", "Statistics")
        )
    )

    private fun initialStudents(): List<Student> = listOf(
        Student(
            id = "stu_10a_01",
            rollNumber = "1024",
            fullName = "Liam Jenkins",
            gradeClassId = "cls_10a",
            gradeClassName = "Grade 10-A",
            guardianName = "Sarah Jenkins",
            guardianPhone = "+1 (555) 234-5678",
            email = "liam.jenkins@student.eduadmin.academy",
            status = "Active",
            attendancePercentage = 95.8f,
            feeBalance = 250.0
        ),
        Student(
            id = "stu_10a_02",
            rollNumber = "1025",
            fullName = "Emma Watson-Clark",
            gradeClassId = "cls_10a",
            gradeClassName = "Grade 10-A",
            guardianName = "Arthur Clark",
            guardianPhone = "+1 (555) 345-6789",
            email = "emma.w@student.eduadmin.academy",
            status = "Active",
            attendancePercentage = 98.2f,
            feeBalance = 0.0
        ),
        Student(
            id = "stu_10a_03",
            rollNumber = "1026",
            fullName = "Noah Al-Mansoor",
            gradeClassId = "cls_10a",
            gradeClassName = "Grade 10-A",
            guardianName = "Tariq Al-Mansoor",
            guardianPhone = "+1 (555) 456-7890",
            email = "noah.m@student.eduadmin.academy",
            status = "Active",
            attendancePercentage = 92.4f,
            feeBalance = 450.0
        ),
        Student(
            id = "stu_10a_04",
            rollNumber = "1027",
            fullName = "Sophia Rodriguez",
            gradeClassId = "cls_10a",
            gradeClassName = "Grade 10-A",
            guardianName = "Elena Rodriguez",
            guardianPhone = "+1 (555) 567-8901",
            email = "sophia.r@student.eduadmin.academy",
            status = "Active",
            attendancePercentage = 96.5f,
            feeBalance = 0.0
        ),
        Student(
            id = "stu_10a_05",
            rollNumber = "1028",
            fullName = "Lucas Bennett",
            gradeClassId = "cls_10a",
            gradeClassName = "Grade 10-A",
            guardianName = "George Bennett",
            guardianPhone = "+1 (555) 678-9012",
            email = "lucas.b@student.eduadmin.academy",
            status = "Active",
            attendancePercentage = 88.0f,
            feeBalance = 150.0
        ),
        Student(
            id = "stu_10b_01",
            rollNumber = "1051",
            fullName = "Ava Martinez",
            gradeClassId = "cls_10b",
            gradeClassName = "Grade 10-B",
            guardianName = "Carlos Martinez",
            guardianPhone = "+1 (555) 789-0123",
            email = "ava.m@student.eduadmin.academy",
            status = "Active",
            attendancePercentage = 97.0f,
            feeBalance = 0.0
        ),
        Student(
            id = "stu_10b_02",
            rollNumber = "1052",
            fullName = "Ethan Zhao",
            gradeClassId = "cls_10b",
            gradeClassName = "Grade 10-B",
            guardianName = "Mei Zhao",
            guardianPhone = "+1 (555) 890-1234",
            email = "ethan.z@student.eduadmin.academy",
            status = "Active",
            attendancePercentage = 91.5f,
            feeBalance = 600.0
        ),
        Student(
            id = "stu_9a_01",
            rollNumber = "9012",
            fullName = "Chloe Patel",
            gradeClassId = "cls_9a",
            gradeClassName = "Grade 9-A",
            guardianName = "Dev Patel",
            guardianPhone = "+1 (555) 901-2345",
            email = "chloe.p@student.eduadmin.academy",
            status = "Active",
            attendancePercentage = 99.1f,
            feeBalance = 0.0
        ),
        Student(
            id = "stu_11a_01",
            rollNumber = "1104",
            fullName = "Alexander Hayes",
            gradeClassId = "cls_11a",
            gradeClassName = "Grade 11-A",
            guardianName = "Patricia Hayes",
            guardianPhone = "+1 (555) 012-3456",
            email = "alex.h@student.eduadmin.academy",
            status = "Active",
            attendancePercentage = 94.0f,
            feeBalance = 300.0
        ),
        Student(
            id = "stu_12a_01",
            rollNumber = "1201",
            fullName = "Isabella Rossi",
            gradeClassId = "cls_12a",
            gradeClassName = "Grade 12-A",
            guardianName = "Marco Rossi",
            guardianPhone = "+1 (555) 123-4567",
            email = "isabella.r@student.eduadmin.academy",
            status = "Active",
            attendancePercentage = 96.8f,
            feeBalance = 0.0
        )
    )

    private fun initialTeachers(): List<Teacher> = listOf(
        Teacher(
            id = "user_teacher",
            fullName = "Prof. Marcus Reed",
            email = "m.reed@eduadmin.academy",
            phone = "+1 (555) 821-4321",
            subjects = listOf("Mathematics", "Physics", "AP Calculus"),
            assignedClasses = listOf("Grade 10-A", "Grade 10-B", "Grade 11-A"),
            roomNumber = "Lab 204"
        ),
        Teacher(
            id = "tch_chen",
            fullName = "Mr. David Chen",
            email = "d.chen@eduadmin.academy",
            phone = "+1 (555) 821-4322",
            subjects = listOf("Biology", "Chemistry"),
            assignedClasses = listOf("Grade 10-B", "Grade 9-A"),
            roomNumber = "Hall 108"
        ),
        Teacher(
            id = "tch_gomez",
            fullName = "Ms. Linda Gomez",
            email = "l.gomez@eduadmin.academy",
            phone = "+1 (555) 821-4323",
            subjects = listOf("General Science", "Social Studies"),
            assignedClasses = listOf("Grade 9-A"),
            roomNumber = "Room 102"
        ),
        Teacher(
            id = "tch_thorne",
            fullName = "Mrs. Rebecca Thorne",
            email = "r.thorne@eduadmin.academy",
            phone = "+1 (555) 821-4324",
            subjects = listOf("Literature", "World History"),
            assignedClasses = listOf("Grade 10-A", "Grade 12-A"),
            roomNumber = "Wing 301"
        )
    )

    private fun initialAttendance(): Map<String, Map<String, AttendanceStatus>> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return mapOf(
            "cls_10a-$today" to mapOf(
                "stu_10a_01" to AttendanceStatus.PRESENT,
                "stu_10a_02" to AttendanceStatus.PRESENT,
                "stu_10a_03" to AttendanceStatus.LATE,
                "stu_10a_04" to AttendanceStatus.PRESENT,
                "stu_10a_05" to AttendanceStatus.ABSENT
            ),
            "cls_10b-$today" to mapOf(
                "stu_10b_01" to AttendanceStatus.PRESENT,
                "stu_10b_02" to AttendanceStatus.PRESENT
            ),
            "cls_9a-$today" to mapOf(
                "stu_9a_01" to AttendanceStatus.PRESENT
            ),
            "cls_11a-$today" to mapOf(
                "stu_11a_01" to AttendanceStatus.PRESENT
            ),
            "cls_12a-$today" to mapOf(
                "stu_12a_01" to AttendanceStatus.PRESENT
            )
        )
    }

    private fun initialResults(): List<ResultRecord> = listOf(
        ResultRecord("res_1", "stu_10a_01", "Liam Jenkins", "cls_10a", "Grade 10-A", "Mathematics", "Midterm Exam", 94.0, 100.0, "A+", "Outstanding problem-solving logic", "Term 1 (2026)"),
        ResultRecord("res_2", "stu_10a_01", "Liam Jenkins", "cls_10a", "Grade 10-A", "Physics", "Midterm Exam", 88.0, 100.0, "A", "Great grasp of kinematics", "Term 1 (2026)"),
        ResultRecord("res_3", "stu_10a_01", "Liam Jenkins", "cls_10a", "Grade 10-A", "Chemistry", "Midterm Exam", 82.5, 100.0, "B+", "Very good lab work & report", "Term 1 (2026)"),
        ResultRecord("res_4", "stu_10a_01", "Liam Jenkins", "cls_10a", "Grade 10-A", "English Lit", "Midterm Exam", 91.0, 100.0, "A", "Insightful analytical essays", "Term 1 (2026)"),
        ResultRecord("res_5", "stu_10a_01", "Liam Jenkins", "cls_10a", "Grade 10-A", "Computer Science", "Quiz 1", 96.0, 100.0, "A+", "Exemplary algorithmic thinking", "Term 1 (2026)"),

        ResultRecord("res_6", "stu_10a_02", "Emma Watson-Clark", "cls_10a", "Grade 10-A", "Mathematics", "Midterm Exam", 98.0, 100.0, "A+", "Highest score in class", "Term 1 (2026)"),
        ResultRecord("res_7", "stu_10a_02", "Emma Watson-Clark", "cls_10a", "Grade 10-A", "Physics", "Midterm Exam", 95.0, 100.0, "A+", "Brilliant conceptual clarity", "Term 1 (2026)"),

        ResultRecord("res_8", "stu_10a_03", "Noah Al-Mansoor", "cls_10a", "Grade 10-A", "Mathematics", "Midterm Exam", 76.0, 100.0, "B", "Solid fundamentals, needs practice on proofs", "Term 1 (2026)"),
        ResultRecord("res_9", "stu_10a_04", "Sophia Rodriguez", "cls_10a", "Grade 10-A", "Mathematics", "Midterm Exam", 89.0, 100.0, "A", "Active participation", "Term 1 (2026)")
    )

    private fun initialFeeRecords(): List<FeeRecord> = listOf(
        FeeRecord(
            id = "fee_01",
            studentId = "stu_10a_01",
            studentName = "Liam Jenkins",
            classId = "cls_10a",
            className = "Grade 10-A",
            title = "Term 1 Tuition & Lab Dues",
            totalAmount = 1250.0,
            paidAmount = 1000.0,
            dueDate = "Nov 15, 2026",
            status = "Pending",
            receipts = listOf(
                FeeReceipt("REC-884120", "12 Sep 2026", 1000.0, "Credit Card", "TXN-88A9F1")
            )
        ),
        FeeRecord(
            id = "fee_02",
            studentId = "stu_10a_02",
            studentName = "Emma Watson-Clark",
            classId = "cls_10a",
            className = "Grade 10-A",
            title = "Term 1 Full Tuition & Science Bundle",
            totalAmount = 1400.0,
            paidAmount = 1400.0,
            dueDate = "Oct 30, 2026",
            status = "Paid",
            receipts = listOf(
                FeeReceipt("REC-773199", "05 Sep 2026", 1400.0, "Bank Transfer", "TXN-49B012")
            )
        ),
        FeeRecord(
            id = "fee_03",
            studentId = "stu_10a_03",
            studentName = "Noah Al-Mansoor",
            classId = "cls_10a",
            className = "Grade 10-A",
            title = "Term 1 Tuition & Transport",
            totalAmount = 1350.0,
            paidAmount = 900.0,
            dueDate = "Nov 10, 2026",
            status = "Pending",
            receipts = listOf(
                FeeReceipt("REC-661023", "18 Sep 2026", 900.0, "Online Banking", "TXN-11C892")
            )
        ),
        FeeRecord(
            id = "fee_04",
            studentId = "stu_10b_02",
            studentName = "Ethan Zhao",
            classId = "cls_10b",
            className = "Grade 10-B",
            title = "Term 1 Tuition & Arts Supplies",
            totalAmount = 1200.0,
            paidAmount = 600.0,
            dueDate = "Oct 15, 2026",
            status = "Overdue",
            receipts = listOf(
                FeeReceipt("REC-550912", "01 Sep 2026", 600.0, "Cash Deposit", "TXN-99D321")
            )
        )
    )

    private fun initialTimetable(): List<TimetableSlot> = listOf(
        // Monday
        TimetableSlot("tt_m1", "Monday", 1, "08:30 - 09:20", "cls_10a", "Grade 10-A", "Mathematics", "Prof. Marcus Reed", "Lab 204"),
        TimetableSlot("tt_m2", "Monday", 2, "09:25 - 10:15", "cls_10a", "Grade 10-A", "Physics", "Prof. Marcus Reed", "Lab 204"),
        TimetableSlot("tt_m3", "Monday", 3, "10:30 - 11:20", "cls_10a", "Grade 10-A", "Chemistry", "Mr. David Chen", "Hall 108"),
        TimetableSlot("tt_m4", "Monday", 4, "11:25 - 12:15", "cls_10a", "Grade 10-A", "English Lit", "Mrs. Rebecca Thorne", "Wing 301"),
        TimetableSlot("tt_m5", "Monday", 5, "01:00 - 01:50", "cls_10a", "Grade 10-A", "Computer Science", "Prof. Marcus Reed", "Lab 204"),
        TimetableSlot("tt_m6", "Monday", 6, "01:55 - 02:45", "cls_10a", "Grade 10-A", "Physical Ed", "Coach Miller", "Gymnasium"),

        // Tuesday
        TimetableSlot("tt_t1", "Tuesday", 1, "08:30 - 09:20", "cls_10a", "Grade 10-A", "English Lit", "Mrs. Rebecca Thorne", "Wing 301"),
        TimetableSlot("tt_t2", "Tuesday", 2, "09:25 - 10:15", "cls_10a", "Grade 10-A", "Mathematics", "Prof. Marcus Reed", "Lab 204"),
        TimetableSlot("tt_t3", "Tuesday", 3, "10:30 - 11:20", "cls_10a", "Grade 10-A", "World History", "Ms. Linda Gomez", "Room 102"),
        TimetableSlot("tt_t4", "Tuesday", 4, "11:25 - 12:15", "cls_10a", "Grade 10-A", "Physics", "Prof. Marcus Reed", "Lab 204"),

        // Wednesday
        TimetableSlot("tt_w1", "Wednesday", 1, "08:30 - 09:20", "cls_10a", "Grade 10-A", "Chemistry Lab", "Mr. David Chen", "Hall 108"),
        TimetableSlot("tt_w2", "Wednesday", 2, "09:25 - 10:15", "cls_10a", "Grade 10-A", "Computer Science", "Prof. Marcus Reed", "Lab 204"),
        TimetableSlot("tt_w3", "Wednesday", 3, "10:30 - 11:20", "cls_10a", "Grade 10-A", "Mathematics", "Prof. Marcus Reed", "Lab 204"),

        // Thursday
        TimetableSlot("tt_th1", "Thursday", 1, "08:30 - 09:20", "cls_10a", "Grade 10-A", "Physics Practicals", "Prof. Marcus Reed", "Lab 204"),
        TimetableSlot("tt_th2", "Thursday", 2, "09:25 - 10:15", "cls_10a", "Grade 10-A", "English Lit", "Mrs. Rebecca Thorne", "Wing 301"),
        TimetableSlot("tt_th3", "Thursday", 3, "10:30 - 11:20", "cls_10a", "Grade 10-A", "Mathematics", "Prof. Marcus Reed", "Lab 204"),

        // Friday
        TimetableSlot("tt_f1", "Friday", 1, "08:30 - 09:20", "cls_10a", "Grade 10-A", "Seminar & Ethics", "Dr. Eleanor Vance", "Auditorium"),
        TimetableSlot("tt_f2", "Friday", 2, "09:25 - 10:15", "cls_10a", "Grade 10-A", "Mathematics Quiz", "Prof. Marcus Reed", "Lab 204"),
        TimetableSlot("tt_f3", "Friday", 3, "10:30 - 11:20", "cls_10a", "Grade 10-A", "Sports / Club", "Coach Miller", "Sports Field")
    )

    private fun initialAnnouncements(): List<Announcement> = listOf(
        Announcement(
            id = "anc_1",
            title = "Annual Science & Tech Fair 2026 - Registration Open",
            content = "The annual inter-school innovation expo is scheduled for November 28th. Students from Grades 9-12 are invited to submit their STEM project abstracts to their respective science advisors by Friday next week.",
            authorName = "Dr. Eleanor Vance",
            authorRole = "Principal",
            category = "Event",
            targetRole = "ALL",
            timestamp = "Today, 08:30 AM",
            isPinned = true,
            readByUsers = emptySet()
        ),
        Announcement(
            id = "anc_2",
            title = "Term 1 Mid-Term Examination Schedule Released",
            content = "Final schedules for all Grade 9-12 Mid-Term written and laboratory assessments have been finalized. Please check your personalized Timetable and Exam portal for exact room allocations.",
            authorName = "Academic Office",
            authorRole = "Administration",
            category = "Academic",
            targetRole = "ALL",
            timestamp = "Yesterday, 03:15 PM",
            isPinned = true,
            readByUsers = setOf("user_parent")
        ),
        Announcement(
            id = "anc_3",
            title = "Reminder: Term 1 Tuition Second Installment Due Date",
            content = "Parents and guardians are kindly reminded that outstanding balances for Term 1 tuition and laboratory fees should be settled before November 15th to avoid late administrative surcharges. Online payments are active via the Fees tab.",
            authorName = "Bursar's Office",
            authorRole = "Finance",
            category = "Fee Notice",
            targetRole = "STUDENT_PARENT",
            timestamp = "Oct 20, 11:00 AM",
            isPinned = false,
            readByUsers = emptySet()
        ),
        Announcement(
            id = "anc_4",
            title = "Staff Development Workshop on Advanced Pedagogy",
            content = "Mandatory faculty development session this coming Friday at 03:00 PM in the Conference Hall. Light refreshments will be provided.",
            authorName = "Dr. Eleanor Vance",
            authorRole = "Principal",
            category = "Urgent",
            targetRole = "TEACHER",
            timestamp = "Oct 19, 09:45 AM",
            isPinned = false,
            readByUsers = emptySet()
        )
    )
}
