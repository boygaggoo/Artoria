package be.artoria.belfortapp.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import be.artoria.belfortapp.R;
import be.artoria.belfortapp.app.DataManager;
import be.artoria.belfortapp.app.Floor;
import be.artoria.belfortapp.app.PrefUtils;

public class MuseumActivity extends BaseActivity {
    public static final String ARG_FLOOR = "be.artoria.MuseumActivity.floor";
    public static final long IMAGE_SWITCH_TIME = 5000;
    private Floor currentFloor;
    private ImageView imgCnt;
    private int currentImage = 0;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_museum);
        initGui();
    }

    private void initGui(){
        TextView txtContent = (TextView)findViewById(R.id.txtContent);
        txtContent.setMovementMethod(new ScrollingMovementMethod());
        imgCnt = (ImageView)findViewById(R.id.imgContent);


        Intent i = getIntent();
        int floor = i.getIntExtra(ARG_FLOOR,0);
        currentFloor = DataManager.getFloorList().get(floor);
        setTitle(currentFloor.getName());

        txtContent.setText(currentFloor.getDescription());

        imgCnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextImage();
            }
        });
        handler = new Handler();
        handler.postDelayed(imageSwitcher,0);
    }

    private Runnable imageSwitcher = new Runnable() {
        @Override
        public void run() {
            nextImage();
            handler.postDelayed(this,IMAGE_SWITCH_TIME);
        }
    };

    private void nextImage(){
        Picasso.with(PrefUtils.getContext()).load(currentFloor.images[currentImage]).into(imgCnt,new Callback(){
            @Override
            public void onSuccess() {
                currentImage = (currentImage +1) % currentFloor.images.length;
            }

            @Override
            public void onError() {
                System.err.println("Failed loading image ...");
                imgCnt.setImageDrawable(getResources().getDrawable(R.drawable.img_not_found));
            }
        });

    }

    public static Intent createIntent(Context ctx, int floor){
        Intent i = new Intent(ctx,MuseumActivity.class);
        i.putExtra(ARG_FLOOR,floor);
        return i;
    }
}
