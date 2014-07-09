package be.artoria.belfortapp.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import be.artoria.belfortapp.R;
import be.artoria.belfortapp.app.DragSortAdapter;
import be.artoria.belfortapp.app.POI;
import be.artoria.belfortapp.app.PrefUtils;
import be.artoria.belfortapp.app.RouteManager;
import be.artoria.belfortapp.mixare.MixView;


public class RouteActivity extends BaseActivity {

    DragSortListView listView;
    DragSortAdapter adapter;
    LinearLayout cntNoRoute;

    private final DragSortListView.DropListener onDrop = new DragSortListView.DropListener()
    {
        @Override
        public void drop(int from, int to)
        {
            if (from != to)
            {
                POI item = adapter.getItem(from);
                adapter.remove(item);
                adapter.insert(item, to);
            }
        }
    };

    private final DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener()
    {
        @Override
        public void remove(int which)
        {
            adapter.remove(adapter.getItem(which));
            PrefUtils.saveRoute(RouteManager.getInstance().getWaypoints());
            routeIsEmpty();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        initGUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*Initialize GUI */
    private void initGUI(){
        listView = (DragSortListView) findViewById(R.id.lstRoute);
        adapter = new DragSortAdapter(this,
                R.layout.route_list_item,  RouteManager.getInstance().getWaypoints());
        listView.setAdapter(adapter);
        listView.setDropListener(onDrop);
        listView.setRemoveListener(onRemove);

        final DragSortController controller = new DragSortController(listView);
        controller.setRemoveEnabled(true);

        listView.setFloatViewManager(controller);
        listView.setDragEnabled(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final POI poi = adapter.getItem(i);
                final int id = poi.id;
                final Intent intent = new Intent(RouteActivity.this, MonumentDetailActivity.class);
                intent.putExtra(MonumentDetailActivity.ARG_ID, id);
                startActivity(intent);
            }
        });

        cntNoRoute = (LinearLayout)findViewById(R.id.cntNoRoute);
        cntNoRoute.setVisibility(View.GONE);
        final Button btnBuilding = (Button)findViewById(R.id.btnBuildings);
        btnBuilding.setText(getResources().getStringArray(R.array.lstMenu)[1]);
        btnBuilding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = MonumentDetailActivity.newIntent(RouteActivity.this,1,false);
                startActivity(i);
            }
        });

        final Button btnPanorama = (Button)findViewById(R.id.btnPanorama);
        btnPanorama.setText(getResources().getStringArray(R.array.lstMenu)[0]);

        btnPanorama.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RouteActivity.this, MixView.class);
                startActivity(i);
            }
        });

        routeIsEmpty();
    }

    public void goToCalcRoute(View view) {
        final Intent i = new Intent(RouteActivity.this, MapActivity.class);
        startActivity(i);
    }

    private void routeIsEmpty(){
        if(RouteManager.getInstance().getWaypoints().isEmpty()){
            cntNoRoute.setVisibility(View.VISIBLE);
            Toast.makeText(this,getResources().getString(R.string.no_route),Toast.LENGTH_LONG).show();
        }else{
            cntNoRoute.setVisibility(View.GONE);
        }
    }

}
