package flightcontrol.ars.br41n.io.flightcontrol;

import android.graphics.Bitmap;
import android.graphics.Canvas;
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
    private ParrotVideoView videoStream;

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

            // retrieve video component from XML
            this.videoStream = (ParrotVideoView) findViewById(R.id.videoView);
            this.drone = new ParrotDrone(this.getApplicationContext(), this.videoStream);

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
                this.drone.StartVideo();
                // we can't even be sure what state the drone is in when we connect but let's take off anyway
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
    @Override
    public void OnItemReceived(String s) {
        Log.i("extendix", "Received command: " + s);

        // duration of the command (per recognised key)
        int timeMs = 100;

        try {
            if (s.equals("KEY_LAND")) {
                this.drone.Land();
            } else if (s.equals("KEY_TAKEOFF")) {
                this.drone.TakeOff();
            } else if (s.equals("KEY_FORWARD")) {
                this.drone.Pitch((byte) 20, timeMs);
            } else if (s.equals("KEY_BACK")) {
                this.drone.Pitch((byte) -20, timeMs);

            // Yaw() == rotation
            } else if (s.equals("KEY_CW")) {
                this.drone.Yaw((byte) 20, timeMs);
            } else if (s.equals("KEY_CCW")) {
                this.drone.Yaw((byte) -20, timeMs);

            // Roll() == 'strafing'
            } else if (s.equals("KEY_LEFT")) {
                this.drone.Roll((byte) -20, timeMs);
            } else if (s.equals("KEY_RIGHT")) {
                this.drone.Roll((byte) 20, timeMs);

            } else if (s.startsWith("KEY_FACE")) {
                // TODO start a subroutine which keeps moving until the face recognition interrupts
            } else {
                Log.w("extendix", "Unknown command: " + s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isFaceInFrame() {
        Bitmap bitmap = Bitmap.createBitmap(this.videoStream.getWidth(), this.videoStream.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        this.videoStream.draw(canvas);
        int[] pixels = null; // FIXME korrekte groesse?
        bitmap.getPixels(pixels, 0, 0, 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // TODO pass pixels array on to face recognition
        return false;
    }

    // these two only get called by non-"KEY_" items
    @Override
    public void OnItemUnknownIntendix(String s) {
        Log.d("extendix", "Unknown: " + s);
    }

    @Override
    public void OnItemUnknownUDPReceived(String s) {
        Log.w("extendix", "Unknown UDP: " + s);
    }

}
