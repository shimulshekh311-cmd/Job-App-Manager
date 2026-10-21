package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DashboardStats
import com.example.ui.theme.ActionBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.StatusCanceled
import com.example.ui.theme.StatusFailed
import com.example.ui.theme.StatusPending

@Composable
fun DashboardStatsSection(
    stats: DashboardStats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Aggregate 4 Overview Counters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AggregateCounterCard(
                modifier = Modifier.weight(1f),
                title = "Total Apply",
                count = stats.totalApplications,
                accentColor = ActionBlue,
                icon = Icons.Default.Assignment
            )
            AggregateCounterCard(
                modifier = Modifier.weight(1f),
                title = "Total Exam",
                count = stats.totalExams,
                accentColor = EmeraldGreen,
                icon = Icons.Default.FactCheck
            )
            AggregateCounterCard(
                modifier = Modifier.weight(1f),
                title = "Total Failed",
                count = stats.totalFailed,
                accentColor = StatusFailed,
                icon = Icons.Default.Cancel
            )
            AggregateCounterCard(
                modifier = Modifier.weight(1f),
                title = "Canceled",
                count = stats.totalCanceled,
                accentColor = StatusCanceled,
                icon = Icons.Default.HourglassEmpty
            )
        }

        // 4 Exam Stage Counter Cards
        Text(
            text = "Stage Status Breakdown",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StageCounterCard(
                modifier = Modifier.weight(1f),
                title = "MCQ",
                icon = Icons.Default.Quiz,
                passed = stats.mcqPassed,
                failed = stats.mcqFailed,
                pending = stats.mcqPending
            )
            StageCounterCard(
                modifier = Modifier.weight(1f),
                title = "Written",
                icon = Icons.Default.RateReview,
                passed = stats.writtenPassed,
                failed = stats.writtenFailed,
                pending = stats.writtenPending
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StageCounterCard(
                modifier = Modifier.weight(1f),
                title = "Typing",
                icon = Icons.Default.Keyboard,
                passed = stats.typingPassed,
                failed = stats.typingFailed,
                pending = stats.typingPending
            )
            StageCounterCard(
                modifier = Modifier.weight(1f),
                title = "Viva",
                icon = Icons.Default.Mic,
                passed = stats.vivaPassed,
                failed = stats.vivaFailed,
                pending = stats.vivaPending
            )
        }
    }
}

@Composable
fun AggregateCounterCard(
    title: String,
    count: Int,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag("aggregate_card_${title.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                ),
                color = accentColor
            )
        }
    }
}

@Composable
fun StageCounterCard(
    title: String,
    icon: ImageVector,
    passed: Int,
    failed: Int,
    pending: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag("stage_card_${title.lowercase()}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .animateContentSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$title Status",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Passed, Failed, Pending Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusPill(label = "$passed Pass", color = EmeraldGreen)
                StatusPill(label = "$failed Fail", color = StatusFailed)
                StatusPill(label = "$pending Wait", color = StatusPending)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mini visual progress track
            val total = (passed + failed + pending).coerceAtLeast(1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFE9ECEF))
            ) {
                if (passed > 0) {
                    Box(
                        modifier = Modifier
                            .weight(passed.toFloat() / total)
                            .height(4.dp)
                            .background(EmeraldGreen)
                    )
                }
                if (failed > 0) {
                    Box(
                        modifier = Modifier
                            .weight(failed.toFloat() / total)
                            .height(4.dp)
                            .background(StatusFailed)
                    )
                }
                if (pending > 0) {
                    Box(
                        modifier = Modifier
                            .weight(pending.toFloat() / total)
                            .height(4.dp)
                            .background(StatusPending)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusPill(
    label: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
