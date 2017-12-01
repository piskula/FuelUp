package sk.momosi.fuelup.screens.backup;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import pub.devrel.easypermissions.EasyPermissions;
import sk.momosi.fuelup.R;
import sk.momosi.fuelup.adapters.ListAccountsAdapter;

import static sk.momosi.fuelup.screens.backup.CheckPermissionsActivity.KEY_ACCOUNT_FROM_CHECK_PERMISSIONS;

/**
 * @author Ondro
 * @version 12.11.2017
 */
public class ChooseAccountActivity extends AppCompatActivity implements View.OnClickListener, ListAccountsAdapter.Callback {

    private static final String LOG_TAG = ChooseAccountActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1001;
    public static final String KEY_IS_THIS_FIRST_RUN = "is_this_first_run";

    private ListAccountsAdapter adapter;

    private ProgressBar progressBar;
    private Button buttonNext;
    private Button buttonSkip;
    private RecyclerView accounts;

    private final Bundle bundle = new Bundle();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_account);

        progressBar = findViewById(R.id.chooseAccount_progress);
        buttonNext = findViewById(R.id.chooseAccount_btnNext);
        buttonSkip = findViewById(R.id.chooseAccount_btnSkip);
        accounts = findViewById(R.id.listAccounts);

        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        progressBar.setVisibility(View.VISIBLE);

        buttonNext.setOnClickListener(this);
        buttonSkip.setOnClickListener(this);
        accounts.setLayoutManager(new LinearLayoutManager(this));

        if (adapter == null)
            adapter = new ListAccountsAdapter(this, this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getBoolean(KEY_IS_THIS_FIRST_RUN, false)) {
                buttonSkip.setVisibility(View.VISIBLE);
            }
        }

        init();
    }

    private void init() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            accounts.setVisibility(View.VISIBLE);
            accounts.setAdapter(adapter);
            progressBar.setVisibility(View.GONE);

        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.googleDrive_accountListNeeded),
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.chooseAccount_btnNext:
                Intent intent = new Intent(this, CheckPermissionsActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                // finish(); //TODO if we do not want to get back to this page
                break;
            case R.id.chooseAccount_btnSkip:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onItemClick(final String account) {
        buttonNext.setVisibility(account != null ? View.VISIBLE : View.GONE);
        bundle.putString(KEY_ACCOUNT_FROM_CHECK_PERMISSIONS, account);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        adapter.setChosenAccount(savedInstanceState.getString(KEY_ACCOUNT_FROM_CHECK_PERMISSIONS));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_ACCOUNT_FROM_CHECK_PERMISSIONS, bundle.getString(KEY_ACCOUNT_FROM_CHECK_PERMISSIONS));
        super.onSaveInstanceState(outState);
    }
}
