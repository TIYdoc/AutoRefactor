/*
 * AutoRefactor - Eclipse plugin to automatically refactor Java code bases.
 *
 * Copyright (C) 2013 Jean-Noël Rouvignac - initial API and implementation
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
package org.autorefactor.samples_out;

import java.util.Collection;

public class SimplifyExpressionSample {

    public static void main(String[] args) {
        {
            // Remove redundant null checks
            boolean b1 = "".equals(args[0]);
            boolean b2 = args[0] instanceof String;
        }
        {
            // Remove redundant null checks
            boolean b1 = "".equals(args[0]);
            boolean b2 = args[0] instanceof String;
        }
        {
            // Remove redundant left / write hand operands
            boolean b3 = args[0] != null;
            boolean b4 = false;
            boolean b5 = true;
            boolean b6 = args[0] != null;
        }
        {
            // Right hand side left unchanged because left hand side can have
            // side effects
            boolean b3 = args[0] != null && true;
            boolean b4 = args[0] != null && false;
            boolean b5 = args[0] != null || true;
            boolean b6 = args[0] != null || false;
        }
    }

    public void compareToTest() {
        boolean b;
        final String s = "";

        // valid, do no change these ones
        b = s.compareTo("") < 0;
        b = s.compareTo("") <= 0;
        b = s.compareTo("") == 0;
        b = s.compareTo("") != 0;
        b = s.compareTo("") >= 0;
        b = s.compareTo("") > 0;
        b = s.compareToIgnoreCase("") == 0;

        // invalid, refactor them
        b = s.compareTo("") < 0;
        b = s.compareTo("") >= 0;
        b = s.compareTo("") <= 0;
        b = s.compareTo("") > 0;
        b = s.compareToIgnoreCase("") > 0;
    }

    public boolean simplifyExpression() throws Exception {
        boolean b = true;
        int i;
        Collection<?> col = null;
        i = 0;
        int[] ar = new int[i];
        ar = new int[]{i};
        ar[i] = i;
        if (b) {
            throw new Exception();
        }
        do {
        } while (b);
        while (b) {
        }
        for (Object obj : col) {
        }
        for (i = 0; b; i++) {
        }
        synchronized (col) {
        }
        switch (i) {
        case 0:
        }
        if (col instanceof Collection) {
        }
        return b;
    }
}