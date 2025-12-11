package com.justbaat.mybishnoiapp.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportBottomSheet(
    onDismiss: () -> Unit,
    onSubmitReport: (reportType: String, message: String) -> Unit,
    isLoading: Boolean = false
) {
    var selectedReportType by remember { mutableStateOf<ReportType?>(null) }
    var reportMessage by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Report Post",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Why are you reporting this post?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Report types
            Column(
                modifier = Modifier
                    .selectableGroup()
                    .padding(bottom = 16.dp)
            ) {
                ReportType.values().forEach { reportType ->
                    ReportTypeItem(
                        reportType = reportType,
                        selected = selectedReportType == reportType,
                        onClick = { selectedReportType = reportType }
                    )
                }
            }

            // Optional message
            OutlinedTextField(
                value = reportMessage,
                onValueChange = { reportMessage = it },
                label = { Text("Additional details (optional)") },
                placeholder = { Text("Tell us more about the issue...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        selectedReportType?.let { type ->
                            onSubmitReport(type.value, reportMessage)
                        }
                    },
                    enabled = selectedReportType != null && !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Submit Report")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReportTypeItem(
    reportType: ReportType,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            imageVector = reportType.icon,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = reportType.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = reportType.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

enum class ReportType(
    val value: String,
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    SPAM(
        "spam",
        "Spam",
        "Repetitive or irrelevant content",
        Icons.Default.Block
    ),
    HARASSMENT(
        "harassment",
        "Harassment or Bullying",
        "Targeting or attacking someone",
        Icons.Default.Warning
    ),
    INAPPROPRIATE(
        "inappropriate",
        "Inappropriate Content",
        "Adult or offensive material",
        Icons.Default.RemoveCircle
    ),
    VIOLENCE(
        "violence",
        "Violence or Dangerous",
        "Content promoting harm",
        Icons.Default.Dangerous
    ),
    HATE_SPEECH(
        "hate_speech",
        "Hate Speech",
        "Attacking based on identity",
        Icons.Default.Report
    ),
    FALSE_INFO(
        "false_information",
        "False Information",
        "Misleading or fake content",
        Icons.Default.Info
    ),
    OTHER(
        "other",
        "Other",
        "Something else",
        Icons.Default.MoreHoriz
    )
}
