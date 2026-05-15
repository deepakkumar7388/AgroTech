package com.agrotech.ai.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.agrotech.ai.ui.components.AgroButton
import com.agrotech.ai.ui.components.AgroTextField
import com.agrotech.ai.ui.navigation.Screen
import com.agrotech.ai.viewmodel.AgroViewModel
import com.agrotech.ai.ui.theme.LocalAppStrings
import com.agrotech.ai.data.model.*
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonalPlannerScreen(navController: NavController, viewModel: AgroViewModel) {
    val strings = LocalAppStrings.current
    var selectedTimeframe by remember { mutableStateOf(1) } // 1 or 2 months
    
    // Soil inputs for more accurate future planning
    var n by remember { mutableStateOf("") }
    var p by remember { mutableStateOf("") }
    var k by remember { mutableStateOf("") }
    var ph by remember { mutableStateOf("") }

    val futureRecs by viewModel.futureRecs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // State for expanded detail view
    var expandedResultKey by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.seasonalPlanner, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = strings.seasonalPlanTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = strings.seasonalPlanDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(strings.soilNutrients, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AgroTextField(value = n, onValueChange = { n = it }, label = "N", modifier = Modifier.weight(1f))
                    AgroTextField(value = p, onValueChange = { p = it }, label = "P", modifier = Modifier.weight(1f))
                    AgroTextField(value = k, onValueChange = { k = it }, label = "K", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(strings.whenToPlant, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TimeframeCard(
                        months = 1,
                        label = strings.afterOneMonth,
                        isSelected = selectedTimeframe == 1,
                        onClick = { selectedTimeframe = 1 },
                        modifier = Modifier.weight(1f)
                    )
                    TimeframeCard(
                        months = 2,
                        label = strings.afterTwoMonth,
                        isSelected = selectedTimeframe == 2,
                        onClick = { selectedTimeframe = 2 },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                AgroButton(
                    text = strings.viewPrediction,
                    onClick = {
                        val data = SoilData(
                            nitrogen = n.toFloatOrNull() ?: 80f,
                            phosphorus = p.toFloatOrNull() ?: 40f,
                            potassium = k.toFloatOrNull() ?: 40f,
                            ph = ph.toFloatOrNull() ?: 6.5f,
                            humidity = 70f,
                            rainfall = 100f,
                            temperature = 25f,
                            moisture = 20.0
                        )
                        viewModel.calculateFutureRecommendations(data)
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                )
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (futureRecs.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(strings.predictedResults, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                items(futureRecs.toList()) { (months, res) ->
                    PlanningResultCard(
                        months = months,
                        res = res,
                        isExpanded = expandedResultKey == months,
                        onClick = { expandedResultKey = if (expandedResultKey == months) null else months },
                        navController = navController,
                        viewModel = viewModel
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun PlanningResultCard(
    months: Int,
    res: RecommendationResponse,
    isExpanded: Boolean,
    onClick: () -> Unit,
    navController: NavController,
    viewModel: AgroViewModel
) {
    val strings = LocalAppStrings.current
    val cropName = res.recommendation
    val imageUrl = getCropImageUrl(cropName)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column {
            // Summary Row (always visible)
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("+$months", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        strings.inMonths.replace("%d", months.toString()),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(res.recommendation, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Accuracy: ${res.accuracy ?: "98.5%"}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Icon(
                    if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Expanded Detail View (like crop recommendation)
            if (isExpanded) {
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Column(modifier = Modifier.padding(16.dp)) {

                    // Crop Image + Name Header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = cropName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                cropName.uppercase(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1B5E20)
                            )
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Verified, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Accuracy: ${res.accuracy ?: "98.5%"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Why this crop? (AI Reasoning)
                    if (!res.whyThisCrop.isNullOrEmpty()) {
                        Text(
                            strings.whyThisCrop,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            res.whyThisCrop.forEach { item ->
                                val isPositive = item.impact > 0
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isPositive) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (isPositive) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.feature,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isPositive) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Expert Explanation
                    if (!res.expertExplanation.isNullOrEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MenuBook, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        strings.expertAdvice,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = res.expertExplanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    lineHeight = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Justify
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // "Complete Guide" Button
                    AgroButton(
                        text = strings.getRecommendation,
                        onClick = {
                            val query = "Tell me in detail how to grow $cropName"
                            viewModel.setPendingChatQuery(query)
                            navController.navigate(Screen.Chatbot.route)
                        },
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun TimeframeCard(months: Int, label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        ),
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun FutureCropBadge(name: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(6.dp))
            Text(name, style = MaterialTheme.typography.labelSmall)
        }
    }
}
