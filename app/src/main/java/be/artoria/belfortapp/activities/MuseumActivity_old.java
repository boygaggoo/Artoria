package be.artoria.belfortapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import be.artoria.belfortapp.R;
import be.artoria.belfortapp.app.DataManager;
import be.artoria.belfortapp.app.Floor;
import be.artoria.belfortapp.app.FloorExhibit;
import be.artoria.belfortapp.app.MuseumImageMapper;

public class MuseumActivity_old extends /*SwipeActivity*/BaseActivity {
    public static final String ARG_FLOOR = "be.artoria.MuseumActivity.floor";
    private static final int MUSEUM_TITLE_SIZE = 32;
    private static final int IMAGE_HEIGHT = 350;
    private Floor currentFloor;
    private Integer currentFloorIndex;
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_museum);
        initGui();
    }
    
    private void initGui(){
        if(currentFloor == null || currentFloorIndex == null) {
        /* Initialize the current floor */
            Intent i = getIntent();
            currentFloorIndex = i.getIntExtra(ARG_FLOOR, 0);
        }
        scrollUp();
        currentFloor = DataManager.getFloorList().get(currentFloorIndex);
        LinearLayout lnrMuseum = (LinearLayout) findViewById(R.id.lnrMuseum);
        lnrMuseum.removeAllViews();


        ImageView imgHeader = (ImageView) findViewById(R.id.imgHeader);
        imgHeader.setImageDrawable(MuseumImageMapper.getDrawableForId(Integer.parseInt(currentFloor.exhibits.get(0).getImage())));

        for(int j = 0; j < currentFloor.exhibits.size();j++){
            addExhibit(currentFloor.exhibits.get(j),lnrMuseum,currentFloorIndex,j+1);
        }

        //next floor button ...
        final LinearLayout btnNextFloor = (LinearLayout)findViewById(R.id.btnNextFloor);
        btnNextFloor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               nextFloor();
            }
        });

        //set the title of the activity to the right floor name, strange way because the array is reversed ...
        String[] floorNames = getResources().getStringArray(R.array.lstMuseum);
        setTitle(floorNames[(floorNames.length -1) - currentFloorIndex]);
        //System.out.println((floorNames.length - 1) - currentFloorIndex);
    }

    public static Intent createIntent(Context ctx, int floor){
        Intent i = new Intent(ctx,MuseumActivity_old.class);
        i.putExtra(ARG_FLOOR,floor);
        return i;
    }

    private void addExhibit(FloorExhibit ex, View parentView, int floor, int exhibit){
        final LinearLayout parent = (LinearLayout) parentView;
        final LinearLayout lnrTitle = new LinearLayout(this);
        lnrTitle.setOrientation(LinearLayout.HORIZONTAL);


        final TextView txtNumber = new TextView(this);
        txtNumber.setTextSize(MUSEUM_TITLE_SIZE);
        txtNumber.setTextColor(Color.GRAY);
        txtNumber.setText(floor + "." + exhibit +"  ");

        final TextView txtTitle = new TextView(this);
        txtTitle.setTextSize(MUSEUM_TITLE_SIZE);
        txtTitle.setText(ex.getName());

        lnrTitle.addView(txtNumber);
        lnrTitle.addView(txtTitle);

        final TextView txtContent = new TextView(this);
        txtContent.setText(ex.getDescription());
        if(exhibit != 1){
            ImageView img = new ImageView(this);
            Drawable drwb = MuseumImageMapper.getDrawableForId(Integer.parseInt(ex.getImage()));
            if(drwb != null)
                img.setImageDrawable(drwb);
            img.setBackgroundColor(getResources().getColor(R.color.color2));
            img.setMaxHeight(IMAGE_HEIGHT);
            img.setMinimumHeight(IMAGE_HEIGHT);
            img.setScaleType(ImageView.ScaleType.FIT_XY);
            img.setMinimumWidth(getScreenWidth());
            img.setAdjustViewBounds(true);
            //img.setPadding(0,5,0,5);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 20, 0, 20);
            parent.addView(img,layoutParams);
        }
        parent.addView(lnrTitle);
        parent.addView(txtContent);

    }

    /*@Override
    protected void previous() {
        previousFloor();
    }

    @Override
    protected void next() {
        nextFloor();
    }*/

    private void nextFloor(){
        //int next = (currentFloorIndex +1) % DataManager.getFloorList().size();
        //System.out.println("about to call to floor " + next);
        //startNewMuseumActivity(next);
        this.currentFloorIndex = (currentFloorIndex +1) % DataManager.getFloorList().size();
        this.initGui();
    }

    private void previousFloor(){
        int prev = (currentFloorIndex -1) % DataManager.getFloorList().size();
        prev = prev == -1 ? DataManager.getFloorList().size() -1 : prev;
        //System.out.println("about to call to floor " + prev);
        //startNewMuseumActivity(prev);
        this.currentFloorIndex = prev;
        this.initGui();
    }

    private void startNewMuseumActivity(int floor){
        Intent toStart = createIntent(this,floor);
        startActivity(toStart);
    }

    private void scrollUp(){
        ScrollView scrMuseum = (ScrollView)findViewById(R.id.scrlMuseum);
       scrMuseum.fullScroll(ScrollView.FOCUS_UP);
    }


    private int getScreenWidth(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }
}
