# Relatório de qualidade

## Escopo e responsável

Avaliação da versão candidata `v1.0.0-rc2`, com base na implementação atual e nos testes executados por `./mvnw -B verify`.

**Responsável pelos riscos aceitos:** Eros Felipe de Oliveira Rêgo.

## Evidências

- A verificação Maven passa com os limites JaCoCo configurados: pelo menos 80% das linhas e 70% dos branches.
- O teste de concorrência envia 100 solicitações com 10 threads e comprova uma reserva criada e 99 conflitos.
- Os testes de integração usam PostgreSQL via Testcontainers e WireMock para as respostas da API de notificação.
- A resposta 500 do WireMock é propagada e causa rollback da reserva.

## Riscos aceitos

| Risco | Aceite e impacto | Responsável |
|---|---|---|
| A API externa de notificação é simulada pelo WireMock. | O teste valida o contrato HTTP básico e o tratamento de 500, mas não confirma credenciais, disponibilidade, latência ou compatibilidade com o provedor real. Notificações em produção dependem de configurar e validar esse provedor. | Richard Arthur |
| O PostgreSQL usado nos testes é efêmero e executado em container. | O teste valida comportamento transacional e concorrência em PostgreSQL, mas não cobre configuração, migrações, backup, alta disponibilidade ou desempenho do banco de produção. | Eros Felipe de Oliveira Rêgo |
| A autorização por perfil cobre a aprovação de reservas, mas não há ciclo de vida de contas nem matriz de permissões para outras operações. | `SOLICITANTE`, `RESPONSAVEL` e `ADMINISTRADOR` são carregados do usuário persistido; aprovação é restrita a Responsável e Administrador e tem testes de 403/200. O provisionamento seguro de usuários e as regras de futuras operações ainda precisam ser definidos. | Eros Felipe de Oliveira Rêgo |
| A classe bootstrap da aplicação é excluída do cálculo JaCoCo. | A cobertura reportada e os limites avaliam o código funcional; a linha de inicialização não contribui para a meta. Alterações relevantes ao bootstrap devem ser avaliadas separadamente. | Gustavo Gila Pereira |
| Não há configuração Docker Compose no repositório. | A execução local requer PostgreSQL configurado separadamente; os testes iniciam seu próprio banco via Testcontainers. | Fernando Florêncio Nascimento da Silva |

