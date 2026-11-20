# Data model

Persistence uses JPA/Hibernate on MySQL (schema `qsauron`). Entities live in `reper/src/main/java/edu/uclm/reper/model/`.

```mermaid
erDiagram
    USER }o--o{ PROJECT : "user_projects"
    USER ||--o{ EXPERIMENT : owns
    PROJECT ||--o| Q_PROGRAM : has
    PROJECT ||--o{ MUTANT_CYCLE : has
    PROJECT ||--o{ TEST_SUITE : has
    Q_PROGRAM ||--o| Q_CIRCUIT : quirkCircuit
    Q_PROGRAM ||--o| GENERATOR : generator
    EXPERIMENT ||--o| EXEC_CONFIG : uses
    EXPERIMENT ||--o{ MUTANT_CYCLE : produces
    MUTANT_CYCLE ||--o{ MUTANT : contains
    MUTANT_CYCLE ||--o| EXEC_CONFIG : configured_by
    EXEC_CONFIG ||--o| KILLING_MATRIX : result
    OPERATOR_TYPE ||--o{ OPERATOR : groups
    OPERATOR ||--o{ MUTANT : applied_in
    MUTANT ||--o{ MUTANT_RESULT : has
    TEST_SUITE ||--o{ TEST_CASE : contains
    TEST_CASE ||--o{ MUTANT_RESULT : evaluated_in
    GENERATOR {
        string type "GRENOBLE, MATRIX, GENETIC, GROVER, BLOCKS, EDITOR"
    }
    EXPERIMENT {
        string status "RUNNING, COMPLETED, FAILED, CANCELLED"
    }
```

The relations above are derived from the JPA annotations; check the entity classes for cascade and fetch details. `MutantCycle` and `MutantResult` use composite identifiers.

## Initial data

`Operadores.sql` (repository root) seeds `qsauron.operator_types` (families: Initialization errors, Control Gates, Swap Gates, Position errors, Other operators, Phase Gates) and `qsauron.operators` (name, description, enabled flag, family). Run it once on a fresh database, after Hibernate has created the tables:

```bash
mysql -u <user> -p qsauron < Operadores.sql
```

`plantuml_export.puml` (repository root) is a PlantUML class diagram of the generator hierarchy and related entities.
