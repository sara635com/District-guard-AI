package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.DistrictGuardViewModel

val UNION_COUNCILS = listOf("UC-01 Alpha", "UC-02 Beta", "UC-03 Gamma", "UC-04 Delta")
val VILLAGES_MAP = mapOf(
    "UC-01 Alpha" to listOf("Village Cedar", "Village Pine", "Village Maple"),
    "UC-02 Beta" to listOf("Village Green", "Village Riverside", "Village Orchard"),
    "UC-03 Gamma" to listOf("Village Oak", "Village Hill", "Village Creek"),
    "UC-04 Delta" to listOf("Village Sunrise", "Village Valley", "Village Ridge")
)

val SYMPTOM_OPTIONS = listOf(
    "Fever", "Diarrhea", "Vomiting", "Cough",
    "Breathing difficulty", "Rash", "Headache", "Body aches"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFormScreen(
    viewModel: DistrictGuardViewModel,
    onNavigateBack: () -> Unit
) {
    val successMsg by viewModel.submissionSuccessMessage.collectAsState()

    var selectedUc by remember { mutableStateOf(UNION_COUNCILS.first()) }
    var selectedVillage by remember { mutableStateOf(VILLAGES_MAP[selectedUc]!!.first()) }
    var ucExpanded by remember { mutableStateOf(false) }
    var villageExpanded by remember { mutableStateOf(false) }

    val selectedSymptoms = remember { mutableStateListOf<String>() }
    var severity by remember { mutableStateOf("Mild") }

    var fever by remember { mutableStateOf(false) }
    var tempText by remember { mutableStateOf("") }
    var vomiting by remember { mutableStateOf(false) }
    var diarrhea by remember { mutableStateOf(false) }
    var cough by remember { mutableStateOf(false) }
    var breathingDifficulty by remember { mutableStateOf(false) }
    var rash by remember { mutableStateOf(false) }
    var headache by remember { mutableStateOf(false) }
    var bodyAches by remember { mutableStateOf(false) }

    var otherSymptoms by remember { mutableStateOf("") }
    var symptomDescription by remember { mutableStateOf("") }
    var hospitalized by remember { mutableStateOf(false) }

    var saveAsOffline by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anonymized Symptom Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_back")) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(HealthLightBg)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Success Feedback Dialog / Card
            if (successMsg != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("success_message_card"),
                    colors = CardDefaults.cardColors(containerColor = RiskLowBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = RiskLowGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Submission Success",
                                fontWeight = FontWeight.Bold,
                                color = RiskLowGreen,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = successMsg!!,
                            fontSize = 13.sp,
                            color = Color(0xFF166534)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                viewModel.clearSubmissionMessage()
                                onNavigateBack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RiskLowGreen)
                        ) {
                            Text("Return to Dashboard")
                        }
                    }
                }
            }

            // Notice Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                elevation = CardDefaults.cardElevation(0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "🔒 Anonymized Public Health Entry: Do NOT enter patient names, CNICs, or home addresses. Only location and symptom telemetry are stored.",
                    fontSize = 11.sp,
                    color = Color(0xFF1E40AF),
                    modifier = Modifier.padding(14.dp)
                )
            }

            // Location Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BentoBorderColor),
                elevation = CardDefaults.cardElevation(0.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("1. Area Location", fontWeight = FontWeight.Bold, color = HealthNavyPrimary)

                    // Union Council Dropdown
                    ExposedDropdownMenuBox(
                        expanded = ucExpanded,
                        onExpandedChange = { ucExpanded = !ucExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedUc,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Union Council") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ucExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("dropdown_uc")
                        )
                        ExposedDropdownMenu(
                            expanded = ucExpanded,
                            onDismissRequest = { ucExpanded = false }
                        ) {
                            UNION_COUNCILS.forEach { uc ->
                                DropdownMenuItem(
                                    text = { Text(uc) },
                                    onClick = {
                                        selectedUc = uc
                                        selectedVillage = VILLAGES_MAP[uc]?.first() ?: ""
                                        ucExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Village Dropdown
                    ExposedDropdownMenuBox(
                        expanded = villageExpanded,
                        onExpandedChange = { villageExpanded = !villageExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedVillage,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Village") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = villageExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("dropdown_village")
                        )
                        ExposedDropdownMenu(
                            expanded = villageExpanded,
                            onDismissRequest = { villageExpanded = false }
                        ) {
                            (VILLAGES_MAP[selectedUc] ?: emptyList()).forEach { v ->
                                DropdownMenuItem(
                                    text = { Text(v) },
                                    onClick = {
                                        selectedVillage = v
                                        villageExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Symptoms Checklist Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BentoBorderColor),
                elevation = CardDefaults.cardElevation(0.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("2. Primary Symptoms Observed", fontWeight = FontWeight.Bold, color = HealthNavyPrimary)

                    SYMPTOM_OPTIONS.chunked(2).forEach { rowSymptoms ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            rowSymptoms.forEach { symptom ->
                                val isChecked = selectedSymptoms.contains(symptom)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("checkbox_symptom_${symptom.lowercase().replace(" ", "_")}")
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                selectedSymptoms.add(symptom)
                                                when (symptom) {
                                                    "Fever" -> fever = true
                                                    "Vomiting" -> vomiting = true
                                                    "Diarrhea" -> diarrhea = true
                                                    "Cough" -> cough = true
                                                    "Breathing difficulty" -> breathingDifficulty = true
                                                    "Rash" -> rash = true
                                                    "Headache" -> headache = true
                                                    "Body aches" -> bodyAches = true
                                                }
                                            } else {
                                                selectedSymptoms.remove(symptom)
                                                when (symptom) {
                                                    "Fever" -> fever = false
                                                    "Vomiting" -> vomiting = false
                                                    "Diarrhea" -> diarrhea = false
                                                    "Cough" -> cough = false
                                                    "Breathing difficulty" -> breathingDifficulty = false
                                                    "Rash" -> rash = false
                                                    "Headache" -> headache = false
                                                    "Body aches" -> bodyAches = false
                                                }
                                            }
                                        }
                                    )
                                    Text(text = symptom, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = otherSymptoms,
                        onValueChange = { otherSymptoms = it },
                        label = { Text("Other Symptoms (Optional)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_other_symptoms")
                    )
                }
            }

            // Severity & Vitals
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BentoBorderColor),
                elevation = CardDefaults.cardElevation(0.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("3. Case Severity & Vitals", fontWeight = FontWeight.Bold, color = HealthNavyPrimary)

                    Text("Severity Level:", fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Mild", "Moderate", "Severe").forEach { level ->
                            FilterChip(
                                selected = severity == level,
                                onClick = { severity = level },
                                label = { Text(level) },
                                modifier = Modifier.testTag("chip_severity_$level")
                            )
                        }
                    }

                    if (fever) {
                        OutlinedTextField(
                            value = tempText,
                            onValueChange = { tempText = it },
                            label = { Text("Recorded Body Temperature (°C)") },
                            modifier = Modifier.fillMaxWidth().testTag("input_temperature")
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = hospitalized,
                            onCheckedChange = { hospitalized = it },
                            modifier = Modifier.testTag("checkbox_hospitalized")
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hospitalization Required / Admitted", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    }

                    OutlinedTextField(
                        value = symptomDescription,
                        onValueChange = { symptomDescription = it },
                        label = { Text("Symptom Notes & Field Observations") },
                        modifier = Modifier.fillMaxWidth().height(80.dp).testTag("input_description")
                    )
                }
            }

            // Offline Mode Toggle & Submit
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BentoBorderColor),
                elevation = CardDefaults.cardElevation(0.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Switch(
                            checked = saveAsOffline,
                            onCheckedChange = { saveAsOffline = it },
                            modifier = Modifier.testTag("switch_offline")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Submit in Offline Mode", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = if (saveAsOffline) "✓ Saved locally — Waiting for synchronization" else "✓ Direct district sync",
                                fontSize = 11.sp,
                                color = HealthTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.submitReport(
                                village = selectedVillage,
                                unionCouncil = selectedUc,
                                symptoms = selectedSymptoms.toList(),
                                symptomDescription = symptomDescription,
                                severity = severity,
                                fever = fever,
                                temperature = tempText.toDoubleOrNull(),
                                vomiting = vomiting,
                                diarrhea = diarrhea,
                                cough = cough,
                                breathingDifficulty = breathingDifficulty,
                                rash = rash,
                                headache = headache,
                                bodyAches = bodyAches,
                                otherSymptoms = otherSymptoms,
                                hospitalized = hospitalized,
                                isOffline = saveAsOffline
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_submit_report"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HealthNavySecondary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SUBMIT REPORT", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
