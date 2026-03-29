/*
 Copyright 2008-2010 Gephi
 Authors : Eduardo Ramos <eduramiba@gmail.com>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */

package org.gephi.desktop.selection;

import java.util.HashSet;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.gephi.desktop.selection.api.SelectionUIController;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.project.spi.Controller;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.openide.windows.WindowManager;

/**
 * Implementation of EditWindowController interface of Tools API.
 *
 * @author Eduardo Ramos
 */
@ServiceProviders({
    @ServiceProvider(service = SelectionUIController.class),
    @ServiceProvider(service = Controller.class)})
public class SelectionUIControllerImpl implements SelectionUIController, Controller<SelectionUIModelImpl> {

    private final Set<SelectionUIModelListener> listeners = new HashSet<>();

    public SelectionUIControllerImpl() {

        Lookup.getDefault().lookup(ProjectController.class).addWorkspaceListener(new WorkspaceListener() {

            @Override
            public void initialize(Workspace workspace) {
            }

            @Override
            public void select(Workspace workspace) {
                SelectionUIModelImpl model = getModel(workspace);
                firePropertyChangeEvent(SelectionUIModelEvent.MODEL, null, model);
            }

            @Override
            public void unselect(Workspace workspace) {
                SelectionUIModelImpl model = getModel(workspace);
                model.resetSelection();
                firePropertyChangeEvent(SelectionUIModelEvent.MODEL, null, null);
            }

            @Override
            public void close(Workspace workspace) {
            }

            @Override
            public void disable() {
                closeWindow();
            }
        });
    }

    @Override
    public SelectionUIModelImpl newModel(Workspace workspace) {
        return new SelectionUIModelImpl(workspace);
    }

    @Override
    public Class<SelectionUIModelImpl> getModelClass() {
        return SelectionUIModelImpl.class;
    }

    protected static SelectionTopComponent findInstance() {
        return (SelectionTopComponent) WindowManager.getDefault().findTopComponent("SelectionTopComponent");
    }

    private void resetSelection() {
        SelectionUIModelImpl model = getModel();
        if (model != null) {
            model.resetSelection();
            firePropertyChangeEvent(SelectionUIModelEvent.SELECTED_ELEMENTS, null, null);
        }
    }

    private void setEditMode(boolean editMode) {
        SelectionUIModelImpl model = getModel();
        if (model.isEditMode() != editMode) {
            model.setEditMode(editMode);
            if (editMode) {
                model.resetSelection();
                firePropertyChangeEvent(SelectionUIModelEvent.SELECTED_ELEMENTS, null, null);
            }
            firePropertyChangeEvent(SelectionUIModelEvent.EDIT_MODE, !editMode, editMode);
        }
    }

    private void setSelectedNodes(Node[] nodes) {
        SelectionUIModelImpl model = getModel();
        if (model.getSelectedNodes() != nodes) {
            model.setSelectedNodes(nodes);
            firePropertyChangeEvent(SelectionUIModelEvent.SELECTED_ELEMENTS, null, nodes);
        }
    }

    private void setSelectedEdges(Edge[] edges) {
        SelectionUIModelImpl model = getModel();
        if (model.getSelectedEdges() != edges) {
            model.setSelectedEdges(edges);
            firePropertyChangeEvent(SelectionUIModelEvent.SELECTED_ELEMENTS, null, edges);
        }
    }

    @Override
    public void openWindow() {
        runAction(() -> {
            SelectionTopComponent topComponent = findInstance();
            topComponent.open();
        });

    }

    @Override
    public void openWindowAndRequestActive() {
        runAction(() -> {
            SelectionTopComponent topComponent = findInstance();
            topComponent.open();
            topComponent.requestActive();
        });

    }

    @Override
    public void closeWindow() {
        runAction(() -> {
            resetSelection();
            SelectionTopComponent topComponent = findInstance();
            topComponent.getEditPanel().disableEdit();
            topComponent.close();
        });
    }

    @Override
    public boolean isOpen() {
        IsOpenRunnable runnable = new IsOpenRunnable();
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return runnable.open;
    }

    @Override
    public void editNode(final Node node) {
        runAction(() -> {
            setEditMode(true);
            setSelectedNodes(new Node[] {node});
        });
    }

    @Override
    public void editNodes(final Node[] nodes) {
        runAction(() -> {
            setEditMode(true);
            setSelectedNodes(nodes);
        });
    }

    @Override
    public void editEdge(final Edge edge) {
        runAction(() -> {
            setEditMode(true);
            setSelectedEdges(new Edge[] {edge});
        });
    }

    @Override
    public void editEdges(final Edge[] edges) {
        runAction(() -> {
            setEditMode(true);
            setSelectedEdges(edges);
        });
    }

    @Override
    public void enableEdit() {
        runAction(() -> {
            setEditMode(true);
        });
    }

    @Override
    public void disableEdit() {
        runAction(() -> {
            setEditMode(false);
        });
    }

    @Override
    public void selectNodes(Node[] nodes) {
        SelectionUIModelImpl model = getModel();
        if (model != null && !model.isEditMode()) {
            runAction(() -> {
                setSelectedNodes(nodes);
            });
        }

    }

    class IsOpenRunnable implements Runnable {

        boolean open = false;

        @Override
        public void run() {
            SelectionTopComponent topComponent = findInstance();
            open = topComponent != null && topComponent.isOpened();
        }
    }

    public void addPropertyChangeListener(SelectionUIModelListener listener) {
        listeners.add(listener);
    }

    public void removePropertyChangeListener(SelectionUIModelListener listener) {
        listeners.remove(listener);
    }

    protected void firePropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
        SelectionUIModelEvent event = new SelectionUIModelEvent(this, propertyName, oldValue, newValue);
        for (SelectionUIModelListener listener : listeners) {
            listener.propertyChange(event);
        }
    }

    private void runAction(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }
}
