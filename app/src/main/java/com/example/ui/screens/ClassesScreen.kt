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
import com.example.model.Screen
import com.example.model.UserAccount
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun ClassesScreen(
    user: UserAccount,
    classes: List<ClassRoom>,
    onAddClass: (name: String, grade: String, section: String, teacher: String, room: String, capacity: Int, subjects: List<String>) -> Unit,
    onNavigate: (Screen) -> Unit,
    onSelectClassForAttendance: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedGradeFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    // Add Class Form State
    var newClassName by remember { mutableStateOf("") }
    var newGradeLevel by remember { mutableStateOf("Grade 10") }
    var newSection by remember { mutableStateOf("C") }
    var newTeacher by remember { mutableStateOf("") }
    var newRoom by remember { mutableStateOf("") }
    var newCapacity by remember { mutableStateOf("30") }

    val filteredClasses = classes.filter { cls ->
        (selectedGradeFilter == "All" || cls.gradeLevel == selectedGradeFilter) &&
        (cls.name.contains(searchQuery, ignoreCase = true) || cls.classTeacherName.contains(searchQuery, ignoreCase = true))
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
                title = "Classes & Academic Cohorts",
                subtitle = "Manage class sections, assigned homeroom teachers, and capacity",
                actionButtonText = if (user.role == Role.ADMIN) "+ Add Class" else null,
                actionButtonIcon = Icons.Default.Add,
                onActionClick = { showAddDialog = true }
            )
        }

        // Search & Filter Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by class or teacher...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("class_search_input")
                )

                // Grade Filter Chips
                val grades = listOf("All", "Grade 9", "Grade 10", "Grade 11", "Grade 12")
                var gradeDropdownOpen by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(
                        onClick = { gradeDropdownOpen = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Text(selectedGradeFilter, fontSize = 13.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(
                        expanded = gradeDropdownOpen,
                        onDismissRequest = { gradeDropdownOpen = false }
                    ) {
                        grades.forEach { grade ->
                            DropdownMenuItem(
                                text = { Text(grade) },
                                onClick = {
                                    selectedGradeFilter = grade
                                    gradeDropdownOpen = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Classes List
        items(filteredClasses) { cls ->
            val fillRatio = cls.studentCount.toFloat() / cls.capacity.toFloat()
            ElevatedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth().testTag("class_card_${cls.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Navy100),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = Navy800, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = cls.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    text = "Homeroom: ${cls.roomNumber}",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                            }
                        }
                        StatusBadge(
                            text = "${cls.studentCount}/${cls.capacity} Enrolled",
                            color = if (fillRatio >= 0.9f) Amber600 else Teal700,
                            backgroundColor = if (fillRatio >= 0.9f) Amber100 else Teal100
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Class Teacher: ",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                        Text(
                            text = cls.classTeacherName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate800
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subjects chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cls.subjects.take(4).forEach { subj ->
                            Surface(
                                color = Slate100,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = subj,
                                    fontSize = 11.sp,
                                    color = Slate700,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (cls.subjects.size > 4) {
                            Surface(
                                color = Slate100,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "+${cls.subjects.size - 4}",
                                    fontSize = 11.sp,
                                    color = Slate500,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Slate100)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onNavigate(Screen.STUDENTS) }
                        ) {
                            Text("View Students", fontSize = 12.sp, color = Slate700)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onSelectClassForAttendance(cls.id)
                                onNavigate(Screen.ATTENDANCE)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Take Attendance", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Add Class Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create New Class Cohort", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newClassName,
                        onValueChange = { newClassName = it },
                        label = { Text("Class Name (e.g. Grade 10-C)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_class_name")
                    )
                    OutlinedTextField(
                        value = newTeacher,
                        onValueChange = { newTeacher = it },
                        label = { Text("Class Teacher Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_class_teacher")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newRoom,
                            onValueChange = { newRoom = it },
                            label = { Text("Room No") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newCapacity,
                            onValueChange = { newCapacity = it },
                            label = { Text("Capacity") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newClassName.isNotBlank() && newTeacher.isNotBlank()) {
                            onAddClass(
                                newClassName,
                                newGradeLevel,
                                newSection,
                                newTeacher,
                                newRoom.ifBlank { "Room 201" },
                                newCapacity.toIntOrNull() ?: 30,
                                listOf("Mathematics", "Science", "English", "Social Studies")
                            )
                            showAddDialog = false
                            newClassName = ""
                            newTeacher = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                    modifier = Modifier.testTag("submit_add_class")
                ) {
                    Text("Create Class")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
