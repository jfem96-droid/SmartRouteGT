package com.smartroutegt.mobile.ui.screens

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import com.smartroutegt.mobile.ui.theme.SmartRoutePalette

/** Contraste consistente con admin navy/menta; no altera validación ni llamadas a API. */
@Composable
internal fun smartAuthTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = SmartRoutePalette.Text,
    unfocusedTextColor = SmartRoutePalette.Text,
    disabledTextColor = SmartRoutePalette.Muted,
    focusedContainerColor = SmartRoutePalette.Navy,
    unfocusedContainerColor = SmartRoutePalette.Navy,
    disabledContainerColor = SmartRoutePalette.Navy,
    focusedLabelColor = SmartRoutePalette.Mint,
    unfocusedLabelColor = SmartRoutePalette.Muted,
    disabledLabelColor = SmartRoutePalette.Muted,
    focusedBorderColor = SmartRoutePalette.Mint,
    unfocusedBorderColor = SmartRoutePalette.Border,
    disabledBorderColor = SmartRoutePalette.Border,
    cursorColor = SmartRoutePalette.Mint
)
