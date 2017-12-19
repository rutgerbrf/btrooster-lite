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
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class TimetableFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    private enum e_WEEKCHANGE {
        YWEEK,  // Yestwerweek
        TWEEK,  // This week
        NWEEK,  // Next week
        ANWEEK  // After next week
    }

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

    // Mogelijkheden om uit te selecteren
    String[] weekMogelijkheden = {
            "Vorige week",
            "Deze week",
            "Volgende week",
            "Over twee weken"
    };

    // Opslag 'roostercode', locatie en type
    String code = "";
    String locatie = "";
    String type = "";

    // Vorige week, deze week, etc.
    e_WEEKCHANGE weekChange = e_WEEKCHANGE.TWEEK;

    // Initialiseer benodigde variabelen
    SharedPreferences sharedPreferences;
    View view;
    WebView webView;

    public TimetableFragment() {
        // Lege constructor is nodig om een fragment te kunnen gebruiken
    }

    public static TimetableFragment newInstance() {
        TimetableFragment fragment = new TimetableFragment();
        return fragment;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reload:
                loadTimetable();
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
        webView = (WebView) view.findViewById(R.id.web_view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (loadSharedPreferences() == 0) {
            setWeekChangePosition(
                    sharedPreferences.getInt("weekChange", 1)
            );

            loadTimetable();
        }

        /**
         * ?getIndex=1&type=afdelingen
         *
         * id | weergavetekst
         * TW1|TW1\n\r
         * TW2|TW2
         * string.split("|", 2);
         */

        Spinner weekSpinner = (Spinner) getActivity().findViewById(R.id.week_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, weekMogelijkheden);
        weekSpinner.setAdapter(adapter);

        weekSpinner.setSelection(sharedPreferences.getInt("weekChange", 1));

        weekSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setWeekChangePosition(position);
                sharedPreferences.edit().putInt("weekChange", position).apply();

                loadTimetable();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    private void setWeekChangePosition(int position) {
        switch(position) {
            case 0:
                weekChange = e_WEEKCHANGE.YWEEK;
                break;
            case 1:
                weekChange = e_WEEKCHANGE.TWEEK;
                break;
            case 2:
                weekChange = e_WEEKCHANGE.NWEEK;
                break;
            case 3:
                weekChange = e_WEEKCHANGE.ANWEEK;
        }
    }

    private void loadTimetable() {
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        if (online()) {
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        } else {
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }

        getTimetable();
    }

    private boolean online() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }

    private void getTimetable() {
        Calendar calendar = Calendar.getInstance();
        Date time = new Date();
        calendar.setTime(time);

        int week = calendar.get(Calendar.WEEK_OF_YEAR);

        switch (weekChange) {
            case YWEEK:
                week --;
                break;
            case TWEEK:
                break;
            case NWEEK:
                week ++;
                break;
            case ANWEEK:
                week = week + 2;
        }

        String typeString;
        typeString = getType(code);

        String requestString = "https://btrfrontend.appspot.com/RoosterEmbedServlet" +
                "?type=" + typeString + "&locatie=" + locatie + "&code=" + code + "&week=" + week;

        Log.d("url", requestString);

        webView.loadUrl(requestString);
    }

    private int loadSharedPreferences() {
        if (!sharedPreferences.contains("code")) {
            showCodeDialog();
            return 1;
        }

        if (!sharedPreferences.contains("locatie")) {
            showLocatieDialog();
            return 1;
        }

        code = sharedPreferences.getString("code", "12345");

        locatie = sharedPreferences.getString("locatie", locaties[0]);
        type = getType(code);

        String sharedPreferencesInfo = "Code: " + code + ", locatie: " + locatie + ", type: " + type;
        Log.v("SharedPreferencesInfo", sharedPreferencesInfo);

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

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sharedPreferences.edit().putString("code", input.getText().toString()).apply();
                onStart();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showLocatieDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Locatie");

        builder.setItems(locaties, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sharedPreferences.edit().putString("locatie", locatiesURL[which]).apply();
                onStart();
            }
        });

        builder.show();
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
