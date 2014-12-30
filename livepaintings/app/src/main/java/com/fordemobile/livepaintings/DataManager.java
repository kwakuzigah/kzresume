package com.fordemobile.livepaintings;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by dai on 14-12-26.
 */
public class DataManager  {

    private List<Painting> paintings;
    private static DataManager INSTANCE;
    private Context context;
    private MixpanelAPI mixpanel;

    public List<Painting> getPaintings() {
        return paintings;
    }

    private DataManager() {

        this.paintings = new ArrayList<Painting>();
    }

    public static DataManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DataManager();
        }
        return INSTANCE;
    }

    public void track(String event) {
        track(event, new JSONObject());
    }

    public void track(String event, JSONObject props) {

        try {
            props.put("Version", "Pro");
            mixpanel.track(event, props);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public MixpanelAPI getMixpanelAPI() {
        return this.mixpanel;
    }

    public void initialize(Context context) {
        this.context = context;
        this.mixpanel =
                MixpanelAPI.getInstance(context, BuildConfig.BUILD_TYPE == "debug" ?  "6a3f7006093936564b200dbb9995800e"
                        : "51202232becedf41195dc3badc084a86");

        try {
            Resources resources = context.getResources();
            AssetManager assets = resources.getAssets();
            String lists[] = assets.list("paintings");
            for (String paint : lists) {
                Painting painting = new Painting(
                        "paintings/" + paint + "/", 0, 0);
                this.paintings.add(painting);
            }
            Collections.sort(this.paintings, new Comparator<Painting>() {
                public int compare(Painting lhs, Painting rhs) {
                    return lhs.getDate().compareTo(rhs.getDate());
                }

                ;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
