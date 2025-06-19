package org.tso.ldap;

import java.util.HashMap;

import org.apache.directory.ldap.client.api.DefaultLdapConnectionFactory;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;

public class DirectoryConnection implements Runnable {
    HashMap<String, String> properties;
    LdapConnection connection;
    SchemaExplorer schemaExplorer;
    DirectoryExplorer directoryExplorer;
    Exception connectionException; 


    public DirectoryConnection(String url, String password) throws Exception {
        this.properties = new HashMap<>();

        String[] parts = url.split("/|:|@");

        if (parts.length == 7) {
            this.properties.put("protocol", parts[0]);
            this.properties.put("username", parts[3]);
            this.properties.put("host", parts[5]);
            this.properties.put("port", parts[6]);
        } else {
            throw new Exception("Invalid URL");
        }
        
        this.properties.put("password", password);

    }

    private void connect() {
        this.connectionException = null;
        try {
            LdapConnectionConfig config = new LdapConnectionConfig();

            config.setLdapHost(this.properties.get("host"));
            config.setLdapPort(Integer.parseInt(this.properties.get("port")));
            config.setName(this.properties.get("username"));
            config.setCredentials(this.properties.get("password"));

            DefaultLdapConnectionFactory factory = new DefaultLdapConnectionFactory(config);

            factory.setTimeOut(10000);

            this.connection = factory.newLdapConnection();

            this.schemaExplorer = new SchemaExplorer(this);
            this.directoryExplorer = new DirectoryExplorer(this);

        } catch (Exception exception) {
            connectionException = exception;
        }

    }

    Exception getConnectionException() {
        return connectionException;
    }

    LdapConnection getLdapConnection() {
        return this.connection;
    }

    SchemaExplorer getSchemaExplorer() {
        return this.schemaExplorer;
    }

    DirectoryExplorer getDirectoryExplorer() {
        return this.directoryExplorer;
    }

    public void close() throws Exception {

        this.connection.close();
    }

    @Override
    public void run() {
        try {
            this.connect();
        } catch (Exception e) {
           
        }
    }

}
