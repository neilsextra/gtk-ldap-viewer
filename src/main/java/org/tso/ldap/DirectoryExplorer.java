package org.tso.ldap;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.message.controls.PagedResults;
import org.apache.directory.api.ldap.model.message.controls.PagedResultsImpl;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.slf4j.LoggerFactory;

public class DirectoryExplorer {

    interface ResultContainer {

        List<String> getResults();

        String getDn();

        String getCursorPosition();
    };

    DirectoryConnection connection;
    final int MAX_RESULTS = 100;

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
        var pageSize = MAX_RESULTS;

        logger.info("Primary Search...");

        if (!this.connection.getLdapConnection().isConnected()) {
            throw new Exception("Connected Disconnected");
        }

        try (EntryCursor cursor = this.connection.getLdapConnection().search(dn, "(objectclass=*)", SearchScope.OBJECT)) {

            for (Entry entry : cursor) {

                entries.add(entry.getDn().toString());
                pageSize = pageSize - 1;

            }

            cursor.close();

        } catch (Exception e) {
            throw e;
        }

        logger.info("Secondary Search...");

        PagedResults pagedControl = new PagedResultsImpl();

        pagedControl.setSize(pageSize);

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

    ResultContainer next(String dn, String cursorPosition) throws Exception {
        var logger = LoggerFactory.getLogger(SchemaExplorer.class);

        if (!this.connection.getLdapConnection().isConnected()) {
            throw new Exception("Connected Disconnected");
        }

        final List<String> entries = new ArrayList<>();
        final StringBuffer nextCursorPosition = new StringBuffer();

        PagedResultsImpl pageControl = new PagedResultsImpl();
        pageControl.setSize(MAX_RESULTS);
        pageControl.setCookie(Base64.getDecoder().decode(cursorPosition));

        SearchRequestImpl searchRequest = new SearchRequestImpl();
        searchRequest.setBase(new Dn(dn));
        searchRequest.setFilter("(objectclass=*)");
        searchRequest.setScope(SearchScope.SUBTREE);
        searchRequest.addControl(pageControl);

        try (SearchCursor cursor = this.connection.getLdapConnection().search(searchRequest)) {

            while (cursor.next()) {
                Entry entry = cursor.getEntry();
                System.out.println(entry.getDn().toString());

                entries.add(entry.getDn().toString());
            }

            if (cursor.getSearchResultDone().getLdapResult().getResultCode() != ResultCodeEnum.SIZE_LIMIT_EXCEEDED) {

                Map<String, Control> controls = cursor.getSearchResultDone().getControls();
                PagedResults responseControl = (PagedResults) controls.get(PagedResults.OID);

                if (responseControl != null) {
                    nextCursorPosition.append(Base64.getEncoder().encodeToString(responseControl.getCookie()));
                    logger.info("Captured Cursor position");
                }

            }

            cursor.close();

        } catch (Exception e) {
           throw e;
        }
        return new ResultContainer() {

            @Override
            public List<String> getResults() {
                return entries;
            }

            @Override
            public String getCursorPosition() {
                return nextCursorPosition.toString();
            }

            @Override
            public String getDn() {
                return dn;
            }

        };

    }

    List<Map<String, String>> retrieve(String dn
    ) throws Exception {

        if (!this.connection.getLdapConnection().isConnected()) {
            throw new Exception("Connected Disconnected");
        }

        List<Map<String, String>> attributes = new ArrayList<>();
        var logger = LoggerFactory.getLogger(DirectoryExplorer.class);

        Map<String, AttributeType> schemaAttributes = connection.getSchemaExplorer().getAttributes();
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

            while ( iterator.hasNext() ) {
                Map<String, String> values = new HashMap<String, String>(properties);
                Value value = iterator.next();

                  if (isHumanReadable(value.getString())) {
                    values.put("type", "String");
                    values.put("value", value.getString());

                } else {
                    values.put("type", "Binary");
                    values.put("value", bytesToHex(value.getBytes()));

                }

                attributes.add(values);

            }
        }

        return attributes;

    }

}
