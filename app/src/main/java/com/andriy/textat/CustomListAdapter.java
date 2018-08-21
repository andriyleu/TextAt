package com.andriy.textat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomListAdapter extends ArrayAdapter<Mark> implements View.OnClickListener {

    private ArrayList<Mark> marks;
    private Context mContext;

    private static class ViewHolder {
        TextView markId;
        TextView description;
        TextView date;
        TextView user;
        TextView title;
    }

    public CustomListAdapter(ArrayList<Mark> data, Context context) {
        super(context, R.layout.list_item_1, data);
        this.marks = data;
        this.mContext = context;
    }


    @Override
    public void onClick(View view) {

    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Mark mark = getItem(position);
        ViewHolder viewHolder;

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_1, parent, false);
            viewHolder.title = (TextView) convertView.findViewById(R.id.listMarkTitle);
            viewHolder.markId = (TextView) convertView.findViewById(R.id.listMarkId);
            viewHolder.date = (TextView) convertView.findViewById(R.id.listMarkDate);
            viewHolder.description = (TextView) convertView.findViewById(R.id.listMarkSummary);
            viewHolder.user = (TextView) convertView.findViewById(R.id.listMarkBy);

            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        viewHolder.markId.setText(mark.getId());
        viewHolder.description.setText(mark.getSnippet());
        viewHolder.date.setText(mark.getDate());
        viewHolder.title.setText(mark.getTitle());
        viewHolder.user.setText(mark.getUser());

        return convertView;
    }
}
