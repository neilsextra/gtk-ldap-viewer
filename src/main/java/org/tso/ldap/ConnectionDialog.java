package org.tso.ldap;

import org.gnome.gtk.AlertDialog;
import org.gnome.gtk.Button;
import org.gnome.gtk.Entry;
import org.gnome.gtk.EntryBuffer;
import org.gnome.gtk.GtkBuilder;
import org.gnome.gtk.Window;
import org.tso.ldap.util.GuiUtils;


public class ConnectionDialog {

    @FunctionalInterface
    interface Callback {

        void onConnection(DirectoryConnection connection);

    }
   
    Window window;
    GtkBuilder builder;
    DirectoryConnection connection = null;

    ConnectionDialog(Window parent, final String definition, Callback callback) throws Exception {

        builder = new GtkBuilder();

        var uiDefinition = GuiUtils.getDefintion(definition);

        builder.addFromString(uiDefinition, uiDefinition.length());

        this.window = (Window) builder.getObject("openDialog");

        this.window.setParent(parent);

        final var okButton = (Button) builder.getObject("button_ok");
        final var connectionUrl = (Entry) builder.getObject("connection");

        okButton.onClicked(() -> {
            EntryBuffer buffer = connectionUrl.getBuffer();

            try {
                connection = new DirectoryConnection(buffer.getText());

                ConnectionDialog.this.connection.connect();

                System.out.println("Connection Successful");

                callback.onConnection(connection);

                window.close();

            } catch (Exception e) {
               AlertDialog.builder()
                        .setModal(true)
                        .setMessage("Connection")
                        .setDetail(e.getMessage())
                        .build()
                        .show(ConnectionDialog.this.window); 
            }
        
        });

        var closeButton = (Button) builder.getObject("button_cancel");

        closeButton.onClicked(window::close);

    }

    void show() {

        this.window.setVisible(true);

    }

}
