package com.example.deliveryis;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.CollationElementIterator;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class PedidosActivity extends AppCompatActivity {
    public static final String URL_IMAGE = "http://192.168.0.104/restaurant/deliveryempleados/";
    public static final String URL_LISTAPEDIDOS = "http://192.168.0.104/restaurant/consult/ListaPedidosAndroidStudio";
    public static final String URL_IMAGE_CLIENT = "http://192.168.0.104/restaurant/deliveryempleados/tipoclienteImage/";

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

    TextView nombre;
    String nombremotorizado = "";
    Button logout;

    String idmotorizado = null;

    // - LISTADO DE PEDIDOS
    // https://www.youtube.com/watch?v=8wJ98WvWwK8
    String[] name;
    String[] idpedido;
    String[] dni;
    String[] hora;
    String[] estado;
    String[] email;
    String[] imagepath;
    String[] lat_lon;

    ListView listView;
    BufferedInputStream is;
    String line=null;
    String result=null;
    // ---------------------

    // Recargar Loyout each time
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 15000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos);
        sharedPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

        idmotorizado = sharedPreferences.getString(ID,"");
        nombremotorizado = sharedPreferences.getString(USERNAME,"");

        // - LISTADO DE PEDIDOS
        listView=(ListView)findViewById(R.id.lview);
        StrictMode.setThreadPolicy((new StrictMode.ThreadPolicy.Builder().permitNetwork().build()));
        collectData();
        CustomListView customListView=new CustomListView(this,name,email,imagepath,idpedido,dni,hora,estado,lat_lon);
        listView.setAdapter(customListView);
        // ---------------------

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view.findViewById(R.id.tvidpedido);
                TextView LatiLong = (TextView) view.findViewById(R.id.tvlatlon);
                String text = textView.getText().toString();
                String LatL = LatiLong.getText().toString();

                Intent i = new Intent(PedidosActivity.this, RepartoActivity.class);
                i.putExtra("idpedido",text);
                i.putExtra("nommotorizado",nombremotorizado);
                i.putExtra("idmotorizado",idmotorizado);
                i.putExtra("lat_lon", LatL);
                startActivity(i);
                //System.out.println("Choosen idpedido = : " + text);

            }});


        // Cargar Imagen desde una url, se debe instalar picasso en build.gradle
        // implementation 'com.squareup.picasso:picasso:2.71828'
        ImageView imageView = findViewById(R.id.fotoperfil);
        String imageUrl = URL_IMAGE + sharedPreferences.getString(FOTO,"");
        Picasso.get().load(imageUrl).into(imageView);
        // Fin cargar imagen al ImageView

        nombre = findViewById(R.id.nombrecomplete);
        nombre.setText(sharedPreferences.getString(USERNAME,"").toUpperCase());

        logout = findViewById(R.id.logout);

        logout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                finish();
                Intent intent = new Intent(PedidosActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }



    protected void onResume() {
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, delay);

                // - LISTADO DE PEDIDOS
                StrictMode.setThreadPolicy((new StrictMode.ThreadPolicy.Builder().permitNetwork().build()));
                collectData();
                CustomListView customListView=new CustomListView(PedidosActivity.this,name,email,imagepath,idpedido,dni,hora,estado,lat_lon);
                listView.setAdapter(customListView);
                // ---------------------
            }
        }, delay);
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable); //stop handler when activity not visible super.onPause();
    }



    // - LISTADO DE PEDIDOS
    private void collectData()
    {
        //Connection
        try{

            URL url=new URL(URL_LISTAPEDIDOS + "?idmotorizado=" + idmotorizado);
            HttpURLConnection con=(HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            is=new BufferedInputStream(con.getInputStream());

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        //content
        try{
            BufferedReader br=new BufferedReader(new InputStreamReader(is));
            StringBuilder sb=new StringBuilder();
            while ((line=br.readLine())!=null){
                sb.append(line+"\n");
            }
            is.close();
            result=sb.toString();

        }
        catch (Exception ex)
        {
            ex.printStackTrace();

        }

        //JSON
        try{
            JSONArray ja=new JSONArray(result);
            JSONObject jo=null;
            name=new String[ja.length()];
            email=new String[ja.length()];
            imagepath=new String[ja.length()];
            idpedido=new String[ja.length()];
            dni=new String[ja.length()];
            hora=new String[ja.length()];
            estado=new String[ja.length()];
            lat_lon=new String[ja.length()];

            for(int i=0;i<=ja.length();i++){
                jo=ja.getJSONObject(i);

                System.out.println(jo.getString("lat_lon"));
                name[i]=jo.getString("name");
                email[i]=jo.getString("email");
                imagepath[i]=URL_IMAGE_CLIENT + jo.getString("photo");
                idpedido[i]=jo.getString("idpedido");
                dni[i]=jo.getString("dni");
                hora[i]=jo.getString("hora");
                estado[i]=jo.getString("estado");
                lat_lon[i]=jo.getString("lat_lon");
            }
        }
        catch (Exception ex)
        {

            ex.printStackTrace();
        }


    }
    // ---------------------

}
