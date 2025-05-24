package org.tso.ldap.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

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

}
