package ar.edu.utn.frc.dsi.bolsines.adapter;

/**
 * Interfaz comun del Patron Adapter de salida (Entrega 2).
 * Cada fila de la matriz devuelta por recibirUbicacionBolsin() responde al formato uniforme:
 * [0] numeroBolsin
 * [1] latitud
 * [2] longitud
 * [3] fechaHoraUltimaActualizacion
 * [4] estadoGPS ("OK" | "ERROR")
 * [5] mensajeError
 * [6] respuestaCruda
 */
public interface IAdaptadorGPSTracker {

    String[][] recibirUbicacionBolsin();

    void solicitarUbicacionBolsin();

    void adaptarUbicacionBolsin();
}
