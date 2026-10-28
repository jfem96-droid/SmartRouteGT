/* SmartRoute GT - CRUD administrativo */
(() => {
  let busesCache=[], stopsCache=[], driversCache=[], usersCache=[], assignmentsCache=[], violationsCache=[], alertsCache=[];
  const q=id=>document.getElementById(id);
  const val=id=>q(id)?.value?.trim() ?? "";
  const num=id=>Number(q(id)?.value);
  const active=x=>x?.activa ?? x?.activo ?? true;

  document.body.insertAdjacentHTML("beforeend", `<div class="modal fade" id="crudModal" tabindex="-1"><div class="modal-dialog modal-dialog-centered modal-lg"><div class="modal-content"><form id="crudForm"><div class="modal-header"><div><small class="crud-kicker">SMARTROUTE GT</small><h5 id="crudTitle" class="modal-title"></h5></div><button class="btn-close" type="button" data-bs-dismiss="modal"></button></div><div id="crudBody" class="modal-body"></div><div class="modal-footer"><button type="button" class="btn btn-light" data-bs-dismiss="modal">Cancelar</button><button id="crudSave" class="btn btn-primary" type="submit">Guardar</button></div></form></div></div></div>`);
  const modal=bootstrap.Modal.getOrCreateInstance(q("crudModal"));

  // Diálogos SmartRoute GT: reemplazan confirm() y prompt() del navegador.
  document.body.insertAdjacentHTML("beforeend", `
    <div class="modal fade" id="smartDialog" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content smart-dialog">
          <div class="modal-header">
            <div>
              <small class="crud-kicker">SMARTROUTE GT</small>
              <h5 id="smartDialogTitle" class="modal-title">Confirmar operación</h5>
            </div>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>

          <div class="modal-body">
            <div class="smart-dialog-icon" id="smartDialogIcon">
              <i class="bi bi-question-lg"></i>
            </div>

            <p id="smartDialogMessage" class="smart-dialog-message"></p>

            <div id="smartDialogInputBox" class="d-none mt-3">
              <label id="smartDialogLabel" class="form-label"></label>
              <textarea
                id="smartDialogInput"
                class="form-control"
                rows="3"
                maxlength="500"
                placeholder="Escribe la justificación..."
              ></textarea>
              <small class="crud-help">
                Este dato quedará asociado a la operación administrativa.
              </small>
            </div>
          </div>

          <div class="modal-footer">
            <button
              id="smartDialogCancel"
              type="button"
              class="btn btn-light"
              data-bs-dismiss="modal">
              Cancelar
            </button>

            <button
              id="smartDialogAccept"
              type="button"
              class="btn btn-primary">
              Confirmar
            </button>
          </div>
        </div>
      </div>
    </div>
  `);

  const smartDialogModal =
    bootstrap.Modal.getOrCreateInstance(q("smartDialog"));

  function smartConfirm(message, options = {}) {
    return new Promise(resolve => {
      const title = options.title || "Confirmar operación";
      const acceptText = options.acceptText || "Confirmar";
      const danger = options.danger || false;

      q("smartDialogTitle").textContent = title;
      q("smartDialogMessage").textContent = message;

      q("smartDialogInputBox").classList.add("d-none");
      q("smartDialogInput").value = "";

      q("smartDialogIcon").className =
        "smart-dialog-icon " + (danger ? "danger" : "info");

      q("smartDialogIcon").innerHTML =
        danger
          ? '<i class="bi bi-exclamation-triangle"></i>'
          : '<i class="bi bi-question-lg"></i>';

      q("smartDialogAccept").textContent = acceptText;
      q("smartDialogAccept").className =
        danger ? "btn btn-danger" : "btn btn-primary";

      let answered = false;

      const finish = value => {
        if (answered) return;
        answered = true;
        resolve(value);
      };

      q("smartDialogAccept").onclick = () => {
        finish(true);
        smartDialogModal.hide();
      };

      q("smartDialog").addEventListener(
        "hidden.bs.modal",
        () => finish(false),
        { once: true }
      );

      smartDialogModal.show();
    });
  }

  function smartPrompt(message, options = {}) {
    return new Promise(resolve => {
      const title = options.title || "Información requerida";
      const label = options.label || "Justificación";
      const placeholder =
        options.placeholder || "Escribe la justificación...";
      const acceptText = options.acceptText || "Continuar";
      const danger = options.danger || false;

      q("smartDialogTitle").textContent = title;
      q("smartDialogMessage").textContent = message;
      q("smartDialogLabel").textContent = label;

      q("smartDialogInputBox").classList.remove("d-none");
      q("smartDialogInput").value = "";
      q("smartDialogInput").placeholder = placeholder;

      q("smartDialogIcon").className =
        "smart-dialog-icon " + (danger ? "danger" : "info");

      q("smartDialogIcon").innerHTML =
        danger
          ? '<i class="bi bi-exclamation-triangle"></i>'
          : '<i class="bi bi-pencil-square"></i>';

      q("smartDialogAccept").textContent = acceptText;
      q("smartDialogAccept").className =
        danger ? "btn btn-danger" : "btn btn-primary";

      let answered = false;

      const finish = value => {
        if (answered) return;
        answered = true;
        resolve(value);
      };

      q("smartDialogAccept").onclick = () => {
        const value = q("smartDialogInput").value.trim();

        if (!value) {
          q("smartDialogInput").classList.add("is-invalid");
          q("smartDialogInput").focus();
          return;
        }

        q("smartDialogInput").classList.remove("is-invalid");

        finish(value);
        smartDialogModal.hide();
      };

      q("smartDialog").addEventListener(
        "hidden.bs.modal",
        () => finish(null),
        { once: true }
      );

      smartDialogModal.show();

      setTimeout(() => q("smartDialogInput").focus(), 250);
    });
  }

 window.smartPrompt = smartPrompt;
 function notify(t,type="success"){q("message").innerHTML=`<div class="alert alert-${type}">${esc(t)}</div>`;setTimeout(()=>{if(q("message"))q("message").innerHTML=""},4500)}
  async function act(path,opt={},ok="Operación completada"){try{await json(path,opt);notify(ok);return true}catch(e){notify(e.message||String(e),"danger");return false}}
  function openForm(title,body,submit,save="Guardar"){q("crudTitle").textContent=title;q("crudBody").innerHTML=body;q("crudSave").type="submit";q("crudSave").textContent=save;q("crudForm").onsubmit=async e=>{e.preventDefault();q("crudSave").disabled=true;try{if(await submit())modal.hide()}finally{q("crudSave").disabled=false}};modal.show()}
  const field=(label,id,value="",type="text",extra="")=>`<div class="col-md-6"><label class="form-label">${label}</label><input id="${id}" class="form-control" type="${type}" value="${esc(value??"")}" ${extra}></div>`;
  const sel=(label,id,items,value="")=>`<div class="col-md-6"><label class="form-label">${label}</label><select id="${id}" class="form-select">${items.map(x=>`<option value="${esc(x.value)}" ${String(x.value)===String(value)?"selected":""}>${esc(x.label)}</option>`).join("")}</select></div>`;
  const area=(label,id,value="")=>`<div class="col-12"><label class="form-label">${label}</label><textarea id="${id}" class="form-control" rows="3">${esc(value??"")}</textarea></div>`;
  function top(view,label,fn,icon="plus-lg"){let h=q(view)?.querySelector(".heading");if(!h||h.querySelector(".crud-top"))return;let b=document.createElement("button");b.className="btn btn-primary crud-top";b.innerHTML=`<i class="bi bi-${icon}"></i> ${label}`;b.onclick=fn;h.appendChild(b)}

  async function smartPasswordDelete(type,id,label,loader){
    document.getElementById("permanentDeleteModal")?.remove();

    const names={
      bus:"bus",
      stop:"parada",
      driver:"piloto",
      route:"ruta",
      user:"usuario"
    };

    const entityName=names[type]||"registro";

    let check;

    try{
      check=await json(`/admin/permanent-delete/${type}/${id}/check`);
    }catch(e){
      notify(e.message||"No se pudieron consultar las dependencias.","danger");
      return;
    }

    const deps=Array.isArray(check?.dependencies)
      ? check.dependencies
      : [];

    /*
     * ======================================================
     * ELIMINACIÓN BLOQUEADA
     * ======================================================
     */
    if(!check?.canDelete){

      const dependencies=deps.map(dep=>{
        const history=dep.type==="HISTORIAL";
        const protectedItem=dep.type==="PROTEGIDO";

        const icon=history
          ?"bi-clock-history"
          :protectedItem
            ?"bi-shield-lock"
            :"bi-link-45deg";

        const typeText=history
          ?"HISTORIAL"
          :protectedItem
            ?"PROTEGIDO"
            :"DESASOCIAR";

        return `
          <div class="smart-delete-dependency ${history?"history":""} ${protectedItem?"protected":""}">

            <div class="smart-delete-dependency-icon">
              <i class="bi ${icon}"></i>
            </div>

            <div class="smart-delete-dependency-body">

              <div class="smart-delete-dependency-head">
                <strong>${esc(dep.label||"Dependencia")}</strong>
                <span class="smart-delete-count">${Number(dep.count)||0}</span>
              </div>

              <span class="smart-delete-type">
                ${typeText}
              </span>

              <p>
                ${esc(dep.action||"Debes resolver esta dependencia antes de eliminar.")}
              </p>

              ${
                history
                ?`
                  <div class="smart-delete-retention">
                    <i class="bi bi-calendar3"></i>

                    <span>
                      SmartRoute conserva este historial durante
                      <b>${Number(check.retentionDays)||7} días</b>.
                      Cuando los registros hayan vencido y hayan sido
                      purgados, podrás intentar eliminar nuevamente.
                    </span>
                  </div>
                `
                :""
              }

            </div>
          </div>
        `;
      }).join("");

      document.body.insertAdjacentHTML("beforeend",`
        <div class="modal fade" id="permanentDeleteModal" tabindex="-1">

          <div class="modal-dialog modal-dialog-centered modal-lg">

            <div class="modal-content smart-dialog danger-zone-modal">

              <div class="modal-header">

                <div>
                  <small class="crud-kicker">
                    ZONA DE SEGURIDAD
                  </small>

                  <h5 class="modal-title">
                    No se puede eliminar
                  </h5>
                </div>

                <button
                  type="button"
                  class="btn-close"
                  data-bs-dismiss="modal">
                </button>

              </div>

              <div class="modal-body">

                <div class="smart-delete-blocked-head">

                  <div class="smart-dialog-icon danger">
                    <i class="bi bi-exclamation-triangle"></i>
                  </div>

                  <div>
                    <h5>${esc(label)}</h5>

                    <p>
                      Este ${entityName} tiene registros relacionados.
                      Revisa lo que debes resolver antes de eliminarlo
                      permanentemente.
                    </p>
                  </div>

                </div>

                <div class="smart-delete-dependencies">
                  ${dependencies}
                </div>

                <div class="smart-delete-info">
                  <i class="bi bi-info-circle"></i>

                  <span>
                    Puedes mantener este ${entityName}
                    <b>desactivado</b> mientras resuelves las dependencias.
                  </span>
                </div>

              </div>

              <div class="modal-footer">

                <button
                  class="btn btn-primary"
                  data-bs-dismiss="modal">
                  Entendido
                </button>

              </div>

            </div>

          </div>

        </div>
      `);

      const m=bootstrap.Modal.getOrCreateInstance(
        q("permanentDeleteModal")
      );

      m.show();

      return;
    }

    /*
     * ======================================================
     * PUEDE ELIMINARSE
     * ======================================================
     */

    document.body.insertAdjacentHTML("beforeend",`
      <div class="modal fade" id="permanentDeleteModal" tabindex="-1">

        <div class="modal-dialog modal-dialog-centered">

          <div class="modal-content smart-dialog danger-zone-modal">

            <div class="modal-header">

              <div>
                <small class="crud-kicker">
                  ZONA DE SEGURIDAD
                </small>

                <h5 class="modal-title">
                  Eliminar permanentemente
                </h5>
              </div>

              <button
                type="button"
                class="btn-close"
                data-bs-dismiss="modal">
              </button>

            </div>

            <div class="modal-body">

              <div class="smart-dialog-icon danger">
                <i class="bi bi-trash3"></i>
              </div>

              <p class="smart-dialog-message">
                Vas a eliminar
                <b>${esc(label)}</b>.
                Esta acción no se puede deshacer.
              </p>

              <div class="smart-delete-ready">
                <i class="bi bi-check-circle-fill"></i>
                <span>
                  No existen dependencias que impidan la eliminación.
                </span>
              </div>

              <label class="form-label mt-3">
                Contraseña del administrador
              </label>

              <input
                id="permanentDeletePassword"
                class="form-control"
                type="password"
                autocomplete="current-password"
                placeholder="Ingresa tu contraseña"
              >

              <div
                id="permanentDeleteError"
                class="text-danger small mt-2">
              </div>

            </div>

            <div class="modal-footer">

              <button
                class="btn btn-light"
                data-bs-dismiss="modal">
                Cancelar
              </button>

              <button
                id="permanentDeleteConfirm"
                class="btn btn-danger">

                <i class="bi bi-trash3"></i>
                Eliminar definitivamente

              </button>

            </div>

          </div>

        </div>

      </div>
    `);

    const m=bootstrap.Modal.getOrCreateInstance(
      q("permanentDeleteModal")
    );

    q("permanentDeleteConfirm").onclick=async()=>{

      const password=q("permanentDeletePassword").value;
      const error=q("permanentDeleteError");

      error.textContent="";

      if(!password){
        error.textContent="La contraseña es obligatoria.";
        return;
      }

      const btn=q("permanentDeleteConfirm");
      btn.disabled=true;

      try{

        await json(
          `/admin/permanent-delete/${type}/${id}`,
          {
            method:"DELETE",
            body:JSON.stringify({password})
          }
        );

        m.hide();

        notify(
          `${entityName.charAt(0).toUpperCase()+entityName.slice(1)} eliminado permanentemente`
        );

        await loader();

      }catch(e){

        error.textContent=
          e.message||
          "No se pudo eliminar.";

      }finally{

        btn.disabled=false;
      }
    };

    m.show();

    setTimeout(
      ()=>q("permanentDeletePassword")?.focus(),
      250
    );
  }

  window.smartPermanentDelete=smartPasswordDelete;

  window.loadBuses=async()=>{try{busesCache=await json("/buses");q("busesTable").innerHTML=busesCache.map(x=>`<tr><td>${x.id}</td><td><b>${esc(x.codigo)}</b></td><td>${esc(x.placa)}</td><td>${x.capacidad??"—"}</td><td>${pill(active(x)?"Activo":"Inactivo",active(x)?"success":"neutral")}</td><td class="actions"><button class="table-btn edit" onclick="crudBus(${x.id})">Editar</button><button class="table-btn ${active(x)?"off":"on"}" onclick="crudBusToggle(${x.id},${!active(x)})">${active(x)?"Desactivar":"Activar"}</button><button class="table-btn delete-permanent" onclick="smartPermanentDelete(\'bus\',${x.id},\'${esc(x.codigo)}\',loadBuses)">Eliminar</button></td></tr>`).join("")||empty(6,"No hay buses.")}catch(e){q("busesTable").innerHTML=empty(6,e.message)}};
  window.crudBus=id=>{let x=id?busesCache.find(v=>v.id===id):null;openForm(x?"Editar bus":"Nuevo bus",`<div class="row g-3">${field("Código","bc",x?.codigo,"text","required")}${field("Placa","bp",x?.placa,"text","required")}${field("Capacidad","bcap",x?.capacidad,"number",'min="1" required')}</div>`,async()=>{let ok=await act(x?`/buses/${id}`:"/buses",{method:x?"PUT":"POST",body:JSON.stringify({codigo:val("bc"),placa:val("bp"),capacidad:num("bcap"),activa:x?active(x):true})},"Bus guardado");if(ok)loadBuses();return ok})};
  window.crudBusToggle=async(id,on)=>{if(!await smartConfirm(
  on ? "¿Deseas activar este bus?" : "¿Deseas desactivar este bus?",
  {
    title: on ? "Activar bus" : "Desactivar bus",
    acceptText: on ? "Activar" : "Desactivar",
    danger: !on
  }
)) return;let ok=await act(on?`/buses/${id}/activar`:`/buses/${id}`,{method:on?"PUT":"DELETE"},on?"Bus activado":"Bus desactivado");if(ok)loadBuses()};

  window.loadStops=async()=>{try{stopsCache=await json("/stops");q("stopsTable").innerHTML=stopsCache.map(x=>`<tr><td><b>${esc(x.nombre)}</b></td><td>${x.latitud}</td><td>${x.longitud}</td><td>${x.radioPermitido} m</td><td>${pill(active(x)?"Activa":"Inactiva",active(x)?"success":"neutral")}</td><td class="actions"><button class="table-btn edit" onclick="crudStop(${x.id})">Editar</button><button class="table-btn ${active(x)?"off":"on"}" onclick="crudStopToggle(${x.id},${!active(x)})">${active(x)?"Desactivar":"Activar"}</button><button class="table-btn delete-permanent" onclick="smartPermanentDelete(\'stop\',${x.id},\'${esc(x.nombre)}\',loadStops)">Eliminar</button></td></tr>`).join("")||empty(6,"No hay paradas.")}catch(e){q("stopsTable").innerHTML=empty(6,e.message)}};
  window.crudStop=id=>{
    let x=id?stopsCache.find(v=>v.id===id):null;

    const initialLat=Number(x?.latitud??14.6349);
    const initialLon=Number(x?.longitud??-90.5069);

    openForm(
      x?"Editar parada":"Nueva parada",
      `
      <div class="smart-stop-editor">

        <div class="row g-3">
          ${field("Nombre","sn",x?.nombre,"text","required")}

          <div class="col-12">
            <label class="form-label">Buscar ubicación</label>

            <div class="smart-location-search">
              <div class="smart-location-searchbox">
                <i class="bi bi-search"></i>

                <input
                  id="stopLocationSearch"
                  class="form-control"
                  type="search"
                  placeholder="Ej. Plaza Obelisco, Roosevelt, Zona 1..."
                  autocomplete="off"
                >

                <button
                  id="stopLocationSearchBtn"
                  type="button"
                  class="btn btn-primary"
                >
                  Buscar
                </button>
              </div>

              <div
                id="stopSearchResults"
                class="smart-location-results"
              ></div>
            </div>

            <small class="crud-help">
              Busca un lugar o haz clic directamente en el mapa.
              También puedes arrastrar el marcador.
            </small>
          </div>

          <div class="col-12">
            <div class="smart-stop-map-shell">
              <div class="smart-stop-map-head">
                <div>
                  <small>UBICACIÓN DE LA PARADA</small>
                  <strong>Selecciona el punto exacto</strong>
                </div>

                <span id="stopMapCoordinates">
                  ${initialLat.toFixed(6)}, ${initialLon.toFixed(6)}
                </span>
              </div>

              <div id="stopEditorMap"></div>
            </div>
          </div>

          ${field(
            "Latitud",
            "slat",
            initialLat,
            "number",
            'step="any" min="-90" max="90" required'
          )}

          ${field(
            "Longitud",
            "slon",
            initialLon,
            "number",
            'step="any" min="-180" max="180" required'
          )}

          ${field(
            "Radio permitido (m)",
            "sr",
            x?.radioPermitido??40,
            "number",
            'min="1" required'
          )}
        </div>
      </div>
      `,
      async()=>{
        let ok=await act(
          x?`/stops/${id}`:"/stops",
          {
            method:x?"PUT":"POST",
            body:JSON.stringify({
              nombre:val("sn"),
              latitud:num("slat"),
              longitud:num("slon"),
              radioPermitido:num("sr"),
              activa:x?active(x):true
            })
          },
          "Parada guardada"
        );

        if(ok)loadStops();
        return ok;
      }
    );

    const modalElement=q("crudModal");

    const initializeStopMap=()=>{
      modalElement.removeEventListener(
        "shown.bs.modal",
        initializeStopMap
      );

      const mapElement=q("stopEditorMap");
      if(!mapElement)return;

      const editorMap=L.map(mapElement,{
        zoomControl:true
      }).setView(
        [initialLat,initialLon],
        x?16:13
      );

      L.tileLayer(
        "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
        {
          maxZoom:19,
          attribution:"&copy; OpenStreetMap contributors"
        }
      ).addTo(editorMap);

      let marker=L.marker(
        [initialLat,initialLon],
        {
          draggable:true
        }
      ).addTo(editorMap);

      const updatePosition=(lat,lon,moveMarker=true)=>{
        lat=Number(lat);
        lon=Number(lon);

        if(!Number.isFinite(lat)||!Number.isFinite(lon))return;

        q("slat").value=lat.toFixed(7);
        q("slon").value=lon.toFixed(7);

        const coords=q("stopMapCoordinates");

        if(coords){
          coords.textContent=
            `${lat.toFixed(6)}, ${lon.toFixed(6)}`;
        }

        if(moveMarker){
          marker.setLatLng([lat,lon]);
        }
      };

      marker.on("dragend",()=>{
        const pos=marker.getLatLng();
        updatePosition(pos.lat,pos.lng,false);
      });

      editorMap.on("click",e=>{
        updatePosition(e.latlng.lat,e.latlng.lng);
      });

      q("slat")?.addEventListener("change",()=>{
        const lat=Number(q("slat").value);
        const lon=Number(q("slon").value);

        if(Number.isFinite(lat)&&Number.isFinite(lon)){
          marker.setLatLng([lat,lon]);
          editorMap.panTo([lat,lon]);
          updatePosition(lat,lon,false);
        }
      });

      q("slon")?.addEventListener("change",()=>{
        const lat=Number(q("slat").value);
        const lon=Number(q("slon").value);

        if(Number.isFinite(lat)&&Number.isFinite(lon)){
          marker.setLatLng([lat,lon]);
          editorMap.panTo([lat,lon]);
          updatePosition(lat,lon,false);
        }
      });

      const searchLocation=async()=>{
        const input=q("stopLocationSearch");
        const results=q("stopSearchResults");
        const term=input?.value.trim();

        if(!term)return;

        results.innerHTML=`
          <div class="smart-location-loading">
            <span class="spinner-border spinner-border-sm"></span>
            Buscando ubicación...
          </div>
        `;

        try{
          const query=encodeURIComponent(term);

          const response=await fetch(
            `https://nominatim.openstreetmap.org/search?format=jsonv2&limit=5&countrycodes=gt&q=${query}`,
            {
              headers:{
                "Accept":"application/json"
              }
            }
          );

          if(!response.ok){
            throw new Error(
              `Error de búsqueda ${response.status}`
            );
          }

          const data=await response.json();

          if(!data.length){
            results.innerHTML=`
              <div class="smart-location-empty">
                No encontramos resultados para esa búsqueda.
              </div>
            `;
            return;
          }

          results.innerHTML=data.map((place,index)=>`
            <button
              type="button"
              class="smart-location-result"
              data-location-result="${index}"
            >
              <i class="bi bi-geo-alt-fill"></i>

              <span>
                <strong>
                  ${esc(
                    place.name ||
                    place.display_name.split(",")[0]
                  )}
                </strong>

                <small>
                  ${esc(place.display_name)}
                </small>
              </span>
            </button>
          `).join("");

          results
            .querySelectorAll("[data-location-result]")
            .forEach(button=>{
              button.onclick=()=>{
                const place=
                  data[Number(button.dataset.locationResult)];

                const lat=Number(place.lat);
                const lon=Number(place.lon);

                updatePosition(lat,lon);

                editorMap.flyTo(
                  [lat,lon],
                  17,
                  {
                    animate:true,
                    duration:.8
                  }
                );

                results.innerHTML="";

                if(!q("sn").value.trim()){
                  q("sn").value=
                    place.name ||
                    place.display_name.split(",")[0];
                }
              };
            });

        }catch(e){
          console.error(e);

          results.innerHTML=`
            <div class="smart-location-empty">
              No fue posible consultar el buscador.
              Puedes seleccionar la ubicación directamente en el mapa.
            </div>
          `;
        }
      };

      q("stopLocationSearchBtn").onclick=searchLocation;

      q("stopLocationSearch").onkeydown=e=>{
        if(e.key==="Enter"){
          e.preventDefault();
          searchLocation();
        }
      };

      setTimeout(()=>{
        editorMap.invalidateSize();
      },150);

      modalElement.addEventListener(
        "hidden.bs.modal",
        ()=>{
          try{
            editorMap.remove();
          }catch{}
        },
        {once:true}
      );
    };

    modalElement.addEventListener(
      "shown.bs.modal",
      initializeStopMap,
      {once:true}
    );
  };

  window.crudStopToggle=async(id,on)=>{if(!await smartConfirm(
  on ? "¿Deseas activar esta parada?" : "¿Deseas desactivar esta parada?",
  {
    title: on ? "Activar parada" : "Desactivar parada",
    acceptText: on ? "Activar" : "Desactivar",
    danger: !on
  }
)) return;let ok=await act(on?`/stops/${id}/activar`:`/stops/${id}`,{method:on?"PUT":"DELETE"},on?"Parada activada":"Parada desactivada");if(ok)loadStops()};

  window.loadDrivers=async()=>{try{[driversCache,assignmentsCache]=await Promise.all([json("/drivers"),json("/assignments")]);q("driversTable").innerHTML=driversCache.map(x=>{let a=assignmentsCache.find(y=>y.driver?.id===x.id&&active(y));return `<tr><td>${x.id}</td><td><b>${esc(x.nombre)}</b><small class="crud-sub">${a?`${esc(a.bus?.codigo||"")} · ${esc(a.route?.nombre||"")} · ${esc(({SOLO_IDA:"Solo ida",SOLO_VUELTA:"Solo vuelta",IDA_Y_VUELTA:"Ida y vuelta"})[a.tipoRecorrido]||"Solo ida")}`:"Sin asignación activa"}</small></td><td>${esc(x.licencia)}</td><td>${esc(x.telefono||"—")}</td><td>${x.sancionesConfirmadas??0}</td><td>${pill(x.activo?"Activo":"Suspendido",x.activo?"success":"danger")}</td><td class="actions"><button class="table-btn edit" onclick="crudDriver(${x.id})">Editar</button><button class="table-btn info" onclick="crudAssign(${x.id})">${a?"Cambiar":"Asignar"}</button>${a?`<button class="table-btn off" onclick="crudUnassign(${a.id},${x.id})">Desasignar</button>`:""}<button class="table-btn ${x.activo?"off":"on"}" onclick="crudDriverToggle(${x.id},${!x.activo})">${x.activo?"Desactivar":"Reactivar"}</button><button class="table-btn delete-permanent" onclick="smartPermanentDelete(\'driver\',${x.id},\'${esc(x.nombre)}\',loadDrivers)">Eliminar</button></td></tr>`}).join("")||empty(7,"No hay pilotos.")}catch(e){q("driversTable").innerHTML=empty(7,e.message)}};
  window.crudDriver=id=>{let x=id?driversCache.find(v=>v.id===id):null;openForm(x?"Editar piloto":"Nuevo piloto",`<div class="row g-3">${field("Nombre","dn",x?.nombre,"text","required")}${field("Licencia","dl",x?.licencia,"text","required")}${field("Teléfono","dt",x?.telefono||"","tel")}</div>`,async()=>{let ok=await act(x?`/drivers/${id}`:"/drivers",{method:x?"PUT":"POST",body:JSON.stringify({nombre:val("dn"),licencia:val("dl"),telefono:val("dt"),activo:x?x.activo:true})},"Piloto guardado");if(ok)loadDrivers();return ok})};
  window.crudDriverToggle=async(id,on)=>{let body;

if (on) {
  const j = await smartPrompt(
    "Para reactivar al piloto debes indicar el motivo.",
    {
      title: "Reactivar piloto",
      label: "Justificación obligatoria",
      placeholder: "Ej. Sanciones revisadas por administración...",
      acceptText: "Reactivar piloto"
    }
  );

  if (!j) return;

  body = JSON.stringify({
    justificacion: j
  });

} else {
  const confirmed = await smartConfirm(
    "El piloto quedará inactivo y no podrá iniciar nuevos viajes.",
    {
      title: "Desactivar piloto",
      acceptText: "Desactivar",
      danger: true
    }
  );

  if (!confirmed) return;
}let ok=await act(on?`/drivers/${id}/activar`:`/drivers/${id}`,{method:on?"PUT":"DELETE",body},on?"Piloto reactivado":"Piloto desactivado");if(ok)loadDrivers()};
  window.crudUnassign=async(assignmentId,driverId)=>{
    const driver=driversCache.find(x=>x.id===driverId);
    const as=assignmentsCache.find(x=>x.id===assignmentId);

    const driverName=driver?.nombre||"este piloto";
    const busName=as?.bus?.codigo||"el bus";
    const routeName=as?.route?.nombre||"la ruta";

    const confirmed=await smartConfirm(
      `Se finalizará la asignación de ${driverName} con ${busName} · ${routeName}. El historial de la asignación se conservará.`,
      {
        title:"Desasignar piloto",
        acceptText:"Desasignar",
        danger:true
      }
    );

    if(!confirmed) return;

    const ok=await act(
      `/assignments/${assignmentId}/deactivate`,
      {
        method:"PUT"
      },
      "Piloto desasignado"
    );

    if(ok){
      await loadDrivers();
    }
  };

  window.crudAssign=async driverId=>{
    try{
      let[b,r,assignments]=await Promise.all([
        json("/buses"),
        json("/routes"),
        json("/assignments")
      ]);

      const current=assignments.find(a=>
        a.driver?.id===driverId && active(a)
      );

      const occupiedBusIds=new Set(
        assignments
          .filter(a=>active(a) && a.driver?.id!==driverId)
          .map(a=>a.bus?.id)
          .filter(Boolean)
      );

      const availableBuses=b.filter(bus=>
        active(bus) && !occupiedBusIds.has(bus.id)
      );

      if(!availableBuses.length){
        notify("No hay buses disponibles para asignar.","warning");
        return;
      }

      openForm(
        current ? "Cambiar bus y ruta" : "Asignar bus y ruta",
        `<div class="row g-3">${
          sel("Bus","ab",availableBuses.map(x=>({
            value:x.id,
            label:`${x.codigo} · ${x.placa}`
          })))
        }${
          sel("Ruta","ar",r.filter(active).map(x=>({
            value:x.id,
            label:`${x.codigo} · ${x.nombre}`
          })))
        }${sel("Recorrido autorizado","atipo",[
          {value:"SOLO_IDA",label:"Solo ida"},
          {value:"SOLO_VUELTA",label:"Solo vuelta"},
          {value:"IDA_Y_VUELTA",label:"Ida y vuelta"}
        ])}</div>`,
        async()=>{
          let ok=await act(
            "/assignments",
            {
              method:"POST",
              body:JSON.stringify({
                driverId,
                busId:num("ab"),
                routeId:num("ar"),
                tipoRecorrido:val("atipo")
              })
            },
            current ? "Asignación actualizada" : "Asignación creada"
          );

          if(ok) await loadDrivers();
          return ok;
        }
      );

      if(current){
        const busSelect=q("ab");
        const routeSelect=q("ar");

        if(busSelect && current.bus?.id)
          busSelect.value=String(current.bus.id);

        if(q("atipo") && current.tipoRecorrido) q("atipo").value=current.tipoRecorrido;
        if(routeSelect && current.route?.id)
          routeSelect.value=String(current.route.id);
      }

    }catch(e){
      notify(e.message,"danger");
    }
  };

  const originalLoadRoutes=window.loadRoutes;
  if(originalLoadRoutes) window.loadRoutes=async()=>{await originalLoadRoutes();document.querySelectorAll("#routesTable tr").forEach((tr,i)=>{let x=routes[i],td=tr.lastElementChild;if(x&&td&&!td.querySelector(".route-stops-btn"))td.insertAdjacentHTML("beforeend",`<button class="table-btn info route-stops-btn" onclick="crudRouteStops(${x.id})">Paradas</button><button class="table-btn delete-permanent" onclick="smartPermanentDelete(\'route\',${x.id},\'${esc(x.nombre)}\',loadRoutes)">Eliminar</button>`)})};
  window.crudRouteStops=async routeId=>{try{let[rels,stops]=await Promise.all([json(`/route-stops/route?routeId=${routeId}`),json("/stops")]);let rows=rels.map(x=>`<tr><td>${x.orden}</td><td>${esc(x.stop?.nombre||"—")}</td><td>${esc(x.tipoParada||"NORMAL")}</td><td>${x.tiempoMinimoSegundos??15}s / ${x.tiempoObjetivoSegundos??20}s / ${x.tiempoMaximoSegundos??30}s</td><td><button type="button" class="table-btn edit" onclick="crudStopConfig(${x.id},'${x.tipoParada||"NORMAL"}',${x.tiempoMinimoSegundos??15},${x.tiempoObjetivoSegundos??20},${x.tiempoMaximoSegundos??30},${routeId})">Configurar</button><button type="button" class="table-btn delete-permanent" onclick="crudRouteStopRemove(${x.id},${routeId})">Quitar</button></td></tr>`).join("");openForm("Paradas de la ruta",`<div class="crud-box"><div class="row g-2">${sel("Agregar parada","rs",stops.filter(active).map(x=>({value:x.id,label:x.nombre})))}${field("Orden","ro",rels.length+1,"number",'min="1"')}</div><button type="button" id="returnStopEdit" class="btn btn-outline-info mt-3 ms-2">Configurar vuelta</button><button type="button" id="routeStopAdd" class="btn btn-outline-primary mt-3">Agregar a ruta</button></div><div class="table-responsive mt-4"><table><thead><tr><th>Orden</th><th>Parada</th><th>Tipo</th><th>Tiempos</th><th></th></tr></thead><tbody>${rows||empty(5,"Sin paradas configuradas.")}</tbody></table></div>`,async()=>true,"Cerrar");q("crudSave").type="button";q("crudSave").onclick=()=>modal.hide();q("returnStopEdit").onclick=()=>crudReturnStops(routeId);q("routeStopAdd").onclick=async()=>{let ok=await act(`/route-stops?routeId=${routeId}&stopId=${num("rs")}&orden=${num("ro")}`,{method:"POST"},"Parada agregada");if(ok){await crudRouteStops(routeId)}}}catch(e){notify(e.message,"danger")}};
  // El administrador configura la vuelta explícitamente. No se invierte automáticamente la ida.
  window.crudReturnStops=async routeId=>{
    try{
      const [itineraries,allStops]=await Promise.all([
        json(`/routes/${routeId}/itineraries`),json("/stops")
      ]);
      const vuelta=itineraries.find(x=>x.direction==="VUELTA");
      let ids=[...(vuelta?.stopIds||[])];
      const paint=()=>{
        const container=q("returnStopRows");
        if(!container)return;
        container.innerHTML=ids.map((id,i)=>`<div class="d-flex align-items-center justify-content-between gap-2 mb-2"><span>${i+1}. ${esc(allStops.find(s=>s.id===id)?.nombre||String(id))}</span><div><button class="btn btn-sm btn-outline-light" type="button" data-move="${i}" data-delta="-1" aria-label="Subir parada">↑</button> <button class="btn btn-sm btn-outline-light" type="button" data-move="${i}" data-delta="1" aria-label="Bajar parada">↓</button> <button class="btn btn-sm btn-outline-danger" type="button" data-remove="${i}">Quitar</button></div></div>`).join("")||"Configura al menos dos paradas de vuelta.";
        container.querySelectorAll("[data-remove]").forEach(button=>button.onclick=()=>{ids.splice(Number(button.dataset.remove),1);paint()});
        container.querySelectorAll("[data-move]").forEach(button=>button.onclick=()=>{
          const i=Number(button.dataset.move),j=i+Number(button.dataset.delta);
          if(j<0||j>=ids.length)return;
          [ids[i],ids[j]]=[ids[j],ids[i]];paint();
        });
      };
      openForm("Recorrido de vuelta · paradas ordenadas",
        `<p class="crud-help">Defina el trayecto de vuelta real. No necesariamente es la ida invertida. No se permiten cambios si hay viajes activos.</p><div id="returnStopRows" class="crud-box mb-3"></div><div class="row g-2">${sel("Agregar parada","returnStopSelect",allStops.filter(active).map(s=>({value:s.id,label:s.nombre})))}</div><button id="returnStopAdd" type="button" class="btn btn-outline-info mt-2">Agregar parada</button>`,
        async()=>{
          if(ids.length<2){notify("Se requieren al menos dos paradas de vuelta", "warning");return false}
          return !!(await act(`/routes/${routeId}/return-stops`, {method:"PUT",body:JSON.stringify({stopIds:ids})}, "Vuelta configurada"));
        });
      paint();
      q("returnStopAdd").onclick=()=>{
        const id=num("returnStopSelect");
        if(!id||ids.includes(id)){notify("Selecciona una parada que no esté repetida", "warning");return}
        ids.push(id);paint();
      };
    }catch(e){notify(e.message,"danger")}
  };
  window.crudRouteStopRemove=async(id,routeId)=>{
    if(!await smartConfirm("La parada se quitará de esta ruta, pero seguirá existiendo en el catálogo general.",{title:"Quitar parada de la ruta",acceptText:"Quitar",danger:true}))return;
    let ok=await act(`/route-stops/${id}`,{method:"DELETE"},"Parada quitada de la ruta");
    if(ok){await crudRouteStops(routeId)}
  };
  window.crudStopConfig=(id,t,m,o,x,routeId)=>{
    document.getElementById("routeStopConfigModal")?.remove();

    document.body.insertAdjacentHTML("beforeend",`
      <div
        class="modal fade"
        id="routeStopConfigModal"
        tabindex="-1"
        data-bs-backdrop="static">

        <div class="modal-dialog modal-dialog-centered">

          <div class="modal-content smart-dialog">

            <div class="modal-header">

              <div>
                <small class="crud-kicker">
                  CONFIGURACIÓN DE PARADA
                </small>

                <h5 class="modal-title">
                  Configurar parada
                </h5>
              </div>

              <button
                type="button"
                class="btn-close"
                data-bs-dismiss="modal">
              </button>

            </div>

            <div class="modal-body">

              <div class="row g-3">

                <div class="col-md-6">
                  <label class="form-label">Tipo</label>

                  <select
                    id="routeStopType"
                    class="form-select">

                    <option value="NORMAL"
                      ${t==="NORMAL"?"selected":""}>
                      Normal
                    </option>

                    <option value="ALTA_DEMANDA"
                      ${t==="ALTA_DEMANDA"?"selected":""}>
                      Alta demanda
                    </option>

                    <option value="REGULADORA"
                      ${t==="REGULADORA"?"selected":""}>
                      Reguladora
                    </option>

                  </select>
                </div>

                <div class="col-md-6">
                  <label class="form-label">
                    Mínimo (seg)
                  </label>

                  <input
                    id="routeStopMin"
                    class="form-control"
                    type="number"
                    min="1"
                    max="300"
                    value="${Number(m)||15}">
                </div>

                <div class="col-md-6">
                  <label class="form-label">
                    Objetivo (seg)
                  </label>

                  <input
                    id="routeStopObj"
                    class="form-control"
                    type="number"
                    min="1"
                    max="300"
                    value="${Number(o)||20}">
                </div>

                <div class="col-md-6">
                  <label class="form-label">
                    Máximo (seg)
                  </label>

                  <input
                    id="routeStopMax"
                    class="form-control"
                    type="number"
                    min="1"
                    max="300"
                    value="${Number(x)||30}">
                </div>

              </div>

              <div
                id="routeStopConfigError"
                class="text-danger small mt-2">
              </div>

            </div>

            <div class="modal-footer">

              <button
                type="button"
                class="btn btn-light"
                data-bs-dismiss="modal">
                Cancelar
              </button>

              <button
                type="button"
                id="routeStopConfigSave"
                class="btn btn-primary">

                <i class="bi bi-check-lg"></i>
                Guardar cambios

              </button>

            </div>

          </div>

        </div>

      </div>
    `);

    const el=q("routeStopConfigModal");
    const configModal=bootstrap.Modal.getOrCreateInstance(el);

    q("routeStopConfigSave").onclick=async()=>{

      const min=Number(q("routeStopMin").value);
      const obj=Number(q("routeStopObj").value);
      const max=Number(q("routeStopMax").value);

      const error=q("routeStopConfigError");
      error.textContent="";

      if(!min || !obj || !max){
        error.textContent="Todos los tiempos son obligatorios.";
        return;
      }

      if(min>obj || obj>max){
        error.textContent=
          "Los tiempos deben cumplir: mínimo ≤ objetivo ≤ máximo.";
        return;
      }

      const btn=q("routeStopConfigSave");
      btn.disabled=true;

      try{

        await json(
          `/route-stops/${id}/configuracion`,
          {
            method:"PUT",
            body:JSON.stringify({
              tipoParada:q("routeStopType").value,
              tiempoMinimoSegundos:min,
              tiempoObjetivoSegundos:obj,
              tiempoMaximoSegundos:max
            })
          }
        );

        notify("Configuración actualizada");

        configModal.hide();

        el.addEventListener(
          "hidden.bs.modal",
          async()=>{
            el.remove();

            if(routeId){
              await crudRouteStops(routeId);
            }
          },
          {once:true}
        );

      }catch(e){

        error.textContent=
          e.message||
          "No se pudo actualizar la parada.";

        btn.disabled=false;
      }
    };

    configModal.show();
  };

  window.loadViolations=async()=>{try{violationsCache=await json("/violations");q("violationsTable").innerHTML=violationsCache.map(x=>`<tr><td>${x.id}</td><td>${esc(x.driver?.nombre||"—")}</td><td>${esc(x.bus?.codigo||"—")}</td><td>${esc(x.tipo||"—")}</td><td>${esc(x.descripcion||"—")}</td><td>${date(x.fechaHora)}</td><td>${state(x.estado)}</td><td class="actions">${x.estado==="PENDING"?`<button class="table-btn edit" onclick="crudViolation(${x.id})">Editar</button><button class="table-btn on" onclick="crudViolationAction(${x.id},'confirm')">Confirmar</button><button class="table-btn off" onclick="crudViolationAction(${x.id},'reject')">Rechazar</button>`:x.estado!=="CANCELLED"?`<button class="table-btn off" onclick="crudViolationAction(${x.id},'cancel')">Cancelar</button>`:"—"}</td></tr>`).join("")||empty(8,"No hay sanciones.")}catch(e){q("violationsTable").innerHTML=empty(8,e.message)}};
  window.crudViolation=id=>{let x=violationsCache.find(v=>v.id===id);openForm("Editar infracción pendiente",`<div class="row g-3">${field("Tipo","vt",x.tipo,"text","required")}${field("Velocidad registrada","vs",x.velocidadRegistrada??"","number",'step="any" min="0"')}${field("Límite permitido","vl",x.limitePermitido??"","number",'step="any" min="0.1"')}${area("Descripción","vd",x.descripcion)}</div>`,async()=>{let ok=await act(`/violations/${id}`,{method:"PUT",body:JSON.stringify({tipo:val("vt"),descripcion:val("vd"),velocidadRegistrada:val("vs")===""?null:num("vs"),limitePermitido:val("vl")===""?null:num("vl")})},"Infracción actualizada");if(ok)loadViolations();return ok})};
  window.crudViolationAction=async(id,a)=>{let body;

if (a === "cancel") {

  const j = await smartPrompt(
    "Indica por qué se cancelará esta sanción.",
    {
      title: "Cancelar sanción",
      label: "Justificación obligatoria",
      placeholder: "Escribe el motivo de cancelación...",
      acceptText: "Cancelar sanción",
      danger: true
    }
  );

  if (!j) return;

  body = JSON.stringify({
    justificacion: j
  });

} else {

  const confirmed = await smartConfirm(
    a === "confirm"
      ? "Al confirmar, aumentará el contador de sanciones del piloto."
      : "La sanción será marcada como rechazada.",
    {
      title:
        a === "confirm"
          ? "Confirmar sanción"
          : "Rechazar sanción",
      acceptText:
        a === "confirm"
          ? "Confirmar sanción"
          : "Rechazar",
      danger: a === "reject"
    }
  );

  if (!confirmed) return;
}let ok=await act(`/violations/${id}/${a}`,{method:"PUT",body},a==="confirm"?"Sanción confirmada":a==="reject"?"Sanción rechazada":"Sanción cancelada");if(ok){loadViolations();dashboard()}};

  window.loadAlerts=async()=>{try{alertsCache=await json("/passenger-alerts");q("alertsTable").innerHTML=alertsCache.map(x=>{let hasLocation=x.latitud!=null&&x.longitud!=null;let passenger=x.passengerNombre?`<b>${esc(x.passengerNombre)}</b><small class="crud-sub">ID usuario: ${x.passengerId??"—"}</small>`:`<span class="text-muted">Alerta anterior</span>`;let contact=x.passengerEmail?`${esc(x.passengerEmail)}<small class="crud-sub">${esc(x.passengerTelefono||"Sin teléfono")}</small>`:"—";let location=hasLocation?`<a class="table-btn info" href="https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(x.latitud+","+x.longitud)}" target="_blank" rel="noopener noreferrer">Ver ubicación</a><small class="crud-sub">${Number(x.latitud).toFixed(5)}, ${Number(x.longitud).toFixed(5)}</small>`:"—";return `<tr><td>${x.id}</td><td><b>${esc(x.tipo||"—")}</b></td><td>${passenger}</td><td>${contact}</td><td>${esc(x.descripcion||"—")}</td><td>${location}</td><td>${date(x.fechaHora)}</td><td>${state(x.estado)}</td><td class="actions">${x.estado==="PENDING"?`<button class="table-btn edit" onclick="crudAlert(${x.id},'review')">Revisar</button>`:x.estado==="REVIEWED"?`<button class="table-btn on" onclick="crudAlert(${x.id},'resolve')">Resolver</button>`:"—"}</td></tr>`}).join("")||empty(9,"No hay alertas.")}catch(e){q("alertsTable").innerHTML=empty(9,e.message)}};
  window.crudAlert=async(id,a)=>{if(!await smartConfirm(
  a === "review"
    ? "La alerta será marcada como en revisión."
    : "La alerta será marcada como resuelta.",
  {
    title:
      a === "review"
        ? "Revisar alerta"
        : "Resolver alerta",
    acceptText:
      a === "review"
        ? "Iniciar revisión"
        : "Resolver"
  }
)) return;let ok=await act(`/passenger-alerts/${id}/${a}`,{method:"PUT"},a==="review"?"Alerta en revisión":"Alerta resuelta");if(ok)loadAlerts()};

  window.loadUsers=async()=>{try{usersCache=await json("/users");q("usersTable").innerHTML=usersCache.map(x=>`<tr><td><b>${esc(x.nombre)}</b>${x.licencia?`<small class="crud-sub">Licencia: ${esc(x.licencia)}</small>`:""}</td><td>${esc(x.email)}</td><td>${esc(x.telefono||"—")}</td><td>${pill(x.role,x.role==="ADMIN"?"info":"neutral")}</td><td>${pill(x.activo?"Activo":"Inactivo",x.activo?"success":"neutral")}</td><td class="actions"><button class="table-btn edit" onclick="crudUser(${x.id})">Editar</button><button class="table-btn info" onclick="crudPassword(${x.id})">Contraseña</button><button class="table-btn ${x.activo?"off":"on"}" onclick="crudUserToggle(${x.id},${!x.activo})">${x.activo?"Desactivar":"Activar"}</button><button class="table-btn delete-permanent" onclick="smartPermanentDelete('user',${x.id},'${esc(x.nombre)}',loadUsers)">Eliminar</button></td></tr>`).join("")||empty(6,"No hay usuarios.")}catch(e){q("usersTable").innerHTML=empty(6,e.message)}};
  window.crudNewUser=()=>openForm("Crear usuario",`<div class="row g-3">${sel("Rol","ur",[{value:"ADMIN",label:"Administrador"},{value:"DRIVER",label:"Piloto"}])}${field("Nombre","un","","text","required")}${field("Correo","ue","","email","required")}${field("Contraseña","up","","password",'minlength="8" required')}${field("Licencia (solo DRIVER)","ul","","text")}</div><p class="crud-help mt-3">Para DRIVER debe existir previamente un piloto activo con esa licencia.</p>`,async()=>{let role=val("ur"),body={nombre:val("un"),email:val("ue"),password:val("up")};if(role==="DRIVER")body.licencia=val("ul");let ok=await act(`/users/${role==="DRIVER"?"drivers":"admins"}`,{method:"POST",body:JSON.stringify(body)},"Usuario creado");if(ok)loadUsers();return ok});
  window.crudUser=id=>{let x=usersCache.find(v=>v.id===id);openForm("Editar usuario",`<div class="row g-3">${field("Nombre","un",x.nombre,"text","required")}${field("Correo","ue",x.email,"email","required")}${field("Teléfono","ut",x.telefono||"","tel",'maxlength="20"')}</div>`,async()=>{let ok=await act(`/users/${id}`,{method:"PUT",body:JSON.stringify({nombre:val("un"),email:val("ue"),telefono:val("ut")})},"Usuario actualizado");if(ok)loadUsers();return ok})};
  window.crudPassword=id=>openForm("Cambiar contraseña",`<div class="row g-3">${field("Nueva contraseña","up","","password",'minlength="8" required')}</div><p class="crud-help mt-3">Mínimo 8 caracteres.</p>`,()=>act(`/users/${id}/password`,{method:"PUT",body:JSON.stringify({password:val("up")})},"Contraseña actualizada"));
  window.crudUserToggle=async(id,on)=>{if(!await smartConfirm(
  on
    ? "¿Deseas activar esta cuenta de usuario?"
    : "La cuenta dejará de tener acceso al sistema.",
  {
    title:
      on
        ? "Activar usuario"
        : "Desactivar usuario",
    acceptText:
      on
        ? "Activar"
        : "Desactivar",
    danger: !on
  }
)) return;let ok=await act(`/users/${id}/${on?"activate":"deactivate"}`,{method:"PUT"},on?"Usuario activado":"Usuario desactivado");if(ok)loadUsers()};

  function enhance(){
    ["buses","paradas","pilotos","sanciones","alertas","usuarios"].forEach(id=>{let tr=q(id)?.querySelector("thead tr");if(tr&&!tr.querySelector(".crud-head")){let th=document.createElement("th");th.className="crud-head";th.textContent="Acciones";tr.appendChild(th)}});
    top("buses","Nuevo bus",()=>crudBus()); top("paradas","Nueva parada",()=>crudStop()); top("pilotos","Nuevo piloto",()=>crudDriver(),"person-plus"); top("usuarios","Nuevo usuario",crudNewUser,"person-plus");
  }

  // Integración definitiva con la navegación principal.
  // app.js emite este evento después de mostrar una vista.
  document.addEventListener("smartroute:view", e => {
    enhance();

    const loaders = {
      buses: window.loadBuses,
      paradas: window.loadStops,
      pilotos: window.loadDrivers,
      rutas: window.loadRoutes,
      sanciones: window.loadViolations,
      alertas: window.loadAlerts,
      usuarios: window.loadUsers
    };

    const loader = loaders[e.detail];

    if (loader) {
      loader();
    }
  });

  enhance();

})();
