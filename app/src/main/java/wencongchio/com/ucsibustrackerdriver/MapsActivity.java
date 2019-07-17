package wencongchio.com.ucsibustrackerdriver;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Response.Listener<String> responseListener;
    private ApiInterface apiInterface;

    private Switch switchStartService;
    private Button btnTowardTBS;
    private Button btnTowardCollege;
    private Button btnTowardUniversity;
    private Spinner spinnerBus;
    private LinearLayout destinationLayout;

    boolean firstLocation = true;
    boolean readyToExit = false;
    String busID = "a";

    final String universityLocation = "3.080022,101.7335";
    final String collegeLocation = "3.085257,101.736870";
    final String tbsLocation = "3.076147,101.711917";

    private static String API_KEY = "API Key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        destinationLayout = (LinearLayout) findViewById(R.id.layout_map_destination);

        btnTowardTBS = (Button) findViewById(R.id.btn_map_towardTBS);
        btnTowardCollege = (Button) findViewById(R.id.btn_map_towardCollege);
        btnTowardUniversity = (Button) findViewById(R.id.btn_map_towardUniversity);

        spinnerBus = (Spinner) findViewById(R.id.spinner_map_busID);
        String[] busList = new String[]{"Bus A", "Bus B", "Bus C"};
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, busList);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerBus.setAdapter(spinnerArrayAdapter);

        switchStartService = (Switch) findViewById(R.id.switch_map_startService);
        switchStartService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    connectDriver();

                    destinationLayout.setVisibility(View.VISIBLE);

                    btnTowardTBS.setEnabled(true);
                    btnTowardCollege.setEnabled(true);
                    btnTowardUniversity.setEnabled(true);

                    spinnerBus.setEnabled(false);
                } else {

                    disconnectDriver();

                    destinationLayout.setVisibility(View.INVISIBLE);

                    btnTowardTBS.setEnabled(false);
                    btnTowardCollege.setEnabled(false);
                    btnTowardUniversity.setEnabled(false);

                    spinnerBus.setEnabled(true);
                }
            }
        });

        btnTowardCollege.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                towardCollege();
            }
        });

        btnTowardTBS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                towardTBS();
            }
        });

        btnTowardUniversity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                towardUniversity();
            }
        });

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onBackPressed() {
        if (readyToExit) {
            disconnectDriver();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("exit", true);
            startActivity(intent);
            finish();
            return;
        }

        readyToExit = true;
        Toast.makeText(this, R.string.double_press_to_exit, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                readyToExit = false;
            }
        }, 2000);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        } else {
            checkLocationPermission();
        }
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (firstLocation) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                        firstLocation = false;
                    }

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("BusAvailable");
                    GeoFire geoFire = new GeoFire(ref);

                    try {
                        geoFire.setLocation(busID, new GeoLocation(location.getLatitude(), location.getLongitude()), new
                                GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {

                                    }
                                });
                    } catch (Exception e) {

                    }
                }
            }
        }
    };

    private void checkLocationPermission() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.message_location_permission)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }

        }
    }

    private void connectDriver() {
        String selectedBus = spinnerBus.getSelectedItem().toString();
        busID = selectedBus.substring(selectedBus.length() - 1).toLowerCase();

        checkLocationPermission();
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);

        getDepartureTime(0, "tbs");
        getDepartureTime(0, "college");
        getDepartureTime(0, "university");
    }

    private void disconnectDriver() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }

        DatabaseReference availableRef = FirebaseDatabase.getInstance().getReference("BusAvailable");
        GeoFire geoFire = new GeoFire(availableRef);

        geoFire.removeLocation(busID, new
                GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {

                    }
                });

        DatabaseReference departureTimeRef = FirebaseDatabase.getInstance().getReference("BusDepartureTime").child(busID);
        departureTimeRef.removeValue();

        checkLocationPermission();
        mMap.setMyLocationEnabled(false);
    }

    private void getDepartureTime(int interval, final String destination) {
        responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonResponse = new JSONArray(response);

                    for (int i = 0; i < jsonResponse.length(); i++) {
                        try {
                            JSONObject json_data = jsonResponse.getJSONObject(i);
                            String departureTime = json_data.getString("time");

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("BusDepartureTime").child(busID).child(destination);
                            ref.setValue(departureTime);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Calendar c = Calendar.getInstance();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        RequestQueue queue = Volley.newRequestQueue(this);
        if (dayOfWeek > 1 && dayOfWeek < 7) {
            DepartureTimeRequest departureTimeRequest = new DepartureTimeRequest(interval, busID, "weekdays", destination, responseListener);
            queue.add(departureTimeRequest);
        } else if (dayOfWeek == 7) {
            DepartureTimeRequest departureTimeRequest = new DepartureTimeRequest(interval, busID, "saturday", destination, responseListener);
            queue.add(departureTimeRequest);
        }
    }


    private void towardTBS() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        final Calendar currentTime = Calendar.getInstance();

        getDepartureTime(10, "university");

        getArrivalTime("now", currentTime, universityLocation + "|" + tbsLocation, tbsLocation + "|" + collegeLocation, "tbs");

        btnTowardTBS.setEnabled(false);
        btnTowardUniversity.setEnabled(true);
        btnTowardCollege.setEnabled(true);
    }


    private void towardCollege() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        getDepartureTime(10, "university");

        //get the arrival time at tbs based on scheduled departure time at university
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("BusDepartureTime").child(busID).child("university");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Calendar departureTime = Calendar.getInstance();
                if (dataSnapshot.exists()) {
                    String time = (String) dataSnapshot.getValue();

                    try {
                        departureTime.setTime(dateFormat.parse(time));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    Date currentTime = new Date();
                    try {
                        currentTime = dateFormat.parse(dateFormat.format(new Date()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    long departureTimeMillis = (System.currentTimeMillis() + (departureTime.getTime().getTime() - currentTime.getTime())) / 1000;

                    getArrivalTime(departureTimeMillis + "", departureTime, universityLocation, tbsLocation, "tbs");

                    ref.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //get the arrival time at college based on current time
        Calendar currentTime = Calendar.getInstance();
        getArrivalTime("now", currentTime, tbsLocation, collegeLocation, "college");

        btnTowardTBS.setEnabled(true);
        btnTowardUniversity.setEnabled(true);
        btnTowardCollege.setEnabled(false);
    }


    private void towardUniversity() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        getDepartureTime(10, "university");

        //get the arrival time at tbs & college based on scheduled departure time at university
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("BusDepartureTime").child(busID).child("university");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Calendar departureTime = Calendar.getInstance();
                if (dataSnapshot.exists()) {
                    String time = (String) dataSnapshot.getValue();

                    try {
                        departureTime.setTime(dateFormat.parse(time));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    Date currentTime = new Date();
                    try {
                        currentTime = dateFormat.parse(dateFormat.format(new Date()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    long departureTimeMillis = (System.currentTimeMillis() + (departureTime.getTime().getTime() - currentTime.getTime())) / 1000;

                    getArrivalTime(departureTimeMillis + "", departureTime, universityLocation + "|" + tbsLocation, tbsLocation + "|" + collegeLocation, "tbs");

                    ref.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        btnTowardTBS.setEnabled(true);
        btnTowardUniversity.setEnabled(false);
        btnTowardCollege.setEnabled(true);
    }

    private void getArrivalTime(String departureTimeMillis, final Calendar departureTime, String originCoordinate, String destinationCoordinate, final String destinationRef) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        apiInterface.getDistanceMatrix(originCoordinate, destinationCoordinate, departureTimeMillis, API_KEY).enqueue(new Callback<APIResult>() {
            @Override
            public void onResponse(Call<APIResult> call, retrofit2.Response<APIResult> response) {
                int duration = response.body().getRow().get(0).getElement().get(0).getDuration_in_traffic().getValue();

                departureTime.add(Calendar.SECOND, duration);

                String arrivalTime = dateFormat.format(departureTime.getTime());

                DatabaseReference tbsRef = FirebaseDatabase.getInstance().getReference("BusDepartureTime").child(busID).child(destinationRef);
                tbsRef.setValue(arrivalTime);

                if (response.body().getRow().size() > 1) {

                    int durationToCollege = response.body().getRow().get(1).getElement().get(1).getDuration_in_traffic().getValue();
                    departureTime.add(Calendar.SECOND, durationToCollege);

                    String formattedCollegeDepartureTime = dateFormat.format(departureTime.getTime());

                    DatabaseReference collegeRef = FirebaseDatabase.getInstance().getReference("BusDepartureTime").child(busID).child("college");
                    collegeRef.setValue(formattedCollegeDepartureTime);

                }
            }

            @Override
            public void onFailure(Call<APIResult> call, Throwable t) {

            }
        });
    }
}
