/*
 * Copyright (c) 2016-2018 Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Confidential and Proprietary - Qualcomm Technologies, Inc.
 */
package com.left.drawingboard.Model.Task;

import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Pair;

import com.qualcomm.qti.snpe.FloatTensor;
import com.qualcomm.qti.snpe.NeuralNetwork;
import com.qualcomm.qti.snpe.Tensor;
import com.left.drawingboard.Model.Model;
import com.left.drawingboard.Model.ModelController;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ClassifyImageWithFloatTensorTask extends AbstractClassifyImageTask {

    private static final String LOG_TAG = ClassifyImageWithFloatTensorTask.class.getSimpleName();

    public ClassifyImageWithFloatTensorTask(ModelController controller,
                             NeuralNetwork network, Bitmap image, Model model) {
        super(controller, network, image, model);
    }

    @Override
    protected String[] doInBackground(Bitmap... params) {
        final List<String> result = new LinkedList<>();

        final FloatTensor tensor = mNeuralNetwork.createFloatTensor(
                mNeuralNetwork.getInputTensorsShapes().get(mInputLayer));

        loadMeanImageIfAvailable(mModel.meanImage, tensor.getSize());

        final int[] dimensions = tensor.getShape();
        final boolean isGrayScale = (dimensions[dimensions.length -1] == 1);
        float[] rgbBitmapAsFloat;
        if (!isGrayScale) {
            rgbBitmapAsFloat = loadRgbBitmapAsFloat(mImage);
        } else {
            rgbBitmapAsFloat = loadGrayScaleBitmapAsFloat(mImage);
        }
        tensor.write(rgbBitmapAsFloat, 0, rgbBitmapAsFloat.length);

        final Map<String, FloatTensor> inputs = new HashMap<>();
        inputs.put(mInputLayer, tensor);

        final long javaExecuteStart = SystemClock.elapsedRealtime();
        final Map<String, FloatTensor> outputs = mNeuralNetwork.execute(inputs);
        final long javaExecuteEnd = SystemClock.elapsedRealtime();
        mJavaExecuteTime = javaExecuteEnd - javaExecuteStart;

        for (Map.Entry<String, FloatTensor> output : outputs.entrySet()) {
            if (output.getKey().equals(mOutputLayer)) {
                FloatTensor outputTensor = output.getValue();

                final float[] array = new float[outputTensor.getSize()];
                outputTensor.read(array, 0, array.length);
                String s="";
                for(Float x:array)
                {
                    s=s.concat(String.format("%.2f",x));
                    s=s.concat(" ");
                }

//                for (Pair<Integer, Float> pair : topK(1, array)) {
//                    s=s.concat(mModel.labels[pair.first]);
//                    s=s.concat(":");
//                    s=s.concat(String.valueOf(pair.second));
//                }
                result.add(s);
            }
        }

        releaseTensors(inputs, outputs);

        return result.toArray(new String[result.size()]);
    }

    @SafeVarargs
    private final void releaseTensors(Map<String, ? extends Tensor>... tensorMaps) {
        for (Map<String, ? extends Tensor> tensorMap: tensorMaps) {
            for (Tensor tensor: tensorMap.values()) {
                tensor.release();
            }
        }
    }
}
