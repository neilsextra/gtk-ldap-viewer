package org.tso.ldap.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.gnome.gtk.Inscription;
import org.gnome.gtk.ListItem;
import org.gnome.gtk.SignalListItemFactory;
import org.gnome.gtk.TextBuffer;
import org.gnome.gtk.TextIter;
import org.gnome.gtk.TextView;
import org.tso.ldap.Navigator;

public class GuiUtils {

    static final public String getDefintion(String definition) throws Exception {
        InputStream inputStream = Navigator.class.
                getResourceAsStream(definition);

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];

        for (int length; (length = inputStream.read(buffer)) != -1;) {
            output.write(buffer, 0, length);
        }

        return output.toString("UTF-8");

    }

    static final String hexToChar(String hex) {

        int result = Integer.parseInt(hex, 16);

        var output = (result >= 32 && result <= 127) ? Character.toChars(result) : '.';

        return output.toString();

    }

    static final public String formatHex(String value) {
        String output = "";

        List<String> hex = new ArrayList<>();

        for (var iChar = 0; iChar < value.length(); iChar += 2) {

            hex.add(value.substring(iChar, iChar + 2));

        }

        var hexValues = "";
        var charValues = "";

        var iHex = 0;
        var iPos = 0;

        for (; iHex < hex.size(); iHex++) {

            output += String.format("%1$010X", iPos).toUpperCase();

            iPos += 1;

            hexValues += " " + hex.get(iHex) + " | ";
            charValues += hexToChar(hex.get(iHex));

            if (iPos % 16 == 0) {
                output += hexValues;
                output += charValues;
                output += "\n";

                hexValues = "";
                charValues = "";
            }

            if (iHex % 16 != 0) {
                output += String.format("%1$010X", iPos).toUpperCase();

                output += hexValues;

                for (var iCount = 0; iPos % 16 != 0; iPos++, iCount++) {

                    output += iCount < 16 ? "   " : "  | ";

                }

                output += charValues;

                output += "\n";
            }

        }

        return output;

    }

    static final public SignalListItemFactory createSignalListItemFactory() {

        var columnFactory = new SignalListItemFactory();

        columnFactory.onSetup(item -> {
            var listitem = (ListItem) item;
            var inscription = Inscription.builder()
                    .setXalign(0)
                    .build();
            listitem.setChild(inscription);

        });

        return columnFactory;

    }

    static final public void clearTextView(TextView textView) {
        TextBuffer buffer = new TextBuffer();
        TextIter iter = new TextIter();

        buffer.getStartIter(iter);
        buffer.insertMarkup(iter, "", -1);

        textView.setBuffer(buffer);
    }

}
