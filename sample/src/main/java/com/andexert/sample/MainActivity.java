package com.andexert.sample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.andexert.calendarlistview.library.DayPickerView;
import com.andexert.calendarlistview.library.SimpleMonthAdapter;

public class MainActivity extends Activity implements com.andexert.calendarlistview.library.DatePickerController {

    private DayPickerView dayPickerView;
    private View goneV;
    private boolean isOnly = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        goneV = findViewById(R.id.gone_v);
        View sureV = findViewById(R.id.sure);
        sureV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        dayPickerView = (DayPickerView) findViewById(R.id.pickerView);
        dayPickerView.setController(this, "zh");
        //Toast.makeText(this, "请选择您的入住日期", Toast.LENGTH_LONG).show();

        //TODO 监听确定按钮, 点击之后传送两个日期, 之后发送给网页
        new MilesHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                Toast.makeText(MainActivity.this, "请选择您的入住日期", Toast.LENGTH_LONG).show();
                showTip(goneV, null);//todo 不建议使用popupWindow, 因为妨碍输入
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public int getMaxYear() {
        return 2017 * 2017;
    }

    @Override
    public void onDayOfMonthSelected(int year, int month, int day) {
        //todo 隐藏/销毁 第一次的toast(往左下方移动);
        Log.e("Day Selected", day + " / " + month + " / " + year);
        if (isOnly) {
            showTip(goneV, "请选择您的入住日期");
            isOnly = false;
        }
    }

    @Override
    public void onDateRangeSelected(SimpleMonthAdapter.SelectedDays<SimpleMonthAdapter.CalendarDay> selectedDays) {
        //todo 隐藏/销毁 第二次的toast(往左下方移动); 并且使确定按钮可用;
        Log.e("Date range selected", selectedDays.getFirst().toString() + " --> " + selectedDays.getLast().toString());
    }
}
