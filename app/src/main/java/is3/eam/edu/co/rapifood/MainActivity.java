package is3.eam.edu.co.rapifood;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import is3.eam.edu.co.rapifood.tools.Constantes;
import is3.eam.edu.co.rapifood.web.VolleySingleton;


public class MainActivity extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static final String EXTRA_MESSAGE = "message";
    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String PROPERTY_EXPIRATION_TIME = "onServerExpirationTimeMs";
    private static final String PROPERTY_USER = "user";

    public static final long EXPIRATION_TIME_MS = 1000 * 3600 * 24 * 7;

    String SENDER_ID = "590363603497";

    static final String TAG = "GCMDemo";

    private Context context;
    private String regid;
    private GoogleCloudMessaging gcm;

    private EditText txtUsuario;
    private Button btnRegistrar;

    boolean reg = false;

    Constantes constante = new Constantes(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        txtUsuario = (EditText)findViewById(R.id.txtUsuario);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/

                context = getApplicationContext();

                //Chequemos si está instalado Google Play Services
                if(checkPlayServices())
                {
                    gcm = GoogleCloudMessaging.getInstance(MainActivity.this);

                    //Obtenemos el Registration ID guardado
                    regid = getRegistrationId(context);

                    //Si no disponemos de Registration ID comenzamos el registro
                    if (regid.equals("")) {
                        TareaRegistroGCM tarea = new TareaRegistroGCM();
                        tarea.execute(txtUsuario.getText().toString());
                        //registrarUsuario(txtUsuario.getText().toString());
                    }
                }else{
                    Log.i(TAG, "No se ha encontrado Google Play Services.");
                }

            }
        });
    }

    private String getRegistrationId(Context context)
    {
        SharedPreferences prefs = getSharedPreferences(
                MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);

        String registrationId = prefs.getString(PROPERTY_REG_ID, "");

        if (registrationId.length() == 0)
        {
            Log.d(TAG, "Registro GCM no encontrado.");
            return "";
        }

        String registeredUser =
                prefs.getString(PROPERTY_USER, "user");

        int registeredVersion =
                prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);

        long expirationTime =
                prefs.getLong(PROPERTY_EXPIRATION_TIME, -1);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String expirationDate = sdf.format(new Date(expirationTime));

        Log.d(TAG, "Registro GCM encontrado (usuario=" + registeredUser +
                ", version=" + registeredVersion +
                ", expira=" + expirationDate + ")");

        int currentVersion = getAppVersion(context);

        if (registeredVersion != currentVersion)
        {
            Log.d(TAG, "Nueva versión de la aplicación.");
            return "";
        }
        else if (System.currentTimeMillis() > expirationTime)
        {
            Log.d(TAG, "Registro GCM expirado.");
            return "";
        }
        else if (!txtUsuario.getText().toString().equals(registeredUser))
        {
            Log.d(TAG, "Nuevo nombre de usuario.");
            return "";
        }

        return registrationId;
    }

    private static int getAppVersion(Context context)
    {
        try
        {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);

            return packageInfo.versionCode;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            throw new RuntimeException("Error al obtener versión: " + e);
        }
    }

    @Override
	protected void onResume()
	{
	    super.onResume();

	    checkPlayServices();
	}

	private boolean checkPlayServices() {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	    if (resultCode != ConnectionResult.SUCCESS)
	    {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
	        {
	            GooglePlayServicesUtil.getErrorDialog(resultCode, this,
	                    PLAY_SERVICES_RESOLUTION_REQUEST).show();
	        }
	        else
	        {
	            Log.i(TAG, "Dispositivo no soportado.");
	            finish();
	        }
	        return false;
	    }
	    return true;
	}


    private class TareaRegistroGCM extends AsyncTask<String,Integer,String>
    {
        @Override
        protected String doInBackground(String... params)
        {
            String msg = "";

            try
            {
                if (gcm == null)
                {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }

                //Nos registramos en los servidores de GCM
                regid = gcm.register(SENDER_ID);

                Log.d(TAG, "Registrado en GCM: registration_id=" + regid);

                System.out.println("regid"+regid);

                //Nos registramos en nuestro servidor
                boolean registrado = registroServidor(params[0], regid);

                //Guardamos los datos del registro
                if(registrado)
                {
                    setRegistrationId(context, params[0], regid);
                }
            }
            catch (IOException ex)
            {
                Log.d(TAG, "Error registro en GCM:" + ex.getMessage());
            }

            return msg;
        }
    }

    private void setRegistrationId(Context context, String user, String regId)
    {
        SharedPreferences prefs = getSharedPreferences(
                MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);

        int appVersion = getAppVersion(context);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_USER, user);
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.putLong(PROPERTY_EXPIRATION_TIME,
                System.currentTimeMillis() + EXPIRATION_TIME_MS);

        editor.commit();
    }

    private boolean registroServidorOld(String usuario, String regId)
    {
        boolean reg = false;

        final String NAMESPACE = "http://sgoliver.net/";
        final String URL="http://10.0.2.2:1634/ServicioRegistroGCM.asmx";
        final String METHOD_NAME = "RegistroCliente";
        final String SOAP_ACTION = "http://sgoliver.net/RegistroCliente";

        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

        request.addProperty("usuario", usuario);
        request.addProperty("regGCM", regId);

        SoapSerializationEnvelope envelope =
                new SoapSerializationEnvelope(SoapEnvelope.VER11);

        envelope.dotNet = true;

        envelope.setOutputSoapObject(request);

        HttpTransportSE transporte = new HttpTransportSE(URL);

        try
        {
            transporte.call(SOAP_ACTION, envelope);

            SoapPrimitive resultado_xml =(SoapPrimitive)envelope.getResponse();
            String res = resultado_xml.toString();

            if(res.equals("1"))
            {
                Log.d(TAG, "Registrado en mi servidor.");
                reg = true;
            }
        }
        catch (Exception e)
        {
            Log.d(TAG, "Error registro en mi servidor: " + e.getCause() + " || " + e.getMessage());
        }

        return reg;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private boolean registroServidor(String usuario, String regId) {
        reg = false;

        HashMap<String, String> map = new HashMap<>();// Mapeo previo

        map.put("usuario", usuario);
        map.put("regGCM", regId);
        map.put("type", "register");

        // Crear nuevo objeto Json basado en el mapa
        JSONObject jobject = new JSONObject(map);

        System.out.println("URL: "+constante.getUrl());
        System.out.println(jobject.toString());
        //System.out.println("params: "+map);

        // Depurando objeto Json...
        //Log.d(TAG, jobject.toString());

        // Actualizar datos en el servidor
        VolleySingleton.getInstance(this).addToRequestQueue(
                new JsonObjectRequest(
                        Request.Method.POST,
                        constante.getUrl(),
                        jobject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                reg = procesarRespuesta(response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                //Log.d(TAG, "Error Volley: " + error.getMessage());
                                System.out.println("Error Volley: " + error.getMessage());
                            }
                        }

                ) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put("Content-Type", "application/json; charset=utf-8");
                        headers.put("Accept", "application/json");
                        return headers;
                    }

                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8" + getParamsEncoding();
                    }
                }
        );

        return reg;
    }

    private boolean procesarRespuesta(JSONObject response) {

        System.out.println("Llege: "+response);

        boolean resp = false;

        Gson gson = new Gson();

        try {

            // Obtener estado
            String estado = response.getString("estado");

            // Obtener mensaje
            String mensaje = response.getString("mensaje");

            switch (estado) {
                case "1":
                    // Mostrar mensaje
                    Toast.makeText(
                            this,
                            mensaje,
                            Toast.LENGTH_LONG).show();
                    // Enviar código de éxito
                    this.setResult(Activity.RESULT_OK);
                    resp = true;
                    break;

                case "0":
                    // Mostrar mensaje
                    Toast.makeText(
                            this,
                            mensaje,
                            Toast.LENGTH_LONG).show();
                    // Enviar código de falla
                    this.setResult(Activity.RESULT_CANCELED);
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return resp;

    }
}
