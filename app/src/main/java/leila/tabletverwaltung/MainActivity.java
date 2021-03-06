package leila.tabletverwaltung;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;

import leila.tabletverwaltung.Adapter.KursAdapter;
import leila.tabletverwaltung.Adapter.LehrerAdapter;
import leila.tabletverwaltung.DataTypes.Kurs;
import leila.tabletverwaltung.DataTypes.Lehrer;

public class MainActivity extends AppCompatActivity {
    private Spinner sLehrer;
    private Spinner sKurs;
    private Switch swKlassenweise;
    private RelativeLayout rlGeraete;
    private RelativeLayout rlEinlesen;
    private static ArrayList<Lehrer> lehrer = new ArrayList<>();
    private static ArrayList<Kurs> kurse = new ArrayList<>();

    private static int selectedLehrerPos = 0;
    private static int selectedKursPos = 0;
    private static boolean klassenweiseAusgeben = false;

    private boolean isCheckingConnection = false;
    public static final int PERMISSION_REQUEST_CODE_CAMERA = 1;

    public static final int REQUESTCODE_SETIINGS = 1;

    private RelativeLayout flLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        flLoading = (RelativeLayout)findViewById(R.id.progress_overlay);
        sLehrer = (Spinner) findViewById(R.id.sLehrer);
        sKurs = (Spinner) findViewById(R.id.spKlasse);
        rlGeraete = (RelativeLayout) findViewById(R.id.rlGeraete);
        rlEinlesen = (RelativeLayout) findViewById(R.id.rlEinlesen);
        swKlassenweise = (Switch) findViewById(R.id.swKlassenweise);

    }


    @Override
    protected void onResume(){
        super.onResume();

        flLoading.setVisibility(View.VISIBLE);

        initKlassenweiseAusgeben();


        boolean connectionIsValid = getIntent().getBooleanExtra("connectionIsValid", false);
        if(connectionIsValid) {
            createMainActivity();
        }else{
            isCheckingConnection = true;

            Utility.checkDbConnection(getBaseContext(), this, new Runnable() {
                @Override
                public void run() {
                    isCheckingConnection = false;

                    createMainActivity();
                }
            }, new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_settings_verbindung_fehlgeschlagen), Toast.LENGTH_LONG).show();
                    Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivityForResult(i, REQUESTCODE_SETIINGS);
                }
            });
        }
    }


    /**
     * setzt den Switch aktiv oder inaktiv
     */
    private void initKlassenweiseAusgeben() {
        swKlassenweise.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                klassenweiseAusgeben = isChecked;
            }
        });

        swKlassenweise.setChecked(klassenweiseAusgeben);
    }


    /**
     * setzt die OnClickListener für die beiden Buttons
     */
    private void createMainActivity(){
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        final Activity currentActivity = this;
        rlEinlesen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final int kursId = kurse.get(sKurs.getSelectedItemPosition()).getKursId();

                final Intent i = new Intent(MainActivity.this, ReaderActivity.class);
                i.putExtra("klassenweiseAusgeben", klassenweiseAusgeben);
                i.putExtra("lehrer", lehrer.get(sLehrer.getSelectedItemPosition()).getId());
                i.putExtra("kurs", kursId);


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final int schuelerAnzahl = Kurs.getSchuelerAnzahl(getBaseContext(), kursId);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(!klassenweiseAusgeben && schuelerAnzahl == 0){
                                    Toast.makeText(getApplicationContext(),R.string.alertReaderkeineSchuelerInKlasse, Toast.LENGTH_LONG).show();
                                }else{
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        int perm = currentActivity.checkSelfPermission(Manifest.permission.CAMERA);
                                        if (perm != PackageManager.PERMISSION_GRANTED) {
                                            // Permission not granted (need to ask for it).
                                            currentActivity.requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE_CAMERA);
                                        }else {
                                            // Permission granted (user already accepted).
                                            startActivity(i);
                                        }
                                    } else {
                                        // Permission granted (because no runtime permission).
                                        startActivity(i);
                                    }
                                }
                            }
                        });
                    }
                }).start();
            }
        });

        rlGeraete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, GeraeteActivity.class);
                startActivity(i);
            }
        });

        initSpinner();
    }


    @Override
    /**
     * erstellt das OptionsMenu
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    /**
     * OnClickListener für ein Item aus dem OptionsMenu
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (isCheckingConnection) return false;

        if (id == R.id.einstellungen) {
            Intent i = new Intent(this.getApplicationContext(), SettingsActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Holt sich alle Lehrer und Kurse und initialisiert die Spinner
     */
    private void initSpinner(){
        //  Färbt das Dreieck des Spinners weis
        sLehrer.getBackground().setColorFilter(getResources().getColor(R.color.white200), PorterDuff.Mode.SRC_ATOP);
        sKurs.getBackground().setColorFilter(getResources().getColor(R.color.white200), PorterDuff.Mode.SRC_ATOP);

        new Thread(new Runnable() {
            @Override
            public void run() {
                MainActivity.lehrer = Lehrer.getAll(getBaseContext());
                MainActivity.kurse = Kurs.getAll(getBaseContext());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LehrerAdapter lAdapter = new LehrerAdapter(getApplicationContext(), MainActivity.lehrer);
                        sLehrer.setAdapter(lAdapter);
                        sLehrer.setSelection(selectedLehrerPos);
                        sLehrer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedLehrerPos = position;
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        KursAdapter kAdapter = new KursAdapter(getApplicationContext(), MainActivity.kurse);
                        sKurs.setAdapter(kAdapter);
                        sKurs.setSelection(selectedKursPos);
                        sKurs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedKursPos = position;
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        flLoading.setVisibility(View.GONE);
                    }
                });

            }
        }).start();
    }





    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case PERMISSION_REQUEST_CODE_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    rlEinlesen.performClick();
                }
                break;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case REQUESTCODE_SETIINGS:
                if(resultCode == Activity.RESULT_OK && data != null){
                    onResume();
                }
                break;
        }
    }
}




