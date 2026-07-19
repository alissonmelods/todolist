# Relatório de Situação do Projeto — Todolist (API de Gerenciamento de Tarefas)

**Para:** Patrocinador, orientação e stakeholders do projeto
**De:** Gerência do Projeto
**Data:** 19/07/2026
**Assunto:** Situação atual, riscos do projeto e decisões necessárias

---

## Resumo executivo

O projeto está **funcional e bem coberto por testes** (61 testes automatizados): a API já cria, lista,
atualiza, conclui, exclui e audita tarefas. A base foi construída de forma organizada e documentada.
No entanto, o levantamento de riscos revelou **um ponto que já causou problema uma vez** — a forma como
o banco de dados é atualizado — e **um conjunto de fragilidades estruturais** (segurança, validação de
dados e verificação automática) que são toleráveis enquanto o projeto roda **apenas em ambiente local de
desenvolvimento**, mas que se tornam **impeditivas caso o sistema evolua para uso real**.

Em resumo: **não há crise em curso**, mas há decisões a tomar **agora**, enquanto são baratas de
resolver. A decisão mais importante — que orienta quase todas as demais — é definir **para onde este
projeto vai**: permanece como exercício acadêmico/local, ou caminha para uso real? **Há quatro pontos
aguardando o posicionamento de vocês, destacados na seção 4.**

---

## 1. Contexto: onde o projeto está

O Todolist é uma API para gerenciamento de tarefas, com trilha de auditoria completa (todo o histórico
de alterações é registrado). Foi desenvolvido como projeto acadêmico, usando tecnologias atuais (Java 17,
Spring Boot, PostgreSQL) e com o código gerado com apoio de inteligência artificial.

O que já funciona está sólido e testado. A análise de riscos que conduzimos — em três etapas
(identificação, análise e estratégias de resposta) — não aponta falhas no que foi entregue, e sim
**condições estruturais** que decidem se o projeto está pronto apenas para demonstração ou também para
uso real. É sobre essas condições que este relatório trata.

## 2. O que está em risco

Organizamos os **13 riscos** levantados em quatro temas. Cada tema indica o nível de atenção.

**a) Atualização do banco de dados — atenção máxima (já ocorreu uma vez).**
Hoje o banco é atualizado automaticamente pelo próprio sistema, sem um controle de versões das mudanças.
Durante o desenvolvimento, uma alteração simples (tradução de valores para português) **já provocou uma
falha** que exigiu recriar a tabela manualmente. Como os requisitos ainda estão evoluindo, esse tipo de
mudança tende a se repetir. **Risco:** sem um mecanismo adequado de migração, cada mudança de modelo pode
causar inconsistência ou perda de dados — hoje sem consequência (dados descartáveis), mas grave se houver
dados que se queira preservar. *(Referência técnica: R04, R10, R11.)*

**b) Segurança — aceitável só enquanto o uso for local.**
Dois pontos: (1) as senhas de acesso ao banco estão **escritas diretamente no código versionado**, e
(2) a API **não tem autenticação** — qualquer pessoa com acesso à rede poderia criar, alterar ou excluir
tarefas. Enquanto o sistema roda isolado no computador de desenvolvimento, o risco é baixo. **Risco:** no
momento em que a API for exposta a uma rede ou à internet, esses dois pontos passam de "aceitáveis" a
"críticos" e precisariam ser resolvidos **antes** da exposição. *(Referência técnica: R06, R12.)*

**c) Robustez e verificação — reduzem retrabalho e surpresas.**
A API ainda **não valida os dados recebidos** (um pedido malformado gera um erro genérico de servidor em
vez de uma mensagem clara) e **não há verificação automática a cada alteração do código** (os testes
existem, mas dependem de cada pessoa rodá-los manualmente). **Risco:** com a equipe distribuída, uma
falha introduzida por um membro pode só ser percebida tarde, gerando retrabalho. *(Referência técnica:
R01, R02, R07, R08.)*

**d) Ambiente e forma de trabalho — pontos de fricção.**
A configuração para rodar o projeto depende de um ajuste específico que não está uniforme entre a
documentação e os atalhos de execução, o que pode gerar o clássico "funciona só na minha máquina". Somam-se
a isso o uso de uma versão muito recente do framework (menos suporte da comunidade, já causou ajustes) e o
fato de o código ter sido gerado por IA, o que pede revisão humana. **Risco:** atrito de coordenação e
pequenas perdas de tempo, sem comprometer o produto. *(Referência técnica: R03, R05, R09, R13.)*

> **Como ler isto:** o tema **(a)** é o único de nível crítico e já se manifestou. Os temas **(b)** e
> **(c)** são a "lista do que fazer antes de qualquer uso real". O tema **(d)** é melhoria contínua.

## 3. Ações já em andamento e recomendadas

O levantamento de riscos já mapeou, para cada ponto, caminhos de tratamento (evitar, mitigar, transferir
ou aceitar). Entre as ações de maior retorno e menor custo, recomendamos priorizar:

1. **Adotar controle de versões do banco** (ferramenta de migração como Flyway/Liquibase) e desligar a
   atualização automática — resolve na raiz o risco de maior atenção (tema **a**).
2. **Ativar validação de dados e padronização de erros** na API — poucas mudanças, grande ganho de
   robustez e clareza (tema **c**).
3. **Configurar verificação automática (CI)** que rode os testes a cada alteração — protege a equipe
   distribuída contra regressões (tema **c**).
4. **Retirar as senhas do código** e movê-las para configuração externa — passo simples que remove a
   exposição desnecessária (tema **b**).

Essas ações **reduzem** os riscos, mas várias dependem de uma definição que está fora da alçada técnica —
o destino do projeto — detalhada a seguir.

## 4. Decisões necessárias — onde precisamos de vocês

> **Decisão 1 — Qual é o destino do projeto: acadêmico/local ou uso real?**
> Esta é a decisão central, porque muda o tratamento de quase tudo. Se o projeto **permanece como
> exercício local**, os riscos de segurança (tema b) podem ser formalmente **aceitos**, e o foco fica na
> qualidade. Se **caminha para uso real**, segurança e atualização do banco tornam-se **obrigatórias
> antes de qualquer publicação**.
> **Precisamos de:** a definição do destino pretendido do projeto.

> **Decisão 2 — Investir agora no controle de versões do banco (tema a)?**
> É o risco que já se materializou. Recomendamos tratá-lo independentemente do destino do projeto, porque
> o custo é baixo e evita novas quebras à medida que os requisitos evoluem.
> **Precisamos de:** aval para priorizar essa ação na próxima etapa de desenvolvimento.

> **Decisão 3 — Nível de rigor de qualidade desejado (tema c)?**
> Verificação automática (CI), validação de dados e revisão humana do código gerado por IA elevam a
> qualidade, mas consomem esforço da equipe. Precisamos saber o **nível de rigor esperado** para dimensionar
> esse investimento.
> **Precisamos de:** direcionamento sobre quanto esforço dedicar a qualidade/verificação nesta fase.

> **Decisão 4 — Confirmação de premissas de contexto.**
> Algumas classificações de risco dependem de informações que não temos confirmadas e que **só os
> stakeholders podem validar**: o repositório é público? As senhas do banco são reutilizadas em outro
> lugar? Qual o volume de dados esperado? Qual o tamanho real da equipe? Há previsão de atualizar o banco?
> **Precisamos de:** respostas a esses pontos para calibrar o plano final (lista completa no rodapé técnico).

## 5. Próximos passos

| O quê | Quando |
|-------|--------|
| Posicionamento sobre o destino do projeto (Decisão 1) | Próxima reunião de acompanhamento |
| Iniciar o controle de versões do banco, se aprovado (Decisão 2) | Início da próxima etapa de desenvolvimento |
| Confirmar as premissas de contexto pendentes (Decisão 4) | Próximos dias |
| Apresentar plano de tratamento priorizado, conforme decisões | Após as definições acima |
| Próximo relatório de situação como este | A combinar |

---

## Mensagem final

O projeto está saudável e entregou uma base funcional e testada — este relatório **não comunica um
problema em andamento**, e sim antecipa decisões enquanto elas ainda são baratas. O único risco que já se
manifestou (atualização do banco) tem solução conhecida e de baixo custo; os demais são, na prática, a
resposta a uma única pergunta: **até onde este projeto pretende ir?** Definido isso, conseguimos apresentar
um plano de tratamento proporcional e honesto — nem sobre-engenharia para um exercício acadêmico, nem
fragilidade para um sistema de uso real.

Estamos à disposição para conversar individualmente com qualquer stakeholder antes da próxima reunião.

---

*Detalhamento técnico completo dos riscos, da análise qualitativa e das estratégias de resposta disponível
em: [identificação](../riscos/identificacao.md) · [análise](../riscos/analise.md) · [estratégias de
resposta](../riscos/respostas.md).*

*Premissas pendentes de validação (Decisão 4), por risco: repositório público / reuso de senhas (R06);
volume de dados e auditoria esperado (R08); tamanho e distribuição real da equipe (R09); previsão de
atualização do PostgreSQL (R11); exposição da API além do ambiente local (R12); existência de revisão
humana do código gerado por IA (R13); confirmação da falha de configuração de testes (R03).*
