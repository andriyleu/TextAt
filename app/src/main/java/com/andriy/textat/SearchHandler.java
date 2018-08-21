package com.andriy.textat;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SearchHandler {

    Client client = new Client("KAJVMYN673", "5d42104707798a805f13ff8658a595dc");
    private Index index;

    public SearchHandler() {
        index = client.getIndex("anotaciones");
    }

    public Index getIndex() {
        return index;
    }

    public List<String> queryUserMarks(String username) throws AlgoliaException, JSONException {
        List<String> markIds = new ArrayList<>();
        JSONObject myMarks = index.search(new Query(username), null);
        Iterator<String> keys = myMarks.keys();

        while (keys.hasNext()) {
                String key = keys.next();
                if (myMarks.get(key) instanceof JSONObject) {
                    markIds.add(key);
                }
        }
        return markIds;
    }
}
