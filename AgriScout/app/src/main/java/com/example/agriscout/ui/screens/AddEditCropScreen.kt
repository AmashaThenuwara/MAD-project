package com.example.agriscout.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agriscout.ui.theme.ShinyGradientStart
import com.example.agriscout.ui.theme.ShinyGradientEnd
import com.example.agriscout.ui.viewmodel.AgriViewModel
import kotlinx.coroutines.launch
import com.example.agriscout.ui.components.ShinyCard
import com.example.agriscout.ui.components.AnimatedButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCropScreen(
    farmId: Long,
    cropId: Long?,
    viewModel: AgriViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCropDetail: (Long) -> Unit
) {
    val scope = rememberCoroutineScope()
    
    var commonName by remember { mutableStateOf("") }
    var scientificName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var origin by remember { mutableStateOf("") }
    var soilType by remember { mutableStateOf("") }
    var fertilizerType by remember { mutableStateOf("Organic") }

    val categories = listOf("Vegetable", "Fruit", "Grain", "Legume", "Other")
    
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(cropId) {
        if (cropId != null) {
            isEditing = true
            val crop = viewModel.getCropById(cropId)
            crop?.let {
                commonName = it.commonName
                scientificName = it.scientificName
                category = it.category
                origin = it.origin
                soilType = it.soilType
                fertilizerType = it.fertilizerType
            }
        }
    }

    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(ShinyGradientStart, ShinyGradientEnd)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Crop" else "Add New Crop", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.background(primaryGradient)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            ShinyCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = commonName,
                        onValueChange = { commonName = it },
                        label = { Text("Common Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = scientificName,
                        onValueChange = { scientificName = it },
                        label = { Text("Scientific Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Text("Category", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 10.sp) },
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = origin,
                        onValueChange = { origin = it },
                        label = { Text("Origin / Native Region") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = soilType,
                        onValueChange = { soilType = it },
                        label = { Text("Soil Type") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Text("Fertilizer", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = fertilizerType == "Organic", onClick = { fertilizerType = "Organic" })
                            Text("Organic")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = fertilizerType == "Inorganic", onClick = { fertilizerType = "Inorganic" })
                            Text("Inorganic")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedButton(
                        onClick = {
                            if (commonName.isNotBlank()) {
                                scope.launch {
                                    if (isEditing && cropId != null) {
                                        val crop = viewModel.getCropById(cropId)
                                        crop?.let {
                                            viewModel.updateCrop(
                                                it.copy(
                                                    commonName = commonName,
                                                    scientificName = scientificName,
                                                    category = category,
                                                    origin = origin,
                                                    soilType = soilType,
                                                    fertilizerType = fertilizerType
                                                )
                                            )
                                        }
                                        onNavigateBack()
                                    } else {
                                        val newCropId = viewModel.insertCrop(
                                            farmId = farmId,
                                            commonName = commonName,
                                            scientificName = scientificName,
                                            category = category,
                                            origin = origin,
                                            soilType = soilType,
                                            fertilizerType = fertilizerType
                                        )
                                        onNavigateToCropDetail(newCropId)
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = commonName.isNotBlank()
                    ) {
                        Text(if (isEditing) "Save Changes" else "Save Crop", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
