package com.ecopaynet.ecoa10_gettingstarted;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.ecopaynet.module.paymentpos.PaymentPOS;
import com.ecopaynet.module.paymentpos.TransactionResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import kotlinx.serialization.json.Json;

public class TransactionCompleteActivity extends AppCompatActivity
{
    ImageView transactionResultImageView;
    TransactionResult transactionResult;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_result);

        transactionResult = Json.Default.decodeFromString(TransactionResult.Companion.serializer(), getIntent().getStringExtra("TRANSACTION_RESULT"));

        transactionResultImageView = (ImageView) findViewById(R.id.transactionResultImageView);

        Button closeButton = (Button) findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        fillTransactionResult();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
        {
            saveTicketsPDF();
        }
        else
        {
            saveTicketsImage();
        }

    }

    private void fillTransactionResult() {
        try {
            List<Bitmap> tickets = PaymentPOS.generateTransactionTicketsBMP(transactionResult, null);

            Bitmap commerceTicketWithBorder = addBorder(tickets.get(0), Color.BLACK, 2);
            transactionResultImageView.setImageBitmap(commerceTicketWithBorder);
        }
        catch (Exception ex)
        {

        }
    }

    private Bitmap addBorder(Bitmap bmp, int borderColor, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(borderColor);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }

    @TargetApi(19)
    private void saveTicketsPDF() {
        try {
            List<PdfDocument> tickets = PaymentPOS.generateTransactionTicketsPDF(transactionResult, null);

            File tracesFolder = new File(Environment.getExternalStorageDirectory(), "/EcoA10/Tickets");
            if (!tracesFolder.exists())
                tracesFolder.mkdirs();

            String fileDateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

            for (int i = 0; i < tickets.size(); i++) {
                String fileName = Environment.getExternalStorageDirectory() + "/EcoA10/Tickets/" + fileDateTime + ((i == 0) ? "" : "_CC") + ".pdf";
                tickets.get(i).writeTo(new FileOutputStream(fileName, false));
            }
        }
        catch(Exception ex)
        {
        }
    }

    private void saveTicketsImage() {
        try {
            List<Bitmap> tickets = PaymentPOS.generateTransactionTicketsBMP(transactionResult, null);

            File tracesFolder = new File(Environment.getExternalStorageDirectory(), "/EcoA10/Tickets");
            if (!tracesFolder.exists())
                tracesFolder.mkdirs();

            String fileDateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

            for (int i = 0; i < tickets.size(); i++) {
                String fileName = Environment.getExternalStorageDirectory() + "/EcoA10/Tickets/" + fileDateTime + ((i == 0) ? "" : "_CC") + ".png";
                OutputStream out = new FileOutputStream(fileName, false);
                tickets.get(i).compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();
            }
        }
        catch(Exception ex)
        {
        }
    }
}
