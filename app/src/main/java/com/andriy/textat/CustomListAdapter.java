package com.andriy.textat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.andriy.textat.Mark;
import com.andriy.textat.R;

import java.util.ArrayList;

public class CustomListAdapter extends ArrayAdapter<Mark> implements View.OnClickListener {

    private ArrayList<Mark> marks;
    private Context mContext;

    // View lookup cache
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
        // Get the data item for this position
        Mark mark = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

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

        /*Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim. : R.anim.down_from_top);
        result.startAnimation(animation);
        lastPosition = position;*/

        viewHolder.markId.setText(mark.getId());
        viewHolder.description.setText(mark.getSnippet());
        viewHolder.date.setText(mark.getTimestamp().toDate().toString()); // hacer método en clase mark
        viewHolder.title.setText(mark.getTitle());
        viewHolder.user.setText(mark.getUser());

        // Return the completed view to render on screen
        return convertView;
    }
}
