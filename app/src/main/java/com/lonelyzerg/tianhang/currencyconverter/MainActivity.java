package com.lonelyzerg.tianhang.currencyconverter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import adapter.CurrencySpinnerAdapter;

public class MainActivity extends AppCompatActivity {

    private final String update_url = "https://api.exchangeratesapi.io/latest?base=USD";
    private final int MAX_LENGTH = 30;

    private long back_pressed = 0;
    private int exit_interval = 3000;

    private Spinner currency_spinner1;
    private Spinner currency_spinner2;
    private TextView state;
    private TextView currency2_amount;
    private EditText currency1_amount;
    private int currency1;
    private int currency2;
    private boolean first;
    private double[] rates;
    private String[] codes;
    private long update_time;
    private InputMethodManager manager;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        state = (TextView) findViewById(R.id.state);
        currency_spinner1 = (Spinner) findViewById(R.id.currency1);
        currency_spinner2 = (Spinner) findViewById(R.id.currency2);
        currency1_amount = (EditText) findViewById(R.id.currency1_amount);
        currency2_amount = (TextView) findViewById(R.id.currency2_amount);
        final String[] currency_list = getResources().getStringArray(R.array.currency);
        final TypedArray flag_list = getResources().obtainTypedArray(R.array.currency_flag);
        codes = getResources().getStringArray(R.array.code);
        CurrencySpinnerAdapter adapter = new CurrencySpinnerAdapter(MainActivity.this, currency_list, flag_list);
        currency_spinner1.setAdapter(adapter);
        currency_spinner2.setAdapter(adapter);
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                convert(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };
        currency_spinner1.setOnItemSelectedListener(listener);
        currency_spinner2.setOnItemSelectedListener(listener);

        SharedPreferences settings = getSharedPreferences("settings", 0);
        editor = settings.edit();
        if(first = settings.getBoolean("first", true)){
            editor.putBoolean("first", true);
            editor.putInt("currency1", 0);
            editor.putInt("currency2", 1);
            editor.apply();
            state.setText(R.string.first_update);
            currency_spinner1.setSelection(0,false);
            currency_spinner2.setSelection(1,false);
        }else{
            retrieveData(settings);
            update_time = settings.getLong("update_time", 0L);
            String date = new SimpleDateFormat(getString(R.string.date_format)).format(new Date(update_time));
            state.setText(getString(R.string.updated_status) + date);
        }

    }

    public void retrieveData(SharedPreferences settings){
        currency1 = settings.getInt("currency1", 0);
        currency2 = settings.getInt("currency2", 1);
        currency_spinner1.setSelection(currency1);
        currency_spinner2.setSelection(currency2);
        Log.i("calculate", String.valueOf(currency1));
        Log.i("calculate", String.valueOf(currency2));
        rates = new double[codes.length];
        for(int i = 0; i < codes.length; i++){
            rates[i] = getDouble(settings, codes[i], 0.0);
        }
    }

    public void exchange(View view){
        currency_spinner1.setSelection(currency2);
        currency_spinner2.setSelection(currency1);
    }

    public void convert(View view){
        String amout = currency1_amount.getText().toString();
        if(amout.equals("")){
            //Toast toast = Toast.makeText(getApplicationContext(),R.string.empty_input, Toast.LENGTH_SHORT);
            //toast.show();
            currency2_amount.setText("0.000");
            return;
        }
        if(!amout.matches("\\d+(\\.\\d+)*")){
            Toast toast = Toast.makeText(getApplicationContext(),R.string.invalid_input, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        if(amout.length() > MAX_LENGTH){
            Toast toast = Toast.makeText(getApplicationContext(),R.string.long_input, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        currency1 = currency_spinner1.getSelectedItemPosition();
        currency2 = currency_spinner2.getSelectedItemPosition();
        Log.i("calculate", String.valueOf(currency1));
        Log.i("calculate", String.valueOf(currency2));
        double result = new Double(amout) / rates[currency1] * rates[currency2];
        //Log.i("calculate", String.valueOf(rates[currency_spinner1.getSelectedItemPosition()]));
        currency2_amount.setText(new DecimalFormat("#0.000").format(result));
    }

    public void updateRate(View view){
        try {
            if(System.currentTimeMillis() - update_time < 60000){
                Toast toast = Toast.makeText(getApplicationContext(),R.string.frequent_update, Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            String json_str = new Query().execute(update_url).get();
            if(json_str == null){
                state.setText(R.string.failure_status);
                return;
            }
            JSONObject rates = new JSONObject(json_str).getJSONObject("rates");

            for (Iterator<String> i = rates.keys(); i.hasNext();){
                String currency_code = i.next();
                double rate = rates.getDouble(currency_code);
                putDouble(editor, currency_code, rate);
            }

            if(first){
                putDouble(editor, "USD", 1.00);
                editor.putBoolean("first", false);
            }
            Date d = Calendar.getInstance().getTime();
            update_time = d.getTime();
            editor.putLong("update_time",update_time);
            String date = new SimpleDateFormat(getString(R.string.date_format)).format(d);
            state.setText(getString(R.string.updated_status) + date);
            editor.apply();
            //Log.i("rate", json_str);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (getCurrentFocus() != null
                    && getCurrentFocus().getWindowToken() != null) {
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        if(back_pressed == 0 || back_pressed + exit_interval < System.currentTimeMillis()){
            back_pressed = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), R.string.press_back_again, Toast.LENGTH_SHORT).show();
            return;
        }
        editor.putInt("currency1", currency1);
        editor.putInt("currency2", currency2);
        editor.commit();
        Log.i("exiting", String.valueOf(currency1));
        Log.i("exiting", String.valueOf(currency2));
        this.finish();
    }

    SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }
    double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        if ( !prefs.contains(key))
            return defaultValue;

        return Double.longBitsToDouble(prefs.getLong(key, 0));
    }

    public static class Query extends AsyncTask<String, Void, String> {

        private static final String REQUEST_METHOD = "GET";
        private static final int READ_TIMEOUT = 3000;
        private static final int CONNECTION_TIMEOUT = 3000;

        @Override
        protected String doInBackground(String... params){
            String url = params[0];
            String result;
            try {
                HttpURLConnection connection =(HttpURLConnection)new URL(url).openConnection();
                //Set methods and timeouts
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);

                connection.connect();

                BufferedReader reader = new BufferedReader(new
                        InputStreamReader(connection.getInputStream()));
                result = reader.readLine();
                reader.close();
            } catch(IOException e){
                e.printStackTrace();
                result = null;
            }
            return result;
        }
        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
        }
    }
}
