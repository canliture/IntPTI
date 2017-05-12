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
package org.sosy_lab.cpachecker.pcc.strategy.partitioning;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.cpachecker.core.interfaces.pcc.BalancedGraphPartitioner;
import org.sosy_lab.cpachecker.pcc.strategy.partialcertificate.PartialReachedSetDirectedGraph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.List;
import java.util.Set;


public class ExplorationOrderBalancedGraphPartitioner implements BalancedGraphPartitioner {

  private final ShutdownNotifier shutdownNotifier;
  private final boolean useDFS;

  public ExplorationOrderBalancedGraphPartitioner(
      final boolean pDFS,
      final ShutdownNotifier pShutdownNotifier) {
    useDFS = pDFS;
    shutdownNotifier = pShutdownNotifier;
  }

  @Override
  public List<Set<Integer>> computePartitioning(
      int pNumPartitions,
      PartialReachedSetDirectedGraph pGraph)
      throws InterruptedException {
    Deque<Integer> waitlist = new ArrayDeque<>();
    BitSet inPartition = new BitSet(pGraph.getNumNodes());
    int partitionSize = pGraph.getNumNodes() / pNumPartitions + 1;

    List<Set<Integer>> result = new ArrayList<>(pNumPartitions);
    for (int i = 0; i < pNumPartitions; i++) {
      result.add(Sets.<Integer>newHashSetWithExpectedSize(partitionSize));
    }

    waitlist.add(0);
    inPartition.set(0);

    Integer next;
    int partition = 0;
    ImmutableList<ImmutableList<Integer>> adjacencyList = pGraph.getAdjacencyList();
    while (!waitlist.isEmpty()) {
      shutdownNotifier.shutdownIfNecessary();

      next = waitlist.poll();
      if (result.get(partition).size() > partitionSize) {
        partition++;
      }
      result.get(partition).add(next);

      for (Integer successor : adjacencyList.get(next)) {
        if (!inPartition.get(successor)) {
          inPartition.set(successor);
          if (useDFS) {
            waitlist.push(successor);
          } else {
            waitlist.offer(successor);
          }
        }
      }
    }

    return result;
  }

}
