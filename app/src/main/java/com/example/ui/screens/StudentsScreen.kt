package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.model.ClassRoom
import com.example.model.Role
import com.example.model.Student
import com.example.model.UserAccount
import com.example.ui.components.EduAvatar
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun StudentsScreen(
    user: UserAccount,
    students: List<Student>,
    classes: List<ClassRoom>,
    onAddStudent: (name: String, roll: String, classId: String, guardian: String, phone: String, email: String) -> Unit,
    onUpdateStudent: (Student) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedClassFilter by remember { mutableStateOf("All Classes") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var viewingStudent by remember { mutableStateOf<Student?>(null) }

    // Add Student Form State
    var newName by remember { mutableStateOf("") }
    var newRoll by remember { mutableStateOf("") }
    var newClassId by remember { mutableStateOf(classes.firstOrNull()?.id ?: "cls_10a") }
    var newGuardian by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }

    val filteredStudents = students.filter { stu ->
        val matchesClass = selectedClassFilter == "All Classes" || stu.gradeClassName == selectedClassFilter
        val matchesSearch = stu.fullName.contains(searchQuery, ignoreCase = true) ||
                stu.rollNumber.contains(searchQuery, ignoreCase = true) ||
                stu.guardianName.contains(searchQuery, ignoreCase = true)
        matchesClass && matchesSearch
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "Student Directory & Records",
                subtitle = "Manage enrolled students, profiles, and classroom assignments",
                actionButtonText = if (user.role != Role.STUDENT_PARENT) "+ Add Student" else null,
                actionButtonIcon = Icons.Default.PersonAdd,
                onActionClick = { showAddDialog = true }
            )
        }

        // Search & Filter Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search name, roll # or parent...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("student_search_input")
                )

                // Class filter dropdown
                var filterExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(
                        onClick = { filterExpanded = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Text(selectedClassFilter, fontSize = 12.sp, maxLines = 1)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(
                        expanded = filterExpanded,
                        onDismissRequest = { filterExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Classes") },
                            onClick = {
                                selectedClassFilter = "All Classes"
                                filterExpanded = false
                            }
                        )
                        classes.forEach { cls ->
                            DropdownMenuItem(
                                text = { Text(cls.name) },
                                onClick = {
                                    selectedClassFilter = cls.name
                                    filterExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Student List
        items(filteredStudents) { stu ->
            ElevatedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_card_${stu.rollNumber}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EduAvatar(
                        initials = stu.fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                        backgroundColor = Navy800,
                        textColor = Color.White,
                        size = 46
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stu.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusBadge(
                                text = stu.gradeClassName,
                                color = Teal700,
                                backgroundColor = Teal100
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Roll #${stu.rollNumber}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Amber600
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Guardian: ${stu.guardianName}",
                                fontSize = 12.sp,
                                color = Slate600
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge(
                                text = "${stu.attendancePercentage}% Att.",
                                color = if (stu.attendancePercentage >= 90) SuccessGreen else Amber600,
                                backgroundColor = if (stu.attendancePercentage >= 90) SuccessGreenLight else Amber100
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (stu.feeBalance > 0) {
                                StatusBadge(
                                    text = "Dues: $${stu.feeBalance.toInt()}",
                                    color = DangerRed,
                                    backgroundColor = DangerRedLight
                                )
                            } else {
                                StatusBadge(
                                    text = "Fees Paid",
                                    color = SuccessGreen,
                                    backgroundColor = SuccessGreenLight
                                )
                            }
                        }
                    }

                    // Actions
                    if (user.role != Role.STUDENT_PARENT) {
                        IconButton(
                            onClick = { editingStudent = stu },
                            modifier = Modifier.testTag("edit_student_${stu.rollNumber}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Student", tint = Slate500, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }

    // Add Student Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Enroll New Student", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Full Legal Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_student_name")
                    )
                    OutlinedTextField(
                        value = newRoll,
                        onValueChange = { newRoll = it },
                        label = { Text("Roll Number / Student ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_student_roll")
                    )

                    // Class selector
                    var classDropdownOpen by remember { mutableStateOf(false) }
                    val selectedClassObj = classes.find { it.id == newClassId }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { classDropdownOpen = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Class: ${selectedClassObj?.name ?: "Select Class"}")
                        }
                        DropdownMenu(
                            expanded = classDropdownOpen,
                            onDismissRequest = { classDropdownOpen = false }
                        ) {
                            classes.forEach { cls ->
                                DropdownMenuItem(
                                    text = { Text(cls.name) },
                                    onClick = {
                                        newClassId = cls.id
                                        classDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newGuardian,
                        onValueChange = { newGuardian = it },
                        label = { Text("Parent / Guardian Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Contact Phone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newRoll.isNotBlank()) {
                            onAddStudent(
                                newName,
                                newRoll,
                                newClassId,
                                newGuardian.ifBlank { "Guardian" },
                                newPhone.ifBlank { "+1 555-000-0000" },
                                newEmail.ifBlank { "${newRoll}@student.eduadmin.academy" }
                            )
                            showAddDialog = false
                            newName = ""
                            newRoll = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                    modifier = Modifier.testTag("submit_add_student")
                ) {
                    Text("Enroll Student")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Student Dialog
    editingStudent?.let { stu ->
        var editName by remember { mutableStateOf(stu.fullName) }
        var editRoll by remember { mutableStateOf(stu.rollNumber) }
        var editClassId by remember { mutableStateOf(stu.gradeClassId) }
        var editGuardian by remember { mutableStateOf(stu.guardianName) }
        var editPhone by remember { mutableStateOf(stu.guardianPhone) }

        AlertDialog(
            onDismissRequest = { editingStudent = null },
            title = { Text("Edit Student Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Legal Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editRoll,
                        onValueChange = { editRoll = it },
                        label = { Text("Roll Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Reassign Class
                    var reassignClassOpen by remember { mutableStateOf(false) }
                    val currentCls = classes.find { it.id == editClassId }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { reassignClassOpen = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reassign Class: ${currentCls?.name ?: stu.gradeClassName}")
                        }
                        DropdownMenu(
                            expanded = reassignClassOpen,
                            onDismissRequest = { reassignClassOpen = false }
                        ) {
                            classes.forEach { cls ->
                                DropdownMenuItem(
                                    text = { Text(cls.name) },
                                    onClick = {
                                        editClassId = cls.id
                                        reassignClassOpen = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editGuardian,
                        onValueChange = { editGuardian = it },
                        label = { Text("Guardian Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Contact Phone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cls = classes.find { it.id == editClassId }
                        val updated = stu.copy(
                            fullName = editName,
                            rollNumber = editRoll,
                            gradeClassId = editClassId,
                            gradeClassName = cls?.name ?: stu.gradeClassName,
                            guardianName = editGuardian,
                            guardianPhone = editPhone
                        )
                        onUpdateStudent(updated)
                        editingStudent = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy800)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingStudent = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
