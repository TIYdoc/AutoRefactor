/*
 * AutoRefactor - Eclipse plugin to automatically refactor Java code bases.
 *
 * Copyright (C) 2014-2015 Jean-Noël Rouvignac - initial API and implementation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program under LICENSE-GNUGPL.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution under LICENSE-ECLIPSE, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.autorefactor.refactoring.rules;

import java.util.List;

import org.autorefactor.refactoring.ASTBuilder;
import org.autorefactor.refactoring.ASTBuilder.Copy;
import org.autorefactor.refactoring.ForLoopHelper.ForLoopContent;
import org.autorefactor.refactoring.Refactorings;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import static org.autorefactor.refactoring.ASTHelper.*;
import static org.autorefactor.refactoring.ForLoopHelper.*;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.*;
import static org.eclipse.jdt.core.dom.PrefixExpression.Operator.*;

/** See {@link #getDescription()} method. */
@SuppressWarnings("javadoc")
public class CollectionRefactoring extends AbstractRefactoringRule {

    @Override
    public String getDescription() {
        return ""
            + "Collection related refactorings:\n"
            + "- replaces for/foreach loops to use Collection.addAll() where possible,\n"
            + "- replaces for/foreach loops to use Collection.containsAll() where possible,\n"
            + "- replaces for/foreach loops to use Collection.removeAll() where possible,\n"
            + "- replaces creating a new Collection, then invoking Collection.addAll() on it,"
            + " by creating the new Collection with the other Collection as parameter,\n"
            + "- replaces some checks on Collection.size() with checks on Collection.isEmpty(),\n"
            + "- replaces calls to Set.contains() immediately followed by Set.add() with straight calls to Set.add().";
    }

    @Override
    public String getName() {
        return "Collection";
    }

    @Override
    public boolean visit(ExpressionStatement node) {
        final MethodInvocation mi = asExpression(node, MethodInvocation.class);
        if (isMethod(mi, "java.util.Collection", "addAll", "java.util.Collection")) {
            final Expression arg0 = arg0(mi);
            final Statement previousStmt = getPreviousSibling(node);

            final Assignment as = asExpression(previousStmt, Assignment.class);
            if (as != null) {
                final Expression lhs = as.getLeftHandSide();
                if (lhs instanceof SimpleName) {
                    final SimpleName sn = (SimpleName) lhs;
                    if (isSameLocalVariable(mi.getExpression(), sn.resolveBinding())) {
                        return replaceInitializer(as.getRightHandSide(), arg0, node);
                    }
                }
            } else if (previousStmt instanceof VariableDeclarationStatement) {
                final VariableDeclarationStatement vds = (VariableDeclarationStatement) previousStmt;
                if (vds.fragments().size() == 1) {
                    final VariableDeclarationFragment vdf = fragments(vds).get(0);
                    if (isSameLocalVariable(mi.getExpression(), vdf.resolveBinding())) {
                        return replaceInitializer(vdf.getInitializer(), arg0, node);
                    }
                }
            }
        }
        return VISIT_SUBTREE;
    }

    private boolean replaceInitializer(Expression nodeToReplace,
            final Expression arg0, ExpressionStatement nodeToRemove) {
        final ClassInstanceCreation cic = as(nodeToReplace, ClassInstanceCreation.class);
        if (canReplaceInitializer(cic)) {
            final ASTBuilder b = this.ctx.getASTBuilder();
            this.ctx.getRefactorings().replace(nodeToReplace,
                    b.new0(cic.resolveTypeBinding(),
                            b.copy(arg0)));
            this.ctx.getRefactorings().remove(nodeToRemove);
            return DO_NOT_VISIT_SUBTREE;
        }
        return VISIT_SUBTREE;
    }

    private boolean canReplaceInitializer(final ClassInstanceCreation cic) {
        if (cic == null) {
            return false;
        }
        final List<Expression> args = arguments(cic);
        final boolean noArgsCtor = args.size() == 0;
        final boolean colCapacityCtor = args.size() == 1 && isPrimitive(args.get(0), "int");
        if (noArgsCtor && hasType(cic,
                "java.util.concurrent.ConcurrentLinkedDeque",
                "java.util.concurrent.ConcurrentLinkedQueue",
                "java.util.concurrent.ConcurrentSkipListSet",
                "java.util.concurrent.CopyOnWriteArrayList",
                "java.util.concurrent.CopyOnWriteArraySet",
                "java.util.concurrent.DelayQueue",
                "java.util.concurrent.LinkedBlockingDeque",
                "java.util.concurrent.LinkedBlockingQueue",
                "java.util.concurrent.LinkedTransferQueue",
                "java.util.concurrent.PriorityBlockingQueue",
                "java.util.concurrent.SynchronousQueue",
                "java.util.ArrayDeque",
                "java.util.ArrayList",
                "java.util.HashSet",
                "java.util.LinkedHashSet",
                "java.util.LinkedList",
                "java.util.PriorityQueue",
                "java.util.Stack",
                "java.util.TreeSet",
                "java.util.Vector")) {
            return true;
        }
        if (colCapacityCtor && hasType(cic,
                "java.util.concurrent.LinkedBlockingDeque",
                "java.util.concurrent.LinkedBlockingQueue",
                "java.util.concurrent.PriorityBlockingQueue",
                "java.util.ArrayDeque",
                "java.util.ArrayList",
                "java.util.HashSet",
                "java.util.LinkedHashSet",
                "java.util.PriorityQueue",
                "java.util.Vector")) {
            // TODO JNR verify capacity arguments is 0, 1 or col.length
            return true;
        }
        return false;
    }

    @Override
    public boolean visit(EnhancedForStatement node) {
        final Expression collection = node.getExpression();
        if (instanceOf(collection, "java.util.Collection")) {
            final List<Statement> stmts = asList(node.getBody());
            if (stmts.size() == 1) {
                final MethodInvocation mi = asExpression(stmts.get(0), MethodInvocation.class);
                if (isMethod(mi, "java.util.Collection", "add", "java.lang.Object")) {
                    return replaceWithCollectionMethod(node, collection, "addAll", mi);
                } else if (isMethod(mi, "java.util.Collection", "contains", "java.lang.Object")) {
                    return replaceWithCollectionMethod(node, collection, "containsAll", mi);
                } else if (isMethod(mi, "java.util.Collection", "remove", "java.lang.Object")) {
                    return replaceWithCollectionMethod(node, collection, "removeAll", mi);
                }
            }
        }
        return VISIT_SUBTREE;
    }

    private boolean replaceWithCollectionMethod(EnhancedForStatement node,
            Expression collection, String methodName, MethodInvocation colMI) {
        final SingleVariableDeclaration foreachVariable = node.getParameter();
        if (isSameLocalVariable(arg0(colMI), foreachVariable.resolveBinding())) {
            return replaceWithCollectionMethod(node, methodName,
                    colMI.getExpression(), collection);
        }
        return VISIT_SUBTREE;
    }

    @Override
    public boolean visit(ForStatement node) {
        final ForLoopContent loopContent = iterateOverContainer(node);
        final List<Statement> stmts = asList(node.getBody());
        if (loopContent != null && stmts.size() == 1) {
            final MethodInvocation mi = asExpression(stmts.get(0), MethodInvocation.class);
            if (isMethod(mi, "java.util.Collection", "add", "java.lang.Object")) {
                return replaceWithCollectionMethod(node, loopContent, "addAll", mi);
            } else if (isMethod(mi, "java.util.Collection", "contains", "java.lang.Object")) {
                return replaceWithCollectionMethod(node, loopContent, "containsAll", mi);
            } else if (isMethod(mi, "java.util.Collection", "remove", "java.lang.Object")) {
                return replaceWithCollectionMethod(node, loopContent, "removeAll", mi);
            }
        }
        return VISIT_SUBTREE;
    }

    private boolean replaceWithCollectionMethod(ForStatement node, ForLoopContent loopContent,
            String methodName, MethodInvocation colMI) {
        final Expression addArg0 = arg0(colMI);
        final MethodInvocation getMI = as(addArg0, MethodInvocation.class);
        if (isMethod(getMI, "java.util.List", "get", "int")
                && getMI.getExpression() instanceof Name) {
            final Expression getArg0 = arg0(getMI);
            if (getArg0 instanceof Name
                    && isSameLocalVariable(getArg0, loopContent.getLoopVariable())) {
                return replaceWithCollectionMethod(node, methodName,
                        colMI.getExpression(), getMI.getExpression());
            }
        }
        return VISIT_SUBTREE;
    }

    private boolean replaceWithCollectionMethod(ASTNode toReplace, String methodName,
            Expression colWhereToAddAll, Expression colToAddAll) {
        final ASTBuilder b = this.ctx.getASTBuilder();
        this.ctx.getRefactorings().replace(toReplace,
                b.toStmt(b.invoke(
                        b.copy(colWhereToAddAll),
                        methodName,
                        b.copy(colToAddAll))));
        return DO_NOT_VISIT_SUBTREE;
    }

    private boolean isSameLocalVariable(Expression expr, IBinding varBinding) {
        return expr instanceof SimpleName && varBinding instanceof IVariableBinding
            && ((SimpleName) expr).resolveBinding().equals(varBinding);
    }

    private boolean isSameLocalVariable(Expression expr1, Expression expr2) {
        if (expr1 instanceof SimpleName && expr2 instanceof SimpleName) {
            final IBinding binding1 = ((SimpleName) expr1).resolveBinding();
            final IBinding binding2 = ((SimpleName) expr2).resolveBinding();
            if (binding1 instanceof IVariableBinding) {
                return binding1.equals(binding2);
            }
        }
        return false;
    }

    @Override
    public boolean visit(InfixExpression node) {
        final MethodInvocation leftMi = as(node.getLeftOperand(), MethodInvocation.class);
        final MethodInvocation rightMi = as(node.getRightOperand(), MethodInvocation.class);
        if (isMethod(leftMi, "java.util.Collection", "size")
                && isZero(node.getRightOperand())) {
            return replaceCollectionSize(node, leftMi);
        } else if (isMethod(rightMi, "java.util.Collection", "size")
                && isZero(node.getLeftOperand())) {
            return replaceInverseCollectionSize(node, rightMi);
        }
        return VISIT_SUBTREE;
    }

    private boolean replaceCollectionSize(InfixExpression node, MethodInvocation miToReplace) {
        final Refactorings r = this.ctx.getRefactorings();
        final ASTBuilder b = this.ctx.getASTBuilder();
        final Expression copyOfExpr = b.copyExpression(miToReplace);
        if (hasOperator(node, GREATER)) {
            r.replace(node,
                    b.not(b.invoke(copyOfExpr, "isEmpty")));
            return DO_NOT_VISIT_SUBTREE;
        } else if (hasOperator(node, GREATER_EQUALS)) {
            r.replace(node,
                    b.boolean0(true));
            return DO_NOT_VISIT_SUBTREE;
        } else if (hasOperator(node, EQUALS)) {
            r.replace(node,
                    b.invoke(copyOfExpr, "isEmpty"));
            return DO_NOT_VISIT_SUBTREE;
        } else if (hasOperator(node, LESS_EQUALS)) {
            r.replace(node,
                    b.invoke(copyOfExpr, "isEmpty"));
            return DO_NOT_VISIT_SUBTREE;
        } else if (hasOperator(node, LESS)) {
            r.replace(node,
                    b.boolean0(false));
        }
        return VISIT_SUBTREE;
    }

    private boolean replaceInverseCollectionSize(InfixExpression node, MethodInvocation miToReplace) {
        final Refactorings r = this.ctx.getRefactorings();
        final ASTBuilder b = this.ctx.getASTBuilder();
        final Expression copyOfExpr = b.copyExpression(miToReplace);
        if (hasOperator(node, LESS)) {
            r.replace(node,
                    b.not(b.invoke(copyOfExpr, "isEmpty")));
            return DO_NOT_VISIT_SUBTREE;
        } else if (hasOperator(node, LESS_EQUALS)) {
            r.replace(node,
                    b.boolean0(true));
            return DO_NOT_VISIT_SUBTREE;
        } else if (hasOperator(node, EQUALS)) {
            r.replace(node,
                    b.invoke(copyOfExpr, "isEmpty"));
            return DO_NOT_VISIT_SUBTREE;
        } else if (hasOperator(node, GREATER_EQUALS)) {
            r.replace(node,
                    b.invoke(copyOfExpr, "isEmpty"));
            return DO_NOT_VISIT_SUBTREE;
        } else if (hasOperator(node, GREATER)) {
            r.replace(node,
                    b.boolean0(false));
        }
        return VISIT_SUBTREE;
    }

    private boolean isZero(Expression expr) {
        if (expr != null) {
            final Object val = expr.resolveConstantExpressionValue();
            if (val instanceof Integer) {
                return ((Integer) val).intValue() == 0;
            } else if (val instanceof Long) {
                return ((Long) val).longValue() == 0;
            }
        }
        return false;
    }

    @Override
    public boolean visit(IfStatement node) {
        final PrefixExpression pe = as(node.getExpression(), PrefixExpression.class);
        if (hasOperator(pe, NOT)) {
            final MethodInvocation miContains = as(pe.getOperand(), MethodInvocation.class);
            return maybeReplaceSetContains(miContains, node.getThenStatement(), pe, false);
        } else {
            final MethodInvocation miContains = as(node.getExpression(), MethodInvocation.class);
            return maybeReplaceSetContains(miContains, node.getElseStatement(), miContains, true);
        }
    }

    private boolean maybeReplaceSetContains(
            MethodInvocation miContains, Statement stmt, Expression toReplace, boolean negate) {
        if (isMethod(miContains, "java.util.Set", "contains", "java.lang.Object")) {
            Statement firstStmt = getAsList(stmt, 0);
            MethodInvocation miAdd = asExpression(firstStmt, MethodInvocation.class);
            if (isMethod(miAdd, "java.util.Set", "add", "java.lang.Object")
                    && match(new ASTMatcher(), arg0(miContains), arg0(miAdd))) {
                ASTBuilder b = this.ctx.getASTBuilder();
                Refactorings r = this.ctx.getRefactorings();
                r.replace(toReplace, negate ? b.negate(miAdd, Copy.COPY.MOVE) : b.move(miAdd));
                r.remove(firstStmt);
                return DO_NOT_VISIT_SUBTREE;
            }
        }
        return VISIT_SUBTREE;
    }

    private Statement getAsList(Statement stmt, int index) {
        if (index < 0) {
            throw new IllegalArgumentException("A list index cannot be negative. Given: " + index);
        }
        final List<Statement> stmts = asList(stmt);
        if (stmts.size() > index) {
            return stmts.get(index);
        }
        return null;
    }
}
