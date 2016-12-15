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
package org.eclipse.che.ide.command.palette;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandGoalRegistry;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.command.goal.CommonGoal;
import org.eclipse.che.ide.command.node.CommandGoalNode;
import org.eclipse.che.ide.command.node.ExecutableCommandNode;
import org.eclipse.che.ide.command.node.NodeFactory;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.window.Window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.api.workspace.shared.Constants.COMMAND_GOAL_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.COMMAND_PREVIEW_URL_ATTRIBUTE_NAME;

/**
 * Implementation of {@link CommandPaletteView}.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CommandPaletteViewImpl extends Window implements CommandPaletteView {

    private static final CommandsPaletteViewImplUiBinder UI_BINDER = GWT.create(CommandsPaletteViewImplUiBinder.class);

    private final CommandGoalRegistry commandGoalRegistry;
    private final NodeFactory         nodeFactory;

    @UiField
    TextBox filterField;

    @UiField(provided = true)
    Tree tree;

    private ActionDelegate delegate;

    @Inject
    public CommandPaletteViewImpl(CommandGoalRegistry commandGoalRegistry, NodeFactory nodeFactory) {
        this.commandGoalRegistry = commandGoalRegistry;
        this.nodeFactory = nodeFactory;

        tree = new Tree(new NodeStorage(), new NodeLoader());

        setWidget(UI_BINDER.createAndBindUi(this));

        setTitle("Command Palette");

        filterField.getElement().setAttribute("placeholder", "Search command");

        // hide footer
        getFooter().removeFromParent();
    }

    @Override
    public void show() {
        super.show();

        filterField.setValue("");

        filterField.setFocus(true);
    }

    @Override
    public void close() {
        hide();
    }

    @Override
    public void setCommands(List<ContextualCommand> commands) {
        final Map<CommandGoal, List<ContextualCommand>> commandsByGoal = new HashMap<>();

        for (ContextualCommand command : commands) {
            String goal = command.getAttributes().get(COMMAND_GOAL_ATTRIBUTE_NAME);
            if (goal == null) {
                goal = commandGoalRegistry.getCommandGoalById("common").getId();
            }

            final CommandGoal commandGoal = commandGoalRegistry.getCommandGoalById(goal);
            List<ContextualCommand> commandsOfType = commandsByGoal.get(commandGoal);

            if (commandsOfType == null) {
                commandsOfType = new ArrayList<>();
                commandsByGoal.put(commandGoal, commandsOfType);
            }

            commandsOfType.add(command);
        }

        renderCommands(commandsByGoal);
    }

    /** Render commands grouped by goals. */
    private void renderCommands(Map<CommandGoal, List<ContextualCommand>> commands) {
        tree.getNodeStorage().clear();

        for (Map.Entry<CommandGoal, List<ContextualCommand>> entry : commands.entrySet()) {
            List<ExecutableCommandNode> commandNodes = new ArrayList<>(entry.getValue().size());
            for (final ContextualCommand command : entry.getValue()) {
                commandNodes.add(nodeFactory.newExecutableCommandNode(command, new ExecutableCommandNode.ActionDelegate() {
                    @Override
                    public void actionPerformed() {
                        delegate.onCommandExecute(command);
                    }
                }));
            }

            final CommandGoalNode commandGoalNode = nodeFactory.newCommandGoalNode(entry.getKey(), commandNodes);
            tree.getNodeStorage().add(commandGoalNode);
        }

        tree.expandAll();
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @UiHandler({"filterField"})
    void onFilterChanged(@SuppressWarnings("UnusedParameters") KeyUpEvent event) {
        delegate.onFilterChanged(filterField.getValue());
    }

    interface CommandsPaletteViewImplUiBinder extends UiBinder<Widget, CommandPaletteViewImpl> {
    }
}
