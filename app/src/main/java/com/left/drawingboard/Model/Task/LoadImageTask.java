/*
 * Copyright (c) 2016 Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Confidential and Proprietary - Qualcomm Technologies, Inc.
 */
package com.left.drawingboard.Model.Task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.left.drawingboard.Model.Model;
import com.left.drawingboard.Model.ModelController;

import java.io.File;

public class LoadImageTask extends AsyncTask<File, Void, Bitmap> {

    private final ModelController mController;

    private final File mImageFile;

    public LoadImageTask(ModelController controller, final File imageFile) {
        mController = controller;
        mImageFile = imageFile;
    }

    @Override
    protected Bitmap doInBackground(File... params) {
        return BitmapFactory.decodeFile(mImageFile.getAbsolutePath());
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        mController.onBitmapLoaded(mImageFile, bitmap);
    }
}

