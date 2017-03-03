package cn.bahamut.vessage.main;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.bahamut.common.FileHelper;
import cn.bahamut.vessage.R;

/**
 * Created by alexchow on 2016/11/16.
 */

public class SelectImageSourceAlertDialogBuilder {
    private Activity context;
    private File fileForSaveFromCamera = null;

    public SelectImageSourceAlertDialogBuilder(Activity context) {
        this.context = context;
        fileForSaveFromCamera = new File(Environment.getExternalStorageDirectory(), FileHelper.generateTempFileNameWithType(".jpg"));
    }

    public File getFileForSaveFromCamera() {
        return fileForSaveFromCamera;
    }

    private void newImageVessageFromAlbum(int requestId) {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);//调用android的图库
        context.startActivityForResult(intent, requestId);
    }

    private void newImageVessageFromCamera(int requestId) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (fileForSaveFromCamera.exists()) {
            fileForSaveFromCamera.delete();
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileForSaveFromCamera));
        context.startActivityForResult(intent, requestId);
    }

    public AlertDialog showSourceImageAlert(int titleResId, int msgResId, final int requestIdAlbum, final int requestIdCamera) {
        DialogInterface.OnClickListener onClickAlbumListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newImageVessageFromAlbum(requestIdAlbum);
            }
        };

        DialogInterface.OnClickListener onClickCameraListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newImageVessageFromCamera(requestIdCamera);
            }
        };

        AlertDialog dialog = new android.support.v7.app.AlertDialog.Builder(context)
                .setTitle(titleResId)
                .setMessage(msgResId)
                .setCancelable(true)
                .setPositiveButton(R.string.img_from_album, onClickAlbumListener)
                .setNegativeButton(R.string.img_from_camera, onClickCameraListener)
                .show();
        return dialog;
    }

    public AlertDialog showSourceImageActionSheet(int titleResId, int msgResId, final int requestIdAlbum, final int requestIdCamera, String[] extraActions, final DialogInterface.OnClickListener onClickWitchListener) {

        List<String> actionList = new ArrayList<>(2 + extraActions.length);
        actionList.add(LocalizedStringHelper.getLocalizedString(R.string.img_from_album));
        actionList.add(LocalizedStringHelper.getLocalizedString(R.string.img_from_camera));
        for (String action : extraActions) {
            actionList.add(action);
        }

        AlertDialog.Builder dialogBuilder = new android.support.v7.app.AlertDialog.Builder(context)
                .setCancelable(true)
                .setItems(actionList.toArray(extraActions), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            newImageVessageFromAlbum(requestIdAlbum);
                        } else if (which == 1) {
                            newImageVessageFromCamera(requestIdCamera);
                        } else {
                            onClickWitchListener.onClick(dialog, which - 2);
                        }
                    }
                });
        if (titleResId != 0) {
            dialogBuilder.setTitle(titleResId);
        }
        if (msgResId != 0) {
            dialogBuilder.setMessage(msgResId);
        }
        return dialogBuilder.show();
    }
}
