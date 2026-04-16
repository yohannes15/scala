package learning.typeclasses

/************************************************************************
Functor 
----------
A `Functor` is anything you can map over while staying "inside" the same
wrapper: turn F[A] into F[B] using A => B, without unpacking the whole
structure by hand.

    E.g: List, Option, Either[E, *], Future, etc are all functors b/c 
    they have a sensible map


************************************************************************/
