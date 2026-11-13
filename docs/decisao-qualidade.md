# Decisão de qualidade

## Decisão

**Liberar com ressalvas** a versão candidata `v1.0.0-rc2`.

## Fundamentação

- A verificação `./mvnw -B verify` passa e a cobertura atende aos limites configurados de 80% de linhas e 70% de branches.
- O teste de concorrência passa com 100 solicitações simultâneas em um `ExecutorService` de 10 threads: uma reserva é criada e 99 falham por conflito.
- A autorização por perfil está implementada para aprovar reservas: Solicitante recebe 403, enquanto Responsável e Administrador conseguem aprovar, conforme testes de integração com usuários autenticados do banco.
- A integração de notificação é exercitada com WireMock; o provedor externo real não foi integrado nem validado.

## Ressalvas para produção

1. Validar o contrato, as credenciais e a disponibilidade do provedor real de notificações.
2. Definir o provisionamento/ciclo de vida de contas e ampliar a matriz de autorização ao adicionar novas operações de negócio.
3. Confirmar a configuração e a estratégia operacional do PostgreSQL do ambiente de produção; o banco containerizado dos testes não cobre esses aspectos.

**Responsável pela decisão e acompanhamento das ressalvas:** Eros Felipe de Oliveira Rêgo.
