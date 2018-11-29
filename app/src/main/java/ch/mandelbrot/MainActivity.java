package ch.mandelbrot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button showMandelbrotButton;
    private Button showJuliaButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showMandelbrotButton = (Button) findViewById(R.id.showMandelbrot);

        showMandelbrotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Starting a new Intent
                Intent mandelbrotActivity = new Intent(getApplicationContext(), MandelbrotActivity.class);
                startActivity(mandelbrotActivity);
            }
        });

        showJuliaButton = (Button) findViewById(R.id.showJulia);

        showJuliaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Starting a new Intent
                Intent showJuliaActivity = new Intent(getApplicationContext(), JuliaActivity.class);
                startActivity(showJuliaActivity);
            }
        });
    }
}
