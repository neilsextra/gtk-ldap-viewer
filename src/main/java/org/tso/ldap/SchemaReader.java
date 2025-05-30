package org.tso.ldap;

import java.util.Set;

import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.ldap.model.schema.SchemaObjectWrapper;
import org.apache.directory.ldap.client.api.LdapConnection;

public class SchemaReader {
    LdapConnection ldapConnection;

    public SchemaReader(LdapConnection ldapConnection) throws Exception {

        this.ldapConnection = ldapConnection;
   
        System.out.println("Print inside Schema Reader");
        
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
                System.out.println("Attribute: " + attribute.toString());
                System.out.println("Attribute: " + attribute.get().toString());
                System.out.println("Attribute Name: " + attribute.get().getName());
                System.out.println("Attribute Oid: " + attribute.get().getOid());
                System.out.println("Attribute Specification: " + attribute.get().getSpecification());
                System.out.println("Attribute Description: " + attribute.get().getDescription());
                System.out.println("Attribute Rdn: " + attribute.get().getObjectType().getRdn());

            }
            
        });

    }

}