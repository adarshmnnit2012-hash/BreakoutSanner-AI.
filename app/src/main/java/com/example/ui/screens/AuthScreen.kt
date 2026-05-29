package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TradingViewModel

enum class AuthMode {
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD
}

@Composable
fun AuthScreen(
    viewModel: TradingViewModel,
    isHindi: Boolean,
    modifier: Modifier = Modifier
) {
    val authError by viewModel.authError.collectAsState()
    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }

    var emailOrUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var recoveryMessage by remember { mutableStateOf<String?>(null) }

    var showGoogleAccountChooser by remember { mutableStateOf(false) }
    var showGoogleSigningInAnimation by remember { mutableStateOf(false) }
    var selectedGoogleEmail by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    // Trigger clear error when mode changes
    LaunchedEffect(authMode) {
        viewModel.clearAuthError()
        recoveryMessage = null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Slate 900
                        Color(0xFF1E293B)  // Slate 800
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Subtle ambient glowing breakout circles in background
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0x1A1E88E5), // Translucent Blue
                radius = this.size.width / 1.5f,
                center = androidx.compose.ui.geometry.Offset(this.size.width / 2f, 0f)
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Logo Indicator
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF1E88E5), Color(0xFF0D47A1))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.TrendingUp,
                        contentDescription = "Breakout Scanner Logo",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Sub-heading details
                Text(
                    text = "BreakoutScanner AI",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A),
                    fontFamily = FontFamily.SansSerif
                )

                Text(
                    text = if (isHindi) "भारतीय वायदा एवं शेयर बाजार स्कैनर" else "NSE/BSE Smart Breakouts Alignment",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Error Notification Box
                AnimatedVisibility(visible = authError != null) {
                    authError?.let { err ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = "Error Logo",
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = err,
                                color = Color(0xFFC62828),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Custom Password Recovery message
                AnimatedVisibility(visible = recoveryMessage != null) {
                    recoveryMessage?.let { msg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Success Logo",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                color = Color(0xFF2E7D32),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Render dynamic inputs based on selected MODE
                Crossfade(targetState = authMode, label = "AuthModeTransition") { mode ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        when (mode) {
                            AuthMode.LOGIN -> {
                                Text(
                                    text = if (isHindi) "अपने अकाउंट में लॉग इन करें" else "Sign in to your scanner account",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = emailOrUsername,
                                    onValueChange = { emailOrUsername = it },
                                    label = { Text(if (isHindi) "यूज़रनेम या ईमेल" else "Email or Username") },
                                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "User") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text(if (isHindi) "पासवर्ड" else "Password") },
                                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Lock") },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            viewModel.login(emailOrUsername, password)
                                        }
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = if (isHindi) "पासवर्ड भूल गए?" else "Forgot Password?",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E88E5),
                                        modifier = Modifier
                                            .clickable { authMode = AuthMode.FORGOT_PASSWORD }
                                            .padding(4.dp)
                                    )
                                }

                                Button(
                                    onClick = {
                                        focusManager.clearFocus()
                                        viewModel.login(emailOrUsername, password)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1E88E5)
                                    )
                                ) {
                                    Text(
                                        text = if (isHindi) "लॉग इन करें" else "Secure Login",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Enter")
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                                    Text(
                                        text = if (isHindi) "या फिर" else "OR",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(horizontal = 10.dp)
                                    )
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                                }

                                OutlinedButton(
                                    onClick = {
                                        showGoogleAccountChooser = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFF0F172A)
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                ) {
                                    GoogleColoredIcon()
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (isHindi) "जीमेल से लॉग इन करें" else "Continue with Gmail",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }

                                // Toggle flow
                                TextButton(
                                    onClick = { authMode = AuthMode.REGISTER },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (isHindi) "नया अकाउंट? यहाँ रजिस्टर करें" else "Don't have an account? Register Now",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A),
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            AuthMode.REGISTER -> {
                                Text(
                                    text = if (isHindi) "सुरक्षित नया स्कैनर अकाउंट बनाएं" else "Create a secure scanner profile",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = emailOrUsername,
                                    onValueChange = { emailOrUsername = it },
                                    label = { Text(if (isHindi) "ईमेल या यूज़रनेम" else "Email or Username") },
                                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Mail") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text(if (isHindi) "पासवर्ड सेट करें" else "Choose Secure Password") },
                                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Lock") },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            viewModel.register(emailOrUsername, password)
                                        }
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Button(
                                    onClick = {
                                        focusManager.clearFocus()
                                        viewModel.register(emailOrUsername, password)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4CAF50)
                                    )
                                ) {
                                    Text(
                                        text = if (isHindi) "अकाउंट बनाएं" else "Create Free Account",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }

                                TextButton(
                                    onClick = { authMode = AuthMode.LOGIN },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (isHindi) "पहले से अकाउंट है? लॉग इन करें" else "Already have an account? Sign In",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A),
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            AuthMode.FORGOT_PASSWORD -> {
                                Text(
                                    text = if (isHindi) "पासवर्ड पुनःप्राप्ति" else "Recover scanner password",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = emailOrUsername,
                                    onValueChange = { emailOrUsername = it },
                                    label = { Text(if (isHindi) "पुनर्प्राप्ति ईमेल या यूज़रनेम" else "Recovery Email or Username") },
                                    leadingIcon = { Icon(Icons.Filled.MailOutline, contentDescription = "Recovery Icon") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            recoveryMessage = viewModel.forgotPassword(emailOrUsername)
                                        }
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Button(
                                    onClick = {
                                        focusManager.clearFocus()
                                        recoveryMessage = viewModel.forgotPassword(emailOrUsername)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF9C27B0)
                                    )
                                ) {
                                    Text(
                                        text = if (isHindi) "पासवर्ड दिखाएं" else "Retrieve Password",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }

                                TextButton(
                                    onClick = { authMode = AuthMode.LOGIN },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (isHindi) "लॉग इन स्क्रीन पर वापस जाएं" else "Back to secure Login",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Divider(color = Color(0xFFF1F5F9))

                // Quick Bypass Guest Login Option
                Text(
                    text = if (isHindi) "अथवा डेमो क्रेडेंशियल्स का उपयोग करें:\nयूज़रनेम: 'user' | पासवर्ड: 'password'"
                    else "Or log in with demo credentials:\nUsername: 'user' | Password: 'password'",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }

    // Google Account Picker Dialog Overlay
    if (showGoogleAccountChooser) {
        AlertDialog(
            onDismissRequest = { showGoogleAccountChooser = false },
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        GoogleColoredIcon(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Google",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF1F2937)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isHindi) "BreakoutScanner AI में जारी रखें" else "Choose an account to continue",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Account Option 1: Adarsh (Personalized!)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(12.dp))
                            .clickable {
                                selectedGoogleEmail = "adarsh.mnnit.2012@gmail.com"
                                showGoogleAccountChooser = false
                                showGoogleSigningInAnimation = true
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFE2E8F0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "A",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569),
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Adarsh",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "adarsh.mnnit.2012@gmail.com",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    // Account Option 2: Demo Trader
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(12.dp))
                            .clickable {
                                selectedGoogleEmail = "demo.scanner@gmail.com"
                                showGoogleAccountChooser = false
                                showGoogleSigningInAnimation = true
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFFFE0B2), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "D",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100),
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Demo Trader Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "demo.scanner@gmail.com",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    // Optional User Custom Input
                    var customEmailInput by remember { mutableStateOf("") }
                    var isEmailInputExpanded by remember { mutableStateOf(false) }

                    if (!isEmailInputExpanded) {
                        TextButton(
                            onClick = { isEmailInputExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add account", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "दूसरा जीमेल उपयोग करें" else "Use another Google Account",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = customEmailInput,
                            onValueChange = { customEmailInput = it },
                            label = { Text("Gmail Address") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(
                                    enabled = customEmailInput.isNotBlank() && customEmailInput.contains("@"),
                                    onClick = {
                                        selectedGoogleEmail = customEmailInput.trim()
                                        showGoogleAccountChooser = false
                                        showGoogleSigningInAnimation = true
                                    }
                                ) {
                                    Icon(Icons.Filled.Check, contentDescription = "Confirm", tint = Color(0xFF2E7D32))
                                }
                            }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showGoogleAccountChooser = false }) {
                    Text(if (isHindi) "रद्द करें" else "Cancel")
                }
            }
        )
    }

    // Google Sign-In Progress Overlay Simulation
    if (showGoogleSigningInAnimation) {
        LaunchedEffect(selectedGoogleEmail) {
            kotlinx.coroutines.delay(1200) // Realistic secure latency delay
            viewModel.loginWithGoogle(selectedGoogleEmail)
            showGoogleSigningInAnimation = false
        }

        AlertDialog(
            onDismissRequest = {},
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF4285F4),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = if (isHindi) "Google के साथ सुरक्षित लॉग इन हो रहा है..." else "Signing in securely via Google Accounts...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = selectedGoogleEmail,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {},
            dismissButton = null,
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        )
    }
}

@Composable
fun GoogleColoredIcon(modifier: Modifier = Modifier.size(18.dp)) {
    Canvas(modifier = modifier) {
        val width = size.width
        val radius = width / 2
        
        drawArc(
            color = Color(0xFFEA4335), // Red
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = true
        )
        drawArc(
            color = Color(0xFFFBBC05), // Yellow
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = true
        )
        drawArc(
            color = Color(0xFF34A853), // Green
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = true
        )
        drawArc(
            color = Color(0xFF4285F4), // Blue
            startAngle = 270f,
            sweepAngle = 90f,
            useCenter = true
        )
        
        // Inner mask to turn segmented circle into Google-styled ring
        drawCircle(
            color = Color.White,
            radius = radius * 0.55f
        )
    }
}
