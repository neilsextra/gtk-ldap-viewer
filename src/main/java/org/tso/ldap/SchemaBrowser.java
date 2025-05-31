package org.tso.ldap;

import java.util.HashMap;
import java.util.Set;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.ldap.model.schema.SchemaObjectWrapper;
import org.apache.directory.ldap.client.api.LdapConnection;


public class SchemaBrowser {
    LdapConnection ldapConnection;
    HashMap<String, AttributeType> types = new HashMap<>();
    HashMap<String, ObjectClass> objects = new HashMap<>();
    HashMap<String, LdapSyntax> definitions = new HashMap<>();

    public SchemaBrowser(LdapConnection ldapConnection) throws Exception {

        this.ldapConnection = ldapConnection;
        
        this.ldapConnection.loadSchemaRelaxed();

        SchemaManager schemaManager =  this.ldapConnection.getSchemaManager();

        if (schemaManager == null) {
           throw new Exception("SchemaManager is NULL");
        }

        schemaManager.loadAllEnabled();

        schemaManager.getAllSchemas().forEach(schema -> {
            System.out.println("Schema: " + schema.toString());

            Set<SchemaObjectWrapper> content =  schema.getContent();

            for (var attribute : content) {
                System.out.println("Attribute: " + attribute.get() +  ":" + (attribute.get().getClass().toString()));

                if (attribute.get() instanceof AttributeType attributeType) {
                   
                    if  (attributeType.getSyntaxName() != null) {
                        types.put(attributeType.getSyntaxName(), attributeType);
                    } 

                }

                if (attribute.get() instanceof ObjectClass objectClass) {
                   
                    objects.put(objectClass.getName(), objectClass); 

                }

                if (attribute.get() instanceof LdapSyntax ldapSyntax) {
                   
                    definitions.put(ldapSyntax.getName(), ldapSyntax); 

                }

            }
            
        });

    }

    public AttributeType getType(String name) {

        return types.get(name);

    }

    public ObjectClass getObject(String name) {

        return objects.get(name);

    }

    public LdapSyntax getDefinition(String name) {

        return definitions.get(name);

    }

}