package com.eliza.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.eliza.messenger.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.Objects;

public class Call implements Parcelable {
    public enum Status {
        INCOMING(R.drawable.ic_call_incoming),
        ONGOING(R.drawable.ic_call_ongoing),
        MISSED(R.drawable.ic_call_missed),
        ENDED(R.drawable.ic_call_end);

        private final int drawableRes;

        Status(int drawableRes) {
            this.drawableRes = drawableRes;
        }

        public int getDrawable() {
            return drawableRes;
        }
    }

    private String id;
    private String callerId;
    private String calleeId;
    private String roomId;
    private String sdp;
    private String type;
    private String candidate;
    private boolean active;
    private Timestamp timestamp;
    private String name;
    private long duration;
    private boolean videoCall;
    private Status status;

    public Call(String id, String callerId, String calleeId, String roomId, String sdp, String type, Status status, boolean isVideoCall) {
        this.id = id;
        this.callerId = callerId;
        this.calleeId = calleeId;
        this.roomId = roomId;
        this.sdp = sdp;
        this.type = type;
        this.status = status;
        this.duration = 0;
        this.videoCall = isVideoCall;
    }

    public Call(Parcel in) {
        id = in.readString();
        callerId = in.readString();
        calleeId = in.readString();
        roomId = in.readString();
        sdp = in.readString();
        type = in.readString();
        candidate = in.readString();
        active = in.readByte() != 0;
        timestamp = in.readParcelable(Timestamp.class.getClassLoader());
        name = in.readString();
        duration = in.readLong();
        videoCall = in.readByte() != 0;
        status = Status.valueOf(in.readString());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(callerId);
        dest.writeString(calleeId);
        dest.writeString(roomId);
        dest.writeString(sdp);
        dest.writeString(type);
        dest.writeString(candidate);
        dest.writeByte((byte) (active ? 1 : 0));
        dest.writeParcelable(timestamp, flags);
        dest.writeString(name);
        dest.writeLong(duration);
        dest.writeByte((byte) (videoCall ? 1 : 0));
        dest.writeString(status.name());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Call> CREATOR = new Creator<Call>() {
        @Override
        public Call createFromParcel(Parcel in) {
            return new Call(in);
        }

        @Override
        public Call[] newArray(int size) {
            return new Call[size];
        }
    };

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getCallerId() {
        return callerId;
    }

    public String getCalleeId() {
        return calleeId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getSdp() {
        return sdp;
    }

    public String getType() {
        return type;
    }

    public String getCandidate() {
        return candidate;
    }

    public boolean isActive() {
        return active;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return callerId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())
                ? "Incoming Call"
                : "Outgoing Call";
    }

    public long getDuration() {
        return duration;
    }

    public boolean isVideoCall() {
        return videoCall;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Call call = (Call) o;
        return active == call.active &&
                duration == call.duration &&
                videoCall == call.videoCall &&
                Objects.equals(id, call.id) &&
                Objects.equals(callerId, call.callerId) &&
                Objects.equals(calleeId, call.calleeId) &&
                Objects.equals(roomId, call.roomId) &&
                Objects.equals(sdp, call.sdp) &&
                Objects.equals(type, call.type) &&
                Objects.equals(candidate, call.candidate) &&
                Objects.equals(timestamp, call.timestamp) &&
                Objects.equals(name, call.name) &&
                status == call.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, callerId, calleeId, roomId, sdp, type, candidate, active, timestamp, name, duration, videoCall, status);
    }
}