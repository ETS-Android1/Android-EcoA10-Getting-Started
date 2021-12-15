package com.ecopaynet.ecoa10_gettingstarted;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ecopaynet.module.paymentpos.Error;

import kotlinx.serialization.json.Json;

public class TransactionErrorActivity extends AppCompatActivity {
    TextView transactionErrorTextView;
    com.ecopaynet.module.paymentpos.Error transactionError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_error);

        transactionError = Json.Default.decodeFromString(Error.Companion.serializer(), getIntent().getStringExtra("TRANSACTION_ERROR"));

        transactionErrorTextView = (TextView) findViewById(R.id.transactionResultTextView);

        Button closeButton = (Button) findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        fillTransactionResult();
    }

    private void fillTransactionResult() {
        String message = "";

        message += transactionError.getCode() + "\r\n";
        message += transactionError.getMessage() + "\r\n";

        transactionErrorTextView.setText(message);
    }

}
