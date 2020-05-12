package kylec.hj.g2048.adapter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import kylec.hj.g2048.R;
import kylec.hj.g2048.app.Constant;
import kylec.hj.g2048.db.GameDatabaseHelper;
import kylec.hj.g2048.db.Gamer;
import kylec.hj.g2048.view.CommonDialog;

public class ChartsAdapter extends RecyclerView.Adapter<ChartsAdapter.ViewHolder> {

    private List<Gamer> mGamer;

    public ChartsAdapter(List<Gamer> Gamer) {
        mGamer = Gamer;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView index;
        TextView name;
        TextView score;
        TextView time;
        public View chartsView;

        public ViewHolder(@NonNull View v) {
            super(v);
            chartsView = v;
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
        holder.chartsView.setOnClickListener(view1 -> {
            CommonDialog dialog = new CommonDialog(parent.getContext(), R.style.CustomDialog);
            dialog.setCancelable(false);
            dialog.setTitle("删除该条记录")
                    .setMessage("")
                    .setOnNegativeClickListener("删除",
                            v -> {
                                int position = holder.getAdapterPosition();
                                deleteData(parent.getContext(),position);
                                dialog.cancel();
                            })
                    .setOnPositiveClickedListener("取消", v -> dialog.cancel())
                    .show();
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Gamer gamer = mGamer.get(position);
        holder.index.setText(String.valueOf(position+1));
        holder.score.setText(String.valueOf(gamer.getScore()));
        holder.name.setText(gamer.getName());
        holder.time.setText(gamer.getTime());

    }

    @Override
    public int getItemCount() {
        return mGamer.size();
    }

    /**
     * 删除排行榜记录
     * @param context
     * @param position
     */
    public void deleteData(Context context,int position){
/*        try {
           Context friendContext = context.createPackageContext("com.hj.datafor2048"
                    , Context.CONTEXT_IGNORE_SECURITY);*/
            GameDatabaseHelper helper = new GameDatabaseHelper(context, Constant.DB_NAME,null,1);
            SQLiteDatabase db = helper.getWritableDatabase();
            db.delete("info","id=?",new String[]{String.valueOf(mGamer.get(position).getId())});
            db.close();
            mGamer.remove(position);
            notifyDataSetChanged();
/*        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }*/
    }
}
