package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AnimatedGradientBackground
import com.example.ui.components.GlassyCard
import com.example.ui.viewmodel.TimeKeeperViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    viewModel: TimeKeeperViewModel
) {
    val isDark by viewModel.settingsDarkMode.collectAsState()
    
    // Smooth intro trigger
    var introStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        introStarted = true
    }

    // Phase selector: 0 for Splash intro, 1 for Name collection
    var onboardPhase by remember { mutableStateOf(0) }
    var nameInput by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    // Floating pulse scale for the central icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    AnimatedGradientBackground(isDarkMode = isDark) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                
                // Rotating and scaling icon decoration
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(130.dp)
                        .scale(pulseScale)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF007AFF).copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF007AFF), Color(0xFFBF5AF2))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = "Time Keeper Chronometer Logo",
                            tint = Color.White,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Interactive transition switcher
                AnimatedContent(
                    targetState = onboardPhase,
                    transitionSpec = {
                        slideInHorizontally(
                            initialOffsetX = { if (targetState > initialState) 400 else -400 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + fadeIn() togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { if (targetState > initialState) -400 else 400 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) + fadeOut()
                    },
                    label = "onboardPhaseTransition"
                ) { phase ->
                    when (phase) {
                        0 -> {
                            // Phase 0: Grand welcome introduction
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Time Keeper",
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isDark) Color.White else Color(0xFF0F172A),
                                        letterSpacing = (-1.2).sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Your High-Performance Space Coach",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = Color(0xFF007AFF),
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                GlassyCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = "Elite Analytics & Dynamic Tracking",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color.White else Color(0xFF0F172A)
                                        ),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Take complete control of your schedule, log deep focus blocks, build streaks of healthy habits, and master your time completely with generative AI advice. Everything is saved strictly local and private on your phone.",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            lineHeight = 22.sp,
                                            color = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF334155),
                                            fontWeight = FontWeight.Normal
                                        ),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Spacer(modifier = Modifier.height(44.dp))

                                Button(
                                    onClick = { onboardPhase = 1 },
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .height(58.dp)
                                        .testTag("get_started_button"),
                                    shape = RoundedCornerShape(29.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF007AFF),
                                        contentColor = Color.White
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Get Started",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            )
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = "Navigate to Next Step"
                                        )
                                    }
                                }
                            }
                        }
                        
                        1 -> {
                            // Phase 1: Interactive name personalization
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Personalize Space",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isDark) Color.White else Color(0xFF0F172A),
                                        letterSpacing = (-0.5).sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Establish your customized experience",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                        textAlign = TextAlign.Center
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                GlassyCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = "Who is taking charge today?",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color.White else Color(0xFF0F172A)
                                        ),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Please enter your nickname. Your space will configure to match your name's aesthetic.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                            lineHeight = 16.sp,
                                            textAlign = TextAlign.Center
                                        ),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    
                                    Spacer(modifier = Modifier.height(18.dp))

                                    OutlinedTextField(
                                        value = nameInput,
                                        onValueChange = {
                                            nameInput = it
                                            if (it.isNotBlank()) nameError = false
                                        },
                                        placeholder = {
                                            Text(
                                                text = "e.g., Alex, Sophia...",
                                                color = if (isDark) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.4f)
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = Color(0xFF007AFF)
                                            )
                                        },
                                        isError = nameError,
                                        singleLine = true,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("username_input"),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.02f),
                                            unfocusedContainerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.02f),
                                            focusedIndicatorColor = Color(0xFF007AFF),
                                            unfocusedIndicatorColor = Color.LightGray.copy(alpha = 0.5f),
                                            errorIndicatorColor = Color.Red,
                                            focusedTextColor = if (isDark) Color.White else Color(0xFF1E293B),
                                            unfocusedTextColor = if (isDark) Color.White else Color(0xFF1E293B)
                                        )
                                    )

                                    if (nameError) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Name cannot be empty!",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color.Red,
                                                fontSize = 11.sp
                                            ),
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Start
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(40.dp))

                                Button(
                                    onClick = {
                                        if (nameInput.isNotBlank()) {
                                            viewModel.saveOnboardingAndName(nameInput.trim())
                                        } else {
                                            nameError = true
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .height(58.dp)
                                        .testTag("personalize_space_button"),
                                    shape = RoundedCornerShape(29.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF34C759), // Elegant iOS vibrant Green for execution
                                        contentColor = Color.White
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Initialize My Space",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            )
                                        )
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Confirm Name"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
