/*  BTRooster Lite: Roosterapp voor Calvijn College
 *  Copyright (C) 2017 Rutger Broekhoff <rutger broekhoff three at gmail dot com>
 *                 and Jochem Broekhoff
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nl.viasalix.btroosterlite;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class TestTimetableFragment extends Fragment {
    View view;
    WebView webView;
    SharedPreferences sharedPreferences;

    String code;
    String location;

    List<String> availableTestweeks = new ArrayList<>();
    List<String> availableTestweeksNames = new ArrayList<>();

    public TestTimetableFragment() {
        // Required empty public constructor
    }

    public static TestTimetableFragment newInstance() {
        TestTimetableFragment fragment = new TestTimetableFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_test_timetable, container, false);
        webView = view.findViewById(R.id.web_view);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.appbar_mainactivity_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reload:
                loadTestTimetable(true);
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                getActivity().startActivity(intent);
                break;
            case R.id.action_opensource:
                Intent ossIntent = new Intent(getActivity(), OssLicensesMenuActivity.class);
                ossIntent.putExtra("title", "Open-source licenties");
                getActivity().startActivity(ossIntent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        code = sharedPreferences.getString("code", "12345");
        location = sharedPreferences.getString("location", "Goes");

        loadTestTimetable(true);
    }

    private boolean online() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }

        return (networkInfo != null && networkInfo.isConnected());
    }

    private void loadTestTimetable(boolean getIndexes) {
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        if (online()) {
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        } else {
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }

        if (getIndexes) getIndexes();
        else            getTestTimetable();
    }

    private void getTestTimetable() {
        int weekChange = sharedPreferences.getInt("tt_weekChange", 0);

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority(MainActivity.AUTHORITY)
                .appendPath("ToetsroosterEmbedServlet")
                .appendQueryParameter("code", code)
                .appendQueryParameter("locatie", location)
                .appendQueryParameter("type", "leerlingen")
                .appendQueryParameter("toetsweek", availableTestweeks.get(weekChange));
        String url = builder.build().toString();

        webView.loadUrl(url);
    }

    private void getIndexes() {
        if (online()) {
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority(MainActivity.AUTHORITY)
                    .appendPath("ToetsroosterEmbedServlet")
                    .appendQueryParameter("indexOphalen", "1")
                    .appendQueryParameter("locatie", location);
            String url = builder.build().toString();

            Log.d("url built", url);

            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            sharedPreferences.edit().putString("tt_indexes", response).apply();
                            Log.d("or", response);
                            handleResponse(response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("error", error.getMessage());
                }
            });

            queue.add(stringRequest);
        } else {
            String response = sharedPreferences.getString("tt_indexes", null);
            handleResponse(response);
            getTestTimetable();
        }
    }

    private void handleResponse(String response) {
        availableTestweeks.clear();
        availableTestweeksNames.clear();

        int i = 0;

        String[] responses = response.split("\n");

        for (String responseString : responses) {
            if (responseString.trim().length() > 0) {
                String[] responseStringSplit = responseString.split("\\|", 2);

                availableTestweeks.add(i, responseStringSplit[0]);
                availableTestweeksNames.add(i, responseStringSplit[1]);
                ++i;
            }
        }

        Spinner weekSpinner = getActivity().findViewById(R.id.tt_week_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, availableTestweeksNames);
        weekSpinner.setAdapter(adapter);

        weekSpinner.setSelection(sharedPreferences.getInt("tt_weekChange", 1));

        weekSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                sharedPreferences.edit().putInt("tt_weekChange", position).apply();

                loadTestTimetable(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }
}
