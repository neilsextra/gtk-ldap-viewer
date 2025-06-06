package org.tso.ldap.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.gnome.gtk.Inscription;
import org.gnome.gtk.ListItem;
import org.gnome.gtk.SignalListItemFactory;
import org.tso.ldap.Navigator;

public class GuiUtils {
    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

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

    static final public ArrayList<Object> asHex(byte[] buf) {

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


     static final public String formatHex(String[] hexString) {
        StringBuffer buffer = new StringBuffer();

        char[] hex = hexString[0].toCharArray();
        char[] ascii = hexString[1].toCharArray();

        int iHex = 0;
        int iChar = 0;

        while (iChar < ascii.length) {
            int iPos = 0;

            for (iPos = 0; iHex < hex.length && iPos < 8; iHex += 2, iPos += 1) {
                buffer.append(hex[iHex]);
                buffer.append(hex[iHex + 1]);
                buffer.append(" ");
            }

            if (iPos < 8) {
                for (; iPos < 8; iPos++) {
                    buffer.append("   ");
                }
            }

            buffer.append("| ");

            iPos = 0;

            for (; iChar < ascii.length && (iPos < 8); iChar++, iPos += 1) {
                buffer.append(ascii[iChar]);
            }
        }

        return buffer.toString();
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

}
