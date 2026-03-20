# 💉 Dependency Injection & Architectural Service Glue

This package contains the **Hilt** (Dagger-based) dependency injection modules that orchestrate the lifecycle and provision of the application's core services. It acts as the "Architectural Glue" that connects the data persistence layer (Room) with the business logic (Repositories) and the presentation layer (ViewModels).

## 🏛️ Inversion of Control (IoC) Strategy

The application utilizes a hierarchical DI strategy to ensure that components are modular, testable, and adhere to the **Single Responsibility Principle**:

1.  **`DatabaseModule.kt`**: The Foundation.
    -   **Role**: Manages the instantiation of the [AppDatabase](../data/AppDatabase.kt) and provides individual Data Access Objects (DAOs).
    -   **Scoping**: The database is provided as a `@Singleton` to ensure that there is only one active connection to the SQLite file, preventing data corruption and ensuring that Room's `InvalidationTracker` remains consistent across the app.
2.  **`RepositoryModule.kt`**: The Abstraction.
    -   **Role**: Provides unified repository instances (e.g., [StudentRepository](../data/StudentRepository.kt)) that encapsulate multiple DAOs.
    -   **Scoping**: Repositories are scoped as `@Singleton`. This is critical for maintaining the "Single Source of Truth" pattern, allowing different ViewModels to observe the same reactive data streams without redundant database queries.

## ⚡ Performance & Reactive Consistency

The DI layer is optimized to support the application's **BOLT** (Performance-Obsessed) philosophy:

-   **Lazy Instantiation**: Hilt ensures that heavy components (like the database) are only initialized when first requested.
-   **Thread Safety**: By managing singletons via DI, we avoid the pitfalls of manual double-checked locking and ensure that state-heavy components (like repositories with in-memory caches) are safely shared across background dispatchers and the UI thread.
-   **Reactive Integrity**: Scoping repositories as singletons ensures that `Flow` and `LiveData` streams are shared across the application. When one ViewModel triggers a database update, all other ViewModels observing the same repository will receive the update simultaneously.

## 🛠️ Usage in the UI Layer

ViewModels utilize the `@HiltViewModel` annotation to automatically receive injected dependencies. For example:

```kotlin
@HiltViewModel
class SeatingChartViewModel @Inject constructor(
    private val repository: StudentRepository,
    // ... other DAOs or services
) : ViewModel() { ... }
```

This pattern ensures that the UI layer remains completely agnostic of how data is fetched or persisted, facilitating easier unit testing and future architectural pivots (e.g., migrating from Room to a different storage engine).

---
*Documentation love letter from Scribe 📜*
