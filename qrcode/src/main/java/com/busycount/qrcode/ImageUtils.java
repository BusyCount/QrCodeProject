package com.busycount.qrcode;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 项目名称：Ytb
 * 类描述：image相关类
 * 创建人：gaokang
 * 创建时间：2016/3/16 13:32
 * 修改人：gaokang
 * 修改时间：2016/3/16 13:32
 * 修改备注：
 */
public class ImageUtils {
    /**
     * 伸缩图片大小
     */
    private static final String SCALE_IMAGE = "!/both/100x100";
    /**
     * 获取图片信息
     */
    private static final String IMAGE_INFO_PATH = "!/info";
    public static final int TAKE_PHOTO_FROM_CAMERA = 0x1000;// 从相机返回
    public static final int TAKE_PHOTO_FROM_ALBUM = 0x1001;// 从系统相册返回
    public static final int TAKE_PHOTO_FROM_CROP = 0x1002;// 从裁剪图片返回
    public static final int TAKE_PHOTO_FROM_MULTIPLE = 0x1003;// 从多选图片返回


    /**
     * 从相册获得图片
     * 首先在回调中拿到uri,根据uri通过方法.拿到路径
     * String imgPath = ImageUtils.getImageAbsolutePath(mContext, data.getData());
     *
     * @param activity
     */
    public static void takePhotoFromAbnum(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        activity.startActivityForResult(intent, TAKE_PHOTO_FROM_ALBUM);
    }

    /**
     * 裁剪图片的临时文件路径
     */
    public static String cropTempImagePath;


    /**
     * 裁剪图片
     *
     * @param activity activity
     * @param imgUri   url
     */
    public static void takePhotoFromCrop(Activity activity, Uri imgUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        intent.setData(imgUri);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);
        activity.startActivityForResult(intent, TAKE_PHOTO_FROM_CROP);
    }
/**
 * 裁剪之后,onActivityResult中的处理
 * Bundle bundle = picdata.getExtras();
 if (null != bundle) {
 Bitmap mBitmap = bundle.getParcelable("data");
 mImageView.setImageBitmap(mBitmap);
 saveBitmap(Environment.getExternalStorageDirectory() + "/crop_"
 + System.currentTimeMillis() + ".png", mBitmap);
 }
 * */
//-------------------------------------------------------


    /**
     * 按质量压缩一个bitmap 压缩级别0-100
     *
     * @param image 将要压缩的bitmap对象
     * @return 返回的是压缩后的大小小于100k的bitmap
     */
    public static Bitmap compressBmpByQuality(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();// 重置baos即清空baos
            image.compress(CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        try {
            isBm.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * @param srcPath 图片文件的路径
     * @return
     */
    public static Bitmap compressBmpBySize(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空,但是options里面已经包含了图片的宽
        // 高信息

        newOpts.inJustDecodeBounds = false;// 把这个标志位设置为false
        int w = newOpts.outWidth;// 把我们得到的宽高取出来
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;// 这里设置高度为800f
        float ww = 480f;// 这里设置宽度为480f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (w / ww);
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (h / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressBmpByQuality(bitmap);// 压缩好比例大小后再进行质量压缩
    }

    /**
     * 保存bitmap到文件
     *
     * @param filePath 文件的路径
     * @param mBitmap  需要保存的bitmap
     */
    public void saveBitmap(String filePath, Bitmap mBitmap) {
        File f = new File(filePath);
        FileOutputStream fOut = null;
        try {
            f.createNewFile();
            fOut = new FileOutputStream(f);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fOut.close();
                Log.e("ImageUtils", "save success");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 根据Uri获取图片绝对路径，解决Android4.4以上版本Uri转换
     *
     * @param context
     * @param imageUri
     * @author yaoxing
     * @date 2016年4月6日
     */
    public static String getImageAbsolutePath(Activity context, Uri imageUri) {
        if (context.getPackageManager().checkPermission("android.permission.WRITE_EXTERNAL_STORAGE", context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
            if (context == null || imageUri == null)
                return null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, imageUri)) {
                if (isExternalStorageDocument(imageUri)) {
                    String docId = DocumentsContract.getDocumentId(imageUri);
                    String[] split = docId.split(":");
                    String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                } else if (isDownloadsDocument(imageUri)) {
                    String id = DocumentsContract.getDocumentId(imageUri);
                    if (!TextUtils.isEmpty(id)) {
                        Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                        return getDataColumn(context, contentUri, null, null);
                    }
                } else if (isMediaDocument(imageUri)) {
                    String docId = DocumentsContract.getDocumentId(imageUri);
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
                    String selection = MediaStore.Images.Media._ID + "=?";
                    String[] selectionArgs = new String[]{split[1]};
                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            } // MediaStore (and general)
            else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
                // Return the remote address
                if (isGooglePhotosUri(imageUri))
                    return imageUri.getLastPathSegment();
                return getDataColumn(context, imageUri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
                return imageUri.getPath();
            }
        } else {
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


}
