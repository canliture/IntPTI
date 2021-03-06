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
package org.sosy_lab.cpachecker.cpa.bounds;

import static com.google.common.base.Preconditions.checkNotNull;

import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.cpachecker.cfa.CFA;
import org.sosy_lab.cpachecker.core.defaults.AbstractCPAFactory;
import org.sosy_lab.cpachecker.core.interfaces.CPAFactory;
import org.sosy_lab.cpachecker.core.interfaces.ConfigurableProgramAnalysis;
import org.sosy_lab.cpachecker.exceptions.CPAException;


public class BoundsCPAFactory extends AbstractCPAFactory {

  private CFA cfa;

  @Override
  public ConfigurableProgramAnalysis createInstance()
      throws InvalidConfigurationException, CPAException {
    checkNotNull(cfa, "CFA instance needed to create LoopstackCPA");
    return new BoundsCPA(getConfiguration(), cfa, getLogger());
  }

  @Override
  public <T> CPAFactory set(T pObject, Class<T> pClass) throws UnsupportedOperationException {
    if (CFA.class.isAssignableFrom(pClass)) {
      cfa = (CFA) pObject;
    } else {
      super.set(pObject, pClass);
    }
    return this;
  }

}
