# 🧠 ClinicalMind - Domain Model & Business Rules

## 1. Visão Geral do Domínio
Este documento descreve as entidades e regras de negócio do sistema ClinicalMind. 
**Diretriz para a IA:** O objetivo é implementar DDD (Domain-Driven Design) com Modelos Ricos. O domínio deve validar suas próprias regras (invariantes) e ser independente de frameworks de persistência.

---

## 2. Entidade: Paciente (Aggregate Root)
**Responsabilidade:** Representa o indivíduo em tratamento.

### Atributos:
- `id`: UUID (Primary Key)
- `nome`: String (Obrigatório)
- `cpf`: String (Único, Validado)
- `data_nascimento`: LocalDate (Obrigatório)
- `email`: String (Válido e Único)
- `telefone`: String
- `responsavel_id`: UUID (Opcional, obrigatório se menor de 18 anos)
- `ativo`: boolean (Default: true)

### Regras de Negócio (Invariantes):
1. **Validação de Maioridade:** Se o paciente tiver menos de 18 anos no momento do cadastro, o sistema deve exigir o vínculo de um `responsavel_id`.
2. **Validação de CPF:** A entidade deve validar o algoritmo do CPF antes da criação.
3. **Soft Delete:** A exclusão física é proibida se houver consultas vinculadas. O método `desativar()` deve ser usado.
4. **Imutabilidade de Identidade:** O CPF e o ID não podem ser alterados após a criação.

---

## 3. Entidade: Psicólogo (Aggregate Root)
**Responsabilidade:** O profissional que realiza os atendimentos.

### Atributos:
- `id`: UUID
- `nome`: String
- `crp`: String (Registro Profissional Único)
- `especialidade`: Enum (TCC, Psicanálise, Fenomenologia, etc.)
- `valor_sessao_base`: BigDecimal

### Regras de Negócio:
1. **Integridade Profissional:** O CRP é obrigatório e deve seguir o padrão regional (ex: 06/123456).
2. **Consistência Financeira:** O valor da sessão base não pode ser negativo.

---

## 4. Entidade: Consulta / Sessão (Aggregate Root)
**Responsabilidade:** Gerenciar o agendamento e o status do encontro clínico.

### Atributos:
- `id`: UUID
- `paciente_id`: UUID
- `psicologo_id`: UUID
- `data_hora_inicio`: LocalDateTime
- `data_hora_fim`: LocalDateTime
- `status`: Enum (AGENDADA, REALIZADA, CANCELADA, AUSENCIA)
- `valor_aplicado`: BigDecimal

### Regras de Negócio:
1. **Conflito de Horário:** Não é permitido agendar uma consulta para um psicólogo se ele já possuir outra consulta cujo intervalo de tempo se sobreponha.
2. **Antecedência Mínima:** Consultas só podem ser agendadas com no mínimo 1 hora de antecedência do horário atual.
3. **Fluxo de Status:**
   - O status só pode mudar para `REALIZADA` após o horário de início ter passado.
   - Uma consulta `CANCELADA` não pode voltar ao status `AGENDADA`.
4. **Duração Padrão:** Por padrão, uma sessão dura 50 minutos, a menos que especificado o contrário.

---

## 5. Entidade: Evolução Clínica (Sensitive Data)
**Responsabilidade:** Registro técnico de cada sessão (Prontuário).

### Regras de Negócio:
1. **LGPD & Privacidade:** O texto da evolução é sigiloso. Apenas o psicólogo vinculado à consulta pode ler ou editar.
2. **Imutabilidade Pós-Assinatura:** Após 24 horas da criação, a evolução deve ser considerada "fechada" e não pode mais ser editada, apenas retificada.

---

## 6. Orientações de Implementação para IA (Java 24 + JdbcClient)

Ao gerar o código seguindo este documento:

1. **Evite Anemia:** Não gere apenas Getters e Setters. As validações de negócio (ex: idade do paciente, conflito de horário) devem estar dentro da classe de domínio ou em um Domain Service.
2. **Uso de Records:** Use `record` para representar os dados vindos do `JdbcClient`, mas converta-os para a classe de domínio rica para aplicar a lógica.
3. **Persistence-Agnostic:** O Domínio não deve ter anotações de banco de dados. O mapeamento SQL (`JdbcClient`) deve ficar na camada de `Infrastructure`.
4. **Tratamento de Erros:** Lance exceções de domínio (ex: `BusinessException`) quando uma regra for violada.