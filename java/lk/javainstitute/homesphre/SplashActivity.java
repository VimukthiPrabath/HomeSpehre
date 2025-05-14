package lk.javainstitute.homesphre;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import java.util.Timer;
import java.util.TimerTask;

import cjh.WaveProgressBarlibrary.WaveProgressBar;

public class SplashActivity extends AppCompatActivity {

    int progress = 0;

    boolean started = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        ImageView imageViewLogo = findViewById(R.id.imageViewLogo);

        SpringAnimation springAnimation = new SpringAnimation(imageViewLogo, DynamicAnimation.TRANSLATION_Y);

        SpringForce springForce = new SpringForce();
        springForce.setStiffness(SpringForce.STIFFNESS_VERY_LOW);
        springForce.setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY);
        springForce.setFinalPosition(100f);

        springAnimation.setSpring(springForce);
        springAnimation.start();

        WaveProgressBar progressBar = findViewById(R.id.waveProgressBar);
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (started) {
                    progress++;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(progress);
                        }
                    });
                    if (progress == 100) {
                        progress = 0;

                    }
                }
            }
        };

        timer.schedule(timerTask, 0, 80);
        started = !started;

        new Handler().postDelayed(() -> {

            FlingAnimation flingAnimation = new FlingAnimation(imageViewLogo, DynamicAnimation.TRANSLATION_Y);
            flingAnimation.setStartVelocity(-5000f);
            flingAnimation.setFriction(0.2f);
            flingAnimation.start();

            SharedPreferences sp = getSharedPreferences("lk.javainstitute.homesphre.data", Context.MODE_PRIVATE);
            String userJson = sp.getString("user", null);

            Intent intent;
            if (userJson != null) {
                // ✅ User exists -> Go to Home
                intent = new Intent(SplashActivity.this, HomeActivity.class);
            } else {
                // ✅ No user -> Go to Sign In
                intent = new Intent(SplashActivity.this, SignIn.class);
            }


            startActivity(intent);
            finish();

        }, 3900);
    }
}