package com.andexert.sample;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.andexert.calendarlistview.library.DayPickerView;
import com.andexert.calendarlistview.library.SimpleMonthAdapter;

/**
 * 这个是单选日历输入:
 * dayPickerView.setController(this, "{\n" +
 * "\tstartTime:'2017-08-022',\n" +
 * "\tendTime:'2017-08-30',\n" +
 * "\tunUse:['2017-08-23','2017-08-24',]\n" +
 * "}", "zh");
 */
public class MainActivity extends Activity implements com.andexert.calendarlistview.library.DatePickerController {

    private DayPickerView dayPickerView;
    private View goneV;
    private boolean isOnly = true;
    private String msg;
    private View sureV;
    private PopupWindow popupWindow;
    private PopupWindow popupWindow1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        goneV = findViewById(R.id.gone_v);
        sureV = findViewById(R.id.sure);
        sureV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Hello man! Selected Date(s) is: " + msg, Toast.LENGTH_SHORT).show();
            }
        });
        sureV.setEnabled(false);
        dayPickerView = (DayPickerView) findViewById(R.id.pickerView);
        dayPickerView.setController(this, new int[3], "zh");

        new MilesHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                popupWindow = showTip(goneV, "请选择您的入住日期");
            }
        }, 200);
    }

    public PopupWindow showTip(View anchorView, String txt) {
        final View contentView = LayoutInflater.from(anchorView.getContext()).inflate(R.layout.popup_empty_content_layout, null);
        if (txt != null) {
            TextView tipTv = (TextView) contentView.findViewById(R.id.toast_txt_tv);
            tipTv.setText(txt);
        }
        final PopupWindow popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        popupWindow.setTouchable(false);
        popupWindow.showAsDropDown(anchorView);
        return popupWindow;
    }

    @Override
    public int getMaxYear() {
        return 2017 * 2017;
    }

    @Override
    public void onDayOfMonthSelected(int year, int month, int day) {
        if (isOnly) {
            if (popupWindow != null) popupWindow.dismiss();
            popupWindow1 = showTip(goneV, "请选择您的离开日期");
            isOnly = false;
        }
        Log.e("Day Selected", day + " / " + month + " / " + year);
    }

    @Override
    public void onDateRangeSelected(SimpleMonthAdapter.SelectedDays<SimpleMonthAdapter.CalendarDay> selectedDays) {
        msg = selectedDays.getFirst().toString() + " --> " + selectedDays.getLast().toString();
        Log.e("Date range selected", msg);
        if (popupWindow1 != null) popupWindow1.dismiss();
        sureV.setEnabled(true);
        sureV.setBackgroundColor(Color.parseColor("#ffff6827"));
    }
}
