package com.example.sql.Controlador;

import android.content.Context;
import android.graphics.Color;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.sql.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import static android.content.Context.WIFI_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Magnetometro#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Magnetometro extends Fragment {
    private Spinner spinner;
    Context context;
    RequestQueue requestQueue;

    String URL2;
    String URL3;
    String URL4;
    String URL5;
    String URL6;
    LineData lineData;
    LineData data;
    LineChart lineChart;
    LineDataSet lineDataSet = new LineDataSet(null,null);
    ArrayList<ILineDataSet> iLineDataSet = new ArrayList<>();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private boolean inicializada = false;
    private  boolean detener = false;
    Double magne[] = {0.0,0.0,0.0};
    Double iguales = 0.0;
    int comprueba = 0;
    EditText Minutos;
    Button btnMostrar;
    private String fechaA;
    private String fechaF;
    
    Switch aSwitch;
    EditText Alias;
    Button btnActualizar;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Magnetometro() {
        inicializada = false;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Magnetometro.
     */
    // TODO: Rename and change types and number of parameters
    public static Magnetometro newInstance(String param1, String param2) {
        Magnetometro fragment = new Magnetometro();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        inicializada = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View magnetometro = inflater.inflate(R.layout.fragment_magnetometro, container, false);
        context = this.getActivity();
        //Conocer la red
        final WifiManager manager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        final DhcpInfo info = manager.getDhcpInfo();
        final String address = Formatter.formatIpAddress(info.gateway);
        Toast.makeText(context, address, Toast.LENGTH_SHORT).show();
        URL2 = "http://"+address+"/Charts/Web-services/checkbox.php";
        URL3 = "http://"+address+"/Charts/Web-services/androidMagnetometro.php";
        URL4 = "http://"+address+"/Charts/Web-services/tDatos.php";
        URL5 = "http://"+address+"/Charts/Web-services/selAlias.php";
        URL6 = "http://"+address+"/Charts/Web-services/setAlias.php";
        ///////////////////////////////////////////////////////////////////////////////
        requestQueue = Volley.newRequestQueue(this.getActivity());
        spinner = magnetometro.findViewById(R.id.spinner3);
        lineChart = magnetometro.findViewById(R.id.linechart3);
        aSwitch = magnetometro.findViewById(R.id.switch3);
        Minutos = magnetometro.findViewById(R.id.txtMinutos3);
        btnMostrar = magnetometro.findViewById(R.id.btnmostrar3);
        btnActualizar = magnetometro.findViewById(R.id.btnupd3);
        Alias = magnetometro.findViewById(R.id.etxtAlias3);
        Alias.setText("Buscando...");
        jsonArrayRequest();
        ejecutar();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if(!inicializada) {
                    inicializada = true;
                    return;
                }
                //Toast.makeText(context, "entre magnetometro", Toast.LENGTH_SHORT).show();
                String elemento = (String) parent.getItemAtPosition(pos);
                Bundle datosMag = new Bundle();
                datosMag.putString("poSpin", elemento);
                getParentFragmentManager().setFragmentResult("datosMag2", datosMag);
                getParentFragmentManager().setFragmentResult("datosMag", datosMag);
                if(!Alias.hasFocus()){
                    datosAlias();
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                detener = !detener;
            }
        });
        //Recibir datos otros fragments
        getParentFragmentManager().setFragmentResultListener("datosAce2", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                String dato = result.getString("poSpin");
                ejecutar();
                int posicion = 0;
                for(int i=0; i<spinner.getCount();i++){
                    if(spinner.getItemAtPosition(i).toString().equals(dato)) {posicion = i; break;}
                }
                inicializada = false;
                spinner.setSelection(posicion);
            }
        });
        getParentFragmentManager().setFragmentResultListener("datosGir2", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                String dato = result.getString("poSpin");
                ejecutar();
                int posicion = 0;
                for(int i=0; i<spinner.getCount();i++){
                    if(spinner.getItemAtPosition(i).toString().equals(dato)) {posicion = i; break;}
                }
                inicializada = false;
                spinner.setSelection(posicion);
            }
        });
        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAlias();
            }
        });
        btnMostrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Minutos.getText().toString().isEmpty()){
                    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-6"));
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-6"));
                    fechaA = dateFormat.format(calendar.getTime());
                    calendar.add(Calendar.MINUTE,-Integer.parseInt(Minutos.getText().toString()));
                    fechaF = dateFormat.format(calendar.getTime());
                    data = null;
                    lineChart.invalidate();
                    lineChart.clear();
                    lineChart.getDescription().setEnabled(true);
                    lineChart.getDescription().setText("Magnetometro");
                    lineChart.setTouchEnabled(true);
                    lineData = new LineData();
                    lineData.setValueTextColor(Color.BLUE);
                    lineChart.setData(lineData);
                    lineChart.getAxisRight().setDrawLabels(false);
                    lineChart.getExtraLeftOffset();
                    detener = true;
                    datosTiempo();
                    detener = false;
                }
            }
        });
        lineChart.getDescription().setEnabled(true);
        lineChart.getDescription().setText("Magnetometro");
        lineChart.setTouchEnabled(true);
        lineData = new LineData();
        lineData.setValueTextColor(Color.BLUE);
        lineChart.setData(lineData);
        lineChart.getAxisRight().setDrawLabels(false);
        lineChart.getExtraLeftOffset();
        Alias.setText("");
        informacion();
        return magnetometro;
    }
    private void jsonArrayRequest() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                URL2,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        int size = response.length();
                        String[] datos = new String[size];
                        for (int i = 0; i < size; i++) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.get(i).toString());
                                datos[i] = jsonObject.getString("ID");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, datos);
                        String text = (String) spinner.getSelectedItem();
                        int posicion = 0;
                        spinner.setAdapter(adapter);
                        for(int i=0; i<spinner.getCount();i++){
                            if(spinner.getItemAtPosition(i).toString().equals(text)) {posicion = i; break;}
                        }
                        inicializada = false;
                        spinner.setSelection(posicion);
                        datosAlias();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, error+"", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        requestQueue.add(jsonArrayRequest);
    }
    private void datosMag() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                URL3+"?id="+spinner.getSelectedItem().toString(),
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        int size = response.length();
                        for (int i = 0; i < size; i++) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.get(i).toString());
                                //Me fijo si los resultados se estan repitiendo en dado caso asumo que el collar esta desactivado
                                Log.d("Busca", jsonObject.getDouble("mx")+"");
                                if(magne[0]==jsonObject.getDouble("mx")&&magne[1]==jsonObject.getDouble("my")&&magne[2]==jsonObject.getDouble("mz")) comprueba++;
                                else if(magne[0]==0.0&&magne[1]==0.0&&magne[2]==0.0) comprueba++;
                                if(jsonObject.getDouble("mx")!=iguales && iguales!=0.0) {
                                    comprueba = 0;
                                    iguales = 0.0;
                                }
                                if(comprueba<=10) {
                                    magne[0] = jsonObject.getDouble("mx");
                                    magne[1] = jsonObject.getDouble("my");
                                    magne[2] = jsonObject.getDouble("mz");
                                }
                                else if(comprueba == 11) {
                                    Toast.makeText(context, "No se han detectado cambios en las lecturas del magnetometro compruebe el estado del dispositivo", Toast.LENGTH_SHORT).show();
                                    iguales = magne[0];
                                }
                                else{
                                    magne[0] = magne[1] = magne[2] = 0.0;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, error+"", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        requestQueue.add(jsonArrayRequest);
    }
    private void datosTiempo() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                URL4+"?id="+spinner.getSelectedItem().toString()+"&fechai="+fechaF+"&fechaf="+fechaA+"?id="+spinner.getSelectedItem().toString(),
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        int size = response.length();
                        String[] datos = new String[size];
                        Log.d("myTag", URL4+"?id="+spinner.getSelectedItem().toString()+"&fechai="+fechaF+"&fechaf="+fechaA+"?id="+spinner.getSelectedItem().toString());
                        for (int i = 0; i < size; i++) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.get(i).toString());
                                magne[0] = jsonObject.getDouble("mx");
                                magne[1] = jsonObject.getDouble("my");
                                magne[2] = jsonObject.getDouble("mz");
                                addEntry();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, error+"", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        requestQueue.add(jsonArrayRequest);
    }

    private void ejecutar() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                jsonArrayRequest();//llamamos nuestro metodo
                handler.postDelayed(this, 10000);//se ejecutara cada 10 segundos
            }
        }, 0);//emp
    }
    private void informacion() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!detener&&spinner.getSelectedItem()!=null){
                    datosMag();
                    addEntry();
                }
                handler.postDelayed(this, 1000);//se ejecutara cada 10 segundos
            }
        }, 5000);//emp
    }

    private void addEntry() {

        data = lineChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            ILineDataSet set2 = data.getDataSetByIndex(1);
            ILineDataSet set3 = data.getDataSetByIndex(2);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet("Dato x",Color.BLUE);
                data.addDataSet(set);
            }
            if (set2== null){
                set2 = createSet("Dato y",Color.RED);
                data.addDataSet(set2);
            }
            if (set3== null){
                set3 = createSet("Dato z",Color.GREEN);
                data.addDataSet(set3);
            }

            data.addEntry(new Entry(set.getEntryCount(), magne[0].floatValue()), 0);
            data.addEntry(new Entry(set2.getEntryCount(), magne[1].floatValue()), 1);
            data.addEntry(new Entry(set3.getEntryCount(), magne[2].floatValue()), 2);
            //data.addEntry(new Entry((float) Math.random(), (float) Math.random()), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            lineChart.notifyDataSetChanged();

            // limit the number of visible entries
            lineChart.setVisibleXRangeMaximum(100);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            lineChart.moveViewToX(data.getEntryCount());

        }
    }
    private LineDataSet createSet(String name, int color) {
        LineDataSet set = new LineDataSet(null, name);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(2f);
        set.setColor(color);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    private void updateAlias() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                URL6+"?id="+spinner.getSelectedItem().toString()+"&alias="+Alias.getText().toString(),
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        );
        requestQueue.add(jsonArrayRequest);
    }

    private void datosAlias() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                URL5+"?id="+spinner.getSelectedItem().toString(),
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("myTag", URL5+"?id="+spinner.getSelectedItem().toString());
                        int size = response.length();
                        String[] datos = new String[size];
                        for (int i = 0; i < size; i++) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.get(i).toString());
                                Alias.setText(jsonObject.getString("alias"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, error+"", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        requestQueue.add(jsonArrayRequest);
    }
}