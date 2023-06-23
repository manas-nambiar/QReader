package com.example.qrcodesfornoobs.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrcodesfornoobs.Models.Player;
import com.example.qrcodesfornoobs.R;
import com.example.qrcodesfornoobs.databinding.LeaderboardItemBinding;

import java.util.List;

public class LeaderboardPlayerAdapter extends RecyclerView.Adapter<LeaderboardPlayerAdapter.ViewHolder> {
    Context context;
    List<Player> playerList;

    public LeaderboardPlayerAdapter(Context context, List<Player> playerList) {
        this.context = context;
        this.playerList = playerList;
    }

    @NonNull
    @Override
    public LeaderboardPlayerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.leaderboard_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardPlayerAdapter.ViewHolder holder, int position) {
        if (playerList == null || playerList.size() == 0) {
            return;
        }
        // title row
        if (position == 0) {
            holder.rankTextView.setText("Rank");
            holder.playerTextView.setText("Player");
            holder.scoreTextView.setText("Score");
            return;
        }
        Player player = playerList.get(position);
        holder.rankTextView.setText(String.valueOf(position + 1));
        holder.playerTextView.setText(player.getUsername());
        holder.scoreTextView.setText(String.valueOf(player.getScore()));
    }

    @Override
    public int getItemCount() {
        return playerList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView rankTextView, playerTextView, scoreTextView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            rankTextView = itemView.findViewById(R.id.rank_textView);
            playerTextView = itemView.findViewById(R.id.player_textView);
            scoreTextView = itemView.findViewById(R.id.score_textView);

        }
    }
}
