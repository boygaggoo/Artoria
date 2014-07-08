package be.artoria.belfortapp.mixare.data.convert;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import be.artoria.belfortapp.app.POI;
import be.artoria.belfortapp.mixare.ArtoriaPOIMarker;
import be.artoria.belfortapp.mixare.MixView;
import be.artoria.belfortapp.mixare.POIMarker;
import be.artoria.belfortapp.mixare.lib.HtmlUnescape;
import be.artoria.belfortapp.mixare.lib.marker.Marker;

/**
 * Created by Laurens on 08/07/2014.
 */
public class ArtoriaDataProcessor implements DataProcessor {
    @Override
    public String[] getUrlMatch() {
        return new String[0];
    }

    @Override
    public String[] getDataMatch() {
        return new String[0];
    }

    @Override
    public boolean matchesRequiredType(String type) {
        return false;
    }

    @Override
    public List<Marker> load(List<POI> rawData, int taskId, int colour){
        List<Marker> markers = new ArrayList<Marker>();
        for (POI poi : rawData) {
            Marker ma = new ArtoriaPOIMarker(poi,taskId, colour);
            markers.add(ma);
        }
        return markers;
    }
}
