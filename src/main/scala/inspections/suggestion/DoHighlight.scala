package inspections.suggestion

import scala.concurrent.{ExecutionContext, Future}

object DoHighlight {

  case class Foo(bar: Option[Int])
  val foo: Foo = Foo(Some(123))
  val listOpt : Option[List[Int]] = Some(List(0, 1, 2, 3))
  def update(i: Int): String = s"value is $i !"
  def compute(i: Int)(implicit ec: ExecutionContext): Future[Int] = Future(i * 100)

  // Simple example of if (option.isDefined) option.get else defaultValue
  val example1: Unit = {
    // initial code (to be replaced)
    if (foo.bar.isDefined) update(foo.bar.get) else update(7)

    // suggestions
    // 1
    foo.bar.fold(update(7))(update)
    // 2
    foo.bar.map(update).getOrElse(update(7))
    // 3
    update(foo.bar.getOrElse(7))
    // 4
    foo.bar match {
      case Some(i) => update(i)
      case _ => update(7)
    }
  }

  // Sometimes if doesn't have else block because it holds a side effect
  val example2: Unit = {
    // initial code (to be replaced)
    if (foo.bar.isDefined) update(foo.bar.get)

    // suggestions
    // 1
    foo.bar.map(update)
    // 2
    foo.bar match {
      case Some(i) => update(i)
      case _ =>
    }
  }

  // Option.get could be in the if condition right after isDefined check
  val example3: Unit = {
    val values = List(0, 123)

    // initial code (to be replaced) with AND
    if (foo.bar.isDefined && values.contains(foo.bar.get)) update(6)
    // suggestions
    if (foo.bar.exists(values.contains)) update(6)

    // initial code (to be replaced) with OR
    if (foo.bar.isDefined || values.contains(foo.bar.get)) update(6)
    // suggestions
    if (foo.bar.forall(values.contains)) update(6)

    // initial code (to be replaced) with additional option.get after the condition
    if (foo.bar.isDefined && values.contains(foo.bar.get)) update(foo.bar.get)
    // suggestions
    foo.bar.filter(values.contains).map(update)
  }

  // two Options are checked if defined and only one used
  val example4: Unit = {
    val defaultList: List[Int] = List(7)

    // initial code (to be replaced)
    if (foo.bar.isDefined && listOpt.isDefined) defaultList ++ listOpt.get.map(_ * 10)
    else defaultList // it could be another action

    // suggestions
    // 1
    foo.bar.zip(listOpt).map(defaultList ++ _._2).getOrElse(defaultList)
    // 2
    foo.bar.map(_ => defaultList ++ listOpt.getOrElse(Nil))
    // 3
    (foo.bar, listOpt) match {
      case (Some(_), Some(l)) => defaultList ++ l
      case _ => defaultList
    }
    // 4
    // maybe a for comprehension? But it looks hard to insert into the code
    val res = for {
      _ <- foo.bar
      l <- listOpt
    } yield l
    defaultList ++ res.getOrElse(Nil)
  }

  // Option to Future without using 3rd party libraries
  // looks exactly like example1
  val example5: Unit = {
    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    // initial code (to be replaced)
    if (foo.bar.isDefined) compute(foo.bar.get)
    else Future.successful(5)

    // suggestions
    // 1
    foo.bar.fold(compute(5))(compute)
    // 2
    foo.bar.map(compute).getOrElse(compute(5))
    // 3
    compute(foo.bar.getOrElse(5))
    // 4
    foo.bar match {
      case Some(i) => compute(i)
      case _ => compute(5)
    }
  }
}
