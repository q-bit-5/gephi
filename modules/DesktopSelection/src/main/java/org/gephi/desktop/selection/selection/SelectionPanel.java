package org.gephi.desktop.selection.selection;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class SelectionPanel extends JPanel {

    private final JLabel label = new JLabel();

     public SelectionPanel() {
         initComponents();
     }

     public void setLabel(String label) {
         this.label.setText(label);
     }

     private void initComponents() {
         setLayout(new java.awt.BorderLayout());
         add(label, java.awt.BorderLayout.CENTER);
     }

}
