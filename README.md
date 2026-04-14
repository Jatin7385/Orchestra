# Orchestra

**Orchestra** is a configuration-driven **BFF (Backend for Frontend)** and API orchestration layer for **Adobe Experience Manager**. It turns “servlet sprawl” into one shared entry point, pluggable `ApiService` / `ValidationService` / `APIAdapterService` implementations, and OSGi-driven wiring so teams can ship in parallel without duplicating integration code.

> **From chaos to control** — eliminate AEM servlet sprawl and enable parallel team development on shared enterprise APIs.

**Disclaimer:** Orchestra is **not** an Adobe official product, release, or endorsed offering. It is independent open-source solution that interoperates with Adobe Experience Manager and the broader AEM ecosystem. Adobe and Adobe Experience Manager are trademarks of Adobe Inc.


The full architecture story, design decisions, and rationale live in **[BLOG](https://medium.com/@jatin.dhall7385/orchestra-a-configuration-driven-api-orchestration-framework-5db72b468977)**. This README is the **project overview** and **how to run and extend** the codebase.

---

## Why Orchestra exists

When many product teams (loans, cards, accounts, and so on) all call the same core APIs, each team often ships its own servlet: same HTTP/FDM plumbing, different validation and response shapes. That produces:

- Duplicated integration and fragile copy-paste “helpers”
- Different JSON shapes per team → duplicated frontend fragments (AEM, React, etc.)
- Every API contract change multiplied across *n* servlets
- Harder observability and inconsistent error handling

Orchestra **separates orthogonal concerns**:

| Layer | Responsibility |
|--------|----------------|
| **GenericServlet** | HTTP contract, request validation, errors, response envelope |
| **APIOrchestrationService** | Reads OSGi config, sequences APIs, merges results, trace id |
| **ApiService** | One external integration per named service (HTTP, FDM bridge, etc.) |
| **ValidationService** | Business rules for that call in that journey/scenario |
| **APIAdapterService** | Map validated API JSON to a stable schema for clients |

Services are registered in the OSGi registry **by name**; the orchestrator resolves them at **runtime** from configuration, which supports hot bundle deploys and team-owned bundles without rewiring a monolithic servlet.

---

## Repository layout

| Module | Role |
|--------|------|
| **core** | OSGi bundle: `GenericServlet`, orchestration, interfaces, default adapter, sample services |
| **ui.apps** | AEM apps and clientlibs |
| **ui.config** | OSGi configs (e.g. orchestration maps) |
| **ui.content** | Sample content |
| **all** | Composite package for install |
| **it.tests** / **ui.tests** | Integration and UI tests |

Key Java packages (core):

- `com.aem.orchestration.core.servlets` — `GenericServlet`
- `com.aem.orchestration.core.services.impl` — `APIOrchestrationServiceImpl`, config service
- `com.aem.orchestration.core.services.apiService` — `ApiService` + samples
- `com.aem.orchestration.core.services.validation` — `ValidationService` + samples
- `com.aem.orchestration.core.services.apiAdapterService` — `APIAdapterService` + default adapter

---

## Request and response (this repo)

**POST** entry (see `GenericServlet`): resource type `orchestration/api/generic`, extension `json`.

Request body must include `requestString.context` (with `journeyName`, `journeyID`, `scenario`, `apiName`) and `requestString.payload`. The servlet flattens context + payload into `parametersJson` for downstream services.

Orchestration returns a container with:

- **`data`** — merged **adapted** output from each step in the configured chain  
- **`original`** — per–`ApiService` name, the **validated** response before adapter merge semantics are applied for that step

Later steps receive **`preProcessPayload`** = the **merged `data` object so far** (flat merge of adapted keys from previous steps). Design adapters so keys needed downstream appear at the top level of `data`, or document your own nesting convention.

---

## Configuration (OSGi)

Defined in `APIOrchestrationConfig` and parsed by `APIOrchestrationConfigServiceImpl`:

| Concept | Format (illustrative) |
|---------|------------------------|
| API chain | `JOURNEY_NAME\|SCENARIO=apiOne\|apiTwo\|apiThree` |
| Validation per API | `JOURNEY_NAME\|SCENARIO\|apiServiceName=validationServiceName` |
| Adapter (optional) | `JOURNEY_NAME\|SCENARIO=adapterServiceName` |
| Response schema | `JOURNEY_NAME={...}` mapping output keys → JSON paths |

`ApiService` / `ValidationService` / `APIAdapterService` implementations must expose an OSGi `name` property that matches these strings.

---

## Build and test

```bash
# Build entire reactor
mvn clean install

# Core bundle only + unit tests
mvn clean install -pl core
```

Deploy to a local AEM author instance using the profiles defined in the root `pom.xml` (for example `autoInstallBundle` / `autoInstallPackage` as documented in the Adobe AEM project pattern).

---

## Extending Orchestra

1. Implement **`ApiService`** with `@Component(service = ApiService.class, property = "name=your-api")`.
2. Implement **`ValidationService`** with a matching `name` and wire it in the validation config map.
3. Optionally implement **`APIAdapterService`** or rely on **`default-adapter-service`** and journey response schema config.
4. Add or update OSGi configuration strings; no change to `GenericServlet` or `APIOrchestrationServiceImpl` is required for new flows if services already follow the contracts.

Sample implementations live under `...apiService.impl` and `...validation.impl` as a starting point.

---

## When to use Orchestra

**Strong fit:** several teams, shared backends, different validation or shaping per journey, frequent rule changes, need for consistent logging and a single HTTP entry.

**Poor fit:** one team, one simple proxy, rarely changing rules — a plain servlet may be enough. Start simple; adopt orchestration when servlet sprawl hurts.

---

## Documentation

| Document | Contents |
|----------|-----------|
| **[BLOG](https://medium.com/@jatin.dhall7385/orchestra-a-configuration-driven-api-orchestration-framework-5db72b468977)** | Full narrative: problem, BFF architecture, patterns (SRP, DIP, strategy, adapter, registry), frontend duplication, configuration-as-code, trade-offs |

*Orchestra — configuration-driven API orchestration for AEM.*
