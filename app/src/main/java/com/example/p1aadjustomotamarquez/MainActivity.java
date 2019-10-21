package com.example.p1aadjustomotamarquez;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final int NADA = -1; // Cuando no se pulsa ningún Radio Button
    private static final int INTERNA = 0; // Radio button memoria interna
    private static final int PRIVADA = 1;// Radio button memoria privada
    private static final int ID_PERMISO_LEER_CONTACTOS = 4;
    private List<Contacto> contactos;
    private static final String KEY_ARCHIVO = "archivo";


    private RadioGroup rgMemorias;
    private RadioButton rbInter, rbPriv;
    private Button btLeer, btExportar;
    private EditText etArchivo;
    private TextView tvRsultado;

    private String valorEtArchivo, nombreArchivo;
    private int tipo;

    /*MENU----------------------------------------------------------*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnSettings:
                mostrarSettings();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void mostrarSettings() {

        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);

    }

    /*------------------------------------------------------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inicializacionComponentes(); // Función que inicializa los componentes
        asignarEventos(); // Función que asigna los eventos a los botones
        readHistorial();

        if (readHistorial()){
            Log.v("si", "si");
        }else{
            Log.v("no", "no");
        }

    }

    private void inicializacionComponentes() {
        rgMemorias = findViewById(R.id.rgMemorias);
        btLeer = findViewById(R.id.btLeer);
        btExportar = findViewById(R.id.btExportar);
        etArchivo = findViewById(R.id.etArchivo);
        tvRsultado = findViewById(R.id.textView);
        rbInter = findViewById(R.id.rbMiInterna);
        rbPriv = findViewById(R.id.rbMprivada);

    }


    private void asignarEventos() {
        btLeer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leerFichero();
            }
        });

        btExportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                escribirFichero();
            }
        });
    }

    private boolean readHistorial() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean("historial", true);
    }

    private boolean readArchivo() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean("ultimoArchivo", true);
    }

    private void leerFichero() {

        if (isValues() || !valorEtArchivo.isEmpty()){
            leerContenido();
        }
    }

    private void leerContenido() {
        File f = new File(getFile(tipo), nombreArchivo);
        Log.v("Ruta", f.getAbsolutePath());

        if(f.exists()){
            Log.v("existe","existe");
        }else{
            Log.v("noexiste","no existe");
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String linea;
            StringBuffer lineas = new StringBuffer("");
            while ((linea = br.readLine()) != null) {
                lineas.append(linea + "\n");
            }
            br.close();
            tvRsultado.setText(lineas);
            savePreferrences();
            Toast toast3 =
                    Toast.makeText(getApplicationContext(),
                            "Leyendo archivo...", Toast.LENGTH_SHORT);

            toast3.show();
        } catch(IOException e) {
            tvRsultado.setText(e.getMessage());
            Toast.makeText(getApplicationContext(),
                    "El archivo no existe", Toast.LENGTH_SHORT).show();
            tvRsultado.setText("");

        }
    }


    private void escribirFichero() {
        valorEtArchivo = etArchivo.getText().toString().trim();

        if (isValues() || !valorEtArchivo.isEmpty()){
            escribirContenido();
        }

    }


    private boolean isValues(){
        nombreArchivo = etArchivo.getText().toString().trim();

        if(rbInter.isChecked()){
            tipo = INTERNA;
        }else{
            tipo = PRIVADA;
        }

        return !(nombreArchivo.isEmpty() || tipo == NADA);

    }

    private static int cogerRadioButton(int item) { //Le pasamos el radioButton pulsado
        int tipo = NADA;
        switch (item) {
            case R.id.rbMiInterna:
                tipo = INTERNA;
                break;
            case R.id.rbMprivada:
                tipo = PRIVADA;
                break;
        }
        return tipo;
    }


    private void escribirContenido() {
        File f = new File(getFile(tipo), nombreArchivo);
        Log.v("ABS",f.getAbsolutePath());
        contactos = getListaContactos();
        if (f.exists()){
            Toast.makeText(getApplicationContext(),
                    "El archivo que usted ha introducido ya existe", Toast.LENGTH_SHORT).show();
            etArchivo.setText(nombreArchivo);
        }else {

            try {
                FileWriter fw = new FileWriter(f);
                fw.write(contactos.toString());
                fw.flush();
                fw.close();
                savePreferrences();
                Toast.makeText(getApplicationContext(),
                        "Archivo creado", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                tvRsultado.setText(e.getMessage());
                Toast toast2 =
                        Toast.makeText(getApplicationContext(),
                                "Error al crear el archivo", Toast.LENGTH_SHORT);

                toast2.show();
            }
        }

        etArchivo.setText("");
        tvRsultado.setText("");
    }


    public void permisos(){

        // AQUI SE COMPRUEBA SI LA APP TIENE PERMISOS PARA LO QUE SOLICITAMOS
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // SI NO TUVIERA PERMISO LA APP VOLVERA A PEDIRLA
            // DEBERIA VOLVER A PREGUNTAR POR EL PERMISO
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                Toast.makeText(this, R.string.razon, Toast.LENGTH_LONG).show();
            }
            // 2º VEZ QUE LE PIDO PERMISO AL USUARIO
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    ID_PERMISO_LEER_CONTACTOS);
        } else {
            // Tengo permiso por lo que realizo la acción
            cogerListaContactos();
        }
    }

    private void cogerListaContactos() {
        contactos = getListaContactos();
        tvRsultado.setText(contactos.toString());
    }

    private void savePreferrences() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(KEY_ARCHIVO, nombreArchivo);
        editor.commit();
    }


    private File getFile(int tipo) {
        File file = null;
        switch(tipo) {
            case INTERNA:
                file = this.getFilesDir();
                break;
            case PRIVADA:
                file = this.getExternalFilesDir(null);
                break;
        }
        return file;
    }

    public List<Contacto> getListaContactos(){

        String phoneNo = "";

        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String proyeccion[] = null;
        String seleccion = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = ? and " +
                ContactsContract.Contacts.HAS_PHONE_NUMBER + "= ?";
        String argumentos[] = new String[]{"1","1"};
        String orden = ContactsContract.Contacts.DISPLAY_NAME + " collate localized asc";
        Cursor cursor = getContentResolver().query(uri, proyeccion, seleccion, argumentos, orden);
        int indiceId = cursor.getColumnIndex(ContactsContract.Contacts._ID);
        int indiceNombre = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        // int indiceTelefono = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
        List<Contacto> lista = new ArrayList<>();
        Contacto contacto;


        ContentResolver cr = getContentResolver();
        while(cursor.moveToNext()){
            contacto = new Contacto();
            contacto.setId(cursor.getLong(indiceId));
            contacto.setNombre(cursor.getString(indiceNombre));

            if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                Cursor pCur = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{id}, null);
                while (pCur.moveToNext()) {
                    phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                }
                pCur.close();
            }

            contacto.setNumero(phoneNo);

            lista.add(contacto);
        }

        return lista;

    }

}
