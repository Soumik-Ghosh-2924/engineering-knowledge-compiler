# Engineering Knowledge Compiler — Project Context

This file is the durable handoff from the ChatGPT project **Business Planning - Bootstrapped**. It records the decisions and working context that should guide future development. The linked conversations remain the detailed historical source; the repository and its tests remain the source of truth for the current implementation.

## Source conversations

1. [Product Development Roadmap](https://chatgpt.com/share/6a8b0259-4e74-83ee-8f9a-5ae76e90d388)
2. [MVP Development Strategy - DAY 1](https://chatgpt.com/share/6a8b02b7-c1b0-83ee-8e7e-75ae9af8acaf)
3. [Engineering Intelligence MVP](https://chatgpt.com/share/6a8b02f5-88b0-83e8-a213-36af5fe19d06)
4. [MVP Control Center](https://chatgpt.com/share/6a8b0308-dfac-83ee-8c6e-957f541bda83)
5. [Day-2 Goals and Objectives](https://chatgpt.com/share/6a8b0319-e57c-83ee-822b-b0d928c0f7ff)

## Product direction

The project is evolving from generic developer productivity or engineering intelligence toward **Engineering Decision Intelligence**.

The central problem is that engineering facts are fragmented across source control, CI/CD, incident systems, project trackers, documentation, and organizational knowledge. Existing tools own individual systems of record, but no system owns the cross-tool reasoning behind an engineering event or decision.

The intended product should:

- reduce engineering decision latency;
- reconstruct engineering events such as releases, incidents, rollbacks, and sprint outcomes;
- preserve evidence and reasoning, not only final states;
- provide role-appropriate views of the same underlying event, from individual contributor to CTO;
- compile repositories and related engineering evidence into an explainable knowledge model;
- produce insights that remain traceable to their source evidence.

The long-term vision is broad, but the product must begin with one high-frequency, high-value engineering decision. Candidate wedges discussed include whether to merge, deploy, or roll back; why production failed; why delivery slowed; who owns a system; and whether a service can be removed safely. The exact first wedge remains an open product decision and should be validated before expanding scope.

## MVP mission and principles

MVP goal:

> Compile engineering repositories into an explainable engineering knowledge graph that can support evidence-backed insights and decisions.

Working principles:

- Build the compiler first; applications consume compiler outputs.
- Optimize technology choices for learning and delivery velocity.
- Keep each compiler stage responsible for one transformation.
- Use immutable artifacts at stage boundaries.
- Isolate third-party implementation details such as JGit and JavaParser.
- Avoid premature infrastructure and features that do not validate the core hypothesis.
- Treat the code, automated tests, and accepted engineering decision records as authoritative when old conversation details conflict with the implemented system.

## Locked technology choices for MVP v0.1

- Repository: `engineering-knowledge-compiler`
- Architecture: modular monolith
- Backend: Oracle OpenJDK 23, Spring Boot 3.x, Maven
- Java compatibility posture: prefer stable language features; avoid preview/incubator dependencies unless justified
- Git integration: JGit behind an internal abstraction
- Source parser: JavaParser, isolated from downstream domain stages
- Graph database direction: Neo4j
- Frontend: React and TypeScript
- Testing: JUnit 5 and Mockito on the backend
- Local backend port established during Day 1: `6040`

## Compiler architecture

The compiler is a staged pipeline coordinated by a `CompilerEngine`. REST controllers should invoke the engine, not individually orchestrate compiler stages.

```text
Compile request
    -> Repository acquisition
    -> Source discovery
    -> AST parsing
    -> AST extraction
    -> Relationship analysis
    -> Knowledge graph
    -> Reasoning/query capabilities
```

The shared immutable `CompilerContext` carries the accumulated artifacts:

```text
CompilerContext
├── RepositoryMetadata
├── RepositorySource
├── RepositoryAst
└── RepositoryStructure
```

Stage outputs:

| Stage | Artifact | Meaning |
| --- | --- | --- |
| Repository acquisition | `RepositoryMetadata` | Validated local repository identity and location |
| Source discovery | `RepositorySource` | Discovered source files |
| AST parsing | `RepositoryAst` | Parser-backed syntax trees associated with source files |
| AST extraction | `RepositoryStructure` | Compiler-owned structural model, independent of JavaParser |

`RepositoryStructure` contains `ExtractedSourceFile` objects. Each extracted file links its `ParsedSourceFile` to a compiler-owned `ParsedCompilationUnit`. Downstream relationship analysis consumes `RepositoryStructure` and must not depend directly on JavaParser.

## Day-1 outcome — repository acquisition

Day 1 produced the first working end-to-end compiler stage:

```text
REST request
    -> request validation
    -> workspace resolution
    -> clone/fetch through JGit
    -> metadata extraction
    -> CompilerContext
```

Established APIs included:

- `GET /health`
- `POST /api/v1/compiler/compile`

Workspace configuration was designed around a user-level directory rather than cloning analyzed repositories inside the source checkout. The proposed hierarchy was `${user.home}/.ekc/workspace` with `repositories`, `temporary`, and potentially `cache` and `output` subdirectories.

`RepositoryLoadResult` was intentionally removed because cloned/updated status is an operational detail rather than a durable compiler-domain concept. The repository acquisition abstraction guarantees that a repository exists at its resolved path or raises an acquisition exception.

## Day-2 outcome — repository understanding

Day 2 added:

- recursive Java source discovery;
- source discovery policy and repository source model;
- JavaParser-based AST parsing;
- parser and extracted structural domain models;
- AST extraction into `RepositoryStructure`;
- immutable compiler context propagation across stages;
- clean separation between parser artifacts and downstream compiler-owned artifacts.

A temporary controller-level orchestration was used to validate the full flow before introducing `CompilerEngine`. The intended durable design is for `CompilerEngine` to own the sequence while the controller remains small.

An observed correctness issue remains important historical context: for some Java files, logged extraction results appeared to differ from the actual source. The agreed debugging method is to reproduce one mismatch and trace it through:

```text
Source file
    -> CompilationUnit
    -> ParsedCompilationUnit
    -> RepositoryStructure
    -> output/logging
```

Check nested types, multiple top-level types, records, enums, discovery/file association, and whether the reporting layer is displaying the correct artifact. Fix the first stage at which divergence occurs rather than patching output symptoms.

## Frontend and MVP control-center intent

The frontend direction includes an interactive MVP control center and, as the product matures, an operational interface for the compiler. Discussed control-center capabilities include:

- overall MVP progress and current capability;
- a 10-day capability roadmap;
- a calendar with manually recorded completed, partial, or failed days;
- daily objectives and milestone history;
- an engineering journal;
- compiler and API health;
- later integration with compilation jobs, graph statistics, and reasoning results;
- persistent state without requiring manual source-code edits.

The active frontend in this repository is the implementation source of truth. Historical mockups and plans should inform intent but must not override working code or current requirements.

## Deployment context

The application is deployed locally to Minikube through Argo CD using the separate deployment repository at `/Users/g.soumik/IdeaProjects/prj-ekc`.

Known operational context from subsequent implementation work:

- backend and frontend are represented as Kubernetes services managed through Argo CD;
- the frontend image is `dockersg6/engineering-knowledge-compiler-ui` with environment-specific tags such as `dev`;
- the public host is `ekc.sgsafesurf.com`;
- ingress routing and a Cloudflare Tunnel expose the local Minikube application;
- local availability depends on Minikube, the Kubernetes/Argo CD workloads, ingress reachability or port forwarding, and the Cloudflare Tunnel process being active;
- image tags referenced by deployment manifests must exist in the registry and be publicly pullable or backed by an appropriate Kubernetes image-pull secret.

Always inspect the current manifests in `prj-ekc` before changing deployment behavior because the repository—not this summary—defines the live desired state.

## Current open decisions

- Select and validate the first narrow, high-value engineering decision wedge.
- Validate and harden AST extraction correctness before relying on relationship analysis.
- Keep the compiler architecture aligned with implemented behavior as later graph and reasoning stages are added.
- Evolve the frontend from control-center and compilation workflow into role-specific, evidence-backed decision views.
- Keep operational runbooks synchronized with the actual Argo CD, Minikube, ingress, image registry, and Cloudflare configuration.

## How future Codex tasks should use this file

At the beginning of project work:

1. Read this file for historical intent and accepted direction.
2. Inspect the current code and tests in this repository.
3. Inspect `/Users/g.soumik/IdeaProjects/prj-ekc` for deployment truth when the task concerns Kubernetes, Argo CD, ingress, container images, or Cloudflare.
4. Call out any conflict between historical intent and current implementation instead of silently choosing one.
5. Update this document only when a product or architectural decision materially changes.
