package inspections.suggestion

object DoNotHighlight {
  // boolean expressions without get in various cases:
  // 	- if else
  // 	- for comprehensions
  // 	- if guards
  // 	- method or value body

  case class Foo(bar: Option[Int])

  val foo: Foo = Foo(Some(123))
  val listOpt: Option[List[Int]] = Some(List(0, 1, 2, 3))

  val example1: Unit = {
    if (foo.bar.isDefined) "bar is defined"
    else "bar is empty"
  }

  val example2: Unit = {
    val res0: Boolean = foo.bar.isDefined || "qwe".isEmpty || List(1, 2).contains(2)

    def res1(s: String): Boolean = foo.bar.isDefined && s.isEmpty
  }

  val example3: Unit = {
    foo match {
      case Foo(bar) if listOpt.isDefined => bar.isDefined
      case _ => false
    }
  }

  val example4: Unit = {
    for {
      i <- List(0, 1, 2)
      if foo.bar.isDefined || listOpt.isDefined
    } yield i * 10
  }
}
