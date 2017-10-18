package sk.momosi.fuelup.business.googledrive.authenticator;

import android.accounts.Account;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Martin Styk on 18.10.2017.
 */
public class AccountService extends Service {

    public static final String ACCOUNT_NAME = "name";
    public static final String ACCOUNT_TYPE = "sk.momosi.fuelup.account";

    // Instance field that stores the authenticator object
    private Authenticator mAuthenticator;
    @Override
    public void onCreate() {
        mAuthenticator = new Authenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

    public static Account getAccount(){
        return new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
    }
}