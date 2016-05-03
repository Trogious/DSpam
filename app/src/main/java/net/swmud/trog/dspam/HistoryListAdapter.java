package net.swmud.trog.dspam;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class HistoryListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private Dspam dspam;

    public HistoryListAdapter(Activity activity, Dspam dspam) {
        this.activity = activity;
        this.dspam = dspam;
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
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_row, null);

        TextView status = (TextView) convertView
                .findViewById(R.id.status);
        TextView from = (TextView) convertView.findViewById(R.id.from);
        TextView signature = (TextView) convertView.findViewById(R.id.signature);
        TextView receivedDate = (TextView) convertView.findViewById(R.id.date);
        TextView subject = (TextView) convertView.findViewById(R.id.subject);

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

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(activity, DetailsActivity.class);
                intent.putExtra("entry", entry);
                activity.startActivity(intent);
//                Toast.makeText(activity, entry.getDeliveryStatus(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        return convertView;
    }
}