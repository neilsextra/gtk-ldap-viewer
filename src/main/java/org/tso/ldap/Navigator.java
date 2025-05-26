package org.tso.ldap;

import org.gnome.gio.ApplicationFlags;
import org.gnome.gio.ListStore;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Application;
import org.gnome.gtk.Button;
import org.gnome.gtk.ColumnView;
import org.gnome.gtk.ColumnViewColumn;
import org.gnome.gtk.GtkBuilder;
import org.gnome.gtk.Inscription;
import org.gnome.gtk.ListItem;
import org.gnome.gtk.NoSelection;
import org.gnome.gtk.SearchEntry;
import org.gnome.gtk.Window;
import org.tso.ldap.util.GuiUtils;

import io.github.jwharm.javagi.gobject.types.Types;

public class Navigator {

    
    Window mainWindow;
    ConnectionDialog connection;
    ListStore<Row> store;
    ColumnView columnView;

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
            inscription.setText(row.getPrimitiveType());

        });

        var columnName = new ColumnViewColumn("Name", columnFactoryName);
        var columnOid = new ColumnViewColumn("OID", columnFactoryOid);
        var columnSyntax = new ColumnViewColumn("Syntax", columnFactorySyntax);
        var columnType = new ColumnViewColumn("Type", columnFactorType);
        var columnValue = new ColumnViewColumn("Value", columnFactorType);

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

    void open() {

        connection.show();

    }

    void search() {

        System.out.println("Searching");

    }

    public void activate(Application app) {
        GtkBuilder builder = new GtkBuilder();

        try {
            var uiDefinition = GuiUtils.getDefintion("/org/tso/ldap/navigator.ui");

            builder.addFromString(uiDefinition, uiDefinition.length());
            mainWindow = (Window) builder.getObject("main");

            var openToolbarButton = (Button) builder.getObject("openToolbarButton");

            openToolbarButton.onClicked(this::open);
            connection = new ConnectionDialog("/org/tso/ldap/open-dialog.ui");

            columnView = (ColumnView) builder.getObject("attributesViewer");

            columnView.setShowColumnSeparators(true);

            store = new ListStore<>(Row.gtype);
            setupColumns(columnView);
            columnView.setModel(new NoSelection<Row>(store));

            var searchEntry = (SearchEntry) builder.getObject("search");

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