package learning.typeclasses

import cats.Applicative


/** Just a binary tree container with **`traverse`**
 * 
 * Same pattern as [[traverse]] on `List`, but the shape is recursive 
 * (left / right subtrees) instead of a list spine — so each step uses
 *  **`map3`** (value, left, right). 
 * 
 * [[LearningTraits.Traverse]] in [[Traverse.scala]] abstracts over both
 * `List` and shapes like this.
 * 
 * *Cases:* `Empty` has no `A` to map — stay `Empty` inside `pure`. `Branch`
 * runs `f` on the value and **recursively** `traverse`s both subtrees, 
 * then rebuilds `Branch`.
*/
object tree {
  sealed abstract class Tree[A] extends Product with Serializable {

    /** Walk the tree in applicative `F`: apply `f` at every `A`, combining all
      * `F[B]`s into one `F[Tree[B]]` while preserving shape (`Empty` vs `Branch`).
      */
    /** The **implementation** lives here (`map3`, recursion). The [[LearningTraits.Traverse]]
      * instance for `Tree` only forwards to this method so generic code can use one API.
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
