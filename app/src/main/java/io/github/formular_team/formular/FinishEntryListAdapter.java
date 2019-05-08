package io.github.formular_team.formular;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.github.formular_team.formular.core.RaceFinishEntry;
import io.github.formular_team.formular.core.User;


public class FinishEntryListAdapter extends ArrayAdapter<RaceFinishEntry> {

    private Context context;
    private int resource;

    public FinishEntryListAdapter(Context context, int resource, ArrayList<RaceFinishEntry> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        User user = getItem(position).getUser();
        long time = getItem(position).getTime();


        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(this.resource, parent, false);

        TextView name = (TextView) convertView.findViewById(R.id.user_name);
        TextView raceTime = (TextView) convertView.findViewById(R.id.user_time);
        TextView placement = (TextView) convertView.findViewById(R.id.finish_placement);


        raceTime.setText(Long.toString(time));
        name.setText(user.getName());
        position += 1;
        placement.setText("Place: " + position);

        return convertView;
    }

}
