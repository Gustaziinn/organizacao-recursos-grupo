package br.com.organizacao.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import br.com.organizacao.domain.entity.Reserva;

@Service
public class NotificacaoClient {

    private static final String RESERVA_CRIADA_PATH = "/notificacoes/reserva";

    private final RestClient restClient;

    public NotificacaoClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void notificarReservaCriada(Reserva reserva) {
        NotificacaoReservaRequest request = new NotificacaoReservaRequest(
                reserva.getId(),
                reserva.getRecursoId(),
                reserva.getTipoRecurso(),
                reserva.getInicio(),
                reserva.getFim(),
                reserva.getSolicitanteId());
        restClient.post()
                .uri(RESERVA_CRIADA_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
