package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartroutegt.mobile.auth.AuthRepository
import com.smartroutegt.mobile.auth.LoginResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

@Composable
fun LoginScreen(
    accessType: String,
    onBack: () -> Unit,
    onCreateAccount: () -> Unit,
    onForgotPassword: () -> Unit,
    onLoginSuccess: (LoginResponse) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository() }

    val smartBlue = Color(0xFF2563EB)
    val background = Color(0xFFF4F7FB)
    val darkText = Color(0xFF172033)
    val mutedText = Color(0xFF667085)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SmartRoute GT",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = smartBlue
            )

            Text(
                text = "Movilidad inteligente",
                fontSize = 15.sp,
                color = mutedText,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    TextButton(
                        onClick = onBack,
                        enabled = !loading
                    ) {
                        Text(
                            text = "← Volver",
                            color = smartBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = if (accessType == "DRIVER") {
                            "Acceso para piloto"
                        } else {
                            "Acceso para usuario"
                        },
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = darkText
                    )

                    Text(
                        text = if (accessType == "DRIVER") {
                            "Ingresa con tu cuenta de piloto"
                        } else {
                            "Ingresa a tu cuenta de SmartRoute GT"
                        },
                        fontSize = 14.sp,
                        color = mutedText,
                        modifier = Modifier.padding(top = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            error = null
                        },
                        label = {
                            Text("Correo electrónico")
                        },
                        singleLine = true,
                        enabled = !loading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            error = null
                        },
                        label = {
                            Text("Contraseña")
                        },
                        singleLine = true,
                        enabled = !loading,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (error != null) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onForgotPassword,
                        enabled = !loading,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "¿Olvidaste tu contraseña?",
                            color = smartBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val cleanEmail = email.trim()

                            if (cleanEmail.isBlank()) {
                                error = "Ingresa tu correo electrónico"
                                return@Button
                            }

                            if (password.isBlank()) {
                                error = "Ingresa tu contraseña"
                                return@Button
                            }

                            scope.launch {
                                loading = true
                                error = null

                                try {
                                    val response = authRepository.login(
                                        email = cleanEmail,
                                        password = password
                                    )

                                    onLoginSuccess(response)

                                } catch (exception: HttpException) {
                                    error = when (exception.code()) {
                                        400 -> "Revisa los datos ingresados"
                                        401 -> "Correo o contraseña incorrectos"
                                        403 -> "Tu usuario no tiene acceso al sistema"
                                        else -> "Error del servidor (${exception.code()})"
                                    }
                                } catch (exception: IOException) {
                                    error = "No se pudo conectar con SmartRoute GT"
                                } catch (exception: Exception) {
                                    error = "No fue posible iniciar sesión"
                                } finally {
                                    loading = false
                                }
                            }
                        },
                        enabled = !loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = smartBlue
                        )
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Iniciar sesión",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (accessType == "PASSENGER") {
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "¿Aún no tienes una cuenta?",
                            color = mutedText,
                            fontSize = 13.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        TextButton(
                            onClick = onCreateAccount,
                            enabled = !loading,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                text = "Crear cuenta nueva",
                                color = smartBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Sistema de Optimización de Transporte Público",
                color = mutedText,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
