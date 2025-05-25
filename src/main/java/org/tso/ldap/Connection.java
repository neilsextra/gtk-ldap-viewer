package org.tso.ldap;


import org.gnome.gtk.GtkBuilder;
import org.gnome.gtk.Window;
import org.tso.ldap.util.GuiUtils;

public class Connection {
    Window window;
    GtkBuilder builder;

    Connection(String definition) throws Exception {
        builder = new GtkBuilder();

        var uiDefinition = GuiUtils.getDefintion(definition);

        builder.addFromString(uiDefinition, uiDefinition.length());

        this.window = (Window) builder.getObject("openDialog");
    }

    void show() {

        this.window.setVisible(true);

    }

}
