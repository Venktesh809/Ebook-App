package com.example.myebook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;

public class UploadPdfActivity extends AppCompatActivity {


    private CardView addPdf;
    private final int REQ = 1;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    String downloadUrl = "";
    private ProgressDialog pd;
    private Uri pdfData;

    private EditText pdfTitle;
    private Button uploadPdfbtn;
    private TextView pdfTextView;
    private String pdfName, title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_pdf);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        pd = new ProgressDialog(this);

        addPdf = findViewById(R.id.addPdf);
        pdfTitle = findViewById(R.id.pdfTitle);
        uploadPdfbtn = findViewById(R.id.uploadPdfbtn);
        pdfTextView = findViewById(R.id.pdfTextView);

        addPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });


        uploadPdfbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = pdfTitle.getText().toString();
                if(title.isEmpty()){
                    pdfTitle.setError("Empty");
                    pdfTitle.requestFocus();
                }else if(pdfData == null){
                    Toast.makeText(UploadPdfActivity.this,"Please Upload pdf",Toast.LENGTH_SHORT).show();
                }else{
                    uploadpdf();
                }
            }
        });
    }

    private void uploadpdf() {
        pd.setTitle("Please wait...");
        pd.setMessage("Uploading pdf");
        pd.show();

        StorageReference reference = storageReference.child("pdf/"+ pdfName+"-"+System.currentTimeMillis()+".pdf");
        reference.putFile(pdfData)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isComplete());
                        Uri uri = uriTask.getResult();
                        uploadData(String.valueOf(uri));
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                pd.dismiss();
                Toast.makeText(UploadPdfActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadData(String downloadUrl) {
        String uniqueKey = databaseReference.child("pdf").push().getKey();
        HashMap data = new HashMap();
        data.put("pdfname",title);
        data.put("pdfUrl",downloadUrl);

        databaseReference.child("pdf").child(uniqueKey).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                pd.dismiss();
                Toast.makeText(UploadPdfActivity.this,"Pdf Uploaded Successfully",Toast.LENGTH_SHORT).show();
                pdfTitle.setText("");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                pd.dismiss();
                Toast.makeText(UploadPdfActivity.this,"Failed to Upload pdf",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // we can use dierect ("pdf/docs/ppt/*(multiple) ) but some time does not support file so use below line code
        intent.setType("application/pdf/*");
        startActivityForResult(Intent.createChooser(intent,"Select Pdf File"), REQ);

    }

    @SuppressLint("Range")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ && resultCode == RESULT_OK){
            pdfData = data.getData();

            if(pdfData.toString().startsWith("content://")){
                Cursor cursor = null ;
                try {
                    cursor = UploadPdfActivity.this.getContentResolver().query(pdfData,null,null,null,null);
                    if(cursor != null && cursor.moveToFirst()) {
                        pdfName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }else if(pdfData.toString().startsWith("file://")){
                pdfName = new File(pdfData.toString()).getName();
            }
            pdfTextView.setText(pdfName);
        }
    }

}
