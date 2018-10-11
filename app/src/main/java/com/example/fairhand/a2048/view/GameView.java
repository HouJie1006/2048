package com.example.fairhand.a2048.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.GridLayout;

import com.example.fairhand.a2048.R;
import com.example.fairhand.a2048.app.Config;
import com.example.fairhand.a2048.util.SaveConfigUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by FairHand on 2018/10/2.<br />
 * 2048界面
 */
public class GameView extends GridLayout {
    
    public static final String KEY_SCORE = "KEY_SCORE";
    public static final String KEY_RESULT = "KEY_RESULT";
    public static final String ACTION_RECORD_SCORE = "ACTION_RECORD_SCORE";
    public static final String ACTION_WIN = "ACTION_WIN";
    public static final String ACTION_LOSE = "ACTION_LOSE";
    
    /**
     * 起始点XY坐标
     */
    private float setX;
    private float setY;
    
    /**
     * 在XY轴上移动的距离
     */
    private float offsetX;
    private float offsetY;
    
    private Cell[][] cells;
    
    /**
     * 滑动后每行(列)的数据
     */
    private List<Integer> dataAfterSwipe = new ArrayList<>();
    
    /**
     * 存放所有空格子的位置
     */
    private List<Point> emptyCellPoint = new ArrayList<>();
    
    /**
     * 滑动后记录上一个位置的数字
     */
    private int recordPreviousDigital = -1;
    
    /**
     * 记录滑动前的一行(列)上的数字
     */
    private ArrayList<Integer> someData = new ArrayList<>();
    
    /**
     * 判断有没有玩过游戏
     */
    public static boolean havePlayed;
    
    private static GameView gameView;
    
    private SoundPool mSoundPool;
    
    private int soundID;
    
    /**
     * 标识是否可以滑动
     */
    private boolean canSwipe;
    
    /**
     * 每行(列)方格数
     */
    private int GIRDColumnCount;
    
    /**
     * 游戏模式
     */
    public int gameMode;
    
    public GameView(Context context) {
        super(context);
        gameView = this;
        initSoundPool();
        initView(0);
    }
    
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        gameView = this;
        initSoundPool();
        initView(0);
    }
    
    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        gameView = this;
        initSoundPool();
        initView(0);
    }
    
    public static GameView getGameView() {
        return gameView;
    }
    
    /**
     * @param mode 游戏模式 0：经典 1：无限
     */
    @SuppressLint("ClickableViewAccessibility")
    public void initView(int mode) {
        gameMode = mode;
        havePlayed = false;
        canSwipe = true;
        // 移除所有视图，以便更改游戏难度
        removeAllViews();
        // 初始化格子
        if (mode == 0) {
            // 经典模式
            GIRDColumnCount = Config.GIRDColumnCount;
        } else if (mode == 1) {
            // 无限模式
            GIRDColumnCount = 6;
        }
        cells = new Cell[GIRDColumnCount][GIRDColumnCount];
        setColumnCount(GIRDColumnCount);// 设置界面大小
        int cellWidth = getCellSize();// 获取格子的宽
        int cellHeight = getCellSize();// 获取格子的高
        addCell(cellWidth, cellHeight);
        initGame();
        setOnTouchListener((v, event) -> {
            // 通知父控件不要拦截此控件的onTouch事件
            v.getParent().requestDisallowInterceptTouchEvent(true);
            if (canSwipe) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:// 按下
                        setX = event.getX();
                        setY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:// 拿起
                        offsetX = event.getX() - setX;
                        offsetY = event.getY() - setY;
                        // 判断滑动方向
                        int orientation = getOrientation(offsetX, offsetY);
                        switch (orientation) {
                            case 0:// 向右滑动
                                swipeRight();
                                break;
                            case 1:// 向左滑动
                                swipeLeft();
                                break;
                            case 2:// 向下滑动
                                swipeDown();
                                break;
                            case 3:// 向上滑动
                                swipeUp();
                                break;
                            default:
                                break;
                        }
                        break;
                }
            }
            return true;
        });
    }
    
    /**
     * 设置游戏配置
     */
    private void setConfiguration() {
        havePlayed = true;
        if (Config.VolumeState) {
            mSoundPool.play(soundID, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }
    
    /**
     * 初始化播放池
     */
    private void initSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSoundPool = new SoundPool.Builder().setMaxStreams(2).build();
        } else {
            mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        }
        // 加载音效资源
        soundID = mSoundPool.load(getContext(), R.raw.game_2048_volume, 1);
    }
    
    /**
     * 获取格子的大小
     *
     * @return 单个格子的宽度
     */
    private int getCellSize() {
        //  获取屏幕的宽度
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int cardWidth = metrics.widthPixels - dp2px();
        return (cardWidth - 12) / GIRDColumnCount;
    }
    
    /**
     * dp转换成px
     */
    private int dp2px() {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) ((float) 16 * scale + 0.5f);
    }
    
    /**
     * 初始化向布局中添加空格子
     *
     * @param cellWidth  格子宽
     * @param cellHeight 格子高
     */
    private void addCell(int cellWidth, int cellHeight) {
        Cell cell;
        for (int i = 0; i < GIRDColumnCount; i++) {
            for (int j = 0; j < GIRDColumnCount; j++) {
                if (i == GIRDColumnCount - 1) {
                    // 为最底下的格子加上bottomMargin
                    cell = new Cell(getContext(), 16, 16, 16);
                } else {
                    cell = new Cell(getContext(), 16, 16, 0);
                }
                cell.setDigital(0);
                addView(cell, cellWidth, cellHeight);
                cells[i][j] = cell;
            }
        }
    }
    
    /**
     * 初始化游戏
     */
    public void initGame() {
        addDigital(false);
        addDigital(false);
    }
    
    /**
     * 重置游戏
     */
    public void resetGame() {
        for (int i = 0; i < GIRDColumnCount; i++) {
            for (int j = 0; j < GIRDColumnCount; j++) {
                cells[i][j].setDigital(0);
            }
        }
        // 随机添加两个数字（2或4）
        addDigital(false);
        addDigital(false);
    }
    
    /**
     * 添加随机数字（2或4）或直接添加一个1024
     *
     * @param isCheat 是否是开挂
     */
    public void addDigital(boolean isCheat) {
        getEmptyCell();
        if (emptyCellPoint.size() > 0) {
            // 随机取出一个空格子的坐标位置
            Point point = emptyCellPoint.get((int) (Math.random() * emptyCellPoint.size()));
            if (isCheat) {
                cells[point.x][point.y].setDigital(1024);
            } else {
                // 通过坐标位置获取到此空格子并以4:6的概率随机设置一个2或4
                cells[point.x][point.y].setDigital(Math.random() > 0.4 ? 2 : 4);
            }
            setAppearAnim(cells[point.x][point.y]);// 设置动画
        }
    }
    
    /**
     * 获取空格子
     */
    private void getEmptyCell() {
        // 清空
        emptyCellPoint.clear();
        // 遍历所有格子，记录所有空格子的坐标位置
        for (int i = 0; i < GIRDColumnCount; i++) {
            for (int j = 0; j < GIRDColumnCount; j++) {
                if (cells[i][j].getDigital() <= 0) {// 空格子
                    emptyCellPoint.add(new Point(i, j));
                }
            }
        }
    }
    
    /**
     * 设置格子出现动画
     */
    private void setAppearAnim(Cell cell) {
        // 设置缩放动画（以自身中心为缩放点，从10%缩放到原始大小）
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0.1f, 1, 0.1f, 1,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(120);
        cell.setAnimation(null);
        cell.getItemCell().startAnimation(scaleAnimation);
    }
    
    // 思想：滑动时，判断每一行(列)的数据，0代表空格子，将非0数字存在一个list中
    // 再判断若当前数字与前一个数字相同，合并，并将此list作为这行(列)的新数据
    // 最后按滑动的方向向这行(列)中存放list中的数字
    
    /**
     * 上滑
     */
    private void swipeUp() {
        boolean needAddDigital = false;// 判断是否需要添加数字
        for (int i = 0; i < GIRDColumnCount; i++) {
            for (int j = 0; j < GIRDColumnCount; j++) {
                int currentDigital = cells[j][i].getDigital();// 获取当前位置数字
                someData.add(currentDigital);
                if (currentDigital != 0) {
                    if (recordPreviousDigital == -1) {// 记录数字
                        recordPreviousDigital = currentDigital;
                    } else {
                        if (recordPreviousDigital != currentDigital) {// 记录的之前的数字和当前数字不同
                            dataAfterSwipe.add(recordPreviousDigital);// 加入记录的数字
                            recordPreviousDigital = currentDigital;
                        } else {// 记录的之前的数字和当前的数字相同
                            dataAfterSwipe.add(recordPreviousDigital * 2);// 加入*2
                            recordScore(recordPreviousDigital * 2);// 记录得分
                            recordPreviousDigital = -1;// 重置记录数字
                        }
                    }
                }
            }
            
            if (recordPreviousDigital != -1) {
                dataAfterSwipe.add(recordPreviousDigital);
            }
            
            // 补0
            for (int p = dataAfterSwipe.size(); p < GIRDColumnCount; p++) {
                dataAfterSwipe.add(0);
            }
            // 若原始数据和移动后的数据不同，视为界面发生改变
            if (!someData.equals(dataAfterSwipe)) {
                needAddDigital = true;
            }
            someData.clear();
            
            // 重新设置格子数据
            for (int k = 0; k < dataAfterSwipe.size(); k++) {
                cells[k][i].setDigital(dataAfterSwipe.get(k));
            }
            // 重置数据
            recordPreviousDigital = -1;
            dataAfterSwipe.clear();
        }
        if (needAddDigital) {
            addDigital(false);// 添加一个随机数字（2或4）
            setConfiguration();
        }
        judgeOverOrAccomplish();
    }
    
    /**
     * 下滑
     */
    private void swipeDown() {
        boolean needAddDigital = false;// 判断是否需要添加数字
        for (int i = GIRDColumnCount - 1; i >= 0; i--) {
            for (int j = GIRDColumnCount - 1; j >= 0; j--) {
                int currentDigital = cells[j][i].getDigital();// 获取当前位置数字
                someData.add(currentDigital);
                if (currentDigital != 0) {
                    if (recordPreviousDigital == -1) {// 记录数字
                        recordPreviousDigital = currentDigital;
                    } else {
                        if (recordPreviousDigital != currentDigital) {// 记录的之前的数字和当前数字不同
                            dataAfterSwipe.add(recordPreviousDigital);// 加入记录的数字
                            recordPreviousDigital = currentDigital;
                        } else {// 记录的之前的数字和当前的数字相同
                            dataAfterSwipe.add(recordPreviousDigital * 2);
                            recordScore(recordPreviousDigital * 2);// 记录得分
                            recordPreviousDigital = -1;// 重置记录数字
                        }
                    }
                }
            }
            
            if (recordPreviousDigital != -1) {
                dataAfterSwipe.add(recordPreviousDigital);
            }
            
            /*// 重新设置格子数据
            for (int k = 0; k < 4 - dataAfterSwipe.size(); k++) {
                cells[k][i].setDigital(0);
            }
            Collections.reverse(dataAfterSwipe);
            int index = 0;
            for (int p = 4 - dataAfterSwipe.size(); p < 4; p++) {
                cells[p][i].setDigital(dataAfterSwipe.get(index++));
            }*/
            
            // 补0
            int temp = GIRDColumnCount - dataAfterSwipe.size();
            for (int k = 0; k < temp; k++) {
                dataAfterSwipe.add(0);
            }
            Collections.reverse(dataAfterSwipe);
            // 若原始数据和移动后的数据不同，视为界面发生改变
            Collections.reverse(someData);
            if (!someData.equals(dataAfterSwipe)) {
                needAddDigital = true;
            }
            someData.clear();
            
            // 重新设置格子数据
            int index = 0;
            for (int p = 0; p < GIRDColumnCount; p++) {
                cells[p][i].setDigital(dataAfterSwipe.get(index++));
            }
            // 重置数据
            recordPreviousDigital = -1;
            dataAfterSwipe.clear();
        }
        if (needAddDigital) {
            addDigital(false);// 添加一个随机数字（2或4）
            setConfiguration();
        }
        judgeOverOrAccomplish();
    }
    
    /**
     * 左滑
     */
    private void swipeLeft() {
        boolean needAddDigital = false;// 判断是否需要添加数字
        for (int i = 0; i < GIRDColumnCount; i++) {
            for (int j = 0; j < GIRDColumnCount; j++) {
                int currentDigital = cells[i][j].getDigital();// 获取当前位置数字
                someData.add(currentDigital);
                if (currentDigital != 0) {
                    if (recordPreviousDigital == -1) {// 记录数字
                        recordPreviousDigital = currentDigital;
                    } else {
                        if (recordPreviousDigital != currentDigital) {// 记录的之前的数字和当前数字不同
                            dataAfterSwipe.add(recordPreviousDigital);// 加入记录的数字
                            recordPreviousDigital = currentDigital;
                        } else {// 记录的之前的数字和当前的数字相同
                            dataAfterSwipe.add(recordPreviousDigital * 2);// 加入*2
                            recordScore(recordPreviousDigital * 2);// 记录得分
                            recordPreviousDigital = -1;// 重置记录数字
                        }
                    }
                }
            }
            
            if (recordPreviousDigital != -1) {
                dataAfterSwipe.add(recordPreviousDigital);
            }
            
            /*// 在list大小>1的情况下，判断是否需要合并数字
            if (dataAfterSwipe.size() > 1) {
                // 若前两个数字相同，合并，移除第二个数字
                Integer fNumber = dataAfterSwipe.get(0);
                if (fNumber.equals(dataAfterSwipe.get(1))) {
                    dataAfterSwipe.set(0, fNumber * 2);
                    dataAfterSwipe.remove(1);
                }
            }*/
            
            // 补0
            for (int p = dataAfterSwipe.size(); p < GIRDColumnCount; p++) {
                dataAfterSwipe.add(0);
            }
            // 若原始数据和移动后的数据不同，视为界面发生改变
            if (!someData.equals(dataAfterSwipe)) {
                needAddDigital = true;
            }
            someData.clear();
            
            // 重新设置格子数据
            for (int k = 0; k < GIRDColumnCount; k++) {
                cells[i][k].setDigital(dataAfterSwipe.get(k));
            }
            dataAfterSwipe.clear();// 每一行结束重置list
            recordPreviousDigital = -1;// 每一行结束重置记录数字
        }
        if (needAddDigital) {
            addDigital(false);// 添加一个随机数字（2或4）
            setConfiguration();
        }
        judgeOverOrAccomplish();
    }
    
    /**
     * 右滑
     */
    private void swipeRight() {
        boolean needAddDigital = false;// 判断是否需要添加数字
        for (int i = GIRDColumnCount - 1; i >= 0; i--) {
            for (int j = GIRDColumnCount - 1; j >= 0; j--) {
                int currentDigital = cells[i][j].getDigital();// 获取当前位置数字
                someData.add(currentDigital);
                if (currentDigital != 0) {
                    if (recordPreviousDigital == -1) {// 记录数字
                        recordPreviousDigital = currentDigital;
                    } else {
                        if (recordPreviousDigital != currentDigital) {// 记录的之前的数字和当前数字不同
                            dataAfterSwipe.add(recordPreviousDigital);// 加入记录的数字
                            recordPreviousDigital = currentDigital;
                        } else {// 记录的之前的数字和当前的数字相同
                            dataAfterSwipe.add(recordPreviousDigital * 2);// 加入*2
                            recordScore(recordPreviousDigital * 2);// 记录得分
                            recordPreviousDigital = -1;// 重置记录数字
                        }
                    }
                }
            }
            
            if (recordPreviousDigital != -1) {
                dataAfterSwipe.add(recordPreviousDigital);
            }
            
            /*// 重新设置格子数据
            for (int k = 0; k < 4 - dataAfterSwipe.size(); k++) {
                cells[i][k].setDigital(0);
            }
            Collections.reverse(dataAfterSwipe);
            int index = 0;
            for (int p = 4 - dataAfterSwipe.size(); p < 4; p++) {
                cells[i][p].setDigital(dataAfterSwipe.get(index++));
            }*/
            
            // 补0
            int temp = GIRDColumnCount - dataAfterSwipe.size();
            for (int k = 0; k < temp; k++) {
                dataAfterSwipe.add(0);
            }
            Collections.reverse(dataAfterSwipe);
            // 若原始数据和移动后的数据不同，视为界面发生改变
            Collections.reverse(someData);
            if (!someData.equals(dataAfterSwipe)) {
                needAddDigital = true;
            }
            someData.clear();
            
            // 重新设置格子数据
            int index = 0;
            for (int p = 0; p < GIRDColumnCount; p++) {
                cells[i][p].setDigital(dataAfterSwipe.get(index++));
            }
            // 重置数据
            recordPreviousDigital = -1;
            dataAfterSwipe.clear();
        }
        if (needAddDigital) {
            addDigital(false);// 添加一个随机数字（2或4）
            setConfiguration();
        }
        judgeOverOrAccomplish();
    }
    
    /**
     * 记录得分
     */
    private void recordScore(int score) {
        Intent intent = new Intent(ACTION_RECORD_SCORE);
        intent.putExtra(KEY_SCORE, score);
        getContext().sendBroadcast(intent);
    }
    
    /**
     * 检查游戏是否结束或达成游戏目标
     */
    private void judgeOverOrAccomplish() {
        boolean isOver = true;// 判断游戏结束的标识
        
        // 判断游戏是否结束
        // 格子都不为空且相邻的格子数字不同
        over:
        for (int i = 0; i < GIRDColumnCount; i++) {
            for (int j = 0; j < GIRDColumnCount; j++) {
                if (cells[i][j].getDigital() == 0) {// 有空格子，游戏还可以继续
                    isOver = false;
                    break over;
                }
                // 判断左右上下有没有相同的
                if (j < GIRDColumnCount - 1) {
                    if (cells[i][j].getDigital() == cells[i][j + 1].getDigital()) {
                        isOver = false;
                        break over;
                    }
                }
                if (i < GIRDColumnCount - 1) {
                    if (cells[i][j].getDigital() == cells[i + 1][j].getDigital()) {
                        isOver = false;
                        break over;
                    }
                }
            }
        }
        
        // 游戏结束，弹出提示框
        if (isOver) {
            canSwipe = false;
            sendGameOverMsg(ACTION_LOSE);
        }
        
        // 经典模式下才判赢
        if (gameMode == 0) {
            // 判断是否达成游戏目标
            for (int i = 0; i < GIRDColumnCount; i++) {
                for (int j = 0; j < GIRDColumnCount; j++) {
                    // 有一个格子数字到达2048则视为达成目标
                    if (cells[i][j].getDigital() == 2048) {
                        canSwipe = false;
                        int currentTime = SaveConfigUtil.getGoalTime(getContext()) + 1;
                        SaveConfigUtil.putGoalTime(getContext(), currentTime);
                        Config.GetGoalTime = currentTime;
                        sendGameOverMsg(ACTION_WIN);
                    }
                }
            }
        }
    }
    
    /**
     * 发送游戏结束消息
     */
    private void sendGameOverMsg(String action) {
        Intent intent = new Intent(action);
        if (action.equals(ACTION_WIN)) {
            intent.putExtra(KEY_RESULT, "You Win!");
        } else {
            intent.putExtra(KEY_RESULT, "You Lose!");
        }
        getContext().sendBroadcast(intent);
    }
    
    /**
     * 获取滑动方向<br />
     * 注：先依据在轴上滑动距离的大小，判断在哪个轴上滑动
     *
     * @param offsetX 在X轴上的移动距离
     * @param offsetY 在Y轴上的移动距离
     * @return 滑动方向
     * <br />
     * 注：0右滑、1左滑、2下滑、3上滑、-1未构成滑动
     */
    private int getOrientation(float offsetX, float offsetY) {
        if (Math.abs(offsetX) > Math.abs(offsetY)) {// X轴移动
            if (offsetX > 64) {
                return 0;
            } else if (offsetX < -64) {
                return 1;
            } else {
                return -1;
            }
        } else {// Y轴移动
            if (offsetY > 64) {
                return 2;
            } else if (offsetY < -64) {
                return 3;
            } else {
                return -1;
            }
        }
    }
    
}
