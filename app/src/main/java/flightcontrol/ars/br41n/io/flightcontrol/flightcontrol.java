package flightcontrol.ars.br41n.io.flightcontrol;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

import at.gtec.droneapi.ParrotConnectionStates;
import at.gtec.droneapi.ParrotDrone;
import at.gtec.droneapi.ParrotEventListener;
import at.gtec.droneapi.ParrotFlyingStates;
import at.gtec.droneapi.ParrotVideoView;
import at.gtec.extendixitemreceiver.ExtendiXItemReceiver;

public class flightcontrol extends AppCompatActivity implements ParrotEventListener {

    private ParrotDrone drone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("main", "Starting up");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flightcontrol);

        try {
            ExtendiXItemReceiver brain = new ExtendiXItemReceiver("TODO", this.getApplicationContext());

            ParrotVideoView video = new ParrotVideoView(this.getApplicationContext());
            this.drone = new ParrotDrone(this.getApplicationContext(), video);

            this.drone.AttachEventListener(this);

            // FIXME catch Exception here too?
            Log.i("main", "Starting scan");
            this.drone.StartScanningForDevices();
        } catch (Exception e) {
            // ExtendiXItemReceiver initialisation failed
            Log.e("main", e.getMessage());
        }
    }

    @Override
    public void OnDevicesAvailable(List<String> list) {
        Log.d("scan", "Received available drones");
        if (list.isEmpty()) {
            Log.e("scan", "No drones available!");
        } else {
            Log.i("scan", "Connecting to serial no. " + list.get(0));
            try {
                this.drone.Open(list.get(0));
            } catch (Exception e) {
                Log.e("scan", "Failed to connect drone: " + e.getMessage());
            }
        }
    }

    @Override
    public void OnConnectionStateChanged(ParrotConnectionStates parrotConnectionStates) {

    }

    @Override
    public void OnFlyingStateChanged(ParrotFlyingStates parrotFlyingStates) {

    }

    @Override
    public void OnBatteryLevelChanged(int i) {

    }

}
