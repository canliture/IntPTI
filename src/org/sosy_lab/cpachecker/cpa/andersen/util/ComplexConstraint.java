/*
 * IntPTI: integer error fixing by proper-type inference
 * Copyright (c) 2017.
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
package org.sosy_lab.cpachecker.cpa.andersen.util;


/**
 * This class models a ComplexConstraint in pointer analysis. This constraint has the
 * structure <code>*a \subseteq b</code>, or <code>a \subseteq *b</code>.
 */
public class ComplexConstraint extends Constraint {

  private final boolean isSubDerefed;

  /**
   * Creates a new {@link BaseConstraint} with the given variables for the sub- and superset.
   *
   * @param subVar       Indentifies the only element of the subset in this Constraint.
   * @param superVar     Indentifies the superset variable in this Constraint.
   * @param isSubDerefed <code>true</code> if the subset is dereferenced, <code>false</code> if the
   *                     superset is dereferenced.
   */
  public ComplexConstraint(String subVar, String superVar, boolean isSubDerefed) {
    super(subVar, superVar);

    this.isSubDerefed = isSubDerefed;
  }

  /**
   * Returns if the subset in this complex constraints is dereferenced. If not, the superset is
   * dereferenced.
   *
   * @return <code>true</code> if the subset is dereferenced, <code>false</code> if the superset is
   * dereferenced.
   */
  public boolean isSubDerefed() {
    return isSubDerefed;
  }

  @Override
  public boolean equals(Object other) {

    if (super.equals(other) && this.isSubDerefed == ((ComplexConstraint) other).isSubDerefed) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {

    return super.hashCode() * 31 + (isSubDerefed ? 1 : 0);
  }
}
