

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
    })
    
    .catch(error => {
    console.error("Error al conectar con el backend:", error);
    mostrarError("No se pudo conectar con el servicio de buses.");
});

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
        console.warn("Backend de rutas no disponible. Usando datos temporales.");

        const rutasDemo = [
            {
                id: 1,
                codigo: "R-01",
                nombre: "Ruta Centro",
                activa: true
            },
            {
                id: 2,
                codigo: "R-02",
                nombre: "Ruta Zona 18",
                activa: true
            },
            {
                id: 3,
                codigo: "R-03",
                nombre: "Ruta Zona 21",
                activa: false
            }
        ];

        mostrarRutas(rutasDemo);
    });

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
            <td>${ruta.activa ? "Activa" : "Inactiva"}</td>
            <td>
                <button class="btn btn-sm btn-secondary me-1">
                    Editar
                </button>

                ${
                    ruta.activa
                        ? `<button class="btn btn-sm btn-danger">Desactivar</button>`
                        : `<button class="btn btn-sm btn-success">Activar</button>`
                }
            </td>
        `;

        tablaRutas.appendChild(fila);
    });
}

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
    })
.catch(error => {
    console.error("Error al conectar con pilotos:", error);
    mostrarError("No se pudo conectar con el servicio de pilotos.");
});