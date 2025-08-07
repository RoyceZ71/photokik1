package com.mocha.photokik

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permission results
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Set status bar color
        window.statusBarColor = Color.TRANSPARENT
        
        // Request permissions
        requestPermissions()
        
        setContent {
            PhotoKikTheme {
                PhotoKikApp()
            }
        }
    }
    
    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        
        val permissionsToRequest = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest)
        }
    }
}

@Composable
fun PhotoKikTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = androidx.compose.ui.graphics.Color(0xFF9C27B0),
            secondary = androidx.compose.ui.graphics.Color(0xFFE91E63),
            background = androidx.compose.ui.graphics.Color(0xFF1A1A2E),
            surface = androidx.compose.ui.graphics.Color(0xFF16213E),
            onPrimary = androidx.compose.ui.graphics.Color.White,
            onSecondary = androidx.compose.ui.graphics.Color.White,
            onBackground = androidx.compose.ui.graphics.Color.White,
            onSurface = androidx.compose.ui.graphics.Color.White
        ),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoKikApp() {
    var currentScreen by remember { mutableStateOf("swipe") }
    val context = LocalContext.current
    
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            androidx.compose.ui.graphics.Color(0xFF6A1B9A),
            androidx.compose.ui.graphics.Color(0xFF3F51B5),
            androidx.compose.ui.graphics.Color(0xFF1A237E)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "PhotoKik",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Main Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                when (currentScreen) {
                    "swipe" -> SwipeScreen()
                    "gallery" -> GalleryScreen()
                    "trash" -> TrashScreen()
                    "settings" -> SettingsScreen()
                }
            }
            
            // Bottom Navigation
            BottomNavigationBar(
                currentScreen = currentScreen,
                onScreenChange = { currentScreen = it }
            )
        }
        
        // Floating Action Button
        FloatingActionButton(
            onClick = {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                context.startActivity(intent)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 80.dp),
            containerColor = androidx.compose.ui.graphics.Color(0xFFE91E63)
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = "Take Photo",
                tint = androidx.compose.ui.graphics.Color.White
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentScreen: String,
    onScreenChange: (String) -> Unit
) {
    NavigationBar(
        containerColor = androidx.compose.ui.graphics.Color(0x80000000),
        contentColor = androidx.compose.ui.graphics.Color.White,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        NavItem("swipe", "Swipe", Icons.Default.SwapHoriz, currentScreen, onScreenChange)
        NavItem("gallery", "Gallery", Icons.Default.PhotoLibrary, currentScreen, onScreenChange)
        NavItem("trash", "Trash", Icons.Default.Delete, currentScreen, onScreenChange)
        NavItem("settings", "Settings", Icons.Default.Settings, currentScreen, onScreenChange)
    }
}

@Composable
fun RowScope.NavItem(
    screen: String,
    label: String,
    icon: ImageVector,
    currentScreen: String,
    onScreenChange: (String) -> Unit
) {
    NavigationBarItem(
        selected = currentScreen == screen,
        onClick = { onScreenChange(screen) },
        icon = {
            Icon(
                icon,
                contentDescription = label,
                tint = if (currentScreen == screen) 
                    androidx.compose.ui.graphics.Color(0xFF9C27B0) 
                else 
                    androidx.compose.ui.graphics.Color.Gray
            )
        },
        label = {
            Text(
                label,
                fontSize = 12.sp,
                color = if (currentScreen == screen) 
                    androidx.compose.ui.graphics.Color(0xFF9C27B0) 
                else 
                    androidx.compose.ui.graphics.Color.Gray
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = androidx.compose.ui.graphics.Color(0xFF9C27B0),
            selectedTextColor = androidx.compose.ui.graphics.Color(0xFF9C27B0),
            unselectedIconColor = androidx.compose.ui.graphics.Color.Gray,
            unselectedTextColor = androidx.compose.ui.graphics.Color.Gray,
            indicatorColor = androidx.compose.ui.graphics.Color.Transparent
        )
    )
}

@Composable
fun SwipeScreen() {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var currentPhotoIndex by remember { mutableStateOf(0) }
    
    val samplePhotos = listOf(
        "Beautiful sunset at the beach",
        "Family dinner last weekend",
        "Document scan from work",
        "Vacation memories from mountains",
        "Concert photo from last night"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (samplePhotos.isNotEmpty() && currentPhotoIndex < samplePhotos.size) {
            Card(
                modifier = Modifier
                    .size(320.dp, 400.dp)
                    .offset(x = offsetX.value.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    if (offsetX.value > 150) {
                                        // Swipe right - Keep
                                        offsetX.animateTo(1000f, tween(200))
                                        currentPhotoIndex = (currentPhotoIndex + 1) % samplePhotos.size
                                        offsetX.snapTo(0f)
                                    } else if (offsetX.value < -150) {
                                        // Swipe left - Kik
                                        offsetX.animateTo(-1000f, tween(200))
                                        currentPhotoIndex = (currentPhotoIndex + 1) % samplePhotos.size
                                        offsetX.snapTo(0f)
                                    } else {
                                        // Return to center
                                        offsetX.animateTo(0f, tween(200))
                                    }
                                }
                            }
                        ) { _, dragAmount ->
                            scope.launch {
                                offsetX.snapTo(offsetX.value + dragAmount.x)
                            }
                        }
                    }
                    .shadow(16.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF2D2D44)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = "Photo",
                        modifier = Modifier.size(120.dp),
                        tint = androidx.compose.ui.graphics.Color(0xFF9C27B0)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = samplePhotos[currentPhotoIndex],
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = androidx.compose.ui.graphics.Color.White,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(40.dp)
                    ) {
                        Text(
                            text = "← Kik",
                            fontSize = 16.sp,
                            color = androidx.compose.ui.graphics.Color(0xFFFF5722)
                        )
                        Text(
                            text = "Keep →",
                            fontSize = 16.sp,
                            color = androidx.compose.ui.graphics.Color(0xFF4CAF50)
                        )
                    }
                }
            }
        } else {
            Text(
                text = "No more photos to review!",
                fontSize = 20.sp,
                color = androidx.compose.ui.graphics.Color.White,
                textAlign = TextAlign.Center
            )
        }
        
        // Action buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        offsetX.animateTo(-1000f, tween(200))
                        currentPhotoIndex = (currentPhotoIndex + 1) % samplePhotos.size
                        offsetX.snapTo(0f)
                    }
                },
                containerColor = androidx.compose.ui.graphics.Color(0xFFFF5722),
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Kik",
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        offsetX.animateTo(1000f, tween(200))
                        currentPhotoIndex = (currentPhotoIndex + 1) % samplePhotos.size
                        offsetX.snapTo(0f)
                    }
                },
                containerColor = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Keep",
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
fun GalleryScreen() {
    val samplePhotos = listOf(
        "Family vacation 2024",
        "Birthday celebration",
        "Work presentation",
        "Pet photos",
        "Food photography",
        "Nature landscape",
        "City skyline",
        "Concert memories"
    )
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "Your Gallery",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        items(samplePhotos) { photo ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = androidx.compose.ui.graphics.Color(0x40FFFFFF)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = "Photo",
                        modifier = Modifier.size(48.dp),
                        tint = androidx.compose.ui.graphics.Color(0xFF9C27B0)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = photo,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                        Text(
                            text = "Saved • ${(1..30).random()} days ago",
                            fontSize = 14.sp,
                            color = androidx.compose.ui.graphics.Color.Gray
                        )
                    }
                    
                    Icon(
                        Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = androidx.compose.ui.graphics.Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun TrashScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Delete,
            contentDescription = "Trash",
            modifier = Modifier.size(80.dp),
            tint = androidx.compose.ui.graphics.Color.Gray
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Trash is Empty",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.ui.graphics.Color.White
        )
        
        Text(
            text = "Photos you kik will appear here",
            fontSize = 16.sp,
            color = androidx.compose.ui.graphics.Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { /* Empty trash */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = androidx.compose.ui.graphics.Color(0xFFFF5722)
            )
        ) {
            Text("Empty Trash", color = androidx.compose.ui.graphics.Color.White)
        }
    }
}

@Composable
fun SettingsScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        item {
            SettingItem(
                title = "Swipe Sensitivity",
                description = "Adjust how sensitive swiping is",
                icon = Icons.Default.TouchApp
            )
        }
        
        item {
            SettingItem(
                title = "Auto Categorize",
                description = "Automatically organize your photos",
                icon = Icons.Default.AutoAwesome
            )
        }
        
        item {
            SettingItem(
                title = "Delete Blurry Photos",
                description = "Automatically remove blurry images",
                icon = Icons.Default.BlurOn
            )
        }
        
        item {
            SettingItem(
                title = "Storage Optimization",
                description = "Optimize photo storage usage",
                icon = Icons.Default.Storage
            )
        }
        
        item {
            SettingItem(
                title = "Privacy Policy",
                description = "View our privacy policy",
                icon = Icons.Default.Security
            )
        }
        
        item {
            SettingItem(
                title = "About PhotoKik",
                description = "Version 1.0.0",
                icon = Icons.Default.Info
            )
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    description: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color(0x40FFFFFF)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = androidx.compose.ui.graphics.Color(0xFF9C27B0)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = androidx.compose.ui.graphics.Color.White
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = androidx.compose.ui.graphics.Color.Gray
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = androidx.compose.ui.graphics.Color.Gray
            )
        }
    }
}
