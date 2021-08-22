/*
 * Author: Dou Walker
 * Time: 2021/8/22
 */
package com.left.drawingboard.adapters;

import android.content.Context;
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

import com.left.drawingboard.R;
import com.left.drawingboard.beans.LevelInfoBean;

import java.util.List;
import java.util.Random;

public class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.MyViewHolder> {

    private Context mContext;
    private List<LevelInfoBean> mlevelInfos;

    public LevelAdapter(Context context, List<LevelInfoBean> levelInfos) {
        mContext = context;
        mlevelInfos = levelInfos;
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

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v2) {
                    Toast.makeText(v2.getContext(), "Click on item", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}