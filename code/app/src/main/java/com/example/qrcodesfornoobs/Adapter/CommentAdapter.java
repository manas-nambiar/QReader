package com.example.qrcodesfornoobs.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.qrcodesfornoobs.R;
import java.util.ArrayList;

/**
 * Adapter class for the QR code's comment recycler view
 * Binds the QR code's comment text to the corresponding view holder.
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyHolder>{
    Context context;
    ArrayList<String> comments;
    LayoutInflater layoutInflater;

    /**
     * Constructor that takes in the current context and list of creature codes.
     * @param context The current context.
     * @param comments The list of QRCode comments.
     */
    public CommentAdapter(Context context, ArrayList<String> comments) {
        this.context = context;
        this.comments = comments;
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
        View view = layoutInflater.inflate(R.layout.qrcode_comment_content, parent, false);
        return new MyHolder(view);
    }

    /**
     * This method updates the contents of the ViewHolder to reflect the item at the given position.
     * It sets the comments for the creature using data from the Creature object at the given position.
     *
     * @param holder   The MyHolder object to update.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.MyHolder holder, int position) {
        holder.commentText.setText(comments.get(position));

    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return comments.size();
    }


    /**
     * View holder class that contains the view objects of a single recycler view row.
     */
    public class MyHolder extends RecyclerView.ViewHolder {
        TextView commentText;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            commentText = itemView.findViewById(R.id.comment_txt);
        }
    }
}
