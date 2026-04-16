# 🚀 ClinicalMind - Application & API Layer Patterns

## 1. Camada Application (Use Cases / Services)
**Localização:** `com.jjohnnys.psa.application`

### Diretrizes de Implementação:
- **Orquestração:** O Use Case deve buscar a entidade no Repository, chamar os métodos de lógica de negócio da própria Entidade e salvar o resultado.
- **Transacionalidade:** Use `@Transactional` (do Spring) nos métodos de Use Case para garantir a atomicidade das operações com `JdbcClient`.
- **Independência:** Esta camada não deve conhecer detalhes de HTTP ou de Banco de Dados SQL. Ela lida com objetos de domínio e interfaces de repositório.
- **Input/Output:** Use **Records** específicos para entrada (Command/Request) e saída (DTO/Response).

---

## 2. Camada API (REST Controllers)
**Localização:** `com.jjohnnys.psa.api`

### Diretrizes de Implementação:
- **Responsabilidade Única:** O Controller apenas valida o JSON de entrada (`@Valid`), chama o Use Case e mapeia o resultado para o Status HTTP correto.
- **Mapping:** Use as anotações padrão do Spring Boot 3.4 (`@RestController`, `@GetMapping`, etc.).
- **Response:** Sempre retorne `ResponseEntity<T>`. Para erros, utilize o padrão `ProblemDetail` definido no `ARCHITECTURE.md`.
- **Versionamento:** Use o prefixo `/api/v1/` em todos os endpoints.

---

## 3. Padrão de DTOs (Data Transfer Objects)
- Todos os DTOs devem ser **Java Records**.
- **Request DTOs:** Devem conter as anotações de Bean Validation (`@NotBlank`, `@CPF`, `@Positive`, etc.).
- **Response DTOs:** Devem ser imutáveis e projetados para o que o cliente (Frontend/Mobile) realmente precisa ver.

---

## 4. Exemplo de Fluxo (Referência para a IA)
**Cenário: Cadastro de Paciente**
1. **Controller:** Recebe `CadastroPacienteRequest` (Record).
2. **UseCase:** - Converte o Request para o objeto de domínio rico `Paciente`.
   - O construtor do `Paciente` valida as regras (ex: maioridade).
   - Chama `pacienteRepository.save(paciente)`.
   - Retorna um `PacienteResponse` (Record).
3. **Controller:** Retorna `201 Created` com o corpo do `PacienteResponse`.

---

## 5. Instruções Adicionais para a IA
> **Atenção IA:** > 1. Ao implementar os Use Cases, certifique-se de que NÃO existam regras de negócio "vazando" do domínio para o serviço. O serviço apenas coordena.
> 2. Utilize as **Virtual Threads** nativas do Java 24 para processamentos que exijam IO pesado ou chamadas externas futuras.
> 3. Mantenha os nomes dos métodos claros e declarativos (ex: `registrarNovoPaciente`, `agendarConsulta`).