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
package org.eclipse.che.ide.command.explorer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.command.node.CommandFileNode;
import org.eclipse.che.ide.command.node.CommandTypeNode;
import org.eclipse.che.ide.command.node.NodeFactory;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.ui.smartTree.SelectionModel.Mode.SINGLE;

/**
 * Implementation of {@link CommandsExplorerView}.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CommandsExplorerViewImpl extends BaseView<CommandsExplorerView.ActionDelegate> implements CommandsExplorerView {

    private static final CommandsExplorerViewImplUiBinder UI_BINDER = GWT.create(CommandsExplorerViewImplUiBinder.class);

    private final CommandsTreeRenderer treeRenderer;
    private final NodeFactory          nodeFactory;

    @UiField(provided = true)
    Tree tree;

    @Inject
    public CommandsExplorerViewImpl(org.eclipse.che.ide.Resources coreResources,
                                    CommandsExplorerResources resources,
                                    NodeFactory nodeFactory) {
        super(coreResources);

        this.nodeFactory = nodeFactory;

        resources.commandsExplorerCss().ensureInjected();

        setTitle("Commands Explorer");

        tree = new Tree(new NodeStorage(), new NodeLoader());

        treeRenderer = new CommandsTreeRenderer(tree.getTreeStyles(), resources, delegate);

        tree.setPresentationRenderer(treeRenderer);
        tree.getSelectionModel().setSelectionMode(SINGLE);

        tree.getSelectionModel().addSelectionHandler(new SelectionHandler<Node>() {
            @Override
            public void onSelection(SelectionEvent<Node> event) {
                final Node selectedNode = event.getSelectedItem();
                if (selectedNode instanceof CommandTypeNode) {
                    delegate.onCommandTypeSelected(((CommandTypeNode)selectedNode).getData());
                } else if (selectedNode instanceof CommandFileNode) {
                    delegate.onCommandSelected(((CommandFileNode)selectedNode).getData());
                }

                for (Node node : tree.getNodeStorage().getAll()) {
                    tree.getNodeDescriptor(node).getNodeContainerElement().removeAttribute("selected");
                }

                tree.getNodeDescriptor(selectedNode).getNodeContainerElement().setAttribute("selected", "selected");
            }
        });

        setContentWidget(UI_BINDER.createAndBindUi(this));
    }


    @Override
    public void setCommands(Map<CommandType, List<ContextualCommand>> commands) {
        treeRenderer.setDelegate(delegate);

        renderCommands(commands);
    }

    private void renderCommands(Map<CommandType, List<ContextualCommand>> commands) {
        tree.getNodeStorage().clear();

        for (Map.Entry<CommandType, List<ContextualCommand>> entry : commands.entrySet()) {
            List<CommandFileNode> commandNodes = new ArrayList<>(entry.getValue().size());
            for (ContextualCommand command : entry.getValue()) {
                commandNodes.add(nodeFactory.newCommandFileNode(command));
            }

            final CommandTypeNode commandTypeNode = nodeFactory.newCommandTypeNode(entry.getKey(), commandNodes);
            tree.getNodeStorage().add(commandTypeNode);
        }

        tree.expandAll();
    }

    @Nullable
    @Override
    public CommandType getSelectedCommandType() {
        final List<Node> selectedNodes = tree.getSelectionModel().getSelectedNodes();

        if (!selectedNodes.isEmpty()) {
            final Node selectedNode = selectedNodes.get(0);
            if (selectedNode instanceof CommandTypeNode) {
                return ((CommandTypeNode)selectedNode).getData();
            }
        }

        return null;
    }

    @Nullable
    @Override
    public ContextualCommand getSelectedCommand() {
        final List<Node> selectedNodes = tree.getSelectionModel().getSelectedNodes();

        if (!selectedNodes.isEmpty()) {
            final Node selectedNode = selectedNodes.get(0);
            if (selectedNode instanceof CommandFileNode) {
                return ((CommandFileNode)selectedNode).getData();
            }
        }

        return null;
    }

    @Override
    public void selectCommand(ContextualCommand command) {
        // TODO
//        commandsTree.getSelectionModel().setSelection(new ArrayList<Node>());
    }

    interface CommandsExplorerViewImplUiBinder extends UiBinder<Widget, CommandsExplorerViewImpl> {
    }
}
