package de.tum.os.views;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.showcasedemo.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.theengine.android.simple_rss2_android.RSSItem;
import at.theengine.android.simple_rss2_android.SimpleRss2Parser;
import at.theengine.android.simple_rss2_android.SimpleRss2ParserCallback;

/**
 * A custom class for displaying RSS Feeds.
 * One item at a time.
 * <p/>
 * Created by Marius on 7/19/13.
 */
public class FeedViewer extends ListView {

    String feedUrl;
    private final String feedItemTitle = "feedItemTitle";
    private final String feedItemContent = "feedItemContent";

    Context context;

    public FeedViewer(Context context, AttributeSet attrs, String feedUrl) throws Exception {
        super(context, attrs);
    }

    public FeedViewer(Context context, String feedUrl) throws Exception {
        super(context);

        this.feedUrl = feedUrl;
        this.context = context;

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT);
        this.setLayoutParams(lp);
        this.setDivider((new ColorDrawable()));
        this.setDividerHeight(25);
        this.setVerticalFadingEdgeEnabled(true);
        this.setFadingEdgeLength(30);

        fetchFeed();

    }

    private void fetchFeed() throws Exception {
        if (this.feedUrl == null || this.feedUrl.isEmpty()) {
            throw new Exception("Feed URL cannot be null");
        }

        SimpleRss2Parser rssParser = new SimpleRss2Parser(feedUrl, new SimpleRss2ParserCallback() {
            @Override
            public void onFeedParsed(List<RSSItem> rssItems) {
                displayFeedItems(rssItems);
            }

            @Override
            public void onError(Exception e) {
                int t = 0;
            }
        });
        rssParser.parseAsync();
    }

    private void displayFeedItems(List<RSSItem> rssItems) {
        ArrayList<HashMap<String, Object>> data = getDisplayableData(rssItems);

        SimpleAdapter feedAdapter = new SimpleAdapter(context, data, R.layout.feed_item_template_layout,
                new String[]{feedItemTitle, feedItemContent}, new int[]{R.id.feedItemTitleTV, R.id.feedItemContentTV});
        this.setAdapter(feedAdapter);
    }


    private ArrayList<HashMap<String, Object>> getDisplayableData(List<RSSItem> rssItems) {
        if (rssItems == null || rssItems.size() < 1) {
            return null;
        }
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

        for (RSSItem item : rssItems) {
            HashMap<String, Object> currentItemMap = new HashMap<String, Object>();
            currentItemMap.put(feedItemTitle, item.getTitle());
            currentItemMap.put(feedItemContent, Html.fromHtml(item.getContent()));
            data.add(currentItemMap);
        }

        return data;
    }
}
