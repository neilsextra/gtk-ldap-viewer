package org.tso.ldap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.gnome.gio.ApplicationFlags;
import org.gnome.gio.File;
import org.gnome.gio.ListStore;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;

import org.gnome.gtk.Application;
import org.gnome.gtk.Button;

import org.gnome.gtk.GtkBuilder;
import org.gnome.gtk.Inscription;
import org.gnome.gtk.ListItem;
import org.gnome.gtk.NoSelection;
import org.gnome.gtk.SignalListItemFactory;
import org.gnome.gtk.Window;

import io.github.jwharm.javagi.gobject.types.Types;

public class Navigator {

    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    Window window;
 
    ArrayList<Object> asHex(byte[] buf) {

        var values = new ArrayList<Object>(2);

        StringBuffer asciiChars = new StringBuffer();

        String[] hex = new String[16];

        for (int iHex = 0; iHex <hex.length; iHex++) {
            hex[iHex] = "";
        }

        for (int i = 0, c = 0; i < buf.length; i++, c++) {
            char[] chars = new char[2];

            chars[0] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
            chars[1] = HEX_CHARS[buf[i] & 0x0F];

            hex[c] = new String(chars);

            asciiChars.append(Character.isLetterOrDigit(buf[i]) ? (char) buf[i] : '.');

        }

        values.add(hex);
        values.add(asciiChars);

        return values;
    }

    void open() {
 
    }

    public void activate(Application app) {
        GtkBuilder builder = new GtkBuilder();

        try {
            InputStream inputStream = Navigator.class.
                    getResourceAsStream("/org/tso/ldap/navigator.ui");

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];

            for (int length; (length = inputStream.read(buffer)) != -1;) {
                output.write(buffer, 0, length);
            }

            var uiDefinition = output.toString("UTF-8");

            builder.addFromString(uiDefinition, uiDefinition.length());

            window = (Window) builder.getObject("main");

            var openToolbarButton = (Button) builder.getObject("openToolbarButton");

            openToolbarButton.onClicked(this::open);
            
            window.setApplication(app);

            window.setVisible(true);

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
