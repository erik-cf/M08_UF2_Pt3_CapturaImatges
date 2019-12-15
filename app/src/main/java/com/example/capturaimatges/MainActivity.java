package com.example.capturaimatges;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.DialogInterface;
import android.content.Intent;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    // Nom de l'arxiu de la foto que es guardarà a memòria:
    public static final String PHOTOFILENAME = "capturaImatgesPhoto";
    // ID del request que es fa al Camera Intent:
    static final int REQUEST_IMAGE_CAPTURE = 1;
    // Nom del paquet de la nostra aplicació:
    public static final String PACKAGENAME= "com.example.capturaimatges";
    // Nom del fileprovider:
    public static final String FILEPROVIDER = "fileprovider";

    // Elements que té i util·litza la classe
    ImageView imgApp;
    Button button;
    File storageDir;
    File photo;
    String currentPhotoPath;
    Uri photoURI;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Trobem els components pel seu ID:
        button = findViewById(R.id.button);
        imgApp = findViewById(R.id.imageApp);

        // Recollim el lloc on es desen les imatges a 'storageDir'
        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Inicialitzem la photo, per tal de si l'hem de carregar o no:
        photo = getPhoto();

        // Si existeix la photo, la carreguem a la ImageView
        if (photo != null && photo.exists()) {
            // Inicialitzem el photoURI amb la URI de la foto carregada:
            photoURI = FileProvider.getUriForFile(this,
                    PACKAGENAME + "." + FILEPROVIDER,
                    photo);
            // Insertem la imatge a l'ImageView:
            imgApp.setImageURI(photoURI);
        }

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Si fem click al botó, comencem comprobacions de sobreescriptura:
                sobreescriurePhoto();
            }
        });

    }

    /**
     * Mètode que truca a l'Intent de la càmera.
     */
    private void takePhoto() {
        // Creem l'Intent de la càmera:
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Si hi ha una aplicació per fer de càmera, seguim, si no, mostrem un Toast:
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Si la photo ja existia, la borrem:
            if (photo != null)
                photo.delete();

            try {
                // Creem la imatge de 0:
                photo = creaImatge();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // Si s'ha pogut crear la imatge:
            if (photo != null) {
                // Agafem la Uri de la imatge per passar-li a l'Intent de la camera
                // on ha de guardar la imatge:
                photoURI = FileProvider.getUriForFile(this,
                        PACKAGENAME + "." + FILEPROVIDER,
                        photo);
                // Li passem a la càmera on ha de guardar la imatge:
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Comencem l'Activity de càmera:
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }else{
            Toast.makeText(this, "Necessites càmera o aplicació de càmera perquè funcioni!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Mètode que crea l'arxiu d'Imatge.
     * @return l'arxiu d'imatge.
     * @throws IOException En cas que no es pugui crear l'arxiu.
     */
    private File creaImatge() throws IOException {
        // Creem un arxiu on guardarem la imatge:
        File image = File.createTempFile(PHOTOFILENAME, ".jpg", storageDir);
        // Assignem el path de la foto (ruta)
        currentPhotoPath = image.getAbsolutePath();
        // Retornem la imatge creada:
        return image;
    }

    /**
     * Mètode que executa certes accions en tornar de l'Intent de la càmera.
     * Aquest mètode no s'ha de trucar des de cap lloc, la seva execució és automàtica.
     * @param requestCode El codi de request que se li ha passat al intent de la càmera.
     * @param resultCode El codi de ressolució que ha retornat l'Intent de la càmera.
     * @param i L'intent de la càmera.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent i) {
        // Si tot va bé al intent de càmera:
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Insertem la imatge al ImageView:
            imgApp.setImageURI(photoURI);
            // Mostrem on s'ha desat la foto:
            Toast.makeText(this, "Foto desada a: " + currentPhotoPath, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Mètode que comprova si existeix la foto. Si existeix, pregunta si es vol sobreescriure.
     */
    public void sobreescriurePhoto() {
        // Si la foto existeix:
        if (photo != null && photo.exists()) {
            // Mostrem un Dialog preguntant si es vol sobreescriure la foto:
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Sobreescriure foto")
                    .setMessage("Ja existeix una foto a la memoria, vols sobreescriure-la?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Si es diu que si, truquem al metode takePhoto
                            // que s'encarrega de fer tota la gestió de la foto
                            // i la càmera:
                            takePhoto();
                        }
                    })

                    // Si es diu que no, tanquem el diàleg i no fem res:
                    .setNegativeButton(android.R.string.no, null)

                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            // Si la foto no existeix: truquem directament al mètode takePhoto:
            takePhoto();
        }
    }

    /**
     * Mètode que retorna la foto que hi hagi al directori de fotos de la app de l'emmagatzematge extern.
     * @return la foto si existeix, null en cas que no existeixi cap foto.
     */
    public File getPhoto() {
        File[] files = storageDir.listFiles();
        if (files.length != 0) {
            return files[0];
        } else {

            return null;
        }
    }
}
