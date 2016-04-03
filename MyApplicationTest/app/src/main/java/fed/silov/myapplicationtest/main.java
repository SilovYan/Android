package fed.silov.myapplicationtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;


public class main extends AppCompatActivity {
    private static final String KEY_COUNT = "COUNT";
    private static final String KEY_RECORD = "RECORD";
    private static final String KEY_CURE_FIELD = "CURE_FIELD";
    private static final String KEY_LAST_FIELD = "LAST_FIELD";
    private static final String KEY_PLAY = "PLAY";
    private static final String KEY_TEXT = "TEXT";

    private SharedPreferences mSettings;

    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    private GestureDetector gestureDetector;
    ViewGroup.LayoutParams cureParamsButton;
    ViewGroup.LayoutParams startGameButtonParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    ArrayList<LinearLayout> linesGUI=new ArrayList<LinearLayout>();
    TableLayout tabLayout;
    Button startGameBut;
    Button restartGameBut;
    Button sendCommandButton;
    TextView gameProgress;
    TextView recordView;
    Button[][] fieldButtons;
    int[] lastField;
    boolean play=true;
    int countHoop=0;
    int record=0;

    enum Vector{
        up,
        left,
        right,
        down
    }

    public boolean isWin(){
        for(int i=0; i<4; i++)
            for(int j=0; (j<4) && (4*i+j<13); j++) // проверям все до 13 позиции
            {
                if(fieldButtons[i][j].getText().toString().compareTo("")==0) // если пустая не на свем месте
                    return false;
                if(Integer.parseInt(fieldButtons[i][j].getText().toString())!=((4*i+j)+1))
                    return false;
            }
        // проверка на задачу Лойда
        if(fieldButtons[3][1].getText().toString().compareTo("")==0 || fieldButtons[3][2].getText().toString().compareTo("")==0)
            return false;
        if((Integer.parseInt(fieldButtons[3][1].getText().toString())!=14 || Integer.parseInt(fieldButtons[3][2].getText().toString())!=15) &&  //не 14 15
           (Integer.parseInt(fieldButtons[3][1].getText().toString())!=15 || Integer.parseInt(fieldButtons[3][2].getText().toString())!=14))    // или не 15 14
            return false;
        play=false;
        gameProgress.setText("Вы победили! Ваш счет: " + countHoop);
        if(fieldButtons[3][1].getText().toString().compareTo("15")==0)
            Toast.makeText(this,"Задача Лойда",Toast.LENGTH_LONG).show();
        if(record==0 || countHoop<record){
            record=countHoop;
            recordView.setText("Рекорд: "+record);
        }
        return true;
    }

    public boolean isEmpty(int y,int x){
        return fieldButtons[y][x].getText().toString().compareTo("")==0;
    }
    public boolean check(int y, int x, Vector vector){
        switch (vector){
            case up:
                return (y+1<=3 && isEmpty(y+1,x));
            case left:
                return (x-1>=0 && isEmpty(y,x-1));
            case right:
                return (x+1<=3 && isEmpty(y,x+1));
            case down:
                return (y-1>=0 && isEmpty(y-1,x));
        }
        return false;
    }

    public void Set(int y, int x, int y1, int x1){
        int cc=Integer.parseInt(fieldButtons[y][x].getText().toString());
        fieldButtons[y1][x1].setText(""+cc);
        fieldButtons[y][x].setText("");
        countHoop++;
        gameProgress.setText("Число ходов: "+countHoop);
    }

    public void Hoop(int y, int x){
        if(play==true) {
            if (fieldButtons[y][x].getText().toString().compareTo("") != 0) {
                if (check(y, x, Vector.right)) // Если можно вправо
                    Set(y, x, y, x + 1);
                if (check(y, x, Vector.left)) // Можно влево
                    Set(y, x, y, x - 1);
                if (check(y, x, Vector.up)) // Можно вверх
                    Set(y, x, y + 1, x);
                if (check(y, x, Vector.down)) // Можно вниз
                    Set(y, x, y - 1, x);
            }
        }
    }
    public void HoopGesture(Vector vector){
        if(play==true) {
            // поиск пустой клетк
            int x = -1, y = -1;
            //Toast.makeText(this, "Ищем 0", Toast.LENGTH_SHORT).show();
            for (int i = 0; i < 4; i++)
                for (int j = 0; j < 4; j++) {
                    if (fieldButtons[i][j].getText().toString().compareTo("") == 0) {
                        y = i;
                        x = j;
                        //Toast.makeText(this, "Координаты 0: "+x + " " + y, Toast.LENGTH_SHORT).show();
                        i = 4;
                        break;
                    }
                }
            // вычисление хода
            if (x == -1 && y == -1)
                Toast.makeText(this, "Что-то пошло не так", Toast.LENGTH_LONG).show();
            switch (vector) {
                case down:
                    if (y - 1 >= 0)
                        Set(y-1, x, y, x);
                    break;
                case right:
                    if (x - 1 >= 0)
                        Set(y, x-1, y, x);
                    break;
                case left:
                    if (x + 1 <= 3)
                        Set(y, x+1, y, x);
                    break;
                case up:
                    if (y + 1 <= 3)
                        Set(y+1, x, y, x);
                    break;
            }
        }
    }
    public void generateNewField(){
        lastField=new int[16];
        Random rnd=new Random();
        int[] digit=new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
        for(int i=0; i<16; i++){
            int j=(rnd.nextInt()%16);
            if(j<0)
                j=-j;
            while(digit[j]==-1)
                j=(j+1)%16;
            lastField[i]=digit[j];
            digit[j]=-1;
        }
    }
    public void newGame(){
        // генерация нового поля
        generateNewField();
        restart();
    }
    public void restart() {
        play=true;
        countHoop = 0;
        for(int i=0; i<16; i++) {
            if (lastField[i] == 0)
                fieldButtons[i / 4][i % 4].setText("");
            else
                fieldButtons[i / 4][i % 4].setText("" + lastField[i]);
        }
        gameProgress.setText("Игра началась");
    }
    public void deleteRecord(){
        record=9999;
        recordView.setText("Record: "+record);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putInt(KEY_RECORD, record);
        editor.apply();
    }

    void genAllElements(int sizeBut){
        cureParamsButton = new ViewGroup.LayoutParams(sizeBut, sizeBut);
        // Создаем окно
        tabLayout = new TableLayout(this);
        tabLayout.setOrientation(TableLayout.VERTICAL);
        tabLayout.setBackgroundColor(Color.rgb(00, 175, 225));
        tabLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        tabLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
            }
        });
        // создаем поле отображения статистики
        LinearLayout statLine = new LinearLayout(this);
        tabLayout.addView(statLine);
        gameProgress = new TextView(this);
        gameProgress.setTextSize(24);
        statLine.addView(gameProgress);
        linesGUI.add(statLine);
        // генерируем кнопки поля игрового
        fieldButtons = new Button[4][4];
        for (int i = 0; i < 4; i++) {
            LinearLayout linear = new LinearLayout(this);
            tabLayout.addView(linear);
            linesGUI.add(linear);
            for (int j = 0; j < 4; j++) {
                final int k = i, l = j;
                fieldButtons[i][j] = new Button(this);
                fieldButtons[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Hoop(k, l);
                        isWin();
                    }
                });
                fieldButtons[i][j].setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return gestureDetector.onTouchEvent(event);
                    }
                });
                linear.addView(fieldButtons[i][j], cureParamsButton);
            }
        }
        startGameBut = new Button(this);
        startGameBut.setText("Новая игра");
        startGameBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newGame();
            }
        });
        startGameBut.setTextSize(20);
        restartGameBut = new Button(this);
        restartGameBut.setText("Переиграть");
        restartGameBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restart();
            }
        });
        restartGameBut.setTextSize(20);
        recordView=new TextView(this);
        recordView.setTextSize(20);
        recordView.setText("Рекорд: " + record);

        sendCommandButton=new Button(this);
        sendCommandButton.setText("Дать команду");
        sendCommandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeak();
            }
        });
    }
    void genVerticalInterface() {
        LinearLayout temp=new LinearLayout(this);
        temp.setOrientation(LinearLayout.VERTICAL);
        linesGUI.add(temp);
        tabLayout.addView(temp);
        temp.addView(startGameBut, startGameButtonParam);
        temp.addView(restartGameBut, startGameButtonParam);
        setContentView(tabLayout);
        temp.addView(recordView);
        temp.addView(sendCommandButton);
    }
    void genHorizontInterface(){
        linesGUI.get(1).addView(startGameBut,startGameButtonParam);
        linesGUI.get(2).addView(restartGameBut, startGameButtonParam);
        linesGUI.get(3).addView(recordView);
        linesGUI.get(4).addView(sendCommandButton, startGameButtonParam);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Запоминаем данные
        SharedPreferences.Editor editor = mSettings.edit();
        int[] field=new int[16];
        for(int i=0; i<16; i++){
            if(fieldButtons[i/4][i%4].getText().toString().compareTo("")==0)
                field[i]=0;
            else
                field[i]=Integer.parseInt(fieldButtons[i/4][i%4].getText().toString());
        }
        editor.putInt(KEY_COUNT, countHoop);
        editor.putInt(KEY_RECORD, record);
        for(int i=0; i<16; i++){
            editor.putInt(KEY_CURE_FIELD + i, field[i]);
            editor.putInt(KEY_LAST_FIELD + i, lastField[i]);
        }
        editor.putString(KEY_TEXT, gameProgress.getText().toString());
        editor.putBoolean(KEY_PLAY, play);
        editor.putInt(KEY_RECORD, record);
        editor.apply();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mSettings.contains(KEY_RECORD)) {
            // Получаем число из настроек

            play = mSettings.getBoolean(KEY_PLAY,false);
            gameProgress.setText(mSettings.getString(KEY_TEXT,"Нажмите Новая игра"));
            countHoop = mSettings.getInt(KEY_COUNT, 0);
            record=mSettings.getInt(KEY_RECORD,0);
            recordView.setText("Рекорд: "+record);
            int[] field=new int[16];
            lastField=new int[16];
            for(int i=0; i<16; i++){
                lastField[i]=mSettings.getInt(KEY_LAST_FIELD+i,0);
                field[i]=mSettings.getInt(KEY_CURE_FIELD+i,0);
            }
            for(int i=0; i<16; i++){
                if(field[i]==0)
                    fieldButtons[i/4][i%4].setText("");
                else
                    fieldButtons[i/4][i%4].setText(""+field[i]);
            }

            record = mSettings.getInt(KEY_RECORD, 0);
            recordView.setText("Рекорд: "+record);
        }
        else
            newGame();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        int[] field=new int[16];
        for(int i=0; i<16; i++){
            if(fieldButtons[i/4][i%4].getText().toString().compareTo("")==0)
                field[i]=0;
            else
                field[i]=Integer.parseInt(fieldButtons[i/4][i%4].getText().toString());
        }
        outState.putInt(KEY_COUNT, countHoop);
        outState.putInt(KEY_RECORD, record);
        outState.putIntArray(KEY_CURE_FIELD, field);
        outState.putIntArray(KEY_LAST_FIELD, lastField);
        outState.putString(KEY_TEXT, gameProgress.getText().toString());
        outState.putBoolean(KEY_PLAY, play);
    }
    protected void checkSaveState(Bundle savedInstanceState){
        if (savedInstanceState != null) { // Если была только смена положения экрана
            play = savedInstanceState.getBoolean(KEY_PLAY);
            gameProgress.setText(savedInstanceState.getString(KEY_TEXT));
            countHoop = savedInstanceState.getInt(KEY_COUNT, 0);
            record=savedInstanceState.getInt(KEY_RECORD,0);
            recordView.setText("Рекорд: "+record);
            lastField=savedInstanceState.getIntArray(KEY_LAST_FIELD);
            int[] field=savedInstanceState.getIntArray(KEY_CURE_FIELD);

            for(int i=0; i<16; i++){
                if(field[i]==0)
                    fieldButtons[i/4][i%4].setText("");
                else
                    fieldButtons[i/4][i%4].setText(""+field[i]);
            }
        }
        else // если запустили приложение
            newGame();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTitle("Пятнашки");
        mSettings = getSharedPreferences(KEY_RECORD, Context.MODE_PRIVATE);
        gestureDetector = initGestureDetector();
        // Вычисляем размеры экрана
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        // Генерация интерфейса
        if(metrics.widthPixels>metrics.heightPixels){ // если горизонтальная ориентация
            genAllElements((int) (metrics.heightPixels / 4.5));
            genHorizontInterface();
        }
        else{
            genAllElements(metrics.widthPixels/4);
            genVerticalInterface();
        }
        setContentView(tabLayout);
        // Убираем панель уведомлений
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // проверяем на наличие данных
        checkSaveState(savedInstanceState);
    }

    // Говорилка
    public void startSpeak() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // намерение для вызова формы обработки речи (ОР)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); // сюда он слушает и запоминает

        //intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "What can you tell me?");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE); // вызываем активность ОР
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList commandList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            // для лучшего распознавания английского языка, поставьте в настройках англ. яз как язык системы
            // хотя все то же самое можно проделать и с русскими словами
            if (commandList.contains("верх")) {
                HoopGesture(Vector.up);
            }

            if (commandList.contains("вниз")) {
                HoopGesture(Vector.down);
            }

            if (commandList.contains("влево") || commandList.contains("слева") || commandList.contains("лево")) {
                HoopGesture(Vector.left);
            }

            if (commandList.contains("вправо") || commandList.contains("права") || commandList.contains("право") || commandList.contains("в право")) {
                HoopGesture(Vector.right);
            }

            // выйти
            if (commandList.contains("новая")) {
                newGame();
            }
            // попробуем открыть гугловские карты
            if (commandList.contains("перезапустить")) {
                restart();
            }
            if (commandList.contains("сбросить") || commandList.contains("сброс")) {
                deleteRecord();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }
    //Жесты
    private static final int SWIPE_MIN_DISTANCE = 80;
    private static final int SWIPE_THRESHOLD_VELOCITY = 150;
    public class SwipeDetector { // вспомогательный класс для обработки жестов

        private int swipe_distance;
        private int swipe_velocity;
        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        public SwipeDetector(int distance, int velocity) {
            super();
            this.swipe_distance = distance;
            this.swipe_velocity = velocity;
        }

        public SwipeDetector() {
            super();
            this.swipe_distance = SWIPE_MIN_DISTANCE;
            this.swipe_velocity = SWIPE_THRESHOLD_VELOCITY;
        }

        public boolean isSwipeDown(MotionEvent e1, MotionEvent e2, float velocityY) {
            return isSwipe(e2.getY(), e1.getY(), velocityY);
        }

        public boolean isSwipeUp(MotionEvent e1, MotionEvent e2, float velocityY) {
            return isSwipe(e1.getY(), e2.getY(), velocityY);
        }

        public boolean isSwipeLeft(MotionEvent e1, MotionEvent e2, float velocityX) {
            return isSwipe(e1.getX(), e2.getX(), velocityX);
        }

        public boolean isSwipeRight(MotionEvent e1, MotionEvent e2, float velocityX) {
            return isSwipe(e2.getX(), e1.getX(), velocityX);
        }

        private boolean isSwipeDistance(float coordinateA, float coordinateB) {
            return (coordinateA - coordinateB) > this.swipe_distance;
        }

        private boolean isSwipeSpeed(float velocity) {
            return Math.abs(velocity) > this.swipe_velocity;
        }

        private boolean isSwipe(float coordinateA, float coordinateB, float velocity) {
            return isSwipeDistance(coordinateA, coordinateB)
                    && isSwipeSpeed(velocity);
        }
    }
    private GestureDetector initGestureDetector() { // регистратор жестов
        return new GestureDetector(new GestureDetector.SimpleOnGestureListener() {

            private SwipeDetector detector = new SwipeDetector();

            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                   float velocityY) {
                try {
                    if (detector.isSwipeDown(e1, e2, velocityY)) {
                        HoopGesture(Vector.down);
                    } else if (detector.isSwipeUp(e1, e2, velocityY)) {
                        HoopGesture(Vector.up);
                    } else if (detector.isSwipeLeft(e1, e2, velocityX)) {
                        HoopGesture(Vector.left);
                    } else if (detector.isSwipeRight(e1, e2, velocityX)) {
                        HoopGesture(Vector.right);
                    }
                } catch (Exception e) {} //for now, ignore
                return false;
            }
        });
    }
}
