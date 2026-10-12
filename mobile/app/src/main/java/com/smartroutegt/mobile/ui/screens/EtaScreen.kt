package com.smartroutegt.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smartroutegt.mobile.data.remote.ApiClient
import com.smartroutegt.mobile.data.remote.PassengerV3Api
import com.smartroutegt.mobile.network.StopDto
import com.smartroutegt.mobile.network.TripDto
import kotlinx.coroutines.launch

@Composable
fun EtaScreen(onBack:()->Unit){
 val api=remember{ApiClient.retrofit.create(PassengerV3Api::class.java)}
 val scope=rememberCoroutineScope()
 var trips by remember{mutableStateOf<List<TripDto>>(emptyList())}
 var stops by remember{mutableStateOf<List<StopDto>>(emptyList())}
 var tripId by remember{mutableStateOf<Long?>(null)}
 var stopId by remember{mutableStateOf<Long?>(null)}
 var result by remember{mutableStateOf("Selecciona un viaje y una parada para consultar el ETA.")}
 var loading by remember{mutableStateOf(false)}
 LaunchedEffect(Unit){try{trips=api.trips();stops=api.stops();tripId=trips.firstOrNull()?.id;stopId=stops.firstOrNull()?.id}catch(e:Exception){result="No se pudieron cargar los datos: ${e.message}"}}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  OutlinedButton(onClick=onBack){Text("← Volver")}
  Text("Tiempo estimado de llegada",style=MaterialTheme.typography.headlineSmall)
  Text("Viajes activos",style=MaterialTheme.typography.titleMedium)
  if(trips.isEmpty())Text("No hay viajes activos")
  trips.forEach{trip->Row{RadioButton(selected=tripId==trip.id,onClick={tripId=trip.id});Text("${trip.bus.codigo} · ${trip.route.nombre}",Modifier.padding(top=12.dp))}}
  Text("Parada de destino",style=MaterialTheme.typography.titleMedium)
  stops.filter{it.activa}.forEach{stop->Row{RadioButton(selected=stopId==stop.id,onClick={stopId=stop.id});Text(stop.nombre,Modifier.padding(top=12.dp))}}
  Button(enabled=!loading&&tripId!=null&&stopId!=null,onClick={scope.launch{loading=true;try{val response=api.eta(tripId!!,stopId!!);result=if(response.isSuccessful){val body=response.body();body?.toString()?:"No hay estimación disponible"}else "No se pudo calcular ETA (HTTP ${response.code()}). Verifica que la parada pertenezca al recorrido."}catch(e:Exception){result="Error de conexión: ${e.message}"}finally{loading=false}}},modifier=Modifier.fillMaxWidth()){Text(if(loading)"Consultando…" else "Consultar ETA real")}
  Card(Modifier.fillMaxWidth()){Text(result,Modifier.padding(16.dp))}
 }
}
