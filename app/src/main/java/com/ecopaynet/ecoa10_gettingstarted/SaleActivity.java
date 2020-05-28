package com.ecopaynet.ecoa10_gettingstarted;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class SaleActivity extends AppCompatActivity
    implements View.OnClickListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale);

        EditText amountEditText = (EditText) findViewById(R.id.amountEditText);
        amountEditText.setRawInputType(Configuration.KEYBOARD_12KEY);
        amountEditText.addTextChangedListener(new CurrencyTextWatcher(amountEditText));
        amountEditText.setSelection(4);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        Button performTransactionButton = (Button) findViewById(R.id.performTransactionButton);
        performTransactionButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.performTransactionButton)
        {
            Intent intent = new Intent(this, PerformTransactionActivity.class);
            intent.putExtra("TRANSACTION_TYPE", "SALE");

            EditText amountEditText = (EditText) findViewById(R.id.amountEditText);
            String amount = amountEditText.getText().toString().replace(" EUR", "").replace(",", "");
            if (Integer.parseInt(amount) > 0)
            {
                intent.putExtra("AMOUNT", amount);

                startActivity(intent);
                finish();
            }
            else
            {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Incorrect amount");
                alertDialogBuilder.setMessage("Amount must be great than zero");
                alertDialogBuilder.setPositiveButton("OK", null);
                alertDialogBuilder.show();
            }
        }
    }
}
