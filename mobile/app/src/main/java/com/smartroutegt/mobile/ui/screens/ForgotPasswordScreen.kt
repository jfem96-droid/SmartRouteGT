package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartroutegt.mobile.auth.AuthRepository
import com.smartroutegt.mobile.ui.theme.SmartRoutePalette
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun ForgotPasswordScreen(
    accessType: String,
    onBack: () -> Unit,
    onPasswordChanged: () -> Unit
) {
    var step by remember { mutableStateOf(1) }

    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showPassword by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val repository = remember { AuthRepository() }

    val smartBlue = SmartRoutePalette.Mint
    val background = SmartRoutePalette.Night
    val darkText = SmartRoutePalette.Text

    fun httpMessage(e: HttpException): String {
        return try {
            val body = e.response()?.errorBody()?.string()
            if (body.isNullOrBlank()) {
                "No fue posible completar la solicitud."
            } else {
                body
                    .replace("{\"message\":\"", "")
                    .replace("\"}", "")
                    .replace("\"", "")
            }
        } catch (_: Exception) {
            "No fue posible completar la solicitud."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextButton(
            onClick = onBack,
            enabled = !loading
        ) {
            Text("← Volver")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "SmartRoute GT",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = smartBlue
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (step == 1) {
                "Recuperar contraseña"
            } else {
                "Crear nueva contraseña"
            },
            style = MaterialTheme.typography.headlineMedium,
            color = darkText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (step == 1) {
                "Ingresa tu correo y te enviaremos un código de seguridad."
            } else {
                "Ingresa el código que enviamos a $email."
            },
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(28.dp))

        if (step == 1) {
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = { Text("Correo electrónico") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                singleLine = true,
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
                colors = smartAuthTextFieldColors()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val cleanEmail = email.trim().lowercase()

                    if (
                        cleanEmail.isBlank() ||
                        !cleanEmail.contains("@")
                    ) {
                        errorMessage = "Ingresa un correo electrónico válido."
                        return@Button
                    }

                    scope.launch {
                        loading = true
                        errorMessage = null
                        infoMessage = null

                        try {
                            repository.forgotPassword(cleanEmail)
                            email = cleanEmail
                            step = 2
                            infoMessage = "Código enviado. Revisa tu correo."
                        } catch (e: HttpException) {
                            errorMessage = httpMessage(e)
                        } catch (_: Exception) {
                            errorMessage =
                                "No fue posible conectar con SmartRoute GT."
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator()
                } else {
                    Text("ENVIAR CÓDIGO")
                }
            }
        } else {
            OutlinedTextField(
                value = code,
                onValueChange = {
                    code = it.filter(Char::isDigit).take(6)
                    errorMessage = null
                },
                label = { Text("Código de 6 dígitos") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
                colors = smartAuthTextFieldColors()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    errorMessage = null
                },
                label = { Text("Nueva contraseña") },
                visualTransformation =
                    if (showPassword) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                singleLine = true,
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
                colors = smartAuthTextFieldColors()
            )

            TextButton(
                onClick = {
                    showPassword = !showPassword
                },
                enabled = !loading,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    if (showPassword) {
                        "Ocultar contraseña"
                    } else {
                        "Mostrar contraseña"
                    }
                )
            }

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = null
                },
                label = { Text("Confirmar nueva contraseña") },
                visualTransformation =
                    if (showPassword) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                singleLine = true,
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
                colors = smartAuthTextFieldColors()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    when {
                        code.length != 6 -> {
                            errorMessage =
                                "Ingresa el código de 6 dígitos."
                        }

                        newPassword.length < 8 -> {
                            errorMessage =
                                "La contraseña debe tener al menos 8 caracteres."
                        }

                        newPassword != confirmPassword -> {
                            errorMessage =
                                "Las contraseñas no coinciden."
                        }

                        else -> {
                            scope.launch {
                                loading = true
                                errorMessage = null

                                try {
                                    repository.resetPassword(
                                        email = email,
                                        code = code,
                                        newPassword = newPassword
                                    )

                                    onPasswordChanged()
                                } catch (e: HttpException) {
                                    errorMessage = httpMessage(e)
                                } catch (_: Exception) {
                                    errorMessage =
                                        "No fue posible conectar con SmartRoute GT."
                                } finally {
                                    loading = false
                                }
                            }
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator()
                } else {
                    Text("CAMBIAR CONTRASEÑA")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    scope.launch {
                        loading = true
                        errorMessage = null
                        infoMessage = null

                        try {
                            repository.forgotPassword(email)
                            infoMessage = "Enviamos un nuevo código."
                        } catch (e: HttpException) {
                            errorMessage = httpMessage(e)
                        } catch (_: Exception) {
                            errorMessage =
                                "No fue posible reenviar el código."
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Reenviar código")
            }
        }

        infoMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = it,
                color = smartBlue
            )
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (accessType.uppercase() == "DRIVER") {
                "Acceso para piloto"
            } else {
                "Acceso para usuario"
            },
            style = MaterialTheme.typography.bodySmall
        )
    }
}
