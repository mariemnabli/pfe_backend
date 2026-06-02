# PlantUML Diagrams

Main diagrams:

- [telecom-class-diagram.puml](/home/iba-conseil002/pfe_backend/docs/diagrams/telecom-class-diagram.puml)
- [assistant-ai-sequence-diagram.puml](/home/iba-conseil002/pfe_backend/docs/diagrams/assistant-ai-sequence-diagram.puml)

## Generate locally

If `plantuml` is installed:

```bash
plantuml docs/diagrams/telecom-class-diagram.puml
plantuml docs/diagrams/assistant-ai-sequence-diagram.puml
```

If you have the PlantUML jar:

```bash
java -jar plantuml.jar docs/diagrams/telecom-class-diagram.puml
java -jar plantuml.jar docs/diagrams/assistant-ai-sequence-diagram.puml
```

This will generate a `.png` or `.svg` depending on your PlantUML setup.
