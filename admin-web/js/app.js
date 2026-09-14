

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
    })
.catch(error => {
    console.error("Error al conectar con pilotos:", error);
    mostrarError("No se pudo conectar con el servicio de pilotos.");
});