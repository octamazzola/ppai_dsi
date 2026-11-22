import { useEffect, useRef, useState } from 'react';
import {
  cancelarCasoDeUso,
  confirmarEnvioCorreo,
  filtrarBolsines,
  iniciarConsulta,
  listarSesiones,
  seleccionarBolsin
} from './services/seguimientoService';

const MODELOS_GPS = [
  { id: 'gps-xtr', value: 'XTR-4500L', label: 'XTR-4500L (JSON items — usa CM Origen)' },
  { id: 'gps-nav', value: 'NavTrack QX-7A', label: 'NavTrack QX-7A (CSV texto plano — usa CM Destino)' },
  { id: 'gps-geo', value: 'GeoPulse MTR-900', label: 'GeoPulse MTR-900 (Matriz JSON — solo Nro Bolsín)' }
];

export default function App() {
  const [sesiones, setSesiones] = useState([]);
  const [usuarioSeleccionado, setUsuarioSeleccionado] = useState('eb_cba');
  const [modeloGPS, setModeloGPS] = useState('XTR-4500L');

  const [consulta, setConsulta] = useState(null);
  const [filtroPrecinto, setFiltroPrecinto] = useState('');
  const [filtroDestino, setFiltroDestino] = useState('');
  const [seleccion, setSeleccion] = useState(null);
  const [resultadoFinal, setResultadoFinal] = useState(null);

  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState('');

  const confirmDialogRef = useRef(null);

  useEffect(() => {
    listarSesiones()
      .then((data) => {
        setSesiones(data);
        if (data.length > 0 && !data.some((s) => s.nombreUsuario === 'eb_cba')) {
          setUsuarioSeleccionado(data[0].nombreUsuario);
        }
      })
      .catch((err) => setError(`No se pudo conectar con el backend (puerto 8081): ${err.message}`));
  }, []);

  useEffect(() => {
    const dialog = confirmDialogRef.current;
    if (!dialog) return;
    if (seleccion?.requiereConfirmacionCorreo && !dialog.open) {
      dialog.showModal();
    } else if (!seleccion?.requiereConfirmacionCorreo && dialog.open) {
      dialog.close();
    }
  }, [seleccion]);

  async function handleIniciar(e) {
    e.preventDefault();
    setError('');
    setResultadoFinal(null);
    setSeleccion(null);
    setFiltroPrecinto('');
    setFiltroDestino('');
    setCargando(true);
    try {
      const data = await iniciarConsulta(modeloGPS, usuarioSeleccionado);
      setConsulta(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setCargando(false);
    }
  }

  async function handleFiltrar(e) {
    e.preventDefault();
    setError('');
    setSeleccion(null);
    setCargando(true);
    try {
      const data = await filtrarBolsines(filtroPrecinto, filtroDestino);
      setConsulta(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setCargando(false);
    }
  }

  async function handleLimpiarFiltros() {
    setFiltroPrecinto('');
    setFiltroDestino('');
    setError('');
    setSeleccion(null);
    setCargando(true);
    try {
      const data = await filtrarBolsines('', '');
      setConsulta(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setCargando(false);
    }
  }

  async function handleSeleccionarBolsin(numeroBolsin) {
    setError('');
    setResultadoFinal(null);
    setCargando(true);
    try {
      const data = await seleccionarBolsin(numeroBolsin);
      setSeleccion(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setCargando(false);
    }
  }

  async function procesarConfirmacion(confirmar) {
    setError('');
    setCargando(true);
    try {
      const data = await confirmarEnvioCorreo(confirmar);
      setResultadoFinal(data);
      setSeleccion(null);
      setConsulta(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setCargando(false);
    }
  }

  function handleDialogClose() {
    const dialog = confirmDialogRef.current;
    if (!dialog) return;
    const action = dialog.returnValue;
    dialog.returnValue = '';

    if (action === 'confirmar') {
      procesarConfirmacion(true);
    } else if (action === 'no-enviar') {
      procesarConfirmacion(false);
    } else {
      // Cerrado con Esc sin confirmar -> mantiene la consulta abierta
      setSeleccion(null);
    }
  }

  async function handleCancelarCU() {
    setError('');
    setCargando(true);
    try {
      const data = await cancelarCasoDeUso();
      setResultadoFinal(data);
      setConsulta(null);
      setSeleccion(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setCargando(false);
    }
  }

  return (
    <main className="container">
      <header className="page-header">
        <h1>CU 36 — Consultar seguimiento de bolsines</h1>
        <p className="subtitle">
          Frontend mínimo funcional — Backend Spring Boot + JPA/H2 (puerto 8081) integrado con API GPS oficial (puerto 8080)
        </p>
      </header>

      <section className="card" aria-labelledby="inicio-heading">
        <h2 id="inicio-heading">1. Inicio del Caso de Uso (Parámetros de prueba)</h2>
        <form onSubmit={handleIniciar} method="POST" aria-labelledby="inicio-heading" className="stack">
          <div className="field">
            <label htmlFor="sesion-usuario">Sesión activa (Encargado de Bolsines):</label>
            <select
              id="sesion-usuario"
              name="nombreUsuario"
              value={usuarioSeleccionado}
              onChange={(e) => setUsuarioSeleccionado(e.target.value)}
            >
              {sesiones.map((s) => (
                <option key={s.nombreUsuario} value={s.nombreUsuario}>
                  {s.nombreEmpleado} ({s.codigoCM} — {s.nombreCM}) — {s.descripcionDemo}
                </option>
              ))}
            </select>
          </div>

          <fieldset className="radio-group">
            <legend>Modelo de GPS Tracker (Patrón Adapter):</legend>
            {MODELOS_GPS.map((m) => (
              <div key={m.id} className="radio-item">
                <input
                  type="radio"
                  id={m.id}
                  name="modeloGPS"
                  value={m.value}
                  checked={modeloGPS === m.value}
                  onChange={(e) => setModeloGPS(e.target.value)}
                />
                <label htmlFor={m.id}>{m.label}</label>
              </div>
            ))}
          </fieldset>

          <div className="actions">
            <button type="submit" disabled={cargando}>
              {cargando ? 'Consultando...' : 'Consultar seguimiento de bolsines'}
            </button>
            {consulta && (
              <button type="button" className="secondary" onClick={handleCancelarCU} disabled={cargando}>
                Cancelar CU (A4)
              </button>
            )}
          </div>
        </form>
      </section>

      {error && (
        <div role="alert" className="banner error">
          <strong>Error:</strong> {error}
        </div>
      )}

      {consulta && (
        <section className="card" aria-labelledby="resultado-consulta-heading">
          <h2 id="resultado-consulta-heading">2. Comisión Médica y Bolsines en estado Enviado</h2>
          <dl className="meta-grid">
            <div>
              <dt>Empleado logueado</dt>
              <dd>{consulta.empleadoLogueado} ({consulta.nombreUsuario})</dd>
            </div>
            <div>
              <dt>Comisión Médica Origen</dt>
              <dd>{consulta.codigoCM} — {consulta.nombreCM}</dd>
            </div>
            <div>
              <dt>Adaptador GPS utilizado</dt>
              <dd>{consulta.modeloGPS}</dd>
            </div>
            <div>
              <dt>Mapa (IGoogleMaps — Opción B)</dt>
              <dd>
                {consulta.mapa?.resumenMapa}
                {consulta.mapa && ` (Centro: ${consulta.mapa.centroLatitud}, ${consulta.mapa.centroLongitud} | Zoom: ${consulta.mapa.zoomSugerido})`}
              </dd>
            </div>
          </dl>

          <div
            role="status"
            aria-live="polite"
            className={`banner ${consulta.flujoAlternativo ? 'warning' : 'info'}`}
          >
            {consulta.flujoAlternativo && <strong>[Flujo Alternativo {consulta.flujoAlternativo}] </strong>}
            {consulta.mensaje}
          </div>

          {!consulta.sinBolsinesEnviados && (
            <>
              <search>
                <form onSubmit={handleFiltrar} method="POST" aria-labelledby="filtros-heading" className="filter-form">
                  <h3 id="filtros-heading">Filtros (Paso 6 / Flujo Alternativo A2)</h3>
                  <div className="filter-row">
                    <div className="field">
                      <label htmlFor="filtro-precinto">Número de precinto:</label>
                      <input
                        id="filtro-precinto"
                        name="numeroPrecinto"
                        type="text"
                        value={filtroPrecinto}
                        onChange={(e) => setFiltroPrecinto(e.target.value)}
                        placeholder="Ej: PRE-101 o PRE-000 (A2)"
                        autoCorrect="off"
                        autoCapitalize="none"
                        spellCheck={false}
                      />
                    </div>
                    <div className="field">
                      <label htmlFor="filtro-destino">CM Destino:</label>
                      <select
                        id="filtro-destino"
                        name="codigoCMDestino"
                        value={filtroDestino}
                        onChange={(e) => setFiltroDestino(e.target.value)}
                      >
                        <option value="">Todas las CM destino</option>
                        {(consulta.destinosDisponibles || []).map((cod) => (
                          <option key={cod} value={cod}>{cod}</option>
                        ))}
                      </select>
                    </div>
                    <div className="actions">
                      <button type="submit" disabled={cargando}>Aplicar filtro</button>
                      <button type="button" className="secondary" onClick={handleLimpiarFiltros} disabled={cargando}>
                        Limpiar
                      </button>
                    </div>
                  </div>
                </form>
              </search>

              <div tabIndex={0} role="region" aria-labelledby="tabla-bolsines-caption" className="table-wrapper">
                <table className="bolsines-table">
                  <caption id="tabla-bolsines-caption">
                    Posiciones de bolsines en tránsito (seleccione un bolsín del mapa/listado)
                  </caption>
                  <thead>
                    <tr>
                      <th scope="col">Bolsín</th>
                      <th scope="col">Precinto</th>
                      <th scope="col">CM Destino</th>
                      <th scope="col">Coordenadas (Lat, Lng)</th>
                      <th scope="col">Última actualización</th>
                      <th scope="col">Respuesta cruda API GPS</th>
                      <th scope="col">Acción</th>
                    </tr>
                  </thead>
                  <tbody>
                    {consulta.bolsines.length === 0 ? (
                      <tr>
                        <td colSpan={7}>No hay bolsines para mostrar con el filtro actual.</td>
                      </tr>
                    ) : (
                      consulta.bolsines.map((b) => (
                        <tr key={b.numeroBolsin}>
                          <th scope="row">#{b.numeroBolsin}</th>
                          <td>{b.numeroPrecinto}</td>
                          <td>{b.codigoCMDestino} ({b.nombreCMDestino})</td>
                          <td>
                            {b.ubicacionDisponible ? (
                              <>
                                {b.latitud}, {b.longitud}{' '}
                                {b.urlGoogleMaps && (
                                  <a href={b.urlGoogleMaps} target="_blank" rel="noreferrer">
                                    [Ver en Maps]
                                  </a>
                                )}
                              </>
                            ) : (
                              <span className="badge-error">A3: {b.mensajeErrorGPS}</span>
                            )}
                          </td>
                          <td>{b.ubicacionDisponible ? b.fechaHoraUltimaActualizacion : '—'}</td>
                          <td><code className="raw-payload">{b.respuestaCrudaGPS}</code></td>
                          <td>
                            <button
                              type="button"
                              onClick={() => handleSeleccionarBolsin(b.numeroBolsin)}
                              disabled={cargando}
                            >
                              Seleccionar
                            </button>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </section>
      )}

      {seleccion && !seleccion.requiereConfirmacionCorreo && (
        <section className="card" aria-live="polite">
          <h2>Bolsín seleccionado (#{seleccion.bolsin?.numeroBolsin})</h2>
          <div role="status" className="banner warning">
            <strong>[{seleccion.flujoAlternativo || 'Aviso'}]</strong> {seleccion.mensajeConfirmacion}
          </div>
        </section>
      )}

      {/* Modal nativo <dialog> para Paso 8-9 (Confirmar envio de correo al GCM destino) */}
      <dialog
        ref={confirmDialogRef}
        id="confirm-correo-dialog"
        aria-labelledby="confirm-correo-title"
        onClose={handleDialogClose}
      >
        <h2 id="confirm-correo-title">Confirmar notificación al GCM destino (CU 31)</h2>
        {seleccion?.bolsin && (
          <div className="dialog-body">
            <p>{seleccion.mensajeConfirmacion}</p>
            <ul>
              <li><strong>Bolsín:</strong> #{seleccion.bolsin.numeroBolsin} (Precinto {seleccion.bolsin.numeroPrecinto})</li>
              <li><strong>CM Destino:</strong> {seleccion.bolsin.codigoCMDestino} — {seleccion.bolsin.nombreCMDestino}</li>
              <li><strong>Coordenadas:</strong> {seleccion.bolsin.latitud}, {seleccion.bolsin.longitud}</li>
              <li><strong>Última actualización:</strong> {seleccion.bolsin.fechaHoraUltimaActualizacion}</li>
            </ul>
          </div>
        )}
        <form method="dialog" className="actions">
          <button value="no-enviar" className="secondary" autoFocus>
            No enviar correo (A5)
          </button>
          <button value="confirmar">Confirmar y enviar correo (CU 31)</button>
        </form>
      </dialog>

      {resultadoFinal && (
        <section className="card" aria-labelledby="resultado-final-heading" aria-live="polite">
          <h2 id="resultado-final-heading">3. Resultado del Caso de Uso</h2>
          <div
            role="status"
            className={`banner ${resultadoFinal.correoEnviado ? 'success' : 'warning'}`}
          >
            {resultadoFinal.flujoAlternativo && <strong>[Flujo Alternativo {resultadoFinal.flujoAlternativo}] </strong>}
            {resultadoFinal.mensaje}
          </div>

          {resultadoFinal.correoEnviado && (
            <div className="email-preview">
              <h3>Detalle de notificación simulada (CU 31)</h3>
              <p><strong>Para:</strong> {resultadoFinal.destinatarioNombre} &lt;{resultadoFinal.destinatarioEmail}&gt;</p>
              <p><strong>CM Destino:</strong> {resultadoFinal.comisionMedicaDestino}</p>
              <p><strong>Asunto:</strong> {resultadoFinal.asuntoCorreo}</p>
              <p><strong>Fecha/Hora de envío:</strong> {resultadoFinal.fechaHoraEnvio}</p>
              <pre>{resultadoFinal.cuerpoCorreo}</pre>
            </div>
          )}
        </section>
      )}
    </main>
  );
}
