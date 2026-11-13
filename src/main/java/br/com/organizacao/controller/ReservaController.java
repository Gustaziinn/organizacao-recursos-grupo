package br.com.organizacao.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.organizacao.domain.entity.Reserva;
import br.com.organizacao.service.ReservaService;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping("/{reservaId}/aprovar")
    @PreAuthorize("hasAnyRole('RESPONSAVEL', 'ADMINISTRADOR')")
    public Reserva aprovar(@PathVariable Long reservaId) {
        return reservaService.aprovarReserva(reservaId);
    }
}
