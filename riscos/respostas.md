# Respostas a Riscos — Todolist

| Campo | Valor |
|-------|-------|
| **Projeto** | Todolist (API REST de gerenciamento de tarefas com auditoria) |
| **Contexto acadêmico** | UFG — Engenharia de Software com Inteligência Artificial Generativa |
| **Documento** | Estratégias de resposta a riscos |
| **Data** | 2026-07-19 |
| **Versão do documento** | 1.0 |
| **Base** | `riscos/identificacao.md` v1.1 e `riscos/analise.md` v1.0 (mesmos IDs: R01–R13) |

## 1. Objetivo

Explorar, para cada risco identificado e analisado, as **quatro estratégias clássicas de resposta**
— **Evitar**, **Mitigar**, **Transferir** e **Aceitar** — descrevendo como cada uma poderia ser
aplicada e quais são suas implicações. O documento é **exploratório**: apresenta alternativas viáveis
para apoiar a decisão posterior, **sem eleger uma resposta definitiva** por risco. A priorização
sugerida pela análise (R04 como Crítico; R01, R02, R06, R07, R11, R12 como Altos) serve apenas de
referência para leitura, não como decisão tomada.

> **Diretrizes:** análise **qualitativa e exploratória**. Nem toda estratégia é igualmente aplicável a
> todo risco — quando uma abordagem for fraca, forçada ou inviável no contexto (ex.: "transferir" um
> risco puramente interno), isso é **explicitamente sinalizado**. As sugestões pressupõem informações
> hoje disponíveis no repositório; itens dependentes de validação estão na seção 4.

## 2. Estratégias possíveis por risco

### R04 — Evolução de schema sem ferramenta de migração *(exposição: Crítico)*
- **Descrição:** Schema gerido por `ddl-auto=update`, sem migração versionada; alterações de modelo
  não são reproduzíveis e já causaram violação de constraint.
- **Estratégias de resposta possíveis:**
  - **Evitar:** adotar ferramenta de migração versionada (Flyway ou Liquibase) e fixar `ddl-auto`
    em `validate` (ou `none`), eliminando a geração automática de schema — remove a causa-raiz da
    deriva e das constraints não recriadas.
  - **Mitigar:** manter `ddl-auto=update`, mas padronizar um procedimento documentado de recriação de
    schema em desenvolvimento e um checklist para mudanças de modelo, reduzindo a chance de erro sem
    eliminá-la; adicionar scripts SQL manuais versionados como paliativo.
  - **Transferir:** delegar a gestão de schema/migrações a uma plataforma gerenciada (ex.: pipeline de
    migração de um serviço de banco na nuvem) — transfere a execução/rollback, mas **não** a autoria
    das migrações; aplicabilidade limitada em contexto acadêmico local.
  - **Aceitar:** aceitar formalmente enquanto o projeto tiver apenas dados descartáveis de
    desenvolvimento, assumindo o custo de recriar a base quando necessário — só é razoável antes de
    existir qualquer dado que se queira preservar.

### R01 — Ausência de validação de entrada *(exposição: Alto)*
- **Descrição:** Requisições não passam por Bean Validation; entrada inválida só falha no banco.
- **Estratégias de resposta possíveis:**
  - **Evitar:** adicionar `spring-boot-starter-validation`, anotar o `TodoListRequestDTO`
    (`@NotBlank`, `@NotNull`, `@Size`) e aplicar `@Valid` no controller — impede que entrada inválida
    chegue à persistência.
  - **Mitigar:** validar manualmente no service os campos críticos (título, prioridade, categoria) sem
    o starter, reduzindo os casos mais comuns, ainda que de forma menos declarativa e completa.
  - **Transferir:** empurrar a validação para o cliente/consumidor da API (ex.: contrato OpenAPI que o
    front-end valida) — transfere parte da responsabilidade, mas **não protege o servidor** contra
    clientes que ignorem o contrato; transferência apenas parcial.
  - **Aceitar:** aceitar temporariamente em ambiente de teste controlado, onde as entradas são
    conhecidas, assumindo respostas 500 esporádicas — aceitável apenas enquanto não houver consumidores
    externos.

### R02 — Ausência de tratamento global de exceções *(exposição: Alto)*
- **Descrição:** Sem `@ControllerAdvice`; exceções não previstas geram respostas inconsistentes e
  possível vazamento de stack trace.
- **Estratégias de resposta possíveis:**
  - **Evitar:** implementar um `@RestControllerAdvice` central que mapeie exceções para respostas
    padronizadas (formato de erro único, sem stack trace) — elimina a inconsistência na origem.
  - **Mitigar:** tratar localmente as exceções mais frequentes (validação, enum inválido, constraint)
    em pontos específicos, reduzindo o alcance do problema sem padronização global.
  - **Transferir:** delegar a normalização de erros a um API gateway/proxy que uniformize respostas de
    erro — depende de infraestrutura externa não presente hoje; aplicabilidade limitada.
  - **Aceitar:** aceitar o comportamento padrão do Spring enquanto a API for consumida apenas
    internamente e o vazamento de detalhe não for sensível — razoável só em fase muito inicial.

### R06 — Credenciais de banco em texto plano versionadas *(exposição: Alto)*
- **Descrição:** Usuário/senha do banco embutidos e versionados no `pom.xml`.
- **Estratégias de resposta possíveis:**
  - **Evitar:** externalizar as credenciais para variáveis de ambiente / arquivo local não versionado
    (`.env`, `application-local.properties` no `.gitignore`) e removê-las do `pom.xml` — elimina o
    segredo do versionamento.
  - **Mitigar:** trocar as credenciais por valores exclusivos de desenvolvimento, sem privilégios além
    do banco local, reduzindo o impacto caso vazem — não remove o segredo do histórico, mas limita o
    dano.
  - **Transferir:** adotar um gerenciador de segredos (Vault, AWS/Azure Secret Manager) que injete as
    credenciais em runtime — transfere a custódia do segredo a um serviço especializado; custo/complexidade
    provavelmente altos para o contexto acadêmico.
  - **Aceitar:** aceitar enquanto as credenciais forem triviais, o banco for estritamente local e o
    repositório for privado — depende de confirmar (validação) que não há reuso nem repositório público.

### R07 — Ausência de pipeline de CI *(exposição: Alto)*
- **Descrição:** Sem verificação automatizada por alteração; testes dependem de execução local.
- **Estratégias de resposta possíveis:**
  - **Evitar:** configurar um pipeline (ex.: GitHub Actions) que rode `mvn test`/`package` a cada push
    e bloqueie merges com testes falhando — remove a dependência da disciplina individual.
  - **Mitigar:** instituir um hook de pré-commit/pré-push local ou um acordo de equipe para rodar o
    `Makefile` antes de integrar, reduzindo (sem garantir) a chance de regressão escapar.
  - **Transferir:** usar um serviço de CI gerenciado de terceiros (o próprio GitHub Actions, GitLab CI,
    CircleCI) — transfere a operação da infraestrutura de build, mas a definição do pipeline continua
    sendo responsabilidade da equipe.
  - **Aceitar:** aceitar a verificação manual enquanto a equipe for muito pequena e o ritmo de
    integração baixo, assumindo o risco de detecção tardia — frágil sob a premissa de equipe distribuída.

### R11 — Banco abaixo da versão mínima do ORM *(exposição: Alto)*
- **Descrição:** PostgreSQL 9.6.24 em uso, abaixo do mínimo (13) do Hibernate 7; opera via workaround.
- **Estratégias de resposta possíveis:**
  - **Evitar:** atualizar o PostgreSQL para 13+ em todos os ambientes, eliminando a incompatibilidade e
    o aviso `HHH000511` — remove a condição de risco na raiz.
  - **Mitigar:** manter a versão atual, porém restringir o uso a recursos comprovadamente suportados e
    documentar o workaround (`GenerationType.SEQUENCE`), reduzindo a chance de encontrar incompatibilidade.
  - **Transferir:** migrar para um PostgreSQL gerenciado (RDS, Cloud SQL, etc.) em versão suportada —
    transfere a manutenção/atualização do banco ao provedor; custo e conectividade podem ser proibitivos
    no contexto local.
  - **Aceitar:** aceitar enquanto a aplicação operar corretamente com o workaround e nenhum recurso não
    suportado for necessário — efeito hoje latente; exige reavaliar se surgir necessidade nova.

### R12 — Ausência de autenticação e autorização *(exposição: Alto)*
- **Descrição:** Todos os endpoints são públicos, sem controle de acesso.
- **Estratégias de resposta possíveis:**
  - **Evitar:** não expor a API além de `localhost` até que haja controle de acesso — evita a
    materialização do risco eliminando a superfície de exposição.
  - **Mitigar:** integrar Spring Security com autenticação (ex.: JWT) e regras de autorização por
    endpoint, reduzindo drasticamente a probabilidade de acesso indevido caso a API seja exposta.
  - **Transferir:** delegar autenticação/autorização a um provedor de identidade gerenciado
    (Keycloak, Auth0, Cognito) — transfere a complexidade e a responsabilidade de segurança de
    identidade a um serviço especializado.
  - **Aceitar:** aceitar enquanto a API rodar exclusivamente em ambiente local isolado, sem dados
    sensíveis e sem exposição de rede — razoável apenas na fase acadêmica atual; **requer validação**
    sobre planos de exposição.

### R03 — Aplicação e testes acoplados ao profile `desenv` *(exposição: Médio)*
- **Descrição:** Configuração essencial só resolvida com o profile `desenv`; divergência README × Makefile.
- **Estratégias de resposta possíveis:**
  - **Evitar:** prover configuração default funcional (ex.: `application.properties` com valores padrão
    ou `application-desenv.properties`) desacoplada da filtragem Maven, de modo que a aplicação e os
    testes subam sem depender do profile.
  - **Mitigar:** alinhar README e Makefile (usar `-P desenv` de forma consistente) e documentar o
    pré-requisito, reduzindo a chance de execução divergente sem eliminar o acoplamento.
  - **Transferir:** padronizar o ambiente via contêiner (Docker Compose com app + Postgres), delegando a
    consistência de configuração à imagem — desloca o problema para a definição da imagem, que a equipe
    ainda mantém.
  - **Aceitar:** aceitar enquanto todos os membros seguirem o mesmo comando documentado, assumindo o
    risco de "funciona só na minha máquina" — depende de confirmar (validação) a falha real de `make test`.

### R05 — Framework recém-lançado (Spring Boot 4) *(exposição: Médio)*
- **Descrição:** Adoção de versão nova, com artefatos renomeados e menos suporte da comunidade.
- **Estratégias de resposta possíveis:**
  - **Evitar:** rebaixar para uma versão LTS/estável amplamente suportada do Spring Boot — remove a
    exposição a instabilidades da linha nova, ao custo de retrabalho de downgrade.
  - **Mitigar:** fixar (pin) todas as versões, manter documentação das incompatibilidades já resolvidas
    e testar atualizações em branch isolada antes de integrar, reduzindo surpresas.
  - **Transferir:** apoiar-se em suporte comercial/LTS do fornecedor do framework, se disponível —
    transfere parte do risco de manutenção; pouco realista em projeto acadêmico.
  - **Aceitar:** aceitar conscientemente a escolha da versão nova como decisão pedagógica do projeto,
    assumindo eventuais retrabalhos pontuais — coerente se a adoção for intencional (**requer validação**).

### R08 — Endpoints de listagem sem paginação *(exposição: Médio)*
- **Descrição:** `read-all` e `all/audit` retornam todos os registros de uma vez.
- **Estratégias de resposta possíveis:**
  - **Evitar:** implementar paginação (`Pageable`) nos endpoints de listagem e auditoria desde já,
    impedindo respostas ilimitadas.
  - **Mitigar:** impor um limite máximo de resultados por consulta ou um teto padrão, reduzindo o
    impacto sem paginação completa.
  - **Transferir:** delegar limitação/streaming a uma camada intermediária (gateway/CDN com limites de
    payload) — paliativo externo que não resolve a origem; aplicabilidade limitada.
  - **Aceitar:** aceitar enquanto o volume de dados/auditoria for comprovadamente baixo (contexto
    acadêmico), reavaliando se o crescimento tornar a degradação perceptível — **requer validação** do
    volume real esperado.

### R09 — Coordenação de equipe distribuída *(exposição: Médio)*
- **Descrição:** Configuração local não padronizada somada à distribuição amplia divergências.
- **Estratégias de resposta possíveis:**
  - **Evitar:** padronizar totalmente o ambiente (contêiner + configuração versionada + CI comum),
    eliminando a variabilidade entre máquinas — depende de resolver R03 e R07.
  - **Mitigar:** estabelecer convenções de trabalho (documentação de setup, canais e ritos de
    comunicação, definição de "pronto"), reduzindo o atrito de coordenação sem uniformizar o ambiente.
  - **Transferir:** adotar ambiente de desenvolvimento hospedado/compartilhado (Codespaces, dev
    container remoto) — transfere a padronização do ambiente para uma plataforma externa.
  - **Aceitar:** aceitar o risco se a equipe for pequena/co-localizada na prática, assumindo divergências
    pontuais — **requer validação** do tamanho e maturidade reais da equipe.

### R10 — Requisitos em evolução vs. enums fixos *(exposição: Médio)*
- **Descrição:** `Priority`/`Category` como enums fixos exigem código e migração a cada mudança de domínio.
- **Estratégias de resposta possíveis:**
  - **Evitar:** modelar prioridade/categoria como tabelas de referência (entidades parametrizáveis),
    permitindo evoluir valores sem recompilar — remove o acoplamento código × domínio.
  - **Mitigar:** manter os enums, mas definir um procedimento claro de mudança (incluindo tratamento das
    revisões antigas do Envers) e testes que cubram valores legados, reduzindo o atrito.
  - **Transferir:** externalizar a configuração de domínio para uma fonte gerida por outra
    equipe/serviço — pouco aplicável a um projeto pequeno e coeso; transferência artificial.
  - **Aceitar:** aceitar enquanto os conjuntos de valores forem estáveis, assumindo o custo pontual de
    uma eventual alteração — **requer validação** da frequência esperada de mudança.

### R13 — Base de código integralmente gerada por IA *(exposição: Médio)*
- **Descrição:** Todo o código foi gerado por IA, com risco de decisões subótimas/defeitos sutis sem
  revisão humana sistemática.
- **Estratégias de resposta possíveis:**
  - **Evitar:** instituir revisão humana obrigatória (code review par-a-par) de todo código gerado antes
    da integração — remove a ausência de escrutínio como fator de risco.
  - **Mitigar:** reforçar a rede de segurança (ampliar cobertura de testes, análise estática/linters,
    revisão focada em pontos críticos como validação e segurança), reduzindo a chance de defeitos
    escaparem.
  - **Transferir:** apoiar-se em ferramentas de revisão automatizada/terceiros (análise estática de
    fornecedor, revisão assistida por IA independente) — transfere parte do escrutínio a ferramentas
    externas, sem substituir o julgamento humano.
  - **Aceitar:** aceitar em componentes de baixo risco e bem cobertos por testes, assumindo que a suíte
    existente (61 testes) oferece garantia suficiente — **requer validação** sobre a existência de
    revisão humana.

## 3. Considerações sobre a aplicação das estratégias

### 3.1 Situações em que cada estratégia tende a ser mais adequada

- **Evitar** — tende a ser adequada para riscos de **exposição alta/crítica cuja causa-raiz é
  eliminável a custo razoável** (ex.: R04, R01, R06, R07, R11, R12). É a resposta mais definitiva, mas
  costuma exigir mudança estrutural ou de escopo.
- **Mitigar** — adequada quando **eliminar a causa é caro ou inviável**, mas é possível reduzir
  probabilidade e/ou impacto (ex.: R05, R08, R10, R13). É a resposta "meio-termo" mais comum em
  software, aplicável à maioria dos riscos como complemento.
- **Transferir** — adequada quando existe um **terceiro especializado disposto a assumir a
  responsabilidade** (provedor gerenciado, serviço de identidade, CI/secrets como serviço). Em projeto
  acadêmico local, tende a ser **pouco aplicável ou desproporcional** para vários riscos (marcado como
  limitado em R02, R04, R06, R10).
- **Aceitar** — adequada para riscos de **baixa exposição, efeito latente ou condicionados a um cenário
  que ainda não se aplica** (ex.: R12 e R08 enquanto o uso for local/pequeno). Deve ser uma decisão
  **consciente e documentada**, não omissão.

### 3.2 Limitações e trade-offs por abordagem

| Estratégia | Principais trade-offs |
|-----------|-----------------------|
| **Evitar** | Maior custo/esforço imediato e possível mudança de escopo, arquitetura ou versão; pode atrasar entregas de curto prazo em troca de robustez. |
| **Mitigar** | Reduz, mas **não elimina** o risco (risco residual permanece); pode gerar falsa sensação de segurança e exigir manutenção contínua dos controles. |
| **Transferir** | Introduz **dependência externa**, custo financeiro e, muitas vezes, apenas transferência **parcial** (a autoria/definição continua interna); pode ser desproporcional ao contexto acadêmico. |
| **Aceitar** | Nenhum esforço de tratamento, mas **exposição integral** se o cenário mudar; só é defensável com premissas explícitas (uso local, dados descartáveis) que **precisam se manter verdadeiras**. |

## 4. Observações gerais

### 4.1 Dependência de contexto adicional para a decisão

As respostas acima são **exploratórias** e a escolha entre elas depende de fatores ainda não fixados:

- **Destino do projeto** — se permanecerá acadêmico/local ou evoluirá para uso real muda radicalmente a
  resposta a R06, R11 e R12 (aceitar torna-se insustentável fora do localhost).
- **Volume esperado de dados/auditoria** — condiciona a urgência de tratar R08 (evitar vs. aceitar).
- **Tamanho e distribuição reais da equipe** — condicionam R07 e R09 (peso de CI e padronização).
- **Existência de dados a preservar** — determina se "aceitar" o R04 ainda é admissível.
- **Custo/orçamento** — viabiliza ou não as estratégias de **transferir** (serviços gerenciados, IdP,
  banco na nuvem), em geral pouco realistas sem orçamento.

### 4.2 Pontos que exigem validação com stakeholders

Herdados das seções de incerteza de `identificacao.md` e `analise.md`:

| Risco | O que validar antes de decidir a resposta |
|-------|-------------------------------------------|
| R03 | Confirmar se `make test` falha sem `-P desenv` em ambiente limpo. |
| R05 | Confirmar se a adoção do Spring Boot 4.0.6 é decisão intencional (aceitar) ou incidental (evitar/rebaixar). |
| R06 | Confirmar se o repositório é público e se as credenciais são reutilizadas fora de desenvolvimento. |
| R08 | Estimar o volume real de tarefas e de revisões de auditoria esperado. |
| R09 | Confirmar tamanho, maturidade e grau real de distribuição da equipe. |
| R10 | Alinhar a frequência esperada de mudança em `Priority`/`Category`. |
| R11 | Confirmar se há previsão de migração do PostgreSQL para 13+ e se recursos não suportados serão necessários. |
| R12 | Confirmar se/quando a API será exposta além do ambiente local. |
| R13 | Confirmar se existe (ou existirá) processo de revisão humana do código gerado por IA. |

### 4.3 Limitações desta análise

- As sugestões baseiam-se apenas no que é observável no repositório e nas premissas de contexto
  fornecidas; **não substituem** um plano de resposta formal com responsáveis, prazos e custos.
- Nenhuma estratégia foi **eleita** por risco — o objetivo é subsidiar a decisão, não tomá-la.
- A viabilidade das estratégias de **transferir** é, em geral, **baixa** neste contexto acadêmico e foi
  sinalizada risco a risco; sua inclusão cumpre o mapeamento das quatro abordagens clássicas, sem
  implicar recomendação.
- Estratégias podem ser **combinadas** (ex.: mitigar + aceitar o risco residual); os itens acima não são
  mutuamente exclusivos.
