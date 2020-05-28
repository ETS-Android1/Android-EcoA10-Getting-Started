package com.ecopaynet.ecoa10_gettingstarted;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RefundActivity extends Activity
    implements View.OnClickListener
{
    private DatePickerDialog datePickerDialog;
    private SimpleDateFormat dateFormatter;
    private EditText saleDateEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refund);

        EditText amountEditText = (EditText) findViewById(R.id.amountEditText);
        amountEditText.setRawInputType(Configuration.KEYBOARD_12KEY);
        amountEditText.addTextChangedListener(new CurrencyTextWatcher(amountEditText));
        amountEditText.setSelection(4);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        Button performTransactionButton = (Button) findViewById(R.id.performTransactionButton);
        performTransactionButton.setOnClickListener(this);

        saleDateEditText = (EditText) findViewById(R.id.saleDateEditText);
        saleDateEditText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                datePickerDialog.show();
            }
        });

        Calendar newCalendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener()
        {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                saleDateEditText.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.performTransactionButton)
        {
            Intent intent = new Intent(this, PerformTransactionActivity.class);
            intent.putExtra("TRANSACTION_TYPE", "REFUND");

            EditText amountEditView = (EditText) findViewById(R.id.amountEditText);
            String amount = amountEditView.getText().toString().replace(" EUR", "").replace(",", "");
            if (Integer.parseInt(amount) > 0)
            {
                intent.putExtra("AMOUNT", amount);

                EditText authorizationCodeEditText = (EditText) findViewById(R.id.authorizationCodeEditText);
                intent.putExtra("AUTHORIZATION_CODE", authorizationCodeEditText.getText().toString());

                EditText operationNumberEditText = (EditText) findViewById(R.id.operationNumberEditText);
                intent.putExtra("OPERATION_NUMBER", operationNumberEditText.getText().toString());

                EditText saleDateEditText = (EditText) findViewById(R.id.saleDateEditText);
                intent.putExtra("SALE_DATE", saleDateEditText.getText().toString().replace("/", ""));

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
