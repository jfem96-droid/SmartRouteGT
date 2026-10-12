/* SmartRoute GT | dashboards basados exclusivamente en registros reales */
(()=>{
 'use strict';
 const configs={
  reportes:{paths:['/passenger-reports','/passenger-feedback','/driver-reports'],tables:['reportsTable','smartFeedbackTable','driverReportsTable']},
  alertas:{paths:['/passenger-alerts'],tables:['alertsTable']},
  sanciones:{paths:['/violations'],tables:['violationsTable']}
 };
 const escapeText=v=>String(v??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
 const statusText={PENDING:'Pendiente',UNDER_REVIEW:'En revisión',REVIEWED:'Revisada',RESOLVED:'Resuelta',REJECTED:'Rechazada',CONFIRMED:'Confirmada',CANCELLED:'Cancelada'};
 const dayOf=x=>{const raw=x.createdAt||x.fechaHora||x.fecha||x.timestamp; if(!raw)return null;const d=new Date(raw);return Number.isFinite(d.getTime())?d:null};
 const allRows=(m)=>configs[m].tables.flatMap(id=>[...document.querySelectorAll('#'+id+' tr')]);
 function kpi(label,value,icon,tone=''){return `<div class="sr-kpi ${tone}"><div class="sr-kpi-icon"><i class="bi ${icon}"></i></div><div><span>${escapeText(label)}</span><strong>${escapeText(value)}</strong></div></div>`}
 function charts(module,items){
  const chart=document.getElementById('sr-'+module+'-chart'),dist=document.getElementById('sr-'+module+'-distribution');if(!chart||!dist)return;
  const now=new Date();now.setHours(0,0,0,0);
  const days=Array.from({length:7},(_,i)=>{const d=new Date(now);d.setDate(now.getDate()-6+i);return d});
  const counts=days.map(d=>items.filter(x=>{const t=dayOf(x);return t&&t.getFullYear()===d.getFullYear()&&t.getMonth()===d.getMonth()&&t.getDate()===d.getDate()}).length);
  const max=Math.max(1,...counts);
  chart.innerHTML=`<div class="sr-bars">${days.map((d,i)=>`<div class="sr-bar-col"><b>${counts[i]}</b><div class="sr-bar" style="height:${Math.max(3,counts[i]/max*137)}px" title="${counts[i]} registros"></div><small>${d.toLocaleDateString('es-GT',{day:'numeric',month:'short'})}</small></div>`).join('')}</div>`;
  const groups={};items.forEach(x=>{let key=module==='reportes'?(x.kind==='COMPLIMENT'?'Felicitaciones':x.kind==='RATING'?'Calificaciones':statusText[x.estado||x.status]||'Reportes'):statusText[x.estado||x.status]||'Sin estado';groups[key]=(groups[key]||0)+1});
  const sorted=Object.entries(groups).sort((a,b)=>b[1]-a[1]).slice(0,6);
  dist.innerHTML=sorted.length?`<div class="sr-statuses">${sorted.map(([name,count])=>`<div><div class="sr-status-line"><span>${escapeText(name)}</span><strong>${count}</strong></div><div class="sr-progress"><span style="width:${count/items.length*100}%"></span></div></div>`).join('')}</div>`:'<div class="sr-empty">Aún no hay registros para analizar.</div>';
 }
 function filterRows(module){const q=(document.getElementById('sr-'+module+'-search')?.value||'').trim().toLowerCase();const state=document.getElementById('sr-'+module+'-status')?.value||'';allRows(module).forEach(tr=>{const txt=tr.textContent.toLowerCase();tr.hidden=!!((q&&!txt.includes(q))||(state&&!txt.includes(state.toLowerCase())))});}
 async function render(module){if(!configs[module]||!document.getElementById('sr-'+module+'-metrics'))return;
  const cfg=configs[module],responses=await Promise.all(cfg.paths.map(path=>json(path).catch(e=>({error:e.message}))));
  const arrays=responses.map(x=>Array.isArray(x)?x:[]),issues=responses.filter(x=>!Array.isArray(x));
  const m=document.getElementById('sr-'+module+'-metrics');let items=[];
  if(module==='reportes'){
   const [reports,feedback,driverReports]=arrays;const ratings=feedback.filter(x=>x.kind==='RATING'&&Number.isFinite(Number(x.stars)));const praises=feedback.filter(x=>x.kind==='COMPLIMENT');const avg=ratings.length?(ratings.reduce((sum,x)=>sum+Number(x.stars),0)/ratings.length).toFixed(1)+' / 5':'—';
   m.innerHTML=kpi('Reportes de pasajeros',reports.length,'bi-shield-exclamation','danger')+kpi('Felicitaciones',praises.length,'bi-heart-fill','positive')+kpi('Calificación promedio',avg,'bi-star-fill','warning')+kpi('Incidentes de pilotos',driverReports.length,'bi-clipboard2-pulse');
   items=[...reports,...feedback,...driverReports];
  }else if(module==='alertas'){
   items=arrays[0];const sos=items.filter(x=>String(x.tipo).toUpperCase()==='SOS').length;
   m.innerHTML=kpi('Alertas recibidas',items.length,'bi-bell-fill')+kpi('SOS',sos,'bi-exclamation-octagon-fill','danger')+kpi('Pendientes',items.filter(x=>x.estado==='PENDING').length,'bi-clock-history','warning')+kpi('Resueltas',items.filter(x=>x.estado==='RESOLVED').length,'bi-check-circle-fill','positive');
  }else{
   items=arrays[0];m.innerHTML=kpi('Sanciones registradas',items.length,'bi-shield-exclamation')+kpi('Pendientes',items.filter(x=>x.estado==='PENDING').length,'bi-clock-history','warning')+kpi('Confirmadas',items.filter(x=>x.estado==='CONFIRMED').length,'bi-check-circle-fill','danger')+kpi('Rechazadas / canceladas',items.filter(x=>['REJECTED','CANCELLED'].includes(x.estado)).length,'bi-x-circle-fill');
  }
  if(issues.length){m.insertAdjacentHTML('afterend',`<p class="sr-data-error" style="color:#b42318;font-size:12px">No se pudieron consultar algunas fuentes. Comprueba tu sesión y conexión.</p>`);const old=document.querySelectorAll('.sr-data-error');if(old.length>1)old[0].remove()}
  else document.querySelectorAll('#'+module+' .sr-data-error').forEach(e=>e.remove());
  charts(module,items);
  const select=document.getElementById('sr-'+module+'-status');const prev=select.value;const states=[...new Set(items.map(x=>x.estado||x.status).filter(Boolean))];select.innerHTML='<option value="">Todos los estados</option>'+states.map(x=>`<option value="${escapeText(statusText[x]||x)}">${escapeText(statusText[x]||x)}</option>`).join('');select.value=[...select.options].some(o=>o.value===prev)?prev:'';
  filterRows(module);
 }
 function refresh(module){if(module==='reportes'){loadReports();smartLoadDriverReports();smartLoadFeedback()}else if(module==='alertas'){if(typeof window.loadAlerts==='function')window.loadAlerts()}else if(typeof window.loadViolations==='function')window.loadViolations();render(module)}
 function exportCSV(module){const rows=configs[module].tables.flatMap(id=>{const t=document.getElementById(id);if(!t)return[];const table=t.closest('table');const head=[...table.querySelectorAll('thead th')].map(x=>x.textContent.trim());return [...t.querySelectorAll('tbody tr')].filter(tr=>!tr.hidden&&tr.cells.length>1).map(tr=>[...tr.cells].map((c,i)=>i===0?c.innerText.trim():c.innerText.trim()).slice(0,head.length)).map(c=>[module,...c])});
  const csv=[['Módulo','Datos (columnas según tabla)'],...rows].map(row=>row.map(v=>'"'+String(v).replaceAll('"','""')+'"').join(',')).join('\r\n');const url=URL.createObjectURL(new Blob(['\ufeff'+csv],{type:'text/csv;charset=utf-8'}));const a=document.createElement('a');a.href=url;a.download=`smartroute_${module}_${new Date().toISOString().slice(0,10)}.csv`;a.click();setTimeout(()=>URL.revokeObjectURL(url),1000)}
 document.addEventListener('click',e=>{const refreshButton=e.target.closest('[data-sr-refresh]');if(refreshButton)refresh(refreshButton.dataset.srRefresh);const exportButton=e.target.closest('[data-sr-export]');if(exportButton)exportCSV(exportButton.dataset.srExport)});
 document.addEventListener('input',e=>{if(e.target.id?.startsWith('sr-')&&e.target.id.endsWith('-search'))filterRows(e.target.id.slice(3,-7))});
 document.addEventListener('change',e=>{if(e.target.id?.startsWith('sr-')&&e.target.id.endsWith('-status'))filterRows(e.target.id.slice(3,-7))});
 document.addEventListener('smartroute:view',e=>{const m=e.detail;if(configs[m]){render(m);setTimeout(()=>filterRows(m),700)}});
 setInterval(()=>{const m=location.hash.replace('#/','');if(configs[m]&&typeof session!=='undefined'&&session?.token)render(m)},30000);
})();
