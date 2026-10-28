package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.smartroutegt.mobile.auth.AuthRepository
import com.smartroutegt.mobile.ui.theme.SmartRoutePalette
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun VerifyEmailScreen(
    email: String,
    onBack: () -> Unit,
    onVerified: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var resending by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    val repository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SmartRoutePalette.Night)
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextButton(
            onClick = onBack,
            enabled = !loading && !resending
        ) {
            Text("← Volver")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Verifica tu correo",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enviamos un código de 6 dígitos a:",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = email,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { value ->
                code = value
                    .filter { it.isDigit() }
                    .take(6)

                message = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Código de verificación") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            enabled = !loading && !resending,
            colors = smartAuthTextFieldColors()
        )

        message?.let {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = it,
                color = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !loading &&
                !resending &&
                code.length == 6,
            onClick = {
                loading = true
                message = null

                scope.launch {
                    try {
                        repository.verifyEmail(
                            email = email,
                            code = code
                        )

                        onVerified()

                    } catch (e: HttpException) {
                        isError = true

                        message =
                            e.response()
                                ?.errorBody()
                                ?.string()
                                ?.takeIf { it.isNotBlank() }
                                ?: "Código incorrecto o vencido."

                    } catch (e: Exception) {
                        isError = true
                        message =
                            "No fue posible conectar con SmartRoute GT."

                    } finally {
                        loading = false
                    }
                }
            }
        ) {
            if (loading) {
                CircularProgressIndicator()
            } else {
                Text("VERIFICAR CUENTA")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading && !resending,
            onClick = {
                resending = true
                message = null

                scope.launch {
                    try {
                        repository.resendCode(email)

                        isError = false
                        message =
                            "Te enviamos un nuevo código."

                    } catch (e: HttpException) {
                        isError = true

                        message =
                            e.response()
                                ?.errorBody()
                                ?.string()
                                ?.takeIf { it.isNotBlank() }
                                ?: "No fue posible reenviar el código."

                    } catch (e: Exception) {
                        isError = true
                        message =
                            "No fue posible conectar con SmartRoute GT."

                    } finally {
                        resending = false
                    }
                }
            }
        ) {
            Text(
                if (resending) {
                    "Enviando..."
                } else {
                    "REENVIAR CÓDIGO"
                }
            )
        }
    }
}
