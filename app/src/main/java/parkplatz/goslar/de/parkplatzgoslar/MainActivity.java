package parkplatz.goslar.de.parkplatzgoslar;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LocationListener, CompoundButton.OnCheckedChangeListener {

    private static final String[] LOCATION_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static final int INITIAL_REQUEST=1337;
    private static final int LOCATION_REQUEST=INITIAL_REQUEST+1;

    private LocationManager locationManager;
    private ArrayList<Location> locations;

    private ToggleButton toggleButton;
    private TextView logView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locations = new ArrayList<Location>();

        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        toggleButton.setChecked(false);
        toggleButton.setOnCheckedChangeListener(this);

        logView = (TextView) findViewById(R.id.logView);
        logView.setText("");
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked)
        {
            startTracking();
        }
        else
        {
            stopTracking();
        }
    }

    private void startTracking()
    {
        if (!canAccessLocation()) {
            addLogLine("Requesting GPS permission");
            ActivityCompat.requestPermissions(this, LOCATION_PERMS, INITIAL_REQUEST);
            return;
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try
        {
            addLogLine("Requesting GPS updates...");
            locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,
                    2000,
                    10, this);
        }
        catch (SecurityException exception)
        {
            addLogLine("GPS exception: " + exception.getLocalizedMessage());
        }
    }

    private void stopTracking()
    {
        locationManager.removeUpdates(this);
        addLogLine("Stopped GPS updates");
        sendLocationsToServer();
    }

    private void sendLocationsToServer()
    {
        if (locations.size() == 0)
        {
            addLogLine("No locations tracked. Skipping server communication.");
            return;
        }

        addLogLine("Trying to send " + locations.size() + " GPS point(s) to server.");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch(requestCode) {

            case LOCATION_REQUEST:
                if (canAccessLocation()) {
                    addLogLine("GPS permission granted");
                    startTracking();
                }
                else {
                    addLogLine("GPS permission not granted");
                }
                break;
        }
    }

    private boolean canAccessLocation() {
        return(hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private boolean hasPermission(String perm) {
        return(PackageManager.PERMISSION_GRANTED== ContextCompat.checkSelfPermission(getBaseContext(), perm));
    }

    @Override
    public void onLocationChanged(Location location) {
        locations.add(location);

        String msg = String.format("New GPS point: %.1f, %.1f", location.getLatitude(), location.getLongitude());
        addLogLine(msg);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        addLogLine("Gps status: " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        addLogLine("Gps is enabled!");
    }

    @Override
    public void onProviderDisabled(String provider) {
        addLogLine("Gps is disabled!");
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    private void addLogLine(String text)
    {
        logView.append("\n" + text);
    }
}
