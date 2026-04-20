package learning.typeclasses

import cats.Applicative

/** Polymorphic **`traverse` for `List`**: `foldRight` with `Applicative[G].map2`, same
  * implementation as in **`Applicative.scala`** (kept here next to [[tree]] and
  * [[LearningTraits.Traverse]]). For the general idea, see the trait’s scaladoc.
  */
def traverse[F[_]: Applicative, A, B](as: List[A])(f: A => F[B]): F[List[B]] =
  as.foldRight(Applicative[F].pure(List.empty[B])) { (a: A, acc: F[List[B]]) =>
    val fb: F[B] = f(a)
    Applicative[F].map2(fb, acc)(_ :: _)
  }

/** Another container with **`traverse`**: a binary tree. Same pattern as [[traverse]]
  * on `List`, but the shape is recursive (left / right subtrees) instead of a list
  * spine — so each step uses **`map3`** (value, left, right). [[LearningTraits.Traverse]]
  * abstracts over both `List` and shapes like this.
  *
  * *Cases:* `Empty` has no `A` to map — stay `Empty` inside `pure`. `Branch` runs `f`
  * on the value and **recursively** `traverse`s both subtrees, then rebuilds `Branch`.
  */
object tree {
  sealed abstract class Tree[A] extends Product with Serializable {

    /** Walk the tree in applicative `F`: apply `f` at every `A`, combining all
      * `F[B]`s into one `F[Tree[B]]` while preserving shape (`Empty` vs `Branch`).
      */
    def traverse[F[_]: Applicative, B](f: A => F[B]): F[Tree[B]] = this match {
      case Tree.Empty() =>
        Applicative[F].pure(Tree.Empty()) // nothing to map; structure stays empty

      case Tree.Branch(v, l, r) =>
        // Three independent effectful results: value, left subtree, right subtree
        Applicative[F].map3(f(v), l.traverse(f), r.traverse(f))(Tree.Branch(_, _, _))
    }
  }

  object Tree {
    final case class Empty[A]() extends Tree[A]
    final case class Branch[A](value: A, left: Tree[A], right: Tree[A]) extends Tree[A]
  }
}

import tree._

object LearningTraits:

  /** Gentle intro: **Traverse** (before you go deep)
    *
    * You have a **structure** of `A`s (a `List`, a `Tree`, …) and a function
    * `f : A => G[B]`: for each element, `f` **returns a `B` wrapped in `G`** (same
    * wrapper every time). Here **`G`** might be `Option`, `Validated`, a task type, …
    * You want **one** outer `G` around the whole rebuilt structure —
    * e.g. `G[List[B]]` or `G[Tree[B]]` — instead of manually nesting `G` at each step.
    * That combined walk is **`traverse`**. It only needs **`Applicative[G]`** (not
    * always `Monad`): the `G` results are combined in a fixed shape using `map2`, `map3`, …
    *
    * *Why it is useful:* Use **`traverse`** when each **`f(a)` is a `G[B]`** (optional
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
  trait Traverse[F[_]]:
    def traverse[G[_]: Applicative, A, B](fa: F[A])(f: A => G[B]): G[F[B]]

