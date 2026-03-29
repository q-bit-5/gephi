package org.gephi.desktop.selection;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.gephi.desktop.selection.edit.EditPanel;
import org.gephi.desktop.selection.selection.SelectionPanel;
import org.gephi.graph.api.Column;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

@ConvertAsProperties(dtd = "-//org.gephi.desktop.selection//Selection//EN",
    autostore = false)
@TopComponent.Description(preferredID = "SelectionTopComponent",
    iconBase = "DesktopSelection/edit.svg",
    persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "rankingmode", openAtStartup = false, roles = {"overview", "datalab"}, position = 20)
@ActionID(category = "Window", id = "org.gephi.desktop.selection.SelectionTopComponent")
@ActionReference(path = "Menu/Window", position = 1500)
@TopComponent.OpenActionRegistration(displayName = "#CTL_SelectionTopComponent",
    preferredID = "SelectionTopComponent")
public final class SelectionTopComponent extends TopComponent implements SelectionUIModelListener {

    private static final String SELECTION_CARD = "selection";
    private static final String EDIT_CARD = "edit";

    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final EditPanel editPanel;
    private final SelectionPanel selectionPanel;
    private final JButton columnsButton;
    private final JToggleButton showNullButton;

    private final SelectionUIControllerImpl controller;

    // Model
    private SelectionUIModelImpl model;

    public SelectionTopComponent() {
        // Register
        controller = Lookup.getDefault().lookup(SelectionUIControllerImpl.class);
        controller.addPropertyChangeListener(this);

        setName(NbBundle.getMessage(SelectionTopComponent.class, "CTL_SelectionTopComponent"));

        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);

        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        selectionPanel = new SelectionPanel();
        editPanel = new EditPanel();

        cardPanel.add(selectionPanel, SELECTION_CARD);
        cardPanel.add(editPanel, EDIT_CARD);

        cardLayout.show(cardPanel, SELECTION_CARD);

        add(cardPanel, BorderLayout.CENTER);

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setRollover(true);

        columnsButton = new JButton(
            ImageUtilities.loadImageIcon("DesktopSelection/column.svg", false));
        columnsButton.setText(
            NbBundle.getMessage(SelectionTopComponent.class, "SelectionTopComponent.columnsButton.text"));
        columnsButton.setToolTipText(
            NbBundle.getMessage(SelectionTopComponent.class, "SelectionTopComponent.columnsButton.tooltip"));
        columnsButton.setFocusable(false);
        columnsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showColumnsPopup(e);
            }
        });
        toolbar.add(columnsButton);

        toolbar.add(Box.createHorizontalGlue());

        showNullButton = new JToggleButton(
            ImageUtilities.loadImageIcon("DesktopSelection/includeNull.svg", false));
        showNullButton.setToolTipText(
            NbBundle.getMessage(SelectionTopComponent.class, "SelectionTopComponent.showNullButton.tooltip"));
        showNullButton.setFocusable(false);
        showNullButton.addActionListener(e -> {
            if (model != null) {
                model.setShowNullColumns(showNullButton.isSelected());
                controller.firePropertyChangeEvent(
                    SelectionUIModelEvent.SHOW_NULL_COLUMNS, !showNullButton.isSelected(),
                    showNullButton.isSelected());
            }
        });
        toolbar.add(showNullButton);

        add(toolbar, BorderLayout.SOUTH);

        // Init if needed
        SelectionUIModelImpl model = controller.getModel();
        if (model != null) {
            setup(model);
        } else {
            unsetup();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SelectionUIModelEvent.SELECTED_ELEMENTS) ||
            evt.getPropertyName().equals(SelectionUIModelEvent.HIDDEN_COLUMN_IDS) ||
            evt.getPropertyName().equals(SelectionUIModelEvent.SHOW_NULL_COLUMNS) ||
            evt.getPropertyName().equals(SelectionUIModelEvent.INCLUDE_PROPERTIES)) {
            refreshSelection();
        } else if (evt.getPropertyName().equals(SelectionUIModelEvent.MODEL)) {
            if (evt.getNewValue() == null) {
                unsetup();
            } else {
                setup((SelectionUIModelImpl) evt.getNewValue());
            }
        } else if (evt.getPropertyName().equals(SelectionUIModelEvent.EDIT_MODE)) {
            setup(this.model);
        }
    }

    private void refreshSelection() {
        if (model.isEditMode()) {
            editPanel.refreshSelected(model);
        } else {
            selectionPanel.refreshSelectedNodes(model);
        }
    }

    private void setup(SelectionUIModelImpl model) {
        this.model = model;

        if (model.isEditMode()) {
            cardLayout.show(cardPanel, EDIT_CARD);
        } else {
            cardLayout.show(cardPanel, SELECTION_CARD);
        }
        showNullButton.setSelected(model.isShowNullColumns());
        showNullButton.setEnabled(true);
        columnsButton.setEnabled(true);
    }

    private void unsetup() {
        this.model = null;

        cardLayout.show(cardPanel, SELECTION_CARD);
        showNullButton.setSelected(false);
        showNullButton.setEnabled(false);
        columnsButton.setEnabled(false);
    }

    private void showColumnsPopup(MouseEvent e) {
        List<Column> columns = model.getEligibleColumns();

        JPopupMenu popup = new JPopupMenu();

        if (columns.isEmpty()) {
            JMenuItem emptyLabel = new JMenuItem(
                NbBundle.getMessage(SelectionTopComponent.class, "SelectionTopComponent.noColumns"));
            emptyLabel.setEnabled(false);
            popup.add(emptyLabel);
        } else {
            boolean allVisible = columns.stream().allMatch(model::isColumnVisible);
            boolean noneVisible = columns.stream().noneMatch(model::isColumnVisible);

            columns.stream()
                .map(column -> {
                    JCheckBoxMenuItem item = new JCheckBoxMenuItem(column.getTitle());
                    item.setSelected(model.isColumnVisible(column));
                    item.addActionListener(evt -> {
                        model.setColumnHidden(column, !item.isSelected());
                        controller.firePropertyChangeEvent(
                            SelectionUIModelEvent.HIDDEN_COLUMN_IDS, null, null);
                    });
                    return item;
                })
                .forEach(popup::add);

            if (model.isEditMode()) {
                popup.addSeparator();

                JCheckBoxMenuItem includeProperties = new JCheckBoxMenuItem(
                    NbBundle.getMessage(SelectionTopComponent.class,
                        "SelectionTopComponent.includeProperties"));
                includeProperties.setSelected(model.isIncludeProperties());
                includeProperties.addActionListener(evt -> {
                    model.setIncludeProperties(includeProperties.isSelected());
                    controller.firePropertyChangeEvent(
                        SelectionUIModelEvent.INCLUDE_PROPERTIES,
                        !includeProperties.isSelected(),
                        includeProperties.isSelected());
                });
                popup.add(includeProperties);
            }

            popup.addSeparator();

            JMenuItem selectAll = new JMenuItem(
                NbBundle.getMessage(SelectionTopComponent.class, "SelectionTopComponent.selectAll"));
            selectAll.setEnabled(!allVisible);
            selectAll.addActionListener(evt -> {
                columns.forEach(column -> model.setColumnHidden(column, false));
                controller.firePropertyChangeEvent(
                    SelectionUIModelEvent.HIDDEN_COLUMN_IDS, null, null);
            });
            popup.add(selectAll);

            JMenuItem unselectAll = new JMenuItem(
                NbBundle.getMessage(SelectionTopComponent.class, "SelectionTopComponent.unselectAll"));
            unselectAll.setEnabled(!noneVisible);
            unselectAll.addActionListener(evt -> {
                columns.forEach(column -> model.setColumnHidden(column, true));
                controller.firePropertyChangeEvent(
                    SelectionUIModelEvent.HIDDEN_COLUMN_IDS, null, null);
            });
            popup.add(unselectAll);
        }

        popup.show(columnsButton, 0, -popup.getPreferredSize().height);
    }

    public EditPanel getEditPanel() {
        return editPanel;
    }

    public SelectionPanel getSelectionPanel() {
        return selectionPanel;
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    void writeProperties(java.util.Properties p) {
        p.setProperty("version", "1.0");
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
    }

}
