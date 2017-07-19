package com.formakidov.itechturvotestproject;

import android.view.View;
import android.widget.TextView;

import butterknife.BindView;

public class CardViewHolder extends BindableViewHolder<CardData> {
    @BindView(R.id.title)
    TextView title;

    public CardViewHolder(View view) {
        super(view);
    }

    @Override
    public void bind(CardData data) {
        title.setText(data.getTitle());
    }
}
