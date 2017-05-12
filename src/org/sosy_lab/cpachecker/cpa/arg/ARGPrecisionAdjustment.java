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
package org.sosy_lab.cpachecker.cpa.arg;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import org.sosy_lab.cpachecker.core.interfaces.AbstractState;
import org.sosy_lab.cpachecker.core.interfaces.Precision;
import org.sosy_lab.cpachecker.core.interfaces.PrecisionAdjustment;
import org.sosy_lab.cpachecker.core.interfaces.PrecisionAdjustmentResult;
import org.sosy_lab.cpachecker.core.interfaces.PrecisionAdjustmentResult.Action;
import org.sosy_lab.cpachecker.core.reachedset.UnmodifiableReachedSet;
import org.sosy_lab.cpachecker.exceptions.CPAEnabledAnalysisPropertyViolationException;
import org.sosy_lab.cpachecker.exceptions.CPAException;

import java.util.HashSet;
import java.util.Set;

public class ARGPrecisionAdjustment implements PrecisionAdjustment {

  private final PrecisionAdjustment wrappedPrecAdjustment;
  protected final boolean inCPAEnabledAnalysis;


  public ARGPrecisionAdjustment(
      PrecisionAdjustment pWrappedPrecAdjustment,
      boolean pInCPAEnabledAnalysis) {
    wrappedPrecAdjustment = pWrappedPrecAdjustment;
    inCPAEnabledAnalysis = pInCPAEnabledAnalysis;
  }

  @Override
  public Optional<PrecisionAdjustmentResult> prec(
      AbstractState pElement,
      Precision oldPrecision,
      UnmodifiableReachedSet pElements,
      final Function<AbstractState, AbstractState> projection,
      AbstractState fullState) throws CPAException, InterruptedException {

    Preconditions.checkArgument(pElement instanceof ARGState);
    //noinspection ConstantConditions
    ARGState element = (ARGState) pElement;

    if (inCPAEnabledAnalysis && element.isTarget()) {
      if (elementHasSiblings(element)) {
        removeUnreachedSiblingsFromARG(element, pElements);
      }
      // strengthening of PredicateCPA already proved if path is infeasible and removed infeasible element
      // thus path is feasible here
      throw new CPAEnabledAnalysisPropertyViolationException(
          "Property violated during successor computation", element, false);
    }

    AbstractState oldElement = element.getWrappedState();

    Optional<PrecisionAdjustmentResult> optionalUnwrappedResult =
        wrappedPrecAdjustment.prec(
            oldElement,
            oldPrecision,
            pElements,
            Functions.compose(
                ARGState.getUnwrapFunction(),
                projection),
            fullState
        );

    if (!optionalUnwrappedResult.isPresent()) {
      element.removeFromARG();
      return Optional.absent();
    }

    PrecisionAdjustmentResult unwrappedResult = optionalUnwrappedResult.get();

    // ensure that ARG and reached set are consistent if BREAK is signaled for a state with multiple children
    if (unwrappedResult.action() == Action.BREAK && elementHasSiblings(element)) {
      removeUnreachedSiblingsFromARG(element, pElements);
    }

    AbstractState newElement = unwrappedResult.abstractState();
    Precision newPrecision = unwrappedResult.precision();
    Action action = unwrappedResult.action();

    if ((oldElement == newElement) && (oldPrecision == newPrecision)) {
      // nothing has changed
      return Optional.of(PrecisionAdjustmentResult.create(pElement, oldPrecision, action));
    }

    ARGState resultElement = new ARGState(newElement, null);

    element.replaceInARGWith(resultElement); // this completely eliminates element

    return Optional.of(PrecisionAdjustmentResult.create(resultElement, newPrecision, action));
  }

  /**
   * This method removes all siblings of the given element from the ARG, if they are not yet in the
   * reached set.
   *
   * These measures are necessary in the cases where precision adjustment signals {@link
   * Action#BREAK} for a state whose parent has multiple children, and not all children have been
   * processed completely. In this case, not all children would be in the reached set, however, are
   * already in the ARG (as children of their parent). To avoid this inconsistency, all children not
   * yet contained in the reached set are removed from the ARG.
   *
   * @param element     the element for which to remove the siblings
   * @param pReachedSet the current reached set
   */
  private void removeUnreachedSiblingsFromARG(
      ARGState element,
      UnmodifiableReachedSet pReachedSet) {
    Set<ARGState> scheduledForDeletion = new HashSet<>();

    for (ARGState sibling : Iterables.getOnlyElement(element.getParents()).getChildren()) {
      if (sibling != element && !pReachedSet.contains(sibling)) {
        scheduledForDeletion.add(sibling);
      }
    }

    for (ARGState sibling : scheduledForDeletion) {
      sibling.removeFromARG();
    }
  }

  /**
   * This method checks if the given element has a sibling in the ARG.
   *
   * @param element the element to check
   * @return true, if the element has a sibling in the ARG
   */
  private boolean elementHasSiblings(ARGState element) {
    return Iterables.getOnlyElement(element.getParents()).getChildren().size() > 1;
  }
}