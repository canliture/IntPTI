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
package org.sosy_lab.cpachecker.util.predicates.smt;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.sosy_lab.common.log.TestLogManager;
import org.sosy_lab.solver.SolverContextFactory.Solvers;
import org.sosy_lab.solver.SolverException;
import org.sosy_lab.solver.api.ArrayFormula;
import org.sosy_lab.solver.api.BooleanFormula;
import org.sosy_lab.solver.api.FormulaType;
import org.sosy_lab.solver.api.NumeralFormula.IntegerFormula;
import org.sosy_lab.solver.test.SolverBasedTest0;

@RunWith(Parameterized.class)
public class SolverQuantifierTest extends SolverBasedTest0 {

  @Parameters(name = "{0}")
  public static Object[] getAllSolvers() {
    return Solvers.values();
  }

  @Parameter(0)
  public Solvers solverUnderTest;

  private QuantifiedFormulaManagerView qfm;

  private IntegerFormula _x;
  private ArrayFormula<IntegerFormula, IntegerFormula> _b;
  private BooleanFormula _b_at_x_eq_1;
  private BooleanFormula _b_at_x_eq_0;

  @Override
  protected Solvers solverToUse() {
    return solverUnderTest;
  }

  @Before
  public void setUp() throws Exception {
    requireArrays();
    requireQuantifiers();
    requireRationals();

    FormulaManagerView mgrv = new FormulaManagerView(mgr, config, TestLogManager.getInstance());
    this.qfm = mgrv.getQuantifiedFormulaManager();
    imgr = mgrv.getIntegerFormulaManager();

    _x = imgr.makeVariable("x");
    _b = amgr.makeArray("b", FormulaType.IntegerType, FormulaType.IntegerType);

    _b_at_x_eq_1 = imgr.equal(amgr.select(_b, _x), imgr.makeNumber(1));
    _b_at_x_eq_0 = imgr.equal(amgr.select(_b, _x), imgr.makeNumber(0));
  }

  @Test
  public void testExistsRestrictedRange() throws SolverException, InterruptedException {
    BooleanFormula f;

    BooleanFormula _exists_10_20_bx_0 = qfm.exists(_x,
        imgr.makeNumber(10),
        imgr.makeNumber(20),
        _b_at_x_eq_0);

    BooleanFormula _exists_10_20_bx_1 = qfm.exists(_x,
        imgr.makeNumber(10),
        imgr.makeNumber(20),
        _b_at_x_eq_1);

    BooleanFormula _forall_x_bx_1 = qfm.forall(_x, _b_at_x_eq_1);
    BooleanFormula _forall_x_bx_0 = qfm.forall(_x, _b_at_x_eq_0);

    // (exists x in [10..20] . b[x] = 0) AND (forall x . b[x] = 0) is SAT
    f = bmgr.and(_exists_10_20_bx_0, _forall_x_bx_0);
    assertThatFormula(f).isSatisfiable();

    // (exists x in [10..20] . b[x] = 1) AND (forall x . b[x] = 0) is UNSAT
    f = bmgr.and(_exists_10_20_bx_1, _forall_x_bx_0);
    assertThatFormula(f).isUnsatisfiable();

    // (exists x in [10..20] . b[x] = 1) AND (forall x . b[x] = 1) is SAT
    f = bmgr.and(_exists_10_20_bx_1, _forall_x_bx_1);
    assertThatFormula(f).isSatisfiable();

    // (exists x in [10..20] . b[x] = 1) AND (b[10] = 0) is SAT
    f = bmgr.and(_exists_10_20_bx_1,
        imgr.equal(amgr.select(_b, imgr.makeNumber(10)), imgr.makeNumber(0)));
    assertThatFormula(f).isSatisfiable();

    // (exists x in [10..20] . b[x] = 1) AND (b[1000] = 0) is SAT
    f = bmgr.and(_exists_10_20_bx_1,
        imgr.equal(amgr.select(_b, imgr.makeNumber(1000)), imgr.makeNumber(0)));
    assertThatFormula(f).isSatisfiable();
  }

  @Test
  public void testForallRestrictedRange() throws SolverException, InterruptedException {
    BooleanFormula f;

    BooleanFormula _forall_10_20_bx_0 = qfm.forall(_x,
        imgr.makeNumber(10),
        imgr.makeNumber(20),
        _b_at_x_eq_0);

    BooleanFormula _forall_10_20_bx_1 = qfm.forall(_x,
        imgr.makeNumber(10),
        imgr.makeNumber(20),
        _b_at_x_eq_1);

    // (forall x in [10..20] . b[x] = 0) AND (forall x . b[x] = 0) is SAT
    f = bmgr.and(
        _forall_10_20_bx_0,
        qfm.forall(_x, _b_at_x_eq_0));
    assertThatFormula(f).isSatisfiable();

    // (forall x in [10..20] . b[x] = 1) AND (exits x in [15..17] . b[x] = 0) is UNSAT
    f = bmgr.and(
        _forall_10_20_bx_1,
        qfm.exists(_x, imgr.makeNumber(15), imgr.makeNumber(17), _b_at_x_eq_0));
    assertThatFormula(f).isUnsatisfiable();

    // (forall x in [10..20] . b[x] = 1) AND b[10] = 0 is UNSAT
    f = bmgr.and(
        _forall_10_20_bx_1,
        imgr.equal(
            amgr.select(_b, imgr.makeNumber(10)),
            imgr.makeNumber(0)));
    assertThatFormula(f).isUnsatisfiable();

    // (forall x in [10..20] . b[x] = 1) AND b[20] = 0 is UNSAT
    f = bmgr.and(
        _forall_10_20_bx_1,
        imgr.equal(
            amgr.select(_b, imgr.makeNumber(20)),
            imgr.makeNumber(0)));
    assertThatFormula(f).isUnsatisfiable();

    // (forall x in [10..20] . b[x] = 1) AND b[9] = 0 is SAT
    f = bmgr.and(
        _forall_10_20_bx_1,
        imgr.equal(
            amgr.select(_b, imgr.makeNumber(9)),
            imgr.makeNumber(0)));
    assertThatFormula(f).isSatisfiable();

    // (forall x in [10..20] . b[x] = 1) AND b[21] = 0 is SAT
    f = bmgr.and(
        _forall_10_20_bx_1,
        imgr.equal(
            amgr.select(_b, imgr.makeNumber(21)),
            imgr.makeNumber(0)));
    assertThatFormula(f).isSatisfiable();

    // (forall x in [10..20] . b[x] = 1) AND (forall x in [0..20] . b[x] = 0) is UNSAT
    f = bmgr.and(
        _forall_10_20_bx_1,
        qfm.forall(_x, imgr.makeNumber(0), imgr.makeNumber(20), _b_at_x_eq_0));
    assertThatFormula(f).isUnsatisfiable();

    // (forall x in [10..20] . b[x] = 1) AND (forall x in [0..9] . b[x] = 0) is SAT
    f = bmgr.and(
        _forall_10_20_bx_1,
        qfm.forall(_x, imgr.makeNumber(0), imgr.makeNumber(9), _b_at_x_eq_0));
    assertThatFormula(f).isSatisfiable();
  }
}
