package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.EduAvatar
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun StudentDashboardScreen(
    user: UserAccount,
    students: List<Student>,
    results: List<ResultRecord>,
    feeRecords: List<FeeRecord>,
    timetableSlots: List<TimetableSlot>,
    announcements: List<Announcement>,
    onNavigate: (Screen) -> Unit,
    onPayFee: (feeId: String, amount: Double, method: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Find active child student for this parent
    val activeStudent = students.find { it.id == user.studentId } ?: students.firstOrNull() ?: Student(
        id = "stu_10a_01",
        rollNumber = "1024",
        fullName = "Liam Jenkins",
        gradeClassId = "cls_10a",
        gradeClassName = "Grade 10-A",
        guardianName = user.fullName,
        guardianPhone = "+1 (555) 234-5678",
        email = "liam.jenkins@student.eduadmin.academy"
    )

    val studentResults = results.filter { it.studentId == activeStudent.id }
    val studentFees = feeRecords.filter { it.studentId == activeStudent.id }
    val pendingFee = studentFees.find { it.status != "Paid" }
    val todaySlots = timetableSlots.filter { it.classId == activeStudent.gradeClassId && it.dayOfWeek == "Monday" }
    val studentAnnouncements = announcements.filter { it.targetRole == "ALL" || it.targetRole == "STUDENT_PARENT" }

    var showPaymentModal by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf("Credit Card") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Child Banner Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Navy800),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("parent_child_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EduAvatar(
                        initials = "LJ",
                        backgroundColor = Amber500,
                        textColor = Navy900,
                        size = 54
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge(
                                text = activeStudent.gradeClassName,
                                color = Teal100,
                                backgroundColor = Teal700.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Roll #${activeStudent.rollNumber}",
                                color = Amber100,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeStudent.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Guardian: ${user.fullName} • Academic Term 1 (2026)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate300
                        )
                    }
                }
            }
        }

        // Attendance Summary & Fee Balance (Side-by-side or stacked)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Attendance Summary Card
                ElevatedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(Screen.ATTENDANCE) }
                        .testTag("student_attendance_summary"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Attendance",
                                style = MaterialTheme.typography.labelLarge,
                                color = Slate600,
                                fontWeight = FontWeight.SemiBold
                            )
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SuccessGreenLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${activeStudent.attendancePercentage}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                        Text(
                            text = "48 Days Present • 2 Absent",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { activeStudent.attendancePercentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = SuccessGreen,
                            trackColor = Slate200
                        )
                    }
                }

                // Fee Balance Card
                ElevatedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(Screen.FEES) }
                        .testTag("student_fee_summary"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (activeStudent.feeBalance > 0) Amber50 else Color.White
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Fee Balance",
                                style = MaterialTheme.typography.labelLarge,
                                color = Slate600,
                                fontWeight = FontWeight.SemiBold
                            )
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (activeStudent.feeBalance > 0) Amber100 else SuccessGreenLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = if (activeStudent.feeBalance > 0) Amber600 else SuccessGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (activeStudent.feeBalance > 0) "$${activeStudent.feeBalance}" else "$0.00",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (activeStudent.feeBalance > 0) Amber600 else SuccessGreen
                        )
                        Text(
                            text = if (activeStudent.feeBalance > 0) "Due: Nov 15, 2026" else "All Dues Cleared",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (activeStudent.feeBalance > 0) {
                            Button(
                                onClick = { showPaymentModal = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = Navy900),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp)
                                    .testTag("pay_now_quick_btn")
                            ) {
                                Text("Pay Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            StatusBadge(text = "Cleared", color = SuccessGreen, backgroundColor = SuccessGreenLight)
                        }
                    }
                }
            }
        }

        // Recent Results Snapshot
        item {
            SectionHeader(
                title = "Recent Academic Performance",
                subtitle = "Mid-Term Examination Results",
                actionButtonText = "Full Report Card",
                actionButtonIcon = Icons.Default.Assessment,
                onActionClick = { onNavigate(Screen.RESULTS) }
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    studentResults.take(4).forEachIndexed { index, res ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = res.subject,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Slate900
                                )
                                Text(
                                    text = res.remarks,
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${res.score.toInt()}/100",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate700
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (res.gradeLetter.startsWith("A")) Teal100 else Amber100),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = res.gradeLetter,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (res.gradeLetter.startsWith("A")) Teal700 else Amber600
                                    )
                                }
                            }
                        }
                        if (index < studentResults.take(4).lastIndex) {
                            HorizontalDivider(color = Slate100)
                        }
                    }
                }
            }
        }

        // Today's Timetable Snapshot
        item {
            SectionHeader(
                title = "Today's Schedule Snapshot",
                subtitle = "Active periods for ${activeStudent.gradeClassName}",
                actionButtonText = "Weekly Schedule",
                actionButtonIcon = Icons.Default.CalendarMonth,
                onActionClick = { onNavigate(Screen.TIMETABLE) }
            )
        }

        items(todaySlots.take(3)) { slot ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Navy100),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "P${slot.periodNumber}",
                            fontWeight = FontWeight.Bold,
                            color = Navy800,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = slot.subject,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Slate900
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${slot.timeRange} • ${slot.roomNumber}",
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }
                    }
                    Text(
                        text = slot.teacherName,
                        fontSize = 12.sp,
                        color = Teal700,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // School Announcements Feed
        item {
            SectionHeader(
                title = "School Circulars & Announcements",
                subtitle = "Official notices for parents and students",
                actionButtonText = "View All",
                actionButtonIcon = Icons.Default.OpenInNew,
                onActionClick = { onNavigate(Screen.ANNOUNCEMENTS) }
            )
        }

        items(studentAnnouncements.take(2)) { anc ->
            ElevatedCard(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(Screen.ANNOUNCEMENTS) }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(
                            text = anc.category,
                            color = Navy800,
                            backgroundColor = Navy100
                        )
                        Text(text = anc.timestamp, fontSize = 11.sp, color = Slate400)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = anc.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = anc.content,
                        fontSize = 12.sp,
                        color = Slate600,
                        maxLines = 2
                    )
                }
            }
        }
    }

    // Pay Fee Modal Dialog
    if (showPaymentModal && pendingFee != null) {
        AlertDialog(
            onDismissRequest = { showPaymentModal = false },
            title = {
                Text("Settle Tuition Fee Balance", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Pay outstanding balance for ${activeStudent.fullName} (${activeStudent.gradeClassName})",
                        fontSize = 13.sp,
                        color = Slate600
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate100),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Outstanding:", fontWeight = FontWeight.Medium)
                            Text("$${activeStudent.feeBalance}", fontWeight = FontWeight.Bold, color = Amber600)
                        }
                    }

                    Text("Select Payment Gateway:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    listOf("Credit / Debit Card", "Bank Direct Transfer", "Campus Digital Wallet").forEach { method ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPaymentMethod = method }
                        ) {
                            RadioButton(
                                selected = selectedPaymentMethod == method,
                                onClick = { selectedPaymentMethod = method },
                                colors = RadioButtonDefaults.colors(selectedColor = Navy800)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(method, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onPayFee(pendingFee.id, activeStudent.feeBalance, selectedPaymentMethod)
                        showPaymentModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_online_payment_btn")
                ) {
                    Text("Pay $${activeStudent.feeBalance} Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
