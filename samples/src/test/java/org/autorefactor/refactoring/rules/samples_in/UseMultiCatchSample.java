/*
 * AutoRefactor - Eclipse plugin to automatically refactor Java code bases.
 *
 * Copyright (C) 2015 Jean-Noël Rouvignac - initial API and implementation
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
package org.autorefactor.refactoring.rules.samples_in;

import java.io.IOException;

import javax.naming.NamingException;

public class UseMultiCatchSample {

    private static final class ThrowingObject<E1 extends Throwable, E2 extends Throwable> {
        private void throwingMethod() throws E1, E2 {
        }
    }

    private static final class Ex1 extends Exception {
        private void print() {
        }

        private String getExplanation() {
            return "";
        }
    }

    private static final class Ex2 extends Exception {
        private void print() {
        }
    }

    private static final class OverridingException1 extends Exception {
        @Override
        public void printStackTrace() {
            super.printStackTrace();
        }
    }

    private static final class OverridingException2 extends Exception {
        @Override
        public void printStackTrace() {
            super.printStackTrace();
        }
    }

    public void refactorMultiCatch(ThrowingObject<IllegalArgumentException, IOException> obj) {
        try {
            obj.throwingMethod();
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void refactorAddToMultiCatch(ThrowingObject<IllegalArgumentException, IOException> obj) {
        try {
            obj.throwingMethod();
        } catch (IllegalArgumentException | IllegalStateException iae) {
            iae.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void removeMoreSpecializedException(ThrowingObject<IllegalArgumentException, RuntimeException> obj) {
        try {
            obj.throwingMethod();
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        } catch (RuntimeException re) {
            re.printStackTrace();
        }
    }

    public void refactorMultiCatchWithOverridenMethods(ThrowingObject<IllegalArgumentException, OverridingException1> obj) {
        try {
            obj.throwingMethod();
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        } catch (OverridingException1 oe1) {
            oe1.printStackTrace();
        }
    }

    public void refactorMultiCatchWithOverridenMethodsFromSupertype(ThrowingObject<OverridingException1, OverridingException2> obj) {
        try {
            obj.throwingMethod();
        } catch (OverridingException1 oe1) {
            oe1.printStackTrace();
        } catch (OverridingException2 oe2) {
            oe2.printStackTrace();
        }
    }

    public void doNotRefactorMultiCatchWithNoOverridenMethods(ThrowingObject<NamingException, Ex1> obj) {
        try {
            obj.throwingMethod();
        } catch (NamingException ne) {
            ne.getExplanation();
        } catch (Ex1 ex1) {
            ex1.getExplanation();
        }
    }

    public void doNotRefactorNoCommonSuperType(ThrowingObject<Ex1, Ex2> obj) {
        try {
            obj.throwingMethod();
        } catch (Ex1 e1) {
            e1.print();
        } catch (Ex2 e2) {
            e2.print();
        }
    }

    public void doNotRefactorChangeInBehaviourClassHierarchy(ThrowingObject<IllegalArgumentException, Exception> obj) {
        try {
            obj.throwingMethod();
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        } catch (Exception ioe) {
            ioe.toString();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void refactorForward(ThrowingObject<IllegalArgumentException, NamingException> obj) {
        try {
            obj.throwingMethod();
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        } catch (RuntimeException re) {
            re.toString();
        } catch (NamingException ne) {
            ne.printStackTrace();
        }
    }

    public void refactorBackward(ThrowingObject<NamingException, RuntimeException> obj) {
        try {
            obj.throwingMethod();
        } catch (NamingException iae) {
            iae.printStackTrace();
        } catch (RuntimeException ioe) {
            ioe.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
