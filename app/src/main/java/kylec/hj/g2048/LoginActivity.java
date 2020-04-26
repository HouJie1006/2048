package kylec.hj.g2048;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import kylec.hj.g2048.app.Constant;
import kylec.hj.g2048.view.GameOverDialog;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private Button startGame;
    private Button continue_btn;
    private Button game_Info;

    public static void start(Context context) {
        Intent starter = new Intent(context, LoginActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        startGame = findViewById(R.id.startGame);
        startGame.setOnClickListener(this);
        continue_btn =findViewById(R.id.continue_btn);
        continue_btn.setOnClickListener(this);
        game_Info = findViewById(R.id.gameInfo);
        game_Info.setOnClickListener(this);

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
                break;
        }
    }
}
