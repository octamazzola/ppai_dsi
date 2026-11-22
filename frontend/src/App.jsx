import { useEffect, useRef, useState } from 'react';
import {
  AlertTriangle,
  Building2,
  CheckCircle2,
  Filter,
  Mail,
  MapPin,
  Package,
  Radio,
  RotateCcw,
  Search,
  User,
  XCircle
} from 'lucide-react';
import MapaBolsines from './components/MapaBolsines';
import {
  cancelarCasoDeUso,
  cerrarSesion,
  confirmarEnvioCorreo,
  consultarSesion,
  filtrarBolsines,
  iniciarConsulta,
  iniciarSesion,
  seleccionarBolsin
} from './services/seguimientoService';

const MODELOS_GPS = [
  { id: 'gps-xtr', value: 'XTR-4500L', label: 'XTR-4500L', hint: 'JSON items · usa CM Origen' },
  { id: 'gps-nav', value: 'NavTrack QX-7A', label: 'NavTrack QX-7A', hint: 'CSV texto plano · usa CM Destino' },
  { id: 'gps-geo', value: 'GeoPulse MTR-900', label: 'GeoPulse MTR-900', hint: 'Matriz JSON · solo Nro Bolsín' }
];

export default function App() {
  const [usuario, setUsuario] = useState(null);
  const [verificando, setVerificando] = useState(true);
  const [autenticando, setAutenticando] = useState(false);
  const [errorSesion, setErrorSesion] = useState('');

  useEffect(() => {
    let vigente = true;
    function salirPorExpiracion() {
      setUsuario(null);
      setErrorSesion('La sesión expiró. Volvé a ingresar.');
    }
    window.addEventListener('sesion-expirada', salirPorExpiracion);
    consultarSesion()
      .then((sesion) => { if (vigente) setUsuario(sesion); })
      .catch(() => { if (vigente) setErrorSesion('No se pudo conectar con el backend.'); })
      .finally(() => { if (vigente) setVerificando(false); });
    return () => {
      vigente = false;
      window.removeEventListener('sesion-expirada', salirPorExpiracion);
    };
  }, []);

  async function handleLogin(event) {
    event.preventDefault();
    const datos = new FormData(event.currentTarget);
    setAutenticando(true);
    setErrorSesion('');
    try {
      setUsuario(await iniciarSesion(datos.get('nombreUsuario').trim(), datos.get('password')));
    } catch (err) {
      setErrorSesion(err.message);
    } finally {
      setAutenticando(false);
    }
  }

  async function handleLogout() {
    await cerrarSesion();
    setUsuario(null);
    setErrorSesion('');
  }

  if (usuario) {
    return <Seguimiento key={usuario.nombreUsuario} usuario={usuario} onLogout={handleLogout} />;
  }

  return (
    <main className="container login-container">
      <section className="card" aria-labelledby="login-heading">
        <div className="card-header">
          <h1 id="login-heading">Ingresar al Sistema de Bolsines</h1>
        </div>
        {verificando ? <p role="status">Verificando sesión…</p> : (
          <form className="stack" onSubmit={handleLogin}>
            <div className="field">
              <label htmlFor="login-usuario">Usuario</label>
              <input id="login-usuario" name="nombreUsuario" type="text" autoComplete="username" required />
            </div>
            <div className="field">
              <label htmlFor="login-password">Contraseña</label>
              <input id="login-password" name="password" type="password" autoComplete="current-password" required />
            </div>
            {errorSesion && <p className="banner error" role="alert">{errorSesion}</p>}
            <button type="submit" className="btn btn-primary" disabled={autenticando}>
              {autenticando ? 'Ingresando…' : 'Iniciar sesión'}
            </button>
          </form>
        )}
      </section>
    </main>
  );
}

function Seguimiento({ usuario, onLogout }) {
  const [modeloGPS, setModeloGPS] = useState('XTR-4500L');

  const [consulta, setConsulta] = useState(null);
  const [filtroPrecinto, setFiltroPrecinto] = useState('');
  const [filtroDestino, setFiltroDestino] = useState('');
  const [seleccion, setSeleccion] = useState(null);
  const [resultadoFinal, setResultadoFinal] = useState(null);

  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState('');

  const confirmDialogRef = useRef(null);
  const cancelDialogRef = useRef(null);

  async function handleLogout() {
    setCargando(true);
    setError('');
    try {
      await onLogout();
    } catch (err) {
      setError(`No se pudo cerrar la sesión. ${err.message}`);
      setCargando(false);
    }
  }

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
      const data = await iniciarConsulta(modeloGPS);
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
      setSeleccion(null);
    }
  }

  function abrirConfirmacionCancelar() {
    if (cancelDialogRef.current && !cancelDialogRef.current.open) {
      cancelDialogRef.current.showModal();
    }
  }

  async function handleCancelDialogClose() {
    const dialog = cancelDialogRef.current;
    if (!dialog) return;
    const action = dialog.returnValue;
    dialog.returnValue = '';

    if (action === 'confirmar-cancelar') {
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
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="topbar-inner">
          <div className="brand">
            <div className="brand-icon" aria-hidden="true">
              <Package size={20} />
            </div>
            <div>
              <h1>Sistema de Bolsines</h1>
              <p>CU 36 — Consultar seguimiento de bolsines en tránsito</p>
            </div>
          </div>
          <div className="actions">
            {consulta && (
              <button
                type="button"
                className="btn btn-danger-outline"
                onClick={abrirConfirmacionCancelar}
                disabled={cargando}
              >
                <XCircle size={16} aria-hidden="true" />
                Cancelar CU (A4)
              </button>
            )}
            <button type="button" className="btn btn-secondary" disabled={cargando} onClick={handleLogout}>
              Cerrar sesión
            </button>
          </div>
        </div>
      </header>

      <main className="container">
        <section className="card" aria-labelledby="inicio-heading">
          <div className="card-header">
            <h2 id="inicio-heading">1. Configuración de consulta</h2>
            <span className="step-pill">Inicio CU 36</span>
          </div>

          <p><strong>{usuario.nombreEmpleado}</strong> · {usuario.nombreCM} ({usuario.codigoCM})</p>
          <form onSubmit={handleIniciar} method="POST" aria-labelledby="inicio-heading" className="stack">

            <fieldset className="radio-group">
              <legend>Modelo de GPS Tracker (Patrón Adapter):</legend>
              <div className="radio-grid">
                {MODELOS_GPS.map((m) => (
                  <div key={m.id} className={`radio-card ${modeloGPS === m.value ? 'selected' : ''}`}>
                    <input
                      type="radio"
                      id={m.id}
                      name="modeloGPS"
                      value={m.value}
                      checked={modeloGPS === m.value}
                      onChange={(e) => setModeloGPS(e.target.value)}
                    />
                    <label htmlFor={m.id}>
                      <strong>{m.label}</strong>
                      <span className="radio-hint">{m.hint}</span>
                    </label>
                  </div>
                ))}
              </div>
            </fieldset>

            <div className="actions">
              <button type="submit" className="btn btn-primary" disabled={cargando}>
                <Search size={16} aria-hidden="true" />
                {cargando ? 'Consultando GPS...' : 'Consultar seguimiento de bolsines'}
              </button>
            </div>
          </form>
        </section>

        {error && (
          <div role="alert" className="banner error">
            <AlertTriangle size={18} aria-hidden="true" />
            <div>
              <strong>Error:</strong> {error}
            </div>
          </div>
        )}

        {consulta && (
          <section className="card" aria-labelledby="resultado-consulta-heading">
            <div className="card-header">
              <h2 id="resultado-consulta-heading">2. Seguimiento en mapa y listado de bolsines</h2>
              <span className="step-pill">{consulta.modeloGPS}</span>
            </div>

            <dl className="meta-grid">
              <div className="meta-item">
                <dt><User size={14} aria-hidden="true" /> Empleado logueado</dt>
                <dd>{consulta.empleadoLogueado} ({consulta.nombreUsuario})</dd>
              </div>
              <div className="meta-item">
                <dt><Building2 size={14} aria-hidden="true" /> Comisión Médica Origen</dt>
                <dd>{consulta.codigoCM} — {consulta.nombreCM}</dd>
              </div>
              <div className="meta-item">
                <dt><Radio size={14} aria-hidden="true" /> Proveedor GPS activo</dt>
                <dd>{consulta.modeloGPS}</dd>
              </div>
              <div className="meta-item">
                <dt><MapPin size={14} aria-hidden="true" /> Bolsines encontrados</dt>
                <dd>{consulta.bolsines.length} en estado Enviado</dd>
              </div>
            </dl>

            <div
              role="status"
              aria-live="polite"
              className={`banner ${consulta.flujoAlternativo ? 'warning' : 'info'}`}
            >
              {consulta.flujoAlternativo ? (
                <AlertTriangle size={18} aria-hidden="true" />
              ) : (
                <CheckCircle2 size={18} aria-hidden="true" />
              )}
              <div>
                {consulta.flujoAlternativo && <strong>[Flujo Alternativo {consulta.flujoAlternativo}] </strong>}
                {consulta.mensaje}
              </div>
            </div>

            {!consulta.sinBolsinesEnviados && (
              <>
                <search>
                  <form onSubmit={handleFiltrar} method="POST" aria-labelledby="filtros-heading" className="filter-form">
                    <h3 id="filtros-heading">
                      <Filter size={15} aria-hidden="true" /> Filtros de búsqueda (Paso 6 / A2)
                    </h3>
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
                        <button type="submit" className="btn btn-primary" disabled={cargando}>
                          Aplicar filtro
                        </button>
                        <button
                          type="button"
                          className="btn btn-secondary"
                          onClick={handleLimpiarFiltros}
                          disabled={cargando}
                        >
                          <RotateCcw size={15} aria-hidden="true" />
                          Limpiar
                        </button>
                      </div>
                    </div>
                  </form>
                </search>

                <MapaBolsines
                  mapa={consulta.mapa}
                  bolsines={consulta.bolsines}
                  onSeleccionarBolsin={handleSeleccionarBolsin}
                />

                <div tabIndex={0} role="region" aria-labelledby="tabla-bolsines-caption" className="table-wrapper">
                  <table className="bolsines-table">
                    <caption id="tabla-bolsines-caption">
                      Detalle de bolsines en tránsito (también podés seleccionar desde la tabla)
                    </caption>
                    <thead>
                      <tr>
                        <th scope="col">Bolsín</th>
                        <th scope="col">Precinto</th>
                        <th scope="col">CM Destino</th>
                        <th scope="col">Ubicación GPS</th>
                        <th scope="col">Última actualización</th>
                        <th scope="col">Respuesta cruda GPS</th>
                        <th scope="col">Acción</th>
                      </tr>
                    </thead>
                    <tbody>
                      {consulta.bolsines.length === 0 ? (
                        <tr>
                          <td colSpan={7} className="empty-cell">
                            No hay bolsines para mostrar con el filtro actual.
                          </td>
                        </tr>
                      ) : (
                        consulta.bolsines.map((b) => (
                          <tr key={b.numeroBolsin}>
                            <th scope="row">#{b.numeroBolsin}</th>
                            <td>{b.numeroPrecinto}</td>
                            <td>
                              <strong>{b.codigoCMDestino}</strong>
                              <div className="cell-sub">{b.nombreCMDestino}</div>
                            </td>
                            <td>
                              {b.ubicacionDisponible ? (
                                <span>
                                  {b.latitud}, {b.longitud}
                                </span>
                              ) : (
                                <span className="badge-error">A3: {b.mensajeErrorGPS}</span>
                              )}
                            </td>
                            <td>{b.ubicacionDisponible ? b.fechaHoraUltimaActualizacion : '—'}</td>
                            <td>
                              <code className="raw-payload">{b.respuestaCrudaGPS}</code>
                            </td>
                            <td>
                              <button
                                type="button"
                                className="btn btn-sm btn-primary"
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
            <div className="card-header">
              <h2>Bolsín seleccionado (#{seleccion.bolsin?.numeroBolsin})</h2>
              <span className="step-pill">Flujo {seleccion.flujoAlternativo || 'Aviso'}</span>
            </div>
            <div role="status" className="banner warning">
              <AlertTriangle size={18} aria-hidden="true" />
              <div>{seleccion.mensajeConfirmacion}</div>
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
          <h2 id="confirm-correo-title" className="dialog-title">
            <Mail size={18} aria-hidden="true" />
            Notificar ubicación al GCM destino (CU 31)
          </h2>
          {seleccion?.bolsin && (
            <div className="dialog-body">
              <p>{seleccion.mensajeConfirmacion}</p>
              <dl className="dialog-details">
                <div>
                  <dt>Bolsín</dt>
                  <dd>#{seleccion.bolsin.numeroBolsin} (Precinto {seleccion.bolsin.numeroPrecinto})</dd>
                </div>
                <div>
                  <dt>CM Destino</dt>
                  <dd>{seleccion.bolsin.codigoCMDestino} — {seleccion.bolsin.nombreCMDestino}</dd>
                </div>
                <div>
                  <dt>Coordenadas</dt>
                  <dd>{seleccion.bolsin.latitud}, {seleccion.bolsin.longitud}</dd>
                </div>
                <div>
                  <dt>Última actualización</dt>
                  <dd>{seleccion.bolsin.fechaHoraUltimaActualizacion}</dd>
                </div>
              </dl>
            </div>
          )}
          <form method="dialog" className="actions dialog-actions">
            <button value="no-enviar" className="btn btn-secondary" autoFocus>
              No enviar correo (A5)
            </button>
            <button value="confirmar" className="btn btn-primary">
              Confirmar y enviar (CU 31)
            </button>
          </form>
        </dialog>

        {/* Modal nativo <dialog> para confirmar cancelacion (Flujo Alternativo A4) */}
        <dialog
          ref={cancelDialogRef}
          id="confirm-cancelar-dialog"
          aria-labelledby="confirm-cancelar-title"
          onClose={handleCancelDialogClose}
        >
          <h2 id="confirm-cancelar-title" className="dialog-title">
            <XCircle size={18} aria-hidden="true" />
            Confirmar cancelación del caso de uso
          </h2>
          <p className="dialog-body">
            ¿Estás seguro de que querés cancelar la consulta de seguimiento en curso (Flujo Alternativo A4)?
          </p>
          <form method="dialog" className="actions dialog-actions">
            <button value="volver" className="btn btn-secondary" autoFocus>
              Continuar consulta
            </button>
            <button value="confirmar-cancelar" className="btn btn-danger">
              Sí, cancelar CU
            </button>
          </form>
        </dialog>

        {resultadoFinal && (
          <section className="card" aria-labelledby="resultado-final-heading" aria-live="polite">
            <div className="card-header">
              <h2 id="resultado-final-heading">3. Resultado del Caso de Uso</h2>
              <span className="step-pill">
                {resultadoFinal.correoEnviado ? 'CU 31 Exitoso' : `Flujo ${resultadoFinal.flujoAlternativo || 'Finalizado'}`}
              </span>
            </div>
            <div
              role="status"
              className={`banner ${resultadoFinal.correoEnviado ? 'success' : 'warning'}`}
            >
              {resultadoFinal.correoEnviado ? (
                <CheckCircle2 size={18} aria-hidden="true" />
              ) : (
                <AlertTriangle size={18} aria-hidden="true" />
              )}
              <div>
                {resultadoFinal.flujoAlternativo && <strong>[Flujo Alternativo {resultadoFinal.flujoAlternativo}] </strong>}
                {resultadoFinal.mensaje}
              </div>
            </div>

            {resultadoFinal.correoEnviado && (
              <div className="email-preview">
                <h3><Mail size={16} aria-hidden="true" /> Datos informados al Caso de Uso 31 (fuera de alcance)</h3>
                <p><strong>Destinatario GCM:</strong> {resultadoFinal.destinatarioNombre} ({resultadoFinal.destinatarioEmail})</p>
                <p><strong>CM Destino:</strong> {resultadoFinal.comisionMedicaDestino}</p>
                <p><strong>Bolsín:</strong> #{resultadoFinal.numeroBolsin}</p>
                <p><strong>Coordenadas:</strong> {resultadoFinal.latitud}, {resultadoFinal.longitud}</p>
                <p><strong>Última actualización:</strong> {resultadoFinal.fechaHoraUltimaActualizacion}</p>
              </div>
            )}
          </section>
        )}
      </main>
    </div>
  );
}
