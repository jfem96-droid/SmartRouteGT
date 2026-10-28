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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.smartroutegt.mobile.ui.theme.SmartRoutePalette
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AccessTypeScreen(
    onPassengerSelected: () -> Unit,
    onDriverSelected: () -> Unit
) {
    val blue = SmartRoutePalette.Mint
    val navy = SmartRoutePalette.Text
    val background = SmartRoutePalette.Night
    val muted = SmartRoutePalette.Muted

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
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = navy
            )

            Text(
                text = "Tu ruta. Tu ciudad. En tiempo real.",
                fontSize = 15.sp,
                color = muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(38.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SmartRoutePalette.Navy,
                    contentColor = SmartRoutePalette.Text
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 5.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "¿Cómo deseas ingresar?",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = navy
                    )

                    Text(
                        text = "Selecciona el tipo de acceso para continuar.",
                        fontSize = 14.sp,
                        color = muted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onPassengerSelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(66.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = blue,
                            contentColor = SmartRoutePalette.Night
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "SOY USUARIO",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Rutas, buses y paradas",
                                fontSize = 12.sp
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onDriverSelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(66.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SmartRoutePalette.Mint
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "SOY PILOTO",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SmartRoutePalette.Mint
                            )

                            Text(
                                text = "Gestionar recorrido asignado",
                                fontSize = 12.sp,
                                color = muted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Sistema de Optimización de Transporte Público",
                color = muted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
