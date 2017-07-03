package com.george.getreadychat;

import android.app.Activity;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.george.getreadychat.data.UserDetails;

import java.util.List;


public class UserMessageAdapterBubbles extends ArrayAdapter<UserMessage> {

    private Activity activity;
    private List<UserMessage> objects;

    int layoutResource;

    public UserMessageAdapterBubbles(Activity context, int resource, List<UserMessage> objects) {
        super(context, resource, objects);
        this.activity = context;
        this.objects = objects;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        UserMessage message = getItem(position);
        int viewType = getItemViewType(position);

        switch (viewType) {
            case 0:
                ViewHolder holder1;
                View v = convertView;
                if (v == null) {
                    LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                    v = inflater.inflate(R.layout.item_message_right, parent, false);
                    holder1 = new ViewHolder(v);
                    v.setTag(holder1);
                } else {
                    holder1 = (ViewHolder) v.getTag();
                }

                boolean isPhoto = message.getPhotoUrl() != null;
                if (isPhoto) {
                    holder1.messageTextView.setVisibility(View.GONE);
                    holder1.photoImageView.setVisibility(View.VISIBLE);
                    Glide.with(holder1.photoImageView.getContext())
                            .load(message.getPhotoUrl())
                            .into(holder1.photoImageView);
                } else {
                    holder1.messageTextView.setVisibility(View.VISIBLE);
                    holder1.photoImageView.setVisibility(View.GONE);
                    holder1.messageTextView.setText(message.getText());
                }

                holder1.timeTextView.setText(message.getTime());

                if (message.getName().equals(UserDetails.username)) {
                    holder1.authorTextView.setText(R.string.stringMyName);

                } else {
                    holder1.authorTextView.setText(message.getName());

                }

                if (message.getIsReaded().equals("false")) {
                    holder1.readedTextView.setText("");
                } else {
                    holder1.readedTextView.setText(R.string.readed);
                }

                if (!message.getName().equals(UserDetails.username)) {
                    holder1.readedTextView.setText("");
                }

                return v;


            case 1:
                ViewHolder holder2 = null;
                View vv = convertView;
                if (vv == null) {
                    LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                    vv = inflater.inflate(R.layout.item_message_left, parent, false);
                    holder2 = new ViewHolder(vv);
                    vv.setTag(holder2);
                } else {
                    holder2 = (ViewHolder) vv.getTag();
                }

                boolean isPhotoo = message.getPhotoUrl() != null;
                if (isPhotoo) {
                    holder2.messageTextView.setVisibility(View.GONE);
                    holder2.photoImageView.setVisibility(View.VISIBLE);
                    Glide.with(holder2.photoImageView.getContext())
                            .load(message.getPhotoUrl())
                            .into(holder2.photoImageView);
                } else {
                    holder2.messageTextView.setVisibility(View.VISIBLE);
                    holder2.photoImageView.setVisibility(View.GONE);
                    holder2.messageTextView.setText(message.getText());
                }

                holder2.timeTextView.setText(message.getTime());

                if (message.getName().equals(UserDetails.username)) {
                    holder2.authorTextView.setText(R.string.stringMyName);

                } else {
                    holder2.authorTextView.setText(message.getName());
                }

                if (message.getIsReaded().equals("false")) {
                    holder2.readedTextView.setText("");
                } else {
                    holder2.readedTextView.setText(R.string.readed);
                }

                if (!message.getName().equals(UserDetails.username)) {
                    holder2.readedTextView.setText("");
                }

                return vv;
        }

        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        // return the total number of view types. this value should never change
        // at runtime
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        // return a value between 0 and (getViewTypeCount - 1)
        UserMessage message = getItem(position);

        if (message.getName().equals(UserDetails.username)) {
            position = 0;
        } else {
            position = 1;
        }

        return position;
    }

    private class ViewHolder {

        private ImageView photoImageView;
        private TextView messageTextView;
        private TextView authorTextView;
        private TextView timeTextView;
        private TextView readedTextView;

        public ViewHolder(View v) {
            photoImageView = (ImageView) v.findViewById(R.id.photoImageView);
            messageTextView = (TextView) v.findViewById(R.id.messageTextView);
            authorTextView = (TextView) v.findViewById(R.id.nameTextView);
            timeTextView = (TextView) v.findViewById(R.id.timeTextView);
            readedTextView = (TextView) v.findViewById(R.id.isReadTextView);
        }
    }

}
