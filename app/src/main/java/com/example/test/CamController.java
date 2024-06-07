package com.example.test;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CamController extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    private ImageView mImageView;
    private ScrollView mWineInfoScrollView;
    private TextView mWineName, mWineYear, mWineGrape, mWineRegion, mWineDescription;
    private Interpreter tflite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);

        mImageView = findViewById(R.id.camera_preview);
        Button captureButton = findViewById(R.id.capture_button);
        mWineInfoScrollView = findViewById(R.id.wine_info_scrollview);
        mWineName = findViewById(R.id.wine_name);
        mWineYear = findViewById(R.id.wine_year);
        mWineGrape = findViewById(R.id.wine_grape);
        mWineRegion = findViewById(R.id.wine_region);
        mWineDescription = findViewById(R.id.wine_description);

        try {
            tflite = new Interpreter(loadModelFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(CamController.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CamController.this,
                            new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                } else {
                    dispatchTakePictureIntent();
                }
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            Bitmap detectedBitmap = detectObject(imageBitmap);
            mImageView.setImageBitmap(detectedBitmap);

            // Google Vision API 호출
            callVisionAPI(detectedBitmap);
        }
    }

    private Bitmap detectObject(Bitmap bitmap) {
        TensorImage tensorImage = new TensorImage(DataType.UINT8);
        tensorImage.load(bitmap);

        int imageSizeX = 300;
        int imageSizeY = 300;
        int outputShape = 10;

        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(new int[]{1, outputShape, 4}, DataType.FLOAT32);
        tflite.run(tensorImage.getBuffer(), outputBuffer.getBuffer());

        float[] detectionResults = outputBuffer.getFloatArray();
        List<RectF> detectedObjects = new ArrayList<>();

        for (int i = 0; i < detectionResults.length; i += 4) {
            float left = detectionResults[i] * bitmap.getWidth();
            float top = detectionResults[i + 1] * bitmap.getHeight();
            float right = detectionResults[i + 2] * bitmap.getWidth();
            float bottom = detectionResults[i + 3] * bitmap.getHeight();
            detectedObjects.add(new RectF(left, top, right, bottom));
        }

        if (!detectedObjects.isEmpty()) {
            RectF detectedObject = detectedObjects.get(0);
            return Bitmap.createBitmap(bitmap,
                    (int) detectedObject.left,
                    (int) detectedObject.top,
                    (int) (detectedObject.right - detectedObject.left),
                    (int) (detectedObject.bottom - detectedObject.top));
        }
        return bitmap;
    }

    private void callVisionAPI(Bitmap bitmap) {
        VisionController.VisionAPI visionAPI = VisionController.create();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String base64EncodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

        VisionAPIRequest request = new VisionAPIRequest();
        VisionAPIRequest.Request.Image image = new VisionAPIRequest.Request.Image();
        image.content = base64EncodedImage;
        VisionAPIRequest.Request.Feature feature = new VisionAPIRequest.Request.Feature();
        feature.type = "LABEL_DETECTION";
        feature.maxResults = 10;
        VisionAPIRequest.Request apiRequest = new VisionAPIRequest.Request();
        apiRequest.image = image;
        apiRequest.features = new ArrayList<>();
        apiRequest.features.add(feature);

        request.requests = new ArrayList<>();
        request.requests.add(apiRequest);

        Call<VisionAPIResponse> call = visionAPI.detectText(request);
        call.enqueue(new Callback<VisionAPIResponse>() {
            @Override
            public void onResponse(Call<VisionAPIResponse> call, Response<VisionAPIResponse> response) {
                if (response.isSuccessful()) {
                    VisionAPIResponse visionResponse = response.body();
                    if (visionResponse != null && !visionResponse.responses.isEmpty()) {
                        VisionAPIResponse.Response resp = visionResponse.responses.get(0);
                        if (resp.textAnnotations != null && !resp.textAnnotations.isEmpty()) {
                            String detectedText = resp.textAnnotations.get(0).description;
                            mWineInfoScrollView.setVisibility(View.VISIBLE);
                            mWineName.setText(detectedText);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<VisionAPIResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        return FileUtil.loadMappedFile(this, "detect.tflite");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                showSettingsDialog();
            } else {
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("필요한 권한");
        builder.setMessage("이 앱은 카메라 기능을 사용하기 위해 권한이 필요합니다. 설정에서 권한을 활성화해주세요.");
        builder.setPositiveButton("설정으로 이동", (dialog, which) -> {
            dialog.cancel();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        });
        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
