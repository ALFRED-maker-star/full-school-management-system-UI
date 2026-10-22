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
import com.example.model.*
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.util.UUID

@Composable
fun TimetableScreen(
    user: UserAccount,
    classes: List<ClassRoom>,
    teachers: List<Teacher>,
    timetableSlots: List<TimetableSlot>,
    onAddSlot: (TimetableSlot) -> Unit,
    onRemoveSlot: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    var selectedDay by remember { mutableStateOf("Monday") }
    var selectedClassFilter by remember { mutableStateOf("All Classes") }
    var showAddDialog by remember { mutableStateOf(false) }

    // Add slot state
    var newPeriodNumber by remember { mutableStateOf("1") }
    var newTimeRange by remember { mutableStateOf("08:30 - 09:20") }
    var newClassId by remember { mutableStateOf(classes.firstOrNull()?.id ?: "cls_10a") }
    var newSubject by remember { mutableStateOf("Mathematics") }
    var newTeacherName by remember { mutableStateOf(teachers.firstOrNull()?.fullName ?: "Prof. Marcus Reed") }
    var newRoom by remember { mutableStateOf("Lab 204") }

    // Filter slots according to role and selected day
    val filteredSlots = timetableSlots.filter { slot ->
        val matchesDay = slot.dayOfWeek.equals(selectedDay, ignoreCase = true)
        val matchesRole = when (user.role) {
            Role.ADMIN -> selectedClassFilter == "All Classes" || slot.className == selectedClassFilter
            Role.TEACHER -> slot.teacherName.contains("Reed") || slot.classId in user.assignedClassIds
            Role.STUDENT_PARENT -> slot.classId == "cls_10a"
        }
        matchesDay && matchesRole
    }.sortedBy { it.periodNumber }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "Academic Timetable & Schedules",
                subtitle = when (user.role) {
                    Role.ADMIN -> "Master institution schedule setup and allocation"
                    Role.TEACHER -> "Your personal faculty teaching schedule"
                    Role.STUDENT_PARENT -> "Grade 10-A weekly lecture timetable"
                },
                actionButtonText = if (user.role == Role.ADMIN) "+ Add Period" else null,
                actionButtonIcon = Icons.Default.Add,
                onActionClick = { showAddDialog = true }
            )
        }

        // Day of Week Tabs
        item {
            Surface(
                color = Slate200,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    daysOfWeek.forEach { day ->
                        val isSelected = selectedDay == day
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Navy800 else Color.Transparent)
                                .clickable { selectedDay = day }
                                .padding(vertical = 8.dp)
                                .testTag("timetable_day_${day.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.take(3),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Slate700
                            )
                        }
                    }
                }
            }
        }

        // Admin Filter by Class if Admin
        if (user.role == Role.ADMIN) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filter by Cohort:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate600)
                    var classFilterExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { classFilterExpanded = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(selectedClassFilter, fontSize = 12.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(
                            expanded = classFilterExpanded,
                            onDismissRequest = { classFilterExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Classes") },
                                onClick = {
                                    selectedClassFilter = "All Classes"
                                    classFilterExpanded = false
                                }
                            )
                            classes.forEach { cls ->
                                DropdownMenuItem(
                                    text = { Text(cls.name) },
                                    onClick = {
                                        selectedClassFilter = cls.name
                                        classFilterExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Timetable Slots List
        if (filteredSlots.isEmpty()) {
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.EventBusy, contentDescription = null, tint = Slate400, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No scheduled lectures for $selectedDay", fontWeight = FontWeight.Bold, color = Slate700)
                        Text("Use '+ Add Period' to assign periods to this day.", fontSize = 12.sp, color = Slate500)
                    }
                }
            }
        } else {
            items(filteredSlots) { slot ->
                ElevatedCard(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().testTag("timetable_slot_${slot.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Navy100),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("P${slot.periodNumber}", fontWeight = FontWeight.Bold, color = Navy800, fontSize = 14.sp)
                                Text("Period", fontSize = 9.sp, color = Navy800.copy(alpha = 0.7f))
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = slot.subject,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Slate900
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusBadge(text = slot.className, color = Teal700, backgroundColor = Teal100)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = Slate400, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(slot.timeRange, fontSize = 12.sp, color = Slate600)
                                Spacer(modifier = Modifier.width(10.dp))
                                Icon(Icons.Default.Room, contentDescription = null, tint = Slate400, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(slot.roomNumber, fontSize = 12.sp, color = Slate600)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Faculty: ${slot.teacherName}",
                                fontSize = 11.sp,
                                color = Teal700,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (user.role == Role.ADMIN) {
                            IconButton(onClick = { onRemoveSlot(slot.id) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = DangerRed, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Slot Dialog (Admin Setup)
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Schedule Timetable Slot", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Day: $selectedDay", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Navy800)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newPeriodNumber,
                            onValueChange = { newPeriodNumber = it },
                            label = { Text("Period #") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newTimeRange,
                            onValueChange = { newTimeRange = it },
                            label = { Text("Time Range") },
                            modifier = Modifier.weight(2f)
                        )
                    }

                    // Class Dropdown
                    var classDrop by remember { mutableStateOf(false) }
                    val currentCls = classes.find { it.id == newClassId }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { classDrop = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Class: ${currentCls?.name ?: "Select Class"}")
                        }
                        DropdownMenu(expanded = classDrop, onDismissRequest = { classDrop = false }) {
                            classes.forEach { cls ->
                                DropdownMenuItem(
                                    text = { Text(cls.name) },
                                    onClick = {
                                        newClassId = cls.id
                                        classDrop = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newSubject,
                        onValueChange = { newSubject = it },
                        label = { Text("Subject (e.g. Physics)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newTeacherName,
                        onValueChange = { newTeacherName = it },
                        label = { Text("Assigned Teacher") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newRoom,
                        onValueChange = { newRoom = it },
                        label = { Text("Classroom / Lab Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cls = classes.find { it.id == newClassId }
                        val slot = TimetableSlot(
                            id = "tt_${UUID.randomUUID().toString().take(6)}",
                            dayOfWeek = selectedDay,
                            periodNumber = newPeriodNumber.toIntOrNull() ?: 1,
                            timeRange = newTimeRange,
                            classId = newClassId,
                            className = cls?.name ?: "Grade 10-A",
                            subject = newSubject,
                            teacherName = newTeacherName,
                            roomNumber = newRoom
                        )
                        onAddSlot(slot)
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                    modifier = Modifier.testTag("submit_add_slot")
                ) {
                    Text("Add Slot")
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
