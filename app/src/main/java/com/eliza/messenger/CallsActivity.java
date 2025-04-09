package com.eliza.messenger;

import static com.eliza.messenger.model.Call.Status.INCOMING;

import static org.webrtc.MediaSource.State.ENDED;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eliza.messenger.adapter.CallsAdapter;
import com.eliza.messenger.databinding.ActivityCallsBinding;
import com.eliza.messenger.model.Call;
import com.eliza.messenger.viewmodel.CallsViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CallsActivity extends AppCompatActivity implements CallsAdapter.OnCallClickListener {
    private static final String EXTRA_CALL_ID = "call_id";
    private static final int PICK_CONTACT = 1;
    private static final int REQUEST_PERMISSIONS = 2;
    private ActivityCallsBinding binding;
    private CallsViewModel viewModel;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private CallsAdapter adapterFavorites;
    private CallsAdapter adapterRecent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(this, PhoneAuthActivity.class));
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(CallsViewModel.class);
        setupUI();
        setupObservers();
    }

    private void setupUI() {
        // Set up RecyclerView for favorites
        binding.recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(this));
        adapterFavorites = new CallsAdapter(
                new DiffUtil.ItemCallback<Call>() {
                    @Override
                    public boolean areItemsTheSame(@NonNull Call oldItem, @NonNull Call newItem) {
                        return oldItem.getId().equals(newItem.getId());
                    }

                    @Override
                    public boolean areContentsTheSame(@NonNull Call oldItem, @NonNull Call newItem) {
                        return oldItem.equals(newItem);
                    }
                },
                this
        );
        binding.recyclerViewFavorites.setAdapter(adapterFavorites);

        // Set up RecyclerView for recent calls
        binding.recyclerViewCalls.setLayoutManager(new LinearLayoutManager(this));
        adapterRecent = new CallsAdapter(
                new DiffUtil.ItemCallback<Call>() {
                    @Override
                    public boolean areItemsTheSame(@NonNull Call oldItem, @NonNull Call newItem) {
                        return oldItem.getId().equals(newItem.getId());
                    }

                    @Override
                    public boolean areContentsTheSame(@NonNull Call oldItem, @NonNull Call newItem) {
                        return oldItem.equals(newItem);
                    }
                },
                this
        );
        binding.recyclerViewCalls.setAdapter(adapterRecent);

        // Initialize call controls with default states
        binding.buttonSpeaker.setImageResource(R.drawable.ic_speaker_off);
        binding.buttonVideo.setImageResource(R.drawable.ic_video_off);

        // Set up FAB for new call
        binding.fabNewCall.setOnClickListener(v -> {
            // Check for permissions
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_CONTACTS},
                        REQUEST_PERMISSIONS);
            } else {
                openContactsPicker();
            }
        });

        // Set up button click listeners
        binding.buttonSpeaker.setOnClickListener(v -> {
            viewModel.toggleSpeaker();
        });

        binding.buttonVideo.setOnClickListener(v -> {
            viewModel.toggleVideo();
        });

        // Set up observers after views are initialized
        viewModel.isSpeakerOn().observe(this, isOn -> {
            if (isOn != null) {
                binding.buttonSpeaker.setImageResource(
                        isOn ? R.drawable.ic_speaker_on : R.drawable.ic_speaker_off
                );
            }
        });

        viewModel.isVideoOn().observe(this, isOn -> {
            if (isOn != null) {
                binding.buttonVideo.setImageResource(
                        isOn ? R.drawable.ic_video_on : R.drawable.ic_video_off
                );
            }
        });

        // Set up observers for favorites and recent calls
        viewModel.getFavorites().observe(this, favorites -> {
            if (favorites != null) {
                adapterFavorites.submitList(favorites);
            }
        });

        viewModel.getRecentCalls().observe(this, recentCalls -> {
            if (recentCalls != null) {
                adapterRecent.submitList(recentCalls);
            }
        });
    }

    private void setupObservers() {
        viewModel.getCallStatus().observe(this, status -> {
            if (status != null) {
                updateUIForCallStatus(status);
            }
        });
    }

    private void updateUIForCallStatus(Call.Status status) {
        // Update UI based on call status
        switch (status) {
            case INCOMING:
                showIncomingCallDialog();
                break;
            case ONGOING:
                showCallControls();
                break;
            case ENDED:
                hideCallControls();
                break;
        }
    }

    private void showIncomingCallDialog() {
        // Show incoming call dialog
    }

    private void showCallControls() {
        // Show call controls (speaker, video, end call buttons)
    }

    private void hideCallControls() {
        // Hide call controls
    }

    private void openContactsPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openContactsPicker();
            } else {
                Toast.makeText(this, "Contacts permission is required to make calls", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT && resultCode == RESULT_OK && data != null) {
            Uri contactData = data.getData();
            Cursor cursor = getContentResolver().query(contactData, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                if (hasPhone.equals("1")) {
                    Cursor phones = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null
                    );
                    if (phones != null && phones.moveToFirst()) {
                        String phoneNumber = phones.getString(
                                phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        );
                        makeCall(phoneNumber);
                        phones.close();
                    }
                }
                cursor.close();
            }
        }
    }

    private void makeCall(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CALL_PHONE},
                    REQUEST_PERMISSIONS);
        } else {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        }
    }

    private void showMoreOptions() {
        MoreOptionsDialogFragment dialog = new MoreOptionsDialogFragment();
        dialog.show(getSupportFragmentManager(), "more_options_dialog");
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.startCallService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewModel.stopCallService();
    }

    @Override
    public void onCallClick(Call call) {
        // Handle the call click within this activity
        viewModel.selectCall(call);
    }
}