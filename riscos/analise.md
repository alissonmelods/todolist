# Análise de Riscos — Todolist

| Campo | Valor |
|-------|-------|
| **Projeto** | Todolist (API REST de gerenciamento de tarefas com auditoria) |
| **Contexto acadêmico** | UFG — Engenharia de Software com Inteligência Artificial Generativa |
| **Documento** | Análise de riscos |
| **Data** | 2026-07-19 |
| **Versão do documento** | 1.0 |
| **Base** | `riscos/identificacao.md` v1.1 (mesmos IDs: R01–R13) |

## 1. Objetivo

Analisar qualitativamente os **13 riscos** identificados em `riscos/identificacao.md`, atribuindo a
cada um sua **probabilidade** e **impacto** qualitativos, detalhando os **possíveis impactos no
projeto**, os **fatores que influenciam a ocorrência** e a **justificativa** da classificação. Ao
final, os riscos são posicionados em uma **matriz qualitativa de probabilidade × impacto**. O
resultado orienta a priorização das respostas em `riscos/respostas.md` (a elaborar).

> **Diretriz:** a análise é estritamente **qualitativa** — não são atribuídos valores numéricos de
> probabilidade ou impacto. Onde a classificação depende de informação não confirmada (volume real de
> dados, tamanho efetivo da equipe, exposição de rede), a **incerteza é sinalizada** no texto.

## 2. Escalas adotadas

- **Probabilidade:** Baixa · Média · Alta
- **Impacto:** Baixo · Médio · Alto
- **Nível de exposição** (severidade), derivado da combinação Probabilidade × Impacto:

| Prob. \ Impacto | Baixo | Médio | Alto |
|-----------------|-------|-------|------|
| **Alta** | Médio | Alto | Crítico |
| **Média** | Baixo | Médio | Alto |
| **Baixa** | Baixo | Baixo | Médio |

## 3. Análise por risco

### R01 — Ausência de validação de entrada nos endpoints
- **Descrição:** Requisições de criação/atualização não passam por Bean Validation; campos
  obrigatórios ausentes ou fora do domínio só são barrados pela restrição do banco.
- **Possíveis impactos no projeto:** respostas HTTP 500 em vez de 400 (má experiência de consumo);
  possível vazamento de detalhe interno na resposta de erro; retrabalho para adicionar validação e
  ajustar testes; ruído no diagnóstico de defeitos.
- **Fatores que influenciam a ocorrência:** entrada externa não controlada; ausência de
  `spring-boot-starter-validation` e de `@Valid`; inexistência de tratamento global de erros (R02),
  que amplifica o efeito.
- **Probabilidade:** Alta
- **Impacto:** Médio
- **Nível de exposição:** Alto
- **Justificativa da classificação:** como não há qualquer barreira de contrato, basta um payload
  incompleto para acionar o risco — ocorrência altamente provável. O impacto é médio: gera erro
  tratável e má resposta, sem corromper dados nem derrubar a aplicação.

### R02 — Ausência de tratamento global de exceções
- **Descrição:** Não há `@ControllerAdvice`; exceções não previstas caem no comportamento padrão do
  Spring, com respostas inconsistentes e possível exposição de stack trace.
- **Possíveis impactos no projeto:** contrato de erro não padronizado dificulta a integração de
  clientes; exposição de informação de implementação (risco de segurança leve); esforço de suporte e
  depuração elevado.
- **Fatores que influenciam a ocorrência:** ausência de validação (R01), que empurra erros para
  camadas mais baixas; envio de enums inválidos; violações de constraint do banco; qualquer falha não
  mapeada como 404.
- **Probabilidade:** Alta
- **Impacto:** Médio
- **Nível de exposição:** Alto
- **Justificativa da classificação:** dado o conjunto de erros não tratados (validação, enum,
  constraint), a ocorrência é muito provável. Impacto médio por ser contornável e não afetar a
  integridade dos dados, embora a exposição de stack trace agregue sensibilidade.

### R03 — Aplicação e testes acoplados ao profile `desenv`
- **Descrição:** A configuração essencial só é resolvida com o profile `desenv` ativo; sem ele, a
  aplicação não sobe e o teste de contexto tende a falhar.
- **Possíveis impactos no projeto:** falhas de build/execução intermitentes entre membros; testes que
  passam para uns e falham para outros; tempo perdido em diagnóstico de "funciona só na minha máquina".
- **Fatores que influenciam a ocorrência:** `application.properties` apenas com placeholders Maven;
  ausência de profile default; **divergência entre README (`mvn test -P desenv`) e Makefile
  (`./mvnw test`)**. *Incerteza:* a falha efetiva de `make test` precisa ser confirmada em ambiente
  limpo.
- **Probabilidade:** Média
- **Impacto:** Médio
- **Nível de exposição:** Médio
- **Justificativa da classificação:** a ocorrência depende de rodar sem o profile (o Makefile favorece
  isso, mas um dev que siga o README evita) — daí probabilidade média, com incerteza. Impacto médio:
  bloqueia temporariamente, mas é diagnosticável e contornável.

### R04 — Evolução de schema sem ferramenta de migração
- **Descrição:** O schema é gerido por `ddl-auto=update`, sem Flyway/Liquibase; alterações de modelo
  não são versionadas nem aplicadas de forma reproduzível.
- **Possíveis impactos no projeto:** deriva de schema entre ambientes; alterações destrutivas não
  rastreadas; necessidade de recriar tabelas manualmente (perda de dados em dev e risco sério caso
  houvesse dados reais); inconsistência com o histórico de auditoria do Envers.
- **Fatores que influenciam a ocorrência:** **requisitos em evolução** (mudanças frequentes de modelo);
  limitação conhecida do `ddl-auto=update` de não recriar constraints; equipe distribuída com bancos
  locais independentes.
- **Probabilidade:** Alta
- **Impacto:** Alto
- **Nível de exposição:** Crítico
- **Justificativa da classificação:** o risco **já se materializou** (violação de constraint CHECK ao
  traduzir os enums, registrada no README), e o contexto de requisitos em evolução torna a recorrência
  provável — probabilidade alta. Impacto alto por afetar integridade/continuidade dos dados e do
  histórico de auditoria.

### R05 — Dependência de versão de framework recém-lançada
- **Descrição:** Adoção do Spring Boot 4.0.6, com artefatos renomeados e menor base de suporte da
  comunidade.
- **Possíveis impactos no projeto:** retrabalho ao integrar novas bibliotecas ou atualizar versões;
  bloqueios temporários por incompatibilidade; dificuldade de encontrar soluções documentadas.
- **Fatores que influenciam a ocorrência:** inclusão de novas dependências; atualizações de versão;
  onboarding de membros com ferramentas/caches desalinhados; ambiente de equipe distribuída.
- **Probabilidade:** Média
- **Impacto:** Médio
- **Nível de exposição:** Médio
- **Justificativa da classificação:** o problema **já ocorreu** (quatro incompatibilidades ao rodar o
  `TodoListControllerTest`, conforme README), mas a base atual está estável e funcional; novas
  ocorrências concentram-se em mudanças de dependência — probabilidade média. Impacto médio, pois é
  resolvível com esforço pontual.

### R06 — Credenciais de banco em texto plano versionadas
- **Descrição:** Usuário e senha do banco estão embutidos e versionados no `pom.xml` (perfil `desenv`).
- **Possíveis impactos no projeto:** exposição de segredo a quem tem acesso ao repositório; dificuldade
  de separar ambientes; potencial comprometimento do banco se as credenciais forem reutilizadas ou o
  repositório tornar-se público.
- **Fatores que influenciam a ocorrência:** ausência de externalização de configuração (variáveis de
  ambiente/secret manager); segredo já presente no histórico de versão. *Incerteza:* gravidade depende
  de o repositório ser público e de as credenciais serem reutilizadas fora de desenvolvimento.
- **Probabilidade:** Alta
- **Impacto:** Médio
- **Nível de exposição:** Alto
- **Justificativa da classificação:** a exposição do segredo é um **fato já consumado** no versionamento
  — probabilidade alta. Impacto classificado como médio no cenário local com credenciais triviais
  (`postgres/pactodb`), mas **poderia escalar para Alto** se houver reuso ou repositório público.

### R07 — Ausência de pipeline de Integração Contínua (CI)
- **Descrição:** Não há verificação automatizada a cada alteração; a execução dos testes depende de
  cada desenvolvedor rodá-los localmente.
- **Possíveis impactos no projeto:** integração quebrada percebida tardiamente; regressões passando
  apesar dos testes existentes; qualidade dependente de disciplina individual; retrabalho de integração.
- **Fatores que influenciam a ocorrência:** inexistência de `.github/workflows`; **equipe distribuída**;
  dependência do Makefile local, agravada pela não determinística execução do R03.
- **Probabilidade:** Alta
- **Impacto:** Médio
- **Nível de exposição:** Alto
- **Justificativa da classificação:** sem CI, a não execução automática é uma condição permanente e a
  chance de uma regressão escapar em equipe distribuída é alta. Impacto médio porque os testes existem
  (61) e podem ser rodados localmente — o problema é a detecção tardia, não a ausência total de
  verificação.

### R08 — Endpoints de listagem sem paginação
- **Descrição:** `read-all` e `all/audit` retornam todos os registros de uma vez, sem `Pageable`.
- **Possíveis impactos no projeto:** aumento de tempo de resposta, uso de memória e tráfego conforme os
  dados crescem; degradação mais acentuada nos endpoints de auditoria, que crescem a cada operação.
- **Fatores que influenciam a ocorrência:** volume de tarefas e, sobretudo, de revisões do Envers;
  ausência de `Pageable` no repositório e no controller. *Incerteza:* em projeto acadêmico o volume
  tende a ser baixo, reduzindo a probabilidade real de degradação perceptível.
- **Probabilidade:** Média
- **Impacto:** Médio
- **Nível de exposição:** Médio
- **Justificativa da classificação:** a ocorrência depende do crescimento do volume — provável no
  histórico de auditoria, incerta no volume acadêmico geral, daí probabilidade média. Impacto médio:
  degrada desempenho sem quebrar a funcionalidade de imediato.

### R09 — Coordenação de equipe distribuída sobre configuração local
- **Descrição:** A dependência de configuração local não padronizada, somada à distribuição da equipe,
  amplia divergências de ambiente e falhas de integração por diferenças de setup.
- **Possíveis impactos no projeto:** retrabalho por comportamento inconsistente entre máquinas; atrito
  de comunicação/coordenação; onboarding mais lento; propagação dos efeitos de R03, R06 e R07.
- **Fatores que influenciam a ocorrência:** configuração acoplada ao ambiente local (R03); ausência de
  contêiner/ambiente padronizado; ausência de CI comum (R07). *Incerteza:* o tamanho e a maturidade
  reais da equipe (projeto acadêmico) não estão explícitos e afetam a probabilidade.
- **Probabilidade:** Média
- **Impacto:** Médio
- **Nível de exposição:** Médio
- **Justificativa da classificação:** sem padronização de ambiente, divergências são plausíveis, mas a
  probabilidade depende do tamanho efetivo da equipe (incerto) — média. Impacto médio: gera retrabalho
  gerenciável, sem comprometer o produto em si.

### R10 — Acoplamento entre requisitos em evolução e domínios fixos em código
- **Descrição:** `Priority` e `Category` são enums fixos; mudanças no domínio exigem alteração de código
  e potencial migração de dados, convivendo com valores antigos na auditoria.
- **Possíveis impactos no projeto:** recompilação e migração a cada mudança de domínio; inconsistência
  entre valores novos e revisões antigas na tabela `todolist_audit`; risco de violação de constraint
  (ligado a R04).
- **Fatores que influenciam a ocorrência:** **requisitos em evolução**; modelagem por enum em vez de
  tabela de referência; ausência de migração versionada (R04). *Incerteza:* frequência esperada de
  mudança nesses conjuntos de valores não está definida.
- **Probabilidade:** Média
- **Impacto:** Médio
- **Nível de exposição:** Médio
- **Justificativa da classificação:** já houve uma alteração (tradução para pt-BR), e requisitos em
  evolução podem trazer novas — mas a frequência é incerta, daí média. Impacto médio: contornável, com
  atrito de recompilação/migração e coerência histórica.

### R11 — Banco de dados abaixo da versão mínima suportada pelo ORM
- **Descrição:** PostgreSQL 9.6.24 em uso, abaixo do mínimo recomendado (13) pelo Hibernate 7;
  operação via workaround, com aviso `HHH000511`.
- **Possíveis impactos no projeto:** comportamento não homologado do ORM; defeitos sutis de
  compatibilidade; bloqueio ao usar recursos que assumam versões mais novas; esforço de migração de
  banco no futuro.
- **Fatores que influenciam a ocorrência:** divergência declarada entre versão do banco e mínimo do
  Hibernate; workaround já necessário (`GenerationType.SEQUENCE`). *Incerteza:* uso local pode nunca
  exercitar recursos incompatíveis, mantendo o efeito latente.
- **Probabilidade:** Alta
- **Impacto:** Médio
- **Nível de exposição:** Alto
- **Justificativa da classificação:** a condição de incompatibilidade é **permanente e já presente**
  (aviso emitido, workaround aplicado) — probabilidade alta. Impacto médio porque a aplicação opera
  hoje; a severidade **escalaria** caso um recurso não suportado passe a ser necessário.

### R12 — Ausência de autenticação e autorização
- **Descrição:** Todos os endpoints são públicos, sem controle de acesso ou identificação de usuário.
- **Possíveis impactos no projeto:** criação, alteração e exclusão de tarefas por qualquer cliente;
  ausência de rastreabilidade de autoria; inviabilidade de uso fora de ambiente isolado sem retrabalho
  significativo (integração de Spring Security/JWT).
- **Fatores que influenciam a ocorrência:** ausência de `spring-boot-starter-security`; endpoints sem
  restrição. *Incerteza:* a materialização de acesso indevido depende da **exposição de rede** — baixa
  em ambiente local acadêmico, alta se a API for publicada.
- **Probabilidade:** Média
- **Impacto:** Alto
- **Nível de exposição:** Alto
- **Justificativa da classificação:** a vulnerabilidade é certa, mas a ocorrência de acesso indevido
  depende de exposição além do localhost — probabilidade média (condicional), sinalizada como incerta.
  Impacto alto: acesso irrestrito a todas as operações de dados, incluindo exclusão.

### R13 — Base de código integralmente gerada por IA
- **Descrição:** Todo o código foi gerado por assistente de IA (Claude Sonnet 4.6), com risco de
  decisões subótimas ou defeitos sutis passarem sem revisão humana sistemática.
- **Possíveis impactos no projeto:** manutenção dificultada por padrões não plenamente compreendidos
  pela equipe; defeitos sutis não cobertos por testes; conhecimento de design concentrado nos prompts.
- **Fatores que influenciam a ocorrência:** ausência (não confirmada) de revisão par-a-par; equipe
  distribuída; interação com R01/R02/R07 (lacunas de validação, tratamento de erro e CI). *Incerteza:*
  existência e rigor de revisão humana do código gerado não estão documentados.
- **Probabilidade:** Média
- **Impacto:** Médio
- **Nível de exposição:** Médio
- **Justificativa da classificação:** há 61 testes e o código aparenta solidez, o que reduz a chance de
  defeitos grosseiros, mas decisões subótimas em código extenso são plausíveis — média. Impacto médio,
  concentrado em qualidade e manutenibilidade, não em falha imediata.

## 4. Resumo

| ID | Risco | Probabilidade | Impacto | Nível de exposição |
|----|-------|---------------|---------|--------------------|
| R04 | Evolução de schema sem ferramenta de migração | Alta | Alto | **Crítico** |
| R01 | Ausência de validação de entrada | Alta | Médio | **Alto** |
| R02 | Ausência de tratamento global de exceções | Alta | Médio | **Alto** |
| R06 | Credenciais de banco versionadas | Alta | Médio | **Alto** |
| R07 | Ausência de pipeline de CI | Alta | Médio | **Alto** |
| R11 | Banco abaixo da versão mínima do ORM | Alta | Médio | **Alto** |
| R12 | Ausência de autenticação e autorização | Média | Alto | **Alto** |
| R03 | Acoplamento ao profile `desenv` | Média | Médio | Médio |
| R05 | Framework recém-lançado (Spring Boot 4) | Média | Médio | Médio |
| R08 | Listagens sem paginação | Média | Médio | Médio |
| R09 | Coordenação de equipe distribuída | Média | Médio | Médio |
| R10 | Requisitos em evolução vs. enums fixos | Média | Médio | Médio |
| R13 | Base de código gerada por IA | Média | Médio | Médio |

**Priorização:** o risco de nível **Crítico** é o **R04** (migração de schema), único que combina alta
probabilidade e alto impacto e que já se materializou — deve ser o foco prioritário das respostas. Em
seguida, os riscos de nível **Alto** (R01, R02, R06, R07, R11, R12) concentram-se em robustez da API,
segurança e verificação. Os riscos de nível **Médio** (R03, R05, R08, R09, R10, R13) são relevantes
para reprodutibilidade, escalabilidade e manutenção, comportando tratamento planejado. As estratégias
de resposta serão detalhadas em `riscos/respostas.md`.

## 5. Matriz Qualitativa de Riscos

Distribuição dos 13 riscos por **probabilidade × impacto** (cada célula lista os IDs correspondentes):

| Probabilidade\Impacto | Baixo | Médio | Alto |
| ----------------------- | ----- | ----- | ---- |
| **Alta** | — | R01, R02, R06, R07, R11 | R04 |
| **Média** | — | R03, R05, R08, R09, R10, R13 | R12 |
| **Baixa** | — | — | — |

> **Leitura da matriz:** a concentração na faixa **Alta/Médio** (cinco riscos) e o único caso
> **Alta/Alto** (R04) indicam que a maior parte da exposição vem de condições **já presentes e muito
> prováveis**, porém de impacto majoritariamente moderado. Não há riscos classificados como
> improváveis (Baixa) nesta análise, pois quase todos decorrem de condições estruturais já observáveis
> no repositório; e nenhum risco foi avaliado com impacto Baixo, dado que todos afetam ao menos
> qualidade, segurança ou continuidade de forma não trivial.

## 6. Observações sobre incertezas

Os itens abaixo têm classificação **condicionada a informação não confirmada** e devem ser revalidados
ao longo do projeto (herdados da seção 5 de `riscos/identificacao.md`):

| Risco | Incerteza | Efeito potencial na classificação |
|-------|-----------|-----------------------------------|
| R03 | Falha real de `make test` sem `-P desenv` | Confirmar elevaria a probabilidade; refutar a reduziria a Baixa. |
| R06 | Repositório público e/ou reuso das credenciais | Se confirmado, o impacto sobe de Médio para Alto (exposição Crítico). |
| R08 | Volume real de dados/auditoria | Volume baixo reduz a probabilidade; alto crescimento a eleva. |
| R09 | Tamanho e maturidade reais da equipe | Equipe mínima/co-localizada reduziria a probabilidade. |
| R11 | Necessidade futura de recursos do ORM não suportados | Elevaria o impacto de Médio para Alto. |
| R12 | Exposição da API além do localhost | Publicação elevaria a probabilidade de Média para Alta (exposição Crítico). |
| R13 | Existência de revisão humana do código gerado | Revisão sistemática reduziria a probabilidade. |
