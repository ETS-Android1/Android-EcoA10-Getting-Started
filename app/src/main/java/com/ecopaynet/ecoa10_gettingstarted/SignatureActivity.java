package com.ecopaynet.ecoa10_gettingstarted;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.ecopaynet.ecoa10.BitmapSerializable;
import com.ecopaynet.ecoa10.SignatureView;
import com.ecopaynet.ecoa10.TransactionRequestSignatureInformation;

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

        transactionRequestSignatureInformation = (TransactionRequestSignatureInformation) getIntent().getSerializableExtra("TRANSACTION_INFORMATION");

        Button signatureContinueButton = (Button) findViewById(R.id.signatureContinueButton);
        signatureContinueButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent=new Intent();

                BitmapSerializable bitmapSerializable = new BitmapSerializable(signatureView.getSignatureBitmap());
                intent.putExtra("SIGNATURE_BITMAP", bitmapSerializable);

                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }
}
