package org.tso.ldap;

import java.util.Map;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.message.controls.PagedResults;
import org.apache.directory.api.ldap.model.message.controls.PagedResultsImpl;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.ldap.client.api.EntryCursorImpl;
import org.slf4j.LoggerFactory;

public class DirectoryExplorer {

    interface ResultContainer {

        List<String> getResults();

        String getDn();

        String getCursorPosition();
    };

    DirectoryConnection connection;

    public DirectoryExplorer(DirectoryConnection connection) {

        this.connection = connection;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();

    }

    boolean isHumanReadable(String value) {
        Pattern pattern = Pattern.compile("[^\\p{ASCII}]", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(value);
        boolean matchFound = matcher.find();

        return (!matchFound);

    }

    ResultContainer search(final String dn) throws Exception {
        var logger = LoggerFactory.getLogger(SchemaExplorer.class);
        List<String> entries = new ArrayList<>();
        final StringBuffer cursorPosition = new StringBuffer();
       
        logger.info("Primary Search...");

        try (EntryCursor cursor = this.connection.getLdapConnection().search(dn, "(objectclass=*)", SearchScope.OBJECT)) {

            for (Entry entry : cursor) {

                entries.add(entry.getDn().toString());

            }

            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        logger.info("Secondary Search...");

        PagedResults pagedControl = new PagedResultsImpl();

        pagedControl.setSize(4);

        SearchRequest searchRequest = new SearchRequestImpl();
        searchRequest.setBase(new Dn(dn));
        searchRequest.setTimeLimit(10000);
        searchRequest.setFilter("(objectclass=*)");
        searchRequest.setScope(SearchScope.ONELEVEL);
        searchRequest.addControl(pagedControl);

        try (SearchCursor cursor = this.connection.getLdapConnection().search(searchRequest)) {
            logger.info("Secondary Search Complete");

            while (cursor.next()) {
                Entry entry = cursor.getEntry();
                System.out.println(entry.getDn().toString());

                entries.add(entry.getDn().toString());

            }

            logger.info("Capturing Cursor position");

            if (cursor.getSearchResultDone().getLdapResult().getResultCode() != ResultCodeEnum.SIZE_LIMIT_EXCEEDED) {

                Map<String, Control> controls = cursor.getSearchResultDone().getControls();
                PagedResults responseControl = (PagedResults) controls.get(PagedResults.OID);

                if (responseControl != null) {
                    cursorPosition.append(Base64.getEncoder().encodeToString(responseControl.getCookie()));
                      logger.info("Captured Cursor position");
                }

            }

            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        logger.info("Search Completed - Returning Results...");
        
        return new ResultContainer() {

            @Override
            public List<String> getResults() {
                return entries;
            }

            @Override
            public String getCursorPosition() {
                return cursorPosition.toString();
            }

            @Override
            public String getDn() {
                return dn;
            }

        };

    }

    List<String> next(String dn, String cursorPosition) throws Exception {
        List<String> entries = new ArrayList<>();

        PagedResultsImpl pageControl = new PagedResultsImpl();
        pageControl.setSize(4);
        pageControl.setCookie(Base64.getDecoder().decode(cursorPosition));

        SearchRequestImpl searchRequest = new SearchRequestImpl();
        searchRequest.setBase(new Dn(dn));
        searchRequest.setFilter("(objectclass=*)");
        searchRequest.setScope(SearchScope.SUBTREE);
        searchRequest.addControl(pageControl);

        EntryCursorImpl cursor = new EntryCursorImpl(this.connection.getLdapConnection().search(searchRequest));

        while (cursor.next()) {
            System.out.println("Second Pass User: " + cursor.get().getDn());
        }

        cursor.close();

        return entries;

    }

    List<Map<String, String>> retrieve(String dn) {
        List<Map<String, String>> attributes = new ArrayList<Map<String, String>>();
        var logger = LoggerFactory.getLogger(DirectoryExplorer.class);

        Map<String, AttributeType> schemaAttributes = connection.getSchemaExplorer().getAttributes();

        try {
            Entry entry = this.connection.getLdapConnection().lookup(dn);

            if (entry == null) {
                logger.info("Entry is NULL");

                return attributes;
            }

            for (Attribute attribute : entry.getAttributes()) {
                Map<String, String> properties = new HashMap<>();

                String oid = schemaAttributes.containsKey(attribute.getId()) ? schemaAttributes.get(attribute.getId()).getOid() : " ";
                String syntaxOid = schemaAttributes.containsKey(attribute.getId()) ? schemaAttributes.get(attribute.getId()).getSyntaxOid() : " ";

                properties.put("name", attribute.getUpId());
                properties.put("oid", oid == null ? "" : oid);
                properties.put("syntaxOid", syntaxOid == null ? "" : syntaxOid);
                properties.put("type", attribute.get().isHumanReadable() ? "String" : "Binary");

                if (isHumanReadable(attribute.get().getString())) {
                    properties.put("type", "String");
                    properties.put("value", attribute.get().getString());

                } else {
                    properties.put("type", "Binary");
                    properties.put("value", bytesToHex(attribute.get().getBytes()));

                }

                attributes.add(properties);

            }

            return attributes;

        } catch (Exception e) {
            logger.error("Search Error", e);
            return attributes;
        }

    }

}
