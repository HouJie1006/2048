package kylec.hj.g2048;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import kylec.hj.g2048.app.Constant;
import kylec.hj.g2048.utils.LanguageUtils;
import kylec.hj.g2048.view.GameOverDialog;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    public static void start(Context context) {
        Intent starter = new Intent(context, LoginActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0,0);//自带设置进入Activity的动画
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button startGame = findViewById(R.id.startGame);
        startGame.setOnClickListener(this);
        Button continue_btn = findViewById(R.id.continue_btn);
        continue_btn.setOnClickListener(this);
        Button game_Info = findViewById(R.id.gameInfo);
        game_Info.setOnClickListener(this);
        Button charts = findViewById(R.id.startCharts);
        charts.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.startGame:
                Intent startGame = new Intent(this,GameActivity.class);
                startGame.putExtra("gameStatus", Constant.GAME_START);
                startActivity(startGame);
                break;
            case R.id.continue_btn:
                Intent continueGame = new Intent(this,GameActivity.class);
                continueGame.putExtra("gameStatus", Constant.GAME_CONTINUE);
                startActivity(continueGame);
                break;
            case R.id.gameInfo:

                LanguageUtils.set(getResources().getConfiguration().locale.getLanguage(),this);
                startActivity(new Intent(this,LoginActivity.class));
                finish();
                break;

            case R.id.startCharts:
                Intent startCharts = new Intent(this,ChartsActivity.class);
                startActivity(startCharts);
                break;
        }
    }
}
