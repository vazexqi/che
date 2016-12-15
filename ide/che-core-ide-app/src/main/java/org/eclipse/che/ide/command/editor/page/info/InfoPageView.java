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

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.resources.Project;

import java.util.List;
import java.util.Map;

/**
 * The view of {@link InfoPage}.
 *
 * @author Artem Zatsarynnyi
 */
@ImplementedBy(InfoPageViewImpl.class)
public interface InfoPageView extends View<InfoPageView.ActionDelegate> {

    /** Sets the command's name value. */
    void setCommandName(String name);

    /** Set the list of goals which are available to set for command. */
    void setAvailableGoals(List<CommandGoal> goals);

    /** Sets the command's goal value. */
    void setGoal(String goal);

    void setWorkspace(boolean value);

    void setProjects(Map<Project, Boolean> projects);

    /** The action delegate for this view. */
    interface ActionDelegate {

        /**
         * Called when command goal has been changed.
         *
         * @param goal
         *         changed value of the command goal
         */
        void onGoalChanged(String goal);

        /**
         * Called when command's name has been changed.
         *
         * @param name
         *         changed value of the command's name
         */
        void onNameChanged(String name);

        void onWorkspaceChanged(boolean value);

        void onProjectChanged(boolean value);

        void onApplicableProjectChanged(Project project, boolean value);
    }
}
