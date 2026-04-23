# Tools

## What's covered

Coursier, sbt build tool, ScalaTest testing framework

---

## Coursier

[Coursier](https://get-coursier.io/docs/overview) is a dependency resolver — similar to Maven and Ivy — written from scratch in Scala. It embraces functional programming principles and downloads artifacts in parallel for fast resolution. sbt uses it under the hood for all dependency resolution. As a standalone CLI tool it can also install sbt, Java, and Scala on your system.

---

## sbt

[sbt](https://www.scala-sbt.org/) is the first build tool created specifically for Scala.

### How it boots

```
sbt (launcher)
  └── reads project/build.properties   → picks sbt version
        └── reads build.sbt            → picks Scala version, dependencies
              └── compiles src/         → produces bytecode in target/
```

### Directory structure

```
.
├── build.sbt                     # Scala version, dependencies, settings
├── project/
│   └── build.properties          # sbt version
├── src/
│   ├── main/
│   │   ├── scala/                # app source
│   │   ├── java/                 # Java source (optional)
│   │   └── resources/            # config files, images (optional)
│   └── test/
│       ├── scala/                # test source
│       ├── java/                 # Java test source (optional)
│       └── resources/            # test resources (optional)
├── target/                       # sbt output (generated, don't edit)
└── lib/                          # unmanaged JARs (optional)
```

### build.sbt styles

**Bare style** — simple, single-project builds:

```scala
name         := "HelloWorld"
version      := "0.1"
scalaVersion := "3.8.2"
```

**Multi-project style** — explicit `lazy val` per subproject:

```scala
ThisBuild / scalaVersion := "3.8.2"   // applies to all subprojects

lazy val root = (project in file("."))
  .settings(
    name := "my-app",
    libraryDependencies ++= Seq(...)
  )
```

Bare settings are shorthand — sbt treats them as if they were inside a single `lazy val root` block.

### `libraryDependencies` format

```scala
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.19" % Test
//  └─ Group ID    │   └─ Artifact  └─ Version  └─ Scope
//                %%
//                 └─ picks the right artifact for your Scala binary version (e.g. scalatest_3)
//                  % would pin the exact artifact name (used for Java libs)
)
```

- `++=` appends a `Seq` to the existing list; `+=` adds a single item
- `%%` resolves to e.g. `scalatest_3` for Scala 3
- `%` (single) for Java libraries: `"org.postgresql" % "postgresql" % "42.7.0"`
- `Test` scope — only on the classpath during tests, not in production

### Creating a new project

Manually:

```bash
mkdir HelloWorld && cd HelloWorld
mkdir -p src/{main,test}/scala
mkdir project target
# then create build.sbt and project/build.properties
```

From a template:

```bash
sbt new scala/scala3.g8
```

### Common sbt commands


| Command                          | Description                                                                    |
| -------------------------------- | ------------------------------------------------------------------------------ |
| `sbt compile`                    | Compile sources                                                                |
| `sbt run`                        | Run the main class                                                             |
| `sbt test`                       | Run all tests                                                                  |
| `sbt clean`                      | Delete `target/` (clears stale build cache)                                    |
| `sbt updateClassifiers`          | Download `-sources.jar` for all dependencies (enables go-to-definition in IDE) |
| `sbt "runMain com.example.Main"` | Run a specific main class                                                      |
| `sbt "project foo"`              | Switch to subproject `foo` in a multi-project build                            |
| `sbt foo/console`                | Start the Scala REPL with subproject `foo`’s classpath (one-shot from the shell) |
| `sbt foo/run`                    | Run the main class in subproject `foo`                                         |
| `sbt "foo/runMain pkg.Main"`     | Run a specific main class in subproject `foo`                                    |


### Multi-project: switching vs scoped commands

You can either **change the active project** inside sbt or **prefix tasks** with the project id.

**Inside an interactive sbt session** (after `sbt`):

```text
sbt:scala-tutorial> project cats
[info] set current project to learning-cats (...)
sbt:learning-cats> compile
sbt:learning-cats> console
```

**From your normal shell** (no need to `project` first): put `<project>/` before the task:

```bash
sbt cats/compile
sbt cats/console
sbt cats/run
sbt "cats/runMain learning.effect.HelloWorld"
```

The first token must be a **task** (e.g. `cats/console`), not a nested `sbt` command — commands like `sbt cats/console` are only valid **outside** sbt; inside sbt, use `project cats` then `console`.

### REPL (`console`) and dependency classpath

Each subproject has its **own** `libraryDependencies`. This repo’s default project is **`root`** (`scala-tutorial`); it does **not** pull in Cats or cats-effect. If you open `console` on `root`, imports like `cats.effect.IO` fail with “Not found”.

Use the module that declares those deps — here, **`cats`**:

```bash
sbt cats/console
```

Then you can use e.g. `cats.effect.IO` (and optional `cats.effect.unsafe.implicits._` if you deliberately run effects in the REPL).

### Scala 3.8 and sbt version (`console`)

From Scala **3.8** onward, the REPL is shipped as **separate JARs** from the compiler. **sbt 1.10.x** still loads the REPL the old way, which often crashes `console` with:

`java.lang.NoClassDefFoundError: dotty/tools/repl/ReplDriver`

**Fix:** use **sbt 1.12 or newer** in `project/build.properties` (this repo pins a 1.12.x line for that reason). Upgrading sbt is the straightforward fix; alternatives include `scala-cli` for a standalone REPL or staying on Scala 3.7.x with an older sbt.

### Cats Effect (`IOApp`) and `run` in this repo

The **`cats`** subproject sets **`Compile / run / fork := true`**. That starts **`run` / `runMain` in a separate JVM**, which matches a normal `java` launch and avoids Cats Effect’s warning about `IOApp` running on a thread that isn’t the real process main thread (common when `fork` is false inside an interactive sbt session).

That setting applies only to **`run` / `runMain`** for that subproject — not to `compile`, `test`, or `console`.

---

## ScalaTest

[ScalaTest](https://www.scalatest.org/) is the most widely used testing framework for Scala. It is flexible and supports several testing styles. The simplest to get started with is `AnyFunSuite`.

### Setup

Add to `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.19" % Test
)
```

### AnyFunSuite style

```scala
import org.scalatest.funsuite.AnyFunSuite

class MathUtilsTests extends AnyFunSuite:

  test("'double' should handle zero") {
    val result = MathUtils.double(0)
    assert(result == 0)
  }

  test("'double' should handle 1") {
    val result = MathUtils.double(1)
    assert(result == 2)
  }

  test("test with Int.MaxValue") (pending)   // placeholder — not written yet

end MathUtilsTests
```

- Extend `AnyFunSuite`
- Each `test("name") { ... }` block is one test case
- Use `assert(condition)` to verify expected behaviour
- Mark unwritten tests as `(pending)` — they show up in the report without failing
- Similar to JUnit if you're coming from Java

### Running tests

```bash
sbt test                        # run all tests
sbt "testOnly math.MathUtilsTests"  # run a specific test class
```

