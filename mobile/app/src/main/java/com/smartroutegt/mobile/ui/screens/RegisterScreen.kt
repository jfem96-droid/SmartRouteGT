package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.smartroutegt.mobile.auth.AuthRepository
import com.smartroutegt.mobile.ui.theme.SmartRoutePalette
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegistrationSuccess: (String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val repository = remember { AuthRepository() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SmartRoutePalette.Night)
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

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Crear cuenta",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Regístrate como usuario de SmartRoute GT",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre completo") },
            singleLine = true,
            enabled = !loading,
            colors = smartAuthTextFieldColors()
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Correo electrónico") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            singleLine = true,
            enabled = !loading,
            colors = smartAuthTextFieldColors()
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = telefono,
            onValueChange = {
                telefono = it
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Teléfono") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            singleLine = true,
            enabled = !loading,
            colors = smartAuthTextFieldColors()
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Contraseña") },
            singleLine = true,
            enabled = !loading,
            visualTransformation =
                if (showPassword) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
            trailingIcon = {
                TextButton(
                    onClick = {
                        showPassword = !showPassword
                    }
                ) {
                    Text(if (showPassword) "Ocultar" else "Ver")
                }
            },
            colors = smartAuthTextFieldColors()
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Confirmar contraseña") },
            singleLine = true,
            enabled = !loading,
            visualTransformation =
                if (showConfirmPassword) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
            trailingIcon = {
                TextButton(
                    onClick = {
                        showConfirmPassword = !showConfirmPassword
                    }
                ) {
                    Text(
                        if (showConfirmPassword) "Ocultar"
                        else "Ver"
                    )
                }
            },
            colors = smartAuthTextFieldColors()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "La contraseña debe tener al menos 8 caracteres.",
            style = MaterialTheme.typography.bodySmall
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !loading,
            onClick = {
                val cleanName = nombre.trim()
                val cleanEmail = email.trim()
                val cleanPhone = telefono.trim()

                when {
                    cleanName.isBlank() -> {
                        errorMessage = "Ingresa tu nombre."
                    }

                    cleanEmail.isBlank() ||
                        !cleanEmail.contains("@") -> {
                        errorMessage =
                            "Ingresa un correo electrónico válido."
                    }

                    cleanPhone.isBlank() -> {
                        errorMessage = "Ingresa tu número de teléfono."
                    }

                    !cleanPhone.matches(Regex("[0-9+()\\- ]{8,20}")) -> {
                        errorMessage = "Ingresa un número de teléfono válido."
                    }

                    password.length < 8 -> {
                        errorMessage =
                            "La contraseña debe tener al menos 8 caracteres."
                    }

                    password != confirmPassword -> {
                        errorMessage =
                            "Las contraseñas no coinciden."
                    }

                    else -> {
                        loading = true
                        errorMessage = null

                        scope.launch {
                            try {
                                repository.register(
                                    nombre = cleanName,
                                    email = cleanEmail,
                                    telefono = cleanPhone,
                                    password = password
                                )

                                onRegistrationSuccess(cleanEmail)

                            } catch (e: HttpException) {
                                errorMessage =
                                    e.response()
                                        ?.errorBody()
                                        ?.string()
                                        ?.takeIf { it.isNotBlank() }
                                        ?: "No fue posible crear la cuenta."

                            } catch (e: Exception) {
                                errorMessage =
                                    "No fue posible conectar con SmartRoute GT."

                            } finally {
                                loading = false
                            }
                        }
                    }
                }
            }
        ) {
            if (loading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Text("CREAR CUENTA")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Te enviaremos un código de verificación a tu correo.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
