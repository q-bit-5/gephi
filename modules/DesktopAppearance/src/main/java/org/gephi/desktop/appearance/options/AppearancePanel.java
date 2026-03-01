/*
 Copyright 2008-2025 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2025 Gephi Consortium. All rights reserved.

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

 Portions Copyrighted 2025 Gephi Consortium.
 */

package org.gephi.desktop.appearance.options;

import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import org.gephi.ui.components.gradientslider.GradientSlider;
import org.gephi.ui.components.gradientslider.MultiThumbSlider;
import org.jdesktop.swingx.JXTitledSeparator;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

final class AppearancePanel extends JPanel {

    private final AppearanceOptionsPanelController controller;

    // Node size
    private JXTitledSeparator nodeSizeSeparator;
    private JLabel nodeMinSizeLabel;
    private JSpinner nodeMinSizeSpinner;
    private JLabel nodeMaxSizeLabel;
    private JSpinner nodeMaxSizeSpinner;
    // Label size
    private JXTitledSeparator labelSizeSeparator;
    private JLabel labelMinSizeLabel;
    private JSpinner labelMinSizeSpinner;
    private JLabel labelMaxSizeLabel;
    private JSpinner labelMaxSizeSpinner;
    // Element (node/edge) color
    private JXTitledSeparator elementColorSeparator;
    private JPanel elementColorGradientPanel;
    private GradientSlider elementColorGradientSlider;
    // Label color
    private JXTitledSeparator labelColorSeparator;
    private JPanel labelColorGradientPanel;
    private GradientSlider labelColorGradientSlider;
    // Reset
    private JButton resetButton;

    AppearancePanel(AppearanceOptionsPanelController controller) {
        this.controller = controller;
        initComponents();
        addChangeListeners();
    }

    private void initComponents() {
        // --- Node size ---
        nodeSizeSeparator = new JXTitledSeparator();
        nodeSizeSeparator.setTitle(
            NbBundle.getMessage(AppearancePanel.class, "AppearancePanel.nodeSizeTitle.title"));
        nodeSizeSeparator.setFont(nodeSizeSeparator.getFont().deriveFont(java.awt.Font.BOLD));

        nodeMinSizeLabel = new JLabel(
            NbBundle.getMessage(AppearancePanel.class, "AppearancePanel.minSize.text"));
        nodeMaxSizeLabel = new JLabel(
            NbBundle.getMessage(AppearancePanel.class, "AppearancePanel.maxSize.text"));
        nodeMinSizeSpinner = new JSpinner(new SpinnerNumberModel(
            AppearancePreferences.DEFAULT_NODE_RANKING_SIZE_MIN, 0.01f, null, 0.5f));
        nodeMaxSizeSpinner = new JSpinner(new SpinnerNumberModel(
            AppearancePreferences.DEFAULT_NODE_RANKING_SIZE_MAX, 0.5f, null, 0.5f));

        // --- Label size ---
        labelSizeSeparator = new JXTitledSeparator();
        labelSizeSeparator.setTitle(
            NbBundle.getMessage(AppearancePanel.class, "AppearancePanel.labelSizeTitle.title"));
        labelSizeSeparator.setFont(labelSizeSeparator.getFont().deriveFont(java.awt.Font.BOLD));

        labelMinSizeLabel = new JLabel(
            NbBundle.getMessage(AppearancePanel.class, "AppearancePanel.minSize.text"));
        labelMaxSizeLabel = new JLabel(
            NbBundle.getMessage(AppearancePanel.class, "AppearancePanel.maxSize.text"));
        labelMinSizeSpinner = new JSpinner(new SpinnerNumberModel(
            AppearancePreferences.DEFAULT_LABEL_RANKING_SIZE_MIN, 0.01f, null, 0.5f));
        labelMaxSizeSpinner = new JSpinner(new SpinnerNumberModel(
            AppearancePreferences.DEFAULT_LABEL_RANKING_SIZE_MAX, 0.5f, null, 0.5f));

        // --- Element (node/edge) color ---
        elementColorSeparator = new JXTitledSeparator();
        elementColorSeparator.setTitle(
            NbBundle.getMessage(AppearancePanel.class, "AppearancePanel.elementColorTitle.title"));
        elementColorSeparator.setFont(elementColorSeparator.getFont().deriveFont(java.awt.Font.BOLD));

        elementColorGradientPanel = new JPanel(new BorderLayout());
        elementColorGradientPanel.setOpaque(false);
        elementColorGradientSlider = new GradientSlider(GradientSlider.HORIZONTAL);
        elementColorGradientPanel.add(elementColorGradientSlider, BorderLayout.CENTER);

        // --- Label color ---
        labelColorSeparator = new JXTitledSeparator();
        labelColorSeparator.setTitle(
            NbBundle.getMessage(AppearancePanel.class, "AppearancePanel.labelColorTitle.title"));
        labelColorSeparator.setFont(labelColorSeparator.getFont().deriveFont(java.awt.Font.BOLD));

        labelColorGradientPanel = new JPanel(new BorderLayout());
        labelColorGradientPanel.setOpaque(false);
        labelColorGradientSlider = new GradientSlider(GradientSlider.HORIZONTAL);
        labelColorGradientPanel.add(labelColorGradientSlider, BorderLayout.CENTER);

        // --- Reset button ---
        resetButton = new JButton(
            NbBundle.getMessage(AppearancePanel.class, "AppearancePanel.resetButton.text"));
        resetButton.addActionListener(e -> resetDefaults());

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            // Node size
            .addComponent(nodeSizeSeparator)
            .addGroup(layout.createSequentialGroup()
                .addGap(10)
                .addComponent(nodeMinSizeLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nodeMinSizeSpinner, GroupLayout.PREFERRED_SIZE, 70,
                    GroupLayout.PREFERRED_SIZE)
                .addGap(18)
                .addComponent(nodeMaxSizeLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nodeMaxSizeSpinner, GroupLayout.PREFERRED_SIZE, 70,
                    GroupLayout.PREFERRED_SIZE))
            // Label size
            .addComponent(labelSizeSeparator)
            .addGroup(layout.createSequentialGroup()
                .addGap(10)
                .addComponent(labelMinSizeLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelMinSizeSpinner, GroupLayout.PREFERRED_SIZE, 70,
                    GroupLayout.PREFERRED_SIZE)
                .addGap(18)
                .addComponent(labelMaxSizeLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelMaxSizeSpinner, GroupLayout.PREFERRED_SIZE, 70,
                    GroupLayout.PREFERRED_SIZE))
            // Element color
            .addComponent(elementColorSeparator)
            .addGroup(layout.createSequentialGroup()
                .addGap(10)
                .addComponent(elementColorGradientPanel, 0, GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE)
                .addGap(10))
            // Label color
            .addComponent(labelColorSeparator)
            .addGroup(layout.createSequentialGroup()
                .addGap(10)
                .addComponent(labelColorGradientPanel, 0, GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE)
                .addGap(10))
            // Reset
            .addComponent(resetButton)
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
            // Node size
            .addComponent(nodeSizeSeparator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                GroupLayout.PREFERRED_SIZE)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(nodeMinSizeLabel)
                .addComponent(nodeMinSizeSpinner, GroupLayout.PREFERRED_SIZE,
                    GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(nodeMaxSizeLabel)
                .addComponent(nodeMaxSizeSpinner, GroupLayout.PREFERRED_SIZE,
                    GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            // Label size
            .addGap(10)
            .addComponent(labelSizeSeparator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                GroupLayout.PREFERRED_SIZE)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labelMinSizeLabel)
                .addComponent(labelMinSizeSpinner, GroupLayout.PREFERRED_SIZE,
                    GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(labelMaxSizeLabel)
                .addComponent(labelMaxSizeSpinner, GroupLayout.PREFERRED_SIZE,
                    GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            // Element color
            .addGap(10)
            .addComponent(elementColorSeparator, GroupLayout.PREFERRED_SIZE,
                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addComponent(elementColorGradientPanel, 22, 22, 22)
            // Label color
            .addGap(10)
            .addComponent(labelColorSeparator, GroupLayout.PREFERRED_SIZE,
                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addComponent(labelColorGradientPanel, 22, 22, 22)
            // Reset
            .addGap(18)
            .addComponent(resetButton)
        );
    }

    private void addChangeListeners() {
        ChangeListener sizeChangeListener = e -> controller.changed();
        nodeMinSizeSpinner.addChangeListener(sizeChangeListener);
        nodeMaxSizeSpinner.addChangeListener(sizeChangeListener);
        labelMinSizeSpinner.addChangeListener(sizeChangeListener);
        labelMaxSizeSpinner.addChangeListener(sizeChangeListener);

        PropertyChangeListener gradientChangeListener = evt -> {
            String prop = evt.getPropertyName();
            if (MultiThumbSlider.VALUES_PROPERTY.equals(prop) ||
                MultiThumbSlider.ADJUST_PROPERTY.equals(prop)) {
                controller.changed();
            }
        };
        elementColorGradientSlider.addPropertyChangeListener(gradientChangeListener);
        labelColorGradientSlider.addPropertyChangeListener(gradientChangeListener);
    }

    private void resetDefaults() {
        NbPreferences.forModule(AppearancePreferences.class).remove(AppearancePreferences.NODE_RANKING_SIZE_MIN);
        NbPreferences.forModule(AppearancePreferences.class).remove(AppearancePreferences.NODE_RANKING_SIZE_MAX);
        NbPreferences.forModule(AppearancePreferences.class).remove(AppearancePreferences.LABEL_RANKING_SIZE_MIN);
        NbPreferences.forModule(AppearancePreferences.class).remove(AppearancePreferences.LABEL_RANKING_SIZE_MAX);
        NbPreferences.forModule(AppearancePreferences.class).remove(AppearancePreferences.ELEMENT_RANKING_COLORS);
        NbPreferences.forModule(AppearancePreferences.class)
            .remove(AppearancePreferences.ELEMENT_RANKING_COLOR_POSITIONS);
        NbPreferences.forModule(AppearancePreferences.class).remove(AppearancePreferences.LABEL_RANKING_COLORS);
        NbPreferences.forModule(AppearancePreferences.class)
            .remove(AppearancePreferences.LABEL_RANKING_COLOR_POSITIONS);
        load();
    }

    void load() {
        nodeMinSizeSpinner.setValue(AppearancePreferences.getNodeRankingSizeMin());
        nodeMaxSizeSpinner.setValue(AppearancePreferences.getNodeRankingSizeMax());
        labelMinSizeSpinner.setValue(AppearancePreferences.getLabelRankingSizeMin());
        labelMaxSizeSpinner.setValue(AppearancePreferences.getLabelRankingSizeMax());

        elementColorGradientSlider.setValues(
            AppearancePreferences.getElementRankingColorPositions(),
            AppearancePreferences.getElementRankingColors());
        labelColorGradientSlider.setValues(
            AppearancePreferences.getLabelRankingColorPositions(),
            AppearancePreferences.getLabelRankingColors());
    }

    void store() {
        NbPreferences.forModule(AppearancePreferences.class)
            .putFloat(AppearancePreferences.NODE_RANKING_SIZE_MIN,
                (Float) nodeMinSizeSpinner.getValue());
        NbPreferences.forModule(AppearancePreferences.class)
            .putFloat(AppearancePreferences.NODE_RANKING_SIZE_MAX,
                (Float) nodeMaxSizeSpinner.getValue());
        NbPreferences.forModule(AppearancePreferences.class)
            .putFloat(AppearancePreferences.LABEL_RANKING_SIZE_MIN,
                (Float) labelMinSizeSpinner.getValue());
        NbPreferences.forModule(AppearancePreferences.class)
            .putFloat(AppearancePreferences.LABEL_RANKING_SIZE_MAX,
                (Float) labelMaxSizeSpinner.getValue());

        NbPreferences.forModule(AppearancePreferences.class)
            .put(AppearancePreferences.ELEMENT_RANKING_COLORS,
                AppearancePreferences.encodeColors(elementColorGradientSlider.getColors()));
        NbPreferences.forModule(AppearancePreferences.class)
            .put(AppearancePreferences.ELEMENT_RANKING_COLOR_POSITIONS,
                AppearancePreferences.encodePositions(elementColorGradientSlider.getThumbPositions()));
        NbPreferences.forModule(AppearancePreferences.class)
            .put(AppearancePreferences.LABEL_RANKING_COLORS,
                AppearancePreferences.encodeColors(labelColorGradientSlider.getColors()));
        NbPreferences.forModule(AppearancePreferences.class)
            .put(AppearancePreferences.LABEL_RANKING_COLOR_POSITIONS,
                AppearancePreferences.encodePositions(labelColorGradientSlider.getThumbPositions()));
    }

    boolean valid() {
        return true;
    }
}
