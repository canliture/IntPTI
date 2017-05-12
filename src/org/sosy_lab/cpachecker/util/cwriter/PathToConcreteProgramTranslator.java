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
package org.sosy_lab.cpachecker.util.cwriter;

import org.sosy_lab.common.Appender;
import org.sosy_lab.cpachecker.core.counterexample.CFAPathWithAssumptions;
import org.sosy_lab.cpachecker.cpa.arg.ARGPath;
import org.sosy_lab.cpachecker.cpa.arg.ARGState;

import java.util.Iterator;
import java.util.Set;

public class PathToConcreteProgramTranslator extends PathTranslator {

  private PathToConcreteProgramTranslator() {
  }

  /**
   * Transform a single linear path into C code.
   * The path needs to be loop free.
   *
   * TODO: Detect loops in the paths and signal an error.
   * Currently when there are loops, the generated C code is invalid
   * because there is a goto to a missing label.
   *
   * @param pPath       The path.
   * @param assignments The variable assignments for the path.
   * @return An appender that generates C code.
   */
  public static Appender translateSinglePath(ARGPath pPath, CFAPathWithAssumptions assignments) {
    PathToConcreteProgramTranslator translator = new PathToConcreteProgramTranslator();

    translator.translateSinglePath0(pPath,
        new ConcreteProgramEdgeVisitor(translator, assignments));

    return translator.generateCCode();
  }

  /**
   * Transform a set of paths into C code. All paths need to have a single root, and all paths need
   * to be loop free.
   *
   * TODO: Detect loops in the paths and signal an error. Currently when there are loops, the
   * generated C code is invalid because there is a goto to a missing label.
   *
   * TODO: Using CFAPathWithAssumptions does not make sense for translatePaths, because
   * CFAPathWithAssumptions encodes only a single path, and if there is only a single path, {@link
   * #translateSinglePath(ARGPath, CFAPathWithAssumptions)} should be used.
   *
   * @param argRoot             The root of all given paths.
   * @param elementsOnErrorPath The set of states that are on all paths.
   * @param assignments         The variable assignments for the path.
   * @return An appender that generates C code.
   */
  public static Appender translatePaths(
      ARGState argRoot,
      Set<ARGState> elementsOnErrorPath,
      CFAPathWithAssumptions assignments) {
    PathToConcreteProgramTranslator translator = new PathToConcreteProgramTranslator();

    translator.translatePaths0(argRoot, elementsOnErrorPath,
        new ConcreteProgramEdgeVisitor(translator, assignments));

    return translator.generateCCode();
  }

  @Override
  protected Appender generateCCode() {
    for (Iterator<String> it = mGlobalDefinitionsList.iterator(); it.hasNext(); ) {
      String s = it.next();
      s = s.toLowerCase().trim();
      if (s.startsWith("void main()")
          || s.startsWith("int main()")
          || s.contains("__verifier_nondet_")) {
        it.remove();
      }
    }

    mGlobalDefinitionsList.add(0, "#define __CPROVER_assume(x) if(!(x)){exit(0);}");
    mGlobalDefinitionsList.add(1, "int __VERIFIER_nondet_int() {return 0;}");
    mGlobalDefinitionsList.add(2, "long __VERIFIER_nondet_long() {return 0;}");
    mGlobalDefinitionsList
        .add(3, "void *__VERIFIER_nondet_pointer() { return malloc(100); }"); // assume a size
    mGlobalDefinitionsList.add(4, "char __VERIFIER_nondet_char() {return '0';}");
    mGlobalDefinitionsList.add(5, "int __VERIFIER_nondet_bool() {return 0;}");
    mGlobalDefinitionsList.add(6, "float __VERIFIER_nondet_float() {return 0.0;}");
    mGlobalDefinitionsList.add(7, "short __VERIFIER_nondet_short() {return 0;}");
    mGlobalDefinitionsList.add(8, "void main();");
    mGlobalDefinitionsList.add(9, "void main() {main_0();}");

    return super.generateCCode();
  }

  @Override
  protected String getTargetState() {
    return "exit(1); // target state; all assumptions were true";
  }

}