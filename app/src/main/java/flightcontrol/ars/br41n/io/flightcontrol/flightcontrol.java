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

public class flightcontrol extends AppCompatActivity implements ParrotEventListener, ExtendiXItemReceiver.OnItemReceivedListener {

    private ExtendiXItemReceiver brain;
    private ParrotDrone drone;

    // called when the android app is backgrounded
    @Override
    public void onPause() {
        super.onPause();
        try {
            // disconnect (which should emergency land the drone in the process)
            this.drone.Close();
            this.brain.EndReceiving();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("main", "Starting up");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flightcontrol);

        try {
            this.brain = new ExtendiXItemReceiver("KEY", this.getApplicationContext());
            this.brain.AttachEventListener(this);
            this.brain.BeginReceiving(12345);

            ParrotVideoView video = new ParrotVideoView(this.getApplicationContext());
            this.drone = new ParrotDrone(this.getApplicationContext(), video);

            this.drone.AttachEventListener(this);

            Log.i("main", "Starting scan");
            this.drone.StartScanningForDevices();
            // setup done, it's all event listening from here...

        } catch (Exception e) {
            e.printStackTrace();
            try {
                // release port, just in case
                this.brain.EndReceiving();
            } catch (Exception e1) {
                // whatever
            }
        }
    }

    // DRONE METHODS HERE
    @Override
    public void OnDevicesAvailable(List<String> list) {
        Log.d("scan", "Scan complete");
        if (list.isEmpty()) {
            Log.i("scan", "No drones found");
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
        Log.i("connection", "Drone connection state changed: " + parrotConnectionStates.toString());
        if (parrotConnectionStates == ParrotConnectionStates.CONNECTED) {
            try {
                this.drone.TakeOff();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void OnFlyingStateChanged(ParrotFlyingStates parrotFlyingStates) {
        Log.i("flight", "Drone flight state changed: " + parrotFlyingStates.toString());

        // the drone goes from: LANDED over TAKING_OFF to HOVERING

        // FIXME do something safe when the new state is EMERGENCY

        // TODO change this -- now we just land as soon as we're hovering
        if (parrotFlyingStates == ParrotFlyingStates.HOVERING) {
            Log.i("flight", "Achieved hovering, landing again");
            try {
                this.drone.Land();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void OnBatteryLevelChanged(int i) {
        Log.i("battery", "Drone battery state changed: " + Integer.toString(i));
    }

    // EXTENDIX METHODS HERE

    // this is the one that gets called
    @Override
    public void OnItemUnknownIntendix(String s) {
        Log.d("extendix", "Unknown: " + s);
    }

    @Override
    public void OnItemUnknownUDPReceived(String s) {
        Log.w("extendix", "Unknown UDP: " + s);
    }

    @Override
    public void OnItemReceived(String s) {
        Log.i("extendix", "Executing command: " + s);
    }
}
