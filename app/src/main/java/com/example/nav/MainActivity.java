package com.example.nav;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import android.location.Geocoder;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener, PermissionsListener {

    // variables for adding location layer
    private MapView mapView;
    private MapboxMap mapboxMap;
    // variables for adding location layer
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    // variables for calculating and drawing a route
    private DirectionsRoute currentRoute;
    private static final String TAG = "MainActivity";
    private NavigationMapRoute navigationMapRoute;
    // variables needed to initialize navigation
    private Button button;
    //appears after mapclick
    private ImageButton btnSetPlace;
    //for selecting alt routes
    private int countAltRoute=0;
    //FAB
    FloatingActionButton fab1, fab2, fab3;
    //open close fab
    boolean isFABOpen=false;
    //search history
    private static String search = null,name,email,dist,time;

    DatabaseReference NavDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        //get current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address
             name = user.getDisplayName();
             email = user.getEmail();
            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();
            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            String uid = user.getUid();

            btnSetPlace = (ImageButton) findViewById(R.id.buttonSetPlace);
//            btnSetPlace.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    placeSet = false;
//                }
//            });
        }

        NavDatabase = FirebaseDatabase.getInstance().getReference("History");

        //FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab3 = (FloatingActionButton) findViewById(R.id.fab3);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFABOpen){
                    showFABMenu();
                    vibrate();
                }
                else{
                    closeFABMenu();
                    vibrate();
                }
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//prev destinations
                vibrate();
                startActivity(new Intent(MainActivity.this, Destinations.class));
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//search
                vibrate();
                showAddItemDialog(MainActivity.this);
            }
        });
        fab3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //open settings page
                vibrate();
                startActivity(new Intent(MainActivity.this, Settings.class));
            }
        });
        //if selection from history add route from selection

        if(Destinations.blDest == true){
            HistDir();
        }
    }

    private void showFABMenu(){
        isFABOpen=true;
        fab1.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        fab2.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
        fab3.animate().translationY(-getResources().getDimension(R.dimen.standard_155));
    }

    private void closeFABMenu() {
        isFABOpen = false;
        fab1.animate().translationY(0);
        fab2.animate().translationY(0);
        fab3.animate().translationY(0);
    }
    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(getString(R.string.navigation_guidance_day), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

                enableLocationComponent(style);

                addDestinationIconSymbolLayer(style);
                //cannot bypass?tried if and while
                    mapboxMap.addOnMapClickListener(MainActivity.this);

                button = findViewById(R.id.startButton);
                try{
                button.setOnClickListener(new View.OnClickListener() {
                    //the go nav button
                    @Override
                    public void onClick(View v) {
                        vibrate();

                        boolean simulateRoute = true;//for testing
                        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                                .directionsRoute(currentRoute)
                                //.shouldSimulateRoute(simulateRoute) //for testing
                                .build();
// Call this method with Context from within an Activity
                        NavigationLauncher.startNavigation(MainActivity.this, options);
                    }
                });
                }catch (Exception e){
                    Toast.makeText(MainActivity.this, "Nav auto error:  "+e, Toast.LENGTH_LONG).show();

                }
            }
        });
        //if selection from history add route from selection
        if(Destinations.blDest == true){
            HistDir();
        }
    }

    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("destination-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        loadedMapStyle.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
        destinationSymbolLayer.withProperties(
                iconImage("destination-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
    }


    public boolean onMapClick(@NonNull LatLng point) {

        Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());

        Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                locationComponent.getLastKnownLocation().getLatitude());

        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
        if (source != null) {
            source.setGeoJson(Feature.fromGeometry(destinationPoint));
        }
//geocode reverse
        try{
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());
            addresses = geocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            Log.d(TAG, "reverse geocde: "+address+" , "+city+" , "+state+" , "+country+" , "+postalCode+" , "+knownName);

            search=""+address+" , "+city+" , "+state+" , "+country+" , "+postalCode+" , "+knownName;//not search but still add to history. var name confusion
            History.search=search;//probably not needed. Note to remove
            AddHistory();

        }catch (Exception e){
            Toast.makeText(this, "reverse geocode issue to address error:  "+e, Toast.LENGTH_LONG).show();
        }//end try trip record
        getRoute(originPoint, destinationPoint);
        button.setEnabled(true);
        button.setBackgroundResource(R.drawable.roundbuttongo);

        return true;
    }

    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .alternatives(true)
                .profile(Settings.trans)//mode type. simple String.
                .voiceUnits(Settings.type)//lit type imperial or metric
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        List<DirectionsRoute> routes;//
// You can get the generic HTTP info about the response
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null && response.isSuccessful()) {
                            Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e(TAG, "No routes found");
                            return;
                        }
                        routes = response.body().routes();
// Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoutes(routes);

                        currentRoute = response.body().routes().get(0);

                        //select alt route
                        //cycle through alt routes
                        try {
                            btnSetPlace.setVisibility(View.VISIBLE);
                            btnSetPlace.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    vibrate();

                                    if(countAltRoute<response.body().routes().size()) {
                                        currentRoute = routes.get(countAltRoute);
                                        navigationMapRoute.addRoute(currentRoute);
                                        if (navigationMapRoute != null) {
                                            //navigationMapRoute.removeRoute();

                                        } else {
                                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                                        }
                                        countAltRoute++;
                                    }else{countAltRoute=0;
                                        currentRoute = routes.get(countAltRoute);
                                        navigationMapRoute.addRoute(currentRoute);
                                        if (navigationMapRoute != null) {
                                            //navigationMapRoute.removeRoute();

                                        } else {
                                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                                        }
                                        countAltRoute++;
                                    }
//display distance
                                    if(Settings.type.equals("metric")) {
                                        double mDist = ((currentRoute.distance()) / 1000);
                                        double time =  (currentRoute.duration()) / 60;

                                        Snackbar snack = Snackbar.make(mapView, "" + String.format("%.2f",mDist) + " Kilometers \t\t\t" + String.format("%.0f", time) + " Minutes", Snackbar.LENGTH_INDEFINITE);
                                        View view = snack.getView();
                                        TextView mTextView = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                                            mTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                        else
                                            mTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                                        FrameLayout.LayoutParams params =(FrameLayout.LayoutParams)view.getLayoutParams();
                                        params.gravity = Gravity.TOP;
                                        view.setLayoutParams(params);
                                        snack.show();

                                    }if(Settings.type.equals("imperial")) {
                                        double iDist = ((currentRoute.distance()) / 1000)*0.621371;
                                        double time =  (currentRoute.duration()) / 60;

                                        Snackbar snack = Snackbar.make(mapView, "" + String.format("%.2f",iDist) + " Miles \t\t\t" + String.format("%.0f", time) + " Minutes", Snackbar.LENGTH_INDEFINITE);
                                        View view = snack.getView();
                                        TextView mTextView = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                                            mTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                        else
                                            mTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                                        FrameLayout.LayoutParams params =(FrameLayout.LayoutParams)view.getLayoutParams();
                                        params.gravity = Gravity.TOP;
                                        view.setLayoutParams(params);
                                        snack.show();

                                    }
                                }
                            });
                        }catch (Exception e){
                            Log.e(TAG, "Error Alt Route: " + e);
                        }

                        //display distance for initial selection
                       if(Settings.type.equals("metric")) {
                           double mDist = ((currentRoute.distance()) / 1000);
                             double time =  (currentRoute.duration()) / 60;

                           Snackbar snack = Snackbar.make(mapView, "" + String.format("%.2f",mDist) + " Kilometers \t\t\t" + String.format("%.0f", time) + " Minutes", Snackbar.LENGTH_INDEFINITE);
                           View view = snack.getView();
                           TextView mTextView = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                               mTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                           else
                               mTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                           FrameLayout.LayoutParams params =(FrameLayout.LayoutParams)view.getLayoutParams();
                           params.gravity = Gravity.TOP;
                           view.setLayoutParams(params);
                           snack.show();

                        }if(Settings.type.equals("imperial")) {
                            double iDist = ((currentRoute.distance()) / 1000)*0.621371;
                            double time =  (currentRoute.duration()) / 60;

                            Snackbar snack = Snackbar.make(mapView, "" + String.format("%.2f",iDist) + " Miles \t\t\t" + String.format("%.0f", time) + " Minutes", Snackbar.LENGTH_INDEFINITE);
                            View view = snack.getView();
                            TextView mTextView = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                                mTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            else
                                mTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                            FrameLayout.LayoutParams params =(FrameLayout.LayoutParams)view.getLayoutParams();
                            params.gravity = Gravity.TOP;
                            view.setLayoutParams(params);
                            snack.show();

                        }
// metric imperial..store to db
                        try{
                        if(Settings.type.equals("metric")) {
                            dist=""+String.format("%.2f",((currentRoute.distance()) / 1000))+" Kilometers.";
                            time=""+ String.format("%.2f",((currentRoute.duration()) / 60)) + " Minutes";
                        }if(Settings.type.equals("imperial")) {
                            dist=""+String.format("%.2f",(((currentRoute.distance()) / 1000)*0.621371))+" Miles.";
                            time=""+String.format("%.2f", ((currentRoute.duration()) / 60 ))+ " Minutes";
                        }
                        }catch (Exception e){}
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                });
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
// Activate the MapboxMap LocationComponent to show user location
// Adding in LocationComponentOptions is also an optional parameter
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);
// Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent(mapboxMap.getStyle());
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    //search popup box
    private void showAddItemDialog(Context c) {
        final EditText taskEditText = new EditText(c);
        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle("SEARCH")
                .setMessage("enter address")
                .setView(taskEditText)
                .setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        vibrate();
                        search = (taskEditText.getText()).toString();
                        History.search=(taskEditText.getText()).toString();
                        AddHistory();
//mapbox search location
                            MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
                                    .accessToken(Mapbox.getAccessToken())
                                    .query(search)
                                    .build();

                            mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
                                @Override
                                public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

                                    List<CarmenFeature> results = response.body().features();

                                    if (results.size() > 0) {

                                        // Log the first results Point.
                                        Point searchResultPoint = results.get(0).center();
                                        Log.d(TAG, "onResponse: " + searchResultPoint.toString());

                                        Point destinationPoint = searchResultPoint;

                                        @SuppressLint("MissingPermission") Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                                                locationComponent.getLastKnownLocation().getLatitude());

                                        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
                                        if (source != null) {
                                            source.setGeoJson(Feature.fromGeometry(destinationPoint));
                                        }

                                        getRoute(originPoint, destinationPoint);

                                        button.setEnabled(true);
                                        button.setBackgroundResource(R.drawable.roundbuttongo);//enable button after search

                                    } else {

                                        // No result for your request were found.
                                        Log.d(TAG, "onResponse: No result found");

                                    }
                                }

                                @Override
                                public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                                    throwable.printStackTrace();
                                }
                            });
//end search
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    public void AddHistory(){
        try
        {
            String id =NavDatabase.push().getKey();
            History.id=id;
            String userEmail = MainActivity.email;
            String address = MainActivity.search;
            History history = new History(address,userEmail,id);
            NavDatabase.child(id).setValue(history);
        }
        catch (Exception e)
        {
            Toast.makeText(this, "AddHistory() function error: "+e, Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(100);
        }
    }

    public void HistDir(){
        Toast.makeText(this, ""+Destinations.clickDest, Toast.LENGTH_LONG).show();

//mapbox search location
        MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
                .accessToken(Mapbox.getAccessToken())
                .query(Destinations.clickDest)//from Destinations
                .build();

        mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

                List<CarmenFeature> results = response.body().features();

                if (results.size() > 0) {

                    // Log the first results Point.
                    Point searchResultPoint = results.get(0).center();
                    Log.d(TAG, "onResponse: " + searchResultPoint.toString());

                    Point destinationPoint = searchResultPoint;

                    @SuppressLint("MissingPermission") Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                            locationComponent.getLastKnownLocation().getLatitude());

                    GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
                    if (source != null) {
                        source.setGeoJson(Feature.fromGeometry(destinationPoint));
                    }

                    getRoute(originPoint, destinationPoint);

                    button.setEnabled(true);
                    button.setBackgroundResource(R.drawable.roundbuttongo);//enable button after search

                } else {

                    // No result for your request were found.
                    Log.d(TAG, "onResponse: No result found");

                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                throwable.printStackTrace();
            }
        });
//end search
    }



    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

}
