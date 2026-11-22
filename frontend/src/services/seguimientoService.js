const API_BASE = 'http://localhost:8081/api/seguimiento';

async function requestJson(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(data.error || data.mensaje || `Error HTTP ${response.status}`);
  }
  return data;
}

export function listarSesiones() {
  return requestJson('/sesiones', { method: 'GET' });
}

export function iniciarConsulta(modeloGPS, nombreUsuario) {
  return requestJson('/iniciar', {
    method: 'POST',
    body: JSON.stringify({ modeloGPS, nombreUsuario })
  });
}

export function filtrarBolsines(numeroPrecinto, codigoCMDestino) {
  return requestJson('/filtrar', {
    method: 'POST',
    body: JSON.stringify({ numeroPrecinto, codigoCMDestino })
  });
}

export function seleccionarBolsin(numeroBolsin) {
  return requestJson('/seleccionar-bolsin', {
    method: 'POST',
    body: JSON.stringify({ numeroBolsin })
  });
}

export function confirmarEnvioCorreo(confirmar) {
  return requestJson('/confirmar-correo', {
    method: 'POST',
    body: JSON.stringify({ confirmar })
  });
}

export function cancelarCasoDeUso() {
  return requestJson('/cancelar', { method: 'POST' });
}
