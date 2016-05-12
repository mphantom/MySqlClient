package com.mphantom.mysqlclient.widget.activity.table;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.mphantom.mysqlclient.App;
import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.model.TableProperty;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TableColumnActivity extends AppCompatActivity implements View.OnClickListener {
    @Bind(R.id.btn_submit_tableColumnA)
    Button btn_submit;
    @Bind(R.id.layout_contant_tableColumnA)
    LinearLayout layout_contant;
    private List<TableProperty> list;
    private List<EditText> listEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_column);
        ButterKnife.bind(this);
        listEdit = new ArrayList<>();
        setTitle("修改行数据");
        btn_submit.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        list = App.getInstance().tablePropertyList;
        for (int i = 0; i < list.size(); i++) {
            TextInputLayout layout = new TextInputLayout(this);
            EditText editText = new EditText(this);
            editText.setHint(list.get(i).getField());
            listEdit.add(editText);
            layout.addView(editText);
            layout_contant.addView(layout);
        }
    }

    @Override
    public void onClick(View v) {

    }
}
