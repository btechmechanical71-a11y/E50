package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: (email: String, name: String, city: String, isGuest: Boolean) -> Unit,
    onBack: () -> Unit
) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("priya.sharma@gujarat.in") }
    var password by remember { mutableStateOf("EcoMission2026") }
    var displayName by remember { mutableStateOf("Priya Sharma") }
    var selectedCity by remember { mutableStateOf("Ahmedabad") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetSuccessMessage by remember { mutableStateOf<String?>(null) }

    val cities = listOf("Ahmedabad", "Surat", "Vadodara", "Rajkot", "Gandhinagar", "Bhavnagar", "Jamnagar")

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showForgotPasswordDialog = false
                resetSuccessMessage = null
            },
            title = { Text("Reset Password", fontWeight = FontWeight.Bold, color = DeepTeal) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Enter your registered email address to receive password recovery instructions.",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth().testTag("reset_email_input"),
                        singleLine = true
                    )
                    resetSuccessMessage?.let { msg ->
                        Text(msg, color = FreshGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetEmail.isNotBlank()) {
                            resetSuccessMessage = "Reset link dispatched for $resetEmail. In local development mode, your session remains accessible."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                    modifier = Modifier.testTag("send_reset_button")
                ) {
                    Text("Send Instructions")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showForgotPasswordDialog = false
                        resetSuccessMessage = null
                    }
                ) {
                    Text("Close", color = DeepTeal)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Back
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            TextButton(
                onClick = onBack,
                modifier = Modifier.testTag("auth_back_button")
            ) {
                Text("← Back to Landing", color = DeepTeal, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(FreshGreen),
            contentAlignment = Alignment.Center
        ) {
            Text("🌱", fontSize = 32.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isSignUp) "Create E50 Account" else "Welcome Back to E50",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = DeepTeal
        )

        Text(
            text = "Mission LiFE • Citizen Energy Action Hub",
            fontSize = 13.sp,
            color = EmeraldGreen,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tab Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LightSage, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (!isSignUp) FreshGreen else Color.Transparent)
                    .clickable { isSignUp = false }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Sign In",
                    color = if (!isSignUp) Color.White else DeepTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSignUp) FreshGreen else Color.Transparent)
                    .clickable { isSignUp = true }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Sign Up",
                    color = if (isSignUp) Color.White else DeepTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (isSignUp) {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("auth_name_input"),
                        singleLine = true
                    )

                    Text("Select Your Gujarat City", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DeepTeal)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        cities.forEach { city ->
                            FilterChip(
                                selected = selectedCity == city,
                                onClick = { selectedCity = city },
                                label = { Text(city, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                if (!isSignUp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Forgot Password?",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FreshGreen,
                            modifier = Modifier
                                .clickable {
                                    resetEmail = email
                                    showForgotPasswordDialog = true
                                }
                                .padding(vertical = 4.dp)
                                .testTag("forgot_password_button")
                        )
                    }
                }

                errorMessage?.let { err ->
                    Text(err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        if (email.isBlank() || password.length < 6) {
                            errorMessage = "Please enter a valid email and minimum 6-character password"
                        } else {
                            errorMessage = null
                            onAuthSuccess(email, displayName, selectedCity, false)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (isSignUp) "Create My E50 Account" else "Sign In",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Guest Entry
        OutlinedButton(
            onClick = {
                onAuthSuccess("guest@e50.in", "Guest Citizen", "Ahmedabad", true)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("auth_guest_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Continue as Guest Explorer", color = DeepTeal, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Development Mode: Data is saved locally and persistently in your device storage.",
            fontSize = 11.sp,
            color = DeepTeal.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
