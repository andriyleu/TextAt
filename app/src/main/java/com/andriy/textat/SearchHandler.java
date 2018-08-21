package com.andriy.textat;

import android.content.Intent;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;

import org.json.JSONArray;
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

    public void addMark(Mark m) throws JSONException {
        index.addObjectAsync(new JSONObject().put("objectID", m.getId())
                .put("description", m.getDescription())
                .put("latitude", m.getLocation().getLatitude())
                .put("longitude", m.getLocation().getLongitude())
                .put("privacy", m.getPrivacy())
                .put("rating", m.getRating())
                .put("uri", m.getUri())
                .put("user", m.getUser())
                .put("date", m.getTimestamp().getSeconds())
                .put("visibility", m.getVisibility())
                .put("hasImages", m.isHasImages())
                , null);
    }

    public void removeMark(Mark m) {
        index.deleteObjectAsync(m.getId(), null);
    }
}
