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
            Bitmap ticketWithBorder = null;
            Bitmap commerceTicket = PaymentPOS.generateCommerceTransactionTicketBMP(transactionResult, null);
            if(commerceTicket != null) {
                ticketWithBorder = addBorder(commerceTicket, Color.BLACK, 2);
            } else {
                Bitmap cardholderTicket = PaymentPOS.generateCardholderTransactionTicketBMP(transactionResult, null);
                if(cardholderTicket != null) {
                    ticketWithBorder = addBorder(cardholderTicket, Color.BLACK, 2);
                }
            }
            if(ticketWithBorder != null) {
                transactionResultImageView.setImageBitmap(ticketWithBorder);
            }
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
            File ticketsFolder = new File(Environment.getExternalStorageDirectory(), "/EcoA10/Tickets");
            if (!ticketsFolder.exists()) ticketsFolder.mkdirs();
            String fileDateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

            PdfDocument commerceTicket = PaymentPOS.generateCommerceTransactionTicketPDF(transactionResult, null);
            if(commerceTicket != null) {
                String commerceTicketFilename = Environment.getExternalStorageDirectory() + "/EcoA10/Tickets/" + fileDateTime + ".pdf";
                commerceTicket.writeTo(new FileOutputStream(commerceTicketFilename, false));
            }

            PdfDocument cardholderTicket = PaymentPOS.generateCardholderTransactionTicketPDF(transactionResult, null);
            if(cardholderTicket != null) {
                String cardholderTicketFilename = Environment.getExternalStorageDirectory() + "/EcoA10/Tickets/" + fileDateTime + "_CC.pdf";
                cardholderTicket.writeTo(new FileOutputStream(cardholderTicketFilename, false));
            }
        }
        catch(Exception ex)
        {
        }
    }

    private void saveTicketsImage() {
        try {
            File ticketsFolder = new File(Environment.getExternalStorageDirectory(), "/EcoA10/Tickets");
            if (!ticketsFolder.exists()) ticketsFolder.mkdirs();
            String fileDateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

            Bitmap commerceTicket = PaymentPOS.generateCommerceTransactionTicketBMP(transactionResult, null);
            if(commerceTicket != null) {
                String commerceTicketFilename = Environment.getExternalStorageDirectory() + "/EcoA10/Tickets/" + fileDateTime + ".png";
                saveTicketImage(commerceTicket, commerceTicketFilename);
            }

            Bitmap cardholderTicket = PaymentPOS.generateCardholderTransactionTicketBMP(transactionResult, null);
            if(cardholderTicket != null) {
                String cardholderTicketFilename = Environment.getExternalStorageDirectory() + "/EcoA10/Tickets/" + fileDateTime + "_CC.png";
                saveTicketImage(cardholderTicket, cardholderTicketFilename);
            }
        }
        catch(Exception ex)
        {
        }
    }

    private void saveTicketImage(Bitmap ticketBitmap, String filename) {
        try {
            OutputStream out = new FileOutputStream(filename, false);
            ticketBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
