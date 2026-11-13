# Estudo para a prova oral

## Por que usamos Testcontainers em vez de um banco H2?

Os testes usam PostgreSQL em container, que é o banco usado pela aplicação. Assim, os testes exercitam o comportamento do banco real do projeto, inclusive transações e locks pessimistas. Um banco H2 pode ter diferenças de comportamento e não substituiria essa validação. É necessário ter o Docker em execução.

## Por que usamos WireMock?

WireMock simula a API externa de notificação durante os testes. Isso permite verificar a requisição enviada e testar respostas diferentes, como sucesso e erro HTTP 500, sem depender do serviço externo real.

## Como o lock pessimista impede a dupla reserva?

Ao criar uma reserva, o serviço inicia uma transação e bloqueia a linha do recurso com um lock de escrita pessimista. Outra transação que tenta reservar o mesmo recurso precisa esperar. Depois que o lock é liberado, ela verifica as reservas que se sobrepõem ao horário solicitado; se encontrar uma reserva em estado que ocupa o recurso, rejeita a tentativa como conflito. O teste de concorrência verifica 100 tentativas: uma reserva é criada e as outras 99 entram em conflito.

## O que é a cobertura JaCoCo?

JaCoCo mede quais linhas e branches do código foram executados durante os testes e gera um relatório HTML. Neste projeto, o `verify` cria o relatório em `target/site/jacoco/index.html` e verifica mínimos de 80% para linhas e 70% para branches. Cobertura mede execução, não garante por si só que o código esteja correto.

## Como funciona a autorização por perfil?

A aplicação autentica usando HTTP Basic. O usuário é carregado pelo e-mail no banco e seu perfil é convertido em uma role do Spring Security. A aprovação de reserva permite os perfis `RESPONSAVEL` e `ADMINISTRADOR`; um `SOLICITANTE` autenticado recebe HTTP 403. A regra está aplicada à operação de aprovação.

## O que são TDD e BDD?

**TDD** é desenvolver em ciclos: escrever um teste que falha, implementar o necessário e refatorar mantendo os testes passando. **BDD** descreve o comportamento esperado em cenários compreensíveis, geralmente como dado um contexto, quando uma ação ocorre e então um resultado é esperado.

Neste projeto há testes de integração que verificam comportamentos, como conflitos de reserva e permissão para aprovar. Isso permite discutir testes orientados a comportamento em nível básico, mas não comprova que todo o desenvolvimento seguiu TDD nem que os testes foram escritos antes do código.
