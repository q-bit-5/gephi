package org.gephi.visualization;

import java.awt.Color;
import java.awt.Font;
import java.io.StringReader;
import javax.xml.stream.XMLStreamReader;
import org.gephi.project.api.Workspace;
import org.gephi.project.io.utils.GephiFormat;
import org.gephi.visualization.api.EdgeColorMode;
import org.gephi.visualization.api.LabelColorMode;
import org.gephi.visualization.api.LabelSizeMode;
import org.junit.Assert;
import org.junit.Test;

public class PersistenceProviderTest {

    @Test
    public void testEmpty() throws Exception {
        VizModel model = Utils.newVizModel();
        Utils.roundTrip(new VizModelPersistenceProvider(), model.getWorkspace());
    }

    @Test
    public void testBackgroundColor() throws Exception {
        VizModel model = Utils.newVizModel();
        model.setBackgroundColor(Color.CYAN);

        Workspace dest = Utils.roundTrip(new VizModelPersistenceProvider(), model.getWorkspace());
        Assert.assertEquals(Color.CYAN, Utils.getVizModel(dest).getBackgroundColor());
    }

    @Test
    public void testZoomAndPan() throws Exception {
        VizModel model = Utils.newVizModel();
        model.setZoom(1.5f);
        model.setPan(new org.joml.Vector2f(123.4f, -56.7f));

        Workspace dest = Utils.roundTrip(new VizModelPersistenceProvider(), model.getWorkspace());
        VizModel read = Utils.getVizModel(dest);
        Assert.assertEquals(1.5f, read.getZoom(), 0.0001f);
        Assert.assertEquals(123.4f, read.getPan().x(), 0.0001f);
        Assert.assertEquals(-56.7f, read.getPan().y(), 0.0001f);
    }

    @Test
    public void testEdgeSettings() throws Exception {
        VizModel model = Utils.newVizModel();
        model.setShowEdges(false);
        model.setEdgeScale(3.5f);
        model.setNodeScale(2.0f);
        model.setEdgeColorMode(EdgeColorMode.MIXED);
        model.setUseEdgeWeight(false);
        model.setEdgeRescaleWeightEnabled(false);

        Workspace dest = Utils.roundTrip(new VizModelPersistenceProvider(), model.getWorkspace());
        VizModel read = Utils.getVizModel(dest);
        Assert.assertFalse(read.isShowEdges());
        Assert.assertEquals(3.5f, read.getEdgeScale(), 0.0001f);
        Assert.assertEquals(2.0f, read.getNodeScale(), 0.0001f);
        Assert.assertEquals(EdgeColorMode.MIXED, read.getEdgeColorMode());
        Assert.assertFalse(read.isUseEdgeWeight());
        Assert.assertFalse(read.isRescaleEdgeWeight());
    }

    @Test
    public void testSelectionSettings() throws Exception {
        VizModel model = Utils.newVizModel();
        model.setAutoSelectNeighbors(false);
        model.setHideNonSelectedEdges(true);
        model.setLightenNonSelectedAuto(false);
        model.setLightenNonSelectedFactor(0.5f);
        model.setEdgeSelectionColor(true);
        model.setEdgeInSelectionColor(Color.RED);
        model.setEdgeOutSelectionColor(Color.GREEN);
        model.setEdgeBothSelectionColor(Color.BLUE);

        Workspace dest = Utils.roundTrip(new VizModelPersistenceProvider(), model.getWorkspace());
        VizModel read = Utils.getVizModel(dest);
        Assert.assertFalse(read.isAutoSelectNeighbors());
        Assert.assertTrue(read.isHideNonSelectedEdges());
        Assert.assertFalse(read.isLightenNonSelectedAuto());
        Assert.assertEquals(0.5f, read.getLightenNonSelectedFactor(), 0.0001f);
        Assert.assertTrue(read.isEdgeSelectionColor());
        Assert.assertEquals(Color.RED, read.getEdgeInSelectionColor());
        Assert.assertEquals(Color.GREEN, read.getEdgeOutSelectionColor());
        Assert.assertEquals(Color.BLUE, read.getEdgeBothSelectionColor());
    }

    @Test
    public void testNodeLabelSettings() throws Exception {
        VizModel model = Utils.newVizModel();
        model.setShowNodeLabels(true);
        model.setNodeLabelFont(new Font("SansSerif", Font.ITALIC, 16));
        model.setNodeLabelScale(0.8f);
        model.setNodeLabelColorMode(LabelColorMode.OBJECT);
        model.setNodeLabelSizeMode(LabelSizeMode.SCREEN);
        model.setHideNonSelectedNodeLabels(true);
        model.setNodeLabelFitToNodeSize(true);
        model.setAvoidNodeLabelOverlap(false);

        Workspace dest = Utils.roundTrip(new VizModelPersistenceProvider(), model.getWorkspace());
        VizModel read = Utils.getVizModel(dest);
        Assert.assertTrue(read.isShowNodeLabels());
        Assert.assertEquals(0.8f, read.getNodeLabelScale(), 0.0001f);
        Assert.assertEquals(LabelColorMode.OBJECT, read.getNodeLabelColorMode());
        Assert.assertEquals(LabelSizeMode.SCREEN, read.getNodeLabelSizeMode());
        Assert.assertTrue(read.isHideNonSelectedNodeLabels());
        Assert.assertTrue(read.isNodeLabelFitToNodeSize());
        Assert.assertFalse(read.isAvoidNodeLabelOverlap());
    }

    @Test
    public void testEdgeLabelSettings() throws Exception {
        VizModel model = Utils.newVizModel();
        model.setShowEdgeLabels(true);
        model.setEdgeLabelFont(new Font("Monospaced", Font.BOLD, 14));
        model.setEdgeLabelScale(0.6f);
        model.setEdgeLabelColorMode(LabelColorMode.OBJECT);
        model.setEdgeLabelSizeMode(LabelSizeMode.SCREEN);
        model.setHideNonSelectedEdgeLabels(true);

        Workspace dest = Utils.roundTrip(new VizModelPersistenceProvider(), model.getWorkspace());
        VizModel read = Utils.getVizModel(dest);
        Assert.assertTrue(read.isShowEdgeLabels());
        Assert.assertEquals(0.6f, read.getEdgeLabelScale(), 0.0001f);
        Assert.assertEquals(LabelColorMode.OBJECT, read.getEdgeLabelColorMode());
        Assert.assertEquals(LabelSizeMode.SCREEN, read.getEdgeLabelSizeMode());
        Assert.assertTrue(read.isHideNonSelectedEdgeLabels());
    }

    @Test
    public void testScreenshotModel() throws Exception {
        VizModel model = Utils.newVizModel();
        model.getScreenshotModel().setScaleFactor(4);
        model.getScreenshotModel().setTransparentBackground(true);
        model.getScreenshotModel().setAutoSave(true);

        Workspace dest = Utils.roundTrip(new VizModelPersistenceProvider(), model.getWorkspace());
        VizModel read = Utils.getVizModel(dest);
        Assert.assertEquals(4, read.getScreenshotModel().getScaleFactor());
        Assert.assertTrue(read.getScreenshotModel().isTransparentBackground());
        Assert.assertTrue(read.getScreenshotModel().isAutoSave());
    }

    @Test
    public void testSelectionModel() throws Exception {
        VizModel model = Utils.newVizModel();
        model.getSelectionModel().setMouseSelectionDiameter(5);
        model.getSelectionModel().setMouseSelectionZoomProportional(true);
        model.getSelectionModel().setRectangleSelection(true);
        model.getSelectionModel().setSelectionEnable(false);

        Workspace dest = Utils.roundTrip(new VizModelPersistenceProvider(), model.getWorkspace());
        VizModel read = Utils.getVizModel(dest);
        Assert.assertEquals(5, read.getMouseSelectionDiameter());
        Assert.assertTrue(read.isMouseSelectionZoomProportional());
        Assert.assertTrue(read.isRectangleSelection());
        Assert.assertFalse(read.isSelectionEnabled());
    }

    @Test
    public void testLegacyScreenshotMakerBackwardCompatibility() throws Exception {
        // Simulate a <vizmodel> from Gephi 0.10 with the old self-closing <screenshotMaker> element.
        // width/height/antialiasing have no equivalent in the new model and must be gracefully ignored.
        String legacyXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<vizmodel>"
            + "<screenshotMaker width=\"1920\" height=\"1080\" antialiasing=\"4\""
            + " transparent=\"true\" autosave=\"true\"/>"
            + "</vizmodel>";

        VizModel model = Utils.newVizModel();
        StringReader stringReader = new StringReader(legacyXml);
        XMLStreamReader xmlReader = GephiFormat.newXMLReader(stringReader);
        new VizModelPersistenceProvider().readXML(xmlReader, model.getWorkspace());
        xmlReader.close();

        Assert.assertTrue(model.getScreenshotModel().isTransparentBackground());
        Assert.assertTrue(model.getScreenshotModel().isAutoSave());
    }

    @Test
    public void testLegacyTextModelBackwardCompatibility() throws Exception {
        // Simulate a <vizmodel> block from Gephi 0.10 containing the old <textmodel> element.
        String legacyXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<vizmodel>"
            + "<textmodel>"
            + "<shownodelabels enable=\"true\"/>"
            + "<showedgelabels enable=\"true\"/>"
            + "<selectedOnly value=\"true\"/>"
            + "<nodefont name=\"SansSerif\" size=\"24\" style=\"1\"/>"
            + "<edgefont name=\"Monospaced\" size=\"18\" style=\"2\"/>"
            + "<nodesizefactor>0.75</nodesizefactor>"
            + "<edgesizefactor>0.4</edgesizefactor>"
            + "<colormode class=\"ObjectColorMode\"/>"
            + "<sizemode class=\"FixedSizeMode\"/>"
            + "<nodecolumns><column id=\"label\"/></nodecolumns>"
            + "<edgecolumns><column id=\"label\"/></edgecolumns>"
            + "</textmodel>"
            + "</vizmodel>";

        VizModel model = Utils.newVizModel();
        StringReader stringReader = new StringReader(legacyXml);
        XMLStreamReader xmlReader = GephiFormat.newXMLReader(stringReader);
        new VizModelPersistenceProvider().readXML(xmlReader, model.getWorkspace());
        xmlReader.close();

        Assert.assertTrue(model.isShowNodeLabels());
        Assert.assertTrue(model.isShowEdgeLabels());
        Assert.assertTrue(model.isHideNonSelectedNodeLabels());
        Assert.assertTrue(model.isHideNonSelectedEdgeLabels());
        Assert.assertEquals("SansSerif", model.getNodeLabelFont().getFamily());
        Assert.assertEquals(24, model.getNodeLabelFont().getSize());
        Assert.assertEquals(Font.BOLD, model.getNodeLabelFont().getStyle());
        Assert.assertEquals("Monospaced", model.getEdgeLabelFont().getFamily());
        Assert.assertEquals(18, model.getEdgeLabelFont().getSize());
        Assert.assertEquals(0.75f, model.getNodeLabelScale(), 0.0001f);
        Assert.assertEquals(0.4f, model.getEdgeLabelScale(), 0.0001f);
        Assert.assertEquals(LabelColorMode.OBJECT, model.getNodeLabelColorMode());
        Assert.assertEquals(LabelColorMode.OBJECT, model.getEdgeLabelColorMode());
        Assert.assertEquals(LabelSizeMode.SCREEN, model.getNodeLabelSizeMode());
        Assert.assertEquals(LabelSizeMode.SCREEN, model.getEdgeLabelSizeMode());
        Assert.assertEquals(1, model.getNodeLabelColumns().length);
        Assert.assertEquals("label", model.getNodeLabelColumns()[0].getId());
    }

    @Test
    public void testDefaultLabelColumnsRoundTrip() throws Exception {
        VizModel model = Utils.newVizModel();
        // Default label column ("label") must survive the round-trip.
        Assert.assertEquals(1, model.getNodeLabelColumns().length);
        Assert.assertEquals("label", model.getNodeLabelColumns()[0].getId());

        Workspace dest = Utils.roundTrip(new VizModelPersistenceProvider(), model.getWorkspace());
        VizModel read = Utils.getVizModel(dest);
        Assert.assertEquals(1, read.getNodeLabelColumns().length);
        Assert.assertEquals("label", read.getNodeLabelColumns()[0].getId());
    }
}
