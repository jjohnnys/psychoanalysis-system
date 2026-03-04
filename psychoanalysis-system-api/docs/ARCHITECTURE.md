# 🏥 ClinicalMind - Backend Architecture Blueprint

## 1. Core Stack (Modern Java 2026)
* **JDK:** Java 24 (Utilizar Virtual Threads, Records e Unnamed Patterns).
* **Framework:** Spring Boot 3.4+ (Latest Stable).
* **Persistence:** PostgreSQL + Spring Boot Starter JDBC (Uso de JdbcClient).
* **Migration:** Flyway (Versionamento de banco de dados).
* **Docs:** SpringDoc OpenAPI (Swagger) UI.
* **AI Engine:** Spring AI (Dependências pré-configuradas).

## 2. Estrutura de Pacotes (Clean Architecture / DDD)
- `com.jjohnnys.psa.domain`: Modelos de domínio (Rich Domain Models), Enums e Business Rules.
- `com.jjohnnys.psa.application`: Services (Use Cases) e interfaces de entrada/saída.
- `com.jjohnnys.psa.infrastructure`: Implementações de Repositórios (JdbcClient), Configurações, Segurança e Spring AI.
- `com.jjohnnys.psa.api`: Controllers, DTOs (Records) e Exception Handlers.

## 3. Persistência & Dados (Pure JDBC)
* **Engine:** Spring Boot `JdbcClient`.
* **Domain Model:** PROIBIDO o uso de Entidades Anêmicas. O domínio deve ser o "Single Source of Truth", contendo validações de negócio e métodos que garantam a consistência do objeto.
* **Mapeamento:** Usar `DataClassRowMapper` para Records ou mapeamento manual via `RowMapper` para lógica complexa.
* **Imutabilidade:** As classes de domínio devem ser Records ou classes imutáveis que retornam novas instâncias ao sofrer alterações de estado.

## 4. Regras de Ouro (Senior Guidelines)
* **Performance:** Habilitar Virtual Threads no `application.properties` (`spring.threads.virtual.enabled=true`).
* **Clean Code:** Métodos pequenos, nomes em inglês, lógica clara.
* **Error Handling:** Implementar `ProblemDetail` (RFC 7807) para respostas de erro padronizadas.
* **Validation:** Uso rigoroso de Bean Validation (`jakarta.validation`) nos DTOs de entrada.

## 5. Persistência & Auditoria
* **Nomenclatura:** Tabelas e colunas em `snake_case`.
* **Auditoria:** Toda tabela deve ter campos `created_at` e `updated_at`.
* **Migration:** Proibido `ddl-auto`. Uso obrigatório de scripts SQL do Flyway.

## 6. Roadmap Spring AI
> **Instrução para a IA:** O projeto deve estar preparado para o Spring AI. Incluir o `spring-ai-bom` no Maven e criar uma estrutura base na camada de `infrastructure` para futuras implementações de Chat e Embedding.