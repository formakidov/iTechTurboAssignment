package com.formakidov.itechturvotestproject;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class CardsAdapter extends BaseAdapter<CardData, BindableViewHolder<CardData>> {

    public CardsAdapter(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(BindableViewHolder<CardData> viewHolder, int position) {
        CardViewHolder holder = (CardViewHolder) viewHolder;
        CardData data = this.data.get(position);
        holder.bind(data);
    }

    @Override
    public BindableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = layoutInflater.inflate(R.layout.card_item, parent, false);
        return new CardViewHolder(view);
    }

}
