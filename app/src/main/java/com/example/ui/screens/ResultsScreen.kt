package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.EduAvatar
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun ResultsScreen(
    user: UserAccount,
    classes: List<ClassRoom>,
    students: List<Student>,
    results: List<ResultRecord>,
    onSubmitBatchResults: (classId: String, subject: String, examType: String, maxScore: Double, scores: Map<String, Double>, remarks: Map<String, String>) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Mode switcher: Teacher entry mode vs Student report sheet
    var activeMode by remember {
        mutableStateOf(if (user.role == Role.STUDENT_PARENT) "Report Card" else "Score Entry")
    }

    // Teacher Score Entry State
    var selectedClassId by remember { mutableStateOf(classes.firstOrNull()?.id ?: "cls_10a") }
    val selectedClassObj = classes.find { it.id == selectedClassId }
    var selectedSubject by remember { mutableStateOf("Mathematics") }
    var selectedExamType by remember { mutableStateOf("Midterm Exam") }
    var maxScoreText by remember { mutableStateOf("100") }

    val classStudents = students.filter { it.gradeClassId == selectedClassId }

    // Map of studentId -> score
    val scoreMap = remember(selectedClassId, selectedSubject, selectedExamType) {
        val map = mutableStateMapOf<String, String>()
        classStudents.forEach { stu ->
            val existing = results.find { it.studentId == stu.id && it.subject == selectedSubject && it.examType == selectedExamType }
            map[stu.id] = existing?.score?.toInt()?.toString() ?: "85"
        }
        map
    }

    val remarksMap = remember(selectedClassId, selectedSubject, selectedExamType) {
        val map = mutableStateMapOf<String, String>()
        classStudents.forEach { stu ->
            val existing = results.find { it.studentId == stu.id && it.subject == selectedSubject && it.examType == selectedExamType }
            map[stu.id] = existing?.remarks ?: "Consistent understanding"
        }
        map
    }

    // Student Report Card Target Student
    val targetStudent = if (user.role == Role.STUDENT_PARENT) {
        students.find { it.id == user.studentId } ?: students.firstOrNull()
    } else {
        students.firstOrNull()
    }

    val targetStudentResults = results.filter { it.studentId == targetStudent?.id }
    val averageScore = if (targetStudentResults.isNotEmpty()) {
        targetStudentResults.map { it.score }.average()
    } else 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mode Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Examination Results & Grades",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Academic grading engine, score entry, and student reports",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }

                // Mode Tabs
                Surface(
                    color = Slate200,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(3.dp)) {
                        listOf("Score Entry", "Report Card").forEach { mode ->
                            val isSelected = activeMode == mode
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Navy800 else Color.Transparent)
                                    .clickable { activeMode = mode }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("result_mode_${mode.lowercase().replace(" ", "_")}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else Slate700,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        if (activeMode == "Score Entry") {
            // Teacher-Facing Score Entry Header Form
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Grading Parameters",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Class Dropdown
                            var classExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { classExpanded = true },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Class: ${selectedClassObj?.name ?: "10-A"}", fontSize = 12.sp, maxLines = 1)
                                }
                                DropdownMenu(expanded = classExpanded, onDismissRequest = { classExpanded = false }) {
                                    classes.forEach { cls ->
                                        DropdownMenuItem(
                                            text = { Text(cls.name) },
                                            onClick = {
                                                selectedClassId = cls.id
                                                classExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Subject Dropdown
                            var subjectExpanded by remember { mutableStateOf(false) }
                            val subjectsList = selectedClassObj?.subjects ?: listOf("Mathematics", "Physics", "Chemistry", "English Lit")
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { subjectExpanded = true },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Subj: $selectedSubject", fontSize = 12.sp, maxLines = 1)
                                }
                                DropdownMenu(expanded = subjectExpanded, onDismissRequest = { subjectExpanded = false }) {
                                    subjectsList.forEach { subj ->
                                        DropdownMenuItem(
                                            text = { Text(subj) },
                                            onClick = {
                                                selectedSubject = subj
                                                subjectExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Exam Type Dropdown
                            var examExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { examExpanded = true },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(selectedExamType, fontSize = 12.sp, maxLines = 1)
                                }
                                DropdownMenu(expanded = examExpanded, onDismissRequest = { examExpanded = false }) {
                                    listOf("Midterm Exam", "Final Exam", "Unit Quiz 1", "Practical Lab").forEach { exam ->
                                        DropdownMenuItem(
                                            text = { Text(exam) },
                                            onClick = {
                                                selectedExamType = exam
                                                examExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Grading ${classStudents.size} students for $selectedSubject",
                                fontSize = 12.sp,
                                color = Slate500
                            )

                            Button(
                                onClick = {
                                    val numericScores = scoreMap.mapNotNull { (id, txt) ->
                                        txt.toDoubleOrNull()?.let { id to it }
                                    }.toMap()
                                    onSubmitBatchResults(
                                        selectedClassId,
                                        selectedSubject,
                                        selectedExamType,
                                        maxScoreText.toDoubleOrNull() ?: 100.0,
                                        numericScores,
                                        remarksMap
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("publish_grades_btn")
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Publish Scores", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Student Scoring Table / List
            items(classStudents) { stu ->
                val currentScoreStr = scoreMap[stu.id] ?: ""
                val scoreVal = currentScoreStr.toDoubleOrNull() ?: 0.0
                val gradeLetter = when {
                    scoreVal >= 90 -> "A+"
                    scoreVal >= 80 -> "A"
                    scoreVal >= 70 -> "B"
                    scoreVal >= 60 -> "C"
                    scoreVal >= 50 -> "D"
                    else -> "F"
                }

                ElevatedCard(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().testTag("student_grade_row_${stu.rollNumber}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                EduAvatar(
                                    initials = stu.fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                                    backgroundColor = Navy800,
                                    size = 36
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = stu.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Slate900
                                    )
                                    Text(
                                        text = "Roll #${stu.rollNumber}",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = currentScoreStr,
                                    onValueChange = { scoreMap[stu.id] = it },
                                    label = { Text("Score") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .width(90.dp)
                                        .testTag("score_input_${stu.rollNumber}")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (gradeLetter.startsWith("A")) Teal100 else Amber100),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = gradeLetter,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (gradeLetter.startsWith("A")) Teal700 else Amber600
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = remarksMap[stu.id] ?: "",
                            onValueChange = { remarksMap[stu.id] = it },
                            label = { Text("Teacher Evaluation Remarks") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            // Student-Facing Official Report Card View
            targetStudent?.let { stu ->
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth().testTag("official_report_card")
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Academy Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "EduAdmin Academy",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Navy800
                                    )
                                    Text(
                                        text = "Official Academic Transcript • Term 1 (2026)",
                                        fontSize = 12.sp,
                                        color = Slate500
                                    )
                                }
                                StatusBadge(
                                    text = "Certified",
                                    color = SuccessGreen,
                                    backgroundColor = SuccessGreenLight
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Slate200)
                            Spacer(modifier = Modifier.height(14.dp))

                            // Student Info Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Student Name", fontSize = 11.sp, color = Slate500)
                                    Text(stu.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate800)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Roll Number", fontSize = 11.sp, color = Slate500)
                                    Text(stu.rollNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate800)
                                }
                                Column {
                                    Text("Class Cohort", fontSize = 11.sp, color = Slate500)
                                    Text(stu.gradeClassName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate800)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Class Standing", fontSize = 11.sp, color = Slate500)
                                    Text("Rank: 2nd / 28", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Teal700)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Overall Average", fontSize = 11.sp, color = Slate500)
                                    Text(
                                        text = String.format("%.1f%%", averageScore),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Navy800
                                    )
                                    StatusBadge(text = "Grade A", color = Teal700, backgroundColor = Teal100)
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Subject Results Table
                            Text(
                                text = "Course Assessments & Evaluations",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Slate700
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            targetStudentResults.forEachIndexed { idx, res ->
                                Surface(
                                    color = if (idx % 2 == 0) Slate50 else Color.White,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1.5f)) {
                                            Text(res.subject, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                                            Text(res.remarks, fontSize = 11.sp, color = Slate500)
                                        }
                                        Text(
                                            text = "${res.score.toInt()}/100",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = Slate700,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .background(if (res.gradeLetter.startsWith("A")) Teal100 else Amber100),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = res.gradeLetter,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (res.gradeLetter.startsWith("A")) Teal700 else Amber600
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Print / Export button
                            Button(
                                onClick = { onShowMessage("Transcript PDF exported successfully") },
                                colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("export_transcript_btn")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Download Official Report Card (PDF)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
