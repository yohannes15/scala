package learning.typeclasses


/**
  * If a type A can form a Semigroup it has an associative binary operation.
  * Associativity means the following equality must hold for any choice of x, y, and z.
  * combine(x, combine(y, z)) = combine(combine(x, y), z)

    trait Semigroup[A] {
        def combine(x: A, y: A): A
    }
*/


@main def semiGroupExample() =
    import cats.Semigroup
    given intAdditionSemiGroup: Semigroup[Int] = _ + _ 
    val x = 1
    val y = 2
    val z = 3

    println(Semigroup[Int].combine(x, y)) // 3
    println(Semigroup[Int].combine(x, Semigroup[Int].combine(y, z))) // 6
    println(Semigroup[Int].combine(Semigroup[Int].combine(x, y), z)) // 6
