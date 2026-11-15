package com.duasmartapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.keyboard.ImeAction
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duasmartapp.R
import com.duasmartapp.ui.AppViewModel
import com.duasmartapp.ui.Screen
import com.duasmartapp.ui.components.*
import com.duasmartapp.ui.theme.*

@Composable
fun LoginScreen(viewModel: AppViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("ADMIN") }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val errorMessage = viewModel.message.value
    if (errorMessage != null && error == null) error = errorMessage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBrush)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(64.dp).background(Color(0xFF64748B), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                DuaIcon(
                    painter = painterResource(R.drawable.ic_launcher),
                    tint = White,
                    size = 36.dp,
                    contentDescription = "DUA SMART APP logo"
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "DUA SMART APP", style = Heading1, color = Ink)
            Text(
                text = "School Administration  •  Attendance  •  Fees",
                style = Caption,
                color = Slate500
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        GlassPanel(
            modifier = Modifier
                .width(minConstraints = 300.dp)
                .padding(24.dp)
                .shadow(12.dp, shape = RoundedCornerShape(20.dp), clip = true)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Sign in", style = Heading2, color = Ink)
                Text(
                    text = "Welcome back to DUA SMART APP",
                    style = Caption,
                    color = Slate500
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Role selector
                Row {
                    val roles = listOf("ADMIN" to "Admin", "TEACHER" to "Teacher")
                    roles.forEachIndexed { index, (key, label) } {
                        val isSelected = role == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(onClick = { role = key })
                                .background(
                                    if (isSelected) GlassFillLight else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .border(
                                    if (isSelected) 1.dp else 0.dp,
                                    if (isSelected) Ink else Slate300,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = Caption.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Ink else Slate500
                            )
                        }
                        if (index < roles.size - 1) Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(text = "Username", style = Body) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(text = "Password", style = Body) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    visualTransformation = VisualTransformation.Password,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))

                error?.let {
                    Text(text = it, style = Caption.copy(color = Crimson))
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        error = null
                        viewModel.login(username, password, role)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Ink)
                ) {
                    Text(text = "Sign In", style = ButtonText.copy(color = White))
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Default: admin / admin123  |  teacher / teacher123",
                    style = Caption,
                    color = Slate500
                )
            }
        }

        CircularProgressIndicator(
            modifier = Modifier.alpha(if (viewModel.loading.value) 1f else 0f)
        )
    }
}
