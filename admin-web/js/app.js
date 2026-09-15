

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

    fetch("http://localhost:8080/api/routes")
    .then(response => {
        if (!response.ok) {
            throw new Error("No se pudo obtener la información de rutas");
        }

        return response.json();
    })
    .then(rutas => {
        console.log("Rutas recibidas:", rutas);
    })
    .catch(error => {
    console.error("Error al conectar con rutas:", error);
    mostrarError("No se pudo conectar con el servicio de rutas.");
});

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