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
package org.sosy_lab.cpachecker.util.collections.tree;

import java.util.Stack;


public interface TreeVisitor<S, E> {
  public static enum TreeVisitStrategy {
    CONTINUE,
    // go deeper
    SKIP,
    // skip current branch
    ABORT       // skip all the rest
  }

  public TreeVisitStrategy visit(Stack<S> path, E element, boolean isLeaf);
}
