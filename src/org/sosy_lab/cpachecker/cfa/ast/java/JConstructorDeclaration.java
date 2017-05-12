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
package org.sosy_lab.cpachecker.cfa.ast.java;

import org.sosy_lab.cpachecker.cfa.ast.FileLocation;
import org.sosy_lab.cpachecker.cfa.types.java.JClassOrInterfaceType;
import org.sosy_lab.cpachecker.cfa.types.java.JClassType;
import org.sosy_lab.cpachecker.cfa.types.java.JConstructorType;
import org.sosy_lab.cpachecker.cfa.types.java.JType;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the Constructor declaration AST node type.
 *
 * ConstructorDeclaration:
 * [ Javadoc ] { ExtendedModifier }
 * [ < TypeParameter { , TypeParameter } > ]
 * Identifier (
 * [ FormalParameter
 * { , FormalParameter } ] )
 * [throws TypeName { , TypeName } ] Block
 *
 * The constructor declaration is a method declaration represented
 * in {@link JMethodDeclaration}, who's return type is denoted as
 * the class type it was declared in. Additionally, not all valid
 * method modifiers are valid for a constructor, e.g. abstract
 * static, native, synchronized, final.
 */
public class JConstructorDeclaration extends JMethodDeclaration {

  private static final JConstructorDeclaration UNRESOLVED_CONSTRUCTOR =
      new JConstructorDeclaration(FileLocation.DUMMY,
          JConstructorType.createUnresolvableConstructorType(), "__UNRESOLVABLE__",
          "__UNRESOLVABLE__", new ArrayList<JParameterDeclaration>(), VisibilityModifier.NONE,
          false, JClassType.createUnresolvableType());

  public JConstructorDeclaration(
      FileLocation pFileLocation,
      JConstructorType pType, String pName, String simpleName,
      List<JParameterDeclaration> pParameterDeclarations,
      VisibilityModifier pVisibility, boolean pIsStrictfp, JClassOrInterfaceType declaringClass) {
    super(pFileLocation, pType, pName, simpleName, pParameterDeclarations, pVisibility,
        false, false, false, false, false,
        pIsStrictfp, declaringClass);
  }

  @Override
  public JConstructorType getType() {
    return (JConstructorType) super.getType();
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = 7;
    return prime * result + super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof JConstructorDeclaration)) {
      return false;
    }

    return super.equals(obj);
  }

  public static JConstructorDeclaration createUnresolvedConstructorDeclaration() {
    return UNRESOLVED_CONSTRUCTOR;
  }

  public static JConstructorDeclaration createExternConstructorDeclaration(
      JConstructorType pConstructorType,
      String pName, String simpleName,
      VisibilityModifier pVisibility,
      boolean pStrictFp, JClassType pDeclaringClassType) {

    List<JType> parameterTypes = pConstructorType.getParameters();
    List<JParameterDeclaration> parameters = new ArrayList<>(parameterTypes.size());

    FileLocation externFileLoc = FileLocation.DUMMY;


    int i = 0;

    for (JType parameterType : parameterTypes) {
      final String parameterName = "parameter" + String.valueOf(i);
      parameters.add(
          new JParameterDeclaration(externFileLoc, parameterType, parameterName,
              pName + "::" + parameterName, false));
      i++;
    }

    return new JConstructorDeclaration(externFileLoc, pConstructorType,
        pName, simpleName, parameters, pVisibility, pStrictFp, pDeclaringClassType);
  }
}