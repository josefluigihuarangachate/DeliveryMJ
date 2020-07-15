package com.example.deliveryis;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    // https://technopoints.co.in/android-login-registration-using-php-and-mysql/

    public static final String URL_LOGIN = "http://192.168.0.104/restaurant/consult/LoginDeliveryAndroidStudio";
    EditText ed_email, ed_password;
    SharedPreferences sharedPreferences;
    public static final String MY_PREFERENCES = "MyPrefs";
    public static final String EMAIL = "email";
    public static final String STATUS = "status";
    public static final String USERNAME = "username";
    public static final String ID = "id";
    public static final String DNI = "dni";
    public static final String PLACA = "placa";
    public static final String CELULAR = "celular";
    public static final String FOTO = "foto";
    public static final String BLOB = "blob";

    private boolean status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ed_email = findViewById(R.id.ed_email);
        ed_password = findViewById(R.id.ed_password);

        sharedPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

        status = sharedPreferences.getBoolean(STATUS, false);

        if (status){
            finish();
            Intent intent = new Intent(MainActivity.this, PedidosActivity.class);
            startActivity(intent);
        }
    }


    public void login(View view){
        final String email = ed_email.getText().toString();
        final String password = ed_password.getText().toString();

        if(email.isEmpty()|| password.isEmpty()){
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
        }else {
            class Login extends AsyncTask<Void, Void, String> {
                ProgressDialog pdLoading = new ProgressDialog(MainActivity.this);

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    //this method will be running on UI thread
                    pdLoading.setMessage("\tCargando...");
                    pdLoading.setCancelable(false);
                    pdLoading.show();
                }

                @Override
                protected String doInBackground(Void... voids) {
                    //creating request handler object
                    RequestHandler requestHandler = new RequestHandler();

                    //creating request parameters
                    HashMap<String, String> params = new HashMap<>();
                    params.put("email", email);
                    params.put("password", password);

                    //returing the response
                    return requestHandler.sendPostRequest(URL_LOGIN, params);
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    pdLoading.dismiss();

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(s);
                        //if no error in response
                        if (!obj.getBoolean("error")) {
                            System.out.println(obj.getString("id"));
                            String id = obj.getString("id");
                            String dni = obj.getString("dni");
                            String placa = obj.getString("placa");
                            String celular = obj.getString("celular");
                            String foto = obj.getString("foto");
                            String email = obj.getString("email");
                            String username = obj.getString("username");

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(ID, id);
                            editor.putString(DNI, dni);
                            editor.putString(PLACA, placa);
                            editor.putString(CELULAR, celular);
                            editor.putString(FOTO, foto);
                            editor.putString(USERNAME, username);
                            editor.putString(EMAIL, email);
                            editor.putBoolean(STATUS, true);
                            editor.apply();

                            finish();
                            Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(MainActivity.this, PedidosActivity.class);
                            startActivity(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Hubo un error", Toast.LENGTH_LONG).show();
                    }
                }
            }

            Login login = new Login();
            login.execute();
        }
    }
}
