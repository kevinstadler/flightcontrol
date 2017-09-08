package flightcontrol.ars.br41n.io.flightcontrol;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import at.gtec.droneapi.ParrotDrone;
import at.gtec.droneapi.ParrotVideoView;
import at.gtec.extendixitemreceiver.ExtendiXItemReceiver;

public class flightcontrol extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flightcontrol);

        ExtendiXItemReceiver brain = new ExtendiXItemReceiver("TODO", this.getApplicationContext());

        ParrotVideoView video = new ParrotVideoView(this.getApplicationContext());
        ParrotDrone drone = new ParrotDrone(this.getApplicationContext(), video);
    }
}
