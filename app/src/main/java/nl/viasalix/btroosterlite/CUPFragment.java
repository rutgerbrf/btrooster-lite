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

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

public class CUPFragment extends Fragment {
    View view;
    WebView webView;

    public CUPFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static CUPFragment newInstance() {
        CUPFragment fragment = new CUPFragment();
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
        view = inflater.inflate(R.layout.fragment_cup, container, false);
        webView = (WebView) view.findViewById(R.id.web_view);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });

        return view;
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
                loadCUP();
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
    public void onResume() {
        super.onResume();

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("CUP");
        }

        webView.loadUrl("https://ccgobb.cupweb6.nl/");
    }

    private void loadCUP() {
        webView.loadUrl("https://ccgobb.cupweb6.nl/");
    }
}
