package glassa.tacoma.uw.edu.moviesproject.movie;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import glassa.tacoma.uw.edu.moviesproject.R;

public class MovieActivity extends AppCompatActivity {

    /**
     * The base of the URL command to follow a user.
     */
    private static final String RATE_MOVIE_URL = "http://cssgate.insttech.washington.edu/~_450team2/rateMovie?";

    String mCurrentUser;
    String mCurrentMovie;
    int mCurrentMovieID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get current user.
        mCurrentUser = getIntent().getStringExtra("CURRENT_USER");

        mCurrentMovie = getIntent().getStringExtra("MOVIE_TITLE");

        mCurrentMovieID = getIntent().getIntExtra("MOVIE_ID", 0);

        setContentView(R.layout.activity_movie);


        TextView tv = (TextView) findViewById(R.id.movie_page_title);
        tv.setText(mCurrentMovie);
    }

    /**
     * The AsyncTask to follow a user.  This method connects to the database and inserts
     * into the database that the Current user follows the Target user.
     */
    private class RateMovieTask extends AsyncTask<String, Void, String> {

        /**
         * Connects the the database and opens an input stream to listen for the server's response.
         * If any exception is thrown, the response is changed to show the exception.
         * @param urls The pre-appended url to connect to the database.
         * @return Returns a response from the server in the form of a JSON object.
         *          The three possible responses are {result: "success"} and {result: "failure"}
         *          and " 'Unable to find user, Reason: ' + Exception.getMessage()"
         */
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            HttpURLConnection urlConnection = null;
            for (String url : urls) {
                try {
                    URL urlObject = new URL(url);
                    urlConnection = (HttpURLConnection) urlObject.openConnection();

                    InputStream content = urlConnection.getInputStream();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }

                } catch (Exception e) {
                    response = "Unable to find user, Reason: "
                            + e.getMessage();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }
            return response;
        }

        /**
         * It checks to see if there was a problem with the URL(Network) which is when an
         * exception is caught. It tries to call the parse Method and checks to see if it was successful.
         * If it was, it takes the user to the TabHostActivity via an intent.
         * If not, it displays the exception.
         *
         * @param result Passed by doInBackground. Used to determine if the user login was successful.
         */
        @Override
        protected void onPostExecute(String result) {
            if (result.startsWith("Unable to")) {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG)
                        .show();
                return;
            }

            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = (String) jsonObject.get("result");
                if (status.equals("success")) {
                    Toast.makeText(getApplicationContext(), "You just rated " + mCurrentMovie
                            , Toast.LENGTH_SHORT)
                            .show();

                } else {
                    Toast.makeText(getApplicationContext(), "Failed: You've already rated " + mCurrentMovie
                            , Toast.LENGTH_SHORT)
                            .show();
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Something wrong with the data" +
                        e.getMessage(), Toast.LENGTH_LONG).show();
            }


        }
    }

    public void rateLike(View view) {
        RateMovieTask task = new RateMovieTask();
        task.execute(buildUserURL(view, 1));
    }

    public void rateNoSee(View view) {
        RateMovieTask task = new RateMovieTask();
        task.execute(buildUserURL(view, 2));
    }

    public void rateDislike(View view) {
        RateMovieTask task = new RateMovieTask();
        task.execute(buildUserURL(view, 3));
    }

    /**
     * Builds the URL for the FollowUser AsyncTask.  It creates the command for
     * @param v
     * @return
     */
    private String buildUserURL(View v, int ratingNum) {

        StringBuilder sb = new StringBuilder(RATE_MOVIE_URL);

        try {
            sb.append("Username=");
            sb.append(URLEncoder.encode(mCurrentUser, "UTF-8"));

            sb.append("&MovieID=" + mCurrentMovieID);

            sb.append("&Rating=" + ratingNum);

            Log.i("MovieActivity", "URL=" + sb.toString());
        }
        catch(Exception e) {
            Toast.makeText(v.getContext(), "Something wrong with the url" + e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }
        return sb.toString();
    }

}