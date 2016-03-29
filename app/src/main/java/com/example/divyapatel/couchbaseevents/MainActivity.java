package com.example.divyapatel.couchbaseevents;

import android.app.DownloadManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.Reducer;
import com.couchbase.lite.View;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.android.AndroidContext;

import com.couchbase.lite.Document;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    final String TAG = "CouchbaseEvents";
    public static final String DB_NAME = "couchbaseevents";
    Manager manager = null;
    Database database = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e(TAG, "Begin Couchbase Events App");
        Log.e(TAG, "End Couchbase Events App");
        try {
            helloCBL();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    private void helloCBL() throws CouchbaseLiteException {
        Manager manager = null;
        Database database = null;
        try {
            manager = new Manager(new AndroidContext(this), Manager.DEFAULT_OPTIONS);
            database = manager.getDatabase(DB_NAME);
        } catch (Exception e) {
            Log.e(TAG, "error");
        }
        String documentId = createDocument(database);
    /* Get and output the contents */
        outputContents(database, documentId);
    /* Update the document and add an attachment */
        updateDoc(database, documentId);
        // Add an attachment
        addAttachment(database, documentId);
    /* Get and output the contents with the attachment */
        outputContentsWithAttachment(database, documentId);

        View view = database.getView("name");
        view.setMapReduce(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                if ("Divya".equals(document.get("name"))) {
                    emitter.emit(document.get("address"), document.get("location"));

                }
            }
        }, new Reducer() {
            @Override
            public Object reduce(List<Object> keys, List<Object> values, boolean rereduce) {
                Log.e(TAG, "reduced size:" + values.size());
                return new Integer(values.size());
            }
        }, "2");
        Query query = database.getView("name").createQuery();
        query.setMapOnly(true);
        QueryEnumerator result = query.run();
        for (Iterator< QueryRow> it = result; it.hasNext();){
            QueryRow row = it.next();
            String address = (String) row.getValue();
            Log.e(TAG,address);
        }


    }

    private void outputContentsWithAttachment(Database database, String documentId) {

    }

    private void addAttachment(Database database, String documentId) {
        Document document = database.getDocument(documentId);
        try {
        /* Add an attachment with sample data as POC */
            ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[] { 0, 0, 0, 0 });
            UnsavedRevision revision = document.getCurrentRevision().createRevision();
            revision.setAttachment("binaryData", "application/octet-stream",inputStream);
        /* Save doc & attachment to the local DB */
             revision.save();
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Error putting", e);
        }
    }

    private void updateDoc(Database database, String documentId) {
        Document document = database.getDocument(documentId);
        try {
            // Update the document with more data
            Map<String, Object> updatedProperties = new HashMap<String, Object>();
            updatedProperties.putAll(document.getProperties());
            updatedProperties.put("eventDescription", "Everyone is invited!");
            updatedProperties.put("address", "123 Elm St.");
            // Save to the Couchbase local Couchbase Lite DB
            document.putProperties(updatedProperties);
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Error putting", e);
        }
    }

    private void outputContents(Database database, String documentId) {

        Document retrievedDocument = database.getDocument(documentId);
        Log.e(TAG, "retrievedDocument=" + String.valueOf(retrievedDocument.getProperties()));
    }

    private String createDocument(Database database) {
        Document document = database.createDocument();
        String documentID = document.getId();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name","Divya");
        map.put("location", "My House");
        try{
            document.putProperties(map);
        }
        catch (CouchbaseLiteException e){
            Log.e(TAG,"error");
        }

        return documentID;
    }

    public Database getDatabseInstance() throws CouchbaseLiteException {
        if ((this.database == null) & (this.manager == null)) {
            this.database = manager.getDatabase(DB_NAME);
        }
        return database;
    }

}



