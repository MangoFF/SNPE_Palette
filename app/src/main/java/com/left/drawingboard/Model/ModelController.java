/*
 * Copyright (c) 2016-2018 Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Confidential and Proprietary - Qualcomm Technologies, Inc.
 */
package com.left.drawingboard.Model;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.view.View;


import com.left.drawingboard.LevelActivity;
import com.left.drawingboard.MainActivity;
import com.left.drawingboard.Model.AbstractViewController;
import com.left.drawingboard.Model.Model;
import com.left.drawingboard.Model.Task.AbstractClassifyImageTask;
import com.left.drawingboard.Model.Task.ClassifyImageWithFloatTensorTask;
import com.left.drawingboard.Model.Task.ClassifyImageWithUserBufferTf8Task;
import com.left.drawingboard.Model.Task.LoadImageTask;
import com.left.drawingboard.Model.Task.LoadModelsTask;
import com.left.drawingboard.Model.Task.LoadNetworkTask;
import com.qualcomm.qti.snpe.NeuralNetwork;
import com.qualcomm.qti.snpe.SNPE;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModelController extends
        AbstractViewController<LevelActivity> {
    //支持的Tensor各式
    public enum SupportedTensorFormat {
        FLOAT,
        UB_TF8
    }
    //名字，BitMap字典，用model.jpg可以得到附带的图
    public final Map<String, SoftReference<Bitmap>> mBitmapCache;

    //返回的多个模型，我们只导入一个，所以只用一个
    public   Set<Model> mModels;
    //表情识别模型
    public Model mModel=null;

    private NeuralNetwork mNeuralNetwork;

    private final Application mApplication;

    private LoadNetworkTask mLoadTask;
    //使用的平台
    private NeuralNetwork.Runtime mRuntime;
    //当前所使用的Tensor格式
    private SupportedTensorFormat mCurrentSelectedTensorFormat;
    //网络支持的Tensor格式
    private SupportedTensorFormat mNetworkTensorFormat;
    //PD不知道是个啥
    private boolean mUnsignedPD;

    public Bitmap mCurrentBitmap2Classify;
    //主窗口的类，用于回显
    public LevelActivity mview;

    public final Context mContext;

    private static final Set<String> mSupportedModels = new HashSet<String>() {{
        add("inception_v3_quantized");
    }};

    public int VideoPlaytime=1;
    private AssetFileDescriptor fileDescriptor;
    private int VideoLenth=-1;
    MediaMetadataRetriever retriever;
    public Boolean ClassifyReturn=true;
    public Handler handler;

    public ModelController(Context context,final Application application) {
        mContext = context;
        mApplication = application;
        mUnsignedPD = true;
        mCurrentSelectedTensorFormat=SupportedTensorFormat.UB_TF8;
        mRuntime=NeuralNetwork.Runtime.CPU;
        mBitmapCache = new HashMap<>();
        retriever = new MediaMetadataRetriever();
    }

    @Override
    protected void onViewAttached(final LevelActivity view) {
        mview=view;
        //设置监视器，检测到文件变化的时候导入模型，否则就直接导入模型
        final ContentResolver contentResolver = mContext.getContentResolver();
        contentResolver.registerContentObserver(Uri.withAppendedPath(
                Model.MODELS_URI, Model.INVALID_ID), false, mModelExtractionFailedObserver);
        contentResolver.registerContentObserver(Model.MODELS_URI, true, mModelExtractionObserver);
        //解压文件，如果已经解压那么啥也不会干
        startModelsExtraction();
        //导入模型（如果第一次异步还没解压，则等文件改变调用service)
        loadModels();

    }
    @Override
    protected void onViewDetached(final LevelActivity view) {
        final ContentResolver contentResolver = mContext.getContentResolver();
        contentResolver.unregisterContentObserver(mModelExtractionObserver);
        contentResolver.unregisterContentObserver(mModelExtractionFailedObserver);
        if (mNeuralNetwork != null) {
            mNeuralNetwork.release();
            mNeuralNetwork = null;
        }
    }
    //开始解压
    public void startModelsExtraction() {
        for (Iterator<String> it = mSupportedModels.iterator(); it.hasNext();) {
            String modelName = it.next();
            int resId = getRawResourceId(modelName);
            if (resId == 0) {
                it.remove();
            } else {
                ModelExtractionService.extractModel(mContext, modelName, resId);
            }
        }
    }
    //导入照片
    public void loadImageSamples() {
        for (int i = 0; i < mModel.jpgImages.length; i++) {
            final File jpeg = mModel.jpgImages[i];
            final Bitmap cached = getCachedBitmap(jpeg);
            final LoadImageTask task = new LoadImageTask(this, jpeg);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    //检测到文件改动，导入模型
    private final ContentObserver mModelExtractionObserver =
            new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    if (isAttached()) {
                        loadModels();
                    }
                }
            };

    //检测到文件为空，报错
    private final ContentObserver mModelExtractionFailedObserver =
            new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    if (isAttached()) {
                    }
                }
            };

    //导入模型
    private void loadModels() {
        final LoadModelsTask task = new LoadModelsTask(mContext, this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    //识别图片 bitmap
    public void classify(final Bitmap bitmap) {
        if (mNeuralNetwork != null && bitmap!=null) {
            AbstractClassifyImageTask task;
            Bitmap bitmap_resize=Resize_bmp(bitmap,430,430);
            //mview.fragment.map.setImageBitmap(bitmap_resize);
            switch (mNetworkTensorFormat) {
                case UB_TF8:
                    ClassifyReturn=false;
                    task = new ClassifyImageWithUserBufferTf8Task(this, mNeuralNetwork, bitmap_resize, mModel);
                    break;
                case FLOAT:
                default:
                    ClassifyReturn=false;
                    task = new ClassifyImageWithFloatTensorTask(this, mNeuralNetwork, bitmap_resize, mModel);
                    break;
            }
            task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

        } else {
            //getView().displayModelNotLoaded();
        }
    }

    public Set<String> getAvailableModels() {
        return mSupportedModels;
    }

    private int getRawResourceId(String rawName) {
        return mContext.getResources().getIdentifier(rawName, "raw", mContext.getPackageName());
    }

    private Bitmap getCachedBitmap(File jpeg) {
        final SoftReference<Bitmap> reference = mBitmapCache.get(jpeg.getAbsolutePath());
        if (reference != null) {
            final Bitmap bitmap = reference.get();
            if (bitmap != null) {
                return bitmap;
            }
        }
        return null;
    }

    //导入网络
    public void loadNetwork() {
        if (isAttached()) {
            final NeuralNetwork neuralNetwork = mNeuralNetwork;
            if (neuralNetwork != null) {
                neuralNetwork.release();
                mNeuralNetwork = null;
            }

            if (mLoadTask != null) {
                mLoadTask.cancel(false);
            }

            mNetworkTensorFormat = mCurrentSelectedTensorFormat;
            mLoadTask = new LoadNetworkTask(mApplication, this, mModel, mRuntime, mCurrentSelectedTensorFormat, mUnsignedPD);
            mLoadTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }
    }

    public void onBitmapLoaded(File imageFile, Bitmap bitmap) {
        mBitmapCache.put(imageFile.getAbsolutePath(), new SoftReference<>(bitmap));
        //classify(mBitmapCache.get(mModel.jpgImages[0].getAbsolutePath()).get());
    }
    //回显导入网络
    public void onNetworkLoaded(NeuralNetwork neuralNetwork, final long loadTime) {
        if (isAttached()) {
            mNeuralNetwork = neuralNetwork;
            loadImageSamples();
        } else {
            neuralNetwork.release();
        }
        mLoadTask = null;
    }//回显调用这个设置导入的model
    public void setModels(Set<Model> modelset) {
        mModels = modelset;
        if (mModels.size() >= 1)
        {
            mModel =(Model)mModels.toArray()[0];
            loadNetwork();
        }

    }

    public Bitmap getBitmapsFromVideo()  {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            AssetFileDescriptor fileDescriptor = mContext.getAssets().openFd("emotion.mp4");

            retriever.setDataSource(fileDescriptor.getFileDescriptor(),fileDescriptor.getStartOffset(),fileDescriptor.getLength());
            // 取得视频的长度(单位为毫秒)
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            // 取得视频的长度(单位为秒)
            int seconds = Integer.valueOf(time) / 1000;
            // 得到每一秒时刻的bitmap比如第一秒,第二秒
            Bitmap bitmap = retriever.getFrameAtTime(seconds/1000* 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            bitmap=Resize_bmp(bitmap,430,430);
            mCurrentBitmap2Classify=bitmap;
            return bitmap;
        }catch (Exception e) {
            return null;
        }
    }
    private Bitmap Resize_bmp(Bitmap rootImg, int goalW, int goalH) {
        int rootW = rootImg.getWidth();
        int rootH = rootImg.getHeight();
        // graphics 包下的
        Matrix matrix = new Matrix();
        matrix.postScale(goalW * 1.0f / rootW, goalH * 1.0f / rootH);
        return Bitmap.createBitmap(rootImg, 0, 0, rootW, rootH, matrix, true);
    }
}
