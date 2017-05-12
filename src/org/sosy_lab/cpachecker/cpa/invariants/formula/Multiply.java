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
package org.sosy_lab.cpachecker.cpa.invariants.formula;

/**
 * Instances of this class are multiplication formulae over other invariants
 * formulae.
 *
 * @param <ConstantType> the type of the constants used in the formulae.
 */
public class Multiply<ConstantType> extends AbstractBinaryFormula<ConstantType>
    implements NumeralFormula<ConstantType> {

  /**
   * Creates a new multiplication formula with the given factors.
   *
   * @param pFactor1 the first factor.
   * @param pFactor2 the second factor.
   */
  private Multiply(
      NumeralFormula<ConstantType> pFactor1,
      NumeralFormula<ConstantType> pFactor2) {
    super("*", true, pFactor1, pFactor2);
  }

  /**
   * Gets the first factor of the multiplication.
   *
   * @return the first factor of the multiplication.
   */
  public NumeralFormula<ConstantType> getFactor1() {
    return super.getOperand1();
  }

  /**
   * Gets the second factor of the multiplication.
   *
   * @return the second factor of the multiplication.
   */
  public NumeralFormula<ConstantType> getFactor2() {
    return super.getOperand2();
  }

  @Override
  public <ReturnType> ReturnType accept(NumeralFormulaVisitor<ConstantType, ReturnType> pVisitor) {
    return pVisitor.visit(this);
  }

  @Override
  public <ReturnType, ParamType> ReturnType accept(
      ParameterizedNumeralFormulaVisitor<ConstantType, ParamType, ReturnType> pVisitor,
      ParamType pParameter) {
    return pVisitor.visit(this, pParameter);
  }

  /**
   * Gets an invariants formula representing the multiplication of the given
   * factors.
   *
   * @param pFactor1 the first factor.
   * @param pFactor2 the second factor.
   * @return an invariants formula representing the multiplication of the given factors.
   */
  static <ConstantType> Multiply<ConstantType> of(
      NumeralFormula<ConstantType> pFactor1,
      NumeralFormula<ConstantType> pFactor2) {
    return new Multiply<>(pFactor1, pFactor2);
  }

}
