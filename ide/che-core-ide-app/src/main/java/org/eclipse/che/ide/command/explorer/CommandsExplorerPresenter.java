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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.DelayedTask;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.command.ContextualCommand.ApplicableContext;
import org.eclipse.che.ide.api.command.ContextualCommandManager;
import org.eclipse.che.ide.api.command.PredefinedCommandGoalRegistry;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.command.CommandUtils;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.parts.PartStackType.NAVIGATION;

/**
 * Presenter for Commands Explorer.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CommandsExplorerPresenter extends BasePresenter implements CommandsExplorerView.ActionDelegate,
                                                                        WsAgentStateHandler,
                                                                        ContextualCommandManager.CommandChangedListener {

    private static final String TITLE   = "Commands";
    private static final String TOOLTIP = "Manage commands";

    private final CommandsExplorerView          view;
    private final CommandsExplorerResources     resources;
    private final WorkspaceAgent                workspaceAgent;
    private final ContextualCommandManager      commandManager;
    private final PredefinedCommandGoalRegistry goalRegistry;
    private final NotificationManager           notificationManager;
    private final CommandTypeChooser            commandTypeChooser;
    private final CommandUtils                  commandUtils;

    private final RefreshViewTask refreshViewTask;

    @Inject
    public CommandsExplorerPresenter(CommandsExplorerView view,
                                     CommandsExplorerResources commandsExplorerResources,
                                     WorkspaceAgent workspaceAgent,
                                     EventBus eventBus,
                                     ContextualCommandManager commandManager,
                                     PredefinedCommandGoalRegistry predefinedCommandGoalRegistry,
                                     NotificationManager notificationManager,
                                     CommandTypeChooser commandTypeChooser,
                                     CommandUtils commandUtils) {
        this.view = view;
        this.resources = commandsExplorerResources;
        this.workspaceAgent = workspaceAgent;
        this.commandManager = commandManager;
        this.goalRegistry = predefinedCommandGoalRegistry;
        this.notificationManager = notificationManager;
        this.commandTypeChooser = commandTypeChooser;
        this.commandUtils = commandUtils;

        refreshViewTask = new RefreshViewTask();

        view.setDelegate(this);

        eventBus.addHandler(WsAgentStateEvent.TYPE, this);

        commandManager.addCommandChangedListener(this);
    }

    @Override
    public void go(AcceptsOneWidget container) {
        // FIXME: ws-agent may not be ready to fetching data
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                refreshView();
            }
        });

        container.setWidget(getView());
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Nullable
    @Override
    public String getTitleToolTip() {
        return TOOLTIP;
    }

    @Nullable
    @Override
    public SVGResource getTitleImage() {
        return resources.explorerPart();
    }

    @Override
    public void onCommandAdd(int left, int top) {
        // by default, command should be applicable to the workspace only
        final ApplicableContext defaultApplicableContext = new ApplicableContext();
        defaultApplicableContext.setWorkspaceApplicable(true);

        commandTypeChooser.show(left, top).then(new Operation<CommandType>() {
            @Override
            public void apply(CommandType selectedCommandType) throws OperationException {
                final CommandGoal selectedCommandGoal = view.getSelectedCommandGoal();

                if (selectedCommandType != null && selectedCommandGoal != null) {
                    commandManager.createCommand(selectedCommandGoal.getId(),
                                                 selectedCommandType.getId(),
                                                 defaultApplicableContext)
                                  .catchError(new Operation<PromiseError>() {
                                      @Override
                                      public void apply(PromiseError arg) throws OperationException {
                                          notificationManager.notify("Unable to create command", arg.getMessage(), FAIL, EMERGE_MODE);
                                      }
                                  });
                }
            }
        });
    }

    @Override
    public void onCommandDuplicate(final ContextualCommand command) {
        commandManager.createCommand(command).then(new Operation<ContextualCommand>() {
            @Override
            public void apply(ContextualCommand arg) throws OperationException {
                view.selectCommand(arg);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify("Unable to duplicate command", arg.getMessage(), FAIL, EMERGE_MODE);
            }
        });
    }

    @Override
    public void onCommandRemove(final ContextualCommand command) {
        commandManager.removeCommand(command.getName()).then(new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                // TODO: select another command
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify("Unable to remove command", arg.getMessage(), FAIL, EMERGE_MODE);
            }
        });
    }

    @Override
    public void onWsAgentStarted(WsAgentStateEvent event) {
        // TODO: chose a better time to open Commands Explorer
        workspaceAgent.openPart(this, NAVIGATION);
        workspaceAgent.setActivePart(this);
    }

    @Override
    public void onWsAgentStopped(WsAgentStateEvent event) {
    }

    @Override
    public void onCommandAdded(ContextualCommand command) {
        refreshViewAndSelectCommand(command);
    }

    @Override
    public void onCommandUpdated(ContextualCommand command) {
        refreshView();
    }

    @Override
    public void onCommandRemoved(ContextualCommand command) {
        refreshView();
    }

    private void refreshView() {
        refreshViewTask.delayAndSelectCommand(null);
    }

    private void refreshViewAndSelectCommand(ContextualCommand command) {
        refreshViewTask.delayAndSelectCommand(command);
    }

    /**
     * {@link DelayedTask} for refreshing the view and optionally selecting the specified command.
     * <p>Tree widget in the view works asynchronously using events
     * and it needs some time to be fully rendered.
     * So successive refreshing view must be called with some delay.
     */
    private class RefreshViewTask extends DelayedTask {

        // delay determined experimentally
        private static final int DELAY_MILLIS = 300;

        private ContextualCommand command;

        @Override
        public void onExecute() {
            refreshView();

            if (command != null) {
                // wait some time while tree in the view will be fully refreshed
                new Timer() {
                    @Override
                    public void run() {
                        view.selectCommand(command);
                    }
                }.schedule(DELAY_MILLIS);
            }
        }

        // FIXME: when #delay() is invoking then command must be cleared
        void delayAndSelectCommand(@Nullable ContextualCommand command) {
            this.command = command;

            delay(DELAY_MILLIS);
        }

        private void refreshView() {
            final Map<CommandGoal, List<ContextualCommand>> commandsByGoal = new HashMap<>();

            // all predefined command goals must be shown in the view
            // so populate map by all registered command goals
            for (CommandGoal goal : goalRegistry.getAllGoals()) {
                commandsByGoal.put(goal, new ArrayList<ContextualCommand>());
            }

            commandsByGoal.putAll(commandUtils.groupCommandsByGoal(commandManager.getCommands()));

            view.setCommands(commandsByGoal);
        }
    }
}
