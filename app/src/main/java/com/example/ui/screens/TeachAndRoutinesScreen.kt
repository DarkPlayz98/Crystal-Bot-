package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AgentShape
import com.example.data.model.TeachableSkillRoutine
import com.example.ui.components.AgentAvatarView
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CrystalCyanPrimary
import com.example.ui.theme.CrystalSkyBlue
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TeachAndRoutinesScreen(
    routines: List<TeachableSkillRoutine>,
    onToggleRoutine: (String, Boolean) -> Unit,
    onRunRoutineNow: (String) -> Unit,
    onCreateNewRoutine: (String, String, String, List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showTeachDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .padding(14.dp)
            .testTag("teach_and_routines_screen")
    ) {
        // Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Teach-a-Task & Routines",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Automated cron schedules & demonstrated skills",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = { showTeachDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CrystalCyanPrimary),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("teach_new_task_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = ObsidianBlack,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Demonstrate Task", color = ObsidianBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Routine List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(routines, key = { it.id }) { routine ->
                RoutineCardItem(
                    routine = routine,
                    onToggle = { onToggleRoutine(routine.id, it) },
                    onRunNow = { onRunRoutineNow(routine.id) }
                )
            }
        }
    }

    if (showTeachDialog) {
        TeachTaskDialog(
            onDismiss = { showTeachDialog = false },
            onSave = { name, trigger, desc, steps ->
                onCreateNewRoutine(name, trigger, desc, steps)
                showTeachDialog = false
                Toast.makeText(context, "Skill saved and scheduled!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun RoutineCardItem(
    routine: TeachableSkillRoutine,
    onToggle: (Boolean) -> Unit,
    onRunNow: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(14.dp)
            .testTag("routine_card_${routine.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AgentAvatarView(shape = routine.assignedAgentShape, size = 32.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = routine.title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Assigned to @${routine.assignedAgentName}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Switch(
                checked = routine.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ObsidianBlack,
                    checkedTrackColor = CrystalCyanPrimary,
                    uncheckedTrackColor = DarkSurfaceHighlight
                ),
                modifier = Modifier.testTag("toggle_routine_${routine.id}")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Trigger & Schedule badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkSurfaceHighlight)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = CrystalSkyBlue,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = routine.triggerSchedule,
                        color = CrystalSkyBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF22C55E).copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = routine.lastRunText,
                    color = Color(0xFF22C55E),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = routine.description,
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${routine.recordedSteps.size} automated steps",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            Button(
                onClick = onRunNow,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHighlight),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("run_routine_now_${routine.id}")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CrystalCyanPrimary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Run Now", color = CrystalCyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Expanded Step-by-Step recorded actions
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceCard)
                    .padding(10.dp)
            ) {
                Text("RECORDED WORKFLOW DEMONSTRATION", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                routine.recordedSteps.forEachIndexed { idx, step ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("${idx + 1}. ", color = CrystalCyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(step, color = TextPrimary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TeachTaskDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, trigger: String, desc: String, steps: List<String>) -> Unit
) {
    var taskName by remember { mutableStateOf("") }
    var taskTrigger by remember { mutableStateOf("Every day at 8:00 AM") }
    var isRecording by remember { mutableStateOf(false) }
    val simulatedSteps = remember {
        listOf(
            "1. Open Headless Chromium to https://app.hubspot.com",
            "2. Extract recent leads from Deal pipeline",
            "3. Run Python enrichment script in sandbox terminal",
            "4. Send consolidated digest to #general on Slack"
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceElevated)
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                .padding(20.dp)
                .testTag("teach_task_dialog")
        ) {
            Text(
                text = "Teach-a-Task Studio",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Demonstrate actions on cloud computer to train an agent.",
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = taskName,
                onValueChange = { taskName = it },
                label = { Text("Task / Routine Name", color = TextMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = taskTrigger,
                onValueChange = { taskTrigger = it },
                label = { Text("Trigger Schedule (Cron or Event)", color = TextMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Screen Recording Demo Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, if (isRecording) Color(0xFFEF4444) else BorderSubtle, RoundedCornerShape(10.dp))
                    .clickable { isRecording = !isRecording }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        tint = if (isRecording) Color(0xFFEF4444) else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRecording) "Recording screen demonstration... (Tap to finish)" else "Tap to Record Screen Demonstration",
                        color = if (isRecording) Color(0xFFEF4444) else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Detected Steps Preview
            Text("CAPTURED WORKFLOW STEPS:", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            simulatedSteps.forEach { step ->
                Text(step, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(vertical = 1.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextSecondary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val finalName = taskName.ifBlank { "Custom Web & Data Routine" }
                        onSave(finalName, taskTrigger, "Automated workflow learned via screen demonstration.", simulatedSteps)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrystalCyanPrimary)
                ) {
                    Text("Save Routine", color = ObsidianBlack, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
