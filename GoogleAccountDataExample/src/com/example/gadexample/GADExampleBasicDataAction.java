package com.example.gadexample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.googleloginexample.R;

/**
 * 
 * @author angelcereijo
 *
 */
public class GADExampleBasicDataAction extends AsyncTask<Void, Void, Boolean> {
	
	private final static String TAG = GADExampleBasicDataAction.class.toString();
	
	final static String OAUTH_PROFILE = "https://www.googleapis.com/auth/userinfo.profile";
	final static String OAUTH2_SCOPE ="oauth2:" + OAUTH_PROFILE;
	final static String URL_GET_USER_INFO = "https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=%s";
	
	private ProgressDialog progressDialog;
	
	private Account googleAccount;
	private Activity actualActivity;
	
	private String name;
	private Drawable pictureProfile;
	
	/**
	 * 
	 * @param actualActivity
	 * @param googleAccount
	 */
	public GADExampleBasicDataAction(Activity actualActivity, Account googleAccount){
		this.actualActivity = actualActivity;
		this.googleAccount = googleAccount;
	}
	
	
	@Override
	protected void onPreExecute() {
		String title = actualActivity.getString(R.string.load_data_progreess_title);
		String message = String.format(actualActivity.getString(R.string.load_data_progreess_message),
				googleAccount.name);
		progressDialog = ProgressDialog.show(actualActivity, title, message);
	}
	
	
	@Override
	protected Boolean doInBackground(Void... arg0) {
		Boolean dataLoaded = Boolean.FALSE;
		try{
			AccountManager mAccountManager = AccountManager.get(actualActivity);
			AccountManagerFuture<Bundle> response = mAccountManager.getAuthToken(googleAccount,
				OAUTH2_SCOPE, null, actualActivity, null, null);

    		Bundle authTokenBundle = response.getResult();
    		String authToken = authTokenBundle.getString(AccountManager.KEY_AUTHTOKEN).toString();
    	    
    	    HttpGet get = new HttpGet(String.format(URL_GET_USER_INFO,authToken));
    		HttpClient c = new DefaultHttpClient();
    		HttpResponse resp = c.execute(get);
    		StatusLine statusLine = resp.getStatusLine();
    	      int statusCode = statusLine.getStatusCode();
    	      if (statusCode == 200) {
    	    	  JSONObject googleAccountData = readJSONResponse(resp);
    	    	  loadData(googleAccountData);
    	    	  dataLoaded = Boolean.TRUE;
    	      } else {
    	        Log.e(TAG, "Failed Get google account data");
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
		return dataLoaded;
	}
	
	
	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		progressDialog.cancel();
		if(result){
			showAccountData();
		}else{
			String errorMessage = String.format(actualActivity.getString(R.string.load_data_error)
					,googleAccount.name);
			Toast.makeText(actualActivity, errorMessage,Toast.LENGTH_LONG).show();
		}		
	}
	
	
	private void loadData(JSONObject googleAccountData) throws JSONException{
		name = googleAccountData.getString("given_name");
		String pictureUrl = googleAccountData.getString("picture");
		pictureProfile = getUserProfileImage(pictureUrl);
	}
	
	
	private void showAccountData(){
		TextView textViewGData = (TextView)actualActivity.findViewById(R.id.google_data);
		textViewGData.setText(name);
		
		Bitmap bitmap = ((BitmapDrawable) pictureProfile).getBitmap();
		pictureProfile = new BitmapDrawable(Bitmap.createScaledBitmap(bitmap, 250, 250, true));
		
		textViewGData.setCompoundDrawablesWithIntrinsicBounds(null, pictureProfile, null, null);
		
		textViewGData.setVisibility(View.VISIBLE);
		((Button)actualActivity.findViewById(R.id.google_login_buttom)).setVisibility(View.GONE);
		
	}
	
	private Drawable getUserProfileImage(String urlPicture){
		Drawable userPictureProfile = null;
    	try{
			HttpGet get = new HttpGet(urlPicture);
			HttpClient c = new DefaultHttpClient();
			HttpResponse resp = c.execute(get);
			int statusCode = resp.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				InputStream in = resp.getEntity().getContent();
				userPictureProfile = Drawable.createFromStream(in, "profile_image_url");
			} else {
				Log.e(TAG, "Failed getting google picture profile");
			}
		} catch (ClientProtocolException e) {
			Log.e(TAG, "Get Twitter info",e);
		} catch (IOException e) {
			Log.e(TAG, "Get Twitter info",e);
		}
		return userPictureProfile;
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
