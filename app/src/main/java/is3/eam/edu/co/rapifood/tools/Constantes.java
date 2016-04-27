package is3.eam.edu.co.rapifood.tools;

import android.content.Context;
import android.content.SharedPreferences;

public class Constantes {

    SharedPreferences prefs;

    /**
     * Transición Home -> Detalle
     */
    public static final int CODIGO_DETALLE = 100;

    /**
     * Transición Detalle -> Actualización
     */
    public static final int CODIGO_ACTUALIZACION = 101;
    /**
     * Puerto que utilizas para la conexión.
     * Dejalo en blanco si no has configurado esta carácteristica.
     */
    private String puerto_host;
    /**
     * Dirección IP de genymotion o AVD
     */
    private String ip;

    /**
     * URLs del Web Service
     */
    public String url;

    public Constantes(Context mContext){

//        prefs = mContext.getSharedPreferences("configuracion", Context.MODE_PRIVATE);
//
//        ip = prefs.getString("host", "");
//        puerto_host = prefs.getString("port", "");
//
//        url = "http://" + ip + ":" + puerto_host + "/productmanager/rest/cuenta/consultarsaldo";

        ip = "192.168.1.52";
        url = "http://" + ip + "/rapifood/registerdevice.php";
    }

    public String getPuerto_host() {
        return puerto_host;
    }

    public String getIp() {
        return ip;
    }

    public String getUrl() {
        return url;
    }
}
