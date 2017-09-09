package net.swmud.trog.dspam.json;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by root on 23.04.16.
 * *  30     jsonObj['timestamp'] = data[0]
 * 31     jsonObj['spamstatus'] = data[1]
 * 32     jsonObj['from'] = data[2]
 * 33     jsonObj['signature'] = data[3]
 * 34     jsonObj['subject'] = data[4]
 * 35     jsonObj['status'] = data[5]
 * 36     jsonObj['msgid'] = data[6]
 */

/*
    $class = $rec{$signature}->{'class'} if ($rec{$signature}->{'class'} ne "");
    if ($class eq "S") { $cl = "spam"; $cllabel="$CONFIG{'LANG'}->{$LANGUAGE}->{'history_label_spam'}"; }
    elsif ($class eq "I") { $cl = "innocent"; $cllabel="$CONFIG{'LANG'}->{$LANGUAGE}->{'history_label_innocent'}"; }
    elsif ($class eq "F") {
      if ($rec{$signature}->{'count'} % 2 != 0) {
        $cl = "false"; $cllabel="$CONFIG{'LANG'}->{$LANGUAGE}->{'history_label_miss'}";
      } else {
        $cl = "innocent"; $cllabel="$CONFIG{'LANG'}->{$LANGUAGE}->{'history_label_innocent'}";
      }
    }
    elsif ($class eq "M") {
      if ($rec{$signature}->{'count'} % 2 != 0) {
          $cl = "missed"; $cllabel="$CONFIG{'LANG'}->{$LANGUAGE}->{'history_label_miss'}";
      } else {
          $cl = "spam"; $cllabel="$CONFIG{'LANG'}->{$LANGUAGE}->{'history_label_spam'}";
      }
    }
    elsif ($class eq "W") { $cl = "whitelisted"; $cllabel="$CONFIG{'LANG'}->{$LANGUAGE}->{'history_label_whitelist'}"; }
    elsif ($class eq "V") { $cl = "virus"; $cllabel="$CONFIG{'LANG'}->{$LANGUAGE}->{'history_label_virus'}"; }
    elsif ($class eq "A") { $cl = "blacklisted"; $cllabel="$CONFIG{'LANG'}->{$LANGUAGE}->{'history_label_rbl'}"; }
    elsif ($class eq "O") { $cl = "blocklisted"; $cllabel="$CONFIG{'LANG'}->{$LANGUAGE}->{'history_label_block'}"; }
    elsif ($class eq "N") { $cl = "inoculation"; $cllabel="$CONFIG{'LANG'}->{$LANGUAGE}->{'history_label_spam'}"; }
    elsif ($class eq "C") { $cl = "corpus"; $cllabel="$CONFIG{'LANG'}->{$LANGUAGE}->{'history_label_corpus'}"; }
    elsif ($class eq "U") { $cl = "unknown"; $cllabel="$CONFIG{'LANG'}->{$LANGUAGE}->{'history_label_unknown'}"; }
    elsif ($class eq "E") { $cl = "error"; $cllabel="$CONFIG{'LANG'}->{$LANGUAGE}->{'history_label_error'}"; }
    if ($messageid ne "" && $messageid ne "1") {
      if ($rec{$messageid}->{'resend'} ne "") {
        $cl = "relay";
        $cllabel = "$CONFIG{'LANG'}->{$LANGUAGE}->{'history_label_resend'}";
      }
      $rec{$messageid}->{'resend'} = $signature;
    }

*/

public class DspamEntry implements Serializable {

    public enum DeliveryStatus {
        UNKNOWN,
        DELIVERED,
        TAGGED
    }

    public enum SpamStatus {
        SPAM("S"),
        INNOCENT("I"),
        FALSE("F"),
        MISSED("M"),
        WHITELISTED("W"),
        VIRUS("V"),
        BLACKLISTED("A"),
        BLOCKLISTED("O"),
        INOCULATION("N"),
        CORPUS("C"),
        UNKNOWN("U"),
        ERROR("E"),
        RESEND("R"),
        _UNSET("");

        private String statusLetter;

        SpamStatus(String statusLetter) {
            this.statusLetter = statusLetter;
        }

        public String getStatusLetter() {
            return statusLetter;
        }
    }

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("spamstatus")
    private String spamStatusText;

    @SerializedName("from")
    private String from;

    @SerializedName("signature")
    private String signature;

    @SerializedName("subject")
    private String subject;

    @SerializedName("status")
    private String deliveryStatus;

    @SerializedName("msgid")
    private String msgId;

    private long signatureCount;
    private SpamStatus spamStatus;
    private Date date;

    public String getTimestamp() {
        return timestamp;
    }

    public String getSpamStatusText() {
        return spamStatusText;
    }

    public long getSignatureCount() {
        return signatureCount;
    }

    public void setSignatureCount(long signatureCount) {
        this.signatureCount = signatureCount;
    }

    public void setSpamStatus() {
        if (null == spamStatusText || spamStatusText.length() < 1) {
            spamStatus = SpamStatus._UNSET;
        } else {
            switch (spamStatusText) {
                case "S":
                    spamStatus = SpamStatus.SPAM;
                    break;
                case "I":
                    spamStatus = SpamStatus.INNOCENT;
                    break;
                case "F":
                    if (signatureCount % 2 == 0) {
                        spamStatus = SpamStatus.INNOCENT;
                    } else {
                        spamStatus = SpamStatus.FALSE;
                    }
                    break;
                case "M":
                    if (signatureCount % 2 == 0) {
                        spamStatus = SpamStatus.SPAM;
                    } else {
                        spamStatus = SpamStatus.MISSED;
                    }
                    break;
                case "W":
                    spamStatus = SpamStatus.WHITELISTED;
                    break;
                case "V":
                    spamStatus = SpamStatus.VIRUS;
                    break;
                case "A":
                    spamStatus = SpamStatus.BLACKLISTED;
                    break;
                case "O":
                    spamStatus = SpamStatus.BLOCKLISTED;
                    break;
                case "N":
                    spamStatus = SpamStatus.INNOCENT;
                    break;
                case "C":
                    spamStatus = SpamStatus.CORPUS;
                    break;
                case "U":
                    spamStatus = SpamStatus.UNKNOWN;
                    break;
                case "E":
                    spamStatus = SpamStatus.ERROR;
                    break;
                default:
                    spamStatus = SpamStatus._UNSET;
            }
        }
    }

    public void setDate() {
        long tstamp = 0;
        try {
            tstamp = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis((long)(tstamp*1000));
        date = calendar.getTime();
    }

    public void retrain() {
        if (spamStatus == SpamStatus.INNOCENT) {
            spamStatus = SpamStatus.MISSED;
        } else if (spamStatus == SpamStatus.SPAM) {
            spamStatus = SpamStatus.FALSE;
        }
    }

    public Date getDate() {
        return date;
    }

    public SpamStatus getSpamStatus() {
        return spamStatus;
    }

    public String getFrom() {
        return from;
    }

    public String getSignature() {
        return signature;
    }

    public String getSubject() {
        return subject;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public String getMsgId() {
        return msgId;
    }
}

