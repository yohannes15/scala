# [Cats Effect](https://typelevel.org/cats-effect/) — learning layout

This package holds **topic-based modules** for learning [Cats Effect](https://typelevel.org/cats-effect/) in this repo.

Cats Effect is a runtime and library for **async and concurrent** programs in Scala (JVM, JS, Native). You describe work with `IO` and related types; the runtime schedules **fibers**, handles **cancellation**, and helps you manage **resources** safely.

**Why it exists (in brief):**

- **Fast** — Lightweight fibers and a work-stealing runtime keep concurrency cheap at scale.
- **Reliable** — Resource safety, backpressure, and cancellation so work can be stopped or timed out without leaking handles.
- **Ecosystem** — Shared abstractions used by streaming (e.g. fs2), HTTP, databases, and other Typelevel libraries.
- **Community** — Open source, maintained by Typelevel.

## Dependency

```scala
libraryDependencies += "org.typelevel" %% "cats-effect" % "3.7.0"
```

## Asynchronous

`IO` wraps asynchronous, callback-style work behind a single, composable API. It is not `Future`: you choose **when** effects run, and you can mix sync and async steps without giving up a clear structure.

## Concurrency

High concurrency is expressed with **fibers** — lightweight, runtime-managed tasks that are cheaper than OS threads. You focus on composition; the runtime handles scheduling and related details.

## Tracing

The runtime can record execution context so errors are easier to place. Tracing is designed to be usable in production with low overhead; deeper instrumentation helps in development.

## Safety

Real programs hold connections, files, and other resources. Cats Effect (notably `Resource` and related patterns) aims to **allocate and release** them correctly even when tasks fail or are **cancelled**.

## Composable

In the functional style, `IO` is often a **description** of a program until you run it. Small `IO` values combine into larger ones, which keeps refactoring and reasoning manageable.

## Concepts and other runtimes

Cats Effect has its own vocabulary. That can feel unfamiliar next to actor systems or callback APIs, but the big picture is the same: an **async runtime** on the JVM (and elsewhere).

Rough analogues (each differs in API and guarantees):

- **Akka** — Broader framework; closest pieces are Actors and `Future` (stdlib), not a one-to-one match to `IO`.
- **Netty** — Event loops and handlers for NIO; CE does not replace low-level NIO (often paired with libraries like fs2 for streams).
- **Tokio** (Rust) — Similar design goals; CE is not a port but shares some ideas.
- **RxJava** — Reactive streams with a different programming model.
- **Vert.x** — Verticle/event-bus style async; different from fiber-based `IO`.

These stacks are **not** interchangeable: features like **cooperative cancellation**, a fiber-aware scheduler, and **production-oriented tracing** line up with Cats Effect in different ways than in older callback or `Future`-centric setups.

Still, the role is similar: a **foundation layer** for scalable async and parallel programs on the JVM and JavaScript.

## Note

As you gain more experience with cats-effect, you will probably find your own solutions to deal with the problems presented here. Also, bear in mind that using cats-effect for copying files or implementing basic concurrency patterns (such as the producer-consumer problem) is suitable for a 'getting things done' approach, but for more complex systems/settings/requirements you might want to take a look at [fs2](https://fs2.io/) or [Monix](https://monix.io/) to find powerful network and file abstractions that integrate with cats-effect.

## Further Reading

- [Cats Effect — home](https://typelevel.org/cats-effect/docs/tutorial)
- [Extra Resources](https://typelevel.org/cats-effect/resources.html)

## Advanced Reading

- [Fs2 — home](https://fs2.io/) - purely functional streaming library
- [Monix — home](https://monix.io/) - purely functional reactive programming library
