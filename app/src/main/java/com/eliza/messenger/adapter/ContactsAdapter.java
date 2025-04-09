package com.eliza.messenger.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.eliza.messenger.R;
import com.eliza.messenger.databinding.ItemContactBinding;
import com.eliza.messenger.model.User;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {
    private final Context context;
    private List<User> contactList;
    private OnContactClickListener listener;

    public ContactsAdapter(Context context, List<User> contactList) {
        this.context = context;
        this.contactList = contactList;
    }

    public void updateData(List<User> newContactList) {
        this.contactList = newContactList;
        notifyDataSetChanged();
    }

    public interface OnContactClickListener {
        void onContactClick(User user);
    }

    public void setOnContactClickListener(OnContactClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContactBinding binding = ItemContactBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ContactViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        User user = contactList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return contactList != null ? contactList.size() : 0;
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        private final ItemContactBinding binding;

        public ContactViewHolder(@NonNull ItemContactBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(User user) {
            binding.contactName.setText(user.getFullName());
            
            if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
                // Format phone number for better readability
                String formattedNumber = user.getPhoneNumber();
                if (formattedNumber.length() > 10) {
                    formattedNumber = "+" + formattedNumber;
                }
                binding.contactPhone.setText(formattedNumber);
                binding.contactPhone.setVisibility(View.VISIBLE);
            } else {
                binding.contactPhone.setVisibility(View.GONE);
            }

            // Load avatar image using profilePictureUrl
            if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                Glide.with(context)
                        .load(user.getProfilePictureUrl())
                        .placeholder(R.drawable.ic_person_placeholder)
                        .error(R.drawable.ic_person_placeholder)
                        .into(binding.avatarImage);
            } else {
                Glide.with(context)
                        .load(R.drawable.ic_person_placeholder)
                        .into(binding.avatarImage);
            }

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContactClick(user);
                }
            });
        }
    }
}
