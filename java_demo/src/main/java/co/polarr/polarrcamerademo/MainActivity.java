package co.polarr.polarrcamerademo;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.polarr.renderer.FilterPackageUtil;
import co.polarr.renderer.entities.FilterItem;
import co.polarr.renderer.entities.FilterPackage;


public class MainActivity extends Activity {
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 2;


    private CameraRenderView mRenderer;
    private List<FilterItem> mFilters;
    private String currentFilterName = "无滤镜";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        mRenderer = (CameraRenderView) findViewById(R.id.renderer_view);

        if (!checkAndRequirePermission(REQUEST_CAMERA_PERMISSION)) {
            mRenderer.setVisibility(View.INVISIBLE);
        }

        findViewById(R.id.btn_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAndRequirePermission(REQUEST_STORAGE_PERMISSION)) {
                    takePhoto();
                }
            }
        });

        List<FilterPackage> packages = FilterPackageUtil.GetAllFilters(getResources());
        mFilters = new ArrayList<>();
        for (FilterPackage filterPackage : packages) {
            mFilters.addAll(filterPackage.filters);
        }

        final Button btnFilters = (Button) findViewById(R.id.btn_filters);
        btnFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
                final CharSequence items[] = new CharSequence[mFilters.size()];
                for (int i = 0; i < mFilters.size(); i++) {
                    items[i] = mFilters.get(i).filterName("zh");
                }
                adb.setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int n) {
                        FilterItem filterItem = mFilters.get(n);

                        mRenderer.updateFilter(filterItem.id);

                        currentFilterName = items[n].toString();
                        btnFilters.setText(currentFilterName);
                    }

                });
                adb.setNegativeButton("Cancel", null);
                adb.setTitle("Choose a filter:");
                adb.show();
            }
        });

        findViewById(R.id.btn_random_9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> testFilters = new ArrayList<>();
                if (mFilters.size() < 9) {
                    for (FilterItem filterItem : mFilters) {
                        testFilters.add(filterItem.id);
                    }
                } else {
                    while (testFilters.size() < 9) {
                        FilterItem randomFilter = mFilters.get((int) (Math.random() * mFilters.size()));
                        if (!testFilters.contains(randomFilter.id)) {
                            testFilters.add(randomFilter.id);
                        }
                    }
                }

                mRenderer.setTestFilters(testFilters);
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onPause() {
        super.onPause();
        mRenderer.onDestroy();

    }

    @Override
    public void onResume() {
        super.onResume();
        mRenderer.onResume();
    }

    private void takePhoto() {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        final File folder = new File(storageDir, "polarr_demo");
        if (!folder.exists()) {
            folder.mkdir();
        }

        mRenderer.takePhoto(new CameraRenderView.OnCaptureCallback() {
            @Override
            public void onPhoto(Bitmap bitmap) {
                String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

                final File outputFile = new File(folder.getPath(), fileName + "_" + currentFilterName + ".jpg");
                try {
                    OutputStream fileOS = new FileOutputStream(outputFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOS);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Rescanning the icon_library/gallery so it catches up with our own changes
                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            mediaScanIntent.setData(Uri.fromFile(outputFile));
                            sendBroadcast(mediaScanIntent);

                            Toast.makeText(MainActivity.this, "Saved:" + outputFile.getPath(), Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                bitmap.recycle();
            }
        });
    }

    private boolean checkAndRequirePermission(int permissionRequestId) {
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        switch (permissionRequestId) {
            case REQUEST_CAMERA_PERMISSION:
                permission = Manifest.permission.CAMERA;
                break;
            case REQUEST_STORAGE_PERMISSION:
                permission = Manifest.permission.READ_EXTERNAL_STORAGE;
                break;
        }
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    permissionRequestId);

            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mRenderer.setVisibility(View.VISIBLE);
            }
        } else if ((requestCode == REQUEST_STORAGE_PERMISSION) && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }

}