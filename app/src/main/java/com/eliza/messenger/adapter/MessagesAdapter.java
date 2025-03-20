package com.eliza.messenger.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.eliza.messenger.R;
import com.eliza.messenger.databinding.ItemMessageReceivedBinding;
import com.eliza.messenger.databinding.ItemMessageSentBinding;
import com.eliza.messenger.model.Message;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_IMAGE_SENT = 3;
    private static final int VIEW_TYPE_IMAGE_RECEIVED = 4;
    
    private final Context context;
    private final List<Message> messages;
    private final String currentUserId;
    private final SimpleDateFormat timeFormat;
    private final SimpleDateFormat dateFormat;
    
    public MessagesAdapter(Context context, List<Message> messages, String currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            ItemMessageSentBinding binding = ItemMessageSentBinding.inflate(
                    LayoutInflater.from(context), parent, false);
            return new SentMessageHolder(binding);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            ItemMessageReceivedBinding binding = ItemMessageReceivedBinding.inflate(
                    LayoutInflater.from(context), parent, false);
            return new ReceivedMessageHolder(binding);
        } else if (viewType == VIEW_TYPE_IMAGE_SENT) {
            // For now, using same layout for text and images
            ItemMessageSentBinding binding = ItemMessageSentBinding.inflate(
                    LayoutInflater.from(context), parent, false);
            return new SentImageHolder(binding);
        } else if (viewType == VIEW_TYPE_IMAGE_RECEIVED) {
            // For now, using same layout for text and images
            ItemMessageReceivedBinding binding = ItemMessageReceivedBinding.inflate(
                    LayoutInflater.from(context), parent, false);
            return new ReceivedImageHolder(binding);
        }
        
        // Default case
        ItemMessageReceivedBinding binding = ItemMessageReceivedBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new ReceivedMessageHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_IMAGE_SENT:
                ((SentImageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_IMAGE_RECEIVED:
                ((ReceivedImageHolder) holder).bind(message);
                break;
        }
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        
        if (message.getSenderId().equals(currentUserId)) {
            if ("image".equals(message.getType())) {
                return VIEW_TYPE_IMAGE_SENT;
            }
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            if ("image".equals(message.getType())) {
                return VIEW_TYPE_IMAGE_RECEIVED;
            }
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }
    
    // Format the timestamp of a message
    private String formatTime(Timestamp timestamp) {
        if (timestamp == null) return "";
        
        Date messageDate = timestamp.toDate();
        Date now = new Date();
        
        // If message is from today, show just the time
        if (DateUtils.isToday(messageDate.getTime())) {
            return timeFormat.format(messageDate);
        }
        
        // If message is from this year, show month and day
        SimpleDateFormat thisYearFormat = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
        SimpleDateFormat otherYearFormat = new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.getDefault());
        
        if (messageDate.getYear() == now.getYear()) {
            return thisYearFormat.format(messageDate);
        } else {
            return otherYearFormat.format(messageDate);
        }
    }
    
    // ViewHolder for sent text messages
    class SentMessageHolder extends RecyclerView.ViewHolder {
        private final ItemMessageSentBinding binding;
        
        SentMessageHolder(ItemMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        void bind(Message message) {
            binding.messageText.setText(message.getText());
            binding.messageTime.setText(formatTime(message.getTimestamp()));
            
            // Show read status
            binding.messageStatus.setVisibility(message.isRead() ? View.VISIBLE : View.GONE);
        }
    }
    
    // ViewHolder for received text messages
    class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        private final ItemMessageReceivedBinding binding;
        
        ReceivedMessageHolder(ItemMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        void bind(Message message) {
            binding.messageText.setText(message.getText());
            binding.messageTime.setText(formatTime(message.getTimestamp()));
        }
    }
    
    // ViewHolder for sent image messages
    class SentImageHolder extends RecyclerView.ViewHolder {
        private final ItemMessageSentBinding binding;
        
        SentImageHolder(ItemMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        void bind(Message message) {
            if (message.getMediaUrl() != null) {
                binding.messageText.setVisibility(View.GONE);
                binding.messageImage.setVisibility(View.VISIBLE);
                
                Glide.with(context)
                        .load(message.getMediaUrl())
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_error)
                        .into(binding.messageImage);
            } else {
                binding.messageText.setVisibility(View.VISIBLE);
                binding.messageImage.setVisibility(View.GONE);
                binding.messageText.setText(message.getText());
            }
            
            binding.messageTime.setText(formatTime(message.getTimestamp()));
            binding.messageStatus.setVisibility(message.isRead() ? View.VISIBLE : View.GONE);
        }
    }
    
    // ViewHolder for received image messages
    class ReceivedImageHolder extends RecyclerView.ViewHolder {
        private final ItemMessageReceivedBinding binding;
        
        ReceivedImageHolder(ItemMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        void bind(Message message) {
            if (message.getMediaUrl() != null) {
                binding.messageText.setVisibility(View.GONE);
                binding.messageImage.setVisibility(View.VISIBLE);
                
                Glide.with(context)
                        .load(message.getMediaUrl())
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_error)
                        .into(binding.messageImage);
            } else {
                binding.messageText.setVisibility(View.VISIBLE);
                binding.messageImage.setVisibility(View.GONE);
                binding.messageText.setText(message.getText());
            }
            
            binding.messageTime.setText(formatTime(message.getTimestamp()));
        }
    }
}
