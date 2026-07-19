# Identificação de Riscos — Todolist

| Campo | Valor |
|-------|-------|
| **Projeto** | Todolist (API REST de gerenciamento de tarefas com auditoria) |
| **Documento** | Identificação de riscos |
| **Data** | 2026-07-19 |
| **Versão do documento** | 1.1 (revisão após leitura do `README.md`) |
| **Contexto acadêmico** | UFG — Engenharia de Software com Inteligência Artificial Generativa |
| **Stack observada** | Java 17, Spring Boot 4.0.6 (`spring-boot-starter-webmvc`, `data-jpa`), Hibernate ORM 7.2.12 + Envers, PostgreSQL 9.6.24, Lombok, Maven |
| **Fonte de análise** | Código-fonte (`src/main`), testes (`src/test`), configuração (`pom.xml`, `application.properties`), automação (`Makefile`), documentação (`README.md`), versionamento (`git log`) e árvore do repositório |
| **Premissas de contexto** | Projeto de desenvolvimento de software; requisitos em evolução; equipe distribuída |

> **Nota de revisão (v1.1):** a v1.0 foi elaborada sem a leitura do `README.md`. Após incorporá-lo,
> três riscos foram **acrescentados** (R11 — versão do PostgreSQL; R12 — ausência de autenticação;
> R13 — base gerada por IA) e riscos existentes foram **reforçados** com evidências do próprio README
> (ver R04, R05) ou anotados como **limitações reconhecidas** pela equipe (R01, R07, R08).

## 1. Objetivo

Este documento identifica os riscos do projeto **Todolist**, uma API REST em Spring Boot para
gerenciamento de tarefas (CRUD sob `/api/tasks`, filtros dinâmicos via `Specification` e histórico
de auditoria via Hibernate Envers). Cada risco é registrado com **ID**, **nome**, **categoria**,
**descrição** e **contexto de ocorrência** (a evidência que o fundamenta no projeto). A análise de
probabilidade/impacto e as respostas são deixadas para documentos complementares
(`riscos/analise.md` e `riscos/respostas.md`, ainda não elaborados).

> A identificação limita-se ao que foi observado no repositório e às três premissas de contexto
> fornecidas (software em desenvolvimento, requisitos em evolução, equipe distribuída). Onde há
> incerteza ou dependência de decisão externa, o texto **sinaliza a necessidade de validação**.

## 2. Método de identificação

Os riscos foram levantados por inspeção dos artefatos e agrupados por categoria:

- **Técnico/Ambiente (TEC)** — configuração, dependências, robustez de execução e evolução do schema.
- **Segurança (SEG)** — exposição de credenciais.
- **Qualidade (QUA)** — validação, verificação automatizada e escalabilidade.
- **Processo/Gestão (PRO)** — coordenação da equipe distribuída e evolução de requisitos.

## 3. Riscos identificados

### R01 — Ausência de validação de entrada nos endpoints
- **Categoria:** Técnico/Ambiente (TEC)
- **Descrição:** As requisições de criação/atualização não passam por validação de contrato. Campos
  obrigatórios ausentes ou inválidos (ex.: `title` nulo/vazio, `priority` ou `category` nulos) só são
  barrados pela restrição do banco, resultando em erro genérico de servidor em vez de resposta de
  requisição inválida.
- **Contexto de ocorrência:** `TodoListRequestDTO` não possui anotações de Bean Validation
  (`@NotBlank`, `@NotNull`, `@Size`), o `TodoListController` não usa `@Valid`, e o `pom.xml` **não
  inclui** `spring-boot-starter-validation`. A entidade `TodoList` define `title` como
  `nullable = false, length = 100` e `priority`/`category` como `nullable = false` — logo, uma entrada
  malformada só falha na camada de persistência (potencial HTTP 500). Manifesta-se sempre que um
  cliente envia payload incompleto ou fora do domínio esperado. **Limitação reconhecida:** o
  `README.md` já registra "Sem validação de entrada" em *Limitações Conhecidas* e prevê a adição de
  `spring-boot-starter-validation` em *Próximos Passos* — o risco existe, mas é conhecido e planejado.

### R02 — Ausência de tratamento global de exceções
- **Categoria:** Técnico/Ambiente (TEC)
- **Descrição:** Não há um manipulador central de erros. Exceções não previstas (violação de
  restrição do banco, falha de desserialização de enum, erro de conexão) tendem a vazar como respostas
  inconsistentes e potencialmente com detalhes internos (stack trace), prejudicando o consumo da API e
  expondo informação de implementação.
- **Contexto de ocorrência:** Não existe classe anotada com `@ControllerAdvice`/`@ExceptionHandler`
  no projeto (busca sem resultados em `src/main`). Apenas o caso 404 é tratado, via
  `ResponseStatusException` no `TodoListService`. Enviar um valor de enum inexistente para `priority`
  (ex.: `"URGENTE"`) ou omitir um campo obrigatório aciona o comportamento padrão do Spring, sem
  formato de erro padronizado.

### R03 — Aplicação e testes acoplados ao profile `desenv`
- **Categoria:** Técnico/Ambiente (TEC)
- **Descrição:** A configuração essencial (URL do banco, credenciais, porta, context-path) só é
  resolvida quando o profile `desenv` está ativo. Sem ele, a aplicação não sobe e os testes de
  contexto falham, gerando divergência entre execuções e "funciona só na minha máquina".
- **Contexto de ocorrência:** `application.properties` contém apenas placeholders de filtragem Maven
  (`@spring.datasource.url@`, `@server.port@`, etc.), cujos valores estão declarados exclusivamente no
  profile `desenv` do `pom.xml`. Não há profile default nem `application-desenv.properties`. O
  `Makefile` executa `run` com `-P desenv`, porém o alvo `test` roda `./mvnw test` **sem** `-P desenv`;
  como `TodolistApplicationTests.contextLoads` usa `@SpringBootTest` (carrega o contexto completo,
  incluindo o `DataSource`), os placeholders não resolvidos tendem a impedir a subida do contexto.
  **Requer validação** executando `make test` em ambiente limpo para confirmar a falha.

### R04 — Evolução de schema sem ferramenta de migração
- **Categoria:** Técnico/Ambiente (TEC)
- **Descrição:** O schema do banco é gerado/alterado automaticamente pelo Hibernate a cada
  inicialização. Em um cenário de requisitos em evolução e equipe distribuída, isso favorece deriva de
  schema (schema drift) entre ambientes e mudanças destrutivas não rastreadas, sem histórico versionado
  de migrações.
- **Contexto de ocorrência:** `application.properties` define `spring.jpa.hibernate.ddl-auto=update` e
  o `pom.xml` **não** inclui Flyway nem Liquibase. Como os requisitos podem mudar (novos campos,
  alteração de restrições), cada desenvolvedor pode acabar com uma versão diferente da tabela
  `todolist`/`todolist_audit`, sem um caminho de migração reproduzível. **Evidência concreta:** o
  próprio `README.md` (seção "Uso da IA") relata que a troca dos valores dos enums provocou violação
  de constraint CHECK porque `ddl-auto=update` **não recria constraints existentes**, exigindo
  recriar a tabela manualmente em desenvolvimento — o risco já se materializou uma vez.

### R05 — Dependência de versão de framework recém-lançada (bleeding-edge)
- **Categoria:** Técnico/Ambiente (TEC)
- **Descrição:** O projeto adota uma linha muito recente do Spring Boot, cujos artefatos foram
  renomeados em relação às versões amplamente usadas. Isso implica menor base de documentação/soluções
  na comunidade e maior chance de mudanças incompatíveis em atualizações.
- **Contexto de ocorrência:** O `pom.xml` fixa o parent `spring-boot-starter-parent` na versão
  `4.0.6` e usa artefatos da nova nomenclatura (`spring-boot-starter-webmvc`,
  `spring-boot-starter-webmvc-test`), em vez dos tradicionais `-web`/`-test`. **Evidência concreta:**
  o `README.md` (seção "Uso da IA") relata que o `TodoListControllerTest` não executava devido a
  **quatro incompatibilidades com o Spring Boot 4.0** (`@MockBean` removido, `@WebMvcTest`/`ObjectMapper`
  em pacotes renomeados, `@EnableJpaAuditing` quebrando o slice de testes). Ambientes de uma equipe
  distribuída podem ter cache/ferramentas desalinhados, e problemas dessa versão têm menos material de
  referência na comunidade.

### R06 — Credenciais de banco em texto plano versionadas
- **Categoria:** Segurança (SEG)
- **Descrição:** Usuário e senha do banco estão embutidos e versionados no repositório, o que expõe
  segredo de ambiente a qualquer pessoa com acesso ao código e dificulta a separação entre ambientes.
- **Contexto de ocorrência:** O profile `desenv` no `pom.xml` define
  `spring.datasource.username=postgres` e `spring.datasource.password=pactodb` em claro, e esses
  valores são o único meio de configuração (ver R03). Embora rotulado como `desenv`, o segredo trafega
  no controle de versão. **Requer validação** sobre se as mesmas credenciais são reutilizadas fora de
  desenvolvimento.

### R07 — Ausência de pipeline de Integração Contínua (CI)
- **Categoria:** Qualidade (QUA)
- **Descrição:** Não há verificação automatizada a cada alteração enviada. Com equipe distribuída, a
  ausência de CI aumenta o risco de integração quebrada só ser percebida tardiamente, e de regressões
  passarem despercebidas apesar da existência de testes.
- **Contexto de ocorrência:** Não existe diretório `.github/workflows` (nem outro descritor de
  pipeline) no repositório. A verificação depende de cada desenvolvedor rodar o `Makefile`
  (`make test`) localmente — o que, combinado ao R03, torna a execução não determinística entre
  máquinas. Reforça o R03: o `README.md` instrui rodar os testes com `mvn test -P desenv` (com
  profile), enquanto o alvo `make test` executa `./mvnw test` (sem profile) — a própria documentação
  diverge do `Makefile`. **Limitação reconhecida:** "Pipeline CI/CD" consta em *Próximos Passos*.

### R08 — Endpoints de listagem sem paginação
- **Categoria:** Qualidade (QUA)
- **Descrição:** As consultas de listagem retornam todos os registros de uma vez. Conforme o volume de
  tarefas e de revisões de auditoria cresce, aumentam o consumo de memória, o tempo de resposta e o
  tráfego, podendo degradar a aplicação.
- **Contexto de ocorrência:** `TodoListService.findAll(...)` faz `repository.findAll(spec).stream()`
  sem `Pageable`, e `TodoListAuditService.findAllAudits()` carrega todas as revisões da tabela de
  auditoria (`forRevisionsOfEntity`) sem limite. Como o Envers grava um snapshot por operação
  (INSERT/UPDATE/DELETE), a tabela de auditoria cresce mais rápido que a de tarefas. **Limitação
  reconhecida:** "Sem paginação" consta em *Limitações Conhecidas* e "Paginação e ordenação" em
  *Próximos Passos*; ainda assim, os endpoints de auditoria (`/all/audit`) permanecem sem plano de
  paginação e tendem a ser os mais afetados.

### R09 — Coordenação de equipe distribuída sobre configuração local
- **Categoria:** Processo/Gestão (PRO)
- **Descrição:** A dependência de configuração local não padronizada, somada à distribuição da equipe,
  amplia o risco de divergências de ambiente, retrabalho e falhas de integração por diferenças de
  setup entre os membros.
- **Contexto de ocorrência:** Premissa de equipe distribuída combinada às evidências dos riscos R03
  (config acoplada ao profile local), R06 (segredo no repositório) e R07 (sem CI que padronize a
  verificação). Sem um ambiente/contêiner padronizado ou pipeline comum, cada membro reproduz o setup
  manualmente, o que tende a gerar comportamento inconsistente entre desenvolvedores.

### R10 — Acoplamento entre requisitos em evolução e domínios fixos em código
- **Categoria:** Processo/Gestão (PRO)
- **Descrição:** Valores de domínio hoje modelados como enumerações fixas exigem alteração de código
  (e potencial migração de dados) a cada mudança de requisito, o que aumenta o custo de evolução e o
  risco de inconsistência com o histórico de auditoria já registrado.
- **Contexto de ocorrência:** `Priority` (`BAIXO`, `MEDIO`, `ALTO`) e `Category` (`TRABALHO`,
  `ESTUDO`, `PESSOAL`) são enums persistidos como String (`@Enumerated(EnumType.STRING)`). Sob
  requisitos em evolução, incluir/renomear uma categoria ou prioridade obriga a recompilar e conviver
  com valores antigos gravados na tabela `todolist_audit`. **Evidência concreta:** a troca dos valores
  desses enums para português já causou violação de constraint CHECK (relatada no `README.md`),
  ilustrando o custo de alterar o domínio. **Requer validação** com os stakeholders sobre a
  estabilidade esperada desses conjuntos de valores.

### R11 — Banco de dados abaixo da versão mínima suportada pelo ORM
- **Categoria:** Técnico/Ambiente (TEC)
- **Descrição:** O PostgreSQL em uso está em uma versão anterior à mínima recomendada pela versão do
  Hibernate adotada, operando apenas via workaround. Isso implica comportamento não homologado,
  avisos de incompatibilidade e risco de defeitos sutis ou de bloqueio ao evoluir recursos do ORM.
- **Contexto de ocorrência:** O `README.md` declara PostgreSQL **9.6.24** no ambiente de
  desenvolvimento, enquanto o Hibernate **7.2.12** recomenda versão mínima **13**; o próprio README
  registra o aviso `HHH000511` (versão não suportada) e o workaround aplicado
  (`GenerationType.SEQUENCE` em vez de `GENERATED AS IDENTITY`, indisponível antes do PostgreSQL 10) —
  confirmado no código pela anotação `@SequenceGenerator` na entidade `TodoList`. Manifesta-se ao usar
  recursos do ORM que assumem versões mais novas ou ao portar para outro ambiente de banco.

### R12 — Ausência de autenticação e autorização
- **Categoria:** Segurança (SEG)
- **Descrição:** Todos os endpoints são públicos, sem qualquer controle de acesso ou identificação de
  usuário. Qualquer cliente com acesso à rede pode criar, alterar, excluir e auditar tarefas
  livremente.
- **Contexto de ocorrência:** O `README.md` lista "Sem autenticação" em *Limitações Conhecidas*; o
  `pom.xml` não inclui `spring-boot-starter-security` e nenhum endpoint do `TodoListController` aplica
  restrição de acesso. Torna-se crítico caso a API seja exposta fora de um ambiente local isolado.
  **Limitação reconhecida:** consta "Autenticação e autorização" em *Próximos Passos*.

### R13 — Base de código integralmente gerada por IA
- **Categoria:** Processo/Gestão (PRO)
- **Descrição:** Todo o código foi gerado por assistente de IA. Sem revisão humana sistemática, há
  risco de decisões subótimas, suposições implícitas ou defeitos sutis passarem despercebidos,
  além de concentrar em prompts o conhecimento sobre as escolhas de design.
- **Contexto de ocorrência:** O `README.md` (seção "Uso da IA durante o desenvolvimento") afirma que
  "todo o código deste projeto foi gerado com o auxílio do Claude Sonnet 4.6". Em um projeto acadêmico
  com equipe distribuída, isso exige processo explícito de revisão e validação do que foi gerado —
  reforçando a importância dos riscos de qualidade R01, R02 e R07. **Requer validação** sobre a
  existência de revisão humana par-a-par do código gerado.

## 4. Resumo

Foram identificados **13 riscos** distribuídos em quatro categorias:

| ID | Risco | Categoria |
|----|-------|-----------|
| R01 | Ausência de validação de entrada nos endpoints | Técnico/Ambiente |
| R02 | Ausência de tratamento global de exceções | Técnico/Ambiente |
| R03 | Aplicação e testes acoplados ao profile `desenv` | Técnico/Ambiente |
| R04 | Evolução de schema sem ferramenta de migração | Técnico/Ambiente |
| R05 | Dependência de versão de framework recém-lançada | Técnico/Ambiente |
| R06 | Credenciais de banco em texto plano versionadas | Segurança |
| R07 | Ausência de pipeline de CI | Qualidade |
| R08 | Endpoints de listagem sem paginação | Qualidade |
| R09 | Coordenação de equipe distribuída sobre configuração local | Processo/Gestão |
| R10 | Acoplamento entre requisitos em evolução e domínios fixos em código | Processo/Gestão |
| R11 | Banco de dados abaixo da versão mínima suportada pelo ORM | Técnico/Ambiente |
| R12 | Ausência de autenticação e autorização | Segurança |
| R13 | Base de código integralmente gerada por IA | Processo/Gestão |

**Distribuição por categoria:** Técnico/Ambiente (6), Segurança (2), Qualidade (2), Processo/Gestão (3).

Os riscos **técnicos** (R01–R05, R11) concentram-se na robustez da API e na reprodutibilidade do
ambiente, sendo R01–R03 e R11 os mais imediatos por afetarem o comportamento em runtime, a execução
dos testes e a compatibilidade com o banco. Os riscos de **segurança** (R06, R12) envolvem segredo
versionado e endpoints públicos. Os riscos de **qualidade** (R07, R08) afetam a confiabilidade da
verificação e a escalabilidade. Os riscos de **processo** (R09, R10, R13) derivam das premissas de
contexto (equipe distribuída e requisitos em evolução) e da natureza gerada por IA da base de código,
amplificando os demais. Vários riscos (R01, R07, R08, R12) são **limitações já reconhecidas** no
`README.md`, o que reduz a probabilidade de surpresa, mas não os elimina enquanto não forem tratados.

## 5. Itens que requerem validação

Os pontos abaixo foram sinalizados no texto por não serem plenamente conclusivos apenas com a inspeção
estática:

| Item | Referência | O que validar |
|------|------------|---------------|
| Falha real de `make test` sem `-P desenv` | R03 | Executar `make test` em ambiente limpo e confirmar se o contexto sobe (README usa `-P desenv`, Makefile não). |
| Política de versão do Spring Boot 4.0.6 | R05 | Confirmar se a adoção da linha recente é decisão consciente da equipe. |
| Reuso das credenciais fora de `desenv` | R06 | Verificar se `postgres/pactodb` é usado apenas localmente. |
| Estabilidade dos enums de domínio | R10 | Alinhar com stakeholders a probabilidade de novas categorias/prioridades. |
| Plano de atualização do PostgreSQL | R11 | Confirmar se há previsão de migração para PostgreSQL 13+ (README cita como próximo passo). |
| Revisão humana do código gerado por IA | R13 | Verificar se existe processo de revisão par-a-par do que foi gerado. |
