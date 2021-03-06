package com.left.drawingboard.fragment;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.left.drawingboard.LevelActivity;
import com.left.drawingboard.MainActivity;
import com.left.drawingboard.R;
import com.left.drawingboard.SketchView;
import com.yancy.imageselector.ImageConfig;
import com.yancy.imageselector.ImageLoader;
import com.yancy.imageselector.ImageSelector;
import com.yancy.imageselector.ImageSelectorActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageSketchFilter;


public class SketchFragment extends Fragment implements SketchView.OnDrawChangedListener {
    ImageView stroke;
    ImageView eraser;
    public SketchView mSketchView;
    ImageView undo;
    ImageView redo;
    ImageView erase;
    ImageView sketch_preview=null;
//    ImageView sketchSave;
//    ImageView sketchPhoto;
    ImageView ivPainted;
    ImageView ivOriginal;
    ImageView score_back=null;
    ImageView suibmit=null;
    TextView levelname=null;
    TextView  result_txt=null;

    ImageView  next=null;
    ImageView  again=null;
    ImageView star=null;
    private View result;
    private int seekBarStrokeProgress, seekBarEraserProgress;
    private View popupStrokeLayout, popupEraserLayout;
    private ImageView strokeImageView, eraserImageView;
    // ??????????????????????????????size
    private int size;
    private ColorPicker mColorPicker;
    // ????????????????????????ColorPicker???????????????????????????????????????????????????
    private int oldColor;
    private MaterialDialog dialog;
    private Bitmap bitmap;
    private Bitmap dstBmp;
    private Bitmap grayBmp;
    private int mScreenWidth;
    private int mScreenHeight;
    private PopupWindow popup_score;
    int level=-1;
    int grad=0;
    public LevelActivity mContext;
    String[] ClassName={"airplane","banana","baseball","bicycle","bird","book","bulldozer","cake","camel","camera","cannon","car","cat","chair","computer","cookie","crown","dog","ear","eye","fish","flower","hand","hat","horse","key","keyboard","knife","ladder","monkey","mouse","nose"};

    @SuppressLint("ValidFragment")
    public SketchFragment(int level_id, LevelActivity context)
    {
        level=level_id;
        mContext=context;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sketch, container, false);
        ButterKnife.bind(this, view);
        mSketchView=view.findViewById(R.id.drawing);
        mSketchView.fragment=this;
        stroke=view.findViewById(R.id.sketch_stroke);
        eraser=view.findViewById(R.id.sketch_eraser);
        mSketchView=view.findViewById(R.id.drawing);
        undo=view.findViewById(R.id.sketch_undo);
        redo=view.findViewById(R.id.sketch_redo);
        erase=view.findViewById(R.id.sketch_erase);
//        sketchSave=view.findViewById(R.id.sketch_save);
//        sketchPhoto=view.findViewById(R.id.sketch_photo);
        ivPainted=view.findViewById(R.id.iv_painted);
        ivOriginal=view.findViewById(R.id.iv_original);
        suibmit= view.findViewById(R.id.submit_button);
        levelname=view.findViewById(R.id.level_name);
        sketch_preview=view.findViewById(R.id.preview);
        Resources res = getContext().getResources();
        Bitmap bitmap=BitmapFactory.decodeResource(res,res.getIdentifier(ClassName[level-1], "drawable", "com.left.drawingboard"));
        if(bitmap!=null)
        {
            sketch_preview.setImageBitmap(bitmap);
        }
        levelname.setText(String.format("Level %d\n%s",level,ClassName[level-1]));

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        // ???????????????????????????
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        // ???mSketchView????????????
        if(mSketchView!=null)
        {
            FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) mSketchView.getLayoutParams();
            p.width = mScreenWidth;
            p.height = mScreenHeight;
            mSketchView.setLayoutParams(p);
        }

        return view;
    }
    public void showDrawScore(String[] labels)
    {
        popup_score = new PopupWindow(getActivity());
        popup_score.setContentView(result);
        popup_score.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup_score.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup_score.setFocusable(true);

        // ??????????????????????????????
        popup_score.setBackgroundDrawable(new BitmapDrawable());
        popup_score.showAtLocation(getView(), 17,0,0);
        grad=Math.round(Float.valueOf(labels[1]).floatValue());
        if(grad>=3)
        {
            grad=3;
        }
        if(star!=null &&labels[0].equals(ClassName[level-1]))
        {
            if(grad==0)
            { star.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.no_star));
                Resources res = getContext().getResources();
                score_back.setImageBitmap(BitmapFactory.decodeResource(res,res.getIdentifier("score_back_failure", "drawable", "com.left.drawingboard")));
            }

            else if(grad==1)
            {
                star.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.one_star));
                Resources res = getContext().getResources();
                score_back.setImageBitmap(BitmapFactory.decodeResource(res,res.getIdentifier("score_back", "drawable", "com.left.drawingboard")));
            }

            else if(grad==2)
            {
                Resources res = getContext().getResources();
                score_back.setImageBitmap(BitmapFactory.decodeResource(res,res.getIdentifier("score_back", "drawable", "com.left.drawingboard")));
                star.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.two_star));
            }
            else if(grad==3)
            {
                Resources res = getContext().getResources();
                score_back.setImageBitmap(BitmapFactory.decodeResource(res,res.getIdentifier("score_back", "drawable", "com.left.drawingboard")));
                star.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.three_star));
            }
            result_txt.setText(String.format("%d Star", grad));
            mContext.changeLevelState(level,grad);
        }else
        {
            Resources res = getContext().getResources();
            score_back.setImageBitmap(BitmapFactory.decodeResource(res,res.getIdentifier("score_back_failure", "drawable", "com.left.drawingboard")));
            star.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.no_star));
            result_txt.setText(String.format("%d Star", 0));
            mContext.changeLevelState(level,0);
        }




    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSketchView.setOnDrawChangedListener(this);

        stroke.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSketchView.getMode() == SketchView.STROKE) {
                    showPopup(v, SketchView.STROKE);
                } else {
                    mSketchView.setMode(SketchView.STROKE);
                    setAlpha(eraser, 0.4f);
                    setAlpha(stroke, 1f);
                }
            }
        });
        // ?????????????????????????????????????????????
        setAlpha(eraser, 0.4f);
        eraser.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSketchView.getMode() == SketchView.ERASER) {
                    showPopup(v, SketchView.ERASER);
                } else {
                    mSketchView.setMode(SketchView.ERASER);
                    setAlpha(stroke, 0.4f);
                    setAlpha(eraser, 1f);
                }
            }
        });

        undo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSketchView.undo();
            }
        });

        redo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSketchView.redo();
            }
        });
        // ????????????
        erase.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(getActivity()).content("?????????????????????").positiveText("??????")
                        .negativeText("??????").callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                mSketchView.erase();
                                // ?????????ivPainted???ivOriginal??????
                                ivPainted.setImageBitmap(null);
                                ivOriginal.setImageBitmap(null);
                            }
                }).build().show();
            }
        });
        suibmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(getActivity()).content("???????????????").positiveText("??????")
                        .negativeText("??????").callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {

                        mSketchView.ReshowPath();

                    }
                }).build().show();
            }
        });


//        // ????????????
//        sketchSave.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mSketchView.getPaths().size() == 0) {
//                    Toast.makeText(getActivity(), "??????????????????", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                //??????
//                new MaterialDialog.Builder(getActivity()).title("??????").negativeText("??????").inputType(InputType
//                        .TYPE_CLASS_TEXT).input("????????????(.png)", "a.png", new MaterialDialog.InputCallback() {
//                            @Override
//                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
//                                if (input == null || input.length() == 0) {
//                                    Toast.makeText(getActivity(), "?????????????????????", Toast.LENGTH_SHORT).show();
//                                } else if (input.length() <= 4 || !input.subSequence(input.length() - 4, input.length()).toString().equals(".png")) {
//                                    Toast.makeText(getActivity(), "??????????????????????????????(.png)", Toast.LENGTH_SHORT).show();
//                                } else
//                                    save(input.toString());
//                            }
//                        }).show();
//            }
//        });
//        sketchPhoto.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //????????????
//                ImageConfig imageConfig = new ImageConfig.Builder(new ImageLoader() {
//                    @Override
//                    public void displayImage(Context context, String path, ImageView imageView) {
//                        Glide.with(context).load(path).placeholder(com.yancy.imageselector.R.mipmap.imageselector_photo).centerCrop().into(imageView);
//                    }
//                }).steepToolBarColor(getResources().getColor(R.color.colorPrimary)).titleBgColor(getResources().getColor(R.color.colorPrimary))
//                        .titleSubmitTextColor(getResources().getColor(R.color.white)).titleTextColor(getResources().getColor(R.color.white))
//                        // ????????????   ?????????????????????
//                        .singleSelect()
//                        // ?????????????????? ??????????????????
//                        .showCamera()
//                        // ??????????????????????????????????????? /temp/picture??? ?????????????????????
//                        .filePath("/DrawingBoard/Pictures")
//                        .build();
//                // ?????????????????????
//                ImageSelector.open(getActivity(), imageConfig);
//            }
//        });

        // Inflate????????????
        LayoutInflater inflaterStroke = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        popupStrokeLayout = inflaterStroke.inflate(R.layout.popup_sketch_stroke, null);
        LayoutInflater inflaterEraser = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        popupEraserLayout = inflaterEraser.inflate(R.layout.popup_sketch_eraser, null);
        LayoutInflater inflatershowresult = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        result=inflaterEraser.inflate(R.layout.score, null);
        //result_img=result.findViewById(R.id.Result_Image);
        score_back=result.findViewById(R.id.score_back);
        result_txt=result.findViewById(R.id.Result_Text);
        next=result.findViewById(R.id.next_level);
        again=result.findViewById(R.id.try_again);
        star=result.findViewById(R.id.Star);
        next.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(popup_score!=null)
                    popup_score.dismiss();
                mContext.changeLevelState(level,grad);
                mContext.getSupportFragmentManager().beginTransaction().remove(mContext.fragment).commit();

            }
        });
        again.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(popup_score!=null)
                popup_score.dismiss();
                mSketchView.erase();
                // ?????????ivPainted???ivOriginal??????
                ivPainted.setImageBitmap(null);
                ivOriginal.setImageBitmap(null);
            }
        });
        // ?????????stroke???eraser????????????????????????????????????
        strokeImageView = popupStrokeLayout.findViewById(R.id.stroke_circle);
        eraserImageView = popupEraserLayout.findViewById(R.id.stroke_circle);

        final Drawable circleDrawable = getResources().getDrawable(R.drawable.circle);
        size = circleDrawable.getIntrinsicWidth();

        setSeekBarProgress(SketchView.DEFAULT_STROKE_SIZE, SketchView.STROKE);
        setSeekBarProgress(SketchView.DEFAULT_ERASER_SIZE, SketchView.ERASER);

        // stroke color picker?????????
        mColorPicker = popupStrokeLayout.findViewById(R.id.stroke_color_picker);
        mColorPicker.addSVBar((SVBar) popupStrokeLayout.findViewById(R.id.sv_bar));
        mColorPicker.addOpacityBar((OpacityBar) popupStrokeLayout.findViewById(R.id.opacity_bar));

        mColorPicker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                mSketchView.setStrokeColor(color);
            }
        });
        mColorPicker.setColor(mSketchView.getStrokeColor());
        mColorPicker.setOldCenterColor(mSketchView.getStrokeColor());
    }

    void setAlpha(View v, float alpha) {
        v.setAlpha(alpha);
    }

//    public void save(final String imgName) {
//        dialog = new MaterialDialog.Builder(getActivity()).title("????????????").content("?????????...").progress(true, 0).progressIndeterminateStyle(true).show();
//        bitmap = mSketchView.getBitmap();
//
//        new AsyncTask() {
//
//            @Override
//            protected Object doInBackground(Object[] params) {
//
//                if (bitmap != null) {
//                    try {
//                        String filePath = "/mnt/sdcard/DrawingBoard/";
//                        File dir = new File(filePath);
//                        if (!dir.exists()) {
//                            dir.mkdirs();
//                        }
//                        File f = new File(filePath, imgName);
//                        if (!f.exists()) {
//                            f.createNewFile();
//                        }
//                        FileOutputStream out = new FileOutputStream(f);
//                        bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
//                        out.close();
//
//                        dialog.dismiss();
//                        return "??????????????????" + filePath + imgName;
//                    } catch (Exception e) {
//
//                        dialog.dismiss();
//                        return "??????????????????" + e.getMessage();
//                    }
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Object o) {
//                super.onPostExecute(o);
//                Toast.makeText(getActivity(), (String) o, Toast.LENGTH_SHORT).show();
//
//            }
//        }.execute("");
//    }

    // ?????????????????????
    private void showPopup(View anchor, final int eraserOrStroke) {

        boolean isErasing = eraserOrStroke == SketchView.ERASER;

        oldColor = mColorPicker.getColor();

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // ?????????????????????
        PopupWindow popup = new PopupWindow(getActivity());
        popup.setContentView(isErasing ? popupEraserLayout : popupStrokeLayout);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (mColorPicker.getColor() != oldColor)
                    mColorPicker.setOldCenterColor(oldColor);
            }
        });

        // ??????????????????????????????
        popup.setBackgroundDrawable(new BitmapDrawable());

        popup.showAsDropDown(anchor);

        // seekbar?????????
        SeekBar mSeekBar;
        mSeekBar = (SeekBar) (isErasing ? popupEraserLayout
                .findViewById(R.id.stroke_seekbar) : popupStrokeLayout
                .findViewById(R.id.stroke_seekbar));
        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                setSeekBarProgress(progress, eraserOrStroke);
            }
        });
        int progress = isErasing ? seekBarEraserProgress : seekBarStrokeProgress;
        mSeekBar.setProgress(progress);
    }

    protected void setSeekBarProgress(int progress, int eraserOrStroke) {
        int calcProgress = progress > 1 ? progress : 1;

        int newSize = Math.round((size / 100f) * calcProgress);
        int offset = Math.round((size - newSize) / 2);

        LayoutParams lp = new LayoutParams(newSize, newSize);
        lp.setMargins(offset, offset, offset, offset);
        if (eraserOrStroke == SketchView.STROKE) {
            strokeImageView.setLayoutParams(lp);
            seekBarStrokeProgress = progress;
        } else {
            eraserImageView.setLayoutParams(lp);
            seekBarEraserProgress = progress;
        }
        mSketchView.setSize(newSize, eraserOrStroke);
    }

    // ??????redo???undo???????????????
    @Override
    public void onDrawChanged() {
        // Undo
        if (mSketchView.getPaths().size() > 0)
            setAlpha(undo, 1f);
        else
            setAlpha(undo, 0.4f);
        // Redo
        if (mSketchView.getUndoneCount() > 0)
            setAlpha(redo, 1f);
        else
            setAlpha(redo, 0.4f);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ImageSelector.IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            // ??????Image Path List
            List<String> pathList = data.getStringArrayListExtra(ImageSelectorActivity.EXTRA_RESULT);
            for (String path : pathList) {
                Glide.with(this).load(path).asBitmap().centerCrop().into(new SimpleTarget<Bitmap>() {

                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                initBitmap(resource);
                            }
                        });
            }
        }
    }

    // ??????????????????????????????????????????
    private void initBitmap(Bitmap bitmap) {

        float scaleRatio = 1;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float screenRatio = 1.0f;
        float imgRatio = (float) height / (float) width;
        if (imgRatio >= screenRatio) {
            // ?????????????????????????????????
            scaleRatio = (float) mScreenHeight / (float) height;
        }

        if (imgRatio < screenRatio) {
            scaleRatio = (float) mScreenWidth / (float) width;
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scaleRatio, scaleRatio);
        dstBmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

        GPUImage gpuImage = new GPUImage(getActivity());
        // ?????????????????????filter
        gpuImage.setFilter(new GPUImageSketchFilter());
        grayBmp = gpuImage.getBitmapWithFilterApplied(dstBmp);

        // ?????????????????????????????????????????????
        mSketchView.getBackground().setAlpha(150);
        ivPainted.setImageBitmap(grayBmp);
        ivOriginal.setImageBitmap(dstBmp);
        // ?????????????????????????????????
        ObjectAnimator alpha = ObjectAnimator.ofFloat(ivOriginal, "alpha", 1.0f, 0.0f);
        alpha.setDuration(2000).start();
        // ??????bitmap???grayBmp
        mSketchView.setBitmap(grayBmp);
    }

    // ??????????????????????????????
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.show_original:
                ObjectAnimator alpha = ObjectAnimator.ofFloat(ivOriginal, "alpha", 0.0f, 1.0f);
                alpha.setDuration(1000).start();
                mSketchView.setBitmap(dstBmp);
                return true;
            case R.id.show_painted:
                ObjectAnimator alpha2 = ObjectAnimator.ofFloat(ivOriginal, "alpha", 1.0f, 0.0f);
                alpha2.setDuration(1000).start();
                mSketchView.setBitmap(grayBmp);
                return true;
        }
        return true;
    }
}