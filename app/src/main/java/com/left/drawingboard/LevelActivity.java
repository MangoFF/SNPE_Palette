/*
 * Author: Dou Walker
 * Time: 2021/8/22
 */
package com.left.drawingboard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.left.drawingboard.Model.ModelController;
import com.left.drawingboard.adapters.LevelAdapter;
import com.left.drawingboard.beans.LevelInfoBean;
import com.left.drawingboard.fragment.SketchFragment;

import java.util.ArrayList;
import java.util.List;

public class LevelActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private LevelAdapter levelAdapter;
    private List<LevelInfoBean> levels = new ArrayList<>();
    public ModelController mController;
    public SketchFragment fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.level_layout);
        initData();

        recyclerView = findViewById(R.id.level_list);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        recyclerView.setLayoutManager(layoutManager);
        levelAdapter = new LevelAdapter(this, levels,this);
        //*******************MANGO*******************************
       // model controller
        mController = new ModelController(this,getApplication());
        //与mainActivity绑定
        mController.attach(this);
        //*******************MANGO*******************************
        recyclerView.setAdapter(levelAdapter);
        // Test
//        Button btn = findViewById(R.id.test_button);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                changeLevelState(1, 0);
//                levelAdapter.notifyDataSetChanged();
//            }
//        });

    }

    void initData() {
        int cnt = 32;
        for (int i = 0; i < cnt; ++i) {
            levels.add(new LevelInfoBean(i+1, 0, i));
        }
    }

    // API: When the player clear the level, use this API alter the grade.
    public void changeLevelState(int level, int grade) {
        // grade: 0 -> never play or are't finished, 1,2,3 -> the grade of user's painting
        LevelInfoBean t = null;
        for (LevelInfoBean l : levels) {
            if (l.getLevelNo() == level) {
                t = l; break;
            }
        }
        if (t == null) return;

        t.setMgrade(grade);
        levelAdapter.notifyDataSetChanged();
    }
}
