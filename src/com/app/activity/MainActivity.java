package com.app.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity implements OnClickListener {
	private Button butph, buttc;
	private ImageView im;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		LogUtils.allowD = true;
		LogUtils.customTagPrefix = "itchen";

		butph = (Button) findViewById(R.id.phones);
		buttc = (Button) findViewById(R.id.tukuxuanze);
		im = (ImageView) findViewById(R.id.imageView1);
		butph.setOnClickListener(this);
		buttc.setOnClickListener(this);
	}

	private Uri imuri;

	private String getFileName() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MMdd_HH");
		return dateFormat.format(date) + "bitmap.jpg";

	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.imageView1:

			break;
		case R.id.phones:
			// 手机sdcark的跟路径,当然这里可以写
			File output = new File(Environment.getExternalStorageDirectory(), getFileName());

			try {
				if (output.exists()) {
					output.delete();
				}
				output.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			imuri = Uri.fromFile(output);
			Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");

			intent.putExtra(MediaStore.EXTRA_OUTPUT, imuri);
			startActivityForResult(intent, 100);
			break;
		case R.id.tukuxuanze:
			bitmapFormTuku();

			break;
		default:
			break;
		}
	}

	/**
	 * 调用图库中图片的各种参数
	 */
	private void bitmapFormTuku() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		// intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
		// "image/*");
		intent.putExtra("crop", true);// 设置了参数，就会调用裁剪，如果不设置，就会跳过裁剪的过程。
		intent.putExtra("aspectX", 53);// 这个是裁剪时候的 裁剪框的 X 方向的比例。
		intent.putExtra("aspecty", 63);

		intent.putExtra("outputX", 50); // 返回数据的时候的 X 像素大小。
		intent.putExtra("outputY", 100); // 返回的时候 Y 的像素大小。

		intent.putExtra("return-data", true);// 是否要返回值。 一般都要
		startActivityForResult(intent, 300);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 100:

				Intent intent = new Intent("com.android.camera.action.CROP");
				intent.setDataAndType(imuri, "image/*");
				intent.putExtra("scale", true);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imuri);
				startActivityForResult(intent, 200);

				break;
			case 200:

				try {
					Bitmap bit = BitmapFactory.decodeStream(getContentResolver().openInputStream(imuri));
					im.setImageBitmap(bit);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			case 300:
				
				
				/*Uri uri = data.getData();
				if (uri != null) {
					String realPath = getRealPathFromURI(uri);
					Log.e(tag, "获取图片成功，path=" + realPath);
					toast("获取图片成功，path=" + realPath);
					setImageView(realPath);
				} else {
					Log.e(tag, "从相册获取图片失败");
				}*/

				Uri uri = data.getData();
				String realPath = getRealPathFromURI(uri);
				LogUtils.d("itchen--realPath="+realPath);
				setImageView(realPath);
				
				/*
				if (data != null) {
					im.setImageURI(uri);
				}*/
				break;
			default:
				break;
			}
		}
	}
	
	
	/**
	 * 读取照片exif信息中的旋转角度<br/>
	 * http://www.eoeandroid.com/thread-196978-1-1.html
	 * 
	 * @param path
	 *            照片路径
	 * @return角度
	 */
	public static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}
	
	private void setImageView(String realPath) {
		Bitmap bmp = BitmapFactory.decodeFile(realPath);
		int degree = readPictureDegree(realPath);
		if (degree <= 0) {
			im.setImageBitmap(bmp);
		} else {
			//Log.e(tag, "rotate:" + degree);
			//创建操作图片是用的matrix对象
			Matrix matrix = new Matrix();
			//旋转图片动作
			matrix.postRotate(degree);
			//创建新图片
			Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
			im.setImageBitmap(resizedBitmap);
		}
	}
	
	public String getRealPathFromURI(Uri contentUri) {
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			// Do not call Cursor.close() on a cursor obtained using this method, 
			// because the activity will do that for you at the appropriate time
			Cursor cursor = this.managedQuery(contentUri, proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} catch (Exception e) {
			return contentUri.getPath();
		}
	}

}
