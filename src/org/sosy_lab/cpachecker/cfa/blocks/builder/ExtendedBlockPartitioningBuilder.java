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
package org.sosy_lab.cpachecker.cfa.blocks.builder;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import org.sosy_lab.cpachecker.cfa.blocks.Block;
import org.sosy_lab.cpachecker.cfa.blocks.BlockPartitioning;
import org.sosy_lab.cpachecker.cfa.blocks.ReferencedVariable;
import org.sosy_lab.cpachecker.cfa.model.CFANode;
import org.sosy_lab.cpachecker.cfa.model.FunctionEntryNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Class implements more intelligent partitioning building,
 * but it is still not working for loops, because they may have more then one entry node into block
 * Current method much more faster handles difficult structures, containing recursive calls,
 * because it doesn't wait a fixpoint
 */
public class ExtendedBlockPartitioningBuilder extends BlockPartitioningBuilder {

  @Override
  public BlockPartitioning build(CFANode mainFunction) {

    Map<CFANode, Set<FunctionEntryNode>> workCopyOfInnerFunctionCalls = new HashMap<>();

    /* We chose one representative from every loop.
     * This map stores for the node its representative.
     */
    Map<CFANode, CFANode> loopMapping = new HashMap<>();

    //Deep clone, because we will delete nodes
    for (CFANode node : innerFunctionCallsMap.keySet()) {
      Set<FunctionEntryNode> newSet = Sets.newHashSet(innerFunctionCallsMap.get(node));
      workCopyOfInnerFunctionCalls.put(node, newSet);
    }

    //Set of not handled CFANodes
    Set<CFANode> workCopyOfCFANodes = Sets.newHashSet(referencedVariablesMap.keySet());
    do {
      //loop for finding single nodes
      Set<CFANode> SingleNodes = new HashSet<>();
      do {
        SingleNodes.clear();
        for (CFANode node : workCopyOfCFANodes) {
          Set<FunctionEntryNode> calls = workCopyOfInnerFunctionCalls.get(node);
          //Remove single-node loop
          calls.remove(node);
          if (calls.isEmpty()) {
            SingleNodes.add(node);
          }
        }
        if (!SingleNodes.isEmpty()) {
          workCopyOfCFANodes.removeAll(SingleNodes);

          for (CFANode node : workCopyOfCFANodes) {
            Iterator<FunctionEntryNode> iterator =
                workCopyOfInnerFunctionCalls.get(node).iterator();
            while (iterator.hasNext()) {
              FunctionEntryNode calledFun = iterator.next();
              if (SingleNodes.contains(calledFun)) {
                iterator.remove();
                joinFunctionPartitioning(node, calledFun);
              }
            }
          }
        }
      } while (!SingleNodes.isEmpty());

      if (workCopyOfCFANodes.isEmpty()) {
        break;
      }
      //Detect a recursion loop
      LinkedList<CFANode> foundedRecursionLoop = new LinkedList<>();
      /* Create a random path by getting the first function call from every node
       * and find the first node which is repeated
       */
      CFANode representativeNode = workCopyOfCFANodes.iterator().next();
      while (!foundedRecursionLoop.contains(representativeNode)) {
        foundedRecursionLoop.add(representativeNode);
        representativeNode = workCopyOfInnerFunctionCalls.get(representativeNode).iterator().next();
        assert (representativeNode != null);
      }

      Set<FunctionEntryNode> functionsCalledFromTheLoop = new HashSet<>();
      //Remove the first elements, which are not included in the loop
      while (!foundedRecursionLoop.pollFirst().equals(representativeNode)) {
      }
      //Join all partitions of functions from the loop
      for (CFANode recursiveCaller : foundedRecursionLoop) {
        loopMapping.put(recursiveCaller, representativeNode);
        joinFunctionPartitioning(representativeNode, recursiveCaller);
        workCopyOfCFANodes.remove(recursiveCaller);
        functionsCalledFromTheLoop.addAll(workCopyOfInnerFunctionCalls.get(recursiveCaller));
        //Remove the handled node from others
        for (CFANode node : workCopyOfCFANodes) {
          Set<FunctionEntryNode> callers = workCopyOfInnerFunctionCalls.get(node);
          if (callers.remove(recursiveCaller)) {
            //Do not add single node loops
            if (!node.equals(representativeNode)) {
              // We should add to callers one node from the loop instead of removed one.
              callers.add((FunctionEntryNode) representativeNode);
            }
          }
        }
      }
      foundedRecursionLoop.add(representativeNode);
      functionsCalledFromTheLoop.removeAll(foundedRecursionLoop);
      //Add to chosen node (representative) all function calls from removed functions
      workCopyOfInnerFunctionCalls.get(representativeNode).addAll(functionsCalledFromTheLoop);

    } while (!workCopyOfCFANodes.isEmpty());

    //Try to optimize the memory
    Map<CFANode, ImmutableSet<ReferencedVariable>> immutableVariablesMap = new HashMap<>();
    Map<CFANode, ImmutableSet<CFANode>> immutableNodesMap = new HashMap<>();
    //Resolve loop mapping
    for (CFANode node : loopMapping.keySet()) {
      CFANode mappedNode = loopMapping.get(node);
      while (loopMapping.containsKey(mappedNode)) {
        mappedNode = loopMapping.get(mappedNode);
      }
      /* We put the same object, because new Block() makes copy of these maps,
       * so we do not care about this equality
       */
      ImmutableSet<ReferencedVariable> resultVars;
      ImmutableSet<CFANode> resultNodes;
      if (!immutableVariablesMap.containsKey(mappedNode)) {
        resultVars = ImmutableSet.copyOf(referencedVariablesMap.get(mappedNode));
        immutableVariablesMap.put(mappedNode, resultVars);
      } else {
        resultVars = immutableVariablesMap.get(mappedNode);
      }
      immutableVariablesMap.put(node, resultVars);
      if (!immutableNodesMap.containsKey(mappedNode)) {
        resultNodes = ImmutableSet.copyOf(blockNodesMap.get(mappedNode));
        immutableNodesMap.put(mappedNode, resultNodes);
      } else {
        resultNodes = immutableNodesMap.get(mappedNode);
      }
      immutableNodesMap.put(node, resultNodes);
    }

    //now we can create the Blocks for the BlockPartitioning
    Collection<Block> blocks = new ArrayList<>(returnNodesMap.keySet().size());
    for (CFANode key : returnNodesMap.keySet()) {
      if (immutableVariablesMap.containsKey(key)) {
        assert immutableNodesMap.containsKey(key);
        blocks.add(new Block(immutableVariablesMap.get(key), callNodesMap.get(key),
            returnNodesMap.get(key), immutableNodesMap.get(key)));
      } else {
        blocks.add(
            new Block(ImmutableSet.copyOf(referencedVariablesMap.get(key)), callNodesMap.get(key),
                returnNodesMap.get(key), ImmutableSet.copyOf(blockNodesMap.get(key))));
      }
    }
    return new BlockPartitioning(blocks, mainFunction);
  }

  private void joinFunctionPartitioning(CFANode node, CFANode caller) {
    Set<ReferencedVariable> functionVars = referencedVariablesMap.get(caller);
    Set<CFANode> functionBody = blockNodesMap.get(caller);
    referencedVariablesMap.get(node).addAll(functionVars);
    blockNodesMap.get(node).addAll(functionBody);
  }
}
