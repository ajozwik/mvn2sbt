package pl.jozwik.mvn2sbt

object ReflectionUtils {

  def newInstance[T](clazz: Class[_]): T =
    castTo(clazz.getDeclaredConstructor().newInstance())

  def castTo[T](any: Any): T = any.asInstanceOf[T]
}
