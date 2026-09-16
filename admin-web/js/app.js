

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
                        ? `${ubicacion.velocidad} km/h`
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
