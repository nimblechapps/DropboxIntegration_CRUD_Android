package app.dropboxapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;

import static android.Manifest.*;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_PATH = "FilesActivity_Path";
    private static final int PICKFILE_REQUEST_CODE = 1;
    Context context;

    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 123;
    private String mPath, uri_get;
    Exception mException;
    Button btn_upload;

    DbxClientV2 client = null;
    DbxClientV2 mDbxClient = null;


    private FileMetadata mSelectedFile;
    boolean isPermissionGranted = false;


    private final static String DROPBOX_FILE_DIR = "/Nimblechapps/";

    // Replace APP_KEY from your APP_KEY
    final static private String APP_KEY = "91u9h78pg1blrga";
    // Relace APP_SECRET from your APP_SECRET  v4d00mo5hftfju2
    final static private String APP_SECRET = "v4d00mo5hftfju2";

    //

    String strImagePath, result1;
    private DropboxAPI<AndroidAuthSession> mDBApi;

    public static Intent getIntent(Context context, String path) {
        Intent filesIntent = new Intent(context, MainActivity.class);
        filesIntent.putExtra(MainActivity.EXTRA_PATH, path);
        return filesIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        // callback method
        initialize_session();


        btn_upload = (Button) findViewById(R.id.uploadfile);

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                uploadFiles();

            }
        });


    }

    /**
     * Initialize the Session of the Key pair to authenticate with dropbox
     */
    protected void initialize_session() {


        // store app key and secret key
        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        //Pass app key pair to the new DropboxAPI object.


        mDBApi = new DropboxAPI<>(session);
        // MyActivity below should be your activity class name
        //start session
        mDBApi.getSession().startOAuth2Authentication(MainActivity.this);


    }

    /**
     * Callback register method to execute the upload method
     */
    public void uploadFiles() {


        SharedPreferences sp = this.getSharedPreferences("IMAGEPATH", MODE_WORLD_READABLE);
        strImagePath = sp.getString("strImagePath", "");
        result1 = sp.getString("result1", "");


        SharedPreferences spuri = this.getSharedPreferences("uri_getSP", MODE_WORLD_READABLE);
        uri_get = spuri.getString("uri_get", "");

        Toast.makeText(MainActivity.this, "uri_get===" + uri_get, Toast.LENGTH_SHORT).show();
        Log.e("uri_get", " " + uri_get);

        Log.e("strImagePath", " " + strImagePath);
        Toast.makeText(MainActivity.this, "strImagePath===" + strImagePath, Toast.LENGTH_SHORT).show();



//        int currentAPIVersion = Build.VERSION.SDK_INT;
//        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
//            checkPermission();
//        }
//
//         else{

            uploadFile(uri_get);

       // }


        // uploadFile(uri_get);

//        if ( isPermissionGranted = true) {
//            uploadFile(uri_get);
//        }
    }


    private void uploadFile(String fileUri) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Uploading");
        dialog.show();

        new UploadFileTask(this, DropboxClientFactory.getClient(), new UploadFileTask.Callback() {
            @Override
            public void onUploadComplete(FileMetadata result) {
                dialog.dismiss();

                String message = result.getName() + " size " + result.getSize() + " modified " +
                        DateFormat.getDateTimeInstance().format(result.getClientModified());
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT)
                        .show();


                Toast.makeText(MainActivity.this, "Successfully Uploaded.", Toast.LENGTH_SHORT)
                        .show();

                Intent it = new Intent(MainActivity.this, PickImageActivity.class);
                startActivity(it);

            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e("ERROR ", "Failed to upload file.", e);
                Toast.makeText(MainActivity.this,
                        "An error has occurred",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(fileUri, strImagePath);
    } 

    /**
     * Asynchronous method to upload any file to dropbox
     */
    public class Upload extends AsyncTask<String, Void, FileMetadata> {

        protected void onPreExecute() {

        }

        protected FileMetadata doInBackground(String... arg0) {


            try {


                // Define path of file to be upload
                File file = new File(strImagePath);
                // FileInputStream inputStream = new FileInputStream(file);

                InputStream inputStream2 = new FileInputStream(strImagePath);

                //FileMetadata metadata=
                Log.e("strImagePath", "  " + strImagePath);
                Log.e("result1", "  " + result1);
                //   File localFile = UriHelpers.getFileForUri(getApplicationContext(), Uri.parse(strImagePath));


                try {


                    InputStream inputStream1 = new FileInputStream(strImagePath);
                    mDbxClient.files().uploadBuilder("/" + result1)
                            .withMode(WriteMode.OVERWRITE)
                            .uploadAndFinish(inputStream1);
                } catch (DbxException | IOException e) {
                    mException = e;

                }


            } catch (FileNotFoundException e) {
                e.printStackTrace();

            }


            return null;
        }

        @Override
        protected void onPostExecute(FileMetadata result) {

            if (mException != null) {

                Log.e("not null", "" + mException);

            } else if (result == null) {
                Log.e("  null", "" + mException);

            } else {

                Log.e("", "" + result);

            }

        }
    }


    protected void onResume() {
        super.onResume();


        if (mDBApi.getSession().authenticationSuccessful()) {


            try {
                // Required to complete auth, sets the access token on the session
                mDBApi.getSession().finishAuthentication();

                String accessToken = mDBApi.getSession().getOAuth2AccessToken();

                SharedPreferences settings = getSharedPreferences("dropbox-sample", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("access-token", accessToken);
                editor.commit();

                if (accessToken == null) {
                    accessToken = Auth.getOAuth2Token();
                    if (accessToken != null) {


                        SharedPreferences prefs = getSharedPreferences("dropbox-sample", MODE_PRIVATE);
                        String accessToken1 = prefs.getString("access-token", null);
                        prefs.edit().putString("access-token", accessToken1).apply();

                        initAndLoadData(accessToken);
                    }
                } else {
                    initAndLoadData(accessToken);
                }

            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
//            SharedPreferences prefs = getSharedPreferences("dropbox-sample", MODE_PRIVATE);
//            String accessToken = prefs.getString("access-token", null);

        }

//        String uid = Auth.getUid();
//        String storedUid = prefs.getString("user-id", null);
//        if (uid != null && !uid.equals(storedUid)) {
//            prefs.edit().putString("user-id", uid).apply();
//        }


//        if (mDBApi.getSession().authenticationSuccessful()) {
//            try {
//                // Required to complete auth, sets the access token on the session
//                mDBApi.getSession().finishAuthentication();
//
//                String accessToken = mDBApi.getSession().getOAuth2AccessToken();
//
//                SharedPreferences settings = getSharedPreferences("my_settings", 0);
//                SharedPreferences.Editor editor = settings.edit();
//                editor.putString("dropboxtoken", accessToken);
//                editor.commit();
//
//            } catch (IllegalStateException e) {
//                Log.i("DbAuthLog", "Error authenticating", e);
//            }
//        }
    }

    private void initAndLoadData(String accessToken) {
        DropboxClientFactory.init(accessToken);
        PicassoClient.init(getApplicationContext(), DropboxClientFactory.getClient());

    }


    protected boolean hasToken() {
        SharedPreferences prefs = getSharedPreferences("dropbox-sample", MODE_PRIVATE);
        String accessToken = prefs.getString("access-token", null);
        return accessToken != null;
    }


    void checkPermission() {

        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission.READ_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("Permission is necessary  !!!");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();

                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            }
                // return false;
             else{

                    uploadFile(uri_get);
                    Log.e("Permission", "GRANTED: true");

                }
            }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:


                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission granted", Toast.LENGTH_SHORT).show();

                    isPermissionGranted = true;
                    uploadFile(uri_get);
                } else {
                    Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                    isPermissionGranted = false;
                }


//
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    uploadFile(uri_get);
//                } else {
//                    //code for deny
//                }
//                break;
        }
    }
}

