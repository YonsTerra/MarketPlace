package com.diegodev.marketplace.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.diegodev.marketplace.R;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
    public interface OnConversationClickListener { void onClick(String contactId); }
    private final Context context;
    private final List<Item> items;
    private OnConversationClickListener listener;
    public static class Item {
        public final String contactId;
        public final String contactName;
        public final String lastMessage;
        public final long lastTimestamp;
        public final String contactAvatarUrl;
        public Item(String contactId, String contactName, String lastMessage, long lastTimestamp, String contactAvatarUrl) {
            this.contactId = contactId;
            this.contactName = contactName;
            this.lastMessage = lastMessage;
            this.lastTimestamp = lastTimestamp;
            this.contactAvatarUrl = contactAvatarUrl;
        }
    }
    public ConversationAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
    }
    public void setOnConversationClickListener(OnConversationClickListener l) { this.listener = l; }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_conversacion, parent, false);
        return new ViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item it = items.get(position);
        holder.tvNombre.setText(it.contactName != null ? it.contactName : "Usuario");
        holder.tvUltimoMensaje.setText(it.lastMessage != null ? it.lastMessage : "");
        Glide.with(context)
                .load(it.contactAvatarUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .circleCrop()
                .into(holder.ivAvatar);
        holder.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(it.contactId); });
    }
    @Override
    public int getItemCount() { return items.size(); }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvUltimoMensaje;
        ImageView ivAvatar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_conv_nombre);
            tvUltimoMensaje = itemView.findViewById(R.id.tv_conv_ultimo);
            ivAvatar = itemView.findViewById(R.id.iv_conv_avatar);
        }
    }
}
