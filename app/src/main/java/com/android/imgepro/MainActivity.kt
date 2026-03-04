package com.android.imgepro

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.automirrored.filled.AlignHorizontalLeft
import androidx.compose.material.icons.automirrored.filled.AlignHorizontalRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.android.imgepro.ui.theme.ImgeProTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImgeProTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.Login) }

                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF080808)) {
                    when (currentScreen) {
                        AppScreen.Login -> LoginScreen { currentScreen = AppScreen.Profile }
                        AppScreen.Profile -> ProfileCreationScreen { currentScreen = AppScreen.Home }
                        AppScreen.Home -> ImageEnhancerApp()
                    }
                }
            }
        }
    }
}

enum class AppScreen { Login, Profile, Home }

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val accentColor = Color(0xFFD0BCFF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "IMAGE PRO",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            letterSpacing = 8.sp,
            color = Color.White
        )
        Text(
            "ENHANCE YOUR VISION",
            style = MaterialTheme.typography.labelMedium,
            color = accentColor,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(64.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = { if (email.isNotEmpty() && password.isNotEmpty()) onLoginSuccess() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Text("LOGIN", fontWeight = FontWeight.Bold, color = Color.Black, letterSpacing = 2.sp)
        }
    }
}

@Composable
fun ProfileCreationScreen(onProfileComplete: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    val accentColor = Color(0xFFD0BCFF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "SET UP PROFILE",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Tell us about yourself", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = { if (name.isNotEmpty()) onProfileComplete() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Text("GET STARTED", fontWeight = FontWeight.Bold, color = Color.Black, letterSpacing = 1.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageEnhancerApp() {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFilter by remember { mutableStateOf(FilterType.None) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var overlayText by remember { mutableStateOf("") }
    var textAlignment by remember { mutableStateOf(Alignment.BottomCenter) }
    var isComparing by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf(EditTab.Filters) }
    var showTextDialog by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            selectedFilter = FilterType.None
            rotation = 0f
            zoomScale = 1f
            overlayText = ""
            textAlignment = Alignment.BottomCenter
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showCamera = true
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val darkBackground = Color(0xFF080808)
    val accentColor = Color(0xFFD0BCFF)
    val surfaceColor = Color(0xFF151515)

    if (showCamera) {
        CameraWithFilterScreen(
            onImageCaptured = { uri, filter ->
                imageUri = uri
                selectedFilter = filter
                rotation = 0f
                zoomScale = 1f
                overlayText = ""
                textAlignment = Alignment.BottomCenter
                showCamera = false
            },
            onDismiss = { showCamera = false },
            accentColor = accentColor
        )
    } else {
        if (showTextDialog) {
            AlertDialog(
                onDismissRequest = { showTextDialog = false },
                title = { Text("Add Text Overlay") },
                text = {
                    TextField(
                        value = overlayText,
                        onValueChange = { overlayText = it },
                        placeholder = { Text("Enter text here...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showTextDialog = false }) { Text("Done") }
                }
            )
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = darkBackground,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "IMAGE PRO",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        if (imageUri != null) {
                            IconButton(onClick = { imageUri = null }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                        }
                    },
                    actions = {
                        if (imageUri != null) {
                            IconButton(onClick = {
                                scope.launch {
                                    saveImageToGallery(context, imageUri!!, selectedFilter, rotation, zoomScale, overlayText, textAlignment)
                                }
                            }) {
                                Icon(Icons.Default.Save, contentDescription = "Save", tint = accentColor)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .drawBehind {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(accentColor.copy(alpha = 0.05f), Color.Transparent),
                                center = center,
                                radius = size.maxDimension / 2f
                            )
                        )
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = imageUri,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.9f) togetherWith
                                    fadeOut(animationSpec = tween(500)) + scaleOut(targetScale = 1.1f)
                        },
                        label = "MainContent"
                    ) { uri ->
                        if (uri != null) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .shadow(32.dp, RoundedCornerShape(20.dp), spotColor = accentColor)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.Black)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onPress = {
                                                    isComparing = true
                                                    tryAwaitRelease()
                                                    isComparing = false
                                                }
                                            )
                                        }
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .rotate(rotation)
                                            .scale(zoomScale),
                                        contentScale = ContentScale.Fit,
                                        colorFilter = if (isComparing) null else selectedFilter.colorFilter
                                    )

                                    if (overlayText.isNotEmpty() && !isComparing) {
                                        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                            Text(
                                                text = overlayText,
                                                modifier = Modifier
                                                    .align(textAlignment)
                                                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                                color = Color.White,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                this@Column.AnimatedVisibility(
                                    visible = !isComparing,
                                    enter = slideInVertically { it } + fadeIn(),
                                    exit = slideOutVertically { it } + fadeOut(),
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                ) {
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier.padding(bottom = 24.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.TouchApp, null, tint = accentColor, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "Hold to Compare",
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    StartOption(
                                        icon = Icons.Default.PhotoLibrary,
                                        label = "Gallery",
                                        accentColor = accentColor,
                                        darkBackground = darkBackground,
                                        onClick = { galleryLauncher.launch("image/*") }
                                    )
                                    StartOption(
                                        icon = Icons.Default.PhotoCamera,
                                        label = "Camera",
                                        accentColor = accentColor,
                                        darkBackground = darkBackground,
                                        onClick = {
                                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                                showCamera = true
                                            } else {
                                                permissionLauncher.launch(Manifest.permission.CAMERA)
                                            }
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(48.dp))
                                Text(
                                    "CHOOSE AN OPTION",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp
                                )
                            }
                        }
                    }
                }

                if (imageUri != null) {
                    Surface(
                        color = surfaceColor.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(vertical = 16.dp)) {
                            TabRow(
                                selectedTabIndex = activeTab.ordinal,
                                containerColor = Color.Transparent,
                                contentColor = accentColor,
                                divider = {},
                                indicator = { tabPositions ->
                                    TabRowDefaults.SecondaryIndicator(
                                        Modifier.tabIndicatorOffset(tabPositions[activeTab.ordinal]),
                                        color = accentColor
                                    )
                                }
                            ) {
                                EditTab.entries.forEach { tab ->
                                    Tab(
                                        selected = activeTab == tab,
                                        onClick = { activeTab = tab },
                                        text = { Text(tab.name, fontSize = 12.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Box(modifier = Modifier.height(160.dp)) {
                                when (activeTab) {
                                    EditTab.Filters -> {
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            contentPadding = PaddingValues(horizontal = 24.dp)
                                        ) {
                                            items(FilterType.entries) { filter ->
                                                InteractiveFilterItem(
                                                    filter = filter,
                                                    imageUri = imageUri,
                                                    isSelected = selectedFilter == filter,
                                                    onClick = { selectedFilter = filter },
                                                    accentColor = accentColor
                                                )
                                            }
                                        }
                                    }
                                    EditTab.Transform -> {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            ToolButton(Icons.AutoMirrored.Filled.RotateRight, "Rotate") { rotation = (rotation + 90f) % 360f }
                                            ToolButton(Icons.Default.ZoomIn, "Zoom In") { zoomScale = (zoomScale + 0.1f).coerceAtMost(3f) }
                                            ToolButton(Icons.Default.ZoomOut, "Zoom Out") { zoomScale = (zoomScale - 0.1f).coerceAtLeast(1f) }
                                            ToolButton(Icons.Default.RestartAlt, "Reset") {
                                                rotation = 0f
                                                zoomScale = 1f
                                            }
                                        }
                                    }
                                    EditTab.Text -> {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Button(
                                                    onClick = { showTextDialog = true },
                                                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                                                ) {
                                                    Icon(Icons.Default.TextFields, null)
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(if (overlayText.isEmpty()) "Add Text" else "Edit Text", color = Color.Black)
                                                }
                                                if (overlayText.isNotEmpty()) {
                                                    Spacer(Modifier.width(8.dp))
                                                    IconButton(onClick = { overlayText = "" }) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.Red)
                                                    }
                                                }
                                            }

                                            if (overlayText.isNotEmpty()) {
                                                Spacer(Modifier.height(8.dp))
                                                Text("Alignment:", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceEvenly
                                                ) {
                                                    AlignmentButton(Icons.Default.VerticalAlignTop, textAlignment == Alignment.TopCenter) { textAlignment = Alignment.TopCenter }
                                                    AlignmentButton(Icons.AutoMirrored.Filled.AlignHorizontalLeft, textAlignment == Alignment.CenterStart) { textAlignment = Alignment.CenterStart }
                                                    AlignmentButton(Icons.Default.CenterFocusStrong, textAlignment == Alignment.Center) { textAlignment = Alignment.Center }
                                                    AlignmentButton(Icons.AutoMirrored.Filled.AlignHorizontalRight, textAlignment == Alignment.CenterEnd) { textAlignment = Alignment.CenterEnd }
                                                    AlignmentButton(Icons.Default.VerticalAlignBottom, textAlignment == Alignment.BottomCenter) { textAlignment = Alignment.BottomCenter }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraWithFilterScreen(
    onImageCaptured: (Uri, FilterType) -> Unit,
    onDismiss: () -> Unit,
    accentColor: Color
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context).apply { implementationMode = PreviewView.ImplementationMode.COMPATIBLE } }
    var selectedFilter by remember { mutableStateOf(FilterType.None) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            Log.e("CameraX", "Binding failed", e)
        }
    }

    // Applying RenderEffect for real-time ColorMatrix filters on API 31+
    val filterEffect = remember(selectedFilter) {
        val matrix = selectedFilter.matrixArray
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && matrix != null) {
            android.graphics.RenderEffect.createColorFilterEffect(
                android.graphics.ColorMatrixColorFilter(matrix)
            ).asComposeRenderEffect()
        } else null
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize().graphicsLayer(renderEffect = filterEffect)
        )

        // Visual fallback for older versions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && selectedFilter != FilterType.None) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(selectedFilter.previewColor.copy(alpha = 0.3f))
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                items(FilterType.entries) { filter ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { selectedFilter = filter }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (selectedFilter == filter) 3.dp else 1.dp,
                                    color = if (selectedFilter == filter) accentColor else Color.White.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .background(if (filter == FilterType.None) Color.Gray else filter.previewColor),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedFilter == filter) {
                                Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            filter.displayName,
                            color = if (selectedFilter == filter) accentColor else Color.White,
                            fontSize = 10.sp,
                            fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(32.dp))
                }

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(4.dp)
                        .border(4.dp, Color.Black, CircleShape)
                        .clickable {
                            val photoFile = File.createTempFile(
                                "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}_",
                                ".jpg",
                                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                            )
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                            imageCapture.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                        val savedUri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.provider",
                                            photoFile
                                        )
                                        onImageCaptured(savedUri, selectedFilter)
                                    }

                                    override fun onError(exc: ImageCaptureException) {
                                        Log.e("CameraX", "Photo capture failed: ${exc.message}", exc)
                                    }
                                }
                            )
                        }
                )

                Spacer(modifier = Modifier.size(48.dp))
            }
        }
    }
}

@Composable
fun StartOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accentColor: Color,
    darkBackground: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(pulseScale)
                .background(
                    brush = Brush.sweepGradient(
                        listOf(accentColor.copy(alpha = 0.1f), accentColor, accentColor.copy(alpha = 0.1f))
                    ),
                    shape = CircleShape
                )
                .padding(2.dp)
                .background(darkBackground, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = accentColor
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AlignmentButton(icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .background(if (isSelected) Color(0xFFD0BCFF) else Color.White.copy(alpha = 0.1f), CircleShape)
    ) {
        Icon(icon, contentDescription = null, tint = if (isSelected) Color.Black else Color.White, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun ToolButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(icon, contentDescription = label, tint = Color.White)
        }
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
    }
}

enum class EditTab { Filters, Transform, Text }

suspend fun saveImageToGallery(
    context: Context,
    uri: Uri,
    filter: FilterType,
    rotation: Float,
    zoom: Float,
    text: String,
    alignment: Alignment
) {
    withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return@withContext

            val width = originalBitmap.width
            val height = originalBitmap.height
            val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(resultBitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

            if (filter.matrixArray != null) {
                paint.colorFilter = android.graphics.ColorMatrixColorFilter(android.graphics.ColorMatrix(filter.matrixArray))
            }

            val matrix = Matrix()
            matrix.postRotate(rotation, width / 2f, height / 2f)
            matrix.postScale(zoom, zoom, width / 2f, height / 2f)

            canvas.drawBitmap(originalBitmap, matrix, paint)

            if (text.isNotEmpty()) {
                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.WHITE
                    textSize = (width * 0.05f).coerceAtLeast(40f)
                    setShadowLayer(5f, 0f, 0f, android.graphics.Color.BLACK)
                }

                val x: Float
                val y: Float
                val padding = width * 0.05f

                when (alignment) {
                    Alignment.TopCenter -> {
                        textPaint.textAlign = Paint.Align.CENTER
                        x = width / 2f
                        y = padding + textPaint.textSize
                    }
                    Alignment.CenterStart -> {
                        textPaint.textAlign = Paint.Align.LEFT
                        x = padding
                        y = height / 2f + textPaint.textSize / 2f
                    }
                    Alignment.Center -> {
                        textPaint.textAlign = Paint.Align.CENTER
                        x = width / 2f
                        y = height / 2f + textPaint.textSize / 2f
                    }
                    Alignment.CenterEnd -> {
                        textPaint.textAlign = Paint.Align.RIGHT
                        x = width - padding
                        y = height / 2f + textPaint.textSize / 2f
                    }
                    else -> { // BottomCenter
                        textPaint.textAlign = Paint.Align.CENTER
                        x = width / 2f
                        y = height - padding
                    }
                }

                canvas.drawText(text, x, y, textPaint)
            }

            val filename = "IMG_PRO_${System.currentTimeMillis()}.jpg"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ImagePro")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val imageUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            imageUri?.let {
                val outputStream: OutputStream? = context.contentResolver.openOutputStream(it)
                outputStream?.use { out ->
                    resultBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    context.contentResolver.update(it, contentValues, null, null)
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Image Saved to Gallery!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to save image: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

@Composable
fun InteractiveFilterItem(
    filter: FilterType,
    imageUri: Uri?,
    isSelected: Boolean,
    onClick: () -> Unit,
    accentColor: Color
) {
    val scale by animateFloatAsState(if (isSelected) 1.1f else 1f, label = "scale")
    val alpha by animateFloatAsState(if (isSelected) 1f else 0.6f, label = "alpha")

    val glowColor by animateColorAsState(
        if (isSelected) accentColor else Color.Transparent,
        animationSpec = tween(300),
        label = "glow"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(
                    elevation = if (isSelected) 12.dp else 0.dp,
                    shape = RoundedCornerShape(18.dp),
                    ambientColor = glowColor,
                    spotColor = glowColor
                )
                .clip(RoundedCornerShape(18.dp))
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = accentColor,
                    shape = RoundedCornerShape(18.dp)
                )
                .background(Color(0xFF202020)),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    colorFilter = filter.colorFilter
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = filter.displayName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = Color.White.copy(alpha = alpha),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

enum class FilterType(val displayName: String, val colorFilter: ColorFilter?, val matrixArray: FloatArray? = null, val previewColor: Color = Color.Transparent) {
    None("Normal", null, previewColor = Color.Transparent),

    HD("HD Enhance", ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
        1.2f, 0f, 0f, 0f, 10f,
        0f, 1.2f, 0f, 0f, 10f,
        0f, 0f, 1.2f, 0f, 10f,
        0f, 0f, 0f, 1f, 0f
    ))), floatArrayOf(
        1.2f, 0f, 0f, 0f, 10f,
        0f, 1.2f, 0f, 0f, 10f,
        0f, 0f, 1.2f, 0f, 10f,
        0f, 0f, 0f, 1f, 0f
    ), previewColor = Color(0xFFFFF9C4)),

    Sketch("Sketch", ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
        1.5f, 1.5f, 1.5f, 0f, -128f,
        1.5f, 1.5f, 1.5f, 0f, -128f,
        1.5f, 1.5f, 1.5f, 0f, -128f,
        0f, 0f, 0f, 1f, 0f
    ))), floatArrayOf(
        1.5f, 1.5f, 1.5f, 0f, -128f,
        1.5f, 1.5f, 1.5f, 0f, -128f,
        1.5f, 1.5f, 1.5f, 0f, -128f,
        0f, 0f, 0f, 1f, 0f
    ), previewColor = Color(0xFFE0E0E0)),

    Cartoon("Cartoon", ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
        2.2f, -0.4f, -0.4f, 0f, 0f,
        -0.4f, 2.2f, -0.4f, 0f, 0f,
        -0.4f, -0.4f, 2.2f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ))), floatArrayOf(
        2.2f, -0.4f, -0.4f, 0f, 0f,
        -0.4f, 2.2f, -0.4f, 0f, 0f,
        -0.4f, -0.4f, 2.2f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ), previewColor = Color(0xFFFFCCBC)),

    Mono("Mono", ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }), floatArrayOf(
        0.33f, 0.33f, 0.33f, 0f, 0f,
        0.33f, 0.33f, 0.33f, 0f, 0f,
        0.33f, 0.33f, 0.33f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ), previewColor = Color.Gray),

    Cinema("Cinema", ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
        1f, 0f, 0f, 0f, 0f,
        0f, 1.1f, 0f, 0f, 0f,
        0f, 0f, 1.3f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ))), floatArrayOf(
        1f, 0f, 0f, 0f, 0f,
        0f, 1.1f, 0f, 0f, 0f,
        0f, 0f, 1.3f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ), previewColor = Color(0xFFB3E5FC)),

    Retro("Retro", ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
        0.9f, 0f, 0f, 0f, 0f,
        0f, 0.8f, 0f, 0f, 0f,
        0f, 0f, 0.7f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ))), floatArrayOf(
        0.9f, 0f, 0f, 0f, 0f,
        0f, 0.8f, 0f, 0f, 0f,
        0f, 0.7f, 0f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ), previewColor = Color(0xFFD7CCC8)),

    HDR("HDR", ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
        1.2f, -0.1f, -0.1f, 0f, 0f,
        -0.1f, 1.2f, -0.1f, 0f, 0f,
        -0.1f, -0.1f, 1.2f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ))), floatArrayOf(
        1.2f, -0.1f, -0.1f, 0f, 0f,
        -0.1f, 1.2f, -0.1f, 0f, 0f,
        -0.1f, -0.1f, 1.2f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ), previewColor = Color(0xFFF8BBD0)),

    Lomo("Lomo", ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
        1.3f, 0f, 0f, 0f, -20f,
        0f, 1.3f, 0f, 0f, -20f,
        0f, 0f, 1.3f, 0f, -20f,
        0f, 0f, 0f, 1f, 0f
    ))), floatArrayOf(
        1.3f, 0f, 0f, 0f, -20f,
        0f, 1.3f, 0f, 0f, -20f,
        0f, 0f, 1.3f, 0f, -20f,
        0f, 0f, 0f, 1f, 0f
    ), previewColor = Color(0xFFC8E6C9)),

    Sepia("Sepia", ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
        0.393f, 0.769f, 0.189f, 0f, 0f,
        0.349f, 0.686f, 0.168f, 0f, 0f,
        0.272f, 0.534f, 0.131f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ))), floatArrayOf(
        0.393f, 0.769f, 0.189f, 0f, 0f,
        0.349f, 0.686f, 0.168f, 0f, 0f,
        0.272f, 0.534f, 0.131f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ), previewColor = Color(0xFFD7CCC8)),

    Vivid("Vivid", ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(2f) }), floatArrayOf(
        2f, -0.5f, -0.5f, 0f, 0f,
        -0.5f, 2f, -0.5f, 0f, 0f,
        -0.5f, -0.5f, 2f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ), previewColor = Color(0xFFFFE082)),

    Twilight("Twilight", ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
        0.8f, 0f, 0f, 0f, 30f,
        0f, 0.9f, 0f, 0f, 0f,
        0f, 0f, 1.2f, 0f, 50f,
        0f, 0f, 0f, 1f, 0f
    ))), floatArrayOf(
        0.8f, 0f, 0f, 0f, 30f,
        0f, 0.9f, 0f, 0f, 0f,
        0f, 0f, 1.2f, 0f, 50f,
        0f, 0f, 0f, 1f, 0f
    ), previewColor = Color(0xFFD1C4E9)),

    Golden("Golden", ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
        1.1f, 0f, 0f, 0f, 30f,
        0f, 1.1f, 0f, 0f, 10f,
        0f, 0f, 0.8f, 0f, -20f,
        0f, 0f, 0f, 1f, 0f
    ))), floatArrayOf(
        1.1f, 0f, 0f, 0f, 30f,
        0f, 1.1f, 0f, 0f, 10f,
        0f, 0f, 0.8f, 0f, -20f,
        0f, 0f, 0f, 1f, 0f
    ), previewColor = Color(0xFFFFD54F)),

    Noir("Noir", ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
        1.5f, 1.5f, 1.5f, 0f, -150f,
        1.5f, 1.5f, 1.5f, 0f, -150f,
        1.5f, 1.5f, 1.5f, 0f, -150f,
        0f, 0f, 0f, 1f, 0f
    ))), floatArrayOf(
        1.5f, 1.5f, 1.5f, 0f, -150f,
        1.5f, 1.5f, 1.5f, 0f, -150f,
        1.5f, 1.5f, 1.5f, 0f, -150f,
        0f, 0f, 0f, 1f, 0f
    ), previewColor = Color(0xFF212121)),

    Chrome("Chrome", ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
        1.2f, 0f, 0f, 0f, 5f,
        0f, 1.2f, 0f, 0f, 5f,
        0f, 0f, 1.2f, 0f, 5f,
        0f, 0f, 0f, 1f, 0f
    ))), floatArrayOf(
        1.2f, 0f, 0f, 0f, 5f,
        0f, 1.2f, 0f, 0f, 5f,
        0f, 0f, 1.2f, 0f, 5f,
        0f, 0f, 0f, 1f, 0f
    ), previewColor = Color(0xFFCFD8DC)),

    Fade("Fade", ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
        0.8f, 0.1f, 0.1f, 0f, 20f,
        0.1f, 0.8f, 0.1f, 0f, 20f,
        0.1f, 0.1f, 0.8f, 0f, 20f,
        0f, 0f, 0f, 1f, 0f
    ))), floatArrayOf(
        0.8f, 0.1f, 0.1f, 0f, 20f,
        0.1f, 0.8f, 0.1f, 0f, 20f,
        0.1f, 0.1f, 0.8f, 0f, 20f,
        0f, 0f, 0f, 1f, 0f
    ), previewColor = Color(0xFFF5F5F5)),

    Cool("Cool", ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
        0.9f, 0f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 1.2f, 0f, 20f,
        0f, 0f, 0f, 1f, 0f
    ))), floatArrayOf(
        0.9f, 0f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 1.2f, 0f, 20f,
        0f, 0f, 0f, 1f, 0f
    ), previewColor = Color(0xFFE1F5FE)),

    Warm("Warm", ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
        1.2f, 0f, 0f, 0f, 20f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 0.9f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ))), floatArrayOf(
        1.2f, 0f, 0f, 0f, 20f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 0.9f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ), previewColor = Color(0xFFFFF3E0)),

    Pastel("Pastel", ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
        0.9f, 0f, 0f, 0f, 40f,
        0f, 0.9f, 0f, 0f, 40f,
        0f, 0f, 0.9f, 0f, 40f,
        0f, 0f, 0f, 1f, 0f
    ))), floatArrayOf(
        0.9f, 0f, 0f, 0f, 40f,
        0f, 0.9f, 0f, 0f, 40f,
        0f, 0f, 0.9f, 0f, 40f,
        0f, 0f, 0f, 1f, 0f
    ), previewColor = Color(0xFFF3E5F5))
}
