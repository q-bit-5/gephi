package org.gephi.desktop.selection;

import org.gephi.desktop.selection.api.SelectionUIModel;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.Model;

public class SelectionUIModelImpl implements SelectionUIModel, Model {

    private final Workspace workspace;

    public SelectionUIModelImpl(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }
}
