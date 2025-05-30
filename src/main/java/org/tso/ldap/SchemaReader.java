package org.tso.ldap;

import java.util.Set;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.ldap.model.schema.SchemaObjectWrapper;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.api.ldap.model.schema.AttributeType;

public class SchemaReader {
    LdapConnection ldapConnection;
     Dictionary<String, AttributeType> defintions = new Hashtable<>();

    public SchemaReader(LdapConnection ldapConnection) throws Exception {

        this.ldapConnection = ldapConnection;
        
        this.ldapConnection.loadSchemaRelaxed();

        SchemaManager schemaManager =  this.ldapConnection.getSchemaManager();

        if (schemaManager == null) {
           System.out.println("SchemaManager is NULL: " );
        } else {
           System.out.println("SchemaManager is not NULL: " );
        }

        schemaManager.loadAllEnabled();

        schemaManager.getAllSchemas().forEach(schema -> {
            System.out.println("Schema: " + schema.toString());

            Set<SchemaObjectWrapper> content =  schema.getContent();

            for (var attribute : content) {
  
                if (attribute.get() instanceof AttributeType) {
                   
                    if  (((AttributeType)attribute.get()).getSyntaxName() != null) {
                        defintions.put(((AttributeType)attribute.get()).getSyntaxName(),  ((AttributeType)attribute.get()));
                    } 

                }

            }
            
        });

    }

    public AttributeType getDefinition(String name) {

        return defintions.get(name);

    }

}