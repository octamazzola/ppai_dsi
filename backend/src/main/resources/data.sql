-- Provincias
INSERT INTO provincia (id, nombre, descripcion) VALUES (1, 'Córdoba', 'Provincia de Córdoba');
INSERT INTO provincia (id, nombre, descripcion) VALUES (2, 'Santa Fe', 'Provincia de Santa Fe');
INSERT INTO provincia (id, nombre, descripcion) VALUES (3, 'Mendoza', 'Provincia de Mendoza');
INSERT INTO provincia (id, nombre, descripcion) VALUES (4, 'C.A.B.A.', 'Ciudad Autónoma de Buenos Aires');
INSERT INTO provincia (id, nombre, descripcion) VALUES (5, 'Jujuy', 'Provincia de Jujuy');

-- Localidades
INSERT INTO localidad (id, nombre, descripcion, provincia_id) VALUES (1, 'Córdoba Capital', 'Capital provincial', 1);
INSERT INTO localidad (id, nombre, descripcion, provincia_id) VALUES (2, 'Rosario', 'Ciudad de Rosario', 2);
INSERT INTO localidad (id, nombre, descripcion, provincia_id) VALUES (3, 'Mendoza Capital', 'Capital provincial', 3);
INSERT INTO localidad (id, nombre, descripcion, provincia_id) VALUES (4, 'C.A.B.A.', 'Sede Central', 4);
INSERT INTO localidad (id, nombre, descripcion, provincia_id) VALUES (5, 'San Salvador de Jujuy', 'Capital provincial', 5);

-- Comisiones Medicas (codigos sincronizados exactamente con LiberaeMentis/dsi-api-bolsines)
INSERT INTO comision_medica (codigo, nombre, direccion, email, telefono, localidad_id)
VALUES ('CM-CBA-01', 'Comisión Médica Córdoba 01', 'Av. Colón 650, Córdoba', 'cm.cba01@organismo.gob.ar', '0351-4210001', 1);
INSERT INTO comision_medica (codigo, nombre, direccion, email, telefono, localidad_id)
VALUES ('CM-CBA-02', 'Comisión Médica Córdoba 02', 'Bv. Chacabuco 180, Córdoba', 'cm.cba02@organismo.gob.ar', '0351-4210002', 1);
INSERT INTO comision_medica (codigo, nombre, direccion, email, telefono, localidad_id)
VALUES ('CM-ROS-02', 'Comisión Médica Rosario 02', 'Av. Pellegrini 1240, Rosario', 'cm.ros02@organismo.gob.ar', '0341-4250002', 2);
INSERT INTO comision_medica (codigo, nombre, direccion, email, telefono, localidad_id)
VALUES ('CM-MDZ-03', 'Comisión Médica Mendoza 03', 'Av. San Martín 920, Mendoza', 'cm.mdz03@organismo.gob.ar', '0261-4290003', 3);
INSERT INTO comision_medica (codigo, nombre, direccion, email, telefono, localidad_id)
VALUES ('CM-BA-01', 'Comisión Médica Central (C.A.B.A.)', 'Bartolomé Mitre 751, C.A.B.A.', 'cmc.ba01@organismo.gob.ar', '011-43210001', 4);
INSERT INTO comision_medica (codigo, nombre, direccion, email, telefono, localidad_id)
VALUES ('CM-JUJ-01', 'Comisión Médica Jujuy 01', 'Belgrano 450, S. S. de Jujuy', 'cm.juj01@organismo.gob.ar', '0388-4220001', 5);

-- Roles
INSERT INTO rol (id, nombre, descripcion) VALUES (1, 'EB', 'Encargado de Bolsines');
INSERT INTO rol (id, nombre, descripcion) VALUES (2, 'GCM', 'Gerente de Comisión Médica');

-- Usuarios
INSERT INTO usuario (nombre_usuario, password) VALUES ('usuario1', 'usu123');
INSERT INTO usuario (nombre_usuario, password) VALUES ('usuario2', '1234');
INSERT INTO usuario (nombre_usuario, password) VALUES ('eb_mdz', '1234');
INSERT INTO usuario (nombre_usuario, password) VALUES ('eb_juj', '1234');
INSERT INTO usuario (nombre_usuario, password) VALUES ('gcm_cba', '1234');
INSERT INTO usuario (nombre_usuario, password) VALUES ('gcm_ros', '1234');
INSERT INTO usuario (nombre_usuario, password) VALUES ('gcm_mdz', '1234');
INSERT INTO usuario (nombre_usuario, password) VALUES ('gcm_ba', '1234');

-- Empleados (EBs de distintas CM y un GCM por cada CM destino)
INSERT INTO empleado (id, nombre, apellido, email, rol_id, comision_medica_codigo, nombre_usuario)
VALUES (1, 'Octavio', 'Mazzola', 'octavio.mazzola@organismo.gob.ar', 1, 'CM-CBA-01', 'usuario1');
INSERT INTO empleado (id, nombre, apellido, email, rol_id, comision_medica_codigo, nombre_usuario)
VALUES (2, 'Rocío', 'Bruno', 'rocio.bruno@organismo.gob.ar', 1, 'CM-ROS-02', 'usuario2');
INSERT INTO empleado (id, nombre, apellido, email, rol_id, comision_medica_codigo, nombre_usuario)
VALUES (3, 'Alejandro', 'Medina', 'alejandro.medina@organismo.gob.ar', 1, 'CM-MDZ-03', 'eb_mdz');
INSERT INTO empleado (id, nombre, apellido, email, rol_id, comision_medica_codigo, nombre_usuario)
VALUES (4, 'Francisco', 'Andrade', 'francisco.andrade@organismo.gob.ar', 1, 'CM-JUJ-01', 'eb_juj');

-- Gerentes de Comision Medica (GCM destino para CU 31)
INSERT INTO empleado (id, nombre, apellido, email, rol_id, comision_medica_codigo, nombre_usuario)
VALUES (10, 'Carlos', 'López', 'gcm.cordoba01@organismo.gob.ar', 2, 'CM-CBA-01', 'gcm_cba');
INSERT INTO empleado (id, nombre, apellido, email, rol_id, comision_medica_codigo, nombre_usuario)
VALUES (11, 'Laura', 'Gómez', 'gcm.rosario02@organismo.gob.ar', 2, 'CM-ROS-02', 'gcm_ros');
INSERT INTO empleado (id, nombre, apellido, email, rol_id, comision_medica_codigo, nombre_usuario)
VALUES (12, 'Martín', 'Pérez', 'gcm.mendoza03@organismo.gob.ar', 2, 'CM-MDZ-03', 'gcm_mdz');
INSERT INTO empleado (id, nombre, apellido, email, rol_id, comision_medica_codigo, nombre_usuario)
VALUES (13, 'Sofía', 'Fernández', 'gcm.central@organismo.gob.ar', 2, 'CM-BA-01', 'gcm_ba');

-- Sesion actual por defecto: usuario1 (CM-CBA-01)
INSERT INTO sesion (id, fecha_hora_inicio, fecha_hora_fin, nombre_usuario)
VALUES (1, '2026-11-22T08:00:00', NULL, 'usuario1');
INSERT INTO sesion (id, fecha_hora_inicio, fecha_hora_fin, nombre_usuario)
VALUES (2, '2026-11-21T08:00:00', '2026-11-21T18:00:00', 'usuario2');

-- Estados (respetando Maquina de Estados de Bolsin de Entrega 1 + 1 estado de otro ambito)
INSERT INTO estado (id, ambito, nombre, descripcion) VALUES (1, 'Bolsin', 'Creado', 'Bolsín recién generado en CM origen');
INSERT INTO estado (id, ambito, nombre, descripcion) VALUES (2, 'Bolsin', 'Cerrado', 'Bolsín cerrado con precinto listo para retiro');
INSERT INTO estado (id, ambito, nombre, descripcion) VALUES (3, 'Bolsin', 'Enviado', 'Bolsín retirado por correo y en tránsito');
INSERT INTO estado (id, ambito, nombre, descripcion) VALUES (4, 'Bolsin', 'Recibido', 'Bolsín recibido en CM destino');
INSERT INTO estado (id, ambito, nombre, descripcion) VALUES (5, 'Bolsin', 'DeBaja', 'Bolsín dado de baja antes del envío');
INSERT INTO estado (id, ambito, nombre, descripcion) VALUES (6, 'Remito', 'Enviado', 'Estado Enviado de ámbito Remito (control de ámbito)');

-- Bolsines (numeros 101, 102, 103, 110 sincronizados con dsi-api-bolsines)
-- 101: CM-CBA-01 -> CM-ROS-02 | Enviado (Flujo Basico OK en los 3 modelos GPS)
INSERT INTO bolsin (numero_bolsin, fecha, numero_precinto, peso, cm_origen_codigo, cm_destino_codigo)
VALUES (101, '2026-03-07', 'PRE-101', 1450.0, 'CM-CBA-01', 'CM-ROS-02');

-- 999: CM-CBA-01 -> CM-MDZ-03 | Enviado (NO existe en dsi-api-bolsines -> prueba Flujo Alternativo A3)
INSERT INTO bolsin (numero_bolsin, fecha, numero_precinto, peso, cm_origen_codigo, cm_destino_codigo)
VALUES (999, '2026-03-07', 'PRE-999', 980.0, 'CM-CBA-01', 'CM-MDZ-03');

-- 120: CM-CBA-01 -> CM-BA-01 | Cerrado (NO debe aparecer -> prueba sosEnviado())
INSERT INTO bolsin (numero_bolsin, fecha, numero_precinto, peso, cm_origen_codigo, cm_destino_codigo)
VALUES (120, '2026-03-07', 'PRE-120', 1100.0, 'CM-CBA-01', 'CM-BA-01');

-- 121: CM-CBA-01 -> CM-ROS-02 | Recibido con historial Enviado previo (NO debe aparecer -> prueba sosActual())
INSERT INTO bolsin (numero_bolsin, fecha, numero_precinto, peso, cm_origen_codigo, cm_destino_codigo)
VALUES (121, '2026-03-05', 'PRE-121', 850.0, 'CM-CBA-01', 'CM-ROS-02');

-- 102: CM-ROS-02 -> CM-MDZ-03 | Enviado (NO aparece para usuario1, SI para usuario2; OK en API GPS)
INSERT INTO bolsin (numero_bolsin, fecha, numero_precinto, peso, cm_origen_codigo, cm_destino_codigo)
VALUES (102, '2026-03-07', 'PRE-102', 1600.0, 'CM-ROS-02', 'CM-MDZ-03');

-- 103: CM-MDZ-03 -> CM-BA-01 | Enviado (OK para eb_mdz en API GPS)
INSERT INTO bolsin (numero_bolsin, fecha, numero_precinto, peso, cm_origen_codigo, cm_destino_codigo)
VALUES (103, '2026-03-07', 'PRE-103', 1250.0, 'CM-MDZ-03', 'CM-BA-01');

-- 110: CM-CBA-02 -> CM-ROS-02 | Enviado (otra CM origen)
INSERT INTO bolsin (numero_bolsin, fecha, numero_precinto, peso, cm_origen_codigo, cm_destino_codigo)
VALUES (110, '2026-03-07', 'PRE-110', 1320.0, 'CM-CBA-02', 'CM-ROS-02');

-- Historial de CambioEstadoBolsin
-- Bolsin 101: Creado -> Cerrado -> Enviado (actual)
INSERT INTO cambio_estado_bolsin (id, fecha_hora_inicio, fecha_hora_fin, estado_id, responsable_ce_id, bolsin_numero)
VALUES (1, '2026-03-06T09:00:00', '2026-03-06T16:00:00', 1, 1, 101);
INSERT INTO cambio_estado_bolsin (id, fecha_hora_inicio, fecha_hora_fin, estado_id, responsable_ce_id, bolsin_numero)
VALUES (2, '2026-03-06T16:00:00', '2026-03-07T08:00:00', 2, 1, 101);
INSERT INTO cambio_estado_bolsin (id, fecha_hora_inicio, fecha_hora_fin, estado_id, responsable_ce_id, bolsin_numero)
VALUES (3, '2026-03-07T08:00:00', NULL, 3, 1, 101);

-- Bolsin 999: Creado -> Cerrado -> Enviado (actual, sin señal en GPS API -> A3)
INSERT INTO cambio_estado_bolsin (id, fecha_hora_inicio, fecha_hora_fin, estado_id, responsable_ce_id, bolsin_numero)
VALUES (4, '2026-03-06T10:00:00', '2026-03-06T17:00:00', 1, 1, 999);
INSERT INTO cambio_estado_bolsin (id, fecha_hora_inicio, fecha_hora_fin, estado_id, responsable_ce_id, bolsin_numero)
VALUES (5, '2026-03-06T17:00:00', '2026-03-07T08:15:00', 2, 1, 999);
INSERT INTO cambio_estado_bolsin (id, fecha_hora_inicio, fecha_hora_fin, estado_id, responsable_ce_id, bolsin_numero)
VALUES (6, '2026-03-07T08:15:00', NULL, 3, 1, 999);

-- Bolsin 120: Creado -> Cerrado (actual, NO Enviado)
INSERT INTO cambio_estado_bolsin (id, fecha_hora_inicio, fecha_hora_fin, estado_id, responsable_ce_id, bolsin_numero)
VALUES (7, '2026-03-07T09:00:00', '2026-03-07T11:00:00', 1, 1, 120);
INSERT INTO cambio_estado_bolsin (id, fecha_hora_inicio, fecha_hora_fin, estado_id, responsable_ce_id, bolsin_numero)
VALUES (8, '2026-03-07T11:00:00', NULL, 2, 1, 120);

-- Bolsin 121: Creado -> Cerrado -> Enviado (pasado) -> Recibido (actual)
INSERT INTO cambio_estado_bolsin (id, fecha_hora_inicio, fecha_hora_fin, estado_id, responsable_ce_id, bolsin_numero)
VALUES (9, '2026-03-05T09:00:00', '2026-03-05T14:00:00', 1, 1, 121);
INSERT INTO cambio_estado_bolsin (id, fecha_hora_inicio, fecha_hora_fin, estado_id, responsable_ce_id, bolsin_numero)
VALUES (10, '2026-03-05T14:00:00', '2026-03-06T08:00:00', 2, 1, 121);
INSERT INTO cambio_estado_bolsin (id, fecha_hora_inicio, fecha_hora_fin, estado_id, responsable_ce_id, bolsin_numero)
VALUES (11, '2026-03-06T08:00:00', '2026-03-06T18:00:00', 3, 1, 121);
INSERT INTO cambio_estado_bolsin (id, fecha_hora_inicio, fecha_hora_fin, estado_id, responsable_ce_id, bolsin_numero)
VALUES (12, '2026-03-06T18:00:00', NULL, 4, 2, 121);

-- Bolsin 102: Enviado (actual, origen CM-ROS-02)
INSERT INTO cambio_estado_bolsin (id, fecha_hora_inicio, fecha_hora_fin, estado_id, responsable_ce_id, bolsin_numero)
VALUES (13, '2026-03-07T08:00:00', NULL, 3, 2, 102);

-- Bolsin 103: Enviado (actual, origen CM-MDZ-03)
INSERT INTO cambio_estado_bolsin (id, fecha_hora_inicio, fecha_hora_fin, estado_id, responsable_ce_id, bolsin_numero)
VALUES (14, '2026-03-07T08:30:00', NULL, 3, 3, 103);

-- Bolsin 110: Enviado (actual, origen CM-CBA-02)
INSERT INTO cambio_estado_bolsin (id, fecha_hora_inicio, fecha_hora_fin, estado_id, responsable_ce_id, bolsin_numero)
VALUES (15, '2026-03-07T08:45:00', NULL, 3, 1, 110);
