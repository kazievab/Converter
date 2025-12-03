package org.scalablytyped.converter.internal
package scalajs
package transforms

/**
  * i18next 21.7.0+ exposes a very rich overload set for `t` / `TFunction`
  * where some overloads differ only in extra intersection refinements on the
  * `options` parameter, e.g.
  *
  *   def t(key: TKeys, options: TOptions[T] with ReturnDetails with ...)
  *   def t(key: TKeys, options: TOptions[T])
  *
  * After Scala 2 erasure those overloads collapse to the same JVM signature and
  * cause "double definition" errors in the generated facades.
  *
  * For Scala.js facades these extra intersection arms (ReturnDetails,
  * ReturnObjects, etc.) don't have a practical effect for most consumers. We
  * therefore normalise such intersection types down to their first component
  * (e.g. `TOptions[...]`), allowing [[CombineOverloads]] to merge the
  * duplicates into a single, simpler overload.
  *
  * The transformation is scoped narrowly to the i18next library and the
  * `TFunction`, `WithT` and `i18n` types where these patterns occur.
  */
object FixI18nextTFunctionOverloads extends TreeTransformation {

  private val I18nextLibName     = Name("i18next")
  private val AffectedClassNames = Set(Name("TFunction"), Name("WithT"), Name("i18n"))

  private def inTargetOwner(scope: TreeScope): Boolean =
    scope.root.libName === I18nextLibName &&
      scope.owner.exists {
        case c: ClassTree => AffectedClassNames.contains(c.name)
        case _            => false
      }

  override def leaveTypeRef(scope: TreeScope)(s: TypeRef): TypeRef = {
    val processed = super.leaveTypeRef(scope)(s)

    if (!inTargetOwner(scope)) processed
    else
      processed match {
        case TypeRef(QualifiedName.INTERSECTION, types, comments) if types.nonEmpty =>
          val head = types(0)
          // Drop refinements like `with ReturnDetails with 0 with ReturnObjects`
          // and keep only the primary options type (typically TOptions[...]).
          head.withComments(comments ++ head.comments)
        case other =>
          other
      }
  }
}
