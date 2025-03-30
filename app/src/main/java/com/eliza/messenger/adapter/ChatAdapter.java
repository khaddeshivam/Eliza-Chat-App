package com.eliza.messenger.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.eliza.messenger.R;
import com.eliza.messenger.model.Chat;
import com.eliza.messenger.model.User;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private static final String TAG = "ChatAdapter";
    private final List<Chat> chatList;
    private final Context context;
    private final String currentUserId;
    private final FirebaseFirestore db;
    private final SimpleDateFormat timeFormat;
    private final SimpleDateFormat dateFormat;
    private OnChatClickListener onChatClickListener;

    public ChatAdapter(Context context) {
        this.context = context;
        this.chatList = new ArrayList<>();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.currentUserId = currentUser != null ? currentUser.getUid() : "";
        this.db = FirebaseFirestore.getInstance();
        this.timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        this.dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
    }

    public void setOnChatClickListener(OnChatClickListener listener) {
        this.onChatClickListener = listener;
    }

    public void updateData(List<Chat> newChats) {
        this.chatList.clear();
        if (newChats != null) {
            this.chatList.addAll(newChats);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        
        // Format the time
        String formattedTime = "";
        if (chat.getLastMessageTimestamp() != null) {
            Date messageDate = chat.getLastMessageTimestamp().toDate();
            Date now = new Date();
            
            // If the message is from today, show time only
            if (isSameDay(messageDate, now)) {
                formattedTime = timeFormat.format(messageDate);
            } else {
                formattedTime = dateFormat.format(messageDate);
            }
        }
        
        // Show unread count indicator if needed
        if (chat.getUnreadCount() > 0) {
            holder.unreadIndicator.setVisibility(View.VISIBLE);
            holder.unreadIndicator.setText(String.valueOf(chat.getUnreadCount()));
        } else {
            holder.unreadIndicator.setVisibility(View.GONE);
        }
        
        // Set last message
        holder.lastMessage.setText(chat.getLastMessage() != null ? chat.getLastMessage() : "");
        
        // Set message time
        holder.messageTime.setText(formattedTime);
        
        // Handle group vs one-on-one chat
        if (chat.isGroup()) {
            // Group chat
            holder.contactName.setText(chat.getGroupName());
            
            if (chat.getGroupIcon() != null && !chat.getGroupIcon().isEmpty()) {
                loadProfileImage(chat.getGroupIcon(), holder.avatarImage);
            } else {
                holder.avatarImage.setImageResource(R.drawable.ic_group_placeholder);
            }
        } else {
            // One-on-one chat - we need to get the other participant
            loadOtherParticipant(chat, holder);
        }
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (onChatClickListener != null) {
                onChatClickListener.onChatClick(chat);
            }
        });
    }
    
    private void loadOtherParticipant(Chat chat, ChatViewHolder holder) {
        if (chat.getParticipants() == null || chat.getParticipants().size() < 2) {
            return;
        }
        
        // Find the other participant's ID (not the current user)
        String otherUserId = null;
        for (String userId : chat.getParticipants()) {
            if (!userId.equals(currentUserId)) {
                otherUserId = userId;
                break;
            }
        }
        
        // If we found another participant, load their info
        if (otherUserId != null) {
            final String finalOtherUserId = otherUserId;
            db.collection("users").document(otherUserId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                user.setId(finalOtherUserId);
                                
                                // Set the contact name
                                holder.contactName.setText(user.getFullName());
                                
                                // Set the profile image
                                if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                                    loadProfileImage(user.getProfileImage(), holder.avatarImage);
                                } else {
                                    holder.avatarImage.setImageResource(R.drawable.ic_person_placeholder);
                                }
                            }
                        }
                    });
        }
    }
    
    private void loadProfileImage(String imageUrl, ShapeableImageView imageView) {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder);
                
        Glide.with(context)
                .load(imageUrl)
                .apply(options)
                .into(imageView);
    }
    
    private boolean isSameDay(Date date1, Date date2) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return fmt.format(date1).equals(fmt.format(date2));
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public int getChatPosition(Chat chat) {
        return chatList.indexOf(chat);
    }

    public Chat getChatAt(int position) {
        if (position >= 0 && position < chatList.size()) {
            return chatList.get(position);
        }
        return null;
    }

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView avatarImage;
        TextView contactName;
        TextView lastMessage;
        TextView messageTime;
        TextView unreadIndicator;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.avatar_image);
            contactName = itemView.findViewById(R.id.contact_name);
            lastMessage = itemView.findViewById(R.id.last_message);
            messageTime = itemView.findViewById(R.id.message_time);
            unreadIndicator = itemView.findViewById(R.id.unread_indicator);
        }
    }
}
