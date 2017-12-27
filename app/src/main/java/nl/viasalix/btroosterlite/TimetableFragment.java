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

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.Layout;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewDatabase;
import android.webkit.WebViewFragment;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.annimon.stream.Stream;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class TimetableFragment extends Fragment {
    Spinner weekSpinner;
    // Locaties
    String[] locaties = {
            "Goes Klein Frankrijk",
            "Goes Noordhoeklaan",
            "Goes Stationspark",
            "Krabbendijke Appelstraat",
            "Krabbendijke Kerkpolder",
            "Middelburg",
            "Tholen"
    };
    // Locaties die in de request URL moeten te komen staan
    String[] locatiesURL = {
            "Goes",
            "GoesNoordhoeklaan",
            "GoesStationspark",
            "KrabbendijkeAppelstraat",
            "KrabbendijkeKerkpolder",
            "Middelburg",
            "Tholen",
    };
    // Opslag 'roostercode', location en type
    String code = "";
    String location = "";
    String type = "";
    // Variabelen om beschikbare weken weer te geven
    List<String> availableWeeks = new ArrayList<>();
    List<String> availableWeeksNames = new ArrayList<>();
    // Initialiseer benodigde variabelen
    SharedPreferences sharedPreferences;
    View view;
    WebView webView;
    WebView webViewPL;
    Stack<String> pagesToLoad = new Stack<>();
    private OnFragmentInteractionListener mListener;

    public TimetableFragment() {
        // Lege constructor is nodig om een fragment te kunnen gebruiken
    }

    private static int getPixelValue(Context context, int dimenId) {
        Resources resources = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dimenId,
                resources.getDisplayMetrics());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.appbar_mainactivity_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private boolean online() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }

        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean mobileIsConnected() {
        final ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(
                ConnectivityManager.TYPE_MOBILE);

        return mobile.isConnected();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reload:
                loadTimetable(true);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_timetable, container, false);
        webView = view.findViewById(R.id.web_view);
        webViewPL = view.findViewById(R.id.web_view_preload);
        weekSpinner = view.findViewById(R.id.week_spinner);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null)
            ((AppCompatActivity) getActivity())
                    .getSupportActionBar()
                    .setDisplayShowTitleEnabled(false);

        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (loadSharedPreferences() != 1)
            loadTimetable(true);
    }

    private int loadSharedPreferences() {
        if (!sharedPreferences.contains("code")) {
            showCodeDialog();
            return 1;
        }

        if (!sharedPreferences.contains("location")) {
            showLocationDialog();
            return 1;
        }

        code = sharedPreferences.getString("code", "12345");

        location = sharedPreferences.getString("location", locaties[0]);
        type = getType(code);

        return 0;
    }

    private String getType(String code) {
        String docentPatternInput = "([A-Za-z]){3}";
        String leerlingPatternInput = "([0-9]){5}";

        Pattern docentPattern = Pattern.compile(docentPatternInput);
        Pattern leerlingPattern = Pattern.compile(leerlingPatternInput);

        if (!docentPattern.matcher(code).matches() && !leerlingPattern.matcher(code).matches()) {
            return "c";
        } else if (docentPattern.matcher(code).matches()) {
            return "t";
        } else if (leerlingPattern.matcher(code).matches()) {
            return "s";
        }

        return "none";
    }

    private void showCodeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Code");

        LinearLayout layout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        layout.setLayoutParams(layoutParams);
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setLayoutParams(layoutParams);

        int margin_left_right_bottom = getPixelValue(getActivity(), 24);
        int margin_top = getPixelValue(getActivity(), 20);
        layoutParams.setMargins(margin_left_right_bottom,
                margin_top,
                margin_left_right_bottom,
                margin_left_right_bottom);

        layout.addView(input);

        builder.setView(layout);

        builder.setPositiveButton("OK", (dialog, which) -> {
            sharedPreferences.edit().putString("code", input.getText().toString()).apply();
            onStart();
        }).setNegativeButton("Annuleren", (dialog, which) -> {
            getActivity().finish();
            dialog.cancel();
        });

        builder.show();
    }

    private void showLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Locatie");
        CharSequence items[] = locaties;

        builder.setSingleChoiceItems(items, -1, (dialog, which) -> {
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
            sharedPreferences.edit().putString("location", locatiesURL[selectedPosition]).apply();
            Log.d("selected", Integer.toString(selectedPosition));
            onStart();
        });

        builder.setNegativeButton("Annuleren", ((dialog, which) -> {
            getActivity().finish();
            dialog.cancel();
        }));

        builder.show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void loadTimetable(boolean getIndexes) {
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        if (online()) {
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        } else {
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }

        if (getIndexes) {
            getIndexes();
        } else {
            getTimetable(
                    sharedPreferences.getString("t_week",
                            Integer.toString(getCurrentWeekOfYear())));
            if (sharedPreferences.getBoolean("t_preload", false)) {
                boolean onlyWifiPref = sharedPreferences.getBoolean("t_preload_only_wifi", true);

                if (online()) {
                    if (mobileIsConnected() != onlyWifiPref) {
                        preloadTimetables();
                    }
                }
            } else {
                webViewPL.destroy();
            }
        }
    }

    private void preloadTimetables() {
        pagesToLoad.empty();

        Stream.of(availableWeeks).forEach(it -> {
                    pagesToLoad.push(
                            buildTimetableURL(it)
                    );
                }
        );

        class PreloadClient extends WebViewClient {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!pagesToLoad.isEmpty()) {
                    view.loadUrl(pagesToLoad.pop());
                }
            }
        }

        PreloadClient client = new PreloadClient();
        webViewPL.setWebViewClient(client);

        webViewPL.loadUrl(pagesToLoad.pop());
    }

    private String buildTimetableURL(String week) {
        String typeString = getType(code);

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority(MainActivity.AUTHORITY)
                .appendPath("RoosterEmbedServlet")
                .appendQueryParameter("code", code)
                .appendQueryParameter("locatie", location)
                .appendQueryParameter("type", typeString)
                .appendQueryParameter("week", week);
        String url = builder.build().toString();

        return url;
    }

    private void getTimetable(String week) {
        webView.loadUrl(buildTimetableURL(week));
    }

    private void getIndexes() {
        if (online()) {
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority(MainActivity.AUTHORITY)
                    .appendPath("RoosterEmbedServlet")
                    .appendQueryParameter("indexOphalen", "1")
                    .appendQueryParameter("locatie", location);
            String url = builder.build().toString();

            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    response -> {
                        sharedPreferences.edit().putString("t_indexes", response).apply();
                        Log.d("or", response);
                        handleResponse(response);
                    }, error -> Log.d("error", error.getMessage()));

            queue.add(stringRequest);
        } else {
            String response = sharedPreferences.getString("t_indexes", null);
            handleResponse(response);
            getTimetable(
                    sharedPreferences.getString(
                            "t_week",
                            Integer.toString(
                                    getCurrentWeekOfYear()
                            )
                    )
            );
        }
    }

    private int getCurrentWeekOfYear() {
        Calendar cl = Calendar.getInstance();
        Log.d("date", Integer.toString(cl.get(cl.WEEK_OF_YEAR)));
        return cl.get(cl.WEEK_OF_YEAR);
    }

    private void handleResponse(String response) {
        if (response != null) {
            availableWeeks.clear();
            availableWeeksNames.clear();

            int i = 0;

            Log.d("response", response);
            String[] responses = response.split("\n");

            for (String responseString : responses) {
                if (responseString.trim().length() > 0) {
                    String[] responseStringSplit = responseString.split("\\|", 2);

                    availableWeeks.add(i, responseStringSplit[0]);
                    availableWeeksNames.add(i, responseStringSplit[1]);
                    ++i;
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, availableWeeksNames);
            weekSpinner.setAdapter(adapter);

            weekSpinner.setSelection(
                    availableWeeks.indexOf(
                            sharedPreferences.getString(
                                    "t_selectedWeek",
                                    Integer.toString(
                                            getCurrentWeekOfYear()
                                    )
                            )
                    )
            );

            weekSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    sharedPreferences.edit().putString("t_week", availableWeeks.get(position)).apply();

                    loadTimetable(false);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }
    }

    interface OnFragmentInteractionListener {
    }
}
