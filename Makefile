.DEFAULT_GOAL := help

install:
	./mvnw dependency:resolve

run:
	./mvnw spring-boot:run -P desenv

test:
	./mvnw test

test-service:
	./mvnw test -Dtest="TodoListServiceTest"

test-controller:
	./mvnw test -Dtest="TodoListControllerTest"

package:
	./mvnw clean package -P desenv -DskipTests

help:
	@echo "Targets disponíveis:"
	@echo "  install          Baixa todas as dependências do projeto"
	@echo "  run              Inicia a aplicação (perfil desenv, porta 8030)"
	@echo "  test             Executa todos os testes (61 testes)"
	@echo "  test-service     Executa apenas TodoListServiceTest (36 testes)"
	@echo "  test-controller  Executa apenas TodoListControllerTest (25 testes)"
	@echo "  package          Gera o JAR em target/ (sem rodar testes)"

.PHONY: install run test test-service test-controller package help
