/*
 * Copyright (c) 2016-2018 Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Confidential and Proprietary - Qualcomm Technologies, Inc.
 */
package com.left.drawingboard.Model.Task;

import android.app.Application;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.left.drawingboard.Model.ModelController;
import com.qualcomm.qti.snpe.NeuralNetwork;
import com.qualcomm.qti.snpe.SNPE;
import com.left.drawingboard.Model.Model;
import com.left.drawingboard.Model.ModelController;
import java.io.File;
import java.io.IOException;

public class LoadNetworkTask extends AsyncTask<File, Void, NeuralNetwork> {

    private static final String LOG_TAG = LoadNetworkTask.class.getSimpleName();

    private final ModelController mController;

    private final Model mModel;

    private final Application mApplication;

    private final NeuralNetwork.Runtime mTargetRuntime;

    private final ModelController.SupportedTensorFormat mTensorFormat;

    private boolean mUnsignedPD;

    private long mLoadTime = -1;

    public LoadNetworkTask(final Application application,
                           final ModelController controller,
                           final Model model,
                           final NeuralNetwork.Runtime targetRuntime,
                           final ModelController.SupportedTensorFormat tensorFormat,
                           boolean unsignedPD) {
        mApplication = application;
        mController = controller;
        mModel = model;
        mTargetRuntime = targetRuntime;
        mTensorFormat = tensorFormat;
        mUnsignedPD = unsignedPD;
    }

    @Override
    protected NeuralNetwork doInBackground(File... params) {
        NeuralNetwork network = null;
        try {
            final SNPE.NeuralNetworkBuilder builder = new SNPE.NeuralNetworkBuilder(mApplication)
                    .setDebugEnabled(false)
                    .setRuntimeOrder(mTargetRuntime)
                    .setModel(mModel.file)
                    .setCpuFallbackEnabled(true)
                    .setUseUserSuppliedBuffers(mTensorFormat != ModelController.SupportedTensorFormat.FLOAT)
                    .setUnsignedPD(mUnsignedPD);
            if (mUnsignedPD){
                builder.setRuntimeCheckOption(NeuralNetwork.RuntimeCheckOption.UNSIGNEDPD_CHECK);
            }

            final long start = SystemClock.elapsedRealtime();
            network = builder.build();
            final long end = SystemClock.elapsedRealtime();

            mLoadTime = end - start;
        } catch (IllegalStateException | IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return network;
    }

    @Override
    protected void onPostExecute(NeuralNetwork neuralNetwork) {
        super.onPostExecute(neuralNetwork);
        if (neuralNetwork != null) {
            if (!isCancelled()) {
                mController.onNetworkLoaded(neuralNetwork, mLoadTime);
            } else {
                neuralNetwork.release();
            }
        } else {
            if (!isCancelled()) {
                //mController.onNetworkLoadFailed();
            }
        }
    }
}
