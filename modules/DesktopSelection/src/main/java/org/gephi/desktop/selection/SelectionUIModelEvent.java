package org.gephi.desktop.selection;

import java.beans.PropertyChangeEvent;

public class SelectionUIModelEvent extends PropertyChangeEvent {

    public static String MODEL = "model";
    public static String EDIT_MODE = "mode";
    public static String HIDDEN_COLUMN_IDS = "hiddenColumnIds";
    public static String SELECTED_ELEMENTS = "selectedElements";
    public static String SHOW_NULL_COLUMNS = "showNullColumns";

    public SelectionUIModelEvent(Object source, String propertyName,
                                 Object oldValue, Object newValue) {
        super(source, propertyName, oldValue, newValue);
    }
}
