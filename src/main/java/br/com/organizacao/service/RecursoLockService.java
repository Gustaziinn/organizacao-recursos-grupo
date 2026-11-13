package br.com.organizacao.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.organizacao.domain.entity.Material;
import br.com.organizacao.domain.entity.Professor;
import br.com.organizacao.domain.entity.Sala;
import br.com.organizacao.domain.entity.TipoRecurso;
import br.com.organizacao.exception.RecursoEmManutencaoException;
import br.com.organizacao.exception.RecursoNaoEncontradoException;
import br.com.organizacao.repository.MaterialRepository;
import br.com.organizacao.repository.ProfessorRepository;
import br.com.organizacao.repository.SalaRepository;

@Service
public class RecursoLockService {

    private final SalaRepository salaRepository;
    private final ProfessorRepository professorRepository;
    private final MaterialRepository materialRepository;

    public RecursoLockService(
            SalaRepository salaRepository,
            ProfessorRepository professorRepository,
            MaterialRepository materialRepository) {
        this.salaRepository = salaRepository;
        this.professorRepository = professorRepository;
        this.materialRepository = materialRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void bloquearEValidar(TipoRecurso tipoRecurso, Long recursoId) {
        boolean emManutencao = switch (tipoRecurso) {
            case SALA -> salaRepository.buscarPorIdComLock(recursoId)
                    .map(Sala::isEmManutencao)
                    .orElseThrow(() -> new RecursoNaoEncontradoException(recursoId));
            case PROFESSOR -> professorRepository.buscarPorIdComLock(recursoId)
                    .map(Professor::isEmManutencao)
                    .orElseThrow(() -> new RecursoNaoEncontradoException(recursoId));
            case MATERIAL -> materialRepository.buscarPorIdComLock(recursoId)
                    .map(Material::isEmManutencao)
                    .orElseThrow(() -> new RecursoNaoEncontradoException(recursoId));
        };
        if (emManutencao) {
            throw new RecursoEmManutencaoException();
        }
    }
}
