package org.gephi.visualization;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.graph.GraphGenerator;
import org.gephi.project.api.Workspace;
import org.gephi.project.io.utils.GephiFormat;
import org.junit.Assert;

public class Utils {

    public static VizModel newVizModel() {
        VizController vizController = new VizController();
        Workspace workspace = GraphGenerator.build().generateTinyGraph().getWorkspace();
        VizModel vizModel = new VizModel(vizController, workspace);
        workspace.add(vizModel);
        return vizModel;
    }

    public static VizModel getVizModel(Workspace workspace) {
        return workspace.getLookup().lookup(VizModel.class);
    }

    /**
     * Performs a full persistence round-trip: serializes the source workspace to XML, reads it
     * into a freshly created destination workspace, serializes the destination again, and asserts
     * both XML representations are identical (idempotency check).
     *
     * <p>The destination workspace is pre-populated with its own {@link VizModel} so that the
     * provider's read path can resolve the model without depending on the global Lookup.
     */
    public static Workspace roundTrip(VizModelPersistenceProvider provider, Workspace sourceWorkspace)
        throws Exception {
        String xmlString = toXMLString(provider, sourceWorkspace);

        VizModel destVizModel = newVizModel();
        Workspace destWorkspace = destVizModel.getWorkspace();

        StringReader stringReader = new StringReader(xmlString);
        XMLStreamReader xmlReader = GephiFormat.newXMLReader(stringReader);
        provider.readXML(xmlReader, destWorkspace);
        xmlReader.close();
        stringReader.close();

        String xmlStringAgain = toXMLString(provider, destWorkspace);
        Assert.assertEquals(xmlString, xmlStringAgain);

        return destWorkspace;
    }

    public static String toXMLString(VizModelPersistenceProvider provider, Workspace workspace)
        throws Exception {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = GephiFormat.newXMLWriter(stringWriter);
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement(provider.getIdentifier());
        provider.writeXML(writer, workspace);
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();
        stringWriter.close();
        return stringWriter.toString();
    }
}
