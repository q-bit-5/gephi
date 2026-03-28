package org.gephi.visualization.contextmenu.items;

import java.awt.event.KeyEvent;
import javax.swing.Icon;
import org.gephi.datalab.api.GraphElementsController;
import org.gephi.graph.api.Node;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = GraphContextMenuItem.class)
public class Edit extends BasicItem {

    @Override
    public void execute() {

    }

    @Override
    public String getName() {
        return NbBundle.getMessage(Edit.class, "GraphContextMenu_Edit");
    }

    @Override
    public boolean canExecute() {
        return nodes.length > 0;
    }

    @Override
    public int getType() {
        return 300;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("VisualizationImpl/edit.svg", false);
    }

    @Override
    public Integer getMnemonicKey() {
        return KeyEvent.VK_E;
    }
}
