/*
 * Author: Dou Walker
 * Time: 2021/8/22
 */
package com.left.drawingboard.beans;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.left.drawingboard.R;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class LevelInfoBean {
    private int levelNo;
    private int buttonImage;
    private int mgrade;
    private int bias;

    private static List<Integer> buttonDrawableId =new ArrayList() {{
        add(R.drawable.green_item);
        add(R.drawable.blue_item);
        add(R.drawable.purple_item);
        add(R.drawable.red_item);
    }};

    private static List<Integer> gradeDrawableId = new ArrayList() {{
        add(R.drawable.no_star);
        add(R.drawable.one_star);
        add(R.drawable.two_star);
        add(R.drawable.three_star);
    }};

    public LevelInfoBean(int level, int grade, int btnImageNo) {
        levelNo = level;
        mgrade = gradeDrawableId.get(Math.abs(grade) % gradeDrawableId.size());
        buttonImage = buttonDrawableId.get(Math.abs(btnImageNo) % buttonDrawableId.size());

        if (level % 2 == 0) {
            bias = 550;
        } else {
            bias = 100;
        }

    }

    public int getBias() {
        return bias;
    }

    public void setBias(int bias) {
        this.bias = bias;
    }

    public int getLevelNo() {
        return levelNo;
    }

    public void setLevelNo(int levelNo) {
        this.levelNo = levelNo;
    }

    public int getButtonImage() {
        return buttonImage;
    }

    public void setButtonImage(int buttonImage) {
        this.buttonImage = buttonDrawableId.get(Math.abs(buttonImage) % buttonDrawableId.size());
    }

    public int getMgrade() {
        return mgrade;
    }

    public void setMgrade(int mgrade) {
        this.mgrade = gradeDrawableId.get(Math.abs(mgrade) % gradeDrawableId.size());
    }
}
