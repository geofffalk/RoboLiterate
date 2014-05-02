/**
 * Copyright (C) 2013 Geoffrey Falk
 */

package org.maskmedia.roboliterate.uicontroller;

import android.content.Context;
import android.content.res.Resources;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import org.maskmedia.roboliterate.R;

/**
 * Customized TextView which acts as program Console, giving information on execution status. Each type of information
 * is colour coded.
 */
public class ConsoleTextView extends TextView {


    protected static final int INFO = 0;
    protected static final int ERROR = 1;
    protected static final int EXECUTION = 2;
    protected static final int LOADING = 3;

    protected static final int APPEND_NEWLINE=10;
    protected static final int APPEND_SPACE=11;
    protected static final int APPEND_NONE=12;
    protected static final int TRIM=13;

    private Context mContext;

    public ConsoleTextView(Context context) {
        super(context);
        mContext= context;
        setMovementMethod(new ScrollingMovementMethod());
    }

    public ConsoleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext= context;
        setMovementMethod(new ScrollingMovementMethod());
    }

    public ConsoleTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext= context;
        setMovementMethod(new ScrollingMovementMethod());
    }

    /**
     * Update textfield
     * @param type      int - type of information that is being added
     * @param s         String - the text of the information
     * @param append    int - flag with information about what to finally append - a newline or a space
     * @return          boolean - success
     */
    protected boolean update(int type, String s, int append) {

        int color;
        String prefix="";
        Resources res = mContext.getResources();
        switch (type) {
            case INFO:
                color = res.getColor(R.color.color1a);
                break;
            case ERROR:
                color = res.getColor(R.color.color5a);
                prefix = "ERROR: ";
                break;
            case EXECUTION:
                color = res.getColor(R.color.color3a);
                prefix = "";
                break;
            case LOADING:
                color = res.getColor(R.color.color2);
                break;
            default:
                color = res.getColor(R.color.color1a);
                break;

        }

        // append new text as a spannableString with bespoke color

        Spannable spannableString = new SpannableString(prefix +s);
        spannableString.setSpan(new ForegroundColorSpan(color), 0, spannableString.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        append(spannableString);

        // final append according to 'append' parameter

        switch (append) {
            case APPEND_NEWLINE:
            append("\n");
                break;
            case APPEND_SPACE:
                append(" ");
                break;
            case APPEND_NONE:
                break;
            case TRIM:
                CharSequence text = getText();
                if (text!=null && text.length()>=1)
                setText(text.subSequence(0, text.length() - 1));

        }

        // auto-scroll textfield to the bottom

        post(new Runnable() {

            @Override
            public void run() {
                Layout layout = getLayout();
                if (layout!=null) {
                final int scrollAmount = layout.getLineTop(getLineCount()) - getHeight();
                if (scrollAmount>0) scrollTo(0,scrollAmount);
                else scrollTo(0,0);
                }
            }
        });


        return true;
    }

    protected boolean update(int type, int resource, int append) {
        String s = mContext.getResources().getString(resource);
  return update(type, s, append);
    }



    protected void clear() {
        setText("");
    }

    protected void lineFeed(int lines) {
        while (lines-->0) {
        append("\n");
        }
    }

    /**
     * Append a percentage to textfield to show state of uploading file
     * @param percent   int - percentage uploaded
     */
    public void appendPercentage(int percent) {
        StringBuilder spacer = new StringBuilder("");
        if (percent<100) spacer.append(" ");
        if (percent<10) spacer.append(" ");
        spacer.append(percent).append("%");

        update(LOADING, spacer.toString(),(percent==100)? APPEND_NEWLINE : APPEND_SPACE);

    }

    /**
     * refresh an already added percent with value
     * @param percent   int -   percentage uploaded
     */
    public void refreshPercentage(int percent) {

        CharSequence currentText = getText();
        CharSequence newText = null;
        if (currentText != null) {
            newText = currentText.subSequence(0, currentText.length()-5);
        }
        setText(newText);
        appendPercentage(percent);

    }
}
