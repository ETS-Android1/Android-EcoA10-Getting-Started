package com.ecopaynet.ecoa10_gettingstarted;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

import com.ecopaynet.ecoa10.BitmapSerializable;
import com.ecopaynet.ecoa10.EcoA10;
import com.ecopaynet.ecoa10.Error;
import com.ecopaynet.ecoa10.Events;
import com.ecopaynet.ecoa10.TransactionRequestSignatureInformation;
import com.ecopaynet.ecoa10.TransactionResult;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR;

public class PerformTransactionActivity extends AppCompatActivity
    implements Events.Transaction
{
    TextView deviceMessagesTextView;

    SharedPreferences sharedPreferences;
    String sharedPreferencesKey = "com.ecopaynet.ecoa10_gettingstarted_preferences";

    int startingOrientation = Configuration.ORIENTATION_UNDEFINED;

    static final int REQUEST_SIGNATURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        try
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_perform_transaction);

            sharedPreferences = this.getSharedPreferences(sharedPreferencesKey, Context.MODE_PRIVATE);

            deviceMessagesTextView = (TextView) findViewById(R.id.deviceMessagesTextView);

            if (savedInstanceState == null)
            {
                this.startingOrientation = this.getResources().getConfiguration().orientation;

                Intent intent = getIntent();
                switch (intent.getStringExtra("TRANSACTION_TYPE"))
                {
                    case "SALE":
                    {
                        BigInteger amount = new BigInteger(intent.getStringExtra("AMOUNT"));
                        if (EcoA10.sale(amount, this))
                        {
                            //ok
                        }
                        else
                        {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                            alertDialogBuilder.setTitle("Unable to perform sale");
                            alertDialogBuilder.setMessage("Incorrect parameters");
                            alertDialogBuilder.setPositiveButton("OK", null);
                            alertDialogBuilder.show();

                            finish();
                        }
                    }
                    break;
                    case "REFUND":
                    {
                        BigInteger amount = new BigInteger(intent.getStringExtra("AMOUNT"));
                        String authorizationCode = intent.getStringExtra("AUTHORIZATION_CODE");
                        String operationNumber = intent.getStringExtra("OPERATION_NUMBER");
                        Date saleDate = new SimpleDateFormat("ddMMyyyy", Locale.US).parse(intent.getStringExtra("SALE_DATE"));

                        if (EcoA10.refund(amount, operationNumber, authorizationCode, saleDate, this))
                        {
                            //ok
                        }
                        else
                        {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                            alertDialogBuilder.setTitle("Unable to perform refund");
                            alertDialogBuilder.setMessage("Incorrect parameters");
                            alertDialogBuilder.setPositiveButton("OK", null);
                            alertDialogBuilder.show();

                            finish();
                        }
                    }
                    break;
                    default:
                    {
                        finish();
                    }
                    break;
                }
            }
            else
            {
                this.startingOrientation = savedInstanceState.getInt("STARTING_ORIENTATION", SCREEN_ORIENTATION_SENSOR);
                if(this.startingOrientation != this.getResources().getConfiguration().orientation)
                {
                    switch (this.startingOrientation)
                    {
                        case Configuration.ORIENTATION_LANDSCAPE:
                            setRequestedOrientation(SCREEN_ORIENTATION_LANDSCAPE);
                            break;
                        case Configuration.ORIENTATION_PORTRAIT:
                            setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
                            break;
                        default:
                            setRequestedOrientation(SCREEN_ORIENTATION_SENSOR);
                            break;
                    }
                }
            }
        }
        catch (Exception ex)
        {
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("STARTING_ORIENTATION", this.startingOrientation);
    }

    @Override
    public void onTransactionRequestSignature(TransactionRequestSignatureInformation transactionRequestSignatureInformation)
    {
        Intent intent = new Intent(PerformTransactionActivity.this, SignatureActivity.class);
        intent.putExtra("TRANSACTION_INFORMATION", transactionRequestSignatureInformation);
		startActivityForResult(intent, REQUEST_SIGNATURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode)
        {
            case REQUEST_SIGNATURE:
            {
                if(resultCode == RESULT_OK)
                {
                    BitmapSerializable bitmapSerializable = (BitmapSerializable) data.getSerializableExtra("SIGNATURE_BITMAP");
                    EcoA10.returnTransactionRequestedSignature(bitmapSerializable.getBitmap());
                }
                else
                {
                    EcoA10.returnTransactionRequestedSignature(null);
                }
            }
            break;
        }
    }

    @Override
    public void onTransactionComplete(TransactionResult transactionResult)
    {
        Intent intent = new Intent(this, TransactionCompleteActivity.class);
        intent.putExtra("TRANSACTION_RESULT", transactionResult);
        startActivity(intent);
        finish();
    }

    @Override
    public void onTransactionError(Error error)
    {
        Intent intent = new Intent(this, TransactionErrorActivity.class);
        intent.putExtra("TRANSACTION_ERROR", error);
        startActivity(intent);
        finish();
    }

    @Override
    public void onTransactionDisplayMessage(final String s)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                deviceMessagesTextView.setText(s);
            }
        });
    }

    @Override
    public void onTransactionDisplayDCCMessage(final String s)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(PerformTransactionActivity.this);
                builder.setMessage(s);

                // add the buttons
                builder.setPositiveButton("OK", null);

                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
}
