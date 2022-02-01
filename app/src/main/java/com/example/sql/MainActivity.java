package com.example.sql;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sql.Controlador.PagerController;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TabLayout tabLayout;
    ViewPager viewPager;
    private Spinner spinner;
    PagerController pagerAdapter;
    TabItem tab1,tab2,tab3,tab4;


    RequestQueue requestQueue;
    Context context;
    private static final String URL1 = "http://192.168.100.27/android/save.php";
    private static final String URL2 = "http://192.168.100.27/android/datos.php";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tabLayout = findViewById(R.id.tablayout);
        viewPager = findViewById(R.id.viewpager);
        tab1 = findViewById(R.id.tabacelerometro);
        tab2 = findViewById(R.id.tabgiroscopio);
        tab3 = findViewById(R.id.tabmagnetometro);
        tab4 = findViewById(R.id.tabmapa);
        pagerAdapter = new PagerController(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(4);
        tabLayout. addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if(tab.getPosition()==0) pagerAdapter.notifyDataSetChanged();
                if(tab.getPosition()==1) pagerAdapter.notifyDataSetChanged();
                if(tab.getPosition()==2) pagerAdapter.notifyDataSetChanged();
                if(tab.getPosition()==3) pagerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        initUI();
        context = this;
        /*btnCreate.setOnClickListener(this);
        btnFetch.setOnClickListener(this);*/
    }

    private void initUI() {
        requestQueue = Volley.newRequestQueue(this);
        //spinner = findViewById(R.id.spinner);
        //jsonArrayRequest();
        //ejecutar();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
/*
        if(id== R.id.btncreate){
            String name = etname.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etpassword.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            createUser(name,email,password,phone);
        }
        else if(id== R.id.btnFetch){
            Intent intent = new Intent(this, MainActivity2.class);
            intent.putExtra("id",etId.getText().toString().trim());
            startActivity(intent);
        }

 */
    }

    private void createUser(final String name, final String email, final String password, final String phone) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                URL1,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(MainActivity.this, "Correct", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, error+"", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);
                params.put("password", password);
                params.put("phone", phone);
                return params;
            }
        };
        requestQueue.add(stringRequest);
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
                                datos[i] = jsonObject.getString("name");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, datos);
                        spinner.setAdapter(adapter);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, error+"", Toast.LENGTH_SHORT).show();
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
        }, 5000);//emp
    }
}