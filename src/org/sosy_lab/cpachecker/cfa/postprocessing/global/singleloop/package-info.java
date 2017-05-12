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

/**
 * Post-processing for the CFA that merges all loops into a large single loop,
 * introducing a separate variable that tracks the program counter.
 */
package org.sosy_lab.cpachecker.cfa.postprocessing.global.singleloop;