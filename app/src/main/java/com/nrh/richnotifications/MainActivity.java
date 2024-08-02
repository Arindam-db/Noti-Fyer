package com.nrh.richnotifications;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private EditText messageEditText;
    private TimePicker timePicker;

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;
    private static final int REQUEST_SCHEDULE_EXACT_ALARM_PERMISSION = 2;

    private MaterialCheckBox repeatDailyCheckBox;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);

        messageEditText = findViewById(R.id.messageEditText);
        timePicker = findViewById(R.id.timePicker);
        repeatDailyCheckBox = findViewById(R.id.repeatDailyCheckBox);

        MaterialCheckBox repeatDailyCheckBox = findViewById(R.id.repeatDailyCheckBox);

        Button setNotificationButton = findViewById(R.id.setNotificationButton);

        // Check and request notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            }
        }

        setNotificationButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SCHEDULE_EXACT_ALARM}, REQUEST_SCHEDULE_EXACT_ALARM_PERMISSION);
                } else {
                    setNotification();
                }
            } else {
                setNotification();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_SCHEDULE_EXACT_ALARM_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setNotification();
            } else {
                Toast.makeText(this, "SCHEDULE_EXACT_ALARM permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setNotification() {
        String message = messageEditText.getText().toString();

        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // Adjust time if it's already past
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(MainActivity.this, NotificationReceiver.class);
        intent.putExtra("message", message);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (repeatDailyCheckBox.isChecked()) {
                // Set repeating alarm
                long repeatInterval = AlarmManager.INTERVAL_DAY; // Repeat daily
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), repeatInterval, pendingIntent);
                Toast.makeText(this, "Daily reminder set successfully!", Toast.LENGTH_SHORT).show();
            } else {
                // Set one-time alarm
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        Toast.makeText(this, "Reminder set successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Exact alarms are not supported on this device", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    Toast.makeText(this, "Reminder set successfully!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}