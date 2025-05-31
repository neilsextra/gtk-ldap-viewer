package org.tso.ldap;

import java.util.HashMap;

import org.apache.directory.ldap.client.api.DefaultLdapConnectionFactory;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;


public class Connection {
    HashMap<String, String> properties;
    LdapConnection connection;
    SchemaBrowser schemaBrowser;
    
    public Connection(String url) throws Exception {
		this.properties = new HashMap<>();

		String[] parts = url.split("/|:|@");

        if (parts.length == 7) {
            this.properties.put("protocol", parts[0]);
            this.properties.put("username", parts[3]);
            this.properties.put("password", parts[4]);
            this.properties.put("host", parts[5]);
            this.properties.put("port", parts[6]);
        } else {
            throw new Exception("Invalid URL");
        }

    }

    Connection connect() throws Exception {
        LdapConnectionConfig config = new LdapConnectionConfig();

        config.setLdapHost( this.properties.get("host") );
        config.setLdapPort( Integer.parseInt(this.properties.get("port" )));
        config.setName( this.properties.get("username" ));
        config.setCredentials( this.properties.get("password") );

         DefaultLdapConnectionFactory factory = new DefaultLdapConnectionFactory( config );
        
         factory.setTimeOut( 10000 );

        this.connection = factory.newLdapConnection();

        this.schemaBrowser = new SchemaBrowser(this.connection);

        return this;
    }

    LdapConnection getLdapConnection() {
        return this.connection;
    }


    SchemaBrowser getSchemaBrowser() {
        return this.schemaBrowser;
    }


    public void close() throws Exception {

        this.connection.close();
    }

}