package ch.mandelbrot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button generateMandelbrotButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        generateMandelbrotButton = (Button) findViewById(R.id.generateMandelbrot);

        generateMandelbrotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Starting a new Intent
                Intent  showMandelbrotScreen = new Intent(getApplicationContext(), ShowMandelbrotActivity.class);

                //Sending data to another Activity
                //nextScreen.putExtra("name", inputName.getText().toString());
                //nextScreen.putExtra("email", inputEmail.getText().toString());

                startActivity(showMandelbrotScreen);
            }
        });
    }
}
