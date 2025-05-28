package org.tso.ldap;

import java.util.ArrayList;

import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.SearchScope;

public class Search {
    Connection connection;

    public Search(Connection connection) {
        this.connection = connection;
    }

    public void search(String dn, ArrayList<Entry> entries) throws Exception {
        try ( EntryCursor cursor = this.connection.getLdapConnection().search( dn, "(objectclass=*)", SearchScope.ONELEVEL ))
        {
            for ( Entry entry : cursor ) {
            
                System.out.println( entry );

                entries.add(entry);

            }   
            
        }

    } 
    
}