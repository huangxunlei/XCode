package com.xingkong.xcode;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.interfaces.MyItemDialogListener;
import com.xingkong.xkzing.CaptureActivity;
import com.xingkong.xkzing.common.ActionUtils;
import com.xingkong.xkzing.common.QrUtils;
import com.xingkong.xkzing.zxing.decoding.InactivityTimer;

import java.util.ArrayList;
import java.util.List;

import static com.xingkong.xcode.R.id.btn_add_qrcode;
import static com.xingkong.xkzing.common.ActionUtils.PHOTO_REQUEST_GALLERY;


public class MainActivity extends AppCompatActivity {
    private TextView resultTextView;
    private EditText qrStrEditText;
    private ImageView qrImgImageView;
    private CheckBox mCheckBox;
    private List<String> index;
    private InactivityTimer inactivityTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        index = new ArrayList<>();

        resultTextView = (TextView) this.findViewById(R.id.tv_scan_result);
        qrStrEditText = (EditText) this.findViewById(R.id.et_qr_string);
        qrImgImageView = (ImageView) this.findViewById(R.id.iv_qr_image);
        mCheckBox = (CheckBox) findViewById(R.id.logo);
        inactivityTimer = new InactivityTimer(this);
        Button scanBarCodeButton = (Button) this.findViewById(R.id.btn_scan_barcode);
        scanBarCodeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, CaptureActivity.class);
                MainActivity.this.startActivityForResult(i, 1);
               /* //打开扫描界面扫描条形码或二维码
                Intent openCameraIntent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(openCameraIntent, 0);*/
            }
        });

        Button generateQRCodeButton = (Button) this.findViewById(btn_add_qrcode);
        generateQRCodeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ActionUtils.startActivityForGallery(MainActivity.this, PHOTO_REQUEST_GALLERY);
             /*   ActionUtils.startActivityForImageCut(MainActivity.this, PHOTO_REQUEST_CUT,
                        Uri.parse(Environment.getExternalStorageDirectory() + "/Picture"),
                        Uri.parse(Environment.getExternalStorageDirectory() + "/image.jpg"), 300, 300);
                String contentString = qrStrEditText.getText().toString();*/
                ;
            /*    if (!contentString.equals("")) {
                    //根据字符串生成二维码图片并显示在界面上，第二个参数为图片的大小（350*350）
                    Bitmap qrCodeBitmap = EncodingUtils.createQRCode(contentString, 350, 350,
                            mCheckBox.isChecked() ?
                                    BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher) :
                                    null);
                    qrImgImageView.setImageBitmap(qrCodeBitmap);
                } else {
                    Toast.makeText(MainActivity.this, "Text can not be empty", Toast.LENGTH_SHORT).show();
                }*/
            }
        });
        qrImgImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Bitmap obmp = ((BitmapDrawable) (qrImgImageView).getDrawable()).getBitmap();
                int width = obmp.getWidth();
                int height = obmp.getHeight();
                int[] data = new int[width * height];
                obmp.getPixels(data, 0, width, 0, 0, width, height);
                RGBLuminanceSource source = new RGBLuminanceSource(width, height, data);
                BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
                QRCodeReader reader = new QRCodeReader();
                Result re = null;
                try {
                    re = reader.decode(bitmap1);
                } catch (NotFoundException e) {
                    e.printStackTrace();
                } catch (ChecksumException e) {
                    e.printStackTrace();
                } catch (FormatException e) {
                    e.printStackTrace();
                }
                if (re == null) {
                    index.clear();
                    index.add("保存图片");
                    // showAlert(obmp);
                    showSelectAlert(obmp);
                } else {
                    index.clear();
                    index.add("保存图片");
                    index.add("识别图片中的二维码");
                    showSelectAlert(obmp);
                }

                return true;
            }
        });
    }

    private void showSelectAlert(final Bitmap obmp) {
        StyledDialog.buildBottomItemDialog(index, null, new MyItemDialogListener() {
            @Override
            public void onItemClick(CharSequence charSequence, int i) {
                if (index.size() == 2) {
                    if (i == 1) {
                        Result result = QrUtils.decodeDirect(obmp);
                        handleDecode(result, null);
                    }

                }
            }
        }).setActivity(this).show();
    }

    /**
     * Handler scan result
     *
     * @param result
     * @param barcode
     */
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        String resultString = result.getText();

        Log.e("hxl", resultString);
        Toast.makeText(this, resultString, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Uri inputUri = data.getData();
            String path = null;
            if (requestCode == PHOTO_REQUEST_GALLERY) {
                //Log.e("hxl", data.getExtras().get("data").toString());
               /* ActionUtils.startActivityForImageCut(MainActivity.this, PHOTO_REQUEST_CUT,
                        data.getData(),
                        Uri.parse(Environment.getExternalStorageDirectory() + "/image.jpg"), 300, 300);*/
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(inputUri, proj, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                }

                Bitmap bitmap = BitmapFactory.decodeFile(path);
                qrImgImageView.setImageBitmap(bitmap);
            } else {
                Bundle bundle = data.getExtras();
                String scanResult = bundle.getString("result");
                resultTextView.setText(scanResult);
            }

        }
    }
}
