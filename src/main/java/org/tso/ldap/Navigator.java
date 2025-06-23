package org.tso.ldap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gnome.gio.ApplicationFlags;
import org.gnome.gio.ListStore;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.AlertDialog;
import org.gnome.gtk.Align;
import org.gnome.gtk.Application;
import org.gnome.gtk.Button;
import org.gnome.gtk.ColumnView;
import org.gnome.gtk.ColumnViewColumn;
import org.gnome.gtk.GtkBuilder;
import org.gnome.gtk.Inscription;
import org.gnome.gtk.Label;
import org.gnome.gtk.ListItem;
import org.gnome.gtk.ListView;
import org.gnome.gtk.ProgressBar;
import org.gnome.gtk.SearchEntry;
import org.gnome.gtk.SelectionModel;
import org.gnome.gtk.SignalListItemFactory;
import org.gnome.gtk.SingleSelection;
import org.gnome.gtk.TextBuffer;
import org.gnome.gtk.TextIter;
import org.gnome.gtk.TextView;
import org.gnome.gtk.Window;
import org.tso.ldap.DirectoryExplorer.ResultContainer;
import org.tso.ldap.util.GuiUtils;
import org.tso.ldap.util.ThreadMonitor;

import io.github.jwharm.javagi.gio.ListIndexModel;
import io.github.jwharm.javagi.gobject.types.Types;

public class Navigator {

    class SearchResult {

        @FunctionalInterface
        interface SearchResultCallback {

            void onCompletion(List<String> result);

        }

        Window window;
        List<String> results = new ArrayList<>();
        DirectoryConnection connection;
        String base;

        SearchResult(Window window, DirectoryConnection connection, String base) {
            this.window = window;
            this.connection = connection;
            this.base = base;
        }

        void process(final SearchResultCallback searchCallback) {
            ThreadMonitor monitor = new ThreadMonitor(() -> {
                try {
                    ResultContainer result = this.connection.getDirectoryExplorer().search(base);

                    results.addAll(result.getResults());
                    searchDn.setText(result.getDn());

                    Navigator.this.redoSearch.setSensitive(true);
                    Navigator.this.expandEntry.setSensitive(!result.getResults().isEmpty());

                    if (result.getCursorPosition().length() > 0) {
                        System.out.println("Cursor Postion: " + result.getCursorPosition());

                        Navigator.this.currentCursor = result.getCursorPosition();
                        Navigator.this.nextPage.setSensitive(true);

                    } else {
                        Navigator.this.nextPage.setSensitive(false);
                    }

                } catch (Exception e) {

                    AlertDialog.builder()
                            .setModal(true)
                            .setMessage("Search")
                            .setDetail(e.getMessage())
                            .build()
                            .show(this.window);
                }

            }, progressBar);

            monitor.process(() -> {
                searchCallback.onCompletion(results);
            });

        }

    }

    class NextResult {

        @FunctionalInterface
        interface SearchResultCallback {

            void onCompletion(List<String> result);

        }

        private Window window;
        private List<String> results = new ArrayList<>();
        private DirectoryConnection connection;
        private String dn;
        private String cursorPosition;

        NextResult(Window window, DirectoryConnection connection, String dn, String cursorPosition) {
            this.window = window;
            this.connection = connection;
            this.dn = dn;
            this.cursorPosition = cursorPosition;
        }

        void process(final SearchResultCallback searchCallback) {
            ThreadMonitor monitor = new ThreadMonitor(() -> {
                try {

                    ResultContainer result = this.connection.getDirectoryExplorer().next(dn, cursorPosition);

                    results.addAll(result.getResults());
                    searchDn.setText(result.getDn());

                    Navigator.this.redoSearch.setSensitive(true);
                    Navigator.this.expandEntry.setSensitive(!result.getResults().isEmpty());

                    if (result.getCursorPosition().length() > 0) {
                        System.out.println("Cursor Postion: " + result.getCursorPosition());

                        Navigator.this.currentCursor = result.getCursorPosition();
                        Navigator.this.nextPage.setSensitive(true);

                    } else {
                        Navigator.this.nextPage.setSensitive(false);
                    }

                } catch (Exception e) {
                    System.out.println("Alert");

                    AlertDialog.builder()
                            .setModal(true)
                            .setMessage("Search")
                            .setDetail(e.getMessage())
                            .build()
                            .show(this.window);
                }

            }, progressBar);

            monitor.process(() -> {
                searchCallback.onCompletion(results);
            });

        }

    }

    class RetrieveResult {

        @FunctionalInterface
        interface RetrieveResultCallback {

            void onCompletion(List<Map<String, String>> results);

        }
        Window window;
        List<String> results = null;
        DirectoryConnection connection;
        List<Map<String, String>> attributes;

        RetrieveResult(Window window, DirectoryConnection connection) {
            this.window = window;
            this.connection = connection;
        }

        void process(final RetrieveResultCallback retrieveCallback, String dn) {
            ThreadMonitor monitor = new ThreadMonitor(() -> {
                try {

                    RetrieveResult.this.attributes = this.connection.getDirectoryExplorer().retrieve(dn);

                } catch (Exception e) {
                    AlertDialog.builder()
                            .setModal(true)
                            .setMessage("Connection")
                            .setDetail(connection.getConnectionException().getMessage())
                            .build()
                            .show(this.window);
                }

            }, progressBar);

            monitor.process(() -> {
                retrieveCallback.onCompletion(RetrieveResult.this.attributes);
            });

        }

    }

    Window mainWindow;
    TextView attributeViewer;
    ProgressBar progressBar;
    ConnectionDialog connectionDialog;
    AboutDialog aboutDialog;
    ListStore<Row> store;
    ListView listView;
    ColumnView columnView;
    SearchEntry searchEntry;
    DirectoryConnection connection = null;
    List<String> entries = new ArrayList<>();
    ListIndexModel listIndexModel;
    Label searchDn;
    String currentCursor;
    Button redoSearch;
    Button nextPage;
    Button expandEntry;

    public static final class Row extends GObject {

        public static Type gtype = Types.register(Row.class);
        public String name;
        public String oid;
        public String syntax;
        public String primitiveType;
        public String value;

        public Row(String name, String oid, String syntax, String primitiveType, String value) {

            this.name = name;
            this.oid = oid;
            this.syntax = syntax;
            this.primitiveType = primitiveType;
            this.value = value;

        }

        public String getName() {
            return this.name;
        }

        public String getOid() {
            return this.oid;
        }

        public String getSyntax() {
            return this.syntax;
        }

        public String getPrimitiveType() {
            return this.primitiveType;
        }

        public String getValue() {
            return this.value;
        }

    }

    void reset() {

        entries.clear();
        Navigator.this.store.clear();
        GuiUtils.clearTextView(attributeViewer);
        Navigator.this.redoSearch.setSensitive(false);
        Navigator.this.expandEntry.setSensitive(false);
        Navigator.this.searchDn.setText("");

        listIndexModel.setSize(entries.size());
    }

    void buildRows(String dn) {

        new RetrieveResult(mainWindow, connection).process((attributes)
                -> {

            GuiUtils.clearTextView(attributeViewer);
            Navigator.this.store.clear();

            for (var attribute : attributes) {

                Row row = new Row(attribute.get("name"),
                        attribute.get("oid"),
                        attribute.get("syntaxOid"),
                        attribute.get("type"),
                        attribute.get("value"));

                Navigator.this.store.add(0, row);

            }

            progressBar.setVisible(false);

            Navigator.this.selectRow(0);

        }, dn);

    }

    void selectRow(int selected) {
        Row row = store.get(selected);

        if (row != null) {
            attributeViewer.setMonospace(true);
            TextBuffer buffer = new TextBuffer();
            TextIter iter = new TextIter();
            buffer.getStartIter(iter);

            String value = row.getPrimitiveType().equals("Binary") ? GuiUtils.formatHex(row.getValue()) : row.getValue();

            String description = "<span weight=\"ultraheavy\" size=\"x-large\">Definition</span>" + "\n"
                    + "<b>Class:</b>" + row.getName() + "\n"
                    + "<b>OID:</b>" + row.getOid() + "\n"
                    + "<b>Syntax:</b>" + row.getSyntax() + "\n\n"
                    + "<span weight=\"ultraheavy\"  size=\"x-large\">Value</span>" + "\n"
                    + value;

            buffer.insertMarkup(iter, description, -1);
            attributeViewer.setBuffer(buffer);

        } else {
            Navigator.this.connection = null;
            Navigator.this.searchEntry.setEditable(false);
            reset();

            AlertDialog.builder()
                    .setModal(true)
                    .setMessage("Connection")
                    .setDetail("Connection has been reset")
                    .build()
                    .show(mainWindow);
        }

    }

    void setupColumns(ColumnView columnview) {
        var columnFactoryName = GuiUtils.createSignalListItemFactory();

        columnFactoryName.onBind(item -> {
            var listitem = (ListItem) item;
            var inscription = (Inscription) listitem.getChild();
            var row = (Row) listitem.getItem();

            inscription.setText(row.getName());

        });

        var columnFactoryOid = GuiUtils.createSignalListItemFactory();

        columnFactoryOid.onBind(item -> {
            var listitem = (ListItem) item;
            var inscription = (Inscription) listitem.getChild();
            var row = (Row) listitem.getItem();

            inscription.setText(row.getOid());

        });

        var columnFactorySyntax = GuiUtils.createSignalListItemFactory();

        columnFactorySyntax.onBind(item -> {
            var listitem = (ListItem) item;
            var inscription = (Inscription) listitem.getChild();

            var row = (Row) listitem.getItem();

            if (inscription != null) {
                inscription.setText(row.getSyntax());
            }

        });

        var columnFactorType = GuiUtils.createSignalListItemFactory();

        columnFactorType.onBind(item -> {
            var listitem = (ListItem) item;
            var inscription = (Inscription) listitem.getChild();
            var row = (Row) listitem.getItem();
            inscription.setText(row.getPrimitiveType());

        });

        var columnFactorValue = GuiUtils.createSignalListItemFactory();

        columnFactorValue.onBind(item -> {
            var listitem = (ListItem) item;
            var inscription = (Inscription) listitem.getChild();
            var row = (Row) listitem.getItem();
            inscription.setText(row.getValue());

        });

        var columnName = new ColumnViewColumn("Name", columnFactoryName);
        var columnOid = new ColumnViewColumn("OID", columnFactoryOid);
        var columnSyntax = new ColumnViewColumn("Syntax", columnFactorySyntax);
        var columnType = new ColumnViewColumn("Type", columnFactorType);
        var columnValue = new ColumnViewColumn("Value", columnFactorValue);

        columnName.setFixedWidth(250);
        columnName.setResizable(true);

        columnOid.setFixedWidth(150);
        columnOid.setResizable(true);

        columnSyntax.setFixedWidth(150);
        columnSyntax.setResizable(true);

        columnType.setFixedWidth(50);
        columnType.setResizable(true);

        columnValue.setExpand(true);
        columnValue.setResizable(true);

        columnview.appendColumn(columnName);
        columnview.appendColumn(columnOid);
        columnview.appendColumn(columnSyntax);
        columnview.appendColumn(columnType);
        columnview.appendColumn(columnValue);

    }

    void setupList(ListView listView) {
        SignalListItemFactory factory = new SignalListItemFactory();

        factory.onSetup(object -> {
            ListItem listitem = (ListItem) object;
            Label label = new Label("");

            label.setHalign(Align.START);
            listitem.setChild(label);

        });

        factory.onBind(object -> {
            ListItem listitem = (ListItem) object;
            Label label = (Label) listitem.getChild();
            ListIndexModel.ListIndex item = (ListIndexModel.ListIndex) listitem.getItem();

            if (label == null || item == null) {
                return;
            }

            int index = item.getIndex();

            String entry = entries.get(index);

            label.setLabel(entry);

        });

        listIndexModel = new ListIndexModel(entries.size());
        listView.setModel(new SingleSelection<>(listIndexModel));
        listView.setFactory(factory);

        ((SingleSelection<?>) (listView.getModel())).onSelectionChanged((int position, int nItems) -> {

            int selected = ((SingleSelection<?>) (listView.getModel())).getSelected();

            String entry = entries.get(selected);

            Navigator.this.buildRows(entry);

        });

    }

    void open() {
        connectionDialog.show();
    }

    void about() {
        aboutDialog.show();
    }

    void search() {
        entries.clear();
        listIndexModel.setSize(0);

        new SearchResult(mainWindow, connection, searchEntry.getText()).process(
                (results) -> {
                    Navigator.this.entries = results;
                    Navigator.this.store.removeAll();

                    listIndexModel.setSize(Navigator.this.entries.size());
                    progressBar.setVisible(false);

                    Navigator.this.buildRows(entries.get(0));

                }
        );

    }

    void redo() {
        entries.clear();
        listIndexModel.setSize(0);

        new SearchResult(mainWindow, connection, searchDn.getText()).process(
                (results) -> {
                    Navigator.this.entries = results;
                    Navigator.this.store.removeAll();

                    listIndexModel.setSize(Navigator.this.entries.size());
                    progressBar.setVisible(false);

                    Navigator.this.buildRows(entries.get(0));

                }
        );

    }

    void next() {
        entries.clear();
        listIndexModel.setSize(0);

        new NextResult(mainWindow, connection, searchDn.getText(), currentCursor).process(
                (results) -> {
                    Navigator.this.entries = results;
                    Navigator.this.store.removeAll();

                    listIndexModel.setSize(Navigator.this.entries.size());
                    progressBar.setVisible(false);

                    Navigator.this.buildRows(entries.get(0));

                }
        );

    }

    void expand() {
        int selected = ((SingleSelection<?>) (listView.getModel())).getSelected();
        String entry = entries.get(selected);

        entries.clear();
        listIndexModel.setSize(0);

        new SearchResult(mainWindow, connection, entry).process(
                (results) -> {
                    Navigator.this.entries = results;
                    Navigator.this.store.removeAll();

                    listIndexModel.setSize(Navigator.this.entries.size());
                    progressBar.setVisible(false);

                    Navigator.this.buildRows(entries.get(0));

                }
        );

    }

    public void activate(Application app) {
        GtkBuilder builder = new GtkBuilder();

        try {
            var uiDefinition = GuiUtils.getDefintion("/org/tso/ldap/navigator.ui");

            builder.addFromString(uiDefinition, uiDefinition.length());

            mainWindow = (Window) builder.getObject("main");
            progressBar = (ProgressBar) builder.getObject("progressBar");
            attributeViewer = (TextView) builder.getObject("attributeViewer");
            searchDn = (Label) builder.getObject("searchDn");
            redoSearch = (Button) builder.getObject("redoSearch");
            nextPage = (Button) builder.getObject("nextPage");
            expandEntry = (Button) builder.getObject("expandEntry");

            var openToolbarButton = (Button) builder.getObject("openToolbarButton");
            var aboutToolbarItem = (Button) builder.getObject("aboutToolbarItem");

            openToolbarButton.onClicked(this::open);
            aboutToolbarItem.onClicked(this::about);

            connectionDialog = new ConnectionDialog(mainWindow, "/org/tso/ldap/open-dialog.ui",
                    directoryConnection -> {

                        Navigator.this.connection = directoryConnection;
                        Navigator.this.searchEntry.setEditable(true);

                        Navigator.this.reset();

                    }
            );

            aboutDialog = new AboutDialog(mainWindow, "/org/tso/ldap/about-dialog.ui");

            columnView = (ColumnView) builder.getObject("attributesViewer");

            columnView.setShowColumnSeparators(true);

            store = new ListStore<>(Row.gtype);
            setupColumns(columnView);

            columnView.setModel(new SingleSelection<Row>(store));

            ((SingleSelection<?>) (columnView.getModel())).onSelectionChanged(new SelectionModel.SelectionChangedCallback() {
                @Override
                public void run(int position, int nItems) {

                    Navigator.this.selectRow(((SingleSelection<?>) (columnView.getModel())).getSelected());
                }

            });

            listView = (ListView) builder.getObject("selectionView");

            setupList(listView);

            searchEntry = (SearchEntry) builder.getObject("search");

            searchEntry.onActivate(this::search);
            redoSearch.onClicked(this::redo);
            nextPage.onClicked(this::next);
            expandEntry.onClicked(this::expand);

            mainWindow.setApplication(app);

            mainWindow.setVisible(true);

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    public Navigator(String[] args) {
        Application app = new Application("org.tso.ldap.Navigator", ApplicationFlags.DEFAULT_FLAGS);

        app.onActivate(() -> activate(app));
        app.run(args);

    }

    public static void main(String[] args) {
        new Navigator(args);
    }

}
