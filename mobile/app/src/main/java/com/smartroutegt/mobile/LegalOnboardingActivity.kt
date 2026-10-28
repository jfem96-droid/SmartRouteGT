package com.smartroutegt.mobile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

/**
 * Puerta de primer uso para la distribución de PRUEBAS.
 * Aceptación local, NO equivale al registro obligatorio por cuenta en el servidor.
 * La sincronización del consentimiento con MySQL requiere integración en el backend.
 */
class LegalOnboardingActivity : ComponentActivity() {
    companion object {
        const val TERMS_VERSION = "2026-10-23-borrador-pruebas-1"
        private const val PREFS = "sr_legal_onboarding"
        private const val ACCEPTED_VERSION = "accepted_version"
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // El usuario puede denegar; el resto de la app debe gestionar permisos ausentes.
        enterApplication()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val accepted = getSharedPreferences(PREFS, MODE_PRIVATE)
            .getString(ACCEPTED_VERSION, null) == TERMS_VERSION
        if (accepted) {
            enterApplication()
            return
        }
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Mint, background = Night, surface = Panel)) {
                LegalIntroScreen(onAccept = {
                    getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                        .putString(ACCEPTED_VERSION, TERMS_VERSION)
                        .putLong("accepted_at_epoch_ms", System.currentTimeMillis())
                        .putBoolean("accepted_terms", true)
                        .putBoolean("accepted_privacy", true)
                        .putBoolean("accepted_responsibility", true)
                        .apply()
                    askInitialPermissions()
                })
            }
        }
    }

    private fun askInitialPermissions() {
        val requested = mutableListOf<String>()
        for (p in listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                requested.add(p)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requested.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (requested.isEmpty()) enterApplication() else permissionLauncher.launch(requested.toTypedArray())
    }

    private fun enterApplication() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

private val Night = Color(0xFF071727)
private val Panel = Color(0xFF10283F)
private val Mint = Color(0xFF63E4C4)
private val Fog = Color(0xFFBDCED9)

@Composable
private fun LegalIntroScreen(onAccept: () -> Unit) {
    var terms by remember { mutableStateOf(false) }
    var privacy by remember { mutableStateOf(false) }
    var responsibility by remember { mutableStateOf(false) }
    val valid = terms && privacy && responsibility

    Surface(modifier = Modifier.fillMaxSize(), color = Night) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Text("SMARTROUTE GT", style = MaterialTheme.typography.labelLarge, color = Mint)
            Text("Antes de comenzar", style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold, color = Color.White)
            Text("Contrato de responsabilidades, términos y privacidad · Versión de pruebas",
                style = MaterialTheme.typography.bodyMedium, color = Fog)
            Text("Lee los documentos completos y marca las tres casillas. No se aceptan automáticamente.",
                color = Color.White)

            Section("1. Condiciones de uso",
                "SmartRoute GT es una plataforma de información y gestión del transporte. Debes utilizar datos propios y veraces, proteger tu cuenta y respetar las normas aplicables. " +
                "Las rutas, horarios, posiciones y estimaciones pueden cambiar; verifica la información antes de viajar. " +
                "El acceso de Piloto corresponde únicamente a personas autorizadas y unidades asignadas. " +
                "El uso indebido puede ocasionar suspensión de cuenta y las consecuencias que correspondan legalmente.")

            Section("2. Responsabilidades y reportes",
                "No está permitido enviar alertas deliberadamente falsas, reportes fraudulentos, suplantaciones o contenido ofensivo. " +
                "El usuario debe describir los hechos de buena fe y no interferir con la conducción. " +
                "Los reportes pueden ser revisados por personal autorizado de SmartRoute GT. " +
                "La aplicación NO tiene coordinación operativa formal con Bomberos ni con la Policía Nacional Civil y NO sustituye los canales oficiales de emergencia. " +
                "Durante estas pruebas no se debe utilizar como mecanismo de despacho de emergencias.")

            Section("3. Datos y privacidad (documento de pruebas)",
                "Según la función utilizada, el sistema puede tratar nombre, correo, identificador de usuario y rol; " +
                "ubicación mientras se usan mapas o un viaje; información de unidad/ruta; reportes y registros técnicos de seguridad. " +
                "Los permisos de ubicación y notificaciones los concede o rechaza el usuario en Android. " +
                "Los accesos de Piloto y Administrador están sujetos a sus permisos. " +
                "La conservación, solicitudes sobre datos y contacto del responsable deben documentarse y verificarse " +
                "antes de una publicación comercial. No ingreses información sensible de terceros durante las pruebas.")

            Section("4. Permisos iniciales",
                "Tras aceptar se solicitarán ubicación aproximada/precisa mientras se utiliza la app, y notificaciones en Android 13 o superior. " +
                "El sistema permite rechazarlos; algunas funciones basadas en ubicación o avisos podrían limitarse. " +
                "Cámara, imágenes y ubicación en segundo plano solamente se solicitarán cuando una función justifique su uso; " +
                "para el rol Piloto el seguimiento continuo requiere una implementación y autorización específicas.")

            Section("5. Vigencia y cambios",
                "Esta es una versión de evaluación del 23 de octubre de 2026. " +
                "Si el contrato cambia, la app volverá a solicitar aceptación. " +
                "La aceptación de esta pantalla se almacena en el dispositivo y NO constituye aún un registro de aceptación por usuario en MySQL. " +
                "La publicación definitiva exige integrar y verificar ese registro en el backend.")

            ConsentRow(terms, { terms = it }, "He leído y acepto las condiciones de uso")
            ConsentRow(privacy, { privacy = it }, "He leído la información de privacidad para las pruebas")
            ConsentRow(responsibility, { responsibility = it }, "Comprendo mis responsabilidades y que no hay despacho oficial de emergencias")
            Spacer(Modifier.height(4.dp))
            Button(onClick = onAccept, enabled = valid, modifier = Modifier.fillMaxWidth()) {
                Text("Aceptar y configurar permisos")
            }
            Text("Sin aceptación no es posible continuar. Puedes salir de la aplicación sin aceptar.",
                style = MaterialTheme.typography.bodySmall, color = Fog)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Section(title: String, description: String) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Panel).padding(17.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold, color = Mint)
        Text(description, style = MaterialTheme.typography.bodyMedium, color = Color.White)
    }
}

@Composable
private fun ConsentRow(checked: Boolean, change: (Boolean) -> Unit, label: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = change)
        Text(label, style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f), color = Color.White)
    }
}
