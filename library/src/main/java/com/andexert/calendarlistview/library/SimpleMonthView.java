/***********************************************************************************
 * The MIT License (MIT)

 * Copyright (c) 2014 Robin Chutaux

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ***********************************************************************************/
package com.andexert.calendarlistview.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;

//todo 今天之前不可选(灰色), 开始日期/结束日期是否分步执行
class SimpleMonthView extends View {

    public static final String VIEW_PARAMS_HEIGHT = "height";
    public static final String VIEW_PARAMS_MONTH = "month";
    public static final String VIEW_PARAMS_YEAR = "year";
    public static final String VIEW_PARAMS_SELECTED_BEGIN_DAY = "selected_begin_day";
    public static final String VIEW_PARAMS_SELECTED_LAST_DAY = "selected_last_day";
    public static final String VIEW_PARAMS_SELECTED_BEGIN_MONTH = "selected_begin_month";
    public static final String VIEW_PARAMS_SELECTED_LAST_MONTH = "selected_last_month";
    public static final String VIEW_PARAMS_SELECTED_BEGIN_YEAR = "selected_begin_year";
    public static final String VIEW_PARAMS_SELECTED_LAST_YEAR = "selected_last_year";
    public static final String VIEW_PARAMS_WEEK_START = "week_start";

    protected static final int DEFAULT_NUM_ROWS = 6;
    protected static int DAY_ROW_MARGIN = 1;
    protected static int MIN_HEIGHT = 10;
    protected static int DEFAULT_HEIGHT = 32;
    protected static int DAY_NUM_HALF_WIDTH;
    protected static int DAY_SEPARATOR_WIDTH = 1;
    protected static int DAY_DIGIT_SIZE;
    protected static int WEEK_NAME_BAR_SIZE;
    protected static int MONTH_HEADER_HEIGHT;
    protected static int MONTH_TITLE_SIZE;
    protected final Boolean mDrawRect;
    protected final Boolean isPrevDayEnabled;
    protected final Calendar today;
    protected final Calendar mCalendar;
    protected final Calendar mDayLabelCalendar;
    protected final StringBuilder mStringBuilder;
    protected Calendar mMinSelectableDay;
    protected Calendar mMaxSelectableDay;
    protected Paint mWeekPaint;
    protected Paint mDayDigitPaint;
    protected Paint mMonthTitlePaint;
    protected Paint mSelectedPointPaint;
    protected Paint mSelectedCirclePaint;
    protected int mCurrentDayTextColor;
    protected int mMonthTextColor;
    protected int mWeekTextColor;
    protected int mDayDigitTextColor;
    protected int mLinkedDayBgColor;
    protected int mPreviousDayTextColor;
    protected int mSelectedDayDigitColor;
    protected boolean mHasToday = false;
    protected boolean mIsPrev = false;
    protected int mSelectedBeginDay = -1;
    protected int mSelectedLastDay = -1;
    protected int mSelectedBeginMonth = -1;
    protected int mSelectedLastMonth = -1;
    protected int mSelectedBeginYear = -1;
    protected int mSelectedLastYear = -1;
    protected int mToday = -1;
    protected int mWeekStart = 1;
    protected int mNumDays = 7;
    protected int mNumCells = mNumDays;
    protected int mDayOfWeekStart = 0;
    protected int mPadding = 0;
    protected int mNumRows = DEFAULT_NUM_ROWS;
    protected int mMonthDivHeight = DEFAULT_HEIGHT;
    protected int mMonth;
    protected int mWidth;
    protected int mYear;
    private String mDayOfWeekTypeface;
    private String mMonthTitleTypeface;
    private DateFormatSymbols mDateFormatSymbols = new DateFormatSymbols();
    private OnDayClickListener mOnDayClickListener;

    private boolean isRuleJson = false;
    private LinkedList<UnUseDay> unUseDays = new LinkedList<>();

    //增加特殊规则的构造方法
    public SimpleMonthView(Context context, String ruleJson, TypedArray typedArray) {
        this(context, new int[3], ruleJson, typedArray);
    }

    public SimpleMonthView(Context context, int dateArr[], String json, TypedArray typedArray) {
        super(context);
        mDayLabelCalendar = Calendar.getInstance();
        mCalendar = Calendar.getInstance();
        today = Calendar.getInstance();//new Time(Time.getCurrentTimezone());
        //today.setToNow();
        mMinSelectableDay = Calendar.getInstance();
        if (!TextUtils.isEmpty(json)) {
            isRuleJson = true;
            //{startTime:'2017-08-01',endTime:'2017-08-08',unUse:['2017-08-03','2017-08-04',]}
            try {
                JSONObject obj = new JSONObject(json);
                String minDay = obj.optString("startTime");
                String maxDay = obj.optString("endTime");
                String[] sArr = minDay.split("-");
                String[] eArr = maxDay.split("-");
                int[] siArr = new int[3];
                int[] eiArr = new int[3];
                for (int i = 0; i < sArr.length; i++) {
                    if (i < 3) siArr[i] = Integer.parseInt(sArr[i]);
                }
                mMinSelectableDay.set(Calendar.YEAR, siArr[0]);
                mMinSelectableDay.set(Calendar.MONTH, siArr[1] - 1);
                mMinSelectableDay.set(Calendar.DAY_OF_MONTH, siArr[2]);

                for (int i = 0; i < eArr.length; i++) {
                    if (i < 3) eiArr[i] = Integer.parseInt(eArr[i]);
                }
                mMaxSelectableDay = Calendar.getInstance();
                mMaxSelectableDay.set(Calendar.YEAR, eiArr[0]);
                mMaxSelectableDay.set(Calendar.MONTH, eiArr[1] - 1);
                mMaxSelectableDay.set(Calendar.DAY_OF_MONTH, eiArr[2]);

                //un use days
                JSONArray array = obj.optJSONArray("unUse");
                int len = array.length();
                String[] unArr = null;
                String unStr = null;
                UnUseDay unUseDay = null;
                for (int i = 0; i < len; i++) {
                    unStr = array.getString(i);
                    unArr = unStr.split("-");
                    if (unArr.length == 3) {
                        unUseDay = new UnUseDay();
                        unUseDay.setYear(Integer.parseInt(unArr[0]));
                        unUseDay.setMonth(Integer.parseInt(unArr[1]) - 1);
                        unUseDay.setDay(Integer.parseInt(unArr[2]));
                        unUseDays.add(unUseDay);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (dateArr != null && dateArr.length > 2) {
            if (dateArr[0] != 0 && dateArr[1] > 0 && dateArr[2] != 0) {
                //Log.d("sie", "arr2 - " + dateArr[2]);
                //mMinSelectableDay.set(dateArr[0], dateArr[1] - 1, dateArr[2]);
                mMinSelectableDay.set(Calendar.YEAR, dateArr[0]);
                mMinSelectableDay.set(Calendar.MONTH, dateArr[1] - 1);
                mMinSelectableDay.set(Calendar.DAY_OF_MONTH, dateArr[2]);
            }
        }

        setBackgroundColor(0xffffffff);
        mDayOfWeekTypeface = context.getString(R.string.sans_serif);
        mMonthTitleTypeface = context.getString(R.string.sans_serif);

        int defClr = ContextCompat.getColor(context, R.color.def_txt_clr);
        mCurrentDayTextColor = typedArray.getColor(R.styleable.DayPickerView_colorCurrentDay, defClr);
        mMonthTextColor = typedArray.getColor(R.styleable.DayPickerView_colorMonthName, defClr);
        mWeekTextColor = typedArray.getColor(R.styleable.DayPickerView_colorDayName, 0xff7b7b7b);
        mDayDigitTextColor = typedArray.getColor(R.styleable.DayPickerView_colorNormalDay, defClr);
        mPreviousDayTextColor = typedArray.getColor(R.styleable.DayPickerView_colorPreviousDay, 0xffc1c1c1);
        mLinkedDayBgColor = typedArray.getColor(R.styleable.DayPickerView_colorLinkedDaysBg, ContextCompat.getColor(context, R.color.linked_bg_clr));
        mSelectedDayDigitColor = typedArray.getColor(R.styleable.DayPickerView_colorSelectedDayText, 0xffffffff);

        DAY_DIGIT_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_textSizeDay, getResources().getDimensionPixelSize(R.dimen.day_digit_size));
        MONTH_TITLE_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_textSizeMonth, getResources().getDimensionPixelSize(R.dimen.month_year_title_size));
        WEEK_NAME_BAR_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_textSizeDayName, getResources().getDimensionPixelSize(R.dimen.text_size_day_name));
        MONTH_HEADER_HEIGHT = typedArray.getDimensionPixelOffset(R.styleable.DayPickerView_headerMonthHeight, getResources().getDimensionPixelOffset(R.dimen.header_month_height));
        DAY_NUM_HALF_WIDTH = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_dayNumHalfWidth, getResources().getDimensionPixelOffset(R.dimen.day_num_half_width));
        DAY_ROW_MARGIN = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        mMonthDivHeight = ((typedArray.getDimensionPixelSize(R.styleable.DayPickerView_calendarHeight, getResources().getDimensionPixelOffset(R.dimen.calendar_height)) - MONTH_HEADER_HEIGHT) / 6);
        isPrevDayEnabled = typedArray.getBoolean(R.styleable.DayPickerView_enablePreviousDay, false);
        mDrawRect = typedArray.getBoolean(R.styleable.DayPickerView_drawRoundRect, true);

        mStringBuilder = new StringBuilder(50);
        initView();
    }

    private int calculateNumRows() {
        int offset = findDayOffset();
        int dividend = (offset + mNumCells) / mNumDays;
        int remainder = (offset + mNumCells) % mNumDays;
        return (dividend + (remainder > 0 ? 1 : 0));
    }

    /*private enum DayOfWeek {
        SUN(0, "周日"), MON(1, "周一"), TUE(2, "周二"), WED(3, "周三"), THU(4, "周四"), FRI(5, "周五"), SAT(6, "周六");

        private int value;
        private String day;

        DayOfWeek(int value, String day) {
            this.value = value;
            this.day = day;
        }

        public String getDay() {
            return day;
        }

        public int getValue() {
            return value;
        }

        //根据枚举的int值取出string值
        public String getWeekTxt(int i) {
            if (i < 0 || i > 6) return "";
            return values()[i].day;
        }
    }*/

    private void drawMonthDayLabels(Canvas canvas) {
        //int y = MONTH_HEADER_HEIGHT - (WEEK_NAME_BAR_SIZE / 2);
        int y = MONTH_HEADER_HEIGHT + (WEEK_NAME_BAR_SIZE / 2);
        int dayWidthHalf = (mWidth - mPadding * 2) / (mNumDays * 2);
        String[] shortWeekdays = mDateFormatSymbols.getShortWeekdays();
        for (int i = 0; i < mNumDays; i++) {
            int calendarDay = (i + mWeekStart) % mNumDays;
            int x = (2 * i + 1) * dayWidthHalf + mPadding;
            mDayLabelCalendar.set(Calendar.DAY_OF_WEEK, calendarDay);
            //Log.d("sie", "星期的文字 -- " + shortWeekdays[mDayLabelCalendar.get(Calendar.DAY_OF_WEEK)].toUpperCase(Locale.getDefault()));
            canvas.drawText(shortWeekdays[mDayLabelCalendar.get(Calendar.DAY_OF_WEEK)].toUpperCase(Locale.getDefault()), x, y, mWeekPaint);
        }
    }


    private void drawMonthTitle(Canvas canvas) {
        int x = (mWidth + 2 * mPadding) / 2;
        int y = (MONTH_HEADER_HEIGHT - WEEK_NAME_BAR_SIZE) / 2 + (MONTH_TITLE_SIZE / 3);
        StringBuilder stringBuilder = new StringBuilder(getMonthAndYearString().toLowerCase());
        //Log.d("sie", "月份的文字 -- " + stringBuilder.toString());
        //Log.d("sie", "月/年  -- " + (mCalendar.get(Calendar.MONTH) + 1) + "<-> " + mCalendar.get(Calendar.YEAR));
        stringBuilder.setCharAt(0, Character.toUpperCase(stringBuilder.charAt(0)));
        canvas.drawText(stringBuilder.toString(), x, y, mMonthTitlePaint);
    }

    private int findDayOffset() {
        return (mDayOfWeekStart < mWeekStart ? (mDayOfWeekStart + mNumDays) : mDayOfWeekStart)
                - mWeekStart;
    }

    private String getMonthAndYearString() {
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NO_MONTH_DAY;
        mStringBuilder.setLength(0);
        long millis = mCalendar.getTimeInMillis();
        return DateUtils.formatDateRange(getContext(), millis, millis, flags);
    }

    /*private String getMonthString() {
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_NO_MONTH_DAY;
        long millis = mCalendar.getTimeInMillis();
        return DateUtils.formatDateRange(getContext(), millis, millis, flags);
    }*/

    private void onDayClick(SimpleMonthAdapter.CalendarDay calendarDay) {
        /*if (mOnDayClickListener != null && (isPrevDayEnabled || !((calendarDay.month == mMinSelectableDay.get(Calendar.MONTH)) && (calendarDay.year == mMinSelectableDay.get(Calendar.YEAR)) && calendarDay.day < mMinSelectableDay.get(Calendar.DAY_OF_MONTH)))) {
            mOnDayClickListener.onDayClick(this, calendarDay);
        }*/
        if (isRuleJson) {
            if (mOnDayClickListener != null & durDay(calendarDay.day, mMinSelectableDay, mMaxSelectableDay)) {
                mOnDayClickListener.onDayClick(this, calendarDay);
            }
        } else {
            if (mOnDayClickListener != null && (!isPrevDayEnabled && !prevDay(calendarDay.day, mMinSelectableDay))) {
                //Log.d("sie", "clickable day - " + calendarDay.day);
                mOnDayClickListener.onDayClick(this, calendarDay);
            }
        }
    }

    private boolean sameDay(int monthDay, Calendar time) {
        return (mYear == time.get(Calendar.YEAR)) && (mMonth == time.get(Calendar.MONTH)) && (monthDay == time.get(Calendar.DAY_OF_MONTH));
    }

    private boolean prevDay(int monthDay, Calendar time) {
        return ((mYear < time.get(Calendar.YEAR))) || (mYear == time.get(Calendar.YEAR) && mMonth < time.get(Calendar.MONTH)) ||
                (mMonth == time.get(Calendar.MONTH) && monthDay < time.get(Calendar.DAY_OF_MONTH));
    }

    private boolean nextDay(int monthDay, Calendar time) {
        return ((mYear > time.get(Calendar.YEAR))) || (mYear == time.get(Calendar.YEAR) && mMonth > time.get(Calendar.MONTH)) ||
                (mMonth == time.get(Calendar.MONTH) && monthDay > time.get(Calendar.DAY_OF_MONTH));
    }

    private boolean durDay(int monthDay, Calendar start, Calendar end) {
        boolean cantClick = false;
        int len = unUseDays.size();
        UnUseDay unUseDay = null;
        for (int i = 0; i < len; i++) {
            unUseDay = unUseDays.get(i);
            if (unUseDay.getDay() == monthDay && unUseDay.getMonth() == mMonth && unUseDay.getYear() == mYear) {
                cantClick = true;
                break;
            }
        }
        return !prevDay(monthDay, start) && !nextDay(monthDay, end) && !cantClick;
    }

    protected void drawMonthNums(Canvas canvas) {
        int y = (mMonthDivHeight + DAY_DIGIT_SIZE) / 2 - DAY_SEPARATOR_WIDTH + MONTH_HEADER_HEIGHT + DAY_ROW_MARGIN;
        int paddingDay = (mWidth - 2 * mPadding) / (2 * mNumDays);
        int dayOffset = findDayOffset();
        int day = 1;

        while (day <= mNumCells) {
            //Log.d("sie", "------------------- " + day + "------------------- ");
            int x = paddingDay * (1 + dayOffset * 2) + mPadding;
            if (isRuleJson) {
                if ((mMonth == mSelectedBeginMonth && mSelectedBeginDay == day && mSelectedBeginYear == mYear)
                        || (mMonth == mSelectedLastMonth && mSelectedLastDay == day && mSelectedLastYear == mYear)) {
                    if (mDrawRect) {
                        float[] radii = {20.0f, 20.0f, 20.0f, 20.0f, 20.0f, 20.0f, 20.0f, 20.0f};
                        RectF rectF = new RectF(x - DAY_NUM_HALF_WIDTH, (y - DAY_DIGIT_SIZE / 3) - DAY_NUM_HALF_WIDTH, x + DAY_NUM_HALF_WIDTH, (y - DAY_DIGIT_SIZE / 3) + DAY_NUM_HALF_WIDTH);
                        //canvas.drawRoundRect(rectF, 10.0f, 10.0f, mSelectedCirclePaint);
                        Path path = new Path();
                        path.addRoundRect(rectF, radii, Path.Direction.CW);
                        canvas.drawPath(path, mSelectedPointPaint);
                    }
                }
            } else {
                if ((mMonth == mSelectedBeginMonth && mSelectedBeginDay == day && mSelectedBeginYear == mYear) || (mMonth == mSelectedLastMonth && mSelectedLastDay == day && mSelectedLastYear == mYear)) {
                    if (mDrawRect) {
                        //Log.d("sie", "day num 0 - origin = " + mYear + ", " + mMonth + ", " + day);
//                    Log.d("sie", "begin - slt " + mSelectedBeginYear + ", " + mSelectedBeginMonth + ", " + mSelectedBeginDay);
//                    Log.d("sie", "last - slt " + mSelectedLastYear + ", " + mSelectedLastMonth + ", " + mSelectedLastDay);
                        RectF rectF = new RectF(x - DAY_NUM_HALF_WIDTH, (y - DAY_DIGIT_SIZE / 3) - DAY_NUM_HALF_WIDTH, x + DAY_NUM_HALF_WIDTH, (y - DAY_DIGIT_SIZE / 3) + DAY_NUM_HALF_WIDTH);
                        //canvas.drawRoundRect(rectF, 10.0f, 10.0f, mSelectedCirclePaint);
                        Path path = new Path();
                        //todo 第二步选好之后, 不允许修改第一步开始日期, 只能重至结束日期----需要修改代码
                        if (mSelectedLastDay != -1 && mSelectedLastDay == day && (
                                (mSelectedLastYear > mSelectedBeginYear) || (mSelectedLastYear == mSelectedBeginYear && mSelectedLastMonth > mSelectedBeginMonth) ||
                                        (mSelectedLastYear == mSelectedBeginYear && mSelectedBeginMonth == mSelectedLastMonth && mSelectedLastDay >= mSelectedBeginDay))
                                ) {//结束日期
                            if (mSelectedLastDay != -1 && day == mSelectedLastDay && mSelectedLastDay == mSelectedBeginDay) {
                                //左上角，右上角，右下角，左下角xy半径
                                float[] radii = {20.0f, 20.0f, 20.0f, 20.0f, 20.0f, 20.0f, 20.0f, 20.0f};
                                path.addRoundRect(rectF, radii, Path.Direction.CW);
                                if (mHasToday && (mToday == day)) {
                                    flagDate(canvas, "/" + getContext().getString(R.string.in_out_txt), 0xffff6827, y, x + 4 + 2 * DAY_NUM_HALF_WIDTH);
                                } else {
                                    flagDate(canvas, getContext().getString(R.string.in_out_txt), 0xffff6827, y, x);
                                }
                            } else {
                                float[] radii = {0f, 0f, 20.0f, 20.0f, 20.0f, 20.0f, 0f, 0f};
                                path.addRoundRect(rectF, radii, Path.Direction.CW);
                                flagDate(canvas, getContext().getString(R.string.check_out_txt), 0xffff6827, y, x);
                            }
                        } else {//开始日期
                            if (mSelectedLastDay != -1 && mSelectedBeginDay == day && (
                                    (mSelectedLastYear < mSelectedBeginYear) || (mSelectedLastYear == mSelectedBeginYear && mSelectedLastMonth < mSelectedBeginMonth) ||
                                            (mSelectedLastYear == mSelectedBeginYear && mSelectedBeginMonth == mSelectedLastMonth && mSelectedLastDay < mSelectedBeginDay))
                                    ) {
                                //结束位置
                                float[] radii = {0f, 0f, 20.0f, 20.0f, 20.0f, 20.0f, 0f, 0f};
                                path.addRoundRect(rectF, radii, Path.Direction.CW);
                                flagDate(canvas, getContext().getString(R.string.check_out_txt), 0xffff6827, y, x);
                            } else {//真正的开始位置
                                float[] radii = {20.0f, 20.0f, 0f, 0f, 0f, 0f, 20.0f, 20.0f};
                                path.addRoundRect(rectF, radii, Path.Direction.CW);
                                if (mHasToday && (mToday == day)) {
                                    flagDate(canvas, "/" + getContext().getString(R.string.check_in_txt), 0xffff6827, y, x - 20 + 2 * DAY_NUM_HALF_WIDTH);
                                } else {
                                    flagDate(canvas, getContext().getString(R.string.check_in_txt), 0xffff6827, y, x);
                                }
                            }
                        }
                        canvas.drawPath(path, mSelectedPointPaint);

                        if ((day == mSelectedLastDay && mSelectedBeginDay + 1 == day) || (day == mSelectedBeginDay && mSelectedLastDay + 1 == day)) {
                            //右边增加宽度
                            int rightBound = x - DAY_NUM_HALF_WIDTH;
                            int miniWid = 2 * (paddingDay - DAY_NUM_HALF_WIDTH);
                            RectF miniRF = new RectF(rightBound - miniWid, (y - DAY_DIGIT_SIZE / 3) - DAY_NUM_HALF_WIDTH,
                                    rightBound, (y - DAY_DIGIT_SIZE / 3) + DAY_NUM_HALF_WIDTH);
                            canvas.drawRect(miniRF, mSelectedCirclePaint);
                        }
                    } else {

                        canvas.drawCircle(x, y - DAY_DIGIT_SIZE / 3, DAY_NUM_HALF_WIDTH, mSelectedCirclePaint);
                    }
                }
            }
            if (mHasToday && (mToday == day)) {
                //todo 今日粗体字号
                flagDate(canvas, getContext().getString(R.string.today_txt), 0xff7b7b7b, y, x);
                mDayDigitPaint.setColor(mCurrentDayTextColor);
                mDayDigitPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            } else {
                mDayDigitPaint.setColor(mDayDigitTextColor);
                mDayDigitPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            }


            if ((mMonth == mSelectedBeginMonth && mSelectedBeginDay == day && mSelectedBeginYear == mYear) || (mMonth == mSelectedLastMonth && mSelectedLastDay == day && mSelectedLastYear == mYear))
                mDayDigitPaint.setColor(mSelectedDayDigitColor);

            if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 && mSelectedBeginYear == mSelectedLastYear &&
                    mSelectedBeginMonth == mSelectedLastMonth &&
                    mSelectedBeginDay == mSelectedLastDay &&
                    day == mSelectedBeginDay &&
                    mMonth == mSelectedBeginMonth &&
                    mYear == mSelectedBeginYear)) {
                //同一年,同一月,同一日
                //Log.d("sie", "day num 1 - " + day);
                //canvas.drawRect(linkRF, mSelectedCirclePaint);
                mDayDigitPaint.setColor(mSelectedDayDigitColor);
            }
            if (!isRuleJson) {
                int linkX = paddingDay * (1 + dayOffset * 2) + mPadding;
                RectF linkRF = new RectF(linkX - paddingDay, (y - DAY_DIGIT_SIZE / 3) - DAY_NUM_HALF_WIDTH,
                        linkX + paddingDay, (y - DAY_DIGIT_SIZE / 3) + DAY_NUM_HALF_WIDTH);
                if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 && mSelectedBeginYear == mSelectedLastYear && mSelectedBeginYear == mYear) &&
                        (((mMonth == mSelectedBeginMonth && mSelectedLastMonth == mSelectedBeginMonth) &&
                                ((mSelectedBeginDay < mSelectedLastDay && day > mSelectedBeginDay && day < mSelectedLastDay) || (mSelectedBeginDay > mSelectedLastDay && day < mSelectedBeginDay && day > mSelectedLastDay))) ||
                                ((mSelectedBeginMonth < mSelectedLastMonth && mMonth == mSelectedBeginMonth && day > mSelectedBeginDay) || (mSelectedBeginMonth < mSelectedLastMonth && mMonth == mSelectedLastMonth && day < mSelectedLastDay)) ||
                                ((mSelectedBeginMonth > mSelectedLastMonth && mMonth == mSelectedBeginMonth && day < mSelectedBeginDay) || (mSelectedBeginMonth > mSelectedLastMonth && mMonth == mSelectedLastMonth && day > mSelectedLastDay)))) {
                    //同一年,同一月
                    //Log.d("sie", "day num 2 - " + day);
                    if (day - 1 == mSelectedBeginDay || day - 1 == mSelectedLastDay) {
                        //左边增加宽度
                        int rightBound = linkX - paddingDay;
                        int miniWid = paddingDay - DAY_NUM_HALF_WIDTH;
                        RectF miniRF = new RectF(rightBound - miniWid, (y - DAY_DIGIT_SIZE / 3) - DAY_NUM_HALF_WIDTH,
                                rightBound, (y - DAY_DIGIT_SIZE / 3) + DAY_NUM_HALF_WIDTH);
                        canvas.drawRect(miniRF, mSelectedCirclePaint);
                    }
                    if (day + 1 == mSelectedLastDay || day + 1 == mSelectedBeginDay) {
                        //右边增加宽度
                        int leftBound = linkX + paddingDay;
                        int miniWid = paddingDay - DAY_NUM_HALF_WIDTH;
                        RectF miniRF = new RectF(leftBound, (y - DAY_DIGIT_SIZE / 3) - DAY_NUM_HALF_WIDTH,
                                leftBound + miniWid, (y - DAY_DIGIT_SIZE / 3) + DAY_NUM_HALF_WIDTH);
                        canvas.drawRect(miniRF, mSelectedCirclePaint);
                    }
                    canvas.drawRect(linkRF, mSelectedCirclePaint);
                    //mDayDigitPaint.setColor(mLinkedDayBgColor);
                }

                if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 && mSelectedBeginYear != mSelectedLastYear &&
                        ((mSelectedBeginYear == mYear && mMonth == mSelectedBeginMonth) || (mSelectedLastYear == mYear && mMonth == mSelectedLastMonth)) &&
                        (((mSelectedBeginMonth < mSelectedLastMonth && mMonth == mSelectedBeginMonth && day < mSelectedBeginDay) || (mSelectedBeginMonth < mSelectedLastMonth && mMonth == mSelectedLastMonth && day > mSelectedLastDay)) ||
                                ((mSelectedBeginMonth > mSelectedLastMonth && mMonth == mSelectedBeginMonth && day > mSelectedBeginDay) || (mSelectedBeginMonth > mSelectedLastMonth && mMonth == mSelectedLastMonth && day < mSelectedLastDay))))) {
                    //不在同一年
                    //Log.d("sie", "day num 3 - " + day);
                    canvas.drawRect(linkRF, mSelectedCirclePaint);
                    //mDayDigitPaint.setColor(mLinkedDayBgColor);
                }

                if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 && mSelectedBeginYear == mSelectedLastYear && mYear == mSelectedBeginYear) &&
                        ((mMonth > mSelectedBeginMonth && mMonth < mSelectedLastMonth && mSelectedBeginMonth < mSelectedLastMonth) ||
                                (mMonth < mSelectedBeginMonth && mMonth > mSelectedLastMonth && mSelectedBeginMonth > mSelectedLastMonth))) {
                    //同一年
                    //Log.d("sie", "day num 4 - " + day);
                    canvas.drawRect(linkRF, mSelectedCirclePaint);
                    //mDayDigitPaint.setColor(mLinkedDayBgColor);
                }

                if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 && mSelectedBeginYear != mSelectedLastYear) &&
                        ((mSelectedBeginYear < mSelectedLastYear && ((mMonth > mSelectedBeginMonth && mYear == mSelectedBeginYear) || (mMonth < mSelectedLastMonth && mYear == mSelectedLastYear))) ||
                                (mSelectedBeginYear > mSelectedLastYear && ((mMonth < mSelectedBeginMonth && mYear == mSelectedBeginYear) || (mMonth > mSelectedLastMonth && mYear == mSelectedLastYear))))) {
                    //不在同一年
                    //Log.d("sie", "day num 5 - " + day);
                    canvas.drawRect(linkRF, mSelectedCirclePaint);
                    //mDayDigitPaint.setColor(mLinkedDayBgColor);
                }
            }
            if (!isPrevDayEnabled && prevDay(day, mMinSelectableDay) /*&& mMinSelectableDay.get(Calendar.MONTH) == mMonth && mMinSelectableDay.get(Calendar.YEAR) == mYear*/) {
                //Log.d("sie", "gray - " + day);
                mDayDigitPaint.setColor(mPreviousDayTextColor);
                //mDayDigitPaint.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
            }
            if (isRuleJson && !isPrevDayEnabled) {
                if (nextDay(day, mMaxSelectableDay)) {
                    mDayDigitPaint.setColor(mPreviousDayTextColor);
                } else if (prevDay(day, mMaxSelectableDay)) {
                    int len = unUseDays.size();
                    UnUseDay unUseDay = null;
                    for (int i = 0; i < len; i++) {
                        unUseDay = unUseDays.get(i);
                        if (unUseDay.getYear() == mYear && unUseDay.getMonth() == mMonth &&
                                unUseDay.getDay() == day) {
                            mDayDigitPaint.setColor(mPreviousDayTextColor);
                        }
                    }
                }
            }
            canvas.drawText(String.format(Locale.getDefault(), "%d", day), x, y, mDayDigitPaint);

            dayOffset++;
            if (dayOffset == mNumDays) {
                dayOffset = 0;
                y += mMonthDivHeight;
            }
            day++;
            //Log.d("sie", "---------------------------------------------- ");
        }
    }

    private void flagDate(Canvas canvas, String flag, @ColorInt int clr, int y, int x) {
        Paint flagP = new Paint();
        flagP.setAntiAlias(true);
        float flagTS = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics());
        flagP.setTextSize(flagTS);
        flagP.setStyle(Style.FILL);
        flagP.setColor(clr);
        flagP.setTextAlign(Align.CENTER);
        flagP.setFakeBoldText(false);
        canvas.drawText(flag, x, y - DAY_ROW_MARGIN - DAY_NUM_HALF_WIDTH, flagP);
    }

    public SimpleMonthAdapter.CalendarDay getDayFromLocation(float x, float y) {
        int padding = mPadding;
        if ((x < padding) || (x > mWidth - mPadding)) {
            return null;
        }

        int yDay = (int) (y - MONTH_HEADER_HEIGHT) / mMonthDivHeight;
        int day = 1 + ((int) ((x - padding) * mNumDays / (mWidth - padding - mPadding)) - findDayOffset()) + yDay * mNumDays;

        if (mMonth > 11 || mMonth < 0 || CalendarUtils.getDaysInMonth(mMonth, mYear) < day || day < 1)
            return null;

        return new SimpleMonthAdapter.CalendarDay(mYear, mMonth, day);
    }

    protected void initView() {
        mMonthTitlePaint = new Paint();
        mMonthTitlePaint.setFakeBoldText(true);
        mMonthTitlePaint.setAntiAlias(true);
        mMonthTitlePaint.setTextSize(MONTH_TITLE_SIZE);
        mMonthTitlePaint.setTypeface(Typeface.create(mMonthTitleTypeface, Typeface.NORMAL));
        mMonthTitlePaint.setColor(mMonthTextColor);
        mMonthTitlePaint.setTextAlign(Align.CENTER);
        mMonthTitlePaint.setStyle(Style.FILL);

        mSelectedPointPaint = new Paint();
        mSelectedPointPaint.setFakeBoldText(true);
        mSelectedPointPaint.setAntiAlias(true);
        mSelectedPointPaint.setColor(0xffff6827);
        mSelectedPointPaint.setTextAlign(Align.CENTER);
        mSelectedPointPaint.setStyle(Style.FILL);

        mSelectedCirclePaint = new Paint();
        mSelectedCirclePaint.setFakeBoldText(true);
        mSelectedCirclePaint.setAntiAlias(true);
        mSelectedCirclePaint.setColor(mLinkedDayBgColor);
        mSelectedCirclePaint.setTextAlign(Align.CENTER);
        mSelectedCirclePaint.setStyle(Style.FILL);
        //mSelectedCirclePaint.setAlpha(SELECTED_CIRCLE_ALPHA);

        mWeekPaint = new Paint();
        mWeekPaint.setAntiAlias(true);
        mWeekPaint.setTextSize(WEEK_NAME_BAR_SIZE);
        mWeekPaint.setColor(mWeekTextColor);
        mWeekPaint.setTypeface(Typeface.create(mDayOfWeekTypeface, Typeface.NORMAL));
        mWeekPaint.setStyle(Style.FILL);
        mWeekPaint.setTextAlign(Align.CENTER);
        mWeekPaint.setFakeBoldText(true);

        mDayDigitPaint = new Paint();
        mDayDigitPaint.setAntiAlias(true);
        mDayDigitPaint.setTextSize(DAY_DIGIT_SIZE);
        mDayDigitPaint.setColor(mDayDigitTextColor);
        mDayDigitPaint.setStyle(Style.FILL);
        mDayDigitPaint.setTextAlign(Align.CENTER);
        mDayDigitPaint.setFakeBoldText(false);
    }

    protected void onDraw(Canvas canvas) {
        drawMonthTitle(canvas);
        drawMonthDayLabels(canvas);
        drawMonthNums(canvas);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mMonthDivHeight * mNumRows + MONTH_HEADER_HEIGHT);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
    }

    public boolean performClick() {
        //Calls the super implementation, which generates an AccessibilityEvent
        //and calls the onClick() listener on the view, if any
        return super.performClick();
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            SimpleMonthAdapter.CalendarDay calendarDay = getDayFromLocation(event.getX(), event.getY());
            if (calendarDay != null) {
                onDayClick(calendarDay);
            }
        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //Handle the action for the custom click here
            performClick();
        }
        return true;
    }

    public void reuse() {
        mNumRows = DEFAULT_NUM_ROWS;
        requestLayout();
    }

    @SuppressLint("WrongConstant")
    public void setMonthParams(HashMap<String, Integer> params) {
        if (!params.containsKey(VIEW_PARAMS_MONTH) && !params.containsKey(VIEW_PARAMS_YEAR)) {
            throw new InvalidParameterException("You must specify month and year for this view");
        }
        setTag(params);

        if (params.containsKey(VIEW_PARAMS_HEIGHT)) {
            mMonthDivHeight = params.get(VIEW_PARAMS_HEIGHT);
            if (mMonthDivHeight < MIN_HEIGHT) {
                mMonthDivHeight = MIN_HEIGHT;
            }
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_BEGIN_DAY)) {
            mSelectedBeginDay = params.get(VIEW_PARAMS_SELECTED_BEGIN_DAY);
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_LAST_DAY)) {
            mSelectedLastDay = params.get(VIEW_PARAMS_SELECTED_LAST_DAY);
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_BEGIN_MONTH)) {
            mSelectedBeginMonth = params.get(VIEW_PARAMS_SELECTED_BEGIN_MONTH);
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_LAST_MONTH)) {
            mSelectedLastMonth = params.get(VIEW_PARAMS_SELECTED_LAST_MONTH);
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_BEGIN_YEAR)) {
            mSelectedBeginYear = params.get(VIEW_PARAMS_SELECTED_BEGIN_YEAR);
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_LAST_YEAR)) {
            mSelectedLastYear = params.get(VIEW_PARAMS_SELECTED_LAST_YEAR);
        }

        mMonth = params.get(VIEW_PARAMS_MONTH);
        //Log.d("sie", "month from - " + mMonth);
        mYear = params.get(VIEW_PARAMS_YEAR);

        mHasToday = false;
        mToday = -1;

        mCalendar.set(Calendar.MONTH, mMonth);
        mCalendar.set(Calendar.YEAR, mYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        mDayOfWeekStart = mCalendar.get(Calendar.DAY_OF_WEEK);

        if (params.containsKey(VIEW_PARAMS_WEEK_START)) {
            mWeekStart = params.get(VIEW_PARAMS_WEEK_START);
        } else {
            mWeekStart = mCalendar.getFirstDayOfWeek();
        }

        mNumCells = CalendarUtils.getDaysInMonth(mMonth, mYear);
        for (int i = 0; i < mNumCells; i++) {
            final int day = i + 1;
            if (sameDay(day, today)) {
                mHasToday = true;
                mToday = day;
            }

            mIsPrev = prevDay(day, mMinSelectableDay);
        }

        mNumRows = calculateNumRows();
    }

    public void setOnDayClickListener(OnDayClickListener onDayClickListener) {
        mOnDayClickListener = onDayClickListener;
    }

    public interface OnDayClickListener {
        void onDayClick(SimpleMonthView simpleMonthView, SimpleMonthAdapter.CalendarDay calendarDay);
    }
}