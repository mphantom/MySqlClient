package com.mphantom.mysqlclient.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
    AutoCompleteTextView edit_time;
    @Bind(R.id.edit_FunctionDialog_event)
    AutoCompleteTextView edit_event;
    @Bind(R.id.edit_FunctionDialog_table)
    EditText edit_table;
    @Bind(R.id.edit_FunctionDialog_statement)
    EditText edit_statement;
    @Bind(R.id.btn_FunctionDialog_confirm)
    Button btn_confirm;

    private Trigger trigger;
    private String[] time = new String[]{"AFTER", "BEFORE"};
    private String[] event = new String[]{"INSERT", "UPDATE", "DELETE"};

    public FunctionDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_function);
        ButterKnife.bind(this);
        btn_confirm.setOnClickListener(this);
        edit_time.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, time));
        edit_event.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, event));
        edit_time.setThreshold(1);
        edit_event.setThreshold(1);
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
                .subscribe(integer1 -> dismiss(),
                        throwable -> {
                            Toast.makeText(getContext(), R.string.create_trigger_fails, Toast.LENGTH_SHORT).show();
                        });
    }

    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }
}
