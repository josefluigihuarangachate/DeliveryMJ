package com.example.deliveryis;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.InputStream;

public class CustomListView extends ArrayAdapter<String>{

    private String[] profilename;
    private String[] email;
    private String[] idpedido;
    private String[] hora;
    private String[] estado;
    private String[] dni;
    private String[] imagepath;
    private String[] lat_lon;
    private Activity context;
    Bitmap bitmap;

    public CustomListView(Activity context,String[] profilename,String[] email,String[] imagepath,String[] idpedido,String[] dni,String[] hora,String[] estado,String[] lat_lon) {
        super(context, R.layout.layout,profilename);
        this.context=context;
        this.profilename=profilename;
        this.email=email;
        this.imagepath=imagepath;
        this.idpedido=idpedido;
        this.dni=dni;
        this.hora=hora;
        this.estado=estado;
        this.lat_lon=lat_lon;
    }

    @NonNull
    @Override

    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View r=convertView;
        ViewHolder viewHolder=null;
        if(r==null){
            LayoutInflater layoutInflater=context.getLayoutInflater();
            r=layoutInflater.inflate(R.layout.layout,null,true);
            viewHolder=new ViewHolder(r);
            r.setTag(viewHolder);
        }
        else {
            viewHolder=(ViewHolder)r.getTag();

        }

        viewHolder.tvw1.setText(profilename[position].toUpperCase());
        viewHolder.tvw2.setText(email[position]); // Num Celular
        new GetImageFromURL(viewHolder.ivw).execute(imagepath[position]);
        viewHolder.tvw3.setText(idpedido[position]); // Id Pedido

        viewHolder.tvw4.setText(dni[position]); // dni
        viewHolder.tvw5.setText("HORA: " + hora[position]); // hora
        viewHolder.tvw6.setText(estado[position].toUpperCase()); // estado del pedido
        viewHolder.tvw7.setText(lat_lon[position]);// Lat Lon

        return r;
    }

    class ViewHolder{

        TextView tvw1;
        TextView tvw2;
        ImageView ivw;
        TextView tvw3;

        TextView tvw4;
        TextView tvw5;
        TextView tvw6;
        TextView tvw7;

        ViewHolder(View v){
            tvw1=(TextView)v.findViewById(R.id.tvprofilename);
            tvw2=(TextView)v.findViewById(R.id.tvemail);
            ivw=(ImageView)v.findViewById(R.id.imageView);
            tvw3=(TextView)v.findViewById(R.id.tvidpedido);

            tvw4=(TextView)v.findViewById(R.id.tvdni);
            tvw5=(TextView)v.findViewById(R.id.tvhora);
            tvw6=(TextView)v.findViewById(R.id.tvestado);
            tvw7=(TextView)v.findViewById(R.id.tvlatlon);
        }

    }

    public class GetImageFromURL extends AsyncTask<String,Void,Bitmap>
    {

        ImageView imgView;
        public GetImageFromURL(ImageView imgv)
        {
            this.imgView=imgv;
        }
        @Override
        protected Bitmap doInBackground(String... url) {
            String urldisplay=url[0];
            bitmap=null;

            try{

                InputStream ist=new java.net.URL(urldisplay).openStream();
                bitmap= BitmapFactory.decodeStream(ist);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap){

            super.onPostExecute(bitmap);
            imgView.setImageBitmap(bitmap);
        }
    }

}
