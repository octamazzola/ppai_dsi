package ar.edu.utn.frc.dsi.bolsines;

import ar.edu.utn.frc.dsi.bolsines.boundary.InterfazGPSTracker;
import ar.edu.utn.frc.dsi.bolsines.config.GpsApiConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SeguimientoSesionTests {

    private static final String API = "/api/seguimiento";

    @Autowired
    private MockMvc mvc;

    @TestConfiguration
    static class ConfiguracionGps {
        @Bean
        @Primary
        InterfazGPSTracker gpsSimulado(GpsApiConfig config) {
            return new InterfazGPSTracker(config) {
                @Override
                public String[][] obtenerUbicacionBolsin(int[] numeros, String origen, String[] destinos, String modelo) {
                    return Arrays.stream(numeros).mapToObj(numero -> new String[]{
                            String.valueOf(numero), "-31.42", "-64.18", "2026-03-07T12:30:00", "OK", "", ""
                    }).toArray(String[][]::new);
                }
            };
        }
    }

    @Test
    void requiereCredencialesValidasParaAccederAlSeguimiento() throws Exception {
        mvc.perform(post(API + "/login").contentType(APPLICATION_JSON)
                        .content("""
                                {"nombreUsuario":"usuario1","password":"incorrecta"}
                                """))
                .andExpect(status().isUnauthorized());

        mvc.perform(get(API + "/sesion")).andExpect(status().isUnauthorized());
        for (String endpoint : new String[]{"/iniciar", "/filtrar", "/seleccionar-bolsin", "/confirmar-correo", "/cancelar"}) {
            mvc.perform(post(API + endpoint).contentType(APPLICATION_JSON).content("{}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void cadaSesionSoloPuedeConsultarYSeleccionarSusBolsines() throws Exception {
        MockHttpSession cordoba = login("usuario1", "usu123");
        MockHttpSession rosario = login("usuario2", "1234");

        mvc.perform(post(API + "/iniciar").session(cordoba).param("empleadoId", "2")
                        .contentType(APPLICATION_JSON).content("""
                                {"nombreUsuario":"usuario2"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value("usuario1"))
                .andExpect(jsonPath("$.bolsines[*].numeroBolsin", containsInAnyOrder(101, 999)));

        mvc.perform(post(API + "/iniciar").session(rosario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bolsines[*].numeroBolsin", containsInAnyOrder(102)));

        mvc.perform(post(API + "/filtrar").session(cordoba)
                        .contentType(APPLICATION_JSON).content("""
                                {"numeroPrecinto":"PRE-102"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bolsines").isEmpty());
        mvc.perform(post(API + "/seleccionar-bolsin").session(cordoba)
                        .contentType(APPLICATION_JSON).content("""
                                {"numeroBolsin":102}
                                """))
                .andExpect(status().isBadRequest());
        mvc.perform(post(API + "/seleccionar-bolsin").session(rosario)
                        .contentType(APPLICATION_JSON).content("""
                                {"numeroBolsin":101}
                                """))
                .andExpect(status().isBadRequest());
        mvc.perform(post(API + "/seleccionar-bolsin").session(cordoba)
                        .contentType(APPLICATION_JSON).content("""
                                {"numeroBolsin":101}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bolsin.numeroBolsin").value(101));
        mvc.perform(post(API + "/confirmar-correo").session(rosario)
                        .contentType(APPLICATION_JSON).content("""
                                {"confirmar":true}
                                """))
                .andExpect(status().isBadRequest());

        mvc.perform(post(API + "/logout").session(cordoba)).andExpect(status().isNoContent());
        assertTrue(cordoba.isInvalid());
        mvc.perform(get(API + "/sesion").session(rosario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value("usuario2"));
    }

    private MockHttpSession login(String nombreUsuario, String password) throws Exception {
        return (MockHttpSession) mvc.perform(post(API + "/login").contentType(APPLICATION_JSON)
                        .content("""
                                {"nombreUsuario":"%s","password":"%s"}
                                """.formatted(nombreUsuario, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value(nombreUsuario))
                .andReturn().getRequest().getSession(false);
    }
}
