package com.formakidov.itechturvotestproject;

import android.view.View;

import butterknife.BindView;

public class CardViewHolder extends BindableViewHolder<CardData> {
    @BindView(R.id.wv)
    TurvoWebView webView;

    public CardViewHolder(View view) {
        super(view);
    }

    @Override
    public void bind(CardData data) {
        webView.loadUrl(data.getUrl());
    }
}
