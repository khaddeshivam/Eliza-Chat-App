package com.eliza.messenger.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.eliza.messenger.R;
import com.eliza.messenger.databinding.ItemCallBinding;
import com.eliza.messenger.model.Call;
import com.eliza.messenger.util.DateUtils;

public class CallsAdapter extends ListAdapter<Call, CallsAdapter.CallViewHolder> {
    private final OnCallClickListener listener;

    public CallsAdapter(DiffUtil.ItemCallback<Call> itemCallback, OnCallClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public CallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCallBinding binding = ItemCallBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CallViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CallViewHolder holder, int position) {
        Call call = getItem(position);
        holder.bind(call);
    }

    static class CallViewHolder extends RecyclerView.ViewHolder {
        private final ItemCallBinding binding;
        private final OnCallClickListener listener;

        CallViewHolder(@NonNull ItemCallBinding binding, OnCallClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }


        void bind(Call call) {
            binding.callName.setText(call.getName());
            binding.callTime.setText(DateUtils.formatCallTime(call.getTimestamp()));
            binding.callDuration.setText(DateUtils.formatCallDuration(call.getDuration()));
            binding.callType.setImageResource(call.isVideoCall() ? R.drawable.ic_video_call : R.drawable.ic_voice_call);
            binding.callStatus.setImageResource(call.getStatus().getDrawable());
            binding.getRoot().setOnClickListener(v -> listener.onCallClick(call));
        }
    }

    public interface OnCallClickListener {
        void onCallClick(Call call);
    }

    private static final DiffUtil.ItemCallback<Call> DIFF_CALLBACK = new DiffUtil.ItemCallback<Call>() {
        @Override
        public boolean areItemsTheSame(@NonNull Call oldItem, @NonNull Call newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Call oldItem, @NonNull Call newItem) {
            return oldItem.equals(newItem);
        }
    };
}