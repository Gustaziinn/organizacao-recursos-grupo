# Matriz de rastreabilidade de requisitos

**Versão de referência:** `v1.0.0-rc2`.

A tabela relaciona os requisitos funcionais implementados aos testes de integração que fornecem evidência.

| ID | Requisito funcional | Teste | Evidência |
|---|---|---|---|
| RF-01 | Criar reserva para um recurso existente, registrando recurso, tipo, período, solicitante e estado inicial `SOLICITADA`. | `criaReservaNotificaComCorpoEsperadoERegistraCadaMudancaDeEstado` | A reserva persistida é verificada com os dados enviados. |
| RF-02 | Permitir apenas uma reserva conflitante para o mesmo recurso e período, mesmo sob concorrência. | `cemCriacoesEmParaleloResultamEmUmaReservaENoventaENoveConflitos` | 100 chamadas paralelas com 10 threads produzem 1 reserva e 99 `ConflitoReservaException`. |
| RF-03 | Impedir reservas de recursos em manutenção e rejeitar recurso inexistente. | `rejeitaRecursoEmManutencaoOuInexistente` | Verifica as exceções específicas e que nenhuma reserva é salva. |
| RF-04 | Impedir intervalos em que `fim` não seja posterior a `inicio`, inclusive por constraint do banco. | `rejeitaIntervaloInvalidoEConstraintDoBancoTambemImpedeFimAnteriorAoInicio` | Verifica validação do serviço e violação da constraint PostgreSQL. |
| RF-05 | Registrar alterações de estado da reserva na tabela de auditoria. | `criaReservaNotificaComCorpoEsperadoERegistraCadaMudancaDeEstado` | Verifica os estados anterior e novo e que uma repetição do mesmo estado não cria evento extra. |
| RF-06 | Notificar a API externa depois de criar a reserva, enviando os dados da reserva em JSON. | `criaReservaNotificaComCorpoEsperadoERegistraCadaMudancaDeEstado` | WireMock retorna 200 e verifica o corpo JSON e o cabeçalho de conteúdo. |
| RF-07 | Não manter a reserva quando a API de notificação responde com erro 500. | `contratoDeNotificacaoCom500ImpedeCriacaoDaReserva` | Verifica a exceção HTTP, a chamada ao WireMock e o rollback da reserva. |
| RF-08 | Persistir os tipos de recurso e usuário usados no fluxo de reservas. | `criaReservasParaProfessorMaterialEUsuario` | Salva professor, material e usuário e cria uma reserva associada ao professor. |
| RF-09 | Permitir que apenas Responsável ou Administrador aprove uma reserva; Solicitante recebe HTTP 403. | `solicitanteRecebe403AoTentarAprovarReserva`, `responsavelConsegueAprovarReserva`, `administradorConsegueAprovarReserva` | Requisições autenticadas com usuários persistidos verificam 403 para Solicitante e 200 para Responsável e Administrador. |

Evidências executáveis em [ReservaServiceIntegrationTest.java](../src/test/java/br/com/organizacao/ReservaServiceIntegrationTest.java). O provisionamento e o ciclo de vida de contas permanecem fora do escopo atual.
