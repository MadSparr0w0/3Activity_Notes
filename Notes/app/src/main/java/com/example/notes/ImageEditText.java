package com.example.notes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Base64;
import androidx.appcompat.widget.AppCompatEditText;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageEditText extends AppCompatEditText {

    private Context context;
    private List<String> imageBase64List = new ArrayList<>();
    private static final String IMAGE_MARKER = "[IMG]";

    public ImageEditText(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public ImageEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public ImageEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        setTextSize(16);
        setPadding(16, 16, 16, 16);
    }

    public void insertImage(Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);

            if (originalBitmap == null) {
                inputStream.close();
                return;
            }

            int maxSize = 800;
            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();

            Bitmap scaledBitmap;
            if (width > height && width > maxSize) {
                float ratio = (float) height / width;
                scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, maxSize, (int)(maxSize * ratio), true);
            } else if (height > width && height > maxSize) {
                float ratio = (float) width / height;
                scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, (int)(maxSize * ratio), maxSize, true);
            } else {
                scaledBitmap = originalBitmap;
            }

            String base64 = bitmapToBase64(scaledBitmap);
            imageBase64List.add(base64);

            Editable editable = getText();
            int cursorPos = getSelectionStart();

            if (cursorPos > 0 && editable.length() > 0) {
                editable.insert(cursorPos, "\n");
                cursorPos++;
            }

            String marker = IMAGE_MARKER + "\n";
            editable.insert(cursorPos, marker);

            replaceMarkersWithImages();

            if (scaledBitmap != originalBitmap && !scaledBitmap.isRecycled()) {
                scaledBitmap.recycle();
            }
            if (!originalBitmap.isRecycled()) {
                originalBitmap.recycle();
            }
            inputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void replaceMarkersWithImages() {
        String text = getText().toString();
        SpannableStringBuilder builder = new SpannableStringBuilder(text);

        int markerIndex = text.indexOf(IMAGE_MARKER);
        int imageCount = 0;

        while (markerIndex != -1 && imageCount < imageBase64List.size()) {
            String base64 = imageBase64List.get(imageCount);
            Bitmap bitmap = base64ToBitmap(base64);

            if (bitmap != null) {
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);

                int maxWidth = getWidth() - getPaddingLeft() - getPaddingRight() - 32;
                if (maxWidth <= 0) maxWidth = 300;

                int width = bitmap.getWidth();
                int height = bitmap.getHeight();

                if (width > maxWidth) {
                    float ratio = (float) height / width;
                    width = maxWidth;
                    height = (int)(width * ratio);
                }

                drawable.setBounds(0, 0, width, height);

                ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);

                builder.setSpan(imageSpan, markerIndex, markerIndex + IMAGE_MARKER.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                imageCount++;
            }

            markerIndex = text.indexOf(IMAGE_MARKER, markerIndex + 1);
        }

        setText(builder);
    }

    private Bitmap base64ToBitmap(String base64) {
        try {
            byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    public String getHtmlContent() {
        String text = getText().toString();
        StringBuilder html = new StringBuilder();

        String[] lines = text.split("\n");
        int imageIndex = 0;

        for (String line : lines) {
            if (line.contains(IMAGE_MARKER)) {
                String lineWithImages = line;
                while (lineWithImages.contains(IMAGE_MARKER) && imageIndex < imageBase64List.size()) {
                    String base64 = imageBase64List.get(imageIndex);
                    String imgTag = "<img src=\"data:image/jpeg;base64," + base64 + "\" style=\"max-width:100%;\" />";
                    lineWithImages = lineWithImages.replaceFirst(IMAGE_MARKER, imgTag);
                    imageIndex++;
                }
                html.append(lineWithImages);
            } else {
                html.append(line);
            }
            html.append("<br/>");
        }

        return html.toString();
    }

    public void setHtmlContent(String html) {
        try {
            imageBase64List.clear();
            setText("");

            if (html == null || html.isEmpty()) return;

            Pattern pattern = Pattern.compile("src=\"data:image/jpeg;base64,([^\"]+)\"");
            Matcher matcher = pattern.matcher(html);

            while (matcher.find()) {
                String base64 = matcher.group(1);
                imageBase64List.add(base64);
            }

            String textWithMarkers = html
                    .replaceAll("<img[^>]*>", IMAGE_MARKER)
                    .replace("<br/>", "\n")
                    .replace("<br>", "\n")
                    .replaceAll("<[^>]*>", "")
                    .replace("&amp;", "&")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&quot;", "\"")
                    .replace("&#39;", "'");

            setText(textWithMarkers);

            postDelayed(this::replaceMarkersWithImages, 100);

        } catch (Exception e) {
            e.printStackTrace();
            setText(html.replaceAll("<[^>]*>", ""));
        }
    }
}