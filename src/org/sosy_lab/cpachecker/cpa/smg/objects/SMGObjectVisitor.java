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
package org.sosy_lab.cpachecker.cpa.smg.objects;

import org.sosy_lab.cpachecker.cpa.smg.objects.sll.SMGSingleLinkedList;


public interface SMGObjectVisitor {
  public void visit(SMGObject pObject);

  public void visit(SMGRegion pObject);

  public void visit(SMGSingleLinkedList pObject);
}
