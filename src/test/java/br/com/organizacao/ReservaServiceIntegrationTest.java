package br.com.organizacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpServerErrorException;

import br.com.organizacao.domain.entity.Auditoria;
import br.com.organizacao.domain.entity.EstadoReserva;
import br.com.organizacao.domain.entity.Material;
import br.com.organizacao.domain.entity.PerfilUsuario;
import br.com.organizacao.domain.entity.Professor;
import br.com.organizacao.domain.entity.Reserva;
import br.com.organizacao.domain.entity.Sala;
import br.com.organizacao.domain.entity.TipoRecurso;
import br.com.organizacao.domain.entity.Usuario;
import br.com.organizacao.exception.ConflitoReservaException;
import br.com.organizacao.exception.RecursoEmManutencaoException;
import br.com.organizacao.exception.RecursoNaoEncontradoException;
import br.com.organizacao.repository.AuditoriaRepository;
import br.com.organizacao.repository.MaterialRepository;
import br.com.organizacao.repository.ProfessorRepository;
import br.com.organizacao.repository.ReservaRepository;
import br.com.organizacao.repository.SalaRepository;
import br.com.organizacao.repository.UsuarioRepository;
import br.com.organizacao.service.NotificacaoReservaRequest;
import br.com.organizacao.service.ReservaService;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ReservaServiceIntegrationTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 12, 10, 0);
    private static final LocalDateTime FIM = LocalDateTime.of(2026, 11, 12, 11, 0);
    private static final String NOTIFICACAO_PATH = "/notificacoes/reserva";
    private static final String SENHA_TESTE = "senha-de-integracao";

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

    private static final WireMockServer WIREMOCK = iniciarWireMock();

    @DynamicPropertySource
    static void configurarPropriedades(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("integracao.notificacao.base-url", WIREMOCK::baseUrl);
    }

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private SalaRepository salaRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void limparDados() {
        WIREMOCK.resetAll();
        WIREMOCK.stubFor(post(urlEqualTo(NOTIFICACAO_PATH)).willReturn(aResponse().withStatus(200)));
        auditoriaRepository.deleteAll();
        reservaRepository.deleteAll();
        salaRepository.deleteAll();
        professorRepository.deleteAll();
        materialRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @AfterAll
    static void pararWireMock() {
        WIREMOCK.stop();
    }

    @Test
    void criaReservaNotificaComCorpoEsperadoERegistraCadaMudancaDeEstado() throws Exception {
        Sala sala = salaRepository.saveAndFlush(new Sala("Sala 1", false));
        Reserva reserva = reservaService.criarReserva(
                sala.getId(), TipoRecurso.SALA, INICIO, FIM, 42L);

        assertEquals(EstadoReserva.SOLICITADA, reserva.getEstado());
        assertEquals(sala.getId(), reserva.getRecursoId());
        assertEquals(TipoRecurso.SALA, reserva.getTipoRecurso());
        assertEquals(INICIO, reserva.getInicio());
        assertEquals(FIM, reserva.getFim());
        assertEquals(42L, reserva.getSolicitanteId());
        String corpoEsperado = objectMapper.writeValueAsString(new NotificacaoReservaRequest(
                reserva.getId(),
                sala.getId(),
                TipoRecurso.SALA,
                INICIO,
                FIM,
                42L));
        WIREMOCK.verify(1, postRequestedFor(urlEqualTo(NOTIFICACAO_PATH))
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(equalToJson(corpoEsperado)));

        reservaService.alterarEstado(reserva.getId(), EstadoReserva.APROVADA);
        reservaService.alterarEstado(reserva.getId(), EstadoReserva.APROVADA);
        reservaService.alterarEstado(reserva.getId(), EstadoReserva.EM_USO);

        List<Auditoria> historico = auditoriaRepository
                .findAllByReservaIdOrderByAlteradoEmAsc(reserva.getId());
        assertEquals(2, historico.size());
        assertEquals(EstadoReserva.SOLICITADA, historico.get(0).getEstadoAnterior());
        assertEquals(EstadoReserva.APROVADA, historico.get(0).getEstadoNovo());
        assertEquals(EstadoReserva.APROVADA, historico.get(1).getEstadoAnterior());
        assertEquals(EstadoReserva.EM_USO, historico.get(1).getEstadoNovo());
        assertTrue(historico.get(0).getAlteradoEm() != null);
    }

    @Test
    void cemCriacoesEmParaleloResultamEmUmaReservaENoventaENoveConflitos() throws Exception {
        Sala sala = salaRepository.saveAndFlush(new Sala("Sala concorrente", false));
        CountDownLatch prontos = new CountDownLatch(10);
        CountDownLatch iniciar = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        try {
            List<Future<Reserva>> tentativas = java.util.stream.IntStream.range(0, 100)
                    .mapToObj(indice -> executor.submit(() ->
                            criarReservaSimultaneamente(sala.getId(), indice, prontos, iniciar)))
                    .toList();

            assertTrue(prontos.await(10, TimeUnit.SECONDS));
            iniciar.countDown();
            int criadas = 0;
            int conflitos = 0;
            for (Future<Reserva> tentativa : tentativas) {
                try {
                    tentativa.get(60, TimeUnit.SECONDS);
                    criadas++;
                } catch (ExecutionException exception) {
                    assertTrue(exception.getCause() instanceof ConflitoReservaException);
                    conflitos++;
                }
            }

            assertEquals(1, criadas);
            assertEquals(99, conflitos);
            assertEquals(1, reservaRepository
                    .findAllByRecursoIdAndTipoRecurso(sala.getId(), TipoRecurso.SALA).size());
            WIREMOCK.verify(1, postRequestedFor(urlEqualTo(NOTIFICACAO_PATH)));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void contratoDeNotificacaoCom500ImpedeCriacaoDaReserva() {
        Sala sala = salaRepository.saveAndFlush(new Sala("Sala notificação", false));
        WIREMOCK.resetAll();
        WIREMOCK.stubFor(post(urlEqualTo(NOTIFICACAO_PATH)).willReturn(aResponse().withStatus(500)));

        assertThrows(HttpServerErrorException.class, () -> reservaService.criarReserva(
                sala.getId(), TipoRecurso.SALA, INICIO, FIM, 24L));

        assertTrue(reservaRepository
                .findAllByRecursoIdAndTipoRecurso(sala.getId(), TipoRecurso.SALA).isEmpty());
        WIREMOCK.verify(1, postRequestedFor(urlEqualTo(NOTIFICACAO_PATH)));
    }

    @Test
    void solicitanteRecebe403AoTentarAprovarReserva() throws Exception {
        Usuario solicitante = criarUsuario("Solicitante", "solicitante@example.com", PerfilUsuario.SOLICITANTE);
        Reserva reserva = criarReservaParaSolicitante(solicitante);

        mockMvc.perform(post("/api/reservas/{reservaId}/aprovar", reserva.getId())
                        .with(httpBasic(solicitante.getEmail(), SENHA_TESTE))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertEquals(EstadoReserva.SOLICITADA,
                reservaRepository.findById(reserva.getId()).orElseThrow().getEstado());
        assertTrue(auditoriaRepository.findAllByReservaIdOrderByAlteradoEmAsc(reserva.getId()).isEmpty());
    }

    @Test
    void responsavelConsegueAprovarReserva() throws Exception {
        Usuario responsavel = criarUsuario("Responsavel", "responsavel@example.com", PerfilUsuario.RESPONSAVEL);
        Reserva reserva = criarReservaParaSolicitante(responsavel);

        mockMvc.perform(post("/api/reservas/{reservaId}/aprovar", reserva.getId())
                        .with(httpBasic(responsavel.getEmail(), SENHA_TESTE))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(EstadoReserva.APROVADA.name()));

        assertEquals(EstadoReserva.APROVADA,
                reservaRepository.findById(reserva.getId()).orElseThrow().getEstado());
        assertEquals(1, auditoriaRepository.findAllByReservaIdOrderByAlteradoEmAsc(reserva.getId()).size());
    }

    @Test
    void administradorConsegueAprovarReserva() throws Exception {
        Usuario administrador = criarUsuario(
                "Administrador", "administrador@example.com", PerfilUsuario.ADMINISTRADOR);
        Reserva reserva = criarReservaParaSolicitante(administrador);

        mockMvc.perform(post("/api/reservas/{reservaId}/aprovar", reserva.getId())
                        .with(httpBasic(administrador.getEmail(), SENHA_TESTE))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(EstadoReserva.APROVADA.name()));
    }

    @Test
    void criaReservasParaProfessorMaterialEUsuario() {
        Professor professor = professorRepository.saveAndFlush(new Professor("Professor 1", false));
        Material material = materialRepository.saveAndFlush(new Material("Projetor", false));
        Usuario usuario = criarUsuario("Ana", "ana@example.com", PerfilUsuario.SOLICITANTE);

        assertEquals("Professor 1", professor.getNome());
        assertFalse(professor.isEmManutencao());
        professor.setEmManutencao(true);
        assertTrue(professor.isEmManutencao());
        assertEquals("Projetor", material.getNome());
        assertFalse(material.isEmManutencao());
        material.setEmManutencao(true);
        assertTrue(material.isEmManutencao());
        assertEquals("Ana", usuario.getNome());
        assertEquals("ana@example.com", usuario.getEmail());

        Reserva reserva = reservaService.criarReserva(
                professor.getId(), TipoRecurso.PROFESSOR, INICIO, FIM, usuario.getId());
        assertEquals(EstadoReserva.SOLICITADA, reserva.getEstado());
    }

    @Test
    void rejeitaRecursoEmManutencaoOuInexistente() {
        Material material = materialRepository.saveAndFlush(new Material("Material indisponível", true));

        assertThrows(RecursoEmManutencaoException.class, () -> reservaService.criarReserva(
                material.getId(), TipoRecurso.MATERIAL, INICIO, FIM, 7L));
        assertThrows(RecursoNaoEncontradoException.class, () -> reservaService.criarReserva(
                999L, TipoRecurso.MATERIAL, INICIO, FIM, 7L));
        assertTrue(reservaRepository.findAll().isEmpty());
    }

    @Test
    void rejeitaIntervaloInvalidoEConstraintDoBancoTambemImpedeFimAnteriorAoInicio() {
        Sala sala = salaRepository.saveAndFlush(new Sala("Sala de teste", false));

        assertThrows(IllegalArgumentException.class, () -> reservaService.criarReserva(
                sala.getId(), TipoRecurso.SALA, FIM, INICIO, 8L));
        assertThrows(DataIntegrityViolationException.class, () -> reservaRepository.saveAndFlush(
                new Reserva(
                        sala.getId(),
                        TipoRecurso.SALA,
                        FIM,
                        INICIO,
                        EstadoReserva.SOLICITADA,
                        8L)));
    }

    private Reserva criarReservaSimultaneamente(
            Long salaId, int solicitanteId, CountDownLatch prontos, CountDownLatch iniciar)
            throws InterruptedException {
        prontos.countDown();
        if (!iniciar.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Tempo esgotado aguardando início concorrente");
        }
        return reservaService.criarReserva(
                salaId, TipoRecurso.SALA, INICIO, FIM, (long) solicitanteId);
    }

    private Usuario criarUsuario(String nome, String email, PerfilUsuario perfil) {
        return usuarioRepository.saveAndFlush(new Usuario(
                nome, email, passwordEncoder.encode(SENHA_TESTE), perfil));
    }

    private Reserva criarReservaParaSolicitante(Usuario solicitante) {
        Sala sala = salaRepository.saveAndFlush(new Sala("Sala para aprovação", false));
        return reservaService.criarReserva(
                sala.getId(), TipoRecurso.SALA, INICIO, FIM, solicitante.getId());
    }

    private static WireMockServer iniciarWireMock() {
        WireMockServer server = new WireMockServer(wireMockConfig().dynamicPort());
        server.start();
        return server;
    }
}
