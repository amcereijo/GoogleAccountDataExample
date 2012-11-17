package com.example.gadexample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.googleloginexample.R;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * 
 * @author angelcereijo
 *
 */
public class GADExampleMainActivity extends Activity {
	
	private final static String TAG = GADExampleMainActivity.class.toString();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glelogin);
    }
    
    public void googleAccount(View v){
    	getGoogleAccount();
    	
    }

    private void getGoogleAccount(){
    	final List<Account> googleAccounts = new ArrayList<Account>();
    	final AccountManager mAccountManager = AccountManager.get(this);
    	Account[] accounts = AccountManager.get(this).getAccounts();
    	for (Account account : accounts) {
    	  if (account.type.equals("com.google") && account.name.contains("@gmail")) {
    	    googleAccounts.add(account);
    	  }
    	}
    	
    	final GADExampleMainActivity gleLoginActivity= this;
    	 final String OAUTH2_SCOPE =
    	    "oauth2:" +
    	    "https://www.googleapis.com/auth/userinfo.profile" +
    	    " " +
    	    "https://www.googleapis.com/auth/userinfo.email";
    	final String urlGetUserInfo = "https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=%s";
    	
    	new Thread(new Runnable() {
			
			public void run() {
				AccountManagerFuture<Bundle> response = mAccountManager.getAuthToken(googleAccounts.get(0),
						OAUTH2_SCOPE, null, gleLoginActivity, null, null);

		    	Bundle authTokenBundle;
		    	String authToken;

		    	try {
		    	    authTokenBundle = response.getResult();
		    	    authToken = authTokenBundle.getString(AccountManager.KEY_AUTHTOKEN).toString();
		    	    
		    	    
		    	    HttpGet get = new HttpGet(String.format(urlGetUserInfo,authToken));
		    		HttpClient c = new DefaultHttpClient();
		    		HttpResponse resp = c.execute(get);
		    		StatusLine statusLine = resp.getStatusLine();
		    	      int statusCode = statusLine.getStatusCode();
		    	      if (statusCode == 200) {
		    	        JSONObject json = readJSONResponse(resp);
		    	        json.getString("name");
		    	      } else {
		    	        Log.e(TAG, "Failed Get Twitter info");
		    	      }
		    	} catch (OperationCanceledException e) {
		    	    Log.e(TAG, e.getMessage());
		    	} catch (AuthenticatorException e) {
		    	    Log.e(TAG, e.getMessage());
		    	} catch (IOException e) {
		    	    Log.e(TAG, e.getMessage());
		    	} catch (IllegalStateException e) {
		    		Log.e(TAG, e.getMessage());
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage());
				}
			}
		}).start();
    	
    	
    }
    
    private JSONObject readJSONResponse(HttpResponse resp) throws IllegalStateException, IOException, JSONException{
    	StringBuilder builder = new StringBuilder();
    	HttpEntity entity = resp.getEntity();
        InputStream content = entity.getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
        String line;
        while ((line = reader.readLine()) != null) {
          builder.append(line);
        }
        Log.i(TAG,builder.toString());
        JSONObject json  = new JSONObject(builder.toString());
        return json;
    }

}
