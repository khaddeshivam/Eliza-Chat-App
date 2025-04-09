package com.eliza.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;
import java.util.Date;

public class User implements Parcelable {
    @DocumentId
    private String id;
    @PropertyName("first_name")
    private String firstName;
    @PropertyName("last_name")
    private String lastName;
    @PropertyName("phone_number")
    private String phoneNumber;
    @PropertyName("profile_image")
    private String profileImage;
    @PropertyName("status")
    private String status;
    @PropertyName("last_seen")
    private Date lastSeen;
    @PropertyName("is_online")
    private boolean isOnline;
    @PropertyName("bio")
    private String bio;
    @PropertyName("test_account")
    private boolean testAccount;
    @PropertyName("test_user_id")
    private String testUserId;
    @PropertyName("display_name")
    private String displayName;
    @PropertyName("profile_picture_url")
    private String profilePictureUrl;

    public User() {
        // Required empty constructor for Firestore
    }

    public User(String id, String phoneNumber, String displayName, String profilePictureUrl) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.displayName = displayName;
        this.profilePictureUrl = profilePictureUrl;
        this.lastSeen = new Date();
        this.isOnline = false;
        this.testAccount = false;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @PropertyName("first_name")
    public String getFirstName() { return firstName; }
    @PropertyName("first_name")
    public void setFirstName(String firstName) { this.firstName = firstName; }

    @PropertyName("last_name")
    public String getLastName() { return lastName; }
    @PropertyName("last_name")
    public void setLastName(String lastName) { this.lastName = lastName; }

    @PropertyName("phone_number")
    public String getPhoneNumber() { return phoneNumber; }
    @PropertyName("phone_number")
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    @PropertyName("profile_image")
    public String getProfileImage() { return profileImage; }
    @PropertyName("profile_image")
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    @PropertyName("status")
    public String getStatus() { return status; }
    @PropertyName("status")
    public void setStatus(String status) { this.status = status; }

    @PropertyName("last_seen")
    public Date getLastSeen() { return lastSeen; }
    @PropertyName("last_seen")
    public void setLastSeen(Date lastSeen) { this.lastSeen = lastSeen; }

    @PropertyName("is_online")
    public boolean isOnline() { return isOnline; }
    @PropertyName("is_online")
    public void setOnline(boolean online) { isOnline = online; }

    @PropertyName("bio")
    public String getBio() { return bio; }
    @PropertyName("bio")
    public void setBio(String bio) { this.bio = bio; }

    @PropertyName("test_account")
    public boolean isTestAccount() { return testAccount; }
    @PropertyName("test_account")
    public void setTestAccount(boolean testAccount) { this.testAccount = testAccount; }

    @PropertyName("test_user_id")
    public String getTestUserId() { return testUserId; }
    @PropertyName("test_user_id")
    public void setTestUserId(String testUserId) { this.testUserId = testUserId; }

    @PropertyName("display_name")
    public String getDisplayName() { return displayName; }
    @PropertyName("display_name")
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    @PropertyName("profile_picture_url")
    public String getProfilePictureUrl() { return profilePictureUrl; }
    @PropertyName("profile_picture_url")
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            fullName.append(firstName);
            if (lastName != null && !lastName.isEmpty()) {
                fullName.append(" ").append(lastName);
            }
        }
        return fullName.toString();
    }

    public void setFullName(String fullName) {
        this.displayName = fullName;
    }

    public String getPhotoUrl() { return profileImage; }
    public void setPhotoUrl(String photoUrl) { this.profileImage = photoUrl; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(phoneNumber);
        dest.writeString(displayName);
        dest.writeString(profilePictureUrl);
        dest.writeLong(lastSeen != null ? lastSeen.getTime() : -1);
        dest.writeByte((byte) (isOnline ? 1 : 0));
        dest.writeByte((byte) (testAccount ? 1 : 0));
    }

    protected User(Parcel in) {
        id = in.readString();
        phoneNumber = in.readString();
        displayName = in.readString();
        profilePictureUrl = in.readString();
        long lastSeenTime = in.readLong();
        lastSeen = lastSeenTime != -1 ? new Date(lastSeenTime) : null;
        isOnline = in.readByte() != 0;
        testAccount = in.readByte() != 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
