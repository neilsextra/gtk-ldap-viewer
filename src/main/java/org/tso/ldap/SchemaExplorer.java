package org.tso.ldap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.SchemaObjectWrapper;
import org.apache.directory.api.ldap.model.schema.registries.Schema;
import org.apache.directory.ldap.client.api.DefaultSchemaLoader;
import org.slf4j.LoggerFactory;

public class SchemaExplorer {

    DirectoryConnection connection;
    Map<String, AttributeType> attributes;

    public SchemaExplorer(DirectoryConnection connection) throws Exception {

        this.connection = connection;

        this.load();

    }

    void load() {
        var logger = LoggerFactory.getLogger(SchemaExplorer.class);

        this.attributes = new HashMap<String, AttributeType>();

        try {
            DefaultSchemaLoader schemaLoader = new DefaultSchemaLoader(this.connection.getLdapConnection(), true);

            Collection<Schema> schemas = schemaLoader.getAllSchemas();

            for (Schema schema : schemas) {

                logger.info("Schema: '" + schema.getSchemaName() + "' - loaded");

                Set<SchemaObjectWrapper> content = schema.getContent();

                for (var attribute : content) {

                    if (attribute.get() instanceof AttributeType attributeType) {

                        this.attributes.put(attributeType.getName().toLowerCase(), attributeType);

                    }

                }

            }

        } catch (Exception e) {
            logger.error("Schema Loader Error", e);
        }

    }

    public Map<String, AttributeType> getAttributes() {

        return this.attributes;
    }

}
