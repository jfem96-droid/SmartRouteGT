package com.smartroutegt.mobile.ui.screens

import com.smartroutegt.mobile.ui.theme.SmartRoutePalette

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.smartroutegt.mobile.auth.SessionManager
import com.smartroutegt.mobile.data.remote.*
import kotlinx.coroutines.launch

@Composable
fun PassengerProfileScreen(onBack:()->Unit,onLogout:()->Unit) {
 val api=remember { ApiClient.retrofit.create(PassengerProfileApi::class.java) }
 val scope=rememberCoroutineScope()
 var nombre by remember { mutableStateOf(SessionManager.getUserName().orEmpty()) }
 var correo by remember { mutableStateOf("") }
 var telefono by remember { mutableStateOf("") }
 var actual by remember { mutableStateOf("") }
 var nueva by remember { mutableStateOf("") }
 var confirmar by remember { mutableStateOf("") }
 var status by remember { mutableStateOf("") }
 var busy by remember { mutableStateOf(false) }
 var logout by remember { mutableStateOf(false) }
 LaunchedEffect(Unit) { try { val data=api.get();nombre=data.nombre;correo=data.email;telefono=data.telefono.orEmpty() } catch(e:Exception) {status="No se pudo cargar el perfil: ${e.message}"} }
 Column(Modifier.fillMaxSize().background(SmartRoutePalette.Night)) {
  PassengerV3TopBar("Mi perfil","Datos personales y seguridad",onBack)
  Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
   PassengerV3SectionCard {
    Text("Información personal",style=MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(nombre,{nombre=it},label={Text("Nombre completo")},modifier=Modifier.fillMaxWidth())
    OutlinedTextField(correo,{correo=it},label={Text("Correo electrónico")},modifier=Modifier.fillMaxWidth())
    OutlinedTextField(telefono,{telefono=it},label={Text("Teléfono")},modifier=Modifier.fillMaxWidth())
    OutlinedTextField(actual,{actual=it},label={Text("Contraseña actual para guardar")},visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth())
    Button(onClick={scope.launch {busy=true;try {val r=api.update(PassengerProfileUpdate(nombre,correo,telefono,actual));status=if(r.isSuccessful) "Datos guardados. Si cambiaste tu correo, vuelve a iniciar sesión." else "No se guardó (HTTP ${r.code()})";if(r.isSuccessful)actual=""}catch(e:Exception){status="Error: ${e.message}"}finally{busy=false}}},enabled=!busy&&nombre.isNotBlank()&&correo.isNotBlank()&&actual.isNotBlank(),modifier=Modifier.fillMaxWidth()){Text("Guardar cambios")}
   }
   PassengerV3SectionCard {
    Text("Cambiar contraseña",style=MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(actual,{actual=it},label={Text("Contraseña actual")},visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth())
    OutlinedTextField(nueva,{nueva=it},label={Text("Nueva contraseña (mínimo 8 caracteres)")},visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth())
    OutlinedTextField(confirmar,{confirmar=it},label={Text("Confirmar nueva contraseña")},visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth())
    Button(onClick={scope.launch {busy=true;try{val r=api.password(PassengerPasswordUpdate(actual,nueva));status=if(r.isSuccessful) "Contraseña actualizada" else "No se actualizó (HTTP ${r.code()})";if(r.isSuccessful){actual="";nueva="";confirmar=""}}catch(e:Exception){status="Error: ${e.message}"}finally{busy=false}}},enabled=!busy&&actual.isNotBlank()&&nueva.length>=8&&nueva==confirmar,modifier=Modifier.fillMaxWidth()){Text("Actualizar contraseña")}
   }
   if(status.isNotBlank())Text(status,color=MaterialTheme.colorScheme.primary)
   OutlinedButton(onClick={logout=true},modifier=Modifier.fillMaxWidth()){Text("Cerrar sesión")}
  }
 }
 if(logout)AlertDialog(onDismissRequest={logout=false},title={Text("Cerrar sesión")},text={Text("¿Deseas salir de SmartRoute GT?")},confirmButton={TextButton(onClick=onLogout){Text("Salir")}},dismissButton={TextButton(onClick={logout=false}){Text("Cancelar")}})
}
