package org.tso.ldap;

import java.util.ArrayList;

import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.gnome.gio.ApplicationFlags;
import org.gnome.gio.ListStore;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Align;
import org.gnome.gtk.Application;
import org.gnome.gtk.Button;
import org.gnome.gtk.ColumnView;
import org.gnome.gtk.ColumnViewColumn;
import org.gnome.gtk.GtkBuilder;
import org.gnome.gtk.Inscription;
import org.gnome.gtk.Label;
import org.gnome.gtk.ListItem;
import org.gnome.gtk.ListView;
import org.gnome.gtk.SearchEntry;
import org.gnome.gtk.SelectionModel;
import org.gnome.gtk.SignalListItemFactory;
import org.gnome.gtk.SingleSelection;
import org.gnome.gtk.TextBuffer;
import org.gnome.gtk.TextIter;
import org.gnome.gtk.TextView;
import org.gnome.gtk.Window;
import org.tso.ldap.util.GuiUtils;

import io.github.jwharm.javagi.gio.ListIndexModel;
import io.github.jwharm.javagi.gobject.types.Types;

public class Navigator {
    Window mainWindow;
    TextView attributeViewer;
    ConnectionDialog connectionDialog;
    AboutDialog aboutDialog;
    ListStore<Row> store;
    ListView listView;
    ColumnView columnView;
    SearchEntry searchEntry;
    Connection connection = null;
    ArrayList<Entry> entries = new ArrayList<Entry>();
    ListIndexModel listIndexModel;
    
    public static final class Row extends GObject {

        public static Type gtype = Types.register(Row.class);
        public String name;
        public String oid;
        public String syntax;
        public String primitiveType;
        public String value;

        public Row(String name, String oid, String syntax, String primitiveType, String value) {

            this.name = name;
            this.oid = oid;
            this.syntax = syntax;
            this.primitiveType = primitiveType;
            this.value = value;

        }

        public String getName() {
            return this.name;
        }

        public String getOid() {
            return this.oid;
        }

        public String getSyntax() {
            return this.syntax;
        }

        public String getPrimitiveType() {
            return this.primitiveType;
        }

        public String getValue() {
            return this.value;
        }

    }

    void buildRows(Entry entry) {

        for (Attribute attribute : entry.getAttributes()) {

            Row row = new Row(attribute.getAttributeType().getName(), 
                              attribute.getAttributeType().getOid(),
                              attribute.getAttributeType().getSyntaxOid(),
                              attribute.get().isHumanReadable() ? "String" : "Binary",
                              attribute.get().isHumanReadable() ? attribute.get().getString() : attribute.get().getEscaped());

            this.store.add(0, row);

        }

    }

    void selectRow(int selected) {
        Row row = store.get(selected);

        attributeViewer.setMonospace(true);
        TextBuffer buffer = new TextBuffer();
        TextIter   iter = new TextIter();
        buffer.getStartIter(iter);

        String description = "<span weight=\"ultraheavy\" size=\"x-large\">Definition</span>" + "\n" + 
                            "<b>Class:</b>" + row.getName() + "\n" + 
                            "<b>OID:</b>" + row.getOid() + "\n" +
                            "<b>Syntax:</b>" + row.getSyntax() + "\n" +
                            connection.getSchemaBrowser().getType( row.getSyntax()) + "\n\n" +
                            "<span weight=\"ultraheavy\"  size=\"x-large\">Value</span>" + "\n" + 
                            row.getValue();
        
        buffer.insertMarkup(iter, description, -1);
        attributeViewer.setBuffer(buffer);
                
    }

    void setupColumns(ColumnView columnview) {
        var columnFactoryName = GuiUtils.createSignalListItemFactory();

        columnFactoryName.onBind(item -> {
            var listitem = (ListItem) item;
            var inscription = (Inscription) listitem.getChild();
            var row = (Row) listitem.getItem();
            inscription.setText(row.getName());

        });

        var columnFactoryOid =  GuiUtils.createSignalListItemFactory();

        columnFactoryOid.onBind(item -> {
            var listitem = (ListItem) item;
            var inscription = (Inscription) listitem.getChild();

            var row = (Row) listitem.getItem();
            inscription.setText(row.getOid());

        });

        var columnFactorySyntax =  GuiUtils.createSignalListItemFactory();

        columnFactorySyntax.onBind(item -> {
            var listitem = (ListItem) item;
            var inscription = (Inscription) listitem.getChild();

            var row = (Row) listitem.getItem();
            inscription.setText(row.getSyntax());

        });

        var columnFactorType = GuiUtils.createSignalListItemFactory();

        columnFactorType.onBind(item -> {
            var listitem = (ListItem) item;
            var inscription = (Inscription) listitem.getChild();
            var row = (Row) listitem.getItem();
            inscription.setText(row.getPrimitiveType());

        });

        var columnFactorValue =  GuiUtils.createSignalListItemFactory();

        columnFactorValue.onBind(item -> {
            var listitem = (ListItem) item;
            var inscription = (Inscription) listitem.getChild();
            var row = (Row) listitem.getItem();
            inscription.setText(row.getValue());

        });

        var columnName = new ColumnViewColumn("Name", columnFactoryName);
        var columnOid = new ColumnViewColumn("OID", columnFactoryOid);
        var columnSyntax = new ColumnViewColumn("Syntax", columnFactorySyntax);
        var columnType = new ColumnViewColumn("Type", columnFactorType);
        var columnValue = new ColumnViewColumn("Value", columnFactorValue);

        columnName.setFixedWidth(250);
        columnName.setResizable(true);

        columnOid.setFixedWidth(150);
        columnOid.setResizable(true);
        
        columnSyntax.setFixedWidth(150);
        columnSyntax.setResizable(true);
        
        columnType.setFixedWidth(50);
        columnType.setResizable(true);

        columnValue.setExpand(true);
        columnValue.setResizable(true);
 
        columnview.appendColumn(columnName);
        columnview.appendColumn(columnOid);
        columnview.appendColumn(columnSyntax);
        columnview.appendColumn(columnType);
        columnview.appendColumn(columnValue);

    }

    void setupList(ListView listView) {
        SignalListItemFactory factory = new SignalListItemFactory();

        factory.onSetup(object -> {
            ListItem listitem = (ListItem) object;
            Label label = new Label("");

            label.setHalign(Align.START);
            listitem.setChild(label);

        });
        
        factory.onBind(object -> {
            ListItem listitem = (ListItem) object;
            Label label = (Label) listitem.getChild();
            ListIndexModel.ListIndex item = (ListIndexModel.ListIndex) listitem.getItem();
            
            if (label == null || item == null) 
                return;

            int index = item.getIndex();
            
            Entry entry = entries.get(index);
            
            label.setLabel(entry.getDn().toString());

        });

        listIndexModel = new ListIndexModel(entries.size());
        listView.setModel(new SingleSelection<>(listIndexModel));
        listView.setFactory(factory);
        
        ((SingleSelection<?>)(listView.getModel())).onSelectionChanged((int position, int nItems) -> {
            Navigator.this.store.removeAll();
            
            int selected = ((SingleSelection<?>)(listView.getModel())).getSelected();
            
            Entry entry = entries.get(selected);
            
            Navigator.this.buildRows(entry);

            Navigator.this.selectRow(0);
        });

    }

    void open() {
        connectionDialog.show();
    }

    
    void about() {
       aboutDialog.show();
    }

    void search() {

        try {
            entries.clear();
            listIndexModel.setSize(entries.size());

            Search search = new Search(this.connection);

            search.search(searchEntry.getText(), entries);

            listIndexModel.setSize(entries.size());

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    public void activate(Application app) {
        GtkBuilder builder = new GtkBuilder();

        try {
            var uiDefinition = GuiUtils.getDefintion("/org/tso/ldap/navigator.ui");

            builder.addFromString(uiDefinition, uiDefinition.length());
           
            mainWindow = (Window) builder.getObject("main");
            attributeViewer = (TextView)builder.getObject("attributeViewer");

            var openToolbarButton = (Button) builder.getObject("openToolbarButton");
            var aboutToolbarItem = (Button) builder.getObject("aboutToolbarItem");

            openToolbarButton.onClicked(this::open);
            aboutToolbarItem.onClicked(this::about);
            
            connectionDialog = new ConnectionDialog(mainWindow, "/org/tso/ldap/open-dialog.ui",
                connection -> {        
                    Navigator.this.connection = connection;
                    Navigator.this.searchEntry.setEditable(true);
                    
                    entries.clear();
                    listIndexModel.setSize(entries.size());

                }
            
            );

            aboutDialog = new AboutDialog(mainWindow, "/org/tso/ldap/about-dialog.ui");

            columnView = (ColumnView) builder.getObject("attributesViewer");
 
            columnView.setShowColumnSeparators(true);

            store = new ListStore<>(Row.gtype);
            setupColumns(columnView);

            columnView.setModel(new SingleSelection<Row>(store));

            ((SingleSelection<?>)(columnView.getModel())).onSelectionChanged(new SelectionModel.SelectionChangedCallback() {
                public void run(int position, int nItems)  {
                
                   Navigator.this.selectRow(((SingleSelection<?>)(columnView.getModel())).getSelected());
                }   

            });

            listView = (ListView) builder.getObject("selectionView");
            setupList(listView);

            searchEntry = (SearchEntry) builder.getObject("search");

            searchEntry.onActivate(this::search);
  
            mainWindow.setApplication(app);

            mainWindow.setVisible(true);

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    public Navigator(String[] args) {
        Application app = new Application("org.tso.ldap.Navigator", ApplicationFlags.DEFAULT_FLAGS);

        app.onActivate(() -> activate(app));
        app.run(args);

    }

    public static void main(String[] args) {
        new Navigator(args);
    }

}