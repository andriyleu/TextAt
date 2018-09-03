package com.andriy.textat;

import com.algolia.instantsearch.helpers.Searcher;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;

import org.json.JSONException;
import org.json.JSONObject;

public class SearchHandler {

    private Client client;
    private Index index;
    private Searcher searcher;


    public SearchHandler() {
        client = new Client("KAJVMYN673", "5d42104707798a805f13ff8658a595dc");
        index = client.getIndex("anotaciones");
        searcher = Searcher.create("KAJVMYN673", "5d42104707798a805f13ff8658a595dc", "anotaciones");
        getSearcher().setQuery(new Query().setRestrictSearchableAttributes("description", "title"));

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
                        .put("markTitle", m.getMarkTitle())
                , null);
    }

    public void removeMark(Mark m) {
        index.deleteObjectAsync(m.getId(), null);
    }

    // Returns everything with user at user field in Algolia
    public Query getUserMarks(String user, boolean accountOwner) {
        Query q = new Query(user);
        q.setRestrictSearchableAttributes("user");

        if (!accountOwner) {
            q.setFilters("privacy: 0");
        }

        return q;
    }

    // Get user mentions
    public Query getUserMentions(String user) {
        Query q = new Query(user);
        q.setRestrictSearchableAttributes("description");
        q.setFilters("privacy: 0");
        return q;
    }

    // Get hashtags
    public Query getHashtag(String hashtag) {
        Query q = new Query(hashtag);
        q.setRestrictSearchableAttributes("description");
        q.setFilters("privacy: 0");

        return q;
    }

    public Query searchInText(String text) {
        Query q = new Query(text);
        q.setFilters("privacy: 0");

        return q;

    }

    public Searcher getSearcher() {
        return searcher;
    }
}
