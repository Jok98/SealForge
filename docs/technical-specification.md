# SealForge Technical Specification

## 1. High-level architecture summary

SealForge should ship as a desktop-only, local-first JavaFX application with a layered architecture:

- `ui`: JavaFX views, form models, presentation controllers, and user feedback
- `application`: use-case orchestration, DTO mapping, and output-safe workflow coordination
- `domain`: validation rules, domain entities, and policy decisions
- `infrastructure`: kubeseal execution, YAML rendering, file export, clipboard integration, and PEM parsing

The MVP keeps all secret authoring local to the workstation. Plaintext secret values live only in active UI state and in-memory request flow. Secret YAML is streamed to `kubeseal` over stdin to avoid plaintext temp files. The only temp-file use in the current design is for the public certificate when the user pastes PEM content, because `kubeseal --cert` expects a file path or URL.

## 2. Detailed module breakdown

### UI

- builds a keyboard-friendly workspace rather than a wizard-only flow
- separates secret input from generated output
- treats certificate material as inspectable metadata
- shows validation feedback without dumping stack traces into the main UI

### Application

- maps UI form state to `SecretDraft`
- coordinates the sequence: load certificate -> validate draft -> render Secret YAML -> seal -> validate -> copy/export
- centralizes use-case boundaries to keep infrastructure replaceable later

### Domain

- owns Kubernetes name validation and duplicate-key checks
- expresses sealing scope semantics explicitly
- keeps the secret draft immutable once created for a use-case run

### Infrastructure

- calls `kubeseal` through `ProcessBuilder`
- renders deterministic YAML with Jackson YAML
- parses PEM certificates to surface fingerprint, subject, issuer, and validity dates
- wraps file export and clipboard access behind ports

## 3. Main screens and UX flow

### Recommended flow

1. Home
2. New Secret workspace
3. Preview and validation
4. Export or copy
5. Reset or start another secret

### MVP screen model

- Home: `New Secret`, `Validate SealedSecret`, `Settings`, `About`
- Certificate screen/panel: paste PEM, load PEM file, inspect fingerprint, show parse status
- Secret editor: name, namespace, type, scope, dynamic key/value rows
- Preview: Secret YAML tab, SealedSecret YAML tab, validation panel
- Diagnostics: friendly error text plus expandable technical details

### UX rules

- default scope is `strict`
- `cluster-wide` must show a warning because it is the most permissive option
- copy/export actions must be explicit buttons, never automatic
- reset must clearly warn that in-memory values will be cleared
- certificate status and kubeseal status should always be visible

## 4. Domain model

### Core entities

- `SecretDraft`
  - `name`
  - `namespace`
  - `type`
  - `scope`
  - `entries`
- `SecretEntry`
  - `key`
  - `value`
- `CertificateReference`
  - `sourceType`
  - `sourceDescription`
  - `fingerprint`
  - `subject`
  - `issuer`
  - `notBefore`
  - `notAfter`
  - `pemContent`
- `GeneratedYaml`
  - `plainSecretYaml`
  - `sealedSecretYaml`
- `ValidationResult`
  - `success`
  - `message`
  - `details`
- `SealingScope`
  - `STRICT`
  - `NAMESPACE_WIDE`
  - `CLUSTER_WIDE`

### Validation policy

- secret name: required, Kubernetes DNS subdomain
- namespace: required, Kubernetes DNS subdomain
- type: required, arbitrary Kubernetes secret type string
- entries: at least one
- key: non-empty, unique, preserved exactly
- value: empty string allowed in MVP because Kubernetes `stringData` permits it

## 5. Class/package structure

```text
com.sealforge
├── app
│   ├── AppLauncher
│   ├── AppContext
│   ├── ApplicationBootstrap
│   └── MainApplication
├── application
│   ├── dto
│   ├── service
│   └── usecase
├── config
│   └── AppConfig
├── domain
│   ├── enumtype
│   ├── exception
│   ├── model
│   └── validation
├── infrastructure
│   ├── certificate
│   ├── clipboard
│   ├── file
│   ├── kubeseal
│   └── yaml
└── ui
    ├── component
    ├── controller
    ├── model
    └── view
```

### Recommended additions after MVP

- `application.port` if port count grows
- `ui.controller` split by screen once the single workspace becomes multiple navigable scenes
- `config.SettingsRepository` when non-sensitive preferences are persisted

## 6. Use case flow end-to-end

1. User pastes or loads a PEM certificate.
2. `LoadCertificateUseCase` parses it and returns `CertificateReference`.
3. User enters metadata and secret entries.
4. `CreateSecretDraftUseCase` maps UI DTOs to `SecretDraft`.
5. `GenerateYamlUseCase` validates the draft and renders Secret YAML with `stringData`.
6. `SealSecretUseCase` streams the Secret YAML into `kubeseal` stdin and captures SealedSecret YAML from stdout.
7. `ValidateSealedSecretUseCase` runs `kubeseal --validate` against the generated SealedSecret.
8. `CopyYamlToClipboardUseCase` or `ExportYamlUseCase` executes only on explicit user action.
9. `ResetDraftUseCase` clears the in-memory form and resets the UI.

## 7. Security considerations

### Security defaults

- never persist plaintext secret drafts by default
- never log secret values
- never send telemetry
- never autosave secret content
- never keep plaintext on disk unless the user explicitly exports it

### In-memory handling

- secret values live only in JavaFX form state and short-lived use-case calls
- reset clears visible controls and backing models
- no crash report pipeline should include form payloads

### Process execution

- Secret YAML should be sent to `kubeseal` through stdin
- stdout and stderr must be captured separately
- stderr should be scrubbed before it is surfaced to end users

### Certificate handling

- certificates are public but still tracked with source and fingerprint
- pasted certificates are acceptable; the adapter can create a short-lived temp PEM file only because `kubeseal --cert` expects a path
- future certificate profile management should persist only certificate metadata and PEM content, never secret data

### Clipboard and export

- copy requires explicit click
- export requires explicit path selection
- warning text should remind users that clipboard and plain Secret files may be observable outside the app

## 8. Error-handling strategy

### Error classes

- `ValidationException`: invalid user input
- `CertificateParseException`: PEM could not be parsed
- `KubesealUnavailableException`: binary missing or not executable
- `KubesealExecutionException`: kubeseal returned a non-zero exit or interrupted
- `TechnicalFailureException`: file I/O and serialization failures

### UI strategy

- show concise user-facing error text in an alert or inline status
- write advanced details into an expandable diagnostics panel
- never dump stack traces into the primary workflow

### Parsing strategy

- normalize `kubeseal` stderr into short messages
- preserve raw stderr only in technical details
- distinguish offline authoring failure from validation-environment failure

### Important product nuance

Official documentation shows validation through `kubeseal --validate` on a SealedSecret resource. The design should treat validation as environment-dependent, because sealing works offline with a certificate but validation may depend on the caller's Kubernetes context and controller reachability. That is an implementation inference from the CLI behavior and should be documented clearly in the UI.

## 9. Testing strategy

### Unit tests

- `SecretDraftValidatorTest`
- `KubernetesNameValidatorTest`
- `JacksonSecretYamlSerializerTest`
- `PemCertificateParserTest`
- `KubesealCommandFactoryTest`

### Adapter tests

- fake-process integration test for `KubesealProcessAdapter`
- optional real-binary integration profile for teams that have `kubeseal` installed in CI or locally

### UI tests

- smoke tests for entry-row add/remove behavior
- form reset clears sensitive values
- preview actions are disabled or guarded when output is empty

### Test data rules

- use synthetic names and values only
- use generated self-signed public test certificates
- never check in real kubeconfig files or real public cluster certs unless intentionally public fixtures

## 10. MVP backlog

1. Certificate paste and file load with parsing feedback
2. Secret metadata form with Kubernetes-aware validation
3. Dynamic key/value entries with masked values
4. Deterministic Secret YAML generation
5. kubeseal-based SealedSecret generation through stdin
6. Validation using `kubeseal --validate`
7. Copy and export actions
8. Safe reset flow
9. Settings for kubeseal binary path
10. Basic About and disclaimer views

## 11. Future roadmap

### Near-term

- fetch certificate via `kubeseal --fetch-cert`
- kubeconfig-aware validation screen
- labels and annotations
- import existing Secret YAML
- import `.env`

### Medium-term

- merge into existing SealedSecret
- raw mode per-field sealing
- certificate profile management
- stale certificate warnings based on age or mismatch metadata

### Long-term

- multi-secret workspace
- templates
- batch mode
- backend abstraction for SOPS or External Secrets manifest generation

## 12. Suggested project skeleton with sample classes/interfaces

### Entry points

- `MainApplication`
- `ApplicationBootstrap`
- `MainController`
- `MainView`

### Ports

- `CertificateParser`
- `SecretYamlRenderer`
- `KubesealGateway`
- `FileExportService`
- `SystemClipboardService`

### Use cases

- `LoadCertificateUseCase`
- `CreateSecretDraftUseCase`
- `GenerateYamlUseCase`
- `SealSecretUseCase`
- `ValidateSealedSecretUseCase`
- `ExportYamlUseCase`
- `CopyYamlToClipboardUseCase`
- `ResetDraftUseCase`

### Adapters

- `PemCertificateParser`
- `JacksonSecretYamlSerializer`
- `KubesealCommandFactory`
- `KubesealProcessAdapter`
- `LocalFileExportAdapter`
- `JavaFxClipboardAdapter`

## 13. Maven module or single-module recommendation

Use a single Maven module for the MVP.

Reasons:

- the codebase is small enough that multi-module structure adds friction without real isolation benefits
- JavaFX packaging and runtime-image configuration stay simpler
- test discovery is easier while the architecture is still moving

When to split later:

- when a reusable non-UI core emerges
- when CLI or library reuse becomes a product goal
- when packaging or plugin extension points create genuinely separate deployable artifacts

## 14. Example YAML input/output

### Plain Secret YAML

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: docker-credentials
  namespace: payments
type: kubernetes.io/dockerconfigjson
stringData:
  .dockerconfigjson: |
    {"auths":{"registry.example.com":{"username":"demo","password":"demo-password"}}}
```

### Representative SealedSecret YAML

```yaml
apiVersion: bitnami.com/v1alpha1
kind: SealedSecret
metadata:
  name: docker-credentials
  namespace: payments
spec:
  encryptedData:
    .dockerconfigjson: AgB4jQExampleEncryptedPayloadOnly
  template:
    metadata:
      name: docker-credentials
      namespace: payments
    type: kubernetes.io/dockerconfigjson
```

## 15. Example kubeseal adapter design

### Seal command

```text
kubeseal --cert /tmp/sealforge-cert-123.pem --scope strict
```

stdin:

- plain Secret YAML

stdout:

- generated SealedSecret YAML

stderr:

- parsed into a short user-facing message plus preserved technical details

### Validate command

```text
kubeseal --validate
```

stdin:

- SealedSecret YAML

### Design notes

- no plaintext secret temp files
- public certificate temp file only when necessary
- command construction is isolated in `KubesealCommandFactory`
- process execution is isolated in `KubesealProcessAdapter`
- future support for `--merge-into`, `--raw`, and `--fetch-cert` can extend the gateway without breaking the domain model

## 16. GitHub project structure

```text
.
├── .github
│   └── workflows
│       ├── build.yml
│       └── release.yml
├── docs
│   ├── images
│   └── technical-specification.md
├── src
│   ├── main
│   │   └── java
│   └── test
│       ├── java
│       └── resources
├── CONTRIBUTING.md
├── LICENSE
├── README.md
├── SECURITY.md
└── pom.xml
```

## 17. Release/build strategy

### Local build

- `mvn test`
- `mvn javafx:run`
- `mvn javafx:jlink`

### CI build

- GitHub Actions matrix on Linux, Windows, macOS
- Java 21 only
- `mvn -B verify`

### Release packaging

- tagged builds create platform-specific runtime images
- native installers are produced through `jpackage`
- artifacts are uploaded per OS
- release notes should include kubeseal installation guidance and the security disclaimer

### Public release checklist

- README with screenshots
- architecture doc
- SECURITY.md
- CONTRIBUTING.md
- issue templates
- signed release artifacts if the project maturity justifies it

## External references

- Sealed Secrets project: https://github.com/bitnami-labs/sealed-secrets
- `kubeseal --validate` usage appears in the project README: https://github.com/bitnami-labs/sealed-secrets#usage
