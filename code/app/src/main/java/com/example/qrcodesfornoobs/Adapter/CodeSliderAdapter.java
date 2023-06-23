package com.example.qrcodesfornoobs.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.qrcodesfornoobs.Models.Creature;
import com.example.qrcodesfornoobs.R;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.ArrayList;

/**
 * Adapter class for displaying QR code images in a slider view.
 */
public class CodeSliderAdapter extends SliderViewAdapter<CodeSliderAdapter.SliderAdapterViewHolder> {

    // URLs to fetch code images from
    private ArrayList<Creature> sliderItems;

    /**
     * Constructor for creating a CodeSliderAdapter instance.
     *
     * @param context the context in which this adapter is being used
     * @param sliderDataArrayList the list of slider data containing URLs of the images to display
     */
    public CodeSliderAdapter(Context context, ArrayList<Creature> sliderDataArrayList) {
        this.sliderItems = sliderDataArrayList;

    }

    /**
     * Inflates the slider view holder layout and returns a new instance of the view holder.
     *
     * @param parent the parent view group
     * @return a new instance of the view holder
     */
    @Override
    public SliderAdapterViewHolder onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_item, parent, false);
        return new SliderAdapterViewHolder(inflate);
    }

    /**
     * Binds the view holder with the data at the given position.
     *
     * @param viewHolder the view holder to bind
     * @param position   the position of the data
     */
    @Override
    public void onBindViewHolder(SliderAdapterViewHolder viewHolder, int position) {

        Creature sliderItem = sliderItems.get(position);

        Glide.with(viewHolder.itemView)
                .load(sliderItem.getPhotoCreatureUrl())
                .fitCenter()
                .into(viewHolder.imageViewBackground);
    }

    /**
     * Returns the number of data items in the adapter.
     *
     * @return the number of data items in the adapter
     */
    @Override
    public int getCount() {
        return sliderItems.size();
    }

    /**
     * View holder class for initializing the views of the slider view.
     */
    static class SliderAdapterViewHolder extends SliderViewAdapter.ViewHolder {
        // Adapter class for initializing
        // the views of our slider view.
        View itemView;
        ImageView imageViewBackground;

        /**
         * Constructor for creating a SliderAdapterViewHolder instance.
         *
         * @param itemView the view item to be held by the view holder
         */
        public SliderAdapterViewHolder(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.slider_item_imageView);
            this.itemView = itemView;
        }
    }
}
