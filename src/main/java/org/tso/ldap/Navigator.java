package org.tso.ldap;

import java.util.ArrayList;

import org.gnome.gio.ApplicationFlags;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Application;
import org.gnome.gtk.Button;
import org.gnome.gtk.ColumnView;
import org.gnome.gtk.ColumnViewColumn;
import org.gnome.gtk.GtkBuilder;
import org.gnome.gtk.Inscription;
import org.gnome.gtk.ListItem;
import org.gnome.gtk.SignalListItemFactory;
import org.gnome.gtk.Window;

import org.tso.ldap.util.GuiUtils;

import io.github.jwharm.javagi.gobject.types.Types;

public class Navigator {

    
    Window mainWindow;
    Connection connection;

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
        var columnFactory = new SignalListItemFactory();

        columnFactory.onSetup(item -> {
            var listitem = (ListItem) item;
            var inscription = Inscription.builder()
                    .setXalign(0)
                    .build();
            listitem.setChild(inscription);

        });

        columnFactory.onBind(item -> {
            var listitem = (ListItem) item;
            var inscription = (Inscription) listitem.getChild();

            if (inscription != null) {
                var row = (Row) listitem.getItem();
                inscription.setText(row.getName());
            }
        });

        var column = new ColumnViewColumn("", columnFactory);
        column.setExpand(true);

        columnview.appendColumn(column);

    }

    void open() {

        connection.show();
        
    }

    public void activate(Application app) {
        GtkBuilder builder = new GtkBuilder();

        try {
            var uiDefinition = GuiUtils.getDefintion("/org/tso/ldap/navigator.ui");

            builder.addFromString(uiDefinition, uiDefinition.length());

            mainWindow = (Window) builder.getObject("main");

            var openToolbarButton = (Button) builder.getObject("openToolbarButton");

            openToolbarButton.onClicked(this::open);

            connection = new Connection("/org/tso/ldap/open-dialog.ui");

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