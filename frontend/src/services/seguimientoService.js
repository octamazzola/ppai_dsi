const API_BASE = '/api/seguimiento';

async function requestJson(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    cache: 'no-store',
    ...options
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    if (response.status === 401) {
      if (path === '/sesion') return null;
      if (path === '/login') throw new Error('Usuario o contraseña incorrectos.');
      window.dispatchEvent(new Event('sesion-expirada'));
      throw new Error('La sesión expiró.');
    }
    throw new Error(data.error || data.mensaje || `Error HTTP ${response.status}`);
  }
  return data;
}

export function consultarSesion() {
  return requestJson('/sesion');
}

export function iniciarSesion(nombreUsuario, password) {
  return requestJson('/login', {
    method: 'POST',
    body: JSON.stringify({ nombreUsuario, password })
  });
}

export function cerrarSesion() {
  return requestJson('/logout', { method: 'POST' });
}

export function iniciarConsulta(modeloGPS) {
  return requestJson('/iniciar', {
    method: 'POST',
    body: JSON.stringify({ modeloGPS })
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
