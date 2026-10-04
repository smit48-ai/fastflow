# FastFlow

FastFlow is a lightweight Java concurrency library focused on making concurrent execution easier to use, safer to manage, and simpler to integrate into normal application code.

The library currently uses **Java Virtual Threads** to execute independent tasks concurrently while keeping the API close to ordinary synchronous Java.

The goal is to let developers benefit from concurrency without having to repeatedly deal with low-level primitives such as `ExecutorService`, `Future`, thread management, cancellation, and failure coordination.

---

## Why FastFlow?

Concurrent programming in Java is powerful, but application code can quickly become difficult to maintain when developers have to manually manage:

- Executors
- Futures
- Task submission
- Result collection
- Exception handling
- Cancellation
- Thread lifecycle
- Concurrency limits
- Timeouts

FastFlow aims to provide a higher-level abstraction around these concerns.

Instead of writing:

```java
try (ExecutorService executor =
        Executors.newVirtualThreadPerTaskExecutor()) {

Future<Customer> customer =
        executor.submit(customerService::getCustomer);

Future<Account> account =
        executor.submit(accountService::getAccount);

Customer customerResult = customer.get();
Account accountResult = account.get();
}
```

FastFlow aims to provide a simpler API:

```java
FastFlowResult result =
        FastFlow.create()
                .addTask(
                        "customer",
                        customerService::getCustomer
                )
                .addTask(
                        "account",
                        accountService::getAccount
                )
                .execute();

Customer customer =
        result.get("customer");

Account account =
        result.get("account");
```

Each independent task is executed concurrently using a virtual thread.

---

# Current Features

The current MVP focuses on a small set of core capabilities.

### Parallel Task Execution

Multiple independent `Callable` tasks can be registered and executed concurrently.

```java
FastFlow.create()
        .addTask("task1", service::callOne)
        .addTask("task2", service::callTwo)
        .addTask("task3", service::callThree)
        .execute();
```

Each task runs on its own Java Virtual Thread.

---

### Fail-Fast Execution

FastFlow processes tasks in **completion order rather than submission order**.

If one task fails, FastFlow can detect the failure without waiting for an earlier submitted long-running task to finish.

For example:

```text
Task A ───────────────────── 10 seconds
Task B ── fails after 200 ms
Task C ─────────────── 5 seconds
```

FastFlow detects the failure from Task B after approximately 200 ms and attempts to cancel unfinished tasks.

```text
Task A ─────── cancelled
Task B ── FAILED
Task C ─────── cancelled
```

The original failure is wrapped inside a `FastFlowExecutionException`.

---

### Simplified Exception Handling

Instead of exposing implementation-specific exceptions such as `ExecutionException`, FastFlow provides a library-specific exception.

```java
try {

    FastFlow.create()
            .addTask("customer", customerService::getCustomer)
            .addTask("account", accountService::getAccount)
            .execute();

} catch (FastFlowExecutionException exception) {

    System.out.println(
            "Failed task: " +
            exception.getFailedTask()
    );

    Throwable originalFailure =
            exception.getCause();
}
```

---

# Requirements

FastFlow currently targets:

```text
Java 21+
```

Java 21 is the minimum supported version because Virtual Threads became a standard Java feature starting with Java 21.

---

# Installation

FastFlow is currently under development and is not yet published to Maven Central.

For now, clone the repository and build it locally.

```bash
git clone https://github.com/smit48-ai/fastflow.git
cd fastflow
```

Build:

```bash
./gradlew build
```

On Windows:

```powershell
gradlew.bat build
```

The generated JAR will be available under:

```text
build/libs/
```

Once FastFlow is published to a public Maven repository, installation will look similar to:

### Gradle

```groovy
dependencies {
    implementation "io.github.smit48ai.parallel:fastflow:<version>"
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.smit48ai.parallel</groupId>
    <artifactId>fastflow</artifactId>
    <version>VERSION</version>
</dependency>
```

---

# Example Use Case

Consider a REST API that builds a customer dashboard.

The application needs to independently fetch:

```text
Customer details       120 ms
Accounts               250 ms
Transactions           300 ms
Offers                 150 ms
```

Running these sequentially could take approximately:

```text
120 + 250 + 300 + 150

≈ 820 ms
```

Since these operations are independent, FastFlow can execute them concurrently.

```java
FastFlowResult result =
        FastFlow.create()

                .addTask(
                        "customer",
                        () -> customerService.getCustomer(customerId)
                )

                .addTask(
                        "accounts",
                        () -> accountService.getAccounts(customerId)
                )

                .addTask(
                        "transactions",
                        () -> transactionService
                                .getTransactions(customerId)
                )

                .addTask(
                        "offers",
                        () -> offerService.getOffers(customerId)
                )

                .execute();
```

Results can then be retrieved:

```java
Customer customer =
        result.get("customer");

List<Account> accounts =
        result.get("accounts");

List<Transaction> transactions =
        result.get("transactions");

List<Offer> offers =
        result.get("offers");
```

Conceptually:

```text
                        Request
                           |
                           v
                       FastFlow
                           |
          +----------------+----------------+
          |                |                |
          v                v                v
      Customer          Accounts       Transactions       Offers
      Virtual           Virtual          Virtual          Virtual
       Thread             Thread           Thread           Thread
          |                |                |                |
          +----------------+----------------+----------------+
                           |
                           v
                         Join
                           |
                           v
                        Result
```

The overall execution time becomes closer to the duration of the slowest task rather than the sum of all task durations.

---

# Broad Goal

The broader goal of FastFlow is:

> **Make concurrency easy to use wherever it provides meaningful value, while keeping application development simple and readable.**

Developers should be able to express:

```text
"These operations are independent and can run concurrently."
```

without having to build the entire concurrency infrastructure around them.

FastFlow should handle the complexity underneath while keeping application code close to ordinary synchronous Java.

The long-term idea is for concurrency to become something developers can introduce incrementally without significantly increasing the complexity of their codebase.

---

# Near-Term Goals

FastFlow is currently in its early stages.

The near-term goal is to progressively introduce features that make concurrent execution safer and more practical for production applications.

Some areas being explored include:

- Task timeouts
- Execution deadlines
- Maximum concurrency limits
- Resource-specific concurrency limits
- Improved cancellation propagation
- Typed task results
- Different failure strategies
- Partial results
- Task dependencies
- Metrics and observability
- Testing utilities

The API is expected to evolve as these problems are explored.

---

# Future Direction

Eventually, FastFlow could support workflows such as:

```java
FastFlow.parallel()
        .task("customer", customerService::getCustomer)
        .task("accounts", accountService::getAccounts)
        .task("offers", offerService::getOffers)
        .timeout(Duration.ofSeconds(2))
        .maxConcurrency(50)
        .failFast()
        .execute();
```

Another possible direction is dependency-aware execution:

```text
             Customer
             /      \
            /        \
        Account      Offers
           |
           |
      Transactions
```

FastFlow could automatically execute independent parts of such workflows concurrently while respecting task dependencies.

---

# What FastFlow Is Not

FastFlow is not intended to make CPU-heavy operations magically faster.

Virtual Threads are primarily useful for workloads involving blocking or I/O-heavy operations such as:

- HTTP calls
- Database queries
- File operations
- External service calls
- Network communication
- SFTP
- Other blocking integrations

CPU-intensive workloads may require a different execution strategy.

FastFlow also does not attempt to automatically determine whether arbitrary pieces of Java code are safe to execute concurrently.

The developer explicitly defines which operations are independent.

---

# Contributing / Ideas

FastFlow is still evolving, and feedback is especially valuable at this stage.

If you have:

- A concurrency problem that is difficult to solve cleanly
- A repetitive concurrency pattern in your application
- An idea that could simplify concurrent Java development
- A production use case that FastFlow should support
- Suggestions for the API
- Performance or design improvements

please open an issue or start a discussion.

A major goal of this project is to understand **which concurrency problems Java developers repeatedly face and which abstractions can make those problems simpler without hiding important behavior**.

Contributions, ideas, criticism, and experiments are welcome.

---

# Status

```text
Experimental / Early Development
```

The API is currently unstable and may change between releases.

FastFlow should not yet be considered production-ready.

---

# License

FastFlow is licensed under the **Apache License 2.0**.

See the `LICENSE` file for details.