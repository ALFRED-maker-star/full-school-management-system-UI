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
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun FeesScreen(
    user: UserAccount,
    students: List<Student>,
    feeRecords: List<FeeRecord>,
    onRecordPayment: (feeId: String, amount: Double, method: String, ref: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Mode switcher: Admin View vs Parent/Student View
    var activeTab by remember {
        mutableStateOf(if (user.role == Role.STUDENT_PARENT) "My Fees" else "Management")
    }

    var showRecordPaymentDialog by remember { mutableStateOf(false) }
    var selectedFeeForPayment by remember { mutableStateOf<FeeRecord?>(null) }
    var paymentAmountText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Credit Card") }
    var paymentReference by remember { mutableStateOf("") }

    var viewingReceipt by remember { mutableStateOf<FeeReceipt?>(null) }

    // Parent target student
    val parentStudent = if (user.role == Role.STUDENT_PARENT) {
        students.find { it.id == user.studentId } ?: students.firstOrNull()
    } else null

    val parentFeeRecords = feeRecords.filter { it.studentId == parentStudent?.id }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header & Tab switcher
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Fees & Finance Portal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Tuition billing, payment tracking, and digital receipts",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }

                Surface(
                    color = Slate200,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(3.dp)) {
                        listOf("Management", "My Fees").forEach { tab ->
                            val isSelected = activeTab == tab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Navy800 else Color.Transparent)
                                    .clickable { activeTab = tab }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("fee_tab_${tab.lowercase().replace(" ", "_")}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab,
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

        if (activeTab == "Management") {
            // Admin-facing Management Overview
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Fees Invoiced",
                        value = "$525,000",
                        subtitle = "Term 1 billings across classes",
                        icon = Icons.Default.RequestQuote,
                        iconBgColor = Navy100,
                        iconTint = Navy800,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Collected",
                        value = "$482,500",
                        subtitle = "91.9% collection rate",
                        icon = Icons.Default.AccountBalanceWallet,
                        iconBgColor = SuccessGreenLight,
                        iconTint = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                SectionHeader(
                    title = "Class Billings & Outstanding Accounts",
                    subtitle = "Monitor collections and issue manual receipts",
                    actionButtonText = "+ Record Payment",
                    actionButtonIcon = Icons.Default.AddCard,
                    onActionClick = {
                        selectedFeeForPayment = feeRecords.firstOrNull()
                        paymentAmountText = "250"
                        showRecordPaymentDialog = true
                    }
                )
            }

            // Fee records list
            items(feeRecords) { fee ->
                val remaining = (fee.totalAmount - fee.paidAmount).coerceAtLeast(0.0)
                ElevatedCard(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().testTag("fee_record_${fee.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = fee.studentName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Slate900
                                )
                                Text(
                                    text = "${fee.className} • ${fee.title}",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                            }
                            StatusBadge(
                                text = fee.status,
                                color = when (fee.status) {
                                    "Paid" -> SuccessGreen
                                    "Overdue" -> DangerRed
                                    else -> Amber600
                                },
                                backgroundColor = when (fee.status) {
                                    "Paid" -> SuccessGreenLight
                                    "Overdue" -> DangerRedLight
                                    else -> Amber100
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Invoiced: $${fee.totalAmount.toInt()}", fontSize = 12.sp, color = Slate600)
                                Text("Paid: $${fee.paidAmount.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Outstanding Balance", fontSize = 11.sp, color = Slate500)
                                Text(
                                    text = "$${remaining.toInt()}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (remaining > 0) Amber600 else SuccessGreen
                                )
                            }
                        }

                        if (remaining > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    selectedFeeForPayment = fee
                                    paymentAmountText = remaining.toInt().toString()
                                    showRecordPaymentDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Collect Payment", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else {
            // Parent / Student Facing Fee View
            val feeItem = parentFeeRecords.firstOrNull() ?: feeRecords.firstOrNull()
            val balance = feeItem?.let { it.totalAmount - it.paidAmount } ?: 0.0

            item {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (balance > 0) Amber50 else Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("parent_fee_balance_card")
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Student Tuition Dues", fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Slate600)
                                Text(
                                    text = "$$balance",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (balance > 0) Amber600 else SuccessGreen
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (balance > 0) Amber100 else SuccessGreenLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = if (balance > 0) Amber600 else SuccessGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (balance > 0) "Payment due date: November 15, 2026 (Grace period active)" else "All academic accounts are fully paid for Term 1",
                            fontSize = 12.sp,
                            color = Slate600
                        )

                        if (balance > 0 && feeItem != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    selectedFeeForPayment = feeItem
                                    paymentAmountText = balance.toInt().toString()
                                    showRecordPaymentDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("parent_pay_now_btn")
                            ) {
                                Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pay Online ($$balance)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Itemized Fee Breakdown
            item {
                SectionHeader(title = "Itemized Fee Schedule (Term 1)")
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        listOf(
                            "Tuition & Instruction" to "$1,000.00",
                            "Science & Computer Lab Fee" to "$150.00",
                            "Library & Digital Resources" to "$100.00"
                        ).forEachIndexed { i, (item, cost) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item, fontSize = 13.sp, color = Slate700)
                                Text(cost, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            }
                            if (i < 2) HorizontalDivider(color = Slate100)
                        }
                    }
                }
            }

            // Payment History & Receipts
            item {
                SectionHeader(title = "Payment History & Issued Receipts")
            }

            items(feeItem?.receipts ?: emptyList()) { receipt ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewingReceipt = receipt }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreenLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(receipt.receiptNo, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                                Text("${receipt.date} • ${receipt.paymentMethod}", fontSize = 11.sp, color = Slate500)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$${receipt.amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate800)
                            IconButton(onClick = { viewingReceipt = receipt }) {
                                Icon(Icons.Default.Visibility, contentDescription = "View Receipt", tint = Slate400, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Record Payment Dialog
    if (showRecordPaymentDialog && selectedFeeForPayment != null) {
        val fee = selectedFeeForPayment!!
        AlertDialog(
            onDismissRequest = { showRecordPaymentDialog = false },
            title = { Text("Record Fee Payment", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Payment for: ${fee.studentName} (${fee.className})", fontSize = 13.sp, color = Slate600)
                    OutlinedTextField(
                        value = paymentAmountText,
                        onValueChange = { paymentAmountText = it },
                        label = { Text("Amount ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
                    )

                    Text("Payment Method:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    listOf("Credit Card", "Bank Transfer", "Cash Deposit").forEach { method ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { paymentMethod = method }
                        ) {
                            RadioButton(
                                selected = paymentMethod == method,
                                onClick = { paymentMethod = method }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(method, fontSize = 13.sp)
                        }
                    }

                    OutlinedTextField(
                        value = paymentReference,
                        onValueChange = { paymentReference = it },
                        label = { Text("Transaction / Reference # (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = paymentAmountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            onRecordPayment(fee.id, amt, paymentMethod, paymentReference)
                            showRecordPaymentDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                    modifier = Modifier.testTag("confirm_payment_btn")
                ) {
                    Text("Confirm Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecordPaymentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // View Receipt Modal
    viewingReceipt?.let { rec ->
        AlertDialog(
            onDismissRequest = { viewingReceipt = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Payment Receipt", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("EduAdmin Academy Bursar Office", fontSize = 12.sp, color = Slate500)
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Receipt #", color = Slate600)
                        Text(rec.receiptNo, fontWeight = FontWeight.Bold)
                    }
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Date", color = Slate600)
                        Text(rec.date)
                    }
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Amount Paid", color = Slate600)
                        Text("$${rec.amount}", fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Method", color = Slate600)
                        Text(rec.paymentMethod)
                    }
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Reference", color = Slate600)
                        Text(rec.referenceNo)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewingReceipt = null }) {
                    Text("Close")
                }
            }
        )
    }
}
