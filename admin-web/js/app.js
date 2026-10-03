

const mensajeSistema = document.getElementById("mensajeSistema");

function mostrarError(mensaje) {
    mensajeSistema.textContent = mensaje;
    mensajeSistema.classList.remove("d-none");
    mensajeSistema.classList.add("alert-danger");
}
fetch("http://localhost:8080/api/buses")
    .then(response => {
        if (!response.ok) {
            throw new Error("No se pudo obtener la información de buses");
        }

        return response.json();
    })
    .then(buses => {
    console.log("Buses recibidos:", buses);

    const busesActivos = buses.filter(bus => bus.activa === true).length;

    document.getElementById("busesActivos").textContent = busesActivos;
})
    
    .catch(error => {
    console.error("Error al conectar con el backend:", error);
    mostrarError("No se pudo conectar con el servicio de buses.");
});

    let rutaEditandoId = null;

function cargarRutas() {
    fetch("http://localhost:8080/api/routes")
        .then(response => {
            if (!response.ok) {
                throw new Error("No se pudo obtener la información de rutas");
            }

            return response.json();
        })
        .then(rutas => {
            mostrarRutas(rutas);
        })
        .catch(error => {
            console.error("Error al cargar rutas:", error);

            const tablaRutas = document.getElementById("tablaRutas");

            tablaRutas.innerHTML = `
                <tr>
                    <td colspan="5" class="text-center text-danger">
                        No se pudieron cargar las rutas.
                    </td>
                </tr>
            `;
        });
}

function mostrarRutas(rutas) {
    const tablaRutas = document.getElementById("tablaRutas");

    tablaRutas.innerHTML = "";

    if (rutas.length === 0) {
        tablaRutas.innerHTML = `
            <tr>
                <td colspan="5" class="text-center text-muted">
                    No hay rutas registradas.
                </td>
            </tr>
        `;
        return;
    }

    rutas.forEach(ruta => {
        const fila = document.createElement("tr");

        fila.innerHTML = `
            <td>${ruta.id}</td>
            <td>${ruta.codigo}</td>
            <td>${ruta.nombre}</td>
            <td>
                ${ruta.activa
                    ? '<span class="badge bg-success">Activa</span>'
                    : '<span class="badge bg-secondary">Inactiva</span>'
                }
            </td>
            <td>
                <button
                    class="btn btn-sm btn-secondary me-1"
                    onclick='editarRuta(${JSON.stringify(ruta)})'>
                    Editar
                </button>

                ${
                    ruta.activa
                        ? `<button
                            class="btn btn-sm btn-danger"
                            onclick="cambiarEstadoRuta(${ruta.id}, false)">
                            Desactivar
                           </button>`
                        : `<button
                            class="btn btn-sm btn-success"
                            onclick="cambiarEstadoRuta(${ruta.id}, true)">
                            Activar
                           </button>`
                }
            </td>
        `;

        tablaRutas.appendChild(fila);
    });
}

function editarRuta(ruta) {
    rutaEditandoId = ruta.id;

    document.getElementById("modalRutaLabel").textContent = "Editar ruta";
    document.getElementById("codigoRuta").value = ruta.codigo;
    document.getElementById("nombreRuta").value = ruta.nombre;
    document.getElementById("activaRuta").checked = ruta.activa;
    document.getElementById("btnGuardarRuta").textContent = "Actualizar";

    const modal = new bootstrap.Modal(document.getElementById("modalRuta"));
    modal.show();
}

document.getElementById("btnNuevaRuta").addEventListener("click", () => {
    rutaEditandoId = null;

    document.getElementById("modalRutaLabel").textContent = "Nueva ruta";
    document.getElementById("btnGuardarRuta").textContent = "Guardar";
    document.getElementById("formRuta").reset();
    document.getElementById("activaRuta").checked = true;
});

document.getElementById("formRuta").addEventListener("submit", event => {
    event.preventDefault();

    const codigo = document.getElementById("codigoRuta").value.trim();
    const nombre = document.getElementById("nombreRuta").value.trim();
    const activa = document.getElementById("activaRuta").checked;

    if (!codigo || !nombre) {
        alert("El código y el nombre son obligatorios.");
        return;
    }

    const ruta = {
        codigo: codigo,
        nombre: nombre,
        activa: activa
    };

    const url = rutaEditandoId === null
        ? "http://localhost:8080/api/routes"
        : `http://localhost:8080/api/routes/${rutaEditandoId}`;

    const metodo = rutaEditandoId === null ? "POST" : "PUT";

    fetch(url, {
        method: metodo,
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(ruta)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("No se pudo guardar la ruta");
            }

            return response.json();
        })
        .then(() => {
            const modalElement = document.getElementById("modalRuta");
            const modal = bootstrap.Modal.getInstance(modalElement);

            if (modal) {
                modal.hide();
            }

            document.getElementById("formRuta").reset();
            rutaEditandoId = null;

            cargarRutas();
        })
        .catch(error => {
            console.error("Error al guardar ruta:", error);
            alert("No se pudo guardar la ruta.");
        });
});

function cambiarEstadoRuta(id, activar) {
    const url = activar
        ? `http://localhost:8080/api/routes/${id}/activar`
        : `http://localhost:8080/api/routes/${id}`;

    const metodo = activar ? "PUT" : "DELETE";

    fetch(url, {
        method: metodo
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("No se pudo cambiar el estado de la ruta");
            }

            return response.text();
        })
        .then(() => {
            cargarRutas();
        })
        .catch(error => {
            console.error("Error al cambiar estado:", error);
            alert("No se pudo cambiar el estado de la ruta.");
        });
}

cargarRutas();

    // PILOTOS
fetch("http://localhost:8080/api/drivers")
    .then(response => {
        if (!response.ok) {
            throw new Error("No se pudo obtener la información de pilotos");
        }

        return response.json();
    })
    .then(pilotos => {
    console.log("Pilotos recibidos:", pilotos);

    const pilotosActivos = pilotos.filter(piloto => piloto.activo === true).length;

    document.getElementById("pilotosActivos").textContent = pilotosActivos;
})
.catch(error => {
    console.error("Error al conectar con pilotos:", error);
    mostrarError("No se pudo conectar con el servicio de pilotos.");
});

fetch("http://localhost:8080/api/violations")
    .then(response => {
        if (!response.ok) {
            throw new Error("No se pudo obtener la información de infracciones");
        }

        return response.json();
    })
    .then(infracciones => {
        console.log("Infracciones recibidas:", infracciones);

        const infraccionesPendientes = infracciones.filter(
            infraccion => infraccion.estado === "PENDING"
        ).length;

        document.getElementById("infraccionesPendientes").textContent =
            infraccionesPendientes;
    })
    .catch(error => {
        console.error("Error al conectar con infracciones:", error);
        mostrarError("No se pudo conectar con el servicio de infracciones.");
    });

    fetch("http://localhost:8080/api/passenger-alerts")
    .then(response => {
        if (!response.ok) {
            throw new Error("No se pudo obtener la información de alertas");
        }

        return response.json();
    })
    .then(alertas => {
        console.log("Alertas recibidas:", alertas);

        const alertasPendientes = alertas.filter(
            alerta => alerta.estado === "PENDING"
        ).length;

        document.getElementById("alertasPendientes").textContent =
            alertasPendientes;
    })
    .catch(error => {
        console.error("Error al conectar con alertas:", error);
        mostrarError("No se pudo conectar con el servicio de alertas.");
    });


    // MONITOREO DE BUSES
const tablaMonitoreoBuses = document.getElementById("tablaMonitoreoBuses");

fetch("http://localhost:8080/api/buses")
    .then(response => {
        if (!response.ok) {
            throw new Error("No se pudo obtener la información de buses");
        }

        return response.json();
    })
    .then(buses => {
        tablaMonitoreoBuses.innerHTML = "";

        if (buses.length === 0) {
            tablaMonitoreoBuses.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center text-muted">
                        No hay buses registrados.
                    </td>
                </tr>
            `;
            return;
        }

        buses.forEach(bus => {
            fetch(`http://localhost:8080/api/locations/bus/${bus.id}/latest`)
                .then(response => {
                    if (response.status === 404) {
                        return null;
                    }

                    if (!response.ok) {
                        throw new Error(
                            `No se pudo obtener la ubicación del bus ${bus.codigo}`
                        );
                    }

                    return response.json();
                })
                .then(ubicacion => {
                    const fila = document.createElement("tr");

                    const estado = bus.activa
                        ? '<span class="badge bg-success">Activo</span>'
                        : '<span class="badge bg-secondary">Inactivo</span>';

                    const velocidad = ubicacion
                        ? `${Number(ubicacion.velocidad).toFixed(1)} km/h`
                        : "Sin registro";

                    const latitud = ubicacion
                        ? ubicacion.latitud
                        : "Sin registro";

                    const longitud = ubicacion
                        ? ubicacion.longitud
                        : "Sin registro";

                    const fechaHora = ubicacion
                        ? new Date(ubicacion.fechaHora).toLocaleString()
                        : "Sin ubicación registrada";

                    fila.innerHTML = `
                        <td>${bus.codigo}</td>
                        <td>${bus.placa}</td>
                        <td>${estado}</td>
                        <td>${velocidad}</td>
                        <td>${latitud}</td>
                        <td>${longitud}</td>
                        <td>${fechaHora}</td>
                    `;

                    tablaMonitoreoBuses.appendChild(fila);
                })
                .catch(error => {
                    console.error(
                        `Error al consultar ubicación del bus ${bus.codigo}:`,
                        error
                    );
                });
        });
    })
    .catch(error => {
        console.error("Error al cargar monitoreo de buses:", error);

        tablaMonitoreoBuses.innerHTML = `
            <tr>
                <td colspan="7" class="text-center text-danger">
                    No se pudo cargar el monitoreo de buses.
                </td>
            </tr>
        `;
    });

    // MAPA EN VIVO DE BUSES
const mapaBuses = L.map("mapaBuses").setView([14.6349, -90.5069], 12);

L.tileLayer("https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png", {
    maxZoom: 17,
    attribution: "Map data: &copy; OpenStreetMap contributors, SRTM | Map style: &copy; OpenTopoMap"
}).addTo(mapaBuses);

const marcadoresBuses = new Map();

function cargarBusesEnMapa() {
    fetch("http://localhost:8080/api/buses")
        .then(response => {
            if (!response.ok) {
                throw new Error("No se pudieron obtener los buses para el mapa");
            }

            return response.json();
        })
       .then(buses => {
    buses.forEach(bus => {
        fetch(`http://localhost:8080/api/locations/bus/${bus.id}/latest`)
            .then(response => {
                if (response.status === 404) {
                    return null;
                }

                if (!response.ok) {
                    throw new Error(
                        `No se pudo obtener la ubicación del bus ${bus.codigo}`
                    );
                }

                return response.json();
            })
            .then(ubicacion => {
    if (!ubicacion) {
        return;
    }

   let marcador = marcadoresBuses.get(bus.id);

if (marcador) {
    marcador.setLatLng([
        ubicacion.latitud,
        ubicacion.longitud
    ]);
} else {
    marcador = L.marker([
        ubicacion.latitud,
        ubicacion.longitud
    ]).addTo(mapaBuses);

    marcadoresBuses.set(bus.id, marcador);
}

    marcador.bindPopup(`
        <strong>${bus.codigo}</strong><br>
        Placa: ${bus.placa}<br>
        Velocidad: ${Number(ubicacion.velocidad).toFixed(1)} km/h<br>
        Última actualización:
        ${new Date(ubicacion.fechaHora).toLocaleString()}
    `);

})
            .catch(error => {
                console.error(
                    `Error al consultar GPS del bus ${bus.codigo}:`,
                    error
                );
            });
    });
})
        .catch(error => {
            console.error("Error al cargar buses en el mapa:", error);
        });
}

cargarBusesEnMapa();

setInterval(cargarBusesEnMapa, 10000);

// Cargar paradas
fetch("http://localhost:8080/api/stops")
    .then(response => {
        if (!response.ok) {
            throw new Error("Error al cargar las paradas");
        }
        return response.json();
    })
    .then(paradas => {
        const tablaParadas = document.getElementById("tablaParadas");

        if (paradas.length === 0) {
            tablaParadas.innerHTML = `
                <tr>
                    <td colspan="5" class="text-center text-muted">
                        No hay paradas registradas.
                    </td>
                </tr>
            `;
            return;
        }

        tablaParadas.innerHTML = "";

        paradas.forEach(parada => {
            const fila = document.createElement("tr");

            fila.innerHTML = `
                <td>${parada.nombre}</td>
                <td>${parada.latitud}</td>
                <td>${parada.longitud}</td>
                <td>${parada.radioPermitido}</td>
                <td>${parada.activa ? "Activa" : "Inactiva"}</td>
            `;

            tablaParadas.appendChild(fila);
        });
    })
    .catch(error => {
        console.error("Error al cargar paradas:", error);

        const tablaParadas = document.getElementById("tablaParadas");

        tablaParadas.innerHTML = `
            <tr>
                <td colspan="5" class="text-center text-danger">
                    No se pudieron cargar las paradas.
                </td>
            </tr>
        `;
    });

    // Cargar buses
fetch("http://localhost:8080/api/buses")
    .then(response => {
        if (!response.ok) {
            throw new Error("Error al cargar los buses");
        }
        return response.json();
    })
    .then(buses => {
        const tablaBuses = document.getElementById("tablaBuses");

        if (buses.length === 0) {
            tablaBuses.innerHTML = `
                <tr>
                    <td colspan="5" class="text-center text-muted">
                        No hay buses registrados.
                    </td>
                </tr>
            `;
            return;
        }

        tablaBuses.innerHTML = "";

        buses.forEach(bus => {
            const fila = document.createElement("tr");

            fila.innerHTML = `
                <td>${bus.id}</td>
                <td>${bus.codigo}</td>
                <td>${bus.placa}</td>
                <td>${bus.capacidad}</td>
                <td>${bus.activa ? "Activo" : "Inactivo"}</td>
            `;

            tablaBuses.appendChild(fila);
        });
    })
    .catch(error => {
        console.error("Error al cargar buses:", error);

        const tablaBuses = document.getElementById("tablaBuses");

        tablaBuses.innerHTML = `
            <tr>
                <td colspan="5" class="text-center text-danger">
                    No se pudieron cargar los buses.
                </td>
            </tr>
        `;
    });

    // Cargar pilotos
fetch("http://localhost:8080/api/drivers")
    .then(response => {
        if (!response.ok) {
            throw new Error("Error al cargar los pilotos");
        }
        return response.json();
    })
    .then(pilotos => {
        const tablaPilotos = document.getElementById("tablaPilotos");

        if (pilotos.length === 0) {
            tablaPilotos.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center text-muted">
                        No hay pilotos registrados.
                    </td>
                </tr>
            `;
            return;
        }

        tablaPilotos.innerHTML = "";

        pilotos.forEach(piloto => {
            const fila = document.createElement("tr");

            fila.innerHTML = `
                <td>${piloto.id}</td>
                <td>${piloto.nombre}</td>
                <td>${piloto.licencia}</td>
                <td>${piloto.telefono}</td>
                <td>${piloto.sancionesConfirmadas}</td>
                <td>${piloto.activo ? "Activo" : "Inactivo"}</td>
            `;

            tablaPilotos.appendChild(fila);
        });
    })
    .catch(error => {
        console.error("Error al cargar pilotos:", error);

        const tablaPilotos = document.getElementById("tablaPilotos");

        tablaPilotos.innerHTML = `
            <tr>
                <td colspan="6" class="text-center text-danger">
                    No se pudieron cargar los pilotos.
                </td>
            </tr>
        `;
    });

    // Cargar sanciones
fetch("http://localhost:8080/api/violations")
    .then(response => {
        if (!response.ok) {
            throw new Error("Error al cargar las sanciones");
        }
        return response.json();
    })
    .then(sanciones => {
        const tablaSanciones = document.getElementById("tablaSanciones");

        if (sanciones.length === 0) {
            tablaSanciones.innerHTML = `
                <tr>
                    <td colspan="9" class="text-center text-muted">
                        No hay sanciones registradas.
                    </td>
                </tr>
            `;
            return;
        }

        tablaSanciones.innerHTML = "";

        sanciones.forEach(sancion => {
            const fila = document.createElement("tr");

            fila.innerHTML = `
                <td>${sancion.id}</td>
                <td>${sancion.driver?.nombre ?? "-"}</td>
                <td>${sancion.bus?.codigo ?? "-"}</td>
                <td>${sancion.tipo ?? "-"}</td>
                <td>${sancion.descripcion ?? "-"}</td>
                <td>${sancion.velocidadRegistrada != null
                    ? Number(sancion.velocidadRegistrada).toFixed(1) + " km/h"
                    : "-"}</td>
                <td>${sancion.limitePermitido != null
                    ? Number(sancion.limitePermitido).toFixed(1) + " km/h"
                    : "-"}</td>
                <td>${sancion.fechaHora
                    ? new Date(sancion.fechaHora).toLocaleString()
                    : "-"}</td>
                <td>${sancion.estado ?? "-"}</td>
            `;

            tablaSanciones.appendChild(fila);
        });
    })
    .catch(error => {
        console.error("Error al cargar sanciones:", error);

        const tablaSanciones = document.getElementById("tablaSanciones");

        tablaSanciones.innerHTML = `
            <tr>
                <td colspan="9" class="text-center text-danger">
                    No se pudieron cargar las sanciones.
                </td>
            </tr>
        `;
    });

    // Cargar alertas de pasajeros
fetch("http://localhost:8080/api/passenger-alerts")
    .then(response => {
        if (!response.ok) {
            throw new Error("Error al cargar las alertas");
        }
        return response.json();
    })
    .then(alertas => {
        const tablaAlertas = document.getElementById("tablaAlertas");

        if (alertas.length === 0) {
            tablaAlertas.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center text-muted">
                        No hay alertas de pasajeros registradas.
                    </td>
                </tr>
            `;
            return;
        }

        tablaAlertas.innerHTML = "";

        alertas.forEach(alerta => {
            const fila = document.createElement("tr");

            fila.innerHTML = `
                <td>${alerta.id}</td>
                <td>${alerta.tipo ?? "-"}</td>
                <td>${alerta.descripcion ?? "-"}</td>
                <td>${alerta.latitud ?? "-"}</td>
                <td>${alerta.longitud ?? "-"}</td>
                <td>${alerta.fechaHora
                    ? new Date(alerta.fechaHora).toLocaleString()
                    : "-"}</td>
                <td>${alerta.estado ?? "-"}</td>
            `;

            tablaAlertas.appendChild(fila);
        });
    })
    .catch(error => {
        console.error("Error al cargar alertas:", error);

        const tablaAlertas = document.getElementById("tablaAlertas");

        tablaAlertas.innerHTML = `
            <tr>
                <td colspan="7" class="text-center text-danger">
                    No se pudieron cargar las alertas.
                </td>
            </tr>
        `;
    });