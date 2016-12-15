/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/

package org.eclipse.che.ide.command.goal;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandGoalRegistry;
import org.eclipse.che.ide.util.loging.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableList;

/**
 * Implementation of {@link CommandGoalRegistry}.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CommandGoalRegistryImpl implements CommandGoalRegistry {

    private final Map<String, CommandGoal> commandGaols;

    public CommandGoalRegistryImpl() {
        this.commandGaols = new HashMap<>();
    }

    @Inject(optional = true)
    private void register(Set<CommandGoal> commandGoals) {
        for (CommandGoal type : commandGoals) {
            final String id = type.getId();

            if (this.commandGaols.containsKey(id)) {
                Log.warn(getClass(), "Command goal with ID " + id + " is already registered.");
            } else {
                this.commandGaols.put(id, type);
            }
        }
    }

    @Nullable
    @Override
    public CommandGoal getCommandGoalById(String id) {
        return commandGaols.get(id);
    }

    @Override
    public List<CommandGoal> getCommandGoals() {
        return unmodifiableList(new ArrayList<>(commandGaols.values()));
    }
}
