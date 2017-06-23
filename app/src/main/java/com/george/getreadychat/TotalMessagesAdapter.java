package com.george.getreadychat;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.george.getreadychat.data.UserDetails;

import java.util.List;


public class TotalMessagesAdapter extends ArrayAdapter<String> {

    private List<String> userNameMessages;
    private Context context;

    public TotalMessagesAdapter(Context context, int resource, List<String> userNameMessages) {
        super(context, resource, userNameMessages);
        this.context = context;
        this.userNameMessages = userNameMessages;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_all_message, parent, false);
        }

        TextView messageTextView = (TextView) convertView.findViewById(R.id.allMessageTextView);
        TextView dummyTextView = (TextView) convertView.findViewById(R.id.allNameTextView);
        TextView numberTextView = (TextView) convertView.findViewById(R.id.allNumberTextView);

        String s= getItem(position);

        dummyTextView.setText("User Messages");
        messageTextView.setText(s);

        String numberOfMessages = String.valueOf(UserDetails.numberOfMessages);
        numberTextView.setText(numberOfMessages);

        return convertView;
    }
}
