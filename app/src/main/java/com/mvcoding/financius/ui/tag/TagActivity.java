/*
 * Copyright (C) 2015 Mantas Varnagiris.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package com.mvcoding.financius.ui.tag;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.EditText;

import com.mvcoding.financius.R;
import com.mvcoding.financius.data.model.Tag;
import com.mvcoding.financius.ui.ActivityComponent;
import com.mvcoding.financius.ui.ActivityStarter;
import com.mvcoding.financius.ui.BaseActivity;
import com.mvcoding.financius.ui.Presenter;
import com.mvcoding.financius.util.rx.Event;

import javax.inject.Inject;

import butterknife.Bind;
import rx.Observable;
import rx.android.view.ViewObservable;
import rx.android.widget.WidgetObservable;
import rx.subjects.PublishSubject;

public class TagActivity extends BaseActivity<TagPresenter.View, TagComponent> implements TagPresenter.View {
    private static final String EXTRA_TAG = "EXTRA_TAG";

    private static final String RESULT_EXTRA_TAG = "RESULT_EXTRA_TAG";

    private final PublishSubject<Integer> colorSubject = PublishSubject.create();

    @Bind(R.id.titleEditText) EditText titleEditText;
    @Bind(R.id.colorButton) Button colorButton;
    @Bind(R.id.saveButton) Button saveButton;

    @Inject TagPresenter presenter;

    public static void startForResult(@NonNull Context context, int requestCode, @NonNull Tag tag) {
        ActivityStarter.with(context, TagActivity.class).extra(EXTRA_TAG, tag).startForResult(requestCode);
    }

    @Override protected int getLayoutId() {
        return R.layout.activity_tag;
    }

    @NonNull @Override protected TagComponent createComponent(@NonNull ActivityComponent component) {
        final Tag tag = getIntent().getParcelableExtra(EXTRA_TAG);
        return component.plus(new TagModule(tag));
    }

    @Override protected void inject(@NonNull TagComponent component) {
        component.inject(this);
    }

    @NonNull @Override protected Presenter<TagPresenter.View> getPresenter() {
        return presenter;
    }

    @NonNull @Override protected TagPresenter.View getPresenterView() {
        return this;
    }

    @NonNull @Override public Observable<String> onTitleChanged() {
        return WidgetObservable.text(titleEditText).compose(textTransformer);
    }

    @NonNull @Override public Observable<Integer> onColorChanged() {
        return colorSubject;
    }

    @NonNull @Override public Observable<Event> onSave() {
        return ViewObservable.clicks(saveButton).compose(clickTransformer);
    }

    @Override public void showTag(@NonNull Tag tag) {
        titleEditText.setText(tag.getTitle());
        colorButton.setText(String.valueOf(tag.getColor()));
    }

    @Override public void startResult(@NonNull Tag tag) {
        final Intent data = new Intent();
        data.putExtra(RESULT_EXTRA_TAG, tag);
        setResult(RESULT_OK, data);
        close();
    }
}
