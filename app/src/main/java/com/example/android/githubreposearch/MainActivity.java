package com.example.android.githubreposearch;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.githubreposearch.Utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;

// implement LoaderManager.LoaderCallbacks<String> on MainActivity
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {
    // COMPLETED (26) Create an EditText variable called mSearchBoxEditText
    private EditText mSearchBoxEditText;

    // COMPLETED (26) Create an TextView variable called mSearchBoxEditText
    private TextView mUrlDisplayTextView;

    // COMPLETED (26) Create an TextView variable called mSearchBoxEditText
    private TextView mSearchResultTextView;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;
    //Create a static final key to store the query's URL
    private static final String SEARCH_QUEARY_URL_EXTRA = "query";

    //Remove the key for storing the search results JSON
    // Create a constant int to uniquely identify your loader. Call it GITHUB_SEARCH_LOADER
    private static final int GITHUB_SEARCH_LOADER = 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // COMPLETED (29) Use findViewById to get a reference to mSearchBoxEditText
        mSearchBoxEditText = (EditText)findViewById(R.id.edit_search_box);

        mUrlDisplayTextView = (TextView) findViewById(R.id.tv_display_url);

        mSearchResultTextView = (TextView) findViewById(R.id.tv_github_search_results_json);

        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        //If the savedInstanceState bundle is not null, set the text of the URL and search results TextView respectively
        if(savedInstanceState != null){
            String queryUrl = savedInstanceState.getString(SEARCH_QUEARY_URL_EXTRA);
            //Remove the code that retrieves the JSON
            mUrlDisplayTextView.setText(queryUrl);
            //Remove the code that displays the JSON
        }
        //Initialize the loader with GITHUB_SEARCH_LOADER as the ID, null for the bundle, and this for the context
        getSupportLoaderManager().initLoader(GITHUB_SEARCH_LOADER,null,this);
    }


    private void makeGithubSearchQuery() {
        String githubQuery = mSearchBoxEditText.getText().toString();
        //If no search was entered, indicate that there isn't anything to search for and return
        if(TextUtils.isEmpty(githubQuery)){
            mSearchResultTextView.setText("No query entered, nothing to search for");
            return;
        }

        URL githubSearchUrl = NetworkUtils.buildUrl(githubQuery);
        mUrlDisplayTextView.setText(githubSearchUrl.toString());
        // COMPLETED (4) Create a new GithubQueryTask and call its execute method, passing in the url to query
        //Remove the call to execute the AsyncTask
        //Create a bundle called queryBundle
        Bundle queryBundle = new Bundle();
        //Use putString with SEARCH_QUERY_URL_EXTRA as the key and the String value of the URL as the value
        queryBundle.putString(SEARCH_QUEARY_URL_EXTRA, githubSearchUrl.toString());

        //Call getSupportLoaderManager and store it in a LoaderManager variable
        LoaderManager loaderManager = getSupportLoaderManager();
        //Get our Loader by calling getLoader and passing the ID we specified
        Loader<String> githubSearchLoader = loaderManager.getLoader(GITHUB_SEARCH_LOADER);
        //If the Loader was null, initialize it. Else, restart it.
        if(githubSearchLoader == null){
            loaderManager.initLoader(GITHUB_SEARCH_LOADER, queryBundle, this);
        }else {
            loaderManager.restartLoader(GITHUB_SEARCH_LOADER, queryBundle, this);
        }
    }

    private void showJsonDataView() {
        // First, make sure the error is invisible
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        // Then, make sure the JSON data is visible
        mSearchResultTextView.setVisibility(View.VISIBLE);
    }


    private void showErrorMessage() {
        // First, hide the currently visible data
        mSearchResultTextView.setVisibility(View.INVISIBLE);
        // Then, show the error
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    //Override onCreateLoader
    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args){
// Within onCreateLoader
        //Return a new AsyncTaskLoader<String> as an anonymous inner class with this as the constructor's parameter
        return new AsyncTaskLoader<String>(this) {

            //Create a String member variable called mGithubJson that will store the raw JSON
              String mGithubJson;
            //Override onStartLoading
            // Within onStartLoading
            @Override
            protected void onStartLoading(){
                //If args is null, return.
               if(args == null){
                   return;
               }
                //Show the loading indicator
                mLoadingIndicator.setVisibility(View.VISIBLE);
                //Force a load
                // END - onStartLoading
                //If mGithubJson is not null, deliver that result. Otherwise, force a load
                if(mGithubJson != null){
                    deliverResult(mGithubJson);
                }else {
                    forceLoad();
                }
            }
            //Override loadInBackground
            @Override
            public String loadInBackground(){
             // Within loadInBackground
            //Get the String for our URL from the bundle passed to onCreateLoader
              String searchQueryUrlString = args.getString(SEARCH_QUEARY_URL_EXTRA);
                //If the URL is null or empty, return null
                if(searchQueryUrlString == null || TextUtils.isEmpty(searchQueryUrlString)){
                    return null;
                }
            //Copy the try / catch block from the AsyncTask's doInBackground method
             // END - loadInBackground
                try {
                    URL githubUrl = new URL(searchQueryUrlString);
                    String githubSearchResults = NetworkUtils.getResponseFromHttpUrl(githubUrl);
                    return githubSearchResults;
                }catch (IOException e){
                    e.printStackTrace();
                    return null;
                }
            }
            //Override deliverResult and store the data in mGithubJson
            @Deprecated
            public void deliverResult(String githubJson){
                mGithubJson = githubJson;
                //Call super.deliverResult after storing the data
                super.deliverResult(githubJson);
            }
        };
    }

    //Override onLoadFinished
    @Override
    public void onLoadFinished(Loader<String> loader, String data){
        // Within onLoadFinished
        //Hide the loading indicator
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        //Use the same logic used in onPostExecute to show the data or the error message
        // END - onLoadFinished
        if(null == data){
            showErrorMessage();
        }else {
            mSearchResultTextView.setText(data);
            showJsonDataView();
        }

    }

    //Override onLoaderReset as it is part of the interface we implement, but don't do anything in this method
    @Override
    public void onLoaderReset(Loader<String> loader){

    }

    //Delete the AsyncTask class

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();
        if (itemThatWasClickedId == R.id.action_search) {
            makeGithubSearchQuery();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Override onSaveInstanceState to persist data across Activity recreation
    @Override
    protected void onSaveInstanceState(Bundle outState){
        // Do the following steps within onSaveInstanceState
        //Make sure super.onSaveInstanceState is called before doing anything else
        super.onSaveInstanceState(outState);
        //Put the contents of the TextView that contains our URL into a variable
        String queryUrl = mUrlDisplayTextView.getText().toString();
        //Using the key for the query URL, put the string in the outState Bundle
        outState.putString(SEARCH_QUEARY_URL_EXTRA, queryUrl);
        //Remove the code that persists the JSON
    }

}
