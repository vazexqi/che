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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.ui.window.Window;

import java.util.List;

/**
 * Implementation of {@link CommandsPaletteView}.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CommandsPaletteViewImpl extends Window implements CommandsPaletteView {

    private static final CommandsPaletteViewImplUiBinder UI_BINDER = GWT.create(CommandsPaletteViewImplUiBinder.class);

    private ActionDelegate delegate;

//    @UiField
//    TextBox filterField;

    @Inject
    public CommandsPaletteViewImpl() {
        setWidget(UI_BINDER.createAndBindUi(this));

        setTitle("Commands Palette");
    }

    @Override
    public void setCommands(List<CommandImpl> commands) {
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    interface CommandsPaletteViewImplUiBinder extends UiBinder<Widget, CommandsPaletteView> {
    }
}