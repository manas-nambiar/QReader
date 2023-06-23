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
 * Adapter class for the profile code recycler view.
 * Binds the creature object's data to the corresponding view holder.
 */
public class ProfileCodeArrayAdapter extends RecyclerView.Adapter<ProfileCodeArrayAdapter.MyHolder>{

    Context context;
    ArrayList<Creature> codes;
    LayoutInflater layoutInflater;

    private RecyclerViewInterface rvListener;


    public interface RecyclerViewInterface {
        void onItemClick(int pos);
    }

    /**
     * Constructor that takes in the current context and list of creature codes.
     * @param context The current context.
     * @param codes The list of creature codes.
     * @param rvListener A listener for the recyclerview
     */
    public ProfileCodeArrayAdapter(Context context, ArrayList<Creature> codes, RecyclerViewInterface rvListener) {
        this.context = context;
        this.codes = codes;
        this.rvListener = rvListener;
        layoutInflater = layoutInflater.from(context);
    }

    /**
     * This method is called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     * It creates and initializes a new MyHolder object by inflating a view from the layout file.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new MyHolder object that holds a view of the given view type.
     */
    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.profile_code_content, parent, false);
        return new MyHolder(view);
    }

    /**
     * This method updates the contents of the ViewHolder to reflect the item at the given position.
     * It sets the creature name, score, number of scans, and image using data from the Creature object at the given position.
     *
     * @param holder   The MyHolder object to update.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        // Set list item info
        Creature creature = codes.get(position);

        RequestOptions options = new RequestOptions().circleCrop().placeholder(R.drawable.face_icon);

        holder.creatureName.setText(creature.getName());
        holder.creatureScore.setText(creature.getScore() + " points");
        Glide.with(context).load(creature.getPhotoCreatureUrl()).apply(options).into(holder.creatureImage);
        if (creature.getNumOfScans() == 1){
            holder.creatureNumOfScans.setText("Scanned by " + creature.getNumOfScans() + " Player");
        } else {
            holder.creatureNumOfScans.setText("Scanned by " + creature.getNumOfScans() + " Players");
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
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return codes.size();
    }

    /**
     * View holder class that contains the view objects of a single recycler view row.
     */
    public class MyHolder extends RecyclerView.ViewHolder {
        TextView creatureName;
        TextView creatureScore;
        TextView creatureNumOfScans;
        ImageView creatureImage;

        /**
         * Constructor that takes in the view object and initializes the view objects of the row.
         * @param itemView The view object of the recycler view row.
         */
        public MyHolder(@NonNull View itemView) {
            // Obtain list item textviews
            super(itemView);
            creatureName = itemView.findViewById(R.id.profile_code_txt);
            creatureScore = itemView.findViewById(R.id.profile_code_points);
            creatureImage = itemView.findViewById(R.id.profile_creature_img);
            creatureNumOfScans = itemView.findViewById(R.id.profile_num_of_scans);
        }
    }
}