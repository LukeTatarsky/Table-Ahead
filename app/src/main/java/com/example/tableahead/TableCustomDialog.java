package com.example.tableahead;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.DialogFragment;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class TableCustomDialog extends DialogFragment {
//    String TAG = "TableCustomDialog";
    private DialogListener listener;

    public interface DialogListener {
        void onDialogComplete(String partySize, Date date, String time);
    }
    public void setListener(DialogListener listener) {
        this.listener = listener;
    }

    // Max party size
    final int max_party_size = 15;
    int current_party_size = 2;  // default party size of 2
    private String date_string = "2023-10-26"; //PLACEHOLDER, will default to current date
    private String time; //PLACEHOLDER, will default to current time
    final int text_size = 18;
    Calendar calendar;
    Calendar mainCalendar;
    Calendar workingCalendar = Calendar.getInstance();
    Date dateToPass;
    String open_time;
    String close_time;
    int openHour;
//    Context context;
    private int intervalMinutes;
    final int book_to_days_in_advance = 30; //could be received from restaurant
    Boolean dateChangedFlag = Boolean.FALSE; // when the date gets advanced in tableScreen

    public void setIntervalMinutes(int intervalMinutes) {
        this.intervalMinutes = intervalMinutes;
    }
    public void setCurrent_party_size(int current_party_size) {
        this.current_party_size = current_party_size;
    }
    public void setCalendar(Calendar calendar) {
        this.mainCalendar = calendar;
        this.mainCalendar.set(Calendar.MINUTE, 0);
        this.mainCalendar.set(Calendar.SECOND, 0);
        this.mainCalendar.set(Calendar.MILLISECOND, 0);
    }
    public void setDateChangedFlag(Boolean dateChangedFlag) {
        this.dateChangedFlag = dateChangedFlag;
    }
    public void setDate_string(String date_string) { this.date_string = date_string; }
    public void setTime(String time) {
        this.time = time;
    }
    public void setOpen_time(String open_time) {
        this.open_time = open_time;
    }
    public void setClose_time(String close_time) {
        this.close_time = close_time;
    }
//    public void setContext(Context context) { this.context = context; }
    public void showDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.table_date_time_party_dialog, null);
        builder.setView(dialogView);
        TextView txtPartySize = dialogView.findViewById(R.id.txtPartySize);
        TextView txtDate = dialogView.findViewById(R.id.txtDate);
        TextView txtTime = dialogView.findViewById(R.id.txtTime);
        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroupContainer);
        Button okButton = dialogView.findViewById(R.id.btn_ok);
        Button cancelButton = dialogView.findViewById(R.id.btn_cancel);
        LinearLayout datePickerLayout = dialogView.findViewById(R.id.datePickerLayout);
        LinearLayout timePickerLayout = dialogView.findViewById(R.id.timePickerLayout);
        AlertDialog dialog = builder.create();
        txtPartySize.setText(String.valueOf(current_party_size));
        txtTime.setText(this.time);
        openHour = Integer.parseInt(this.open_time.split(":")[0]);


        // generate some radio buttons
        for (int i = 1; i <= max_party_size; i++) {
            RadioButton radioButton = new RadioButton(context);
            radioButton.setText(String.valueOf(i));
            radioButton.setTextSize(text_size);
            radioButton.setTooltipText(String.valueOf(i));
            radioButton.setPadding(0,0,8,0);
            radioButton.setOnClickListener(view -> txtPartySize.setText(radioButton.getText()));
            radioGroup.addView(radioButton);
        }
        radioGroup.check(radioGroup.getChildAt(current_party_size - 1).getId());


        /*
          GENERATE DATES
         */
        SimpleDateFormat sdf_date = new SimpleDateFormat("EEE, MMM d", Locale.US);
        SimpleDateFormat displayDateFormat = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date startdate = new Date();
        try {
            txtDate.setText(displayDateFormat.format(Objects.requireNonNull(dateFormat.parse(this.date_string))));
            txtDate.setTooltipText(sdf_date.format(Objects.requireNonNull(dateFormat.parse(this.date_string))));
            dateToPass = dateFormat.parse(this.date_string);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        SimpleDateFormat sdf_time = new SimpleDateFormat("h:mm a", Locale.US);
        Date endTime;
        try {
            endTime = sdf_time.parse(this.close_time);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        assert endTime != null;
        workingCalendar.setTime(endTime);
//        int closeHour = workingCalendar.get(Calendar.HOUR_OF_DAY);

        calendar = Calendar.getInstance();
        calendar.setTime(startdate);
        if (this.dateChangedFlag){
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        startdate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, book_to_days_in_advance);
        Date endDate = calendar.getTime();

        generate_times(context, txtTime, txtDate,timePickerLayout);

        while (startdate.before(endDate)) {
            calendar.setTime(startdate);
            // set textView text
            TextView dateText = new TextView(context);
            dateText.setText(displayDateFormat.format(startdate));
            dateText.setTooltipText(sdf_date.format(startdate));
            dateText.setTextSize(text_size);
            dateText.setTag(startdate);
            dateText.setOnClickListener(view -> {
                txtDate.setText(dateText.getText());
                // a way to save the year since year gets lost with parsing
                dateToPass = (Date) dateText.getTag();
                // update the times based on date
                generate_times(context, txtTime, dateText, timePickerLayout);

            });
            // go to next time interval
            datePickerLayout.addView(dateText);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            startdate = calendar.getTime();
        }

        // OK & CANCEL listeners
        okButton.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId != -1) {
                String partySize = (String) radioGroup.findViewById(selectedId).getTooltipText();
                if (listener != null) {
                    // Validate time
                    calendar.setTime(dateToPass);

                    String amPm = txtTime.getText().toString().split(" ")[1];
                    if (amPm.equals("AM")) {
                        // Currently doesnt work for restaurants open later than midnight.
                        // too messy to fix now. solution is dont have close time after 12 :)
                        int selectedHour = Integer.parseInt(txtTime.getText().toString().split(":")[0]);
                        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                    }
                    else{
                        calendar.set(Calendar.HOUR_OF_DAY, 12 + Integer.parseInt(txtTime.getText().toString().split(":")[0]));
                    }
                    if (calendar.before(mainCalendar)){
                        Toast.makeText(context, getString(R.string.selectValidTime), Toast.LENGTH_SHORT).show();
                        return;
                    }else {
                        listener.onDialogComplete(partySize, dateToPass, String.valueOf(txtTime.getText()));
                    }
                }
                dialog.dismiss();
            }
        });
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    /**
     * Generate Times
     *
     */
    private void generate_times(Context context, TextView txtTime,TextView txtDate, LinearLayout timePickerLayout){
        // should not have objects created each time you click a date... :(
//        Log.d("generate_times", txtTime.getText() + "  " + txtDate.getText());
        SimpleDateFormat sdf_time = new SimpleDateFormat("h:mm a", Locale.US);
        SimpleDateFormat sdf_date = new SimpleDateFormat("EEE, MMM d", Locale.US);
//        SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.US);
        try {
            Date endTime = sdf_time.parse(this.close_time);
            assert endTime != null;
            workingCalendar.setTime(endTime);
            int closeHour = workingCalendar.get(Calendar.HOUR_OF_DAY);

//            Date startDateToCheck = sdf_date.parse(String.valueOf(txtDate.getText()));
            Date startDateToCheck = sdf_date.parse(String.valueOf(txtDate.getTooltipText()));
            // date is not today, show full hours
            workingCalendar = (Calendar) mainCalendar.clone();
            Date startTime;

            if(isDateToday(startDateToCheck)) {
                // date is today, show future hours
                workingCalendar.set(Calendar.MINUTE,0);
                workingCalendar.set(Calendar.SECOND,0);
                startTime = workingCalendar.getTime();
            }
            else{
                workingCalendar.set(Calendar.MINUTE,0);
                workingCalendar.set(Calendar.SECOND,0);
                workingCalendar.set(Calendar.HOUR_OF_DAY, openHour);
                startTime = workingCalendar.getTime();
            }

            timePickerLayout.removeAllViews();
            Calendar endCal = (Calendar) workingCalendar.clone();
            endCal.set(Calendar.HOUR_OF_DAY, closeHour);

            // if the restaurant is open till 12am or later. add a day to close
            if (closeHour < openHour){
                endCal.add(Calendar.HOUR_OF_DAY, 24);
            }
            endTime = endCal.getTime();
//            Log.d("GEN TIMES"," Generating Times:  startTime: "+ startTime + "  endTime: " + endTime+" openHour: " + openHour+" closeHour: " + closeHour );

            while (startTime.before(endTime)) {
//                 Log.d("GEN TIMES","generating  endCal " + endCal.getTime().toString() +" startTime "+ startTime + " endTime "+ endTime+ " workingCal "+ workingCalendar.getTime());
                workingCalendar.setTime(startTime);
                // set textView text
                TextView timeText = new TextView(context);
                timeText.setText(sdf_time.format(startTime));
                timeText.setTooltipText(sdf_time.format(startTime));
                timeText.setTextSize(text_size);
                timeText.setOnClickListener(view ->
                        txtTime.setText(timeText.getText()));

                // go to next time interval
                timePickerLayout.addView(timeText);
                workingCalendar.add(Calendar.MINUTE, intervalMinutes);
                startTime = workingCalendar.getTime();
//                Log.d("generate_times", txtTime.getText() + "  " + txtDate.getText());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    public boolean isDateToday(Date dateToCheck) {
        // Get the current date
        Calendar currentDate = Calendar.getInstance();
        // Create a Calendar instance for the date to check
        Calendar dateToCheckCal = Calendar.getInstance();
        dateToCheckCal.setTime(dateToCheck);
        // Compare month, and day
        return currentDate.get(Calendar.MONTH) == dateToCheckCal.get(Calendar.MONTH) &&
                currentDate.get(Calendar.DAY_OF_MONTH) == dateToCheckCal.get(Calendar.DAY_OF_MONTH);
    }
}