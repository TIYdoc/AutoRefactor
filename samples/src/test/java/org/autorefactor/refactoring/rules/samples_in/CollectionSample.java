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
package org.autorefactor.refactoring.rules.samples_in;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class CollectionSample {

    public void replaceNewNoArgsAssignmentThenAddAll(List<String> col, List<String> output) {
        output = new ArrayList<String>();
        output.addAll(col);
    }

    public List<String> replaceNewNoArgsThenAddAll(List<String> col) {
        final List<String> output = new ArrayList<String>();
        output.addAll(col);
        return output;
    }

    public List<String> replaceNew0ArgThenAddAll(List<String> col) {
        final List<String> output = new ArrayList<String>(0);
        output.addAll(col);
        return output;
    }

    public List<String> replaceNew1ArgThenAddAll(List<String> col) {
        final List<String> output = new ArrayList<String>(0);
        output.addAll(col);
        return output;
    }

    public List<String> replaceNewCollectionSizeThenAddAll(List<String> col) {
        final List<String> output = new ArrayList<String>(col.size());
        output.addAll(col);
        return output;
    }

    public void replaceAddWithForLoop(List<String> col, List<String> output) {
        for (int i = 0; i < col.size(); i++) {
            output.add(col.get(i));
        }
    }

    public void replaceAddWithForEach(Collection<String> col, List<String> output) {
        for (String s : col) {
            output.add(s);
        }
    }

    public void replaceContainsWithForLoop(List<String> col, List<String> output) {
        for (int i = 0; i < col.size(); i++) {
            output.contains(col.get(i));
        }
    }

    public void replaceContainsWithForEach(Collection<String> col, List<String> output) {
        for (String s : col) {
            output.contains(s);
        }
    }

    public void replaceRemoveWithForLoop(List<String> col, List<String> output) {
        for (int i = 0; i < col.size(); i++) {
            output.remove(col.get(i));
        }
    }

    public void replaceRemoveWithForEach(Collection<String> col, List<String> output) {
        for (String s : col) {
            output.remove(s);
        }
    }

    public void replaceChecksOnSize(Collection<String> col) {
        System.out.println(col.size() > 0);
        System.out.println(col.size() >= 0);
        System.out.println(col.size() == 0);
        System.out.println(col.size() <= 0);
        System.out.println(col.size() < 0);

        System.out.println(0 < col.size());
        System.out.println(0 <= col.size());
        System.out.println(0 == col.size());
        System.out.println(0 >= col.size());
        System.out.println(0 > col.size());
    }

    public void replaceCheckOnSetContainsBeforeAdd(Set<String> col, String s) {
        if (!col.contains(s)) {
            col.add(s);
            System.out.println("OK");
        } else {
            System.out.println("KO");
        }
    }

    public void replaceCheckOnSetContainsBeforeAdd2(Set<String> col, String s) {
        if (col.contains(s)) {
            System.out.println("KO");
        } else {
            col.add(s);
            System.out.println("OK");
        }
    }

    public void doNotReplaceWhenCheckedValueIsDifferent(Set<String> col) {
        if (!col.contains("this")) {
            col.add("that");
            System.out.println("OK");
        }
    }

    public void doNotReplaceCheckOnListContainsBeforeAdd(List<String> col, String s) {
        if (!col.contains(s)) {
            col.add(s);
            System.out.println("OK");
        } else {
            System.out.println("KO");
        }
    }
}
