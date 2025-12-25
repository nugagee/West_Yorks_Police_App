package com.example.uob_23057989_wypf_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uob_23057989_wypf_app.R;

import java.util.List;

import data.Crime;

public class CrimeAdapter extends RecyclerView.Adapter<CrimeAdapter.ViewHolder> {

    public void setData(List<Crime> crimes) {
    }

    public interface OnCrimeClick {
        void onCrimeSelected(Crime crime);
    }

    private List<Crime> crimes;
    private OnCrimeClick listener;


    public CrimeAdapter(List<Crime> crimes, OnCrimeClick listener){
        this.crimes = crimes;
        this.listener = listener;
    }

    public void setCrimes(List<Crime> newCrimes){
        this.crimes = newCrimes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CrimeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_crime, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CrimeAdapter.ViewHolder holder, int position) {
        Crime c = crimes.get(position);
        holder.title.setText(c.crime_type);
        holder.location.setText(c.location_desc);
        holder.itemView.setOnClickListener(v -> listener.onCrimeSelected(c));
//        holder.txtMonth.setText(crime.getMonth());
    }

    @Override
    public int getItemCount() {
        return crimes == null ? 0 : crimes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, location;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txtCrimeType);
            location = itemView.findViewById(R.id.txtLocation);
//            txtMonth = itemView.findViewById(R.id.txtMonth);
        }
    }
}

