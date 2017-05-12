/*
 * Tsmart-BD: The static analysis component of Tsmart platform
 *
 * Copyright (C) 2013-2017  Tsinghua University
 *
 * Open-source component:
 *
 * CPAchecker
 * Copyright (C) 2007-2014  Dirk Beyer
 *
 * Guava: Google Core Libraries for Java
 * Copyright (C) 2010-2006  Google
 *
 *
 */
package org.sosy_lab.cpachecker.cfa.ast.java;


/**
 * Interface for the visitor pattern. Typically used to evaluate expressions.
 *
 * @param <R> the return type of an evaluation.
 * @param <X> the exception thrown, if there are errors while evaluating an expression.
 */
public interface JExpressionVisitor<R, X extends Exception> extends JLeftHandSideVisitor<R, X> {

  R visit(JCharLiteralExpression paCharLiteralExpression) throws X;

  R visit(JStringLiteralExpression paStringLiteralExpression) throws X;

  R visit(JBinaryExpression paBinaryExpression) throws X;

  R visit(JUnaryExpression pAUnaryExpression) throws X;

  R visit(JIntegerLiteralExpression pJIntegerLiteralExpression) throws X;

  R visit(JBooleanLiteralExpression pJBooleanLiteralExpression) throws X;

  R visit(JFloatLiteralExpression pJFloatLiteralExpression) throws X;

  R visit(JArrayCreationExpression pJArrayCreationExpression) throws X;

  R visit(JArrayInitializer pJArrayInitializer) throws X;

  R visit(JArrayLengthExpression pJArrayLengthExpression) throws X;

  R visit(JVariableRunTimeType pJThisRunTimeType) throws X;

  R visit(JRunTimeTypeEqualsType pJRunTimeTypeEqualsType) throws X;

  R visit(JNullLiteralExpression pJNullLiteralExpression) throws X;

  R visit(JEnumConstantExpression pJEnumConstantExpression) throws X;

  R visit(JCastExpression pJCastExpression) throws X;

  R visit(JThisExpression pThisExpression) throws X;

}
