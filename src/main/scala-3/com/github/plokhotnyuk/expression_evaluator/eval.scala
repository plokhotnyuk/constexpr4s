package com.github.plokhotnyuk.expression_evaluator

import scala.quoted._

object eval {
  inline def apply[A](inline expr: A): A = ${applyImpl('expr)}

  private[this] def applyImpl[A](expr: Expr[A])(using QuoteContext): Expr[A] = '{$expr}
}