package com.wallet.league.leaguewallet;

import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by irteza.arif on 2017-04-03.
 */

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CardViewHolder> {

    ArrayList<Card> cards;

    public CustomAdapter(@Nullable final ArrayList<Card> objects) {
        cards = objects != null ? objects : new ArrayList<Card>();
    }
    public CustomAdapter() {
        this(null);
    }

    public void addItem(final int position, final Card object) {
        cards.add(position, object);
        notifyItemInserted(position);
    }

    public void addItem(final Card object) {
        addItem(cards.size(), object);
    }

    public void clear() {
        final int size = getItemCount();
        cards.clear();
        notifyItemRangeRemoved(0, size);
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_wallet, parent, false);
        CardViewHolder cvh = new CardViewHolder(v);
        return cvh;
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        holder.account.setText(formatAccountName(cards.get(position).type));
        holder.amount.setText(cards.get(position).currency+" "+cards.get(position).amount);
        holder.duration.setText("Effective: "+formatDate(cards.get(position).startDate)+" - "+formatDate(cards.get(position).endDate));
    }

    public String formatDate(String date) {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-mm-dd'T'hh':00:00Z'");
        SimpleDateFormat format2 = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
        Date unformattedDate;
        try {
            unformattedDate=format1.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return format2.format(unformattedDate);
    }

    public String formatAccountName(String account) {
        account = account.replaceAll("_"," ");
        account = account.toUpperCase();
        return account;
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView account;
        TextView amount;
        TextView duration;

        CardViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.item_card_view);
            account = (TextView)itemView.findViewById(R.id.account_name);
            amount = (TextView)itemView.findViewById(R.id.amount);
            duration = (TextView)itemView.findViewById(R.id.policy_duration);
        }
    }
}
