package org.scalablytyped.converter.internal
package scalajs
package transforms

/**
  * i18next 22.0.0 introduces a helper conditional type `TypeOptionsFallback`
  * in its TypeScript typings. After translation to Scala.js, the resulting
  * type alias can participate in cyclic aliasing chains that Scala 2 rejects
  * with:
  *   "cyclic aliasing or subtyping involving type TypeOptionsFallback".
  *
  * For Scala.js facades we don't need the full conditional logic – it only
  * influences the precise return type of `t` in very advanced scenarios. To
  * keep the facades compiling, we conservatively rewrite the alias to `Any`.
  *
  * Original TS (simplified):
  *   type TypeOptionsFallback<TranslationValue, Option, MatchingValue> =
  *     Option extends false ? ... : TranslationValue
  *
  * Our Scala version:
  *   type TypeOptionsFallback[TranslationValue, Option, MatchingValue] = Any
  */
object FixI18nextTypeOptionsFallback extends TreeTransformation {

  private val I18nextLibName = Name("i18next")
  private val AliasName      = Name("TypeOptionsFallback")

  override def leaveTypeAliasTree(scope: TreeScope)(s: TypeAliasTree): TypeAliasTree = {
    val processed = super.leaveTypeAliasTree(scope)(s)

    if (scope.root.libName === I18nextLibName && processed.name === AliasName)
      processed.copy(alias = TypeRef.Any)
    else
      processed
  }
}
