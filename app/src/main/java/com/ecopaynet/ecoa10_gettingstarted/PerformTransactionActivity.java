package com.ecopaynet.ecoa10_gettingstarted;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ecopaynet.module.paymentpos.Error;
import com.ecopaynet.module.paymentpos.Events;
import com.ecopaynet.module.paymentpos.PaymentPOS;
import com.ecopaynet.module.paymentpos.TransactionRequestSignatureInformation;
import com.ecopaynet.module.paymentpos.TransactionResult;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import kotlinx.serialization.json.Json;

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
                switch (intent.getStringExtra("TRANSACTION_TYPE")) {
                    case "SALE": {
                        Long amount = Long.parseLong(intent.getStringExtra("AMOUNT"));
                        if (PaymentPOS.sale(amount, this)) {
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
                    case "REFUND": {
                        Long amount = Long.parseLong(intent.getStringExtra("AMOUNT"));
                        String authorizationCode = intent.getStringExtra("AUTHORIZATION_CODE");
                        String operationNumber = intent.getStringExtra("OPERATION_NUMBER");
                        LocalDate saleDate = LocalDate.parse(intent.getStringExtra("SALE_DATE"), DateTimeFormatter.ofPattern("ddMMyyyy")).atStartOfDay().toLocalDate();

                        if (PaymentPOS.refund(amount, operationNumber, authorizationCode, kotlinx.datetime.LocalDate.Companion.parse(saleDate.toString()), this)) {
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
        intent.putExtra("TRANSACTION_INFORMATION", Json.Default.encodeToString(TransactionRequestSignatureInformation.Companion.serializer(), transactionRequestSignatureInformation));
        startActivityForResult(intent, REQUEST_SIGNATURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SIGNATURE: {
                if (resultCode == RESULT_OK) {
                    byte[] signatureBitmap = data.getByteArrayExtra("SIGNATURE_BITMAP");
                    PaymentPOS.returnTransactionRequestedSignature(signatureBitmap);
                } else {
                    PaymentPOS.returnTransactionRequestedSignature(null);
                }
            }
            break;
        }
    }

    @Override
    public void onTransactionComplete(TransactionResult transactionResult)
    {
        Intent intent = new Intent(this, TransactionCompleteActivity.class);
        intent.putExtra("TRANSACTION_RESULT", Json.Default.encodeToString(TransactionResult.Companion.serializer(), transactionResult));
        startActivity(intent);
        finish();
    }

    @Override
    public void onTransactionError(Error error)
    {
        Intent intent = new Intent(this, TransactionErrorActivity.class);
        intent.putExtra("TRANSACTION_ERROR", Json.Default.encodeToString(Error.Companion.serializer(), error));
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
