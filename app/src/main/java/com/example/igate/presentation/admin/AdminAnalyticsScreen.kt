package com.example.igate.presentation.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igate.domain.model.GateBranch
import com.example.igate.theme.AcademicNegative
import com.example.igate.theme.AcademicSuccess
import com.example.igate.theme.AcademicWarning
import com.example.igate.theme.BrandBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnalyticsScreen(onBackClick: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Platform Analytics", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Institute-wide KPIs � Classplus Style", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // KPI Row 1
            item {
                Text("Revenue & Subscriptions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PlatformKPICard("? 34.2L", "Total Revenue", "This Month", Icons.Filled.CurrencyRupee, AcademicSuccess, Modifier.weight(1f))
                    PlatformKPICard("12,450", "Active Students", "Currently enrolled", Icons.Filled.Group, BrandBlue, Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PlatformKPICard("8,230", "Paid Students", "Active plans", Icons.Filled.CardMembership, AcademicWarning, Modifier.weight(1f))
                    PlatformKPICard("4,220", "Free Tier", "Trial students", Icons.Filled.PersonOutline, MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f))
                }
            }

            // Engagement Row
            item {
                Text("Platform Engagement", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PlatformKPICard("78%", "Daily Active", "Students active today", Icons.AutoMirrored.Filled.TrendingUp, AcademicSuccess, Modifier.weight(1f))
                    PlatformKPICard("124K hrs", "Study Hours", "Total this month", Icons.Filled.AccessTime, BrandBlue, Modifier.weight(1f))
                    PlatformKPICard("34.2K", "Tests Taken", "All time", Icons.Filled.AssignmentTurnedIn, AcademicWarning, Modifier.weight(1f))
                }
            }

            // Weekly Revenue Chart
            item {
                Text("Weekly Revenue (₹ Thousands)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    val revenueData = listOf(28f, 42f, 35f, 56f, 48f, 62f, 70f)
                    val barColor = AcademicSuccess
                    val trackColor = MaterialTheme.colorScheme.surfaceVariant
                    val maxVal = 80f
                    Canvas(Modifier.fillMaxSize().padding(20.dp)) {
                        val cw = size.width; val ch = size.height - 20f
                        val bw = cw / (revenueData.size * 2f); val gap = bw
                        revenueData.forEachIndexed { i, v ->
                            val x = (i * (bw + gap)) + (gap / 2f)
                            val bh = (v / maxVal) * ch; val y = ch - bh
                            drawRoundRect(trackColor, Offset(x, 0f), Size(bw, ch), CornerRadius(bw / 2f))
                            drawRoundRect(barColor, Offset(x, y), Size(bw, bh), CornerRadius(bw / 2f))
                        }
                    }
                }
            }

            // Branch-wise student split
            item {
                Text("Branch-wise Enrollment", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                val branchData = listOf(
                    Triple(GateBranch.CS, 5200, 0.42f),
                    Triple(GateBranch.ME, 2100, 0.17f),
                    Triple(GateBranch.EE, 1950, 0.16f),
                    Triple(GateBranch.EC, 1600, 0.13f),
                    Triple(GateBranch.CE, 950, 0.08f),
                    Triple(GateBranch.DA, 650, 0.05f),
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    branchData.forEach { (branch, count, frac) ->
                        BranchBar(branch.displayName, branch.code, count, frac)
                    }
                }
            }

            // Mock test quality
            item {
                Text("Mock Test Performance", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PlatformKPICard("72.4%", "Avg Accuracy", "Across all mocks", Icons.Filled.BarChart, BrandBlue, Modifier.weight(1f))
                    PlatformKPICard("52 mins", "Avg Duration", "Per test attempt", Icons.Filled.Timer, AcademicWarning, Modifier.weight(1f))
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun PlatformKPICard(value: String, title: String, subtitle: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.1f)), Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            }
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BranchBar(name: String, code: String, count: Int, fraction: Float) {
    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(10.dp), MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("$code � $name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("$count students", fontSize = 11.sp, color = BrandBlue, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)), color = BrandBlue, trackColor = MaterialTheme.colorScheme.surfaceVariant)
        }
    }
}
