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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class TestTimetableFragment extends Fragment {
    View view;
    WebView webView;
    SharedPreferences sharedPreferences;

    String code;
    String locatie;

    public TestTimetableFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
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
        webView = (WebView) view.findViewById(R.id.web_view);
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
                loadTestTimetable();
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

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Toetsrooster");
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        code = sharedPreferences.getString("code", "12345");
        locatie = sharedPreferences.getString("locatie", "Goes");

        loadTestTimetable();
    }

    private boolean online() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }

    private void loadTestTimetable() {
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        if (online()) {
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        } else {
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }

        getTestTimetable();
    }

    private void getTestTimetable() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("btrfrontend.appspot.com")
                .appendPath("ToetsroosterEmbedServlet")
                .appendQueryParameter("code", code)
                .appendQueryParameter("locatie", locatie)
                .appendQueryParameter("type", "leerlingen")
                .appendQueryParameter("toetsweek", "TW2");
        String url = builder.build().toString();

        webView.loadUrl(url);
    }
}
