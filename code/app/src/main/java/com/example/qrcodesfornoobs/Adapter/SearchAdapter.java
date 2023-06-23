package com.example.qrcodesfornoobs.Adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.qrcodesfornoobs.Models.Creature;
import com.example.qrcodesfornoobs.R;

import java.util.ArrayList;

/**
 * A custom RecyclerView adapter to display a list of search results.
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyHolder>{

    Context context;
    ArrayList<?> codes;
    LayoutInflater layoutInflater;
    private RecyclerViewInterface rvListener;
    private String collection;


    /**
     * An interface to handle click events on the list items.
     */
    public interface RecyclerViewInterface {
        void onItemClick(int pos);
    }

    /**
     * Creates a new instance of the SearchAdapter.
     *
     * @param context The context in which the adapter is being used.
     * @param codes The list of search results to be displayed.
     * @param rvListener An interface to handle click events on the list items.
     * @param collection To tell whether displaying a player or a creature.
     */
    public SearchAdapter(Context context, ArrayList<?> codes, RecyclerViewInterface rvListener, String collection) {
        this.context = context;
        this.codes = codes;
        this.rvListener = rvListener;
        this.collection = collection;
        layoutInflater = layoutInflater.from(context);
    }

    /**
     * Called when the RecyclerView needs a new ViewHolder of the given type to represent an item.
     *
     * @param parent The ViewGroup into which the new View will be added.
     * @param viewType The type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.search_item, parent, false);
        return new MyHolder(view);
    }

    /**
     * Called by the RecyclerView to display the data at the specified position.
     * Either shows list of usernames or creatures based on nearby geolocation.
     *
     * @param holder The ViewHolder that holds a View of the given view type.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        if (collection == "Players"){
            holder.name.setText(codes.get(position).toString());
        }

        else if(collection == "Creatures") {
            Creature creature = (Creature) codes.get(position);
            holder.name.setText(creature.getName());
            RequestOptions options = new RequestOptions().circleCrop().placeholder(R.drawable.face_icon);
            Glide.with(context).load(creature.getPhotoCreatureUrl()).apply(options).into(holder.creatureImage);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = holder.getAdapterPosition();
                rvListener.onItemClick(pos);
            }
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in the data set held by the adapter.
     */
    @Override
    public int getItemCount() {
        return codes.size();
    }

    /**
     * Sets view to a search result's name and image.
     */
    public class MyHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView creatureImage;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txt);
            creatureImage = itemView.findViewById(R.id.profile_creature_img);
        }
    }
}