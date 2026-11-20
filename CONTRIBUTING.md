# Contributing to QSauron

Thanks for your interest in improving QSauron! This project is part of the QSauron ecosystem.

## Getting set up

```bash
cd reper
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## Workflow

1. Open an issue describing the bug or feature (use the templates).
2. Fork the repository and create a branch: `git checkout -b feature/short-description`.
3. Make your change, keeping it focused and consistent with the surrounding code style.
4. Run the tests: `mvn test`
5. Commit using short, imperative messages (e.g. `fix: handle empty circuit`) and open a pull request.

## Reporting security issues

Do not open a public issue for vulnerabilities or leaked credentials. Contact the maintainers privately instead.

## License

By contributing you agree that your contributions are licensed under the MIT License.
