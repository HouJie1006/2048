package kylec.me.g2048;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

/**
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LoginActivity.start(this);
        finish();
    }
}
