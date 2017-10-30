package app.dropboxapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class PickImageActivity extends AppCompatActivity {

    public static final int PERMISSION_REQUEST_CODE = 111;
    protected Button mUploadButton;
    protected ImageView mSelectedIv = null;
    protected TextView mImagePathTv = null;
    protected String mImagePath = "no image selected";
    private boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_image);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Button pickImageButton = (Button) findViewById(R.id.btn_pick_image);
        mSelectedIv = (ImageView) findViewById(R.id.iv);
        mImagePathTv = (TextView) findViewById(R.id.tv_image_path);
        mUploadButton = (Button) findViewById(R.id.btn_upload);
        mUploadButton.setVisibility(View.GONE);

        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startUpload();
            }
        });
        pickImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                } else {
                    selectImage();
                }
            }
        });
    }

    private void startUpload() {
        Intent it = new Intent(PickImageActivity.this, MainActivity.class);
        startActivity(it);
    }

    private void selectImage() {
        if (isKitKat) {
            fileSelectorForKitkat();
        } else {
            fileSelectorForLowerVersions();
        }
    }

    private void fileSelectorForLowerVersions() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "SELECT"), 1);
    }

    private void fileSelectorForKitkat() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "SELECT"), 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectImage();
                } else {
                    Toast.makeText(this, "Please accept permission to select file.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @TargetApi(19)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && data.getData() != null && resultCode == RESULT_OK) {
            boolean isImageFromGoogleDrive = false;
            Uri uri = data.getData();
            SharedPreferences sp_uri = PickImageActivity.this.getSharedPreferences("uri_getSP", MODE_WORLD_READABLE);
            SharedPreferences.Editor spedituri = sp_uri.edit();
            spedituri.putString("uri_get", uri.toString());
            spedituri.commit();
            Log.e(" STARTING Uri", " " + uri);
            if (isKitKat && DocumentsContract.isDocumentUri(PickImageActivity.this, uri)) {

                if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                    String docId = DocumentsContract.getDocumentId(uri);
                    String[] split = docId.split(":");
                    String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        mImagePath = Environment.getExternalStorageDirectory() + "/" + split[1];
                        Log.e(" Environment=", " " + mImagePath);
                    } else {
                        Pattern DIR_SEPORATOR = Pattern.compile("/");
                        Set<String> rv = new HashSet<>();
                        String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
                        String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
                        String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
                        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
                            if (TextUtils.isEmpty(rawExternalStorage)) {
                                rv.add("/storage/sdcard0");
                            } else {
                                rv.add(rawExternalStorage);
                            }
                        } else {
                            String rawUserId;
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                rawUserId = "";
                            } else {
                                String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                                String[] folders = DIR_SEPORATOR.split(path);
                                String lastFolder = folders[folders.length - 1];
                                boolean isDigit = false;
                                try {
                                    Integer.valueOf(lastFolder);
                                    isDigit = true;
                                } catch (NumberFormatException ignored) {
                                }
                                rawUserId = isDigit ? lastFolder : "";
                            }
                            if (TextUtils.isEmpty(rawUserId)) {
                                rv.add(rawEmulatedStorageTarget);
                            } else {
                                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
                            }
                        }
                        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
                            String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
                            Collections.addAll(rv, rawSecondaryStorages);
                        }
                        String[] temp = rv.toArray(new String[rv.size()]);

                        for (int i = 0; i < temp.length; i++) {
                            File tempf = new File(temp[i] + "/" + split[1]);
                            if (tempf.exists()) {
                                mImagePath = temp[i] + "/" + split[1];


                                Log.e(" temp[i]=", " " + mImagePath);

                            }
                        }
                    }
                } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                    String id = DocumentsContract.getDocumentId(uri);
                    Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    Cursor cursor = null;
                    String column = "_data";
                    String[] projection = {
                            column
                    };

                    try {
                        cursor = getContentResolver().query(contentUri, projection, null, null,
                                null);
                        if (cursor != null && cursor.moveToFirst()) {
                            final int column_index = cursor.getColumnIndexOrThrow(column);
                            mImagePath = cursor.getString(column_index);

                            Log.e(" cursor.get1", " " + mImagePath);


                        }
                    } finally {
                        if (cursor != null)
                            cursor.close();
                    }
                } else if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                    String docId = DocumentsContract.getDocumentId(uri);
                    String[] split = docId.split(":");
                    String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    String selection = "_id=?";
                    String[] selectionArgs = new String[]{
                            split[1]
                    };

                    Cursor cursor = null;
                    String column = "_data";
                    String[] projection = {
                            column
                    };

                    try {
                        cursor = getContentResolver().query(contentUri, projection, selection, selectionArgs,
                                null);
                        if (cursor != null && cursor.moveToFirst()) {
                            int column_index = cursor.getColumnIndexOrThrow(column);
                            mImagePath = cursor.getString(column_index);


                            Log.e(" cursor.get2", " " + mImagePath);
                        }
                    } finally {
                        if (cursor != null)
                            cursor.close();
                    }
                } else if ("com.google.android.apps.docs.storage".equals(uri.getAuthority())) {
                    isImageFromGoogleDrive = true;
                }
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                Cursor cursor = null;
                String column = "_data";
                String[] projection = {
                        column
                };

                try {
                    cursor = getContentResolver().query(uri, projection, null, null,
                            null);
                    if (cursor != null && cursor.moveToFirst()) {
                        int column_index = cursor.getColumnIndexOrThrow(column);
                        mImagePath = cursor.getString(column_index);


                        Log.e(" cursor.get3", " " + mImagePath);
                    }
                } finally {
                    if (cursor != null)
                        cursor.close();
                }
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {


                String uri_get = uri.getPath();


                mImagePath = uri.getPath();

                Log.e(" uri.getpath", " " + mImagePath);


            }


            if (isImageFromGoogleDrive) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                    mSelectedIv.setImageBitmap(bitmap);
                    mImagePathTv.setText("iMAGEPATH");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                File f = new File(mImagePath);


                Log.e(" file f===", " " + f);

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), bmOptions);
                mSelectedIv.setImageBitmap(bitmap);
                mImagePathTv.setText("filepath" + ": " + mImagePath);

                String result1 = mImagePath.substring(mImagePath.lastIndexOf("/") + 1);


                Log.e("result1==", " " + result1);

                Toast.makeText(PickImageActivity.this, "abs== " + result1, Toast.LENGTH_LONG).show();

                mUploadButton.setVisibility(View.VISIBLE);


                String strImagePath_new = mImagePath.toString();
                Toast.makeText(PickImageActivity.this, "TO STRING==" + strImagePath_new, Toast.LENGTH_LONG).show();
                SharedPreferences sp_clicked = PickImageActivity.this.getSharedPreferences("IMAGEPATH", MODE_WORLD_READABLE);
                SharedPreferences.Editor spedit = sp_clicked.edit();
                spedit.putString("mImagePath", mImagePath);
                spedit.putString("result1", result1);
                spedit.commit();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
