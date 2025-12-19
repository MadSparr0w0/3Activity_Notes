package com.example.notes;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageEditorFragment extends Fragment {

    private ImageView imageView;
    private SeekBar widthSeekBar, heightSeekBar;
    private TextView textWidth, textHeight;
    private Button btnLoadImage, btnSaveImage;
    private Bitmap originalBitmap;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    loadImage(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_editor, container, false);

        imageView = view.findViewById(R.id.image_view);
        widthSeekBar = view.findViewById(R.id.seekbar_width);
        heightSeekBar = view.findViewById(R.id.seekbar_height);
        textWidth = view.findViewById(R.id.text_width);
        textHeight = view.findViewById(R.id.text_height);
        btnLoadImage = view.findViewById(R.id.btn_load_image);
        btnSaveImage = view.findViewById(R.id.btn_save_image);

        setupSeekBars();
        setupButtons();

        return view;
    }

    private void setupSeekBars() {
        widthSeekBar.setMax(400);
        widthSeekBar.setProgress(100);

        heightSeekBar.setMax(400);
        heightSeekBar.setProgress(100);

        updateTextFields();

        widthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTextFields();
                updateImage();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        heightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTextFields();
                updateImage();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateTextFields() {
        textWidth.setText("Ширина: " + (widthSeekBar.getProgress() + 100) + "%");
        textHeight.setText("Высота: " + (heightSeekBar.getProgress() + 100) + "%");
    }

    private void setupButtons() {
        btnLoadImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnSaveImage.setOnClickListener(v -> {
            if (imageView.getDrawable() != null) {
                saveToGallery();
            } else {
                Toast.makeText(getContext(), "Сначала загрузите изображение", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadImage(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            imageView.setImageBitmap(originalBitmap);
            widthSeekBar.setProgress(100);
            heightSeekBar.setProgress(100);
            updateTextFields();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateImage() {
        if (originalBitmap == null) return;

        float widthScale = (widthSeekBar.getProgress() + 100) / 100f;
        float heightScale = (heightSeekBar.getProgress() + 100) / 100f;

        int newWidth = (int)(originalBitmap.getWidth() * widthScale);
        int newHeight = (int)(originalBitmap.getHeight() * heightScale);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
        imageView.setImageBitmap(scaledBitmap);
    }

    private void saveToGallery() {
        try {
            if (imageView.getDrawable() == null) return;

            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "IMG_" + timeStamp + ".jpg";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

                Uri uri = requireContext().getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                if (uri != null) {
                    OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();
                    Toast.makeText(getContext(), "Сохранено в галерею", Toast.LENGTH_SHORT).show();
                }
            } else {
                File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                directory.mkdirs();

                File file = new File(directory, fileName);
                FileOutputStream outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();

                MediaStore.Images.Media.insertImage(
                        requireContext().getContentResolver(),
                        file.getAbsolutePath(),
                        fileName,
                        "Изображение из Notes"
                );

                Toast.makeText(getContext(), "Сохранено: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}