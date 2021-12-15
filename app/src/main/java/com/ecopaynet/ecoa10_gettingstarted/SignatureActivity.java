package com.ecopaynet.ecoa10_gettingstarted;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.ecopaynet.module.paymentpos.SignatureView;
import com.ecopaynet.module.paymentpos.TransactionRequestSignatureInformation;

import kotlinx.serialization.json.Json;

public class SignatureActivity extends AppCompatActivity
{
    SignatureView signatureView;
    TransactionRequestSignatureInformation transactionRequestSignatureInformation;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);

        signatureView = (SignatureView) findViewById(R.id.signatureView);

        transactionRequestSignatureInformation = Json.Default.decodeFromString(TransactionRequestSignatureInformation.Companion.serializer(), getIntent().getStringExtra("TRANSACTION_INFORMATION"));

        Button signatureContinueButton = (Button) findViewById(R.id.signatureContinueButton);
        signatureContinueButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();

                intent.putExtra("SIGNATURE_BITMAP", signatureView.getSignatureBitmap());

                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }
}
