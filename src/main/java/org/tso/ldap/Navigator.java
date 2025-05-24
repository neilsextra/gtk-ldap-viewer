package org.tso.ldap;

import java.util.ArrayList;

import org.gnome.gio.ApplicationFlags;

import org.gnome.gtk.Application;
import org.gnome.gtk.Button;

import org.gnome.gtk.GtkBuilder;

import org.gnome.gtk.Window;

import org.tso.ldap.util.GuiUtils;

public class Navigator {

    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    Window mainWindow;
    Connection connection;

    ArrayList<Object> asHex(byte[] buf) {

        var values = new ArrayList<Object>(2);

        StringBuffer asciiChars = new StringBuffer();

        String[] hex = new String[16];

        for (int iHex = 0; iHex < hex.length; iHex++) {
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