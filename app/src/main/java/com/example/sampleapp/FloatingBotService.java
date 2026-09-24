package com.example.sampleapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class FloatingBotService extends Service {

    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;

    private View layoutBubble;
    private View layoutChatScreen;
    private LinearLayout chatMessagesLayout;
    private ScrollView scrollChat;
    private EditText etChatInput;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotification();

        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_bot, null);

        int layoutType = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 50;
        params.y = 250;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(floatingView, params);

        layoutBubble = floatingView.findViewById(R.id.layout_bubble);
        layoutChatScreen = floatingView.findViewById(R.id.layout_chat_screen);
        chatMessagesLayout = (LinearLayout) floatingView.findViewById(R.id.chat_messages_layout);
        scrollChat = (ScrollView) floatingView.findViewById(R.id.scroll_chat);
        etChatInput = (EditText) floatingView.findViewById(R.id.et_chat_input);

        Button btnClose = (Button) floatingView.findViewById(R.id.btn_close_chat);
        Button btnSend = (Button) floatingView.findViewById(R.id.btn_send_chat);

        layoutBubble.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            private boolean isClick = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isClick = true;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        int diffX = (int) (event.getRawX() - initialTouchX);
                        int diffY = (int) (event.getRawY() - initialTouchY);
                        if (Math.abs(diffX) > 10 || Math.abs(diffY) > 10) {
                            isClick = false;
                        }
                        params.x = initialX + diffX;
                        params.y = initialY + diffY;
                        windowManager.updateViewLayout(floatingView, params);
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (isClick) {
                            openChatScreen();
                        }
                        return true;
                }
                return false;
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeChatScreen();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = etChatInput.getText().toString().trim();
                if (!msg.isEmpty()) {
                    addUserMessage(msg);
                    etChatInput.setText("");
                    processBotResponse(msg);
                }
            }
        });
    }

    private void openChatScreen() {
        layoutBubble.setVisibility(View.GONE);
        layoutChatScreen.setVisibility(View.VISIBLE);
        params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        windowManager.updateViewLayout(floatingView, params);
    }

    private void closeChatScreen() {
        layoutChatScreen.setVisibility(View.GONE);
        layoutBubble.setVisibility(View.VISIBLE);
        params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        windowManager.updateViewLayout(floatingView, params);
    }

    private void addUserMessage(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setBackgroundColor(Color.parseColor("#1B5E20"));
        tv.setTextColor(Color.WHITE);
        tv.setPadding(16, 12, 16, 12);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.gravity = Gravity.END;
        lp.setMargins(0, 8, 0, 8);
        tv.setLayoutParams(lp);
        chatMessagesLayout.addView(tv);
        scrollDown();
    }

    private void addBotMessage(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setBackgroundColor(Color.parseColor("#262626"));
        tv.setTextColor(Color.parseColor("#FFD54F"));
        tv.setPadding(16, 12, 16, 12);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.gravity = Gravity.START;
        lp.setMargins(0, 8, 0, 8);
        tv.setLayoutParams(lp);
        chatMessagesLayout.addView(tv);
        scrollDown();
    }

    private void processBotResponse(String query) {
        String lower = query.toLowerCase();
        if (lower.contains("report") || lower.contains("அறிக்கை")) {
            addBotMessage("📊 Full Production Report:\nLoom 1: Active\nLoom 2: Active\nBalance: Checked");
        } else if (lower.contains("balance") || lower.contains("பாக்கி")) {
            addBotMessage("💰 Ledger Balance Due: ₹ 0 (All clear)");
        } else if (lower.contains("warp") || lower.contains("பாவு")) {
            addBotMessage("🧵 Warp auto-calculated (6.5m/saree). Recorded!");
        } else {
            addBotMessage("✓ பதிவு செய்யப்பட்டது: \"" + query + "\"");
        }
    }

    private void scrollDown() {
        scrollChat.post(new Runnable() {
            @Override
            public void run() {
                scrollChat.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void createNotification() {
        String channelId = "bot_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Tex Assistant Bot",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (mgr != null) mgr.createNotificationChannel(channel);

            Notification notification = new Notification.Builder(this, channelId)
                    .setContentTitle("Sri Veeramathi Amman Tex")
                    .setContentText("Bot is floating on your home screen")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .build();

            startForeground(101, notification);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null && windowManager != null) {
            windowManager.removeView(floatingView);
        }
    }
}
