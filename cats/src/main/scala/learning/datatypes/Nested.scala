package learning.datatypes

import cats.data.Validated
import cats.data.Validated.Valid

/*
In day-to-day programming we quite often end up with data inside nested
effects. e.g an integer inside an `Either` or `Validated`, which in turn
is nested inside an option.

This can be quite annoying to work with, as you have to traverse the
nested structure every time you want to perform a map or something similar.

Nested can help with this by composing the two map operations into one.

    `final case class Nested[F[_], G[_], A](value: F[G[A]])`

`Nested` is a thin newtype around `F[G[A]]`. You wrap with `Nested(...)`, use the
composed typeclass ops (`map`, `traverse`, …), then `.value` when you need the
bare `F[G[A]]` again.
 */
object LearningNested:

  def nestedEffectAnnoyingExample() =
    val x: Option[Either[String, Int]] = Some(Right(123))
    val y: Option[Validated[String, Int]] = Some(Valid(123))
    // Option[Validated[String, String]] = Some(value = Valid(a = "123"))
    // Outer `map` is on Option; inner `map` is on Validated — two layers, two hops.
    println(y.map(_.map(_.toString)))
    // Option[Either[String, String]] = Some(value = Right(a = "123"))
    println(x.map(_.map(_.toString)))

  def nestedTypeClassCanHelp() =
    import cats.data.Nested
    import cats.syntax.all.*
    // `G[_]` as a type lambda: same as `Validated[String, *]` with kind-projector.
    val nested: Nested[Option, [X] =>> Validated[String, X], Int] = Nested(
      Some(Valid(123))
    )
    // One `map` updates the `Int` inside both layers; `.value` peels off the Nested newtype.
    println(nested.map(_.toString).value)

  /*
    In a sense, Nested is similar to monad transformers like OptionT and EitherT, as
    it represents the nesting of effects inside each other. But Nested is more general,
    it does not place any restriction on the type of the two nested effects:

        final case class Nested[F[_], G[_], A](value: F[G[A]])

    Instead, it provides a set of inference rules based on the properties of F[_] and G[_].
    -------------------
    1) F[_] and G[_] are both Functors, then Nested[F, G, *] is also a Functor
        -> (we saw this in action in the nestedTypeClassCanHelp example above)
    2) F[_] and G[_] are both Applicatives, then Nested[F, G, *] is also an Applicative
    3) If F[_] and G[_] are both Traverses, then Nested[F, G, *] is also a Traverse
    4) F[_] is an ApplicativeError and G[_] is an Applicative, then Nested[F, G, *]
       is an ApplicativeError

    Example.
    ---------------------
    Say we have an API for creating users
    The `Future[Either[…, …]]` stack is `F` = Future, `G` = `Either[List[String], *]`
    (same idea as `Validated` nesting: one effect around another).
   */
  import scala.concurrent.Future

  case class UserInfo(name: String, age: Int)
  case class User(id: String, name: String, age: Int)

  def createUser(userInfo: UserInfo): Future[Either[List[String], User]] =
    Future.successful(Right(User("user 123", userInfo.name, userInfo.age)))

  /*
    Using Nested we can write a function that, given list of UserInfos,
    creates a list of Users:.
   */
  def usingNestedForCreatingUsers() =
    import scala.concurrent.Await
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.duration._
    import cats.Applicative
    import cats.data.Nested
    import cats.syntax.all._

    /*
         `traverse` over `Nested(createUser(_))` uses the Applicative for `Nested[Future, Either[L, *], *]`,
         so the outer `Future` and inner `Either` compose: result type `Future[Either[L, List[User]]]`.
         Inner failures use the `Either` Applicative (here `L` is `List[String]` with concatenation), so
         errors can combine rather than sitting in separate cells of a list.

         Note that if we hadn't used Nested, the behaviour of our function would have been different,
         resulting in a different return type. Plain createUser() only knows about `Future`’s Applicative:
         you get `Future[List[Either[L, User]]]` — a list of independent outcomes, not one `Either`
         over the list.
     */
    def createUsers(
        userInfos: List[UserInfo]
    ): Future[Either[List[String], List[User]]] =
      userInfos.traverse(userInfo => Nested(createUser(userInfo))).value
      // userInfos.traverse(createUser) => this would make list of independent outcomes :(

    val userInfos = List(UserInfo("Alice", 42), UserInfo("Bob", 99))
    val result = Await.result(createUsers(userInfos), 1.second)
    // Right(List(User(user 123,Alice,42), User(user 123,Bob,99)))
    println(s"result of createUsers(userInfos): ${result}")

@main def nestedExamples() =
  import LearningNested.*
  nestedEffectAnnoyingExample()
  nestedTypeClassCanHelp()
  usingNestedForCreatingUsers()
