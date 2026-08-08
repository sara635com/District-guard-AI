package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClinicEntity
import com.example.data.model.HealthWorkerEntity
import com.example.data.model.SystemConfigEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.DistrictGuardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminConfigScreen(
    viewModel: DistrictGuardViewModel,
    onNavigateBack: () -> Unit
) {
    val config by viewModel.systemConfig.collectAsState()
    val workers by viewModel.allWorkers.collectAsState()
    val clinics by viewModel.allClinics.collectAsState()
    val reports by viewModel.allReports.collectAsState()

    var alertThresholdText by remember(config) { mutableStateOf((config?.alertThresholdScore ?: 60).toString()) }

    var newWorkerName by remember { mutableStateOf("") }
    var newWorkerRole by remember { mutableStateOf("Community Inspector") }
    var newWorkerArea by remember { mutableStateOf("UC-01 Alpha") }

    var newClinicName by remember { mutableStateOf("") }
    var newClinicUc by remember { mutableStateOf("UC-01 Alpha") }
    var newClinicPhone by remember { mutableStateOf("+92-42-9920000") }

    var showWorkerDialog by remember { mutableStateOf(false) }
    var showClinicDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Administration & Rules", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_back_admin")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HealthNavyPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(HealthLightBg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Risk Score Threshold Config
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BentoBorderColor),
                    elevation = CardDefaults.cardElevation(0.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("1. Risk Threshold Configuration", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Configure automatic alert triggering sensitivity score (0–100).", fontSize = 11.sp, color = HealthTextSecondary)

                        OutlinedTextField(
                            value = alertThresholdText,
                            onValueChange = { alertThresholdText = it },
                            label = { Text("Auto Alert Score Threshold (Default: 60)") },
                            modifier = Modifier.fillMaxWidth().testTag("input_alert_threshold")
                        )

                        Button(
                            onClick = {
                                val score = alertThresholdText.toIntOrNull() ?: 60
                                viewModel.saveConfig(
                                    (config ?: SystemConfigEntity()).copy(alertThresholdScore = score)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HealthNavySecondary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("btn_save_config")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Risk Configuration", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Health Worker Management
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BentoBorderColor),
                    elevation = CardDefaults.cardElevation(0.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("2. Health Workers (${workers.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { showWorkerDialog = true }, modifier = Modifier.testTag("btn_add_worker")) {
                                Icon(Icons.Default.Add, contentDescription = "Add Worker")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        workers.forEach { worker ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(worker.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${worker.role} • ${worker.assignedArea}", fontSize = 11.sp, color = HealthTextSecondary)
                                }
                                IconButton(onClick = { viewModel.deleteHealthWorker(worker.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RiskCriticalRed)
                                }
                            }
                        }
                    }
                }
            }

            // Clinics Management
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BentoBorderColor),
                    elevation = CardDefaults.cardElevation(0.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("3. District Clinics & Health Units (${clinics.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { showClinicDialog = true }, modifier = Modifier.testTag("btn_add_clinic")) {
                                Icon(Icons.Default.Add, contentDescription = "Add Clinic")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        clinics.forEach { clinic ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(clinic.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${clinic.unionCouncil} • ${clinic.contactPhone}", fontSize = 11.sp, color = HealthTextSecondary)
                                }
                                IconButton(onClick = { viewModel.deleteClinic(clinic.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RiskCriticalRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Worker Dialog
    if (showWorkerDialog) {
        AlertDialog(
            onDismissRequest = { showWorkerDialog = false },
            title = { Text("Register Health Worker") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newWorkerName,
                        onValueChange = { newWorkerName = it },
                        label = { Text("Worker Full Name") },
                        modifier = Modifier.testTag("input_new_worker_name")
                    )
                    OutlinedTextField(
                        value = newWorkerRole,
                        onValueChange = { newWorkerRole = it },
                        label = { Text("Role Title") }
                    )
                    OutlinedTextField(
                        value = newWorkerArea,
                        onValueChange = { newWorkerArea = it },
                        label = { Text("Assigned Union Council") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newWorkerName.isNotBlank()) {
                            viewModel.addHealthWorker(newWorkerName, newWorkerRole, newWorkerArea)
                            newWorkerName = ""
                            showWorkerDialog = false
                        }
                    },
                    modifier = Modifier.testTag("btn_confirm_add_worker")
                ) {
                    Text("Add Worker")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWorkerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Clinic Dialog
    if (showClinicDialog) {
        AlertDialog(
            onDismissRequest = { showClinicDialog = false },
            title = { Text("Register District Clinic") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newClinicName,
                        onValueChange = { newClinicName = it },
                        label = { Text("Clinic Name") },
                        modifier = Modifier.testTag("input_new_clinic_name")
                    )
                    OutlinedTextField(
                        value = newClinicUc,
                        onValueChange = { newClinicUc = it },
                        label = { Text("Union Council") }
                    )
                    OutlinedTextField(
                        value = newClinicPhone,
                        onValueChange = { newClinicPhone = it },
                        label = { Text("Emergency Phone") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newClinicName.isNotBlank()) {
                            viewModel.addClinic(newClinicName, newClinicUc, newClinicPhone)
                            newClinicName = ""
                            showClinicDialog = false
                        }
                    },
                    modifier = Modifier.testTag("btn_confirm_add_clinic")
                ) {
                    Text("Add Facility")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClinicDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
