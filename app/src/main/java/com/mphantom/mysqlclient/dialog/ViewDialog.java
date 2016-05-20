package com.mphantom.mysqlclient.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mphantom.mysqlclient.App;
import com.mphantom.mysqlclient.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by wushaorong on 16-5-12.
 */
public class ViewDialog extends Dialog implements View.OnClickListener {
    @Bind(R.id.edit_ViewDialog_name)
    EditText edit_name;
    @Bind(R.id.edit_ViewDialog_viewinfo)
    EditText edit_functioninfo;
    @Bind(R.id.btn_ViewDialog_confirm)
    Button btn_confirm;


    public ViewDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_view);
        ButterKnife.bind(this);
        btn_confirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String name = edit_name.getText().toString().trim();
        String viewInfo = edit_functioninfo.getText().toString().trim();
        Observable.create((Observable.OnSubscribe<Integer>) onSubscribe -> onSubscribe.onNext(1))
                .subscribeOn(Schedulers.io())
                .doOnNext(integer -> App.getInstance().connectionService.createView(name,viewInfo))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer1 -> dismiss(),
                        throwable -> {
                            Toast.makeText(getContext(), R.string.create_View_fails, Toast.LENGTH_SHORT).show();
                        });
    }
}