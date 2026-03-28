package org.gephi.desktop.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import org.gephi.desktop.selection.api.SelectionUIModel;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.Model;
import org.gephi.project.spi.WorkspaceXMLPersistenceProvider;

public class SelectionUIModelImpl implements SelectionUIModel, Model, WorkspaceXMLPersistenceProvider {

    private final Workspace workspace;
    private final GraphModel graphModel;
    private final Set<String> hiddenColumnIds = new HashSet<>();
    private boolean editMode = false;
    private boolean showNullColumns = false;
    private Node[] selectedNodes = null;
    private Edge[] selectedEdges = null;

    public SelectionUIModelImpl(Workspace workspace) {
        this.workspace = workspace;
        this.graphModel = workspace.getLookup().lookup(GraphModel.class);

        // Hidden by default
        hiddenColumnIds.add(craftColumnId(graphModel.defaultColumns().nodeTimeSet()));
        hiddenColumnIds.add(craftColumnId(graphModel.defaultColumns().degree()));
        hiddenColumnIds.add(craftColumnId(graphModel.defaultColumns().inDegree()));
        hiddenColumnIds.add(craftColumnId(graphModel.defaultColumns().outDegree()));
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }


    public GraphModel getGraphModel() {
        return graphModel;
    }

    public List<Column> getEligibleColumns() {
        Table nodeTable = graphModel.getNodeTable();
        final boolean directed = graphModel.isDirected();
        final Column nodeTimesetCol = graphModel.defaultColumns().nodeTimeSet();
        final Column nodeDegreeCol = graphModel.defaultColumns().degree();
        final Column inDegreeCol = graphModel.defaultColumns().inDegree();
        final Column outDegreeCol = graphModel.defaultColumns().outDegree();
        List<Column> cols = StreamSupport.stream(nodeTable.spliterator(), false)
            .filter(column -> {
                if (column == nodeTimesetCol && graphModel.isDynamic()) {
                    return true;
                }
                if (column == nodeDegreeCol) {
                    return true;
                }
                return !column.isProperty();
            }).collect(Collectors.toCollection(ArrayList::new));
        if (directed) {
            cols.add(0, outDegreeCol);
            cols.add(0, inDegreeCol);
        }
        cols.add(0, nodeDegreeCol);
        return cols;
    }

    public List<Column> getSelectedColumns() {
        return getEligibleColumns().stream().filter(this::isColumnVisible).toList();
    }

    public Node[] getSelectedNodes() {
        return selectedNodes;
    }

    public void setSelectedNodes(Node[] selectedNodes) {
        this.selectedNodes = selectedNodes;
        this.selectedEdges = null;
    }

    public void resetSelection() {
        this.selectedNodes = null;
        this.selectedEdges = null;
    }

    public Edge[] getSelectedEdges() {
        return selectedEdges;
    }

    public void setSelectedEdges(Edge[] selectedEdges) {
        this.selectedEdges = selectedEdges;
        this.selectedNodes = null;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public boolean isShowNullColumns() {
        return showNullColumns;
    }

    public void setShowNullColumns(boolean showNullColumns) {
        this.showNullColumns = showNullColumns;
    }

    public boolean isColumnVisible(Column column) {
        return !hiddenColumnIds.contains(craftColumnId(column));
    }

    private String craftColumnId(Column column) {
        String id = column.getId();
        if (column.isProperty()) {
            id = "_property_" + id;
        }
        return id;
    }

    public void setColumnHidden(Column column, boolean hidden) {
        String columnId = craftColumnId(column);
        if (hidden) {
            hiddenColumnIds.add(columnId);
        } else {
            hiddenColumnIds.remove(columnId);
        }
    }

    public Set<String> getHiddenColumnIds() {
        return Collections.unmodifiableSet(hiddenColumnIds);
    }

    @Override
    public void writeXML(XMLStreamWriter writer, Workspace workspace) {
        try {
            writer.writeStartElement("editmode");
            writer.writeCharacters(String.valueOf(editMode));
            writer.writeEndElement();

            writer.writeStartElement("shownullcolumns");
            writer.writeCharacters(String.valueOf(showNullColumns));
            writer.writeEndElement();

            for (String columnId : hiddenColumnIds) {
                writer.writeStartElement("hiddencolumn");
                writer.writeAttribute("id", columnId);
                writer.writeEndElement();
            }
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void readXML(XMLStreamReader reader, Workspace workspace) {
        try {
            boolean end = false;
            boolean readHiddenColumns = false;
            while (reader.hasNext() && !end) {
                int eventType = reader.next();
                if (eventType == XMLEvent.START_ELEMENT) {
                    String name = reader.getLocalName();
                    if ("editmode".equalsIgnoreCase(name)) {
                        editMode = Boolean.parseBoolean(reader.getElementText());
                    } else if ("shownullcolumns".equalsIgnoreCase(name)) {
                        showNullColumns = Boolean.parseBoolean(reader.getElementText());
                    } else if ("hiddencolumn".equalsIgnoreCase(name)) {
                        if (!readHiddenColumns) {
                            hiddenColumnIds.clear();
                            readHiddenColumns = true;
                        }
                        String id = reader.getAttributeValue(null, "id");
                        if (id != null) {
                            hiddenColumnIds.add(id);
                        }
                    }
                } else if (eventType == XMLEvent.END_ELEMENT) {
                    if (getIdentifier().equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                }
            }
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getIdentifier() {
        return "selectionui";
    }
}
