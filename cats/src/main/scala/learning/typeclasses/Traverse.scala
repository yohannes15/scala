package learning.typeclasses

import cats.{Applicative, Functor, Foldable}
import tree.Tree


object LearningTraits:
  /** Gentle intro: **Traverse** (before you go deep)
    * 
    * Given a function which returns a G effect, thread this effect
    * through the running of this function on all the values in F,
    * returning an F[B] in a G context. The function f is applied to each value in F,
    * and the result is a new F[B] in a G context.
    * 
    * In short, Given a collection of data F[A], for each value apply the function
    * f which returns an effectful value. The result of traverse is the composition
    * of all these effectful values. 
    *
    * You have a **structure** of `A`s (a `List`, a `Tree`, …) and a function
    * `f : A => G[B]`: for each element, `f` **returns a `B` wrapped in `G`** (same
    * wrapper every time). Here **`G`** might be `Option`, `Validated`, a task type, …
    * You want **one** outer `G` around the whole rebuilt structure —
    * e.g. `G[List[B]]` or `G[Tree[B]]` — instead of manually nesting `G` at each step.
    * That combined walk is **`traverse`**. It only needs **`Applicative[G]`** (not
    * always `Monad`): the `G` results are combined in a fixed shape using `map2`, `map3`, …
    *
    * *Why it is useful:* 
    * 
    * Use **`traverse`** when each **`f(a)` is a `G[B]`** (optional
    * result, async call, validation, …) — not a plain `B` — and you want **one** outer
    * `G` for the whole structure. All that really matters is the **`A => G[B]`** type;
    * `traverse` avoids hand-written loops, nested `flatMap`, or building `List[G[B]]`
    * and fixing the shape yourself.
    *
    * Think of it as generalising `Future.traverse` / `Future.sequence` to any
    * applicative `G`. This trait is the abstraction: **any** container `F` that can
    * expose `traverse` in terms of an applicative `G`. The full Cats type class is
    * [[cats.Traverse]], with laws and many instances.
    *
    */
  trait Traverse[F[_]]: // extends Functor[F] with Foldable[F]
    def traverse[G[_]: Applicative, A, B](fa: F[A])(f: A => G[B]): G[F[B]]

  /** `List` instance: implementation here (same `foldRight` + `map2` idea as top-level [[traverse]]). */
  given traverseForList: Traverse[List] = new Traverse[List]: 
    def traverse[G[_]: Applicative, A, B](fa: List[A])(f: A => G[B]): G[List[B]] =
      fa.foldRight(Applicative[G].pure(List.empty[B])) { case (a, acc) =>
        Applicative[G].map2(f(a), acc)((x, y) => x :: y)
      }

  /** No second algorithm: this **only** calls [[tree.Tree#traverse]] so `Tree` fits the
    * same `Traverse[F]` shape as `List`. Generic code takes `(using ev: Traverse[F])` and
    * calls `ev.traverse(fa)(f)`.
    */
  
  given traverseForTree: Traverse[Tree] = new Traverse[Tree]:
    def traverse[G[_]: Applicative, A, B](fa: Tree[A])(f: A => G[B]): G[Tree[B]] =
      fa.traverse(f)

  /** Shows: `t.traverse(f)` and `traverseForTree.traverse(t)(f)` do the same work. */
  def treeMethodVsTypeclassExample(): Unit =
    import cats.syntax.all.*

    val t: Tree[Int] =
      Tree.Branch(
        10,
        Tree.Branch(4, Tree.Empty(), Tree.Empty()),
        Tree.Branch(7, Tree.Empty(), Tree.Empty())
      )
    
    def halfIfEven(n: Int): Option[Int] = if n % 2 == 0 then Some(n / 2) else None

    // 1) Data-type API: you know you have a Tree
    val direct: Option[Tree[Int]] = t.traverse(halfIfEven)

    // 2) Type-class API: same result — call the named `given` instance explicitly
    val viaTypeclass: Option[Tree[Int]] = traverseForTree.traverse(t)(halfIfEven)

    println(s"direct (None because 10 and 7 are odd): $direct")
    println(s"via Traverse[Tree] (same): $viaTypeclass")

    val t2: Tree[Int] = 
      Tree.Branch(
        8, 
        Tree.Branch(4, Tree.Empty(), Tree.Empty()), Tree.Empty()
      )
    println(s"all even → Some tree: ${t2.traverse(halfIfEven)}")


@main def traverseExamples() = 
  import LearningTraits.treeMethodVsTypeclassExample
  treeMethodVsTypeclassExample()


