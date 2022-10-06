package com.example.a15squarepuzzle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int size = 4;
    private GameMatrix gameMatrix;
    private int[][] id;
    private AppCompatButton[][] buttons;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        init();
        updateBoard(new GameMatrix(size));
    }

    @SuppressLint("ClickableViewAccessibility")
    public void init() {
        id = new int[size][size];

        fillIdMatrix(id);

        buttons = new AppCompatButton[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                buttons[i][j] = findViewById(id[i][j]);
                buttons[i][j].setOnClickListener(this);
                buttons[i][j].setOnTouchListener(new OnSwipeTouchListener(this));
            }
        }
        findViewById(R.id.shuffle_btn).setOnClickListener(view -> {
            updateBoard(new GameMatrix(size));
        });
    }


    public void fillIdMatrix(int[][] id) {
        id[0][0] = R.id.btn00;
        id[0][1] = R.id.btn01;
        id[0][2] = R.id.btn02;
        id[0][3] = R.id.btn03;

        id[1][0] = R.id.btn10;
        id[1][1] = R.id.btn11;
        id[1][2] = R.id.btn12;
        id[1][3] = R.id.btn13;

        id[2][0] = R.id.btn20;
        id[2][1] = R.id.btn21;
        id[2][2] = R.id.btn22;
        id[2][3] = R.id.btn23;

        id[3][0] = R.id.btn30;
        id[3][1] = R.id.btn31;
        id[3][2] = R.id.btn32;
        id[3][3] = R.id.btn33;
    }

    private void updateTile(int positionX, int positionY, int value) {
        int lightColor = getResources().getColor(R.color.light);
        int backgroundColor = getResources().getColor(R.color.background);

        String text, altText;
        int color;

        if (value == 0) {
            text = "";
            altText = "Empty Tile";
            color = lightColor;
        } else {
            text = value + "";
            altText = "Tile " + value;
            color = backgroundColor;
        }

        Log.d("Umar", "updateTile: " + positionX + " " + positionY);

        buttons[positionX][positionY].setText(text);
        buttons[positionX][positionY].setBackgroundTintList(ColorStateList.valueOf(color));
        buttons[positionX][positionY].setContentDescription(altText);
        if(isTileOnCorrectPosition(positionX, positionY, value)){
            buttons[positionX][positionY].setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.correct_color)));
        }
    }

    public void updateBoard(GameMatrix gameMatrix) {
        this.gameMatrix = gameMatrix;
        int size = gameMatrix.getSize();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (gameMatrix.isEmpty(i, j)) {
                    updateTile(i, j, 0);
                } else {
                    updateTile(i, j, gameMatrix.get(i, j));
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        int i, j = 0;

        // Get button's coordinates using id matrix
        label:
        for (i = 0; i < size; i++) {
            for (j = 0; j < size; j++) {
                if (v.getId() == id[i][j])
                    break label;
            }
        }
        makeMove(i - gameMatrix.getEmptyCellRow(), j - gameMatrix.getEmptyCellCol());
    }

    public void wonGame() {
        Toast.makeText(this, "You won", Toast.LENGTH_LONG).show();
        startNewGame();
    }

    public void startNewGame() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Do you want to play new game?");
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                updateBoard(new GameMatrix(size));
            }
        });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    private boolean isValidPosition(int rowIndex, int colIndex) {
        return rowIndex >= 0 && rowIndex < size && colIndex >= 0 && colIndex < size;
    }

    public class OnSwipeTouchListener implements View.OnTouchListener,
            SwipeGestureListener.OnSwipeInterface {
        private final GestureDetector gestureDetector;

        private int[] rowMoves = {0, 0, 1, -1};
        private int[] colMoves = {-1, 1, 0, 0};

        OnSwipeTouchListener(Context ctx) {
            gestureDetector = new GestureDetector(ctx, new SwipeGestureListener(this));
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        @Override
        public void onSwipeRight() {
            makeMove(rowMoves[0], colMoves[0]);
        }

        @Override
        public void onSwipeLeft() {
            makeMove(rowMoves[1], colMoves[1]);
        }

        @Override
        public void onSwipeTop() {
            makeMove(rowMoves[2], colMoves[2]);
        }

        @Override
        public void onSwipeBottom() {
            makeMove(rowMoves[3], colMoves[3]);
        }
    }

    private void makeMove(int rowMove, int colMove) {
        // abs sum ensures that only one of the rowMove or colMove is 1 or -1
        if (Math.abs(rowMove) + Math.abs(colMove) != 1) return;
        int newEmptyRowIndex = gameMatrix.getEmptyCellRow() + rowMove;
        int newEmptyColIndex = gameMatrix.getEmptyCellCol() + colMove;

        if (!isValidPosition(newEmptyRowIndex, newEmptyColIndex)) return;

        // swapping of buttons
        int i = gameMatrix.getEmptyCellRow(), j = gameMatrix.getEmptyCellCol();

        updateTile(i, j, gameMatrix.get(newEmptyRowIndex, newEmptyColIndex));
        updateTile(newEmptyRowIndex, newEmptyColIndex, 0);

        gameMatrix.swap(i, j, newEmptyRowIndex, newEmptyColIndex);

        if (gameMatrix.isSolved()) {
            wonGame();
        }
    }
    private boolean isTileOnCorrectPosition(int x,int y,int value){
        boolean correct = false;
        if(x==0&&y==0&&value==1){
            correct = true;
        }else if(x==0&&y==1&&value==2){
            correct = true;
        }else if(x==0&&y==2&&value==3){
            correct = true;
        }else if(x==0&&y==3&&value==4){
            correct = true;
        }else if(x==1&&y==0&&value==5){
            correct = true;
        }else if(x==1&&y==1&&value==6){
            correct = true;
        }else if(x==1&&y==2&&value==7){
            correct = true;
        }else if(x==1&&y==3&&value==8){
            correct = true;
        }else if(x==2&&y==0&&value==9){
            correct = true;
        }else if(x==2&&y==1&&value==10){
            correct = true;
        }else if(x==2&&y==2&&value==11){
            correct = true;
        }else if(x==2&&y==3&&value==12){
            correct = true;
        }else if(x==3&&y==0&&value==13){
            correct = true;
        }else if(x==3&&y==1&&value==14){
            correct = true;
        }else if(x==3&&y==2&&value==15){
            correct = true;
        }else if(x==3&&y==3&&value==16){
            correct = true;
        }
        return correct;
    }
}