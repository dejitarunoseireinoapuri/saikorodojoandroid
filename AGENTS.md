# AGENTS.md — Repository Guidelines (SaikoroDojo)

This repository contains an **Android** project.  
**All agents and contributors must read this file before making any code changes.**  
If any instruction conflicts with other documents, **this file takes precedence**.

This is a **game project** with **animations**. Animations are required, but they must be **as lightweight as possible** to minimize CPU/GPU usage, allocations, recompositions, and battery drain.

---

## Language Policy (Mandatory)
- **All code comments must be written in English.**
- **All commit messages must be written in English.**
- Prefer clear, concise wording; avoid unnecessary commentary.

---

## Architecture (Mandatory): Clean Architecture + MVVM (Strict)

This project must follow **Clean Architecture** with **MVVM** at all times.  
Any new code must respect layer boundaries and be designed for **low resource usage** (CPU, memory, battery).

### Layers and dependencies
- **presentation**: UI (Jetpack Compose), ViewModels, UI state/events/effects.
- **domain**: pure Kotlin business logic (entities, use cases, repository interfaces). **No Android dependencies.**
- **data**: implementations of repositories, data sources (local/remote), mappers, DTOs.

**Dependency rule (strict):**
- `presentation -> domain`
- `data -> domain`
- `domain -> (nothing)`

Never import `data` from `presentation`. Never use Android framework types in `domain`.

### Clean boundaries (non-negotiable)
- Do not leak DTOs, database entities, or network models into `domain` or `presentation`.
- Use mappers at the boundary (data <-> domain).
- Keep `domain` deterministic and side-effect free when possible.

---

## Project Structure & Module Organization
- `app/` is the main Android application module.
- Source code lives under `app/src/main/java/com/dejitarunoseireinoapuri/saikorodojo/` (Kotlin).
- UI theme setup is in `app/src/main/java/com/dejitarunoseireinoapuri/saikorodojo/ui/theme/`.
- Resources (strings, themes, drawables, launchers) are in `app/src/main/res/`.
- Unit tests go in `app/src/test/`, instrumented tests in `app/src/androidTest/`.

### Package conventions (recommended)
Prefer **feature-first** organization:
- `.../feature/<featureName>/presentation/...`
- `.../feature/<featureName>/domain/...`
- `.../feature/<featureName>/data/...`

---

## MVVM & UI State Conventions
Use unidirectional data flow:
- `UiState` (data class)
- `UiEvent` (sealed interface/class) from UI to ViewModel
- `UiEffect` (sealed interface/class) for one-off effects (navigation, snackbars, etc.)

ViewModels should expose:
- `val uiState: StateFlow<UiState>`
- `fun onEvent(event: UiEvent)`

In Compose, prefer `collectAsStateWithLifecycle()` for observing state.

---

## Async & Streams (Coroutines/Flow)
- Async work must use **Kotlin Coroutines** (no raw threads, no unmanaged timers).
- UI state exposed from ViewModels should use **StateFlow**.
- Use **Flow** for streams (e.g., game ticks, state updates, repository observers).
- Collect flows in Compose using `collectAsStateWithLifecycle()`.
- No `GlobalScope`; use structured concurrency (`viewModelScope`) and cancelable jobs.
- Avoid tight loops; prefer `delay()` and bounded-frequency updates to reduce battery usage.
- Inject/centralize dispatchers for testability (no hardcoded `Dispatchers.IO` in domain).

---

## Testing Policy (Mandatory): Tests for Every Functionality Change
**Every new function or behavior change must include unit tests** that prove correct behavior and prevent regressions.

Minimum expectations:
- **domain**: pure unit tests (fast, no Android).
- **data**: unit tests using fakes/mocks for data sources.
- **presentation**: ViewModel unit tests; Compose UI tests for key user flows when applicable.

Guidelines:
- Cover success + failure paths.
- Add regression tests for every fixed bug.
- Prefer deterministic tests; avoid reliance on real time, network, or filesystem (use fakes).
- Test naming: `*Test.kt` matching the class under test.

---

## Performance & Resource Usage Rules (Mandatory)
Design everything to minimize recompositions, allocations, and background work.  
Because this is a game, **animations must be present but as lightweight as possible**.

### Animations (Mandatory constraints)
- Prefer **simple transforms** (`offset`, `translation`, `scale`, `alpha`, `rotation` via `graphicsLayer`) instead of layout re-measure/re-layout.
- Avoid per-frame allocations and heavy computations inside animation blocks.
- Keep the number of simultaneously animated properties small and bounded.
- Avoid high-frequency timers; drive animation with Compose animation APIs and state, not manual loops.
- Keep asset sizes reasonable; avoid huge bitmaps; prefer vector assets when appropriate.
- If an animation is optional, provide a reduced-motion or low-power alternative when feasible.

### Compose
- Avoid heavy work in Composables (I/O, parsing, random generation, file operations).
- Prefer stable state holders: `@Immutable` / `@Stable` where appropriate.
- Use `remember` and `derivedStateOf` to avoid recomputation.
- Collect flows with `collectAsStateWithLifecycle()`.
- Every screen must respect system bar insets (status/navigation bars) via `Modifier.systemBarsPadding()`.

### Data & background work
- Prefer local caching when appropriate.
- Avoid frequent polling; prefer push-based updates when possible.
- Any periodic work should be batched and scheduled responsibly (WorkManager if needed).

---

## Build, Test, and Development Commands
Don’t compile the app or run the tests yourself, as the user will run them.

---

## Coding Style & Naming Conventions
- Kotlin with Jetpack Compose; 4-space indentation.
- Classes/objects: `PascalCase` (e.g., `MainActivity`).
- Functions/variables: `camelCase`.
- Composables: `PascalCase` (e.g., `GameScreen`).
- UI types: `SomethingUiState`, `SomethingUiEvent`, `SomethingUiEffect`.
- Use Android Studio formatter.

---

## Commit & Pull Request Guidelines
- Use clear, imperative commit messages in **English** (e.g., "Add dice roll animation", "Refactor to Clean Architecture").
- PRs should include a concise summary, linked issues if applicable, and screenshots/GIFs for UI changes.
- For changes affecting animations/background work, include a brief note on performance/resource impact.

---

## Security & Configuration
- `local.properties` contains local SDK paths; do not commit secrets.
- Keep API keys and tokens out of `gradle.properties` and source files; use environment variables or local config.

---

## Change Checklist (Must Follow)
Before opening a PR or pushing changes:
1. Confirm you have read **AGENTS.md**.
2. Verify Clean Architecture boundaries are preserved.
3. Add/Update unit tests for the change.
4. Run: `./gradlew test` and fix failures.
5. Run: `./gradlew lint` (and UI tests if relevant).
6. Confirm animations remain lightweight (no heavy per-frame work, bounded properties).
