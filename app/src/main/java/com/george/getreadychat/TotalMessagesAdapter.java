package com.george.getreadychat;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.george.getreadychat.data.UserDetails;

import java.security.Key;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.R.attr.key;
import static android.R.attr.value;


public class TotalMessagesAdapter extends ArrayAdapter<Totalmessage> {

    private List<Totalmessage> userNameMessages;
    private Context context;

    public TotalMessagesAdapter(Context context, int resource, List<Totalmessage> userNameMessages) {
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
        dummyTextView.setVisibility(View.INVISIBLE);

        Totalmessage totalmessage= getItem(position);

        dummyTextView.setText(totalmessage.getFirstEntry());
        messageTextView.setText(totalmessage.getSecondEnrty());

        String numberOfMessages = String.valueOf(UserDetails.numberOfMessages);
        numberTextView.setText(numberOfMessages);

        return convertView;
    }
}
