/*
 * Author: Dou Walker
 * Time: 2021/8/22
 */
package com.left.drawingboard.adapters;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.left.drawingboard.LevelActivity;
import com.left.drawingboard.R;
import com.left.drawingboard.beans.LevelInfoBean;
import com.left.drawingboard.fragment.SketchFragment;

import java.util.List;
import java.util.Random;

public class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.MyViewHolder> {

    private Context mContext;
    private List<LevelInfoBean> mlevelInfos;
    private FragmentManager mManager;
    public SketchFragment fragment;
    public LevelActivity mActivity;
    private final static String FRAGMENT_TAG = "SketchFragmentTag";
    public LevelAdapter(Context context, List<LevelInfoBean> levelInfos,LevelActivity activity) {
        mContext = context;
        mlevelInfos = levelInfos;
        mActivity=activity;
        mManager=activity.getSupportFragmentManager();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.level_item, null);
        return new MyViewHolder(view);
    }



    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        LevelInfoBean levelinfo = mlevelInfos.get(position);
        holder.level = levelinfo.getLevelNo();

        holder.relativeLayout.setPadding(levelinfo.getBias(), 0, 0, 0 );
        holder.levelNo.setText(String.valueOf(levelinfo.getLevelNo()));
        holder.grade.setImageResource(levelinfo.getMgrade());
        holder.imageButton.setBackgroundResource(levelinfo.getButtonImage());
    }


    @Override
    public int getItemCount() {
        return mlevelInfos.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageButton imageButton;
        ImageView grade;
        TextView levelNo;
        RelativeLayout relativeLayout;
        int level;
        public MyViewHolder(View view) {
            super(view);
            relativeLayout = view.findViewById(R.id.level_relative_layout);
            imageButton = view.findViewById(R.id.item_button);
            grade = view.findViewById(R.id.star_grade);
            levelNo = view.findViewById(R.id.level_no);
            String text=levelNo.getText().toString();
            level=Integer.valueOf(text).intValue();
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v2) {
                    FragmentTransaction ft = mManager.beginTransaction();
                    //fragment
                    fragment=new SketchFragment(level,mActivity);
                    mActivity.fragment=fragment;
                    ft.add(R.id.level,fragment ,FRAGMENT_TAG).commit();
                    //model controller
//        mController = new ModelController(this,(Application)this.getApplicationContext());
                    //与mainActivity绑定
//        mController.attach(this);
                }
            });
        }
    }
}