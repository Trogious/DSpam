package net.swmud.trog.dspam.gui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import net.swmud.trog.dspam.R;
import net.swmud.trog.dspam.core.DateFormatter;
import net.swmud.trog.dspam.json.Dspam;
import net.swmud.trog.dspam.json.DspamEntry;
import net.swmud.trog.dspam.json.RetrainRequest;


public class HistoryListAdapter extends BaseAdapter {
    private HistoryActivity activity;
    private LayoutInflater inflater;
    private Dspam dspam;
    private boolean[] positionsChecked;
    private int position = -1;

    public HistoryListAdapter(HistoryActivity activity, Dspam dspam) {
        this.activity = activity;
        this.dspam = dspam;
        positionsChecked = new boolean[dspam.dspam.size()];
    }

    @Override
    public int getCount() {
        return dspam.dspam.size();
    }

    @Override
    public Object getItem(int location) {
        return dspam.dspam.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        this.position = position;
        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
        }
        convertView = inflater.inflate(R.layout.list_row, null);

        final View cv = convertView;
        TextView status = (TextView) convertView
                .findViewById(R.id.status);
        TextView from = (TextView) convertView.findViewById(R.id.from);
        TextView signature = (TextView) convertView.findViewById(R.id.signature);
        TextView receivedDate = (TextView) convertView.findViewById(R.id.date);
        TextView subject = (TextView) convertView.findViewById(R.id.subject);
        final CheckBox retrainBox = (CheckBox) convertView.findViewById(R.id.checkBoxRetrainSelected);

        final DspamEntry entry = dspam.dspam.get(position);

        DspamEntry.SpamStatus spamStatus = entry.getSpamStatus();
        status.setText(entry.getSpamStatusText());
        switch (spamStatus) {
            case SPAM:
                status.setBackgroundResource(R.color.dspam_status_spam);
                break;
            case INNOCENT:
                status.setBackgroundResource(R.color.dspam_status_innocent);
                break;
            case FALSE:
                status.setBackgroundResource(R.color.dspam_status_false);
                break;
            case MISSED:
                status.setBackgroundResource(R.color.dspam_status_missed);
                break;
            case WHITELISTED:
                status.setBackgroundResource(R.color.dspam_status_whitelisted);
                break;
            case VIRUS:
                status.setBackgroundResource(R.color.dspam_status_virus);
                break;
            case BLACKLISTED:
                status.setBackgroundResource(R.color.dspam_status_blacklisted);
                break;
            case BLOCKLISTED:
                status.setBackgroundResource(R.color.dspam_status_blocklisted);
                break;
            case INOCULATION:
                status.setBackgroundResource(R.color.dspam_status_inoculation);
                break;
            case CORPUS:
                status.setBackgroundResource(R.color.dspam_status_corpus);
                break;
            case UNKNOWN:
                status.setBackgroundResource(R.color.dspam_status_unknown);
                break;
            case ERROR:
                status.setBackgroundResource(R.color.dspam_status_error);
                break;
            default:
                status.setBackgroundResource(R.color.dspam_status_resend);
                status.setText("R");
        }

        status.setText(spamStatus.getStatusLetter().toCharArray(), 0, 1);
        from.setText(entry.getFrom());
        signature.setText(entry.getSignature());
        subject.setText(entry.getSubject());
        receivedDate.setText(DateFormatter.format(entry.getDate()));
        retrainBox.setVisibility(activity.checkboxesVisible);
        if (activity.checkboxesVisible == View.GONE) {
            retrainBox.setChecked(false);
        } else if (positionsChecked[position]) {
            retrainBox.setChecked(true);
        }
//        retrainBox.setText("" + position);
        retrainBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                positionsChecked[position] = checked;
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, DetailsActivity.class);
                intent.putExtra("entry", entry);
                activity.startActivity(intent);
            }
        });


        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                activity.checkboxesVisible = (activity.checkboxesVisible == View.GONE) ? View.VISIBLE : View.GONE;
                Button retrainButton = (Button) activity.findViewById(R.id.buttonRetrainSelected);
                retrainButton.setVisibility(activity.checkboxesVisible);
                positionsChecked[position] = (activity.checkboxesVisible == View.VISIBLE);
                retrainButton.setOnClickListener((activity.checkboxesVisible == View.GONE) ? null : new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        RetrainRequest retrainRequest = new RetrainRequest(dspam, positionsChecked);
                        Gson gson = new Gson();
                        String jsonStr = gson.toJson(retrainRequest);
                        LaunchActivity.sendMessage(jsonStr);
                        Toast.makeText(activity, jsonStr, Toast.LENGTH_LONG).show();
                    }
                });
                notifyDataSetChanged();
                return true;
            }
        });

        return convertView;
    }
}