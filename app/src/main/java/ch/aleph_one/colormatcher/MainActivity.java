package ch.aleph_one.colormatcher;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    static final String TAG = "ColorMatcher";
    ImageView imageView;
    int rgb;
    private File uploadFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.imageView = (ImageView) findViewById(R.id.imageView);
        rgb = new Random().nextInt();
        this.imageView.setBackgroundColor(rgb);
    }

    public void search(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                File f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File outputDir = getCacheDir();
                uploadFile = File.createTempFile("search", ".bmp", f);
                Log.w(MainActivity.TAG, "uploadFile: " + uploadFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(uploadFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.w(MainActivity.TAG, "wwwwwwwwwwwwwwww" + requestCode + ":" + resultCode + ":" + data + ":" + uploadFile);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                //FileInputStream in = new FileInputStream(uploadFile);
                Log.w(TAG, uploadFile.length() + ":" + uploadFile.isFile());
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                String imagePath = uploadFile.getAbsolutePath();
                BitmapFactory.decodeFile(imagePath, bmOptions);
                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;

                // Determine how much to scale down the image
                int scaleFactor = Math.min(photoW/imageView.getHeight(), photoH/imageView.getWidth());

                // Decode the image file into a Bitmap sized to fill the View
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = scaleFactor;
                bmOptions.inPurgeable = true;

                Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                uploadFile.deleteOnExit();
                int r = (this.rgb >> 16) & 0xFF;
                int g = (this.rgb >> 8) & 0xFF;
                int b = this.rgb & 0xFF;
                int score = 0;
                for (int x = 0; x < bitmap.getWidth(); x++) {
                    for (int y = 0; y < bitmap.getHeight(); y++) {
                        int p = bitmap.getPixel(x, y);
                        if (check2(p, r, g, b)) {
                            score++;
                            bitmap.setPixel(x, y, 0xFFFFFF);
                        }
                    }
                }
                score = (int) Math.log10(score);
                Toast.makeText(getBaseContext(), "Score: " + score, Toast.LENGTH_LONG).show();
                Log.w(TAG, "Score: " + score);
                imageView.setImageBitmap(bitmap);
                imageView.setRotation(90);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    private int maxDist = 60;
    private boolean check(int pixel, int r, int g, int b) {
        int b2 = pixel & 0xFF;
        if (b > b2 + maxDist || b < b2 - maxDist) {
            return false;
        }
        int g2 = (pixel >> 8) & 0xFF;
        if (g > g2 + maxDist || g < g2 - maxDist) {
            return false;
        }
        int r2 = (pixel >> 16) & 0xFF;
        if (r > r2 + maxDist || r < r2 - maxDist) {
            return false;
        }
        return true;
    }
    private boolean check2(int pixel, int r, int g, int b) {
        int b2 = (pixel & 0xFF) - b;
        int g2 = ((pixel >> 8) & 0xFF) - g;
        int r2 = ((pixel >> 16) & 0xFF) - r;
        long d = b2 * b2 + g2 * g2 + r2 * r2;
        return d < 20000;
    }

}
