package com.example.deliveryis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.core.location.LocationManagerCompat;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RepartoActivity extends AppCompatActivity {

    private String mapurlTracker = "http://192.168.0.104/restaurant/AndroidStudioTrackerDelivery.php?idpedido=";
    private String updateLocationMotorizado = "http://192.168.0.104/restaurant/consult/deliveryLatLon.php";

    private WebView webView;
    String nommotorizado = "";
    String idmotorizado = "";
    String lat_lon = "";
    Button btnWaze;

    // lat_lon
    private TextView lat_tv;
    private TextView lon_tv;
    private final String TAG = "RepartoActivity";
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    // -------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reparto);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("idpedido");
            lat_lon = extras.getString("lat_lon");
            //System.out.println("idpedido recibido = : " + lat_lon);

            nommotorizado = extras.getString("nommotorizado");
            idmotorizado = extras.getString("idmotorizado");
            mapurlTracker = mapurlTracker + value + "&idmotorizado=" + idmotorizado;
            //System.out.println("Nombre Motorizado : " + value);
            //The key argument here must match that used in the other activity
        }

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.loadUrl(mapurlTracker);

        btnWaze = findViewById(R.id.btnwaze);

        btnWaze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String uri = "http://maps.google.com/maps?daddr=" + lat_lon + " (Cliente)";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                try {
                    System.out.println("APP lat_lon Cliente : " + lat_lon);
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    System.out.println("OPEN PLAY STORE lat_lon Cliente : " + lat_lon);
                    try {
                        Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        startActivity(unrestrictedIntent);
                    } catch (ActivityNotFoundException innerEx) {
                        Toast.makeText(RepartoActivity.this, "Please install a maps application", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });


        callPermissions();
    }

    public void requestLocationUpdates(){
        // lat_lon
        if(
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)== PermissionChecker.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)== PermissionChecker.PERMISSION_GRANTED
        ) {


            fusedLocationProviderClient = new FusedLocationProviderClient(RepartoActivity.this);
            locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            // el gps obtendra la latitud y longitud lo mas pronto posible en 2 segundos
            locationRequest.setFastestInterval(2000);
            // se ejecutara cada 10 segundos para enviar la latitud y longitud
            locationRequest.setInterval(10000);

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    // ENVIO LATITUD AND LONGITUD A MI WEBSERVICE
                    updateLocationDelivery(String.valueOf(locationResult.getLastLocation().getLatitude()),String.valueOf(locationResult.getLastLocation().getLongitude()));

                    //Log.e(TAG, "Lat :" + locationResult.getLastLocation().getLatitude()
                    //        + " Lon :" + locationResult.getLastLocation().getLongitude());
                }
            }, getMainLooper());
        }else callPermissions();
        // -------
    }
    public void callPermissions(){
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        Permissions.check(this/*context*/, permissions, null/*rationale*/, null/*options*/, new PermissionHandler() {
            @Override
            public void onGranted() {
                requestLocationUpdates();
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions){
                super.onDenied(context,deniedPermissions);
                callPermissions();
            }
        });
    }

    public void updateLocationDelivery(final String latitudM, final String longitudM){

        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {

                String latitudMo = latitudM;
                String longitudMo = longitudM;

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("idmotorizado", idmotorizado));
                nameValuePairs.add(new BasicNameValuePair("latitude", latitudMo));
                nameValuePairs.add(new BasicNameValuePair("longitude", longitudMo));

                try {
                    HttpClient httpClient = new DefaultHttpClient();

                    HttpPost httpPost = new HttpPost(updateLocationMotorizado);

                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse httpResponse = httpClient.execute(httpPost);

                    HttpEntity httpEntity = httpResponse.getEntity();


                } catch (ClientProtocolException e) {

                } catch (IOException e) {

                }
                return "Data Inserted Successfully";
            }

            @Override
            protected void onPostExecute(String result) {

                super.onPostExecute(result);

                //Toast.makeText(RepartoActivity.this, "Data Submit Successfully", Toast.LENGTH_LONG).show();
                System.out.println("Latitud y Longitud de Delivery Enviado Correctamente");

            }
        }

        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();

        sendPostReqAsyncTask.execute(latitudM, longitudM);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}












