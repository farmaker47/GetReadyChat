package com.george.getreadychat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class UserMessageAdapter extends ArrayAdapter<UserMessage> {

    private Context mContext;
    /*AfterPickingMessages act = (AfterPickingMessages) mContext;*/

    public UserMessageAdapter(Context context, int resource, List<UserMessage> objects) {
        super(context, resource, objects);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_message, parent, false);
        }

        ImageView photoImageView = (ImageView) convertView.findViewById(R.id.photoImageView);
        TextView messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
        TextView authorTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        TextView timeTextView = (TextView) convertView.findViewById(R.id.timeTextView);
        TextView readedTextView = (TextView) convertView.findViewById(R.id.isReadTextView);


        /*TextView nameToNameTextView = (TextView) convertView.findViewById(R.id.nameToNameTextView);*/

        UserMessage message = getItem(position);

        boolean isPhoto = message.getPhotoUrl() != null;
        if (isPhoto) {
            messageTextView.setVisibility(View.GONE);
            photoImageView.setVisibility(View.VISIBLE);
            Glide.with(photoImageView.getContext())
                    .load(message.getPhotoUrl())
                    .into(photoImageView);
        } else {
            messageTextView.setVisibility(View.VISIBLE);
            photoImageView.setVisibility(View.GONE);
            messageTextView.setText(message.getText());
        }
        authorTextView.setText(message.getName());
        timeTextView.setText(message.getTime());
        /*nameToNameTextView.setText(message.getNameToName());*/

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String strUsername = sharedPref.getString("myUsername", null);
        Log.e("CONTEXT OF ADAPTER", strUsername);

        String getName = message.getName();
        Log.e("MESSAGE__HAS__NAME?", getName);


        if (!message.getName().equals(strUsername)) {
            readedTextView.setVisibility(View.INVISIBLE);
        }

        if (message.getIsReaded() == false) {
            readedTextView.setText("Delivered");
        } else {
            readedTextView.setText("Readed");
        }


        return convertView;
    }
}
