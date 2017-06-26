package com.george.getreadychat;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.george.getreadychat.data.UserDetails;

import java.util.List;

/**
 * Created by farmaker1 on 26/06/2017.
 */

public class UserMessageAdapterBubbles extends ArrayAdapter<UserMessage> {

    private Activity activity;
    private List<UserMessage> objects;

    public UserMessageAdapterBubbles(Activity context, int resource, List<UserMessage> objects) {
        super(context, resource, objects);
        this.activity = context;
        this.objects = objects;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        int layoutResource = 0; // determined by view type
        UserMessage message = getItem(position);
        int viewType = getItemViewType(position);

        if (message.getName().equals(UserDetails.username)) {
            layoutResource = R.layout.item_message_left;
        } else {
            layoutResource = R.layout.item_message_right;
        }

        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = inflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        ////////
        boolean isPhoto = message.getPhotoUrl() != null;
        if (isPhoto) {
            holder.messageTextView.setVisibility(View.GONE);
            holder.photoImageView.setVisibility(View.VISIBLE);
            Glide.with(holder.photoImageView.getContext())
                    .load(message.getPhotoUrl())
                    .into(holder.photoImageView);
        } else {
            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.photoImageView.setVisibility(View.GONE);
            holder.messageTextView.setText(message.getText());
        }

        holder.timeTextView.setText(message.getTime());
        /*nameToNameTextView.setText(message.getNameToName());*/

        if (message.getName().equals(UserDetails.username)) {
            holder.authorTextView.setText(R.string.stringMyName);

        } else {
            holder.authorTextView.setText(message.getName());
        }

        if (message.getIsReaded().equals("false")) {
            holder.readedTextView.setText("");
        } else {
            holder.readedTextView.setText(R.string.readed);
        }

        if (!message.getName().equals(UserDetails.username)) {
            holder.readedTextView.setText("");
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
        return position % 2;
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
