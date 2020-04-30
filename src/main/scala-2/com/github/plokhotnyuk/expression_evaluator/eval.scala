package com.github.plokhotnyuk.expression_evaluator

import java.math.BigInteger

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object eval {
  def apply[A](expr: A): A = macro Impl.apply[A]

  private object Impl {
    def apply[A: c.WeakTypeTag](c: blackbox.Context)(expr: c.Expr[A]): c.Expr[A] = {
      import c.universe._

      implicit def lift: c.universe.Liftable[A] = Liftable[A]({
        case x: String => q"$x"
        case x: Byte => q"$x"
        case x: Boolean => q"$x"
        case x: Short => q"$x"
        case x: Char => q"$x"
        case x: Int => q"$x"
        case x: Float => q"$x"
        case x: Long => q"$x"
        case x: Double => q"$x"
        case x: BigInt => q"_root_.scala.math.BigInt(${x.underlying().toByteArray})"
        case x: BigInteger => q"new _root_.java.math.BigInteger(${x.toByteArray})"
        case x: java.time.ZoneOffset => q"_root_.java.time.ZoneOffset.ofTotalSeconds(${x.getTotalSeconds})"
        case x: java.time.ZoneId => q"_root_.java.time.ZoneId.of(${x.getId})"
        case x: Array[A] => q"$x"
        case _ => c.abort(c.enclosingPosition, s"Unsupported type of expression: '${weakTypeOf[A].dealias}'")
      })

      val result: Tree = q"${c.eval[A](c.Expr[A](c.untypecheck(expr.tree.duplicate)))}"
      if (c.settings.contains("print-expr-results")) {
        c.info(c.enclosingPosition, s"Expression result:\n${showCode(result)}", force = true)
      }
      c.Expr[A](result)
    }
  }
}