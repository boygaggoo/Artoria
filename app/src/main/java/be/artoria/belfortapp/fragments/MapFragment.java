package be.artoria.belfortapp.fragments;



import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

import be.artoria.belfortapp.R;
import be.artoria.belfortapp.activities.MapActivity;
import be.artoria.belfortapp.activities.MonumentDetailActivity;
import be.artoria.belfortapp.app.ArtoriaOverlayItem;
import be.artoria.belfortapp.app.DataManager;
import be.artoria.belfortapp.app.adapters.DescriptionRow;
import be.artoria.belfortapp.app.ManeuverType;
import be.artoria.belfortapp.app.POI;
import be.artoria.belfortapp.app.adapters.RouteDescAdapter;
import be.artoria.belfortapp.app.RouteManager;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class MapFragment extends android.support.v4.app.Fragment {
    public static final int DEFAULT_ZOOM = 17;
    private String MAP_QUEST_API_KEY; //TODO request key for Artoria, this key now is 'licensed' to Dieter Beelaert
    public static final String LANG_ENG = "en_GB";
    public static final String LANG_NL = "nl_BE";
    public static final String LANG_FR = "fr_FR";
    private MapView mapView;
    private boolean showsMap = true;
    private boolean firstStart = true; //to check when the mapview is loaded for the first time (this is for the ViewTreeObserver)
    public boolean isFullscreen = true;

    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        return fragment;
    }

    public MapFragment(){
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater,container,savedInstanceState);
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView)getView().findViewById(R.id.mapview);
        MAP_QUEST_API_KEY = getResources().getString(R.string.map_quest_api_key);
        initGUI();
    }

    private void initGUI(){
        /*Setup map and draw route*/
        mapView.setTileSource(TileSourceFactory.MAPQUESTOSM);
        mapView.setBuiltInZoomControls(true);
        final MapController mapCtrl = (MapController) mapView.getController();
        mapCtrl.setZoom(DEFAULT_ZOOM);
        calculateRoute();
        /*Initially show map*/
        toggleMap(true);

        final Button btnTogglemap = (Button)getView().findViewById(R.id.btnRouteDesc);
        btnTogglemap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showsMap = !showsMap;
                toggleMap(showsMap);
                if(showsMap){
                    btnTogglemap.setText(getResources().getString(R.string.route_desc));
                }else{
                    btnTogglemap.setText(getResources().getString(R.string.show_map));
                }
            }
        });

        final ImageButton btnFullScreen = (ImageButton)getView().findViewById(R.id.btnFullScreen);
        btnFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(),MapActivity.class);
                getActivity().startActivity(i);
            }
        });

        if(!isFullscreen){
            btnTogglemap.setVisibility(View.GONE);
        }else{
            btnFullScreen.setVisibility(View.GONE);
        }

        /*Set center of map to current location or Belfry*/
        final ViewTreeObserver vto = mapView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(firstStart){
                    mapView.getController().setCenter(getCurrentLocation());
                    firstStart = false;
                }

            }
        });

        drawMarkers();
    }

    private OverlayItem getOverlayItemFromPOI(POI poi,Drawable icon){
        final ArtoriaOverlayItem overlayItem = new ArtoriaOverlayItem(poi);
        if(icon != null) {
            overlayItem.setMarker(icon);
        }
        return overlayItem;
    }


    /* Transfer the route to an ArrayList of GeoPoint objects */
    private ArrayList<GeoPoint> getGeoPointsFromRoute(){
        final ArrayList<GeoPoint>toReturn = new ArrayList<GeoPoint>();
        toReturn.add(getCurrentLocation());
        for(final POI poi : RouteManager.getInstance().getWaypoints()){
            toReturn.add(new GeoPoint(Double.parseDouble(poi.lat),Double.parseDouble(poi.lon)));
        }
        return toReturn;
    }

    /*Gets the route from the web and draws it on the map when done*/
    private class RouteCalcTask extends AsyncTask {

        @Override
        protected Road doInBackground(Object[] objects) {
            final RoadManager roadManager = new MapQuestRoadManager(MAP_QUEST_API_KEY);
            //RoadManager roadManager = new OSRMRoadManager();
            roadManager.addRequestOption("routeType=pedestrian");
            final String lang;
            switch(DataManager.getInstance().getCurrentLanguage()){
                case ENGLISH:lang = LANG_ENG; break;
                case FRENCH: lang = LANG_FR;  break;
                case DUTCH:
                default:     lang = LANG_NL;
            }
            roadManager.addRequestOption("locale="+lang ); //display the directions in the selected language
            roadManager.addRequestOption("unit=k"); //display the distance in kilometers
            final Road road = roadManager.getRoad(getGeoPointsFromRoute());
            return road;
        }

        @Override
        protected void onPostExecute(Object result){
            mapView.getOverlays().clear();
            drawMarkers();
            final Road road = (Road) result;
            final Polyline routeOverlay = RoadManager.buildRoadOverlay(road, getActivity());
            initRouteInstructions(road);
            final TextView lblDistance = (TextView)getView().findViewById(R.id.lblDistance);
            lblDistance.setText(String.format("%.2f", road.mLength)  + "km");
            final TextView lblTime = (TextView)getView().findViewById(R.id.lblTime);
            lblTime.setText(String.format("%.0f",(road.mDuration / 60)) + " " + getResources().getString(R.string.minutes)); //set estimated time in minutes
            mapView.getOverlays().add(routeOverlay);
            mapView.invalidate();
        }
    }

    private void initRouteInstructions(Road road){
        if(road != null && road.mNodes != null) {
            final ListView lstRouteDesc = (ListView)getView().findViewById(R.id.lstRouteDesc);
            final List<DescriptionRow> descriptions = new ArrayList<DescriptionRow>();
            int waypoint = 0;
            for(int i = 0; i < road.mNodes.size(); i++){
                final RoadNode node = road.mNodes.get(i);
                String instructions = node.mInstructions;
                if(node.mInstructions.toUpperCase().contains(getResources().getString(R.string.destination).toUpperCase())){
                    instructions = RouteManager.getInstance().getWaypoints().get(waypoint).getName();
                    waypoint++;
                }
                descriptions.add(new DescriptionRow(getIconForManeuver(node.mManeuverType),instructions));
            }
            lstRouteDesc.setAdapter(new RouteDescAdapter(getActivity(),android.R.layout.simple_list_item_1,descriptions));
        }
    }

    /*Get the correct icon for each maneuver*/
    private Drawable getIconForManeuver(int maneuver){
        int toReturn = R.drawable.ic_empty;
        switch(maneuver){
            case ManeuverType.NONE:
            case ManeuverType.TRANSIT_TAKE:
            case ManeuverType.TRANSIT_TRANSFER:
            case ManeuverType.TRANSIT_ENTER:
            case ManeuverType.TRANSIT_EXIT:
            case ManeuverType.TRANSIT_REMAIN_ON:
            case ManeuverType.ENTERING:
            case ManeuverType.BECOMES: toReturn = R.drawable.ic_empty;
                break;

            case ManeuverType.STRAIGHT:
            case ManeuverType.MERGE_STRAIGHT:
            case ManeuverType.RAMP_STRAIGHT:
            case ManeuverType.STAY_STRAIGHT: toReturn = R.drawable.ic_continue;
                break;

            case ManeuverType.ROUNDABOUT1:
            case ManeuverType.ROUNDABOUT2:
            case ManeuverType.ROUNDABOUT3:
            case ManeuverType.ROUNDABOUT4:
            case ManeuverType.ROUNDABOUT5:
            case ManeuverType.ROUNDABOUT6:
            case ManeuverType.ROUNDABOUT7:
            case ManeuverType.ROUNDABOUT8: toReturn = R.drawable.ic_roundabout;
                break;

            case ManeuverType.DESTINATION:
            case ManeuverType.DESTINATION_RIGHT:
            case ManeuverType.DESTINATION_LEFT: toReturn = R.drawable.ic_arrived;
                break;

            case ManeuverType.SLIGHT_LEFT:
            case ManeuverType.EXIT_LEFT:
            case ManeuverType.STAY_LEFT:
            case ManeuverType.MERGE_LEFT: toReturn = R.drawable.ic_slight_left;
                break;

            case ManeuverType.SLIGHT_RIGHT:
            case ManeuverType.EXIT_RIGHT:
            case ManeuverType.STAY_RIGHT:
            case ManeuverType.MERGE_RIGHT: toReturn = R.drawable.ic_slight_right;
                break;

            case ManeuverType.UTURN:
            case ManeuverType.UTURN_LEFT:
            case ManeuverType.UTURN_RIGHT: toReturn = R.drawable.ic_u_turn;
                break;

            case ManeuverType.RAMP_LEFT:
            case ManeuverType.LEFT: toReturn = R.drawable.ic_turn_left;
                break;

            case ManeuverType.RAMP_RIGHT:
            case ManeuverType.RIGHT: toReturn = R.drawable.ic_turn_right;
                break;

            case ManeuverType.SHARP_LEFT: toReturn = R.drawable.ic_sharp_left;
                break;

            case ManeuverType.SHARP_RIGHT : toReturn = R.drawable.ic_sharp_right;
                break;

        }
        return getResources().getDrawable(toReturn);
    }

    private void toggleMap(boolean showMap){
        final RelativeLayout mapview = (RelativeLayout)getView().findViewById(R.id.mapContainer);
        mapview.setVisibility(showMap ? View.VISIBLE : View.GONE);
        final ListView cntDesc = (ListView)getView().findViewById(R.id.lstRouteDesc);
        cntDesc.setVisibility( showMap ? View.GONE : View.VISIBLE);
    }

    private GeoPoint getCurrentLocation(){
        // Get the location manager
        final LocationManager locationManager = (LocationManager) getActivity().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        final Criteria criteria = new Criteria();
        final String provider = locationManager.getBestProvider(criteria, false);
        final Location loc = locationManager.getLastKnownLocation(provider);

        if(loc != null){
            return new GeoPoint(loc.getLatitude(),loc.getLongitude());
        }else{
            return new GeoPoint(DataManager.BELFORT_LAT,DataManager.BELFORT_LON);
        }
    }

    public void calculateRoute(){
        final ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null) {
            if (info.isConnected()) {
                new RouteCalcTask().execute();
            }else{
                Toast.makeText(getActivity(), "Please check your wireless connection and try again.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getActivity(), "Please check your wireless connection and try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void drawMarkers(){
       /*Add markers on the map*/
        final ItemizedOverlayWithFocus<OverlayItem> overlay;
        final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        final POI belfort = new POI();
        belfort.id = -1; //used to disable the clickhandler for this poi
        belfort.ENG_name = "Belfry";
        belfort.ENG_description = "";
        belfort.NL_name = "Belfort";
        belfort.NL_description = "";
        belfort.lat = DataManager.BELFORT_LAT +"";
        belfort.lon = DataManager.BELFORT_LON +"";
        final OverlayItem overlayItem = getOverlayItemFromPOI(belfort,getResources().getDrawable(R.drawable.castle));
        items.add(overlayItem);

        for(POI poi : RouteManager.getInstance().getWaypoints()){
            final OverlayItem item = getOverlayItemFromPOI(poi,null);
            items.add(item);
        }

        overlay = new ItemizedOverlayWithFocus<OverlayItem>(getActivity().getApplicationContext(), items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {

                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        final ArtoriaOverlayItem overlayItem = (ArtoriaOverlayItem)item;
                        final POI selectedItem = ((ArtoriaOverlayItem) item).poi;
                        if(selectedItem.id != -1) {
                            Intent i = MonumentDetailActivity.newIntent(getActivity(), selectedItem.id, false);
                            startActivity(i);
                        }
                        return true; // We 'handled' this event.
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                });

        mapView.getOverlays().add(overlay);
        mapView.invalidate();
    }

}
