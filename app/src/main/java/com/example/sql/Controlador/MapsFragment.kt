package com.example.sql.Controlador

import android.content.Context
import android.content.pm.PackageManager
import android.net.DhcpInfo
import android.net.wifi.WifiManager
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.Handler
import android.text.format.Formatter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.sql.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MapsFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var mark: com.google.android.gms.maps.model.Marker
    var requestQueue: RequestQueue? = null
    private var spinner: Spinner? = null
    var URL2: String? = null
    var URL1:String? = null
    val direccion = LatLng(21.502227, -104.898208)
    val direcciones:List<LatLng> = listOf(LatLng(21.502853533629004, -104.89880839975233),LatLng(21.504523467742203, -104.9003480340099),LatLng(21.50453112794625, -104.89753223231959),
        LatLng(21.502554781926097, -104.89834733280888)
    )
    companion object {
        const val REQUEST_CODE_LOCATION = 0
    }
    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        val tepic = LatLng(21.502227, -104.898208)
        mark = mMap.addMarker(MarkerOptions().position(tepic).title("Tepic: La loma"))!!
        mark.remove()
        enableLocation()
        mMap.uiSettings.isZoomControlsEnabled = true
        ejecutar()
        jsonArrayRequest()
        ejecutarInfo()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val maps = inflater.inflate(R.layout.fragment_maps, container, false)
        requestQueue = Volley.newRequestQueue(this.activity)
        spinner = maps.findViewById(R.id.spinnerMaps)
        //Conocer la red
        val manager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = manager.dhcpInfo
        val address = Formatter.formatIpAddress(info.gateway)
        URL1 = "http://$address/Charts/Web-services/androidGps.php"
        URL2 = "http://$address/Charts/Web-services/checkbox.php"
        return maps
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onMapReady(p0: GoogleMap) {

    }

    private fun ejecutar() {
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                datosGps()
                /*pruebas
                mark.remove()
                val num = (0..3).random()
                mark = mMap.addMarker(MarkerOptions().position(direcciones[num]).title("Perro")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.perro)))!!
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(direcciones[num],mMap.cameraPosition.zoom))
                */
                handler.postDelayed(this, 5000) //se ejecutara cada 5 s
            }
        }, 2000) //emp
    }

    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED

    private fun enableLocation(){
        if(!::mMap.isInitialized) return
        if(isLocationPermissionGranted()){
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            mMap.isMyLocationEnabled = true
        }
        else{
            requestLocationPermission()
        }
    }
    private fun requestLocationPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),android.Manifest.permission.ACCESS_FINE_LOCATION)){
            Toast.makeText(requireContext(),"Haz rechazado los permisos, ve a ajustes y aceptalos", Toast.LENGTH_LONG).show()
        }
        else{
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQUEST_CODE_LOCATION -> if(grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                mMap.isMyLocationEnabled = true
            }else{
                Toast.makeText(requireContext(),"Haz rechazado los permisos, ve a ajustes y aceptalos", Toast.LENGTH_LONG).show()
            }
            else ->{}
        }
    }
    private fun jsonArrayRequest() {
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            URL2,
            null,
            { response ->
                val size = response.length()
                val datos = arrayOfNulls<String>(size)
                Log.d("Datos",size.toString())
                for (i in 0 until size) {
                    try {
                        val jsonObject = JSONObject(response[i].toString())
                        datos[i] = jsonObject.getString("ID")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, datos)
                if(spinner!!.selectedItem==null) {
                    spinner!!.adapter = adapter
                    spinner!!.setSelection(0)
                }
                val text = spinner!!.selectedItem as String
                var posicion = 0
                spinner!!.adapter = adapter
                for (i in 0 until spinner!!.count) {
                    if (spinner!!.getItemAtPosition(i).toString() == text) {
                        posicion = i
                        break
                    }
                }
                spinner!!.setSelection(posicion)
            }
        ) {
            //Toast.makeText(context, error+"", Toast.LENGTH_SHORT).show();
        }
        requestQueue!!.add(jsonArrayRequest)
    }
    private fun ejecutarInfo() {
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                jsonArrayRequest() //llamamos nuestro metodo
                handler.postDelayed(this, 10000) //se ejecutara cada 10 segundos
            }
        }, 5000) //emp
    }

    private fun datosGps() {
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            URL1 + "?id=" + spinner!!.selectedItem.toString(),
            null,
            { response ->
                val size = response.length()
                for (i in 0 until size) {
                    try {
                        val jsonObject = JSONObject(response[i].toString())
                        mark.remove()
                        val perro = LatLng(jsonObject.getDouble("latitud"),jsonObject.getDouble("longitud"))
                        mark = mMap.addMarker(MarkerOptions().position(perro).title("Perro")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.perro)))!!
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(perro,mMap.cameraPosition.zoom))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        ) {
            //Toast.makeText(context, error+"", Toast.LENGTH_SHORT).show();
        }
        requestQueue!!.add(jsonArrayRequest)
    }
}