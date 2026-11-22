import { useEffect, useRef } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

export default function MapaBolsines({ mapa, bolsines = [], onSeleccionarBolsin }) {
  const containerRef = useRef(null);
  const mapRef = useRef(null);
  const markersLayerRef = useRef(null);
  const onSeleccionarRef = useRef(onSeleccionarBolsin);

  useEffect(() => {
    onSeleccionarRef.current = onSeleccionarBolsin;
  }, [onSeleccionarBolsin]);

  // Inicializar mapa una sola vez (compatible con React 19 StrictMode)
  useEffect(() => {
    if (!containerRef.current || mapRef.current) return;

    const initialLat = mapa?.centroLatitud ?? -31.4201;
    const initialLng = mapa?.centroLongitud ?? -64.1888;
    const initialZoom = mapa?.zoomSugerido ?? 6;

    const map = L.map(containerRef.current, {
      center: [initialLat, initialLng],
      zoom: initialZoom,
      scrollWheelZoom: true
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors',
      maxZoom: 18
    }).addTo(map);

    markersLayerRef.current = L.layerGroup().addTo(map);
    mapRef.current = map;

    return () => {
      map.remove();
      mapRef.current = null;
      markersLayerRef.current = null;
    };
  }, []);

  // Actualizar marcadores y vista cada vez que cambian los bolsines o filtros
  useEffect(() => {
    const map = mapRef.current;
    const layer = markersLayerRef.current;
    if (!map || !layer) return;

    layer.clearLayers();

    const validos = bolsines.filter(
      (b) => b.ubicacionDisponible && b.latitud != null && b.longitud != null
    );

    validos.forEach((b) => {
      const icon = L.divIcon({
        className: 'bolsin-map-pin-wrapper',
        html: `<div class="bolsin-map-pin">#${b.numeroBolsin}</div>`,
        iconSize: [54, 28],
        iconAnchor: [27, 28],
        popupAnchor: [0, -28]
      });

      const marker = L.marker([b.latitud, b.longitud], { icon }).addTo(layer);

      // Construimos el popup con nodos DOM seguros (sin innerHTML)
      const popupEl = document.createElement('div');
      popupEl.className = 'map-popup';

      const title = document.createElement('strong');
      title.textContent = `Bolsín #${b.numeroBolsin} (${b.numeroPrecinto})`;
      popupEl.appendChild(title);

      const route = document.createElement('div');
      route.textContent = `${b.codigoCMOrigen} → ${b.codigoCMDestino} (${b.nombreCMDestino})`;
      popupEl.appendChild(route);

      const coords = document.createElement('div');
      coords.className = 'map-popup-meta';
      coords.textContent = `Coords: ${b.latitud}, ${b.longitud}`;
      popupEl.appendChild(coords);

      const ts = document.createElement('div');
      ts.className = 'map-popup-meta';
      ts.textContent = `Actualización: ${b.fechaHoraUltimaActualizacion}`;
      popupEl.appendChild(ts);

      const btn = document.createElement('button');
      btn.type = 'button';
      btn.className = 'map-popup-btn';
      btn.textContent = 'Seleccionar bolsín';
      btn.addEventListener('click', () => {
        onSeleccionarRef.current?.(b.numeroBolsin);
      });
      popupEl.appendChild(btn);

      marker.bindPopup(popupEl);
    });

    if (validos.length === 1) {
      map.setView([validos[0].latitud, validos[0].longitud], mapa?.zoomSugerido ?? 12);
    } else if (validos.length > 1) {
      const bounds = L.latLngBounds(validos.map((b) => [b.latitud, b.longitud]));
      map.fitBounds(bounds, { padding: [48, 48], maxZoom: 12 });
    } else {
      map.setView(
        [mapa?.centroLatitud ?? -31.4201, mapa?.centroLongitud ?? -64.1888],
        mapa?.zoomSugerido ?? 5
      );
    }
  }, [bolsines, mapa]);

  const cantidadConSenal = bolsines.filter((b) => b.ubicacionDisponible).length;
  const cantidadSinSenal = bolsines.length - cantidadConSenal;

  return (
    <div className="map-section">
      <div className="map-toolbar">
        <span className="map-caption">
          Mapa interactivo — hacé clic en un pin para ver los datos y seleccionar el bolsín
        </span>
        <div className="map-badges">
          <span className="pill pill-ok">{cantidadConSenal} en mapa</span>
          {cantidadSinSenal > 0 && (
            <span className="pill pill-warn">{cantidadSinSenal} sin señal GPS (A3)</span>
          )}
        </div>
      </div>

      <div
        ref={containerRef}
        className="map-canvas"
        role="region"
        aria-label="Mapa de posiciones de bolsines en tránsito"
      />

      {mapa && (
        <p className="map-footnote">
          {mapa.resumenMapa} · Centro: {mapa.centroLatitud}, {mapa.centroLongitud} · Zoom: {mapa.zoomSugerido}
        </p>
      )}
    </div>
  );
}
