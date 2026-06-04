package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AnimatedGradientBackground
import com.example.ui.components.GlassyCard
import com.example.ui.viewmodel.TimeKeeperViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: TimeKeeperViewModel,
    initialMode: String = "login", // "login", "signup", "forgot"
    onBack: () -> Unit
) {
    val isDark by viewModel.settingsDarkMode.collectAsState()
    var mode by remember { mutableStateOf(initialMode) } // "login", "signup", "forgot"

    // Inputs
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }

    var rememberMe by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Validation/Feedbacks
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Clear inputs on mode change
    LaunchedEffect(mode) {
        errorMessage = null
        successMessage = null
        nameInput = ""
        emailInput = ""
        passwordInput = ""
        confirmPasswordInput = ""
    }

    AnimatedGradientBackground(isDarkMode = isDark) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header navigation / Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isDark) Color.White else Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Mini Identity
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassEmpty,
                    contentDescription = null,
                    tint = Color(0xFF007AFF),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Time Keeper",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1C1C1E)
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // MAIN GLASS CARD
            GlassyCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when (mode) {
                        "login" -> "Welcome Back"
                        "signup" -> "Create Account"
                        else -> "Reset Password"
                    },
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1C1C1E)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = when (mode) {
                        "login" -> "Sign in to resume tracking your smart agenda"
                        "signup" -> "Unlock personalized AI motivation & focus blocks"
                        else -> "Enter your email to receive recovery instructions"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF8E8E93)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    textAlign = TextAlign.Center
                )

                // Validation banners
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .background(Color(0xFFFF453A).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = "Error", tint = Color(0xFFFF453A))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(errorMessage!!, color = Color(0xFFFF1744), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (successMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .background(Color(0xFF30D158).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = Color(0xFF30D158))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(successMessage!!, color = Color(0xFF00E676), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // INPUT FIELDS
                if (mode == "signup") {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Full Name", color = Color(0xFF8E8E93)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = if (isDark) Color.White else Color.Black,
                            unfocusedTextColor = if (isDark) Color.White else Color.Black,
                            focusedBorderColor = Color(0xFF007AFF),
                            unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_name_input"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF8E8E93)) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Email", color = Color(0xFF8E8E93)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDark) Color.White else Color.Black,
                        unfocusedTextColor = if (isDark) Color.White else Color.Black,
                        focusedBorderColor = Color(0xFF007AFF),
                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF8E8E93)) }
                )

                if (mode != "forgot") {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password", color = Color(0xFF8E8E93)) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = if (isDark) Color.White else Color.Black,
                            unfocusedTextColor = if (isDark) Color.White else Color.Black,
                            focusedBorderColor = Color(0xFF007AFF),
                            unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF8E8E93)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = Color(0xFF8E8E93)
                                )
                            }
                        }
                    )
                }

                if (mode == "signup") {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = { confirmPasswordInput = it },
                        label = { Text("Confirm Password", color = Color(0xFF8E8E93)) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = if (isDark) Color.White else Color.Black,
                            unfocusedTextColor = if (isDark) Color.White else Color.Black,
                            focusedBorderColor = Color(0xFF007AFF),
                            unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF8E8E93)) }
                    )
                }

                // REMEMBER ME / FORGOT PASSWORD ROW
                if (mode == "login") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF007AFF))
                            )
                            Text(
                                "Remember Me",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.8f)
                            )
                        }

                        Text(
                            "Forgot Password?",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF007AFF),
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .clickable { mode = "forgot" }
                                .padding(4.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // PRIMARY ACTION BUTTON
                Button(
                    onClick = {
                        errorMessage = null
                        successMessage = null

                        if (emailInput.isEmpty() || !emailInput.contains("@")) {
                            errorMessage = "Please enter a valid email address."
                            return@Button
                        }

                        if (mode != "forgot" && passwordInput.length < 6) {
                            errorMessage = "Password must be at least 6 characters."
                            return@Button
                        }

                        if (mode == "signup" && passwordInput != confirmPasswordInput) {
                            errorMessage = "Passwords do not match."
                            return@Button
                        }

                        // Complete Action
                        when (mode) {
                            "login" -> {
                                viewModel.login(emailInput, emailInput.substringBefore("@").replaceFirstChar { it.uppercase() })
                            }

                            "signup" -> {
                                viewModel.signup(emailInput, nameInput.ifEmpty { "Alex" })
                            }

                            "forgot" -> {
                                successMessage = "A recovery link has been dispatched to $emailInput"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("auth_submit_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF007AFF)
                    )
                ) {
                    Text(
                        text = when (mode) {
                            "login" -> "Sign In"
                            "signup" -> "Sign Up"
                            else -> "Send Recovery Link"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // DIRECT MOCK GOOGLE SIGN IN
                if (mode != "forgot") {
                    OutlinedButton(
                        onClick = {
                            viewModel.login("alex_google@example.com", "Alex G")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isDark) Color.White.copy(alpha = 0.2f) else Color.LightGray
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VpnKey, // generic auth key icon mock
                                contentDescription = null,
                                tint = Color(0xFF007AFF),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Continue with Google",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color.Black
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // FOOTER / TOGGLE TABS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = when (mode) {
                            "login" -> "Don't have an account? "
                            "signup" -> "Already have an account? "
                            else -> "Remembered your credentials? "
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8E8E93)
                    )
                    Text(
                        text = when (mode) {
                            "login" -> "Sign Up"
                            "signup" -> "Sign In"
                            else -> "Sign In"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF007AFF),
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable {
                            mode = when (mode) {
                                "login" -> "signup"
                                "signup" -> "login"
                                else -> "login"
                            }
                        }
                    )
                }
            }
        }
    }
}
