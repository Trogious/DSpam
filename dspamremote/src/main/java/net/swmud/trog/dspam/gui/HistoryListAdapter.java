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

import net.swmud.trog.dspam.R;
import net.swmud.trog.dspam.core.DateFormatter;
import net.swmud.trog.dspam.json.DspamEntry;
import net.swmud.trog.dspam.json.JsonRpc;
import net.swmud.trog.dspam.json.RetrainRequest;

import java.util.List;


public class HistoryListAdapter extends BaseAdapter {
    private HistoryActivity activity;
    private LayoutInflater inflater;
    private List<DspamEntry> entries;
    private boolean[] positionsChecked;

    public HistoryListAdapter(HistoryActivity activity, List<DspamEntry> entries) {
        this.activity = activity;
        this.entries = entries;
        positionsChecked = new boolean[entries.size()];
    }

    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public Object getItem(int location) {
        return entries.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
        }
        convertView = inflater.inflate(R.layout.list_row, null);

        TextView status = (TextView) convertView.findViewById(R.id.status);
        TextView from = (TextView) convertView.findViewById(R.id.from);
        TextView signature = (TextView) convertView.findViewById(R.id.signature);
        TextView receivedDate = (TextView) convertView.findViewById(R.id.date);
        TextView subject = (TextView) convertView.findViewById(R.id.subject);
        final CheckBox retrainBox = (CheckBox) convertView.findViewById(R.id.checkBoxRetrainSelected);
        final Button retrainButton = (Button) activity.findViewById(R.id.buttonRetrainSelected);

        final DspamEntry entry = entries.get(position);

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

        retrainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RetrainRequest retrainRequest = new RetrainRequest(entries, positionsChecked);
                JsonRpc.JsonRequest request = retrainRequest.getJsonRpcRequest();
                LaunchActivity.sendMessage(request, HistoryActivity.ARG_RETRAINED, HistoryActivity.class);
            }
        });

        retrainBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                positionsChecked[position] = checked;
                if (!checked && countChecked() < 1) {
                    activity.checkboxesVisible = View.GONE;
                    retrainButton.setVisibility(activity.checkboxesVisible);
                    notifyDataSetChanged();
                }
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, DetailsActivity.class);
                intent.putExtra(DetailsActivity.ARG_ENTRY, entry);
                activity.startActivity(intent);
            }
        });

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                activity.checkboxesVisible = (activity.checkboxesVisible == View.GONE) ? View.VISIBLE : View.GONE;
                retrainButton.setVisibility(activity.checkboxesVisible);
                if (activity.checkboxesVisible == View.GONE) {
                    checkAll(false);
                } else {
                    positionsChecked[position] = true;
                }
                notifyDataSetChanged();
                return true;
            }
        });

        return convertView;
    }

    private void checkAll(boolean checked) {
        for (int i = 0; i < positionsChecked.length; ++i) {
            positionsChecked[i] = checked;
        }
    }

    private int countChecked() {
        int checked = 0;
        for (int i = 0; i < positionsChecked.length; ++i) {
            if (positionsChecked[i]) {
                ++checked;
            }
        }

        return checked;
    }

    void retrainEntries(final List<String> sigs) {
        for (DspamEntry entry : entries) {
            if (sigs.contains(entry.getSignature())) {
                entry.retrain();
            }
        }
        positionsChecked = new boolean[entries.size()];
    }
}