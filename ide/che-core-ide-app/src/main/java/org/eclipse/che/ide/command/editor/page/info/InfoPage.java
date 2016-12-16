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
package org.eclipse.che.ide.command.editor.page.info;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.PredefinedCommandGoalRegistry;
import org.eclipse.che.ide.api.command.ContextualCommand.ApplicableContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.command.editor.page.AbstractCommandEditorPage;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.api.workspace.shared.Constants.COMMAND_GOAL_ATTRIBUTE_NAME;

/**
 * {@link CommandEditorPage} which allows to edit basic command's information, like:
 * <ul>
 * <li>name;</li>
 * <li>applicable context.</li>
 * </ul>
 *
 * @author Artem Zatsarynnyi
 */
public class InfoPage extends AbstractCommandEditorPage implements InfoPageView.ActionDelegate {

    private final InfoPageView                  view;
    private final AppContext                    appContext;
    private final PredefinedCommandGoalRegistry predefinedCommandGoalRegistry;

    private final Map<Project, Boolean> projectsState;

    private String       goalInitial;
    // initial value of the command's name
    private String       commandNameInitial;
    // initial value of the workspace flag
    private boolean      workspaceInitial;
    // initial value of the applicable projects list
    private List<String> applicableProjectsInitial;

    @Inject
    public InfoPage(InfoPageView view,
                    AppContext appContext,
                    PredefinedCommandGoalRegistry predefinedCommandGoalRegistry) {
        super("Info", "General command info");

        this.view = view;
        this.appContext = appContext;
        this.predefinedCommandGoalRegistry = predefinedCommandGoalRegistry;

        projectsState = new HashMap<>();

        view.setDelegate(this);
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Override
    protected void initialize() {
        String goal = editedCommand.getAttributes().get(COMMAND_GOAL_ATTRIBUTE_NAME);
        if (goal == null) {
            goal = "";
        }

        final ApplicableContext context = editedCommand.getApplicableContext();

        goalInitial = goal;
        commandNameInitial = editedCommand.getName();
        workspaceInitial = context.isWorkspaceApplicable();
        applicableProjectsInitial = new ArrayList<>(context.getApplicableProjects());

        view.setAvailableGoals(predefinedCommandGoalRegistry.getAllGoals());
        view.setGoal(goal);
        view.setCommandName(editedCommand.getName());
        view.setWorkspace(editedCommand.getApplicableContext().isWorkspaceApplicable());

        refreshProjects();
    }

    /** Refresh projects in the view. */
    private void refreshProjects() {
        projectsState.clear();

        final ApplicableContext context = editedCommand.getApplicableContext();

        for (Project project : appContext.getProjects()) {
            final boolean applicable = context.getApplicableProjects().contains(project.getPath());

            projectsState.put(project, applicable);
        }

        view.setProjects(projectsState);
    }

    @Override
    public boolean isDirty() {
        if (editedCommand == null) {
            return false;
        }

        String goal = editedCommand.getAttributes().get(COMMAND_GOAL_ATTRIBUTE_NAME);
        if (goal == null) {
            goal = "";
        }

        final ApplicableContext applicableContext = editedCommand.getApplicableContext();

        return !(goalInitial.equals(goal) &&
                 commandNameInitial.equals(editedCommand.getName()) &&
                 workspaceInitial == applicableContext.isWorkspaceApplicable() &&
                 applicableProjectsInitial.equals(applicableContext.getApplicableProjects()));
    }

    @Override
    public void onGoalChanged(String goal) {
        editedCommand.getAttributes().put(COMMAND_GOAL_ATTRIBUTE_NAME, goal);

        notifyDirtyStateChanged();
    }

    @Override
    public void onNameChanged(String name) {
        editedCommand.setName(name);

        notifyDirtyStateChanged();
    }

    @Override
    public void onWorkspaceChanged(boolean value) {
        editedCommand.getApplicableContext().setWorkspaceApplicable(value);

        notifyDirtyStateChanged();
    }

    @Override
    public void onProjectChanged(boolean value) {
    }

    @Override
    public void onApplicableProjectChanged(Project project, boolean value) {
        projectsState.put(project, value);

        final ApplicableContext applicableContext = editedCommand.getApplicableContext();

        if (value) {
            applicableContext.addProject(project.getPath());
        } else {
            applicableContext.removeProject(project.getPath());
        }

        notifyDirtyStateChanged();
    }
}
