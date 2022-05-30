package com.example.phinma_upangstudentsupportmodule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AssessmentActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    private boolean finished;
    private DataSnapshot snap;

    //Total questions and main question TextView
    private TextView questions;
    private TextView question;

    //options
    private AppCompatButton option1, option2, option3, option4, option5;

    //next button
    private AppCompatButton nextBtn;

    //questions array list
    private List<QuestionsList> questionsLists;

    //Current questions index position from questionsLists ArrayList
    private int currentQuestionPosition = 0;

    //selectedOption's value. If user not selected any option yet then it is empty by default
    private String selectedOptionByUser = "";

    //background music
    private MediaPlayer mediaPlayer;

    String[] questionBank;
    int cnt = 0;
    String getSelectedTestName;
    private static double physicalScore, currentScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment);

        progressDialog = ProgressDialog.show(AssessmentActivity.this, null, null);
        progressDialog.setContentView(new ProgressBar(AssessmentActivity.this));
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //for animated background
        LinearLayout linearLayout = findViewById(R.id.mainLayout);

        AnimationDrawable animationDrawable = (AnimationDrawable) linearLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2500);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();

        //for background music
        mediaPlayer = MediaPlayer.create(AssessmentActivity.this, R.raw.music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();


        //Initialize widgets from activity_main.xml file
        final ImageView backBtn = findViewById(R.id.backBtn);
        final TextView selectedTestName = findViewById(R.id.AssessmentName);
        questions = findViewById(R.id.questions);
        question = findViewById(R.id.question);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        option5 = findViewById(R.id.option5);
        nextBtn = findViewById(R.id.nextBtn);

        // DEBUG: The data transferred is a static "physical". Make it dynamic by using radio buttons or selection.
        //Get test name from MainActivity via Intent
        getSelectedTestName = getIntent().getStringExtra("selectedTest"); //Either Physical or Mental;
        if(getSelectedTestName.equals("mental")) {
            physicalScore = getIntent().getDoubleExtra("physicalScore", 0);
        }

        //Set test name to TextView
        selectedTestName.setText(getSelectedTestName.toUpperCase());

        Log.d("AssessmentAct(89): gSTN", getSelectedTestName);

        /*
        //Get questions from Firebase Database according to the selectedTestName and assign questionsLists ArrayList
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://assess-module-default-rtdb.firebaseio.com/");

        //show dialog while questions are being fetched
        ProgressDialog progressDialog = new ProgressDialog(AssessmentActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //Getting all questions from Firebase Database for a specific test
                for (DataSnapshot dataSnapshot : snapshot.child(getSelectedTestName).getChildren()){

                    //Getting data from Firebase Database
                    final String getQuestion = dataSnapshot.child("question").getValue(String.class);
                    final String getOption1 = dataSnapshot.child("option1").getValue(String.class);
                    final String getOption2 = dataSnapshot.child("option2").getValue(String.class);
                    final String getOption3 = dataSnapshot.child("option3").getValue(String.class);
                    final String getOption4 = dataSnapshot.child("option4").getValue(String.class);

                    //Adding data to the questionsLists
                    QuestionsList questionsList = new QuestionsList(getQuestion, getOption1, getOption2, getOption3, getOption4);
                    questionsLists.add(questionsList);
                }
                //hide dialog
                progressDialog.hide();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        */

        //Get Questions from Database
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("tbank");
        finished = false;
        questionsLists = new ArrayList<>();

        switch (getSelectedTestName) {
            case "physical":
                dbRef.child("physicalq").get().addOnCompleteListener(task -> {
                    finished = true;
                    snap = task.getResult();

                    questionBank = new String[(int) snap.getChildrenCount()];

                    for(DataSnapshot questions : snap.getChildren()) {
                        questionBank[cnt++] = questions.getValue().toString();
                    }

                    for(int x = 0; x<10; x++) {
                        Random generator = new Random();
                        int randomIndex = generator.nextInt(questionBank.length);

                        if(questionBank[randomIndex] == "selected") {
                            x--;
                            continue;
                        }

                        questionsLists.add(new QuestionsList(questionBank[randomIndex], "Always", "Often", "Sometimes", "Occasionally", "Never", ""));
                        questionBank[randomIndex] = "selected";
                    }

                    questions.setText((currentQuestionPosition + 1) + "/" + questionsLists.size());
                    question.setText(questionsLists.get(0).getQuestion());
                    option1.setText(questionsLists.get(0).getOption1());
                    option2.setText(questionsLists.get(0).getOption2());
                    option3.setText(questionsLists.get(0).getOption3());
                    option4.setText(questionsLists.get(0).getOption4());
                    option5.setText(questionsLists.get(0).getOption5());

                    progressDialog.dismiss();
                });
                break;

            default:
                dbRef.child("mentalq").get().addOnCompleteListener(task -> {
                    finished = true;
                    snap = task.getResult();

                    questionBank = new String[(int) snap.getChildrenCount()];

                    for(DataSnapshot questions : snap.getChildren()) {
                        questionBank[cnt++] = questions.getValue().toString();
                    }

                    for(int x = 0; x<10; x++) {
                        Random generator = new Random();
                        int randomIndex = generator.nextInt(questionBank.length);

                        if(questionBank[randomIndex] == "selected") {
                            x--;
                            continue;
                        }

                        questionsLists.add(new QuestionsList(questionBank[randomIndex], "Not Present", "Mild", "Moderate", "Severe", "Extremely Severe", ""));
                        questionBank[randomIndex] = "selected";
                    }

                    questions.setText((currentQuestionPosition + 1) + "/" + questionsLists.size());
                    question.setText(questionsLists.get(0).getQuestion());
                    option1.setText(questionsLists.get(0).getOption1());
                    option2.setText(questionsLists.get(0).getOption2());
                    option3.setText(questionsLists.get(0).getOption3());
                    option4.setText(questionsLists.get(0).getOption4());
                    option5.setText(questionsLists.get(0).getOption5());

                    progressDialog.dismiss();
                });
                break;
        }

        //Set current questions to TextView along with options from questionsLists ArrayList
//        while(questionsLists.isEmpty()) {
//            Log.d("Weee", ":<");
//        }

        option1.setOnClickListener(v -> {
            //check if user has not attempted this question yet
            if (selectedOptionByUser.isEmpty()){
                selectedOptionByUser = option1.getText().toString();

                //change selected AppCompatButton background color and text color
                option1.setBackgroundResource(R.drawable.round_back_red10);
                option1.setTextColor(Color.WHITE);

                //assign user answer value to userSelectedOption in QuestionsList class
                questionsLists.get(currentQuestionPosition).setUserSelectedAnswer(selectedOptionByUser);
            }
        });

        option2.setOnClickListener(v -> {
            //check if user has not attempted this question yet
            if (selectedOptionByUser.isEmpty()){
                selectedOptionByUser = option2.getText().toString();

                //change selected AppCompatButton background color and text color
                option2.setBackgroundResource(R.drawable.round_back_red10);
                option2.setTextColor(Color.WHITE);

                //assign user answer value to userSelectedOption in QuestionsList class
                questionsLists.get(currentQuestionPosition).setUserSelectedAnswer(selectedOptionByUser);

            }

        });

        option3.setOnClickListener(v -> {
            //check if user has not attempted this question yet
            if (selectedOptionByUser.isEmpty()){
                selectedOptionByUser = option3.getText().toString();

                //change selected AppCompatButton background color and text color
                option3.setBackgroundResource(R.drawable.round_back_red10);
                option3.setTextColor(Color.WHITE);

                //assign user answer value to userSelectedOption in QuestionsList class
                questionsLists.get(currentQuestionPosition).setUserSelectedAnswer(selectedOptionByUser);

            }

        });

        option4.setOnClickListener(v -> {
            //check if user has not attempted this question yet
            if (selectedOptionByUser.isEmpty()){
                selectedOptionByUser = option4.getText().toString();

                //change selected AppCompatButton background color and text color
                option4.setBackgroundResource(R.drawable.round_back_red10);
                option4.setTextColor(Color.WHITE);

                //assign user answer value to userSelectedOption in QuestionsList class
                questionsLists.get(currentQuestionPosition).setUserSelectedAnswer(selectedOptionByUser);

            }

        });

        option5.setOnClickListener(v -> {
            //check if user has not attempted this question yet
            if (selectedOptionByUser.isEmpty()){
                selectedOptionByUser = option5.getText().toString();

                //change selected AppCompatButton background color and text color
                option5.setBackgroundResource(R.drawable.round_back_red10);
                option5.setTextColor(Color.WHITE);

                //assign user answer value to userSelectedOption in QuestionsList class
                questionsLists.get(currentQuestionPosition).setUserSelectedAnswer(selectedOptionByUser);

            }

        });

        nextBtn.setOnClickListener(v -> {
            //check if user has not selected any option yet
            if (selectedOptionByUser.isEmpty()) {
                Toast.makeText(AssessmentActivity.this, "Please select an option", Toast.LENGTH_SHORT).show();
            } else {
                changeNextQuestion();
            }

        });

        backBtn.setOnClickListener(v -> {
            startActivity(new Intent(AssessmentActivity.this, MainActivity.class));
            finish();

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AssessmentActivity.this, MainActivity.class));
        finish();
    }

    private void changeNextQuestion(){
        currentQuestionPosition++;

        int sel;
        switch (selectedOptionByUser) {
            case "Always":
                sel = 1;
                break;
            case "Often":
                sel = 2;
                break;
            case "Sometimes":
                sel = 3;
                break;
            case "Occasionally":
                sel = 4;
                break;
            case "Never":
                sel = 5;
                break;
            default:
                sel = 0;
        }

        currentScore += sel;
        Log.d("Score", String.valueOf(currentScore));
        if ((currentQuestionPosition+1) == questionsLists.size()) {
            nextBtn.setText("Submit Assessment");
        }

        if ((currentQuestionPosition < questionsLists.size())) {

            selectedOptionByUser = "";

            option1.setBackgroundResource(R.drawable.round_back_white_stroke10);
            option1.setTextColor(Color.parseColor("#1F6BB8"));

            option2.setBackgroundResource(R.drawable.round_back_white_stroke10);
            option2.setTextColor(Color.parseColor("#1F6BB8"));

            option3.setBackgroundResource(R.drawable.round_back_white_stroke10);
            option3.setTextColor(Color.parseColor("#1F6BB8"));

            option4.setBackgroundResource(R.drawable.round_back_white_stroke10);
            option4.setTextColor(Color.parseColor("#1F6BB8"));

            option5.setBackgroundResource(R.drawable.round_back_white_stroke10);
            option5.setTextColor(Color.parseColor("#1F6BB8"));

            questions.setText((currentQuestionPosition+1)+"/"+questionsLists.size());
            question.setText(questionsLists.get(currentQuestionPosition).getQuestion());
            option1.setText(questionsLists.get(currentQuestionPosition).getOption1());
            option2.setText(questionsLists.get(currentQuestionPosition).getOption2());
            option3.setText(questionsLists.get(currentQuestionPosition).getOption3());
            option4.setText(questionsLists.get(currentQuestionPosition).getOption4());
            option5.setText(questionsLists.get(currentQuestionPosition).getOption5());
        }
        else {
//            DEVLOG: COMPUTE AVERAGE FOR TEST AND PASS AS INTENT
            double score = (currentScore/50)*100;

            Intent intent;
            if(getSelectedTestName.equals("physical")) {
                intent = new Intent(AssessmentActivity.this, AssessmentActivity.class);
                intent.putExtra("selectedTest", "mental");
                physicalScore = score;
            }
            else {
                intent = new Intent(AssessmentActivity.this, AssessmentResults.class);
                intent.putExtra("mentalScore", score);
            }

            intent.putExtra("physicalScore", physicalScore);
            currentScore = 0;
            startActivity(intent);

            finish();
        }
    }
}