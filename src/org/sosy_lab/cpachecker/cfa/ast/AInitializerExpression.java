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
package org.sosy_lab.cpachecker.cfa.ast;

import java.util.Objects;


public abstract class AInitializerExpression extends AbstractInitializer {

  private final AExpression expression;

  public AInitializerExpression(FileLocation pFileLocation, final AExpression pExpression) {
    super(pFileLocation);
    expression = pExpression;
  }

  @Override
  public String toASTString() {
    return expression.toASTString();
  }

  public AExpression getExpression() {
    return expression;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 7;
    result = prime * result + Objects.hashCode(expression);
    result = prime * result + super.hashCode();
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof AInitializerExpression)
        || !super.equals(obj)) {
      return false;
    }

    AInitializerExpression other = (AInitializerExpression) obj;

    return Objects.equals(other.expression, expression);
  }

}
