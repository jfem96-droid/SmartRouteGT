/* SmartRoute GT 1.3.1 - compatible con el panel existente. No envía acciones sin interacción del administrador. */
(()=>{'use strict';
const $=id=>document.getElementById(id);
const escapeHTML=x=>String(x??'—').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
// Ordenamiento por la columna que elija el administrador, sin alterar los registros.
const norm = value => String(value ?? '').normalize('NFD').replace(/[\u0300-\u036f]/g,'').toLocaleLowerCase('es-GT').trim();
function columnsOf(table){return [...table.querySelectorAll('thead th')].map((th,i)=>({i,label:th.textContent.trim(),key:norm(th.textContent)}));}
function choicesFor(table){
 const cols=columnsOf(table), choices=[];
 const specs=[['fecha','Fecha',/fecha|inicio|actualizacion|creacion|ultimo gps|hora/],['piloto','Piloto',/piloto|conductor/],['id','ID',/^id$/],['bus','Bus',/^bus$|codigo/],['estado','Estado',/estado/],['tipo','Tipo',/tipo/],['nombre','Nombre',/nombre/]];
 for(const [key,label,pattern] of specs){const col=cols.find(c=>pattern.test(c.key));if(col)choices.push({key,label,index:col.i});}
 return choices;
}
function comparable(row, choice){
 if(choice.key==='fecha'){
  const marked=row.querySelector('time[data-order]');
  if(marked){const n=Number(marked.dataset.order);if(Number.isFinite(n))return n;}
  const time=row.querySelector('time[datetime]');
  if(time){const n=Date.parse(time.dateTime||time.getAttribute('datetime'));if(Number.isFinite(n))return n;}
  const raw=row.cells[choice.index]?.textContent.trim();const n=raw?Date.parse(raw):NaN;
  return Number.isFinite(n)?n:null;
 }
 const cell=row.cells[choice.index];if(!cell)return null;
 const str=norm(cell.textContent);if(choice.key==='id'&&/^\d+$/.test(str))return Number(str);
 return str;
}
function sortTable(tbody,choice,ascending){
 if(!choice)return;
 const rows=[...tbody.rows].filter(tr=>!tr.querySelector('.empty')&&tr.cells.length>1);
 if(rows.length<2)return;
 const collator=new Intl.Collator('es-GT',{numeric:true,sensitivity:'base'});
 const ordered=[...rows].sort((a,b)=>{
  const x=comparable(a,choice),y=comparable(b,choice);
  if(x===null&&y!==null)return 1;if(x!==null&&y===null)return -1;
  const comparison=x===null?0:typeof x==='number'&&typeof y==='number'?x-y:collator.compare(String(x),String(y));
  return ascending?comparison:-comparison;
 });
 if(rows.some((row,i)=>row!==ordered[i]))tbody.append(...ordered);
}
function installSortControls(){
 document.querySelectorAll('main section.view table').forEach((table,i)=>{
  const tbody=table.tBodies[0];if(!tbody||table.dataset.sortReady)return;
  const choices=choicesFor(table);if(!choices.length)return;
  table.dataset.sortReady='1';
  const view=table.closest('section.view')?.id||'vista';
  const storageKey=`sr-order-fields-v2-${view}-${tbody.id||i}`;
  let saved={};try{saved=JSON.parse(localStorage.getItem(storageKey)||'{}')}catch{}
  const toolbar=document.createElement('div');toolbar.className='sr-sort-toolbar';
  const fieldLabel=document.createElement('label');fieldLabel.textContent='Ordenar por: ';
  const field=document.createElement('select');field.setAttribute('aria-label',`Campo para ordenar tabla de ${view}`);
  choices.forEach(choice=>{const option=document.createElement('option');option.value=choice.key;option.textContent=choice.label;field.append(option)});
  field.value=choices.some(c=>c.key===saved.field)?saved.field:(choices.find(c=>c.key==='fecha')||choices.find(c=>c.key==='piloto')||choices[0]).key;
  fieldLabel.append(field);
  const directionLabel=document.createElement('label');directionLabel.textContent='Sentido: ';
  const direction=document.createElement('select');direction.setAttribute('aria-label',`Sentido de orden de ${view}`);
  direction.innerHTML='<option value="desc">Descendente ↓</option><option value="asc">Ascendente ↑</option>';
  direction.value=saved.direction==='asc'?'asc':'desc';directionLabel.append(direction);
  toolbar.append(fieldLabel,directionLabel);
  const wrapper=table.closest('.table-responsive')||table;wrapper.parentNode.insertBefore(toolbar,wrapper);
  let running=false;
  const run=()=>{if(running)return;running=true;try{sortTable(tbody,choices.find(c=>c.key===field.value),direction.value==='asc')}finally{running=false}};
  const changed=()=>{try{localStorage.setItem(storageKey,JSON.stringify({field:field.value,direction:direction.value}))}catch{}run()};
  field.addEventListener('change',changed);direction.addEventListener('change',changed);
  const observer=new MutationObserver(()=>{if(!running)run()});observer.observe(tbody,{childList:true});
  run();
 });
}
// Navegación de indicadores del dashboard. Soporta mouse y teclado.
document.addEventListener('click',e=>{const link=e.target.closest('[data-smart-go]');if(link&&typeof window.go==='function')window.go(link.dataset.smartGo);const close=e.target.closest('[data-sr-finish-trip]');if(close)openCloseDialog(close.dataset.srFinishTrip)});
document.addEventListener('keydown',e=>{const link=e.target.closest('[data-smart-go]');if(link&&(e.key==='Enter'||e.key===' ')){e.preventDefault();if(typeof window.go==='function')window.go(link.dataset.smartGo)}});
let loading=false;
window.loadActiveTrips=async function(){const tbody=$('activeTripsTable');if(!tbody||loading)return;loading=true;tbody.innerHTML='<tr><td colspan="7">Consultando viajes activos…</td></tr>';try{const records=await json('/trips/active');if(!Array.isArray(records))throw Error('Respuesta no válida');tbody.innerHTML=records.map(t=>`<tr><td>${escapeHTML(t.id)}</td><td>${escapeHTML(t.bus?.codigo)}</td><td>${escapeHTML(t.driver?.nombre)}</td><td>${escapeHTML(t.route?.nombre)}</td><td>${escapeHTML(t.sentidoActual||'IDA')}</td><td>${date(t.fechaInicio)}</td><td><button type="button" class="btn btn-sm btn-outline-warning" data-sr-finish-trip="${Number(t.id)}">Finalizar con motivo</button></td></tr>`).join('')||'<tr><td colspan="7">No hay viajes activos.</td></tr>';}catch(e){tbody.innerHTML=`<tr><td colspan="7">${escapeHTML(e.message||e)}</td></tr>`}finally{loading=false}};
function openCloseDialog(id){if(!/^\d+$/.test(String(id)))return;const dialog=$('srCloseTripDialog');$('srCloseTripId').value=String(id);$('srCloseTripReason').value='';$('srCloseTripConfirm').value='';$('srCloseTripError').textContent='';dialog.showModal();}
document.addEventListener('DOMContentLoaded',()=>{
 installSortControls();
 $('refreshTrips')?.addEventListener('click',()=>window.loadActiveTrips());
 $('srCloseTripCancel')?.addEventListener('click',()=>$('srCloseTripDialog').close());
 $('srCloseTripForm')?.addEventListener('submit',async e=>{e.preventDefault();const id=$('srCloseTripId').value,reason=$('srCloseTripReason').value.trim();if(reason.length<15||$('srCloseTripConfirm').value.trim()!==id){$('srCloseTripError').textContent='Comprueba el ID y escribe un motivo de al menos 15 caracteres.';return}const submit=e.target.querySelector('button[type=submit]');submit.disabled=true;try{await json(`/trips/${id}/admin-finish`,{method:'PUT',body:JSON.stringify({reason})});$('srCloseTripDialog').close();await window.loadActiveTrips();if(typeof dashboard==='function')dashboard();}catch(error){$('srCloseTripError').textContent=error.message||'No se pudo cerrar el viaje.'}finally{submit.disabled=false}});
});
})();