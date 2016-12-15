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
package org.eclipse.che.api.workspace.shared.dto.event;

import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.dto.shared.DTO;

/**
 * Describes workspace status changes.
 *
 * @author Alexander Garagatyi
 * @author Yevhenii Voevodin
 */
@EventOrigin("workspace")
@DTO
public interface WorkspaceStatusEvent {

    /** Defines event type for workspace status event changes. */
    enum EventType {

        /**
         * Published after workspace status is changed to STARTING.
         * If {@link WorkspaceStatusEvent#getEventType()} returns this value, then
         * the value of {@link WorkspaceStatusEvent#getPrevStatus()} is
         * {@link WorkspaceStatus#STOPPED}.
         *
         * @see WorkspaceStatus#STARTING
         */
        STARTING,

        /**
         * Published after workspace status becomes {@link WorkspaceStatus#RUNNING}.
         * If {@link WorkspaceStatusEvent#getEventType()} returns this value, then
         * the value of {@link WorkspaceStatusEvent#getPrevStatus()} is
         * either {@link WorkspaceStatus#STARTING} or {@link WorkspaceStatus#SNAPSHOTTING}.
         *
         * @see WorkspaceStatus#RUNNING
         */
        RUNNING,

        /**
         * Published after workspace status becomes {@link WorkspaceStatus#STOPPING}.
         * If {@link WorkspaceStatusEvent#getEventType()} returns this value, then
         * the value of {@link WorkspaceStatusEvent#getPrevStatus()} is {@link WorkspaceStatus#RUNNING}.
         *
         * @see WorkspaceStatus#STOPPING
         */
        STOPPING,

        /**
         * Published after workspace is successfully stopped.
         * If {@link WorkspaceStatusEvent#getEventType()} returns this value, then
         * the value of {@link WorkspaceStatusEvent#getPrevStatus()} is {@link WorkspaceStatus#STOPPING}.
         *
         * @see WorkspaceStatus#STOPPED
         */
        STOPPED,

        /**
         * Published after workspace status becomes {@link WorkspaceStatus#SNAPSHOTTING}.
         * If {@link WorkspaceStatusEvent#getEventType()} returns this value, then
         * the value of {@link WorkspaceStatusEvent#getPrevStatus()} is {@link WorkspaceStatus#RUNNING}.
         *
         * @see WorkspaceStatus#SNAPSHOTTING
         */
        SNAPSHOT_CREATING,

        /**
         * Published after workspace snapshot is successfully created.
         * If {@link WorkspaceStatusEvent#getEventType()} returns this value, then
         * the value of {@link WorkspaceStatusEvent#getPrevStatus()} is {@link WorkspaceStatus#SNAPSHOTTING}.
         *
         * @see WorkspaceStatus#SNAPSHOTTING
         */
        SNAPSHOT_CREATED,

        /**
         * Published after workspace snapshot is not created due to occurred error.
         * If {@link WorkspaceStatusEvent#getEventType()} returns this value, then
         * the value of {@link WorkspaceStatusEvent#getPrevStatus()} is {@link WorkspaceStatus#SNAPSHOTTING}.
         *
         * @see WorkspaceStatus#SNAPSHOTTING
         */
        SNAPSHOT_CREATION_ERROR,

        /**
         * Published when any error occurs.
         * If {@link WorkspaceStatusEvent#getEventType()} returns this value, then
         * the value returned by {@link WorkspaceStatusEvent#getPrevStatus()} is
         * one of the values above, never null.
         */
        ERROR
    }

    /** Returns the type of this event. */
    EventType getEventType();

    void setEventType(EventType eventType);

    WorkspaceStatusEvent withEventType(EventType eventType);

    /**
     * Returns previous workspace status.
     *
     * @see WorkspaceStatus for more information about certain values
     */
    WorkspaceStatus getPrevStatus();

    void setPrevStatus(WorkspaceStatus status);

    WorkspaceStatusEvent withPrevStatus(WorkspaceStatus status);

    /** The id of the workspace to which this event belongs to . */
    String getWorkspaceId();

    void setWorkspaceId(String machineId);

    WorkspaceStatusEvent withWorkspaceId(String machineId);

    /**
     * Returns an error message value if and only if the type of this event is
     * either {@link EventType#ERROR} or {@link EventType#SNAPSHOT_CREATION_ERROR}.
     */
    @Nullable
    String getError();

    void setError(String error);

    WorkspaceStatusEvent withError(String error);
}
