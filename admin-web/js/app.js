const API="/api",KEY="smartroute_admin_session";let session,map,mapTimer,routeId,routes=[],markers=new Map(),routeLayers=new Map(),routeStopLayers=new Map(),allStopsLayer=null,selectedMapRoutes=new Set();const titles={dashboard:"Dashboard",viajes:"Viajes activos",mapa:"Mapa en vivo",rutas:"Rutas",paradas:"Paradas",buses:"Buses",pilotos:"Pilotos",sanciones:"Sanciones",alertas:"Alertas",reportes:"Reportes",auditoria:"Auditoría",usuarios:"Usuarios"};const $=id=>document.getElementById(id);
document.addEventListener("DOMContentLoaded",()=>{document.querySelectorAll("[data-view]").forEach(b=>b.onclick=()=>go(b.dataset.view));$("loginForm").onsubmit=login;$("logout").onclick=logout;$("refresh").onclick=dashboard;$("mobileMenu").onclick=()=>document.querySelector("aside").classList.toggle("open");$("newRoute").onclick=()=>openRoute();$("routeForm").onsubmit=saveRoute;restore()});
async function login(e){e.preventDefault();try{let r=await fetch(API+"/auth/login",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({email:$("email").value.trim(),password:$("password").value})}),b=await parse(r);if(!r.ok)throw Error(msg(b,"No fue posible iniciar sesión"));if(b.role!=="ADMIN")throw Error("Esta plataforma es exclusiva para administradores.");session={token:b.token,tokenType:b.tokenType||"Bearer",id:b.id,nombre:b.nombre,email:b.email,role:b.role};sessionStorage.setItem(KEY,JSON.stringify(session));showApp()}catch(e){$("loginMsg").innerHTML=`<div class="alert alert-danger">${esc(e.message)}</div>`}}
function restore(){try{session=JSON.parse(sessionStorage.getItem(KEY));session?.token&&session.role==="ADMIN"?showApp():showLogin()}catch{showLogin()}}function showLogin(){$("login").classList.remove("d-none");$("app").classList.add("d-none")}function showApp(){$("login").classList.add("d-none");$("app").classList.remove("d-none");$("adminName").textContent=session.nombre||"Administrador";$("adminEmail").textContent=session.email||"";go(location.hash.replace("#/","")||"dashboard");setTimeout(smartStartNotifications,800)}function logout(){smartStopNotifications();sessionStorage.removeItem(KEY);session=null;if(mapTimer)clearInterval(mapTimer);showLogin()}
async function req(path,opt={}){let h=new Headers(opt.headers||{});h.set("Accept","application/json");h.set("Authorization",(session.tokenType||"Bearer")+" "+session.token);if(opt.body)h.set("Content-Type","application/json");let r;try{r=await fetch(API+path,{...opt,headers:h})}catch{throw Error("No se pudo conectar con el backend.")}if(r.status===401){logout();throw Error("La sesión expiró.")}if(r.status===403)throw Error("No tienes permisos para esta operación.");return r}async function json(path,opt={}){let r=await req(path,opt),b=await parse(r);if(!r.ok)throw Error(msg(b,"Error HTTP "+r.status));return b}async function parse(r){let t=await r.text();if(!t)return null;try{return JSON.parse(t)}catch{return t}}function msg(b,f){return typeof b==="string"?b:b?.message||b?.error||f}
function go(v){if(!titles[v])v="dashboard";document.querySelectorAll(".view").forEach(x=>x.classList.toggle("active",x.id===v));document.querySelectorAll("[data-view]").forEach(x=>x.classList.toggle("active",x.dataset.view===v));$("title").textContent=titles[v];history.replaceState(null,"","#/"+v);document.querySelector("aside").classList.remove("open");({dashboard,viajes:loadActiveTrips,mapa:mapView,rutas:loadRoutes,paradas:()=>simple("/stops","stopsTable",5,x=>`<tr><td><b>${esc(x.nombre)}</b></td><td>${x.latitud}</td><td>${x.longitud}</td><td>${x.radioPermitido} m</td><td>${pill(x.activa?"Activa":"Inactiva",x.activa?"success":"neutral")}</td></tr>`),buses:()=>simple("/buses","busesTable",5,x=>`<tr><td>${x.id}</td><td><b>${esc(x.codigo)}</b></td><td>${esc(x.placa)}</td><td>${x.capacidad??"—"}</td><td>${pill(x.activa?"Activo":"Inactivo",x.activa?"success":"neutral")}</td></tr>`),pilotos:()=>simple("/drivers","driversTable",6,x=>`<tr><td>${x.id}</td><td><b>${esc(x.nombre)}</b></td><td>${esc(x.licencia)}</td><td>${esc(x.telefono||"—")}</td><td>${x.sancionesConfirmadas??x.sanciones??0}</td><td>${pill(x.activo?"Activo":"Suspendido",x.activo?"success":"danger")}</td></tr>`),sanciones:()=>simple("/violations","violationsTable",7,x=>`<tr><td>${x.id}</td><td>${esc(x.driver?.nombre||"—")}</td><td>${esc(x.bus?.codigo||"—")}</td><td>${esc(smartTipoSancionES(x.tipo))}</td><td>${esc(x.descripcion||"—")}</td><td>${date(x.fechaHora)}</td><td>${state(x.estado)}</td></tr>`),alertas:()=>simple("/passenger-alerts","alertsTable",5,x=>`<tr><td>${x.id}</td><td><b>${esc(x.tipo||"—")}</b></td><td>${esc(x.descripcion||"—")}</td><td>${date(x.fechaHora)}</td><td>${state(x.estado)}</td></tr>`),reportes:()=>{loadReports();smartLoadDriverReports();smartLoadFeedback()},auditoria:()=>simple("/audit-logs","auditTable",6,x=>`<tr><td>${date(x.fechaHora)}</td><td><b>${esc(x.actor||"SISTEMA")}</b></td><td>${esc(x.accion||"—")}</td><td>${esc(x.entidad||"—")}</td><td>${x.entidadId??"—"}</td><td>${esc(x.descripcion||"—")}</td></tr>`),usuarios:()=>simple("/users","usersTable",4,x=>`<tr><td><b>${esc(x.nombre)}</b></td><td>${esc(x.email)}</td><td>${esc(x.role)}</td><td>${pill(x.activo?"Activo":"Inactivo",x.activo?"success":"neutral")}</td></tr>`)}[v])?.();document.dispatchEvent(new CustomEvent("smartroute:view",{detail:v}));if(v==="mapa"&&map)setTimeout(()=>map.invalidateSize(),100)}
async function dashboard(){try{let[b,d,t,v,a]=await Promise.all([json("/buses"),json("/drivers"),json("/trips/active"),json("/violations"),json("/passenger-alerts")]);$("mBuses").textContent=b.filter(x=>x.activa).length;$("mDrivers").textContent=d.filter(x=>x.activo).length;$("mTrips").textContent=t.length;$("mViolations").textContent=v.filter(x=>x.estado==="PENDING").length;$("mAlerts").textContent=a.filter(x=>x.estado==="PENDING").length;$("dashTable").innerHTML=(await Promise.all(b.slice(0,6).map(async x=>{let l=await latest(x.id);return `<tr><td><b>${esc(x.codigo)}</b></td><td>${esc(x.placa)}</td><td>${pill(x.activa?"Activo":"Inactivo",x.activa?"success":"neutral")}</td><td>${l?speed(l.velocidad):"Sin GPS"}</td><td>${date(l?.fechaHora)}</td></tr>`}))).join("")||empty(5,"No hay buses.")}catch(e){error(e)}}
async function latest(id){try{let r=await req(`/locations/bus/${id}/latest`);return r.ok?await parse(r):null}catch{return null}}function initMap(){
 if(map)return;
 map=L.map("map").setView([14.6349,-90.5069],12);
 L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",{maxZoom:19,attribution:"&copy; OpenStreetMap contributors"}).addTo(map)
}
const SMART_ROUTE_COLORS=["#2563eb","#16a34a","#f97316","#7c3aed","#db2777","#0891b2","#ca8a04","#dc2626"];
function smartRouteColor(route,index){return SMART_ROUTE_COLORS[index%SMART_ROUTE_COLORS.length]}
function smartClearRouteLayers(){
 routeLayers.forEach(x=>map?.removeLayer(x));routeStopLayers.forEach(x=>map?.removeLayer(x));
 routeLayers.clear();routeStopLayers.clear()
}
function smartRouteSelector(data){
 let host=document.getElementById("smartRouteSelector");
 if(!host){
  let me=$("map");if(!me)return;
  host=document.createElement("div");host.id="smartRouteSelector";host.className="smart-route-selector";
  me.parentElement.insertBefore(host,me)
 }
 host.innerHTML=`<div class="smart-route-selector-head"><div><small>RECORRIDOS</small><b>Rutas visibles</b></div><div class="smart-route-selector-actions"><button type="button" id="smartRoutesAll">Todas</button><button type="button" id="smartRoutesNone">Ninguna</button></div></div><div class="smart-route-selector-list">${data.map((r,i)=>`<label class="smart-route-option"><input type="checkbox" data-route-map="${r.id}" ${selectedMapRoutes.has(r.id)?"checked":""}><span class="smart-route-swatch" style="background:${smartRouteColor(r,i)}"></span><span><b>${esc(r.codigo)}</b><small>${esc(r.nombre)}</small></span></label>`).join("")||'<span class="text-muted">No hay rutas activas.</span>'}</div>`;
 host.querySelectorAll("[data-route-map]").forEach(input=>input.onchange=async()=>{
  let id=Number(input.dataset.routeMap);input.checked?selectedMapRoutes.add(id):selectedMapRoutes.delete(id);
  await smartDrawSelectedRoutes(data)
 });
 host.querySelector("#smartRoutesAll")?.addEventListener("click",async()=>{
  data.forEach(r=>selectedMapRoutes.add(r.id));host.querySelectorAll("[data-route-map]").forEach(x=>x.checked=true);await smartDrawSelectedRoutes(data)
 });
 host.querySelector("#smartRoutesNone")?.addEventListener("click",()=>{
  selectedMapRoutes.clear();host.querySelectorAll("[data-route-map]").forEach(x=>x.checked=false);smartClearRouteLayers()
 })
}

async function smartDrawAllStops(){
 try{
  const stops=(await json("/stops")).filter(s=>s.activa!==false);

  if(allStopsLayer){
   map.removeLayer(allStopsLayer);
  }

  allStopsLayer=L.layerGroup().addTo(map);

  stops.forEach(stop=>{
   const lat=Number(stop.latitud);
   const lon=Number(stop.longitud);

   if(!Number.isFinite(lat)||!Number.isFinite(lon))return;

   const icon=L.divIcon({
    className:"smart-stop-marker-wrap",
    html:`
     <div class="smart-stop-marker">
      <i class="bi bi-geo-alt-fill"></i>
     </div>
    `,
    iconSize:[26,34],
    iconAnchor:[13,30],
    popupAnchor:[0,-27]
   });

   const marker=L.marker([lat,lon],{
    icon,
    zIndexOffset:200
   });

   marker.bindPopup(`
    <div class="smart-stop-popup">
     <small>PARADA SMARTROUTE</small>
     <h6>${esc(stop.nombre)}</h6>
     <div>
      <span>Estado</span>
      <b>${stop.activa!==false?"Activa":"Inactiva"}</b>
     </div>
     <div>
      <span>Radio permitido</span>
      <b>${stop.radioPermitido??"—"} m</b>
     </div>
     <div>
      <span>Coordenadas</span>
      <b>${lat.toFixed(5)}, ${lon.toFixed(5)}</b>
     </div>
    </div>
   `);

   allStopsLayer.addLayer(marker);
  });

  // Mantener checkpoints de rutas por encima de las paradas base.
  routeStopLayers.forEach(group=>{
   group.eachLayer(layer=>{
    if(layer.setZIndexOffset)layer.setZIndexOffset(800);
   });
  });

 }catch(e){
  console.warn("No se pudieron cargar todas las paradas",e);
 }
}

async function smartRoadRoute(points){
 if(points.length<2)return points;

 try{
  // OSRM usa LONGITUD,LATITUD
  const coordinates=points
   .map(([lat,lon])=>`${lon},${lat}`)
   .join(";");

  const url=
   `https://router.project-osrm.org/route/v1/driving/${coordinates}`+
   `?overview=full&geometries=geojson&steps=false`;

  const response=await fetch(url);

  if(!response.ok){
   throw new Error(`OSRM HTTP ${response.status}`);
  }

  const data=await response.json();

  if(data.code!=="Ok" || !data.routes?.length){
   throw new Error(data.code||"NoRoute");
  }

  // GeoJSON devuelve LONGITUD,LATITUD.
  // Leaflet necesita LATITUD,LONGITUD.
  return data.routes[0].geometry.coordinates.map(
   ([lon,lat])=>[lat,lon]
  );

 }catch(e){
  console.warn(
   "No se pudo calcular recorrido por calles; usando línea directa.",
   e
  );

  // Fallback para que la ruta nunca desaparezca.
  return points;
 }
}

async function smartDrawSelectedRoutes(data){
 smartClearRouteLayers();

 let bounds=[];

 for(let i=0;i<data.length;i++){
  const route=data[i];

  if(!selectedMapRoutes.has(route.id))continue;

  let stops=[];

  try{
   stops=await json(`/route-stops/route?routeId=${route.id}`);
  }catch(e){
   console.warn(`No se pudieron cargar paradas de ${route.codigo}`,e);
   continue;
  }

  // Respetamos SIEMPRE el orden configurado de la ruta.
  stops=[...stops].sort(
   (a,b)=>(Number(a.orden)||0)-(Number(b.orden)||0)
  );

  const validStops=stops.filter(rs=>
   Number.isFinite(Number(rs.stop?.latitud)) &&
   Number.isFinite(Number(rs.stop?.longitud))
  );

  const points=validStops.map(rs=>[
   Number(rs.stop.latitud),
   Number(rs.stop.longitud)
  ]);

  const color=smartRouteColor(route,i);

  /*
   * CHECKPOINTS
   * Se dibujan aunque la ruta solo tenga una parada.
   */
  const group=L.layerGroup().addTo(map);

  validStops.forEach(rs=>{
   const pos=[
    Number(rs.stop.latitud),
    Number(rs.stop.longitud)
   ];

   const cp=L.marker(pos,{
    zIndexOffset:800,
    icon:L.divIcon({
     className:"smart-checkpoint-wrap",
     html:`
      <span
       class="smart-checkpoint"
       style="--route-color:${color}"
      >
       ${rs.orden??"•"}
      </span>
     `,
     iconSize:[30,30],
     iconAnchor:[15,15]
    })
   });

   cp.bindPopup(`
    <b>
     Parada ${rs.orden??"—"} ·
     ${esc(rs.stop.nombre)}
    </b>
    <br>
    ${esc(route.codigo)} · ${esc(route.nombre)}
    <br>
    Radio: ${rs.stop.radioPermitido??"—"} m
   `);

   group.addLayer(cp);
   bounds.push(pos);
  });

  routeStopLayers.set(route.id,group);

  /*
   * RECORRIDO POR CALLES
   */
  if(points.length>=2){
   const roadPoints=await smartRoadRoute(points);

   const line=L.polyline(roadPoints,{
    color,
    weight:6,
    opacity:.88,
    lineJoin:"round",
    lineCap:"round"
   }).addTo(map);

   line.bindPopup(`
    <b>${esc(route.codigo)} · ${esc(route.nombre)}</b>
    <br>
    ${points.length} checkpoints
    <br>
    <small>Recorrido calculado por red vial</small>
   `);

   routeLayers.set(route.id,line);
  }else{
   console.warn(
    `Ruta ${route.codigo}: solamente ${points.length} checkpoint(s) válido(s).`
   );
  }
 }

 if(bounds.length>=2){
  map.fitBounds(bounds,{
   padding:[35,35],
   maxZoom:15
  });
 }
}

async function smartLoadMapRoutes(){
 let data=[];try{data=(await json("/routes")).filter(r=>r.activa!==false)}catch(e){console.warn("No se pudieron cargar rutas",e);return}
 if(selectedMapRoutes.size===0&&data.length)selectedMapRoutes.add(data[0].id);
 let valid=new Set(data.map(r=>r.id));[...selectedMapRoutes].forEach(id=>{if(!valid.has(id))selectedMapRoutes.delete(id)});
 smartRouteSelector(data);await smartDrawSelectedRoutes(data)
}
async function mapView(){initMap();await Promise.all([refreshMap(),smartLoadMapRoutes(),smartDrawAllStops()]);if(!mapTimer)mapTimer=setInterval(refreshMap,10000)}
async function refreshMap() {
 try {
  const monitoring = await json("/monitoring/buses");

  const fleet = monitoring
   .filter(bus =>
    bus.activo !== false &&
    bus.driverId != null &&
    bus.routeId != null
   )
   .map(bus => ({
    b: {
     id: bus.busId,
     codigo: bus.codigo,
     placa: bus.placa,
     capacidad: bus.capacidad,
     activa: bus.activo,
     piloto: bus.piloto,
     driverId: bus.driverId,
     ruta: bus.ruta,
     routeId: bus.routeId,
     viajeActivo: bus.viajeActivo
    },
    l: bus.latitud != null && bus.longitud != null
     ? {
        latitud: bus.latitud,
        longitud: bus.longitud,
        velocidad: bus.velocidad,
        fechaHora: bus.ultimaActualizacion
       }
     : null
   }));

  const gpsCount = fleet.filter(x => x.l).length;

  $("liveList").innerHTML = `
   <div class="smart-fleet-summary">
    <div>
     <span>Flota</span>
     <strong>${fleet.length}</strong>
    </div>
    <div>
     <span>Con GPS</span>
     <strong class="gps-online">${gpsCount}</strong>
    </div>
    <div>
     <span>Sin GPS</span>
     <strong class="gps-offline">${fleet.length - gpsCount}</strong>
    </div>
   </div>

   <div class="smart-fleet-search">
    <i class="bi bi-search"></i>
    <input
     id="fleetSearch"
     type="search"
     placeholder="Buscar bus, placa, piloto o ruta..."
     autocomplete="off"
    >
   </div>

   <div id="fleetItems">
    ${
     fleet.length
      ? fleet.map(({b,l}) => smartFleetCard(b,l)).join("")
      : `<div class="text-center text-muted py-4">
          <i class="bi bi-bus-front fs-3 d-block mb-2"></i>
          No hay buses actualmente en servicio
         </div>`
    }
   </div>
  `;

  const currentIds = new Set(fleet.map(x => x.b.id));

  markers.forEach((marker,id) => {
   if (!currentIds.has(id)) {
    map.removeLayer(marker);
    markers.delete(id);
   }
  });

  fleet.forEach(({b,l}) => {
   if (!l) {
    const oldMarker = markers.get(b.id);

    if (oldMarker) {
     map.removeLayer(oldMarker);
     markers.delete(b.id);
    }

    return;
   }

   const lat = Number(l.latitud);
   const lon = Number(l.longitud);

   if (!Number.isFinite(lat) || !Number.isFinite(lon)) return;

   const position = [lat,lon];

   const icon = L.divIcon({
    className: "smart-bus-marker-wrap",
    html: `
     <div class="smart-bus-marker">
      <i class="bi bi-bus-front-fill"></i>
      <span>${esc(b.codigo)}</span>
     </div>
    `,
    iconSize: [74,44],
    iconAnchor: [37,22]
   });

   let marker = markers.get(b.id);

   if (marker) {
    marker.setLatLng(position);
    marker.setIcon(icon);
   } else {
    marker = L.marker(position,{icon}).addTo(map);
    markers.set(b.id,marker);
   }

   marker.bindPopup(`
    <div class="smart-bus-popup">
     <small>SMARTROUTE GT</small>
     <h6>${esc(b.codigo)}</h6>

     <div>
      <span>Placa</span>
      <b>${esc(b.placa)}</b>
     </div>

     <div>
      <span>Piloto</span>
      <b>${esc(b.piloto || "—")}</b>
     </div>

     <div>
      <span>Ruta</span>
      <b>${esc(b.ruta || "—")}</b>
     </div>

     <div>
      <span>Velocidad</span>
      <b>${speed(l.velocidad)}</b>
     </div>

     <div>
      <span>Última ubicación</span>
      <b>${date(l.fechaHora)}</b>
     </div>
    </div>
   `);
  });

  const search = $("fleetSearch");

  if (search) {
   search.oninput = () => {
    const term = search.value.toLowerCase().trim();

    document
     .querySelectorAll("#fleetItems .smart-fleet-card")
     .forEach(card => {
      card.style.display =
       card.dataset.search.includes(term) ? "" : "none";
     });
   };
  }

 } catch(e) {
  error(e);
 }
}

function smartFleetCard(bus,location) {
 const hasGps = !!location;

 const searchText =
  `${bus.codigo || ""} ${bus.placa || ""} ${bus.piloto || ""} ${bus.ruta || ""}`.toLowerCase();

 return `
  <button
   type="button"
   class="smart-fleet-card ${hasGps ? "has-gps" : "no-gps"}"
   data-search="${esc(searchText)}"
   onclick="${hasGps ? `smartFocusBus(${bus.id})` : ""}"
  >
   <div class="smart-fleet-icon">
    <i class="bi bi-bus-front-fill"></i>
    <span class="smart-gps-dot"></span>
   </div>

   <div class="smart-fleet-info">
    <strong>${esc(bus.codigo)}</strong>
    <span>${esc(bus.placa)}</span>

    <small>
     <i class="bi bi-person-fill"></i>
     ${esc(bus.piloto || "Sin piloto")}
    </small>

    <small>
     <i class="bi bi-signpost-split-fill"></i>
     ${esc(bus.ruta || "Sin ruta")}
    </small>

    <small>
     ${
      hasGps
       ? `<i class="bi bi-geo-alt"></i> ${date(location.fechaHora)}`
       : `<i class="bi bi-slash-circle"></i> Sin señal GPS`
     }
    </small>
   </div>

   <div class="smart-fleet-speed">
    ${
     hasGps
      ? `<strong>${speed(location.velocidad)}</strong>
         <span>velocidad</span>`
      : `<strong>—</strong>
         <span>sin datos</span>`
    }
   </div>
  </button>
 `;
}

window.smartFocusBus = id => {
 const marker = markers.get(id);

 if (!marker || !map) return;

 map.flyTo(
  marker.getLatLng(),
  Math.max(map.getZoom(),16),
  {
   animate:true,
   duration:.8
  }
 );

 setTimeout(() => marker.openPopup(),450);
};

async function loadRoutes(){try{routes=await json("/routes");$("routesTable").innerHTML=routes.map(x=>`<tr><td>${x.id}</td><td><b>${esc(x.codigo)}</b></td><td>${esc(x.nombre)}</td><td>${pill(x.activa?"Activa":"Inactiva",x.activa?"success":"neutral")}</td><td><button class="table-btn edit" onclick="editRoute(${x.id})">Editar</button><button class="table-btn ${x.activa?"off":"on"}" onclick="toggleRoute(${x.id},${!x.activa})">${x.activa?"Desactivar":"Activar"}</button></td></tr>`).join("")||empty(5,"No hay rutas.")}catch(e){$("routesTable").innerHTML=empty(5,e.message)}}function openRoute(x){routeId=x?.id||null;$("routeTitle").textContent=x?"Editar ruta":"Nueva ruta";$("routeCode").value=x?.codigo||"";$("routeName").value=x?.nombre||"";$("routeActive").checked=x?!!x.activa:true;bootstrap.Modal.getOrCreateInstance($("routeModal")).show()}window.editRoute=id=>openRoute(routes.find(x=>x.id===id));async function saveRoute(e){e.preventDefault();try{await json(routeId?`/routes/${routeId}`:"/routes",{method:routeId?"PUT":"POST",body:JSON.stringify({codigo:$("routeCode").value.trim(),nombre:$("routeName").value.trim(),activa:$("routeActive").checked})});bootstrap.Modal.getInstance($("routeModal"))?.hide();loadRoutes()}catch(e){error(e)}}window.toggleRoute=async(id,on)=>{try{let r=await req(on?`/routes/${id}/activar`:`/routes/${id}`,{method:on?"PUT":"DELETE"});if(!r.ok)throw Error(msg(await parse(r),"No se pudo cambiar la ruta."));loadRoutes()}catch(e){error(e)}};
async function loadReports(){try{let d=await json("/passenger-reports");$("reportsTable").innerHTML=d.map(x=>`<tr><td>${x.id}</td><td><b>${esc(smartReportLabel(x.tipo))}</b></td><td>${esc(x.descripcion||"—")}</td><td>${date(x.fechaHora)}</td><td>${state(x.estado)}</td><td>${x.estado==="PENDING"?`<button class="table-btn edit" onclick="reportAction(${x.id},'review')">Revisar</button>`:x.estado==="UNDER_REVIEW"?`<button class="table-btn on" onclick="reportAction(${x.id},'resolve')">Resolver</button><button class="table-btn off" onclick="reportAction(${x.id},'reject')">Rechazar</button>`:"—"}</td></tr>`).join("")||empty(6,"No hay reportes.")}catch(e){$("reportsTable").innerHTML=empty(6,e.message)}}window.reportAction=async(id,a)=>{let o={method:"PUT"};if(a!=="review"){let t=await window.smartPrompt(
  a==="resolve"
    ? "Escribe la resolución que se dará al reporte."
    : "Explica el motivo por el que se rechaza el reporte.",
  {
    title: a==="resolve" ? "Resolver reporte" : "Rechazar reporte",
    label: a==="resolve" ? "Resolución" : "Motivo del rechazo",
    placeholder: a==="resolve"
      ? "Describe cómo se resolvió el reporte..."
      : "Escribe el motivo del rechazo...",
    acceptText: a==="resolve" ? "Resolver" : "Rechazar",
    danger: a==="reject"
  }
);if(!t?.trim())return;o.body=JSON.stringify({resolucion:t.trim()})}try{await json(`/passenger-reports/${id}/${a}`,o);loadReports()}catch(e){error(e)}};
async function simple(path,id,n,render){try{let d=await json(path);$(id).innerHTML=d.map(render).join("")||empty(n,"No hay registros.")}catch(e){$(id).innerHTML=empty(n,e.message)}}function pill(t,c){return `<span class="pill ${c}">${esc(t)}</span>`}function state(s){let x=({PENDING:["Pendiente","warning"],UNDER_REVIEW:["En revisión","info"],REVIEWED:["Revisada","info"],CONFIRMED:["Confirmada","danger"],RESOLVED:["Resuelta","success"],REJECTED:["Rechazada","neutral"],CANCELLED:["Cancelada","neutral"]}[s]||[s||"—","neutral"]);return pill(...x)}function speed(v){v=Number(v);return Number.isFinite(v)?v.toFixed(1)+" km/h":"—"}function smartParseDate(v){if(!v)return null;let raw=String(v).trim().replace(' ','T');if(/^\d{4}-\d{2}-\d{2}T/.test(raw)&&!/(Z|[+-]\d{2}:?\d{2})$/i.test(raw))raw+='Z';const d=new Date(raw);return Number.isFinite(d.getTime())?d:null}
window.smartParseDate=smartParseDate;
const smartDateFormatter=new Intl.DateTimeFormat('es-GT',{timeZone:'America/Guatemala',year:'numeric',month:'2-digit',day:'2-digit',hour:'2-digit',minute:'2-digit',hour12:false});
function date(v){const d=smartParseDate(v);return d?`<time data-order="${d.getTime()}" datetime="${d.toISOString()}" title="Hora de Guatemala">${smartDateFormatter.format(d)}</time>`:"—"}function esc(v){return String(v??"").replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll('"',"&quot;").replaceAll("'","&#039;")}function empty(n,t){return `<tr><td colspan="${n}" class="empty">${esc(t)}</td></tr>`}function error(e){$("message").innerHTML=`<div class="alert alert-danger">${esc(e.message||e)}</div>`}
/* =========================================================
   SMARTROUTE GT - NOTIFICACIONES EN TIEMPO REAL
   Sanciones: aviso suave
   SOS: alerta prioritaria
   ========================================================= */

let smartNotificationTimer = null;
let smartNotificationReady = false;
let knownViolationIds = new Set();
let knownAlertIds = new Set();
let knownDriverReportIds = new Set();
let smartDriverReportReady = false;
let smartAudioContext = null;

function smartInitAudio() {
    try {
        if (!smartAudioContext) {
            smartAudioContext = new (window.AudioContext || window.webkitAudioContext)();
        }

        if (smartAudioContext.state === "suspended") {
            smartAudioContext.resume();
        }
    } catch (_) {}
}

function smartTone(frequency = 620, duration = 0.12, volume = 0.035) {
    try {
        if (!smartAudioContext) return;

        const osc = smartAudioContext.createOscillator();
        const gain = smartAudioContext.createGain();

        osc.type = "sine";
        osc.frequency.value = frequency;

        gain.gain.setValueAtTime(volume, smartAudioContext.currentTime);
        gain.gain.exponentialRampToValueAtTime(
            0.001,
            smartAudioContext.currentTime + duration
        );

        osc.connect(gain);
        gain.connect(smartAudioContext.destination);

        osc.start();
        osc.stop(smartAudioContext.currentTime + duration);
    } catch (_) {}
}

function smartSanctionSound() {
    smartTone(610, 0.11, 0.025);
}

function smartSosSound() {
    smartTone(760, 0.14, 0.045);

    setTimeout(() => {
        smartTone(920, 0.18, 0.045);
    }, 180);
}

function smartEnsureNotificationContainer() {
    let container = document.getElementById("smartNotifications");

    if (!container) {
        container = document.createElement("div");
        container.id = "smartNotifications";
        container.className = "smart-notification-container";
        document.body.appendChild(container);
    }

    return container;
}

function smartNotify({
    title,
    message,
    type = "info",
    icon = "bi-bell",
    duration = 7000,
    actionView = null
}) {
    const container = smartEnsureNotificationContainer();

    const item = document.createElement("div");
    item.className = `smart-live-notification ${type}`;

    item.innerHTML = `
        <div class="smart-live-icon">
            <i class="bi ${icon}"></i>
        </div>

        <div class="smart-live-body">
            <strong>${esc(title)}</strong>
            <span>${esc(message)}</span>
        </div>

        <button class="smart-live-close" type="button" aria-label="Cerrar">
            <i class="bi bi-x-lg"></i>
        </button>
    `;

    const close = () => {
        item.classList.add("leaving");
        setTimeout(() => item.remove(), 220);
    };

    item.querySelector(".smart-live-close").onclick = e => {
        e.stopPropagation();
        close();
    };

    if (actionView) {
        item.classList.add("clickable");

        item.onclick = e => {
            if (e.target.closest(".smart-live-close")) return;
            go(actionView);
            close();
        };
    }

    container.appendChild(item);

    requestAnimationFrame(() => item.classList.add("show"));

    if (duration > 0) {
        setTimeout(close, duration);
    }
}

function smartShowSosAlert(alert) {
    smartSosSound();

    const description =
        alert.descripcion?.trim() ||
        "Se recibió una solicitud de emergencia de un pasajero.";

    smartNotify({
        title: "¡ALERTA SOS!",
        message: description,
        type: "sos",
        icon: "bi-exclamation-triangle-fill",
        duration: 15000,
        actionView: "alertas"
    });

    const existing = document.getElementById("smartSosBanner");
    if (existing) existing.remove();

    const banner = document.createElement("div");
    banner.id = "smartSosBanner";
    banner.className = "smart-sos-banner";

    banner.innerHTML = `
        <div class="smart-sos-symbol">
            <i class="bi bi-exclamation-triangle-fill"></i>
        </div>

        <div>
            <small>EMERGENCIA DE PASAJERO</small>
            <strong>Nueva alerta SOS recibida</strong>
            <span>${esc(description)}</span>
        </div>

        <button id="smartSosOpen" class="btn btn-light btn-sm">
            Ver alerta
        </button>

        <button id="smartSosDismiss" class="smart-sos-dismiss" type="button">
            <i class="bi bi-x-lg"></i>
        </button>
    `;

    document.body.appendChild(banner);

    document.getElementById("smartSosOpen").onclick = () => {
        go("alertas");
        banner.remove();
    };

    document.getElementById("smartSosDismiss").onclick = () => banner.remove();

    setTimeout(() => {
        if (banner.isConnected) banner.remove();
    }, 20000);
}

async function smartCheckNotifications() {
    if (!session?.token) return;

    try {
        const [violations, alerts, driverReports] = await Promise.all([
            json("/violations"),
            json("/passenger-alerts"),
            json("/driver-reports").catch(() => null)
        ]);

        if (!smartNotificationReady) {
            violations.forEach(x => knownViolationIds.add(String(x.id)));
            alerts.forEach(x => knownAlertIds.add(String(x.id)));
            smartNotificationReady = true;
            if(Array.isArray(driverReports)){
                driverReports.forEach(x=>knownDriverReportIds.add(String(x.id)));
                smartDriverReportReady=true;
            }
            smartUpdateIncidentBadge(driverReports);
            return;
        }

        const newViolations = violations.filter(
            x => !knownViolationIds.has(String(x.id))
        );

        const newAlerts = alerts.filter(
            x => !knownAlertIds.has(String(x.id))
        );

        violations.forEach(x => knownViolationIds.add(String(x.id)));
        alerts.forEach(x => knownAlertIds.add(String(x.id)));

        if(Array.isArray(driverReports)){
            if(!smartDriverReportReady){
                driverReports.forEach(x=>knownDriverReportIds.add(String(x.id)));
                smartDriverReportReady=true;
            }else{
                const newReports=driverReports.filter(x=>!knownDriverReportIds.has(String(x.id)));
                driverReports.forEach(x=>knownDriverReportIds.add(String(x.id)));
                newReports.forEach(r=>{
                    if(r.severity==="HIGH")smartSanctionSound();
                    smartNotify({title:r.severity==="HIGH"?"Incidente grave de piloto":"Nuevo reporte de piloto",
                        message:`#${r.id} · ${r.driver?.nombre||"Piloto"} · ${r.description||r.type||"Incidente"}`,
                        type:r.severity==="HIGH"?"sanction":"info",icon:"bi-clipboard2-pulse",
                        duration:r.severity==="HIGH"?12000:8000,actionView:"reportes"});
                });
                if(newReports.length&&location.hash==="#/reportes")smartLoadDriverReports();
            }
            smartUpdateIncidentBadge(driverReports);
        }
        for (const violation of newViolations) {
            smartSanctionSound();

            const driver =
                violation.driver?.nombre ||
                "Piloto";

            const bus =
                violation.bus?.codigo
                    ? ` · ${violation.bus.codigo}`
                    : "";

            smartNotify({
                title: "Nueva sanción detectada",
                message: `${driver}${bus} · ${violation.tipo || "Infracción"}`,
                type: "sanction",
                icon: "bi-shield-exclamation",
                duration: 8000,
                actionView: "sanciones"
            });
        }

        for (const alert of newAlerts) {
            if (String(alert.tipo || "").toUpperCase() === "SOS") {
                smartShowSosAlert(alert);
            } else {
                smartTone(540, 0.1, 0.02);

                smartNotify({
                    title: "Nueva alerta de pasajero",
                    message: alert.descripcion || alert.tipo || "Nueva alerta",
                    type: "passenger",
                    icon: "bi-bell-fill",
                    duration: 8000,
                    actionView: "alertas"
                });
            }
        }

        if (newViolations.length && typeof window.loadViolations === "function") {
            const current = location.hash.replace("#/", "");
            if (current === "sanciones") window.loadViolations();
        }

        if (newAlerts.length && typeof window.loadAlerts === "function") {
            const current = location.hash.replace("#/", "");
            if (current === "alertas") window.loadAlerts();
        }

        if (newViolations.length || newAlerts.length) {
            dashboard();
        }

    } catch (e) {
        /* El monitor no interrumpe el uso normal del Admin si falla una consulta. */
        console.debug("SmartRoute notification monitor:", e.message);
    }
}

function smartStartNotifications() {
    if (smartNotificationTimer) {
        clearInterval(smartNotificationTimer);
    }

    smartNotificationReady = false;
    knownViolationIds.clear();
    knownAlertIds.clear();
    knownDriverReportIds.clear();
    smartDriverReportReady=false;

    smartCheckNotifications();

    smartNotificationTimer = setInterval(
        smartCheckNotifications,
        7000
    );
}

function smartStopNotifications() {
    if (smartNotificationTimer) {
        clearInterval(smartNotificationTimer);
        smartNotificationTimer = null;
    }

    smartNotificationReady = false;
    knownViolationIds.clear();
    knownAlertIds.clear();
    knownDriverReportIds.clear();
    smartDriverReportReady=false;

    document.getElementById("smartSosBanner")?.remove();
    document.getElementById("smartNotifications")?.replaceChildren();
}

/* Chrome/Safari permiten audio únicamente después de una interacción real. */
function smartUnlockAudio() {
    smartInitAudio();

    if (smartAudioContext?.state === "running") {
        document.removeEventListener("click", smartUnlockAudio);
        document.removeEventListener("keydown", smartUnlockAudio);
    }
}

document.addEventListener("click", smartUnlockAudio);
document.addEventListener("keydown", smartUnlockAudio);

/* SMARTROUTE_GT_TRADUCCIONES_REPORTES_V2 */
function smartReportLabel(value) {
 const labels={
  INCIDENT:"Incidente", ACCIDENT:"Accidente", MECHANICAL:"Falla mecánica",
  HIGH:"Alta", MEDIUM:"Media", LOW:"Baja",
  RECKLESS_DRIVING:"Conducción imprudente", MISSED_STOP:"Parada omitida",
  BAD_BEHAVIOR:"Mala conducta", OTHER:"Otro",
  PENDING:"Pendiente", UNDER_REVIEW:"En revisión", RESOLVED:"Resuelto",
  REJECTED:"Rechazado", REVIEWED:"Revisado", CONFIRMED:"Confirmado",
  CANCELLED:"Cancelado"
 };
 return labels[String(value??"").toUpperCase()] || String(value??"—");
}

async function smartLoadDriverReports(){
 const tbody=document.getElementById("driverReportsTable");if(!tbody||!session?.token)return;
 try{
  const items=await json("/driver-reports");
  tbody.innerHTML=items.map(r=>{
   const sev=r.severity==="HIGH"?"danger":r.severity==="MEDIUM"?"warning":"neutral";
   const lat=Number(r.latitude),lon=Number(r.longitude);const coords=r.latitude!=null&&r.longitude!=null&&Number.isFinite(lat)&&Number.isFinite(lon)&&Math.abs(lat)<=90&&Math.abs(lon)<=180?`<button class="table-btn edit" onclick="smartFocusDriverReport(${lat},${lon},${Number(r.id)})">Ver mapa</button>`:"—";
   const next=r.status==="PENDING"?`<button class="table-btn edit" onclick="smartSetDriverReport(${Number(r.id)},'UNDER_REVIEW')">Revisar</button>`:r.status==="UNDER_REVIEW"?`<button class="table-btn on" onclick="smartSetDriverReport(${Number(r.id)},'RESOLVED')">Resolver</button>`:"—";
   return `<tr><td>${Number(r.id)}</td><td>${esc(r.driver?.nombre||"—")}</td><td>${esc(smartReportLabel(r.type))}</td><td><span class="pill ${sev}">${esc(smartReportLabel(r.severity))}</span></td><td>${esc(r.description)}${r.resolution?`<div class="text-success small mt-1"><b>Resolución:</b> ${esc(r.resolution)}</div>`:""}</td><td>${coords}</td><td>${date(r.createdAt)}</td><td>${state(r.status)}</td><td>${next}</td></tr>`;
  }).join("")||empty(9,"No hay reportes de pilotos.");
 }catch(e){tbody.innerHTML=empty(9,e.message)}
}
window.smartSetDriverReport=function(id,status){
 if(status==="RESOLVED"){
  document.getElementById("smartDriverResolveId").value=String(id);
  document.getElementById("smartDriverResolveText").value="";
  bootstrap.Modal.getOrCreateInstance(document.getElementById("smartDriverResolveModal")).show();
  return;
 }
 smartSaveDriverStatus(id,status,null);
};
async function smartSaveDriverStatus(id,status,resolution){
 try{
  await json(`/driver-reports/${id}/status`,{method:"PUT",body:JSON.stringify({status,resolution})});
  await smartLoadDriverReports();
 }catch(e){error(e);throw e}
}
window.smartConfirmDriverResolution=async function(e){
 e.preventDefault();const id=Number(document.getElementById("smartDriverResolveId").value);
 const resolution=document.getElementById("smartDriverResolveText").value.trim();
 if(!resolution){document.getElementById("smartDriverResolveText").focus();return}
 const button=document.getElementById("smartDriverResolveSubmit");button.disabled=true;
 try{await smartSaveDriverStatus(id,"RESOLVED",resolution);bootstrap.Modal.getInstance(document.getElementById("smartDriverResolveModal"))?.hide()}
 catch(_){}finally{button.disabled=false}
};
let smartIncidentMarker=null;
window.smartFocusDriverReport=async function(lat,lon,id){
 if(!Number.isFinite(lat)||!Number.isFinite(lon))return;
 go("mapa");
 initMap();
 await new Promise(resolve=>setTimeout(resolve,200));
 if(smartIncidentMarker)map.removeLayer(smartIncidentMarker);
 smartIncidentMarker=L.marker([lat,lon],{zIndexOffset:1500}).addTo(map);
 smartIncidentMarker.bindPopup(`<b>Incidente de piloto #${Number(id)}</b><br>Ubicación reportada`).openPopup();
 map.invalidateSize();map.flyTo([lat,lon],16);
};

setInterval(()=>{if(session?.token&&location.hash==="#/reportes")smartLoadDriverReports()},15000);

function smartUpdateIncidentBadge(items){
 const badge=document.getElementById("smartDriverIncidentBadge");if(!badge)return;
 if(!Array.isArray(items)){badge.hidden=true;return}
 const pending=items.filter(x=>x.status==="PENDING").length;
 badge.textContent=String(pending);badge.hidden=pending===0;
 badge.title=`${pending} incidentes de piloto pendientes`;
}


function smartTipoSancionES(tipo) {
    const nombres = {
        SPEEDING: "Exceso de velocidad",
        RECKLESS_DRIVING: "Conducción imprudente",
        MISSED_STOP: "Parada omitida",
        BAD_BEHAVIOR: "Mala conducta",
        OTHER: "Otro"
    };
    return nombres[tipo] || String(tipo || "—").replaceAll("_", " ");
}

// SmartRoute GT - evaluaciones y felicitaciones de pasajeros
let smartFeedbackFilter="ALL";
async function smartLoadFeedback(){
 const tbody=document.getElementById("smartFeedbackTable"), summary=document.getElementById("smartFeedbackSummary");
 if(!tbody)return;
 try{
  const data=await json("/passenger-feedback");
  const ratings=data.filter(x=>x.kind==="RATING"&&Number.isFinite(x.stars));
  const compliments=data.filter(x=>x.kind==="COMPLIMENT");
  const avg=ratings.length?(ratings.reduce((n,x)=>n+x.stars,0)/ratings.length).toFixed(1):"—";
  summary.textContent=`${ratings.length} calificaciones · ${compliments.length} felicitaciones · Promedio: ${avg} / 5`;
  const filtered=data.filter(x=>smartFeedbackFilter==="ALL"||x.kind===smartFeedbackFilter).sort((a,b)=>String(b.createdAt).localeCompare(String(a.createdAt)));
  tbody.innerHTML=filtered.map(x=>`<tr><td>${date(x.createdAt)}</td><td>${x.kind==="COMPLIMENT"?'<span class="badge bg-success">Felicitación</span>':'<span class="badge bg-warning text-dark">Calificación</span>'}</td><td>${esc(x.driver||"—")}</td><td>${esc(x.bus||"—")}</td><td>${esc(x.tripId)}</td><td>${esc(x.passenger||"—")}</td><td>${x.kind==="RATING"?esc(String(x.stars)+" / 5 estrellas"):esc(x.reason||"—")}${x.comment?" · "+esc(x.comment):""}</td></tr>`).join("")||'<tr><td colspan="7">Sin registros de esta categoría.</td></tr>';
 }catch(e){tbody.innerHTML=`<tr><td colspan="7">${esc(e.message||"Error al cargar")}</td></tr>`;}
}
