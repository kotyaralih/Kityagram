/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.BubbleCounterPath;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieImageView;

import java.util.Set;

public class DrawerActionCell extends FrameLayout {

    private ImageView imageView;
    private TextView textView;
    private int currentId;
    private RectF rect = new RectF();

    public DrawerActionCell(Context context) {
        super(context);

        imageView = new ImageView(context);
        imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_menuItemIcon), PorterDuff.Mode.SRC_IN));

        textView = new TextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        addView(imageView, LayoutHelper.createFrame(24, 24, Gravity.LEFT | Gravity.TOP, 19, 12, 0, 0));
        addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP, 72, 0, 16, 0));

        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (currentId == 8) {
            Set<String> suggestions = MessagesController.getInstance(UserConfig.selectedAccount).pendingSuggestions;
            if (suggestions.contains("VALIDATE_PHONE_NUMBER") || suggestions.contains("VALIDATE_PASSWORD")) {
                int countTop = AndroidUtilities.dp(12.5f);
                int countWidth = AndroidUtilities.dp(9);
                int countLeft = getMeasuredWidth() - countWidth - AndroidUtilities.dp(25);

                int x = countLeft - AndroidUtilities.dp(5.5f);
                rect.set(x, countTop, x + countWidth + AndroidUtilities.dp(14), countTop + AndroidUtilities.dp(23));
                Theme.chat_docBackPaint.setColor(Theme.getColor(Theme.key_chats_archiveBackground));
                canvas.drawRoundRect(rect, 11.5f * AndroidUtilities.density, 11.5f * AndroidUtilities.density, Theme.chat_docBackPaint);

                int w = Theme.dialogs_errorDrawable.getIntrinsicWidth();
                int h = Theme.dialogs_errorDrawable.getIntrinsicHeight();
                Theme.dialogs_errorDrawable.setBounds((int) (rect.centerX() - w / 2), (int) (rect.centerY() - h / 2), (int) (rect.centerX() + w / 2), (int) (rect.centerY() + h / 2));
                Theme.dialogs_errorDrawable.draw(canvas);
            }
        }

        if (currentId == 14) {
            int counter = MessagesStorage.getInstance(UserConfig.selectedAccount).getArchiveUnreadCount();
            if (counter <= 0) {
                return;
            }

            String text = String.valueOf(counter);
            int countTop = AndroidUtilities.dp(12.5f);
            int textWidth = (int) Math.ceil(Theme.dialogs_countTextPaint.measureText(text));
            int countWidth = Math.max(AndroidUtilities.dp(10), textWidth);
            int countLeft = getMeasuredWidth() - countWidth - AndroidUtilities.dp(25);

            int x = countLeft - AndroidUtilities.dp(5.5f);
            rect.set(x, countTop, x + countWidth + AndroidUtilities.dp(14), countTop + AndroidUtilities.dp(23));

            @SuppressLint("DrawAllocation") RectF counterPathRect = new RectF();
            @SuppressLint("DrawAllocation") Path counterPath = new Path();
            if (counterPath == null || counterPathRect == null || !counterPathRect.equals(rect)) {
                if (counterPathRect == null) {
                    counterPathRect = new RectF(rect);
                } else {
                    counterPathRect.set(rect);
                }
                if (counterPath == null) {
                    counterPath = new Path();
                }
                BubbleCounterPath.addBubbleRect(counterPath, counterPathRect, AndroidUtilities.dp(11.5f));
            }
            //canvas.drawRoundRect(rect, 11.5f * AndroidUtilities.density, 11.5f * AndroidUtilities.density, Theme.dialogs_countGrayPaint);
            canvas.drawPath(counterPath, Theme.dialogs_countGrayPaint);
            canvas.drawText(text, rect.left + (rect.width() - textWidth) / 2, countTop + AndroidUtilities.dp(16), Theme.dialogs_countTextPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        textView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
    }

    public void setTextAndIcon(int id, String text, int resId) {
        currentId = id;
        try {
            textView.setText(text);
            imageView.setImageResource(resId);
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    public void updateTextAndIcon(String text, int resId) {
        try {
            textView.setText(text);
            imageView.setImageResource(resId);
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    public ImageView getImageView() {
        return imageView;
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName("android.widget.Button");
        info.addAction(AccessibilityNodeInfo.ACTION_CLICK);
        info.addAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
        info.setText(textView.getText());
        info.setClassName(TextView.class.getName());
    }
}
