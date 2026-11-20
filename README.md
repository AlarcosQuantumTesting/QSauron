<p align="center">
  <img src="reper/docs/images/banner.png" alt="QSauron banner" width="720"/>
</p>

# QSauron

> Mutation testing for quantum programs: generate mutants of a circuit, execute them and find out which ones your tests can kill.

![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.2-50816f?style=for-the-badge&logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-50816f?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-JPA-50816f?style=for-the-badge&logo=mysql&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-50816f?style=for-the-badge)

## What is in this repository

| Path | Description |
|---|---|
| [`reper/`](reper/README.md) | Spring Boot backend (API, persistence, experiment orchestration) |
| [`Operadores.sql`](Operadores.sql) | Seed data for mutation operators and their families |
| [`plantuml_export.puml`](plantuml_export.puml) | PlantUML class diagram of generators and related entities |
| [`reper/docs/`](reper/docs) | API reference, data model and generator message formats |

Start with the [reper README](reper/README.md).

## QSauron ecosystem

```mermaid
flowchart LR
    FE["QSauron Frontend<br/>Angular 19"] -->|REST| REPER["reper<br/>Spring Boot API :8080"]
    REPER --> DB[("MySQL")]
    REPER --> QG["qumugen<br/>mutant generation :8500"]
    REPER --> QE["qiskitexec<br/>mutant execution :8502"]
    REPER --> QC["q-convert-api<br/>format conversion"]
    REPER --> MA["mutant_analytics<br/>statistics :8503"]
    REPER --> MAIL["Brevo<br/>e-mail"]
    QE --> RR["remote_runner<br/>Flask :8081"]
```

Ports are the defaults found in the code and may differ per deployment.

| Repository | Role |
|---|---|
| [QSauron (this repo)](https://github.com/AlarcosQuantumTesting/QSauron) | `reper` API and persistence |
| [QSauron Frontend](https://github.com/SamRo28/QSauron) | Angular 19 web client |
| [Qumuclient](https://github.com/SamRo28/Qumuclient) | Original UI plus `qumugen` and `qiskitexec` |
| [q-convert-api](https://github.com/SamRo28/q-convert-api) | REST wrapper around the `q-convert` CLI |
| [QuCo `remote_runner`](https://github.com/AlarcosQuantumTesting/QuCo) | Remote execution of Qiskit/Cirq programs |

## License

[MIT](LICENSE)
