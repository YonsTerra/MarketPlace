package com.diegodev.marketplace.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.diegodev.marketplace.R;

import java.util.List;

public class ImagenCarouselAdapter extends RecyclerView.Adapter<ImagenCarouselAdapter.ViewHolder> {
    private final Context context;
    private final List<String> imageUrls;
    public ImagenCarouselAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_carousel_imagen, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String url = imageUrls.get(position);
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.agregar_img)
                .error(R.drawable.error_image)
                .centerCrop()
                .into(holder.imageView);
    }
    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_carousel_imagen);
        }
    }
}
