package com.wallet.league.leaguewallet;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class WalletFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList> {

    private final String LOG_TAG = this.getClass().toString();
    private static final int FETCH_CARDS_LOADER = 22;

    CustomAdapter adapter = new CustomAdapter();
    private RecyclerView.LayoutManager layoutManager;

    TextView errorMessageTextView;
    RecyclerView recyclerView;
    ProgressBar progressBar;

    public WalletFragment() {
        // Required empty public constructor
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.action_refresh) {
            updateCards();
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateCards();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wallet, container, false);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView = (RecyclerView) rootView.findViewById(R.id.listview_cards);
        errorMessageTextView = (TextView) rootView.findViewById(R.id.error_message);
        progressBar = (ProgressBar) rootView.findViewById(R.id.pb_loading_indicator);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        return rootView;
    }

    @Override
    public Loader<ArrayList> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<ArrayList>(getContext()) {

            ArrayList queryResult;

            @Override
            protected void onStartLoading() {

                progressBar.setVisibility(View.VISIBLE);

                if (queryResult != null) {
                    deliverResult(queryResult);
                } else {
                    forceLoad();
                }
            }

            @Override
            public void deliverResult(ArrayList data) {
                queryResult = data;
                super.deliverResult(data);
            }

            @Override
            public ArrayList loadInBackground() {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                String cardJsonStr = null;

                try {
                    final String BASE_URL =
                            "https://gist.githubusercontent.com/Shanjeef/3562ebc5ea794a945f723de71de1c3ed/raw/25da03b403ffa860dd68a9bfc84f562262ee5ca5/walletEndpoint";

                    Uri.Builder uri = Uri.parse(BASE_URL).buildUpon();

                    URL url = new URL(uri.toString());

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(5000);
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream==null) {
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }

                    if (buffer.length()==0) {
                        return null;
                    }
                    cardJsonStr = buffer.toString();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error ", e);
                    return null;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try{
                            reader.close();
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "Error closing stream", e);
                            return null;
                        }
                    }
                }

                try {
                    return getDataFromJson(cardJsonStr);
                } catch (JSONException e) {
                    Log.e(LOG_TAG,e.getMessage(),e);
                    e.printStackTrace();
                    return null;
                }
            }

        };
    }

    @Override
    public void onLoadFinished(Loader<ArrayList> loader, ArrayList data) {
        progressBar.setVisibility(View.INVISIBLE);
        if (data != null)
        {
            showData();
            adapter.clear();
            for (Object card : data) {
                adapter.addItem((Card) card);
            }
        } else {
            showError();
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList> loader) {

    }

    private void updateCards() {

        Bundle bundle = new Bundle();

        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        Loader<ArrayList> updateWeatherLoader = loaderManager.getLoader(FETCH_CARDS_LOADER);

        if (updateWeatherLoader != null && updateWeatherLoader.isReset()) {
            loaderManager.restartLoader(FETCH_CARDS_LOADER, bundle, this);
        } else {
            loaderManager.initLoader(FETCH_CARDS_LOADER, bundle, this);
        }

    }

    private ArrayList getDataFromJson(String cardsJsonStr)
            throws JSONException {

        ArrayList<Card> cards = new ArrayList<>();

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_INFO = "info";
        final String OWM_CARDS = "cards";
        final String OWM_TYPE = "type";
        final String OWM_AMOUNT = "amount";
        final String OWM_CURRENCY = "currency";
        final String OWM_START = "policy_start_date";
        final String OWM_END = "policy_end_date";

        JSONObject cardsJson = new JSONObject(cardsJsonStr).getJSONObject(OWM_INFO);
        JSONArray cardsArray = cardsJson.getJSONArray(OWM_CARDS);

        for(int i = 0; i < cardsArray.length(); i++) {

            // Get the JSON object representing the card
            JSONObject card = cardsArray.getJSONObject(i);
            String type = card.getString(OWM_TYPE);
            int amount = card.getInt(OWM_AMOUNT);
            String currency = card.getString(OWM_CURRENCY);
            String startDate = card.getString(OWM_START);
            String endDate = card.getString(OWM_END);

            cards.add(new Card(type,amount,currency,startDate,endDate));
        }
        return cards;

    }

    private void showData() {
        recyclerView.setVisibility(View.VISIBLE);
        errorMessageTextView.setVisibility(View.INVISIBLE);
    }

    private void showError() {
        errorMessageTextView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }
}
