package com.example.test;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

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

/**
 * TensorFlow Lite을 사용한 와인병 인식:
 * 학습된 객체 감지 모델 (예: SSD MobileNet V2)을 다운로드하여 assets 폴더에 추가
 * 모델 파일 이름은 detect.tflite로 가정합니다.
 *
 * 의존성 추가: 위에서 설명한 대로 app/build.gradle(모듈) 파일에 TensorFlow Lite 의존성 추가
 * implementation("org.tensorflow:tensorflow-lite:2.4.0")
 * implementation("org.tensorflow:tensorflow-lite-support:0.1.0")
 *
 * 객체 감지: TensorFlow Lite을 사용하여 이미지에서 와인병을 감지하고 이를 잘라냅니다.
 * */
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

        // Define the input shape
        int imageSizeX = 300;  // Specify the input image width for your model
        int imageSizeY = 300;  // Specify the input image height for your model

        // Define the output shape
        int outputShape = 10;  // The number of objects your model can detect

        // Run the inference
        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(new int[]{1, outputShape, 4}, DataType.FLOAT32);
        tflite.run(tensorImage.getBuffer(), outputBuffer.getBuffer());

        // Process the output (Assuming the output is bounding box coordinates)
        float[] detectionResults = outputBuffer.getFloatArray();
        List<RectF> detectedObjects = new ArrayList<>();

        for (int i = 0; i < detectionResults.length; i += 4) {
            float left = detectionResults[i] * bitmap.getWidth();
            float top = detectionResults[i + 1] * bitmap.getHeight();
            float right = detectionResults[i + 2] * bitmap.getWidth();
            float bottom = detectionResults[i + 3] * bitmap.getHeight();
            detectedObjects.add(new RectF(left, top, right, bottom));
        }

        // Crop the detected object from the original bitmap
        if (!detectedObjects.isEmpty()) {
            RectF detectedObject = detectedObjects.get(0);  // Assuming the first detected object is the wine bottle
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

        // Bitmap을 Base64로 인코딩
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String base64EncodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

        // Vision API 요청 생성
        VisionAPIRequest request = new VisionAPIRequest();
        VisionAPIRequest.Request.Image image = new VisionAPIRequest.Request.Image();
        image.content = base64EncodedImage;

        VisionAPIRequest.Request.Feature feature = new VisionAPIRequest.Request.Feature();
        feature.type = "LABEL_DETECTION";  // 필요한 기능 타입으로 변경 가능
        feature.maxResults = 10;

        VisionAPIRequest.Request apiRequest = new VisionAPIRequest.Request();
        apiRequest.image = image;
        apiRequest.features = new ArrayList<>();
        apiRequest.features.add(feature);

        request.requests = new ArrayList<>();
        request.requests.add(apiRequest);

        // Vision API 호출
        Call<VisionAPIResponse> call = visionAPI.detectText(request);
        call.enqueue(new Callback<VisionAPIResponse>() {
            @Override
            public void onResponse(Call<VisionAPIResponse> call, Response<VisionAPIResponse> response) {
                if (response.isSuccessful()) {
                    VisionAPIResponse visionResponse = response.body();
                    if (visionResponse != null && !visionResponse.responses.isEmpty()) {
                        // API 응답 처리
                        VisionAPIResponse.Response resp = visionResponse.responses.get(0);
                        if (resp.textAnnotations != null && !resp.textAnnotations.isEmpty()) {
                            String detectedText = resp.textAnnotations.get(0).description;
                            // 와인 정보를 표시
                            mWineInfoScrollView.setVisibility(View.VISIBLE);
                            mWineName.setText(detectedText);  // 실제 응답 데이터에 따라 적절히 변경
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
            }
        }
    }
}