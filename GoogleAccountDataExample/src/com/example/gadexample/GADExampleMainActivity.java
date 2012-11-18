package com.example.gadexample;

import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.googleloginexample.R;

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
    	Account googleAccount = getGoogleAccount();
    	if(googleAccount != null){
    		new GADExampleBasicDataAction(this, googleAccount).execute();
    	}else{
    		Toast.makeText(this, R.string.error_no_google_accounts, Toast.LENGTH_LONG).show();
    	}
    	
    }

    private Account getGoogleAccount(){
    	final List<Account> googleAccounts = new ArrayList<Account>();
    	final AccountManager mAccountManager = AccountManager.get(this);
    	Account[] accounts = mAccountManager.getAccountsByType("com.google");
    	for (Account account : accounts) {
    	  if (account.name.contains("@gmail")) {
    	    googleAccounts.add(account);
    	  }
    	}
    	return googleAccounts.size()>0?googleAccounts.get(0):null;
    }
    


}
