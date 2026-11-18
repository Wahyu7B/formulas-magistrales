package com.utp.formulas;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static com.utp.formulas.dto.ApiDtos.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utp.formulas.model.Usuario;
import com.utp.formulas.repository.*;
import com.utp.formulas.service.*;
import java.util.List;
import java.math.BigDecimal;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Avance2IntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate sql;
    @Autowired UsuarioRepository usuarios;
    @Autowired PacienteRepository pacientesRepository;
    @Autowired FormulaRepository formulasRepository;
    @Autowired PacienteService pacientes;
    @Autowired FormulaService formulas;
    @Autowired PasswordEncoder passwords;
    @BeforeEach
    void preparar() {
        assertThat(sql.queryForObject("select current_database()", String.class)).isEqualTo("formulas_avance2_test");
        sql.update("delete from ingredientes");
        sql.update("delete from formulas");
        sql.update("delete from pacientes");
        sql.update("delete from usuarios");
        usuario("admin", "ADMIN");
        usuario("usuario", "USUARIO");
        usuario("quimico", "QUIMICO");
        usuario("atencion", "ATENCION");
    }
    private void usuario(String username, String rol) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setRol(rol);
        u.setPassword(passwords.encode("Clave2026!"));
        usuarios.save(u);
    }
    private String token(String username) throws Exception {
        String contenido = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(new LoginRequest(username, "Clave2026!"))))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(contenido).get("token").asText();
    }
    private PacienteRequest pacienteRequest(String dni) {
        return new PacienteRequest(dni, "Persona Demo", "Prueba", "900000001", "Dirección ficticia");
    }
    private FormulaRequest formulaRequest(String nombre, Long pacienteId) {
        return new FormulaRequest(nombre, "Envase de prueba", "Ejemplo académico", pacienteId,
            List.of(new IngredienteRequest("Base", new BigDecimal("95"), "g"), new IngredienteRequest("Componente", new BigDecimal("5"), "g")));
    }
    @Test
    void jwtRealPermiteLoginYRechazaTokenAusenteOManipulado() throws Exception {
        mvc.perform(get("/api/dashboard")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/dashboard").header("Authorization", "Bearer invalido")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/dashboard").header("Authorization", "Bearer " + token("admin")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.totalPacientes").value(0));
    }
    @Test
    void usuarioConsultaPeroNoPuedeModificarNiVerPacientes() throws Exception {
        String token = token("usuario");
        mvc.perform(get("/api/formulas").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        mvc.perform(get("/api/pacientes").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
        Long p = pacientes.guardar(null, pacienteRequest("99000101")).id();
        mvc.perform(post("/api/formulas").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(formulaRequest("Prueba", p)))).andExpect(status().isForbidden());
    }
    @Test
    void registroPublicoSiempreCreaUsuarioConPasswordCifrada() throws Exception {
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"nuevo\",\"password\":\"Clave2026!\",\"rol\":\"ADMIN\"}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.rol").value("USUARIO"));
        Usuario u = usuarios.findByUsername("nuevo").orElseThrow();
        assertThat(u.getRol()).isEqualTo("USUARIO");
        assertThat(u.getPassword()).isNotEqualTo("Clave2026!");
        assertThat(passwords.matches("Clave2026!", u.getPassword())).isTrue();
    }
    @Test
    void crudPacienteYValidacionDesdeHttp() throws Exception {
        String token = token("admin");
        var creado = mvc.perform(post("/api/pacientes").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(pacienteRequest("99000102")))).andExpect(status().isCreated()).andReturn();
        Long id = json.readTree(creado.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(put("/api/pacientes/" + id).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(new PacienteRequest("99000102", "Nombre Editado", "Prueba", "900000001", "Dirección ficticia"))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.nombres").value("Nombre Editado"));
        mvc.perform(post("/api/pacientes").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(pacienteRequest("1")))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.dni").exists());
        mvc.perform(delete("/api/pacientes/" + id).header("Authorization", "Bearer " + token)).andExpect(status().isNoContent());
        mvc.perform(get("/api/pacientes/" + id).header("Authorization", "Bearer " + token)).andExpect(status().isNotFound());
        assertThat(pacientesRepository.findById(id).orElseThrow().isActivo()).isFalse();
    }
    @Test
    void crudFormulaConIngredientesYDashboard() throws Exception {
        String token = token("admin");
        Long p = pacientes.guardar(null, pacienteRequest("99000103")).id();
        var creado = mvc.perform(post("/api/formulas").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(formulaRequest("Crema Demo", p)))).andExpect(status().isCreated()).andExpect(jsonPath("$.ingredientes.length()").value(2)).andReturn();
        Long id = json.readTree(creado.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(put("/api/formulas/" + id).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(formulaRequest("Crema Editada", p)))).andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Crema Editada"));
        assertThat(sql.queryForObject("select count(*) from ingredientes", Long.class)).isEqualTo(2);
        mvc.perform(get("/api/dashboard").header("Authorization", "Bearer " + token)).andExpect(jsonPath("$.totalPacientes").value(1)).andExpect(jsonPath("$.totalFormulas").value(1));
        mvc.perform(delete("/api/formulas/" + id).header("Authorization", "Bearer " + token)).andExpect(status().isNoContent());
        mvc.perform(get("/api/formulas").header("Authorization", "Bearer " + token)).andExpect(jsonPath("$.length()").value(0));
    }
    @Test
    void dosConsultasJpqlFiltranNombreYPaciente() {
        Long a = pacientes.guardar(null, pacienteRequest("99000104")).id();
        Long b = pacientes.guardar(null, pacienteRequest("99000105")).id();
        formulas.guardar(null, formulaRequest("Crema Alfa", a));
        formulas.guardar(null, formulaRequest("Gel Beta", b));
        assertThat(formulas.listar("CREMA", null)).extracting(FormulaResponse::nombre).containsExactly("Crema Alfa");
        assertThat(formulas.listar("", b)).extracting(FormulaResponse::nombre).containsExactly("Gel Beta");
        assertThat(formulas.listar("Crema", b)).isEmpty();
    }
    @Test
    void rollbackRealNoDejaFormulaNiIngredientesCuandoFallaElSegundoItem() {
        Long p = pacientes.guardar(null, pacienteRequest("99000106")).id();
        FormulaRequest invalida = new FormulaRequest("No debe persistir", "Envase", "Prueba de rollback", p, List.of(
            new IngredienteRequest("Válido", BigDecimal.ONE, "g"),
            new IngredienteRequest("Inválido", BigDecimal.ZERO, "g")));
        assertThatThrownBy(() -> formulas.guardar(null, invalida)).hasMessageContaining("cantidad positiva");
        assertThat(formulasRepository.count()).isZero();
        assertThat(sql.queryForObject("select count(*) from ingredientes", Long.class)).isZero();
        assertThat(pacientesRepository.count()).isEqualTo(1);
    }
    @Test
    void rollbackDeEdicionRestauraNombreYLosIngredientesBorrados() {
        Long p = pacientes.guardar(null, pacienteRequest("99000108")).id();
        Long f = formulas.guardar(null, formulaRequest("Composición original", p)).id();
        FormulaRequest invalida = new FormulaRequest("Cambio que debe revertirse", "Envase", "Prueba", p,
            List.of(new IngredienteRequest("Inválido", BigDecimal.ZERO, "g")));
        assertThatThrownBy(() -> formulas.guardar(f, invalida)).hasMessageContaining("cantidad positiva");
        FormulaResponse intacta = formulas.obtener(f);
        assertThat(intacta.nombre()).isEqualTo("Composición original");
        assertThat(intacta.ingredientes()).hasSize(2);
        assertThat(sql.queryForObject("select count(*) from ingredientes", Long.class)).isEqualTo(2);
    }
    @Test
    void duplicadosPacienteInexistenteYBajaConRelacionSeRechazan() {
        Long p = pacientes.guardar(null, pacienteRequest("99000107")).id();
        assertThatThrownBy(() -> pacientes.guardar(null, pacienteRequest("99000107"))).hasMessageContaining("ese DNI");
        assertThatThrownBy(() -> formulas.guardar(null, formulaRequest("Inválida", -1L))).hasMessageContaining("Paciente no encontrado");
        formulas.guardar(null, formulaRequest("Válida", p));
        assertThatThrownBy(() -> pacientes.eliminar(p)).hasMessageContaining("fórmulas activas");
    }
    @Test
    void quimicoGestionaFormulasPeroNoModificaPacientes() throws Exception {
        String token = token("quimico");
        Long p = pacientes.guardar(null, pacienteRequest("99000109")).id();
        mvc.perform(get("/api/pacientes").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        mvc.perform(post("/api/pacientes").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(pacienteRequest("99000110")))).andExpect(status().isForbidden());
        mvc.perform(post("/api/formulas").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(formulaRequest("Fórmula del químico", p)))).andExpect(status().isCreated());
    }
    @Test
    void atencionGestionaPacientesPeroNoModificaFormulas() throws Exception {
        String token = token("atencion");
        Long p = pacientes.guardar(null, pacienteRequest("99000111")).id();
        mvc.perform(post("/api/pacientes").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(pacienteRequest("99000112")))).andExpect(status().isCreated());
        mvc.perform(get("/api/formulas").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        mvc.perform(post("/api/formulas").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(formulaRequest("No permitida", p)))).andExpect(status().isForbidden());
    }
    @Test
    void corsPermiteSoloElFrontendConfigurado() throws Exception {
        mvc.perform(options("/api/formulas").header("Origin", "http://127.0.0.1:4202").header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "authorization,content-type"))
            .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://127.0.0.1:4202"));
        mvc.perform(options("/api/formulas").header("Origin", "http://localhost:4202").header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "authorization,content-type"))
            .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4202"));
        mvc.perform(options("/api/formulas").header("Origin", "https://origen-no-permitido.example").header("Access-Control-Request-Method", "GET"))
            .andExpect(status().isForbidden());
    }
}
