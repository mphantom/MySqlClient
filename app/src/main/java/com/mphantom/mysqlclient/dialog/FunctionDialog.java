package com.mphantom.mysqlclient.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mphantom.mysqlclient.App;
import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.model.Trigger;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by wushaorong on 16-5-13.
 */
public class FunctionDialog extends Dialog implements View.OnClickListener {
    @Bind(R.id.edit_FunctionDialog_name)
    EditText edit_name;
    @Bind(R.id.edit_Functiondialog_time)
    EditText edit_time;
    @Bind(R.id.edit_FunctionDialog_event)
    EditText edit_event;
    @Bind(R.id.edit_FunctionDialog_table)
    EditText edit_table;
    @Bind(R.id.edit_FunctionDialog_statement)
    EditText edit_statement;
    @Bind(R.id.btn_FunctionDialog_confirm)
    Button btn_confirm;

    private Trigger trigger;

    public FunctionDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_function);
        ButterKnife.bind(this);
        btn_confirm.setOnClickListener(this);
        if (trigger != null) {
            edit_name.setText(trigger.getTrigger());
            edit_time.setText(trigger.getTiming());
            edit_event.setText(trigger.getEvent());
            edit_table.setText(trigger.getTable());
            edit_statement.setText(trigger.getStatement());
        }
        edit_statement.setText("BEGIN\n" +
                "     insert into tab2(tab2_id) values(new.tab1_id);\n" +
                "END;");
    }

    @Override
    public void onClick(View v) {
        Trigger info = new Trigger();
        info.setTrigger(edit_name.getText().toString().trim());
        info.setTiming(edit_time.getText().toString().trim());
        info.setEvent(edit_event.getText().toString().trim());
        info.setTable(edit_table.getText().toString());
        info.setStatement(edit_statement.getText().toString());
        Observable.create((Observable.OnSubscribe<Integer>) onSubscribe -> onSubscribe.onNext(1))
                .subscribeOn(Schedulers.io())
                .doOnNext(integer -> App.getInstance().connectionService.createTrigger(info))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer1 -> dismiss());
    }

    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }
}
