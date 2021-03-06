package com.example.olio_project;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    Context context = this;
    ArrayList<User> userList = new ArrayList<User>();



    Pattern hasSmallLetters = Pattern.compile("[a-z]");
    Pattern hasCapitalLetters = Pattern.compile("[a-z]");
    Pattern hasNumber = Pattern.compile("[0-9]");
    Pattern hasSpecial = Pattern.compile ("[!@#$%&*()_+=|<>?{}\\[\\]~-]");



    EditText usernameField;
    EditText passwordField;
    TextView errorMsg;
    Button newUserButton;



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);
        newUserButton = findViewById(R.id.createUser);
        errorMsg = findViewById(R.id.errorMsg);
        //See if login file exists on the phone already, if not, create one. NullPointerException detects corrupted data and deletes the offending file
        try {
            readFile();
        }
        catch (NullPointerException e){
            deleteFile("users.txt");
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public LocalDate getThisWeeksMonday(){
        LocalDate thisWeeksMonday = LocalDate.now();

        while(thisWeeksMonday.getDayOfWeek().toString() != "MONDAY"){
            thisWeeksMonday = thisWeeksMonday.minusDays(1);
        }
        return (thisWeeksMonday);
    }

    public void login(View v){
        if(userList.size() == 0){
            System.out.println("Userlist size="+userList.size());
            Toast.makeText(context,"No users exist, create a new user!", Toast.LENGTH_SHORT).show();
        }
        else {
            int i = 0;
            for (i = 0; i < userList.size(); i++) {

                if(userList.get(i).userName.equals(usernameField.getText().toString()) == true && userList.get(i).password.equals(passwordField.getText().toString()) == true){
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("Index",i);
                    System.out.println("###USERINDEX in loginactivity:"+i);
                    startActivity(intent);
                    break;
                }
                else{
                    System.out.println("Userlistan nimi: "+userList.get(i).userName+" annettu nimi:"+usernameField.getText().toString());
                    System.out.println("Userlistan salasana: "+userList.get(i).password+" annettu salasana: "+passwordField.getText().toString());
                }

            }
            if(userList.size() == i){
                Toast.makeText(context,"Username or password is incorrect.", Toast.LENGTH_SHORT).show();
                errorMsg.setText("");
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addNewUser(View v){

        if(usernameField.getText().length() != 0 && passwordField.getText().length() != 0) {                                    //Check if user has inputted anything into both fields
            User newUser = new User(usernameField.getText().toString(), passwordField.getText().toString());
            for (int i = 0; i < userList.size(); i++) {
                if(userList.get(i).userName.equals(newUser.userName) == true){
                    System.out.println("Username already exists! Password in case you forgot: "+userList.get(i).password);
                    Toast.makeText(this,"Username already exists!",Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Matcher smallLetterMatcher = hasSmallLetters.matcher(passwordField.getText().toString());
            Matcher capitalLetterMatcher = hasCapitalLetters.matcher(passwordField.getText().toString());
            Matcher numberMatcher = hasNumber.matcher(passwordField.getText().toString());
            Matcher specialMatcher = hasSpecial.matcher(passwordField.getText().toString());
            boolean smallLetters = smallLetterMatcher.find();
            boolean capitalLetters = capitalLetterMatcher.find();
            boolean numbers = numberMatcher.find();
            boolean specials = specialMatcher.find();

            if(smallLetters == true && capitalLetters == true && numbers == true && specials == true && passwordField.getText().toString().length() >= 12) {
                userList.add(newUser);
                writeUserListToFile(userList);
                Toast.makeText(this, "New user added!", Toast.LENGTH_SHORT).show();
                errorMsg.setText("");
            }
            else{
                errorMsg.setText("Password needs to have one small letter, one capital letter, one digit, one special character and be 12 characters long!");
                return;
            }
        }
        else{
            System.out.println("Username or password cannot be empty!");
            Toast.makeText(this,"Username or password cannot be empty!",Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void readFile(){
        try{
            FileInputStream fis = context.openFileInput("users.txt");
            boolean cont = true;
            ObjectInputStream ois = new ObjectInputStream(fis);
            while(cont){
                try {
                    Object object = ois.readObject();
                    if (object != null) {
                        System.out.println("User found");
                        userList.add((User) object);
                    } else {

                    }
                }
                catch (EOFException e){
                    cont = false;
                    fis.close();
                    System.out.println("File read! Found users:");
                    for (int i = 0; i < userList.size(); i++) {
                        System.out.println(userList.get(i).userName);
                    }
                }
            }
            ois.close();
            fis.close();
        }
        //Do this if no file exists
        catch(FileNotFoundException e){
            Log.e("FileNotFoundException","File not found, creating a new userfile!");
            //User newUser = new User("Ketsuppimakkara","1234");  //TODO Get username and password from UI
            //userList.add(newUser);
            User secondUser = new User("Admin","1234");  //TODO Get username and password from UI
            userList.add(secondUser);
            writeUserListToFile(userList);
            //System.out.println(userList.get(0).userName);
            //System.out.println(userList.get(1).userName);

        }
        catch(IOException e){
            Log.e("IOException",e.toString());
        } catch (ClassNotFoundException e) {
            Log.e("IOException","Class not found");
        }
        }

    public void writeUserListToFile(ArrayList<User> writeUsers) {
        try {
            FileOutputStream fos = getApplicationContext().openFileOutput("users.txt",Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            int i = 0;
            while(i < writeUsers.size()) {
                oos.writeObject(writeUsers.get(i));
                System.out.println("Wrote "+writeUsers.get(i).userName+" to file.");
                i++;
            }
            oos.close();
            fos.close();
        }
        catch(FileNotFoundException e){
            Log.e("FileNotFoundException","File not found");
        }
        catch(IOException e){
            Log.e("IOException","Error in input");
        }
    }
    public ArrayList<User> getUserList(){
        return userList;
    }
}