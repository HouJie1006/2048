package kylec.hj.g2048.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import kylec.hj.g2048.R;
import kylec.hj.g2048.db.Gamer;

public class ChartsAdapter extends RecyclerView.Adapter<ChartsAdapter.ViewHolder> {

    private List<Gamer> mGamer;

    public ChartsAdapter(List<Gamer> Gamer) {
        mGamer = Gamer;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView index;
        TextView name;
        TextView score;
        TextView time;

        public ViewHolder(@NonNull View v) {
            super(v);
            index = v.findViewById(R.id.gamer_index);
            name = v.findViewById(R.id.gamer_name);
            score = v.findViewById(R.id.gamer_score);
            time = v.findViewById(R.id.gamer_time);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.charts_items,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Gamer gamer = mGamer.get(position);
        holder.index.setText(String.valueOf(gamer.getId()));
        holder.score.setText(String.valueOf(gamer.getScore()));
        holder.name.setText(gamer.getName());
        holder.time.setText(gamer.getTime());

    }

    @Override
    public int getItemCount() {
        return mGamer.size();
    }
}
