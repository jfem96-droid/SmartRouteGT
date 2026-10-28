package com.smartroutegt.mobile.ui.screens
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.smartroutegt.mobile.ui.theme.SmartRoutePalette
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.smartroutegt.mobile.data.remote.*
import kotlinx.coroutines.launch

@Composable
fun PassengerFeedbackScreen(kind:String,onBack:()->Unit){
 val context=LocalContext.current
 val api=remember{ApiClient.retrofit.create(PassengerV3Api::class.java)}
 val gps=remember{LocationServices.getFusedLocationProviderClient(context)}
 val scope=rememberCoroutineScope()
 var boarding by remember{mutableStateOf<BoardingView?>(null)}
 var stars by remember{mutableIntStateOf(5)}
 var reason by remember{mutableStateOf("Amabilidad")}
 var comment by remember{mutableStateOf("")}
 var status by remember{mutableStateOf("")}
 var sending by remember{mutableStateOf(false)}
 LaunchedEffect(Unit){try{boarding=api.myBoarding()}catch(e:Exception){status="No se pudo consultar el viaje: ${e.message}"}}
 val compliment=kind=="COMPLIMENT"
 Column(Modifier.fillMaxSize().background(SmartRoutePalette.Night).verticalScroll(rememberScrollState())){
  PassengerV3TopBar(if(compliment)"Felicitar al piloto" else "Evaluar al piloto","SmartRoute GT",onBack)
  Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
   Card(colors=CardDefaults.cardColors(containerColor=SmartRoutePalette.Panel, contentColor=SmartRoutePalette.Text),shape=RoundedCornerShape(18.dp)){
    Column(Modifier.fillMaxWidth().padding(18.dp)){
     Text(if(compliment)"Reconoce un buen servicio" else "Tu opinión mejora el servicio",color=Color.White,style=MaterialTheme.typography.titleLarge)
     Text("${boarding?.bus ?: "Sin abordaje"} · ${boarding?.route ?: ""}",color=Color.White)
    }
   }
   if(compliment){
    Text("Motivo de la felicitación",style=MaterialTheme.typography.titleMedium,color=SmartRoutePalette.Text)
    listOf("Amabilidad","Conducción segura","Puntualidad","Excelente atención","Otro").forEach { label ->
     Row(Modifier.fillMaxWidth(),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){RadioButton(reason==label,onClick={reason=label});Text(label,color=SmartRoutePalette.Text)}
    }
   }else{
    Text("Califica del 1 al 5",style=MaterialTheme.typography.titleMedium,color=SmartRoutePalette.Text)
    Row { (1..5).forEach { n -> TextButton(onClick={stars=n}){Text(if(n<=stars)"★" else "☆",color=Color(0xFFE0A000),style=MaterialTheme.typography.headlineMedium)} } }
   }
   OutlinedTextField(value=comment,onValueChange={if(it.length<=1000)comment=it},label={Text("Comentario opcional")},minLines=3,modifier=Modifier.fillMaxWidth())
   Button(enabled=!sending&&boarding?.tripId!=null,modifier=Modifier.fillMaxWidth(),onClick={
    val tripId=boarding?.tripId ?: return@Button
    if(ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){status="Autoriza la ubicación precisa";return@Button}
    sending=true;status="Comprobando ubicación..."
    gps.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,CancellationTokenSource().token).addOnSuccessListener { loc ->
     if(loc==null||loc.accuracy>100f||System.currentTimeMillis()-loc.time>30000L){sending=false;status="GPS impreciso o desactualizado. Intenta nuevamente"}
     else scope.launch{try{
      val response=api.feedback(PassengerFeedbackPayload(tripId,kind,if(compliment)null else stars,if(compliment)reason else "",comment.trim(),loc.latitude,loc.longitude))
      status=if(response.isSuccessful)"Enviado correctamente. ¡Gracias!" else when(response.code()){
       403->"No se puede enviar: verifica el abordaje o el plazo de 5 minutos."
       409->"Ya registraste esta evaluación para este abordaje."
       else->"No se pudo enviar (HTTP ${response.code()}): ${response.errorBody()?.string()?.take(150)}"
      }
     }catch(e:Exception){status="Error de conexión: ${e.message}"}finally{sending=false}}
    }.addOnFailureListener{sending=false;status="No se pudo obtener el GPS"}
   }){Text(if(sending)"Enviando..." else if(compliment)"Enviar felicitación" else "Enviar calificación")}
   if(status.isNotBlank())Text(status,color=if(status.startsWith("Enviado"))SmartRoutePalette.Mint else MaterialTheme.colorScheme.error)
  }
 }
}
