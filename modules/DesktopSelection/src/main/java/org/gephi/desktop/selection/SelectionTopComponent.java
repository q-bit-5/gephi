package org.gephi.desktop.selection;

import org.gephi.desktop.selection.edit.EditPanel;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
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
public final class SelectionTopComponent extends TopComponent {

    private final EditPanel editPanel;

    public SelectionTopComponent() {
        setName(NbBundle.getMessage(SelectionTopComponent.class, "CTL_SelectionTopComponent"));

        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);

        setLayout(new java.awt.BorderLayout());
        editPanel = new EditPanel();
        add(editPanel, java.awt.BorderLayout.CENTER);
    }

    public EditPanel getEditPanel() {
        return editPanel;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

}
