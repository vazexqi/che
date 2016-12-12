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
package org.eclipse.che.ide.command.node;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.data.tree.HasAction;
import org.eclipse.che.ide.api.icon.IconRegistry;

/**
 * Extension of {@link AbstractCommandNode} that can execute
 * a command when performing an action is requested.
 *
 * @author Artem Zatsarynnyi
 * @see #actionPerformed()
 */
public class ExecutableCommandNode extends AbstractCommandNode implements HasAction {

    private final ActionDelegate actionDelegate;

    @Inject
    public ExecutableCommandNode(@Assisted ContextualCommand data,
                                 @Assisted ActionDelegate actionDelegate,
                                 CommandTypeRegistry commandTypeRegistry,
                                 IconRegistry iconRegistry) {
        super(data, null, commandTypeRegistry, iconRegistry);

        this.actionDelegate = actionDelegate;
    }

    @Override
    public void actionPerformed() {
        actionDelegate.actionPerformed();
    }

    /** Interface for delegating performing action on node. */
    public interface ActionDelegate {
        void actionPerformed();
    }
}
