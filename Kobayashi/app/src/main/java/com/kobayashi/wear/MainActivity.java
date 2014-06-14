package com.kobayashi.wear;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preview.support.v4.app.NotificationManagerCompat;
import android.preview.support.wearable.notifications.RemoteInput;
import android.preview.support.wearable.notifications.WearableNotifications;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.Locale;


public class MainActivity extends ActionBarActivity implements TextToSpeech.OnInitListener {

    private TextToSpeech mTts;
    String mSpeechString="S O S";

    private static final String TAG = "(´・ω・｀)";
    public static final String BUNDLE_KEY = "token";
    TextView textView;

    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView)findViewById(R.id.textView);

        String token = getIntent().getStringExtra(BUNDLE_KEY);
//        diplayMorse(token);

        mTts = new TextToSpeech(this, this);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                processTweet(intent);
            }
        };
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.v(TAG, "newIntent");
        String token = getIntent().getStringExtra(BUNDLE_KEY);
//        diplayMorse(token);

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, new IntentFilter(Constants.ACTION_WEAR_RESPONSE));
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mReceiver);
        super.onPause();
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

    private void diplayMorse(String token){
        Log.v(TAG, "token:" + token);
        String text = textView.getText().toString();
        text = text + token;
        textView.setText(text);
    }

    public void onClcikNotificationButton(View v){
        sendNotification();
    }

    private void sendNotification(){
        int notificationId = 001;
        int eventId = 1;
        String eventTitle = "Morse";
        String eventMessate = new Date().toString();

        Intent viewIntent1 = new Intent(this, MainActivity.class);
//        Uri uri = Uri.parse();
  //      viewIntent1.setData();
        viewIntent1.putExtra("eventId", eventId);
        viewIntent1.putExtra(BUNDLE_KEY, "1");
        PendingIntent viewPendingIntent1 =
                PendingIntent.getActivity(this, 0, viewIntent1, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent viewIntent3 = new Intent(this, MainActivity.class);
        viewIntent3.putExtra("eventId", eventId);
        viewIntent3.putExtra(BUNDLE_KEY, "3");
        PendingIntent viewPendingIntent3 =
                PendingIntent.getActivity(this, 0, viewIntent3, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(eventTitle)
                        .setContentText(eventMessate)
                        .setContentIntent(viewPendingIntent1)
                        .addAction(R.drawable.one, "1", viewPendingIntent1)
                        .addAction(R.drawable.three, "3", viewPendingIntent3)
                ;

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);


        notificationManager.notify(notificationId, notificationBuilder.build());

    }

    public void onClcikSpeech(View v){

        Toast.makeText(this, mSpeechString +":onClcikSpeech()", Toast.LENGTH_LONG).show();

        if (0 < mSpeechString.length()) {
            if (mTts.isSpeaking()) {
                // 読み上げ中なら止める
                mTts.stop();
            }// 読み上げ開始
             mTts.speak(mSpeechString, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    public void onInit(int status) {
        if (TextToSpeech.SUCCESS == status) {
            Locale locale = Locale.ENGLISH;
            if (mTts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                mTts.setLanguage(locale);
            } else {
                Log.d("", "Error SetLocale");
            }
        } else {
            Log.d("", "Error Init");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mTts) {
           // TextToSpeechのリソースを解放する
           mTts.shutdown();
           }
     }

    /**
     *
     * Tweetするメッセージを表示するNotificationをWearに表示する
     *
     * @param Tweetmessage Tweet Message
     */
    public void sendWearNotification(String Tweetmessage) {
        Intent intent = new Intent(Constants.ACTION_WEAR_RESPONSE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                intent, 0);

        // Create Tweet Message Notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.tweet_message_title))
                .setContentText(Tweetmessage).setContentIntent(pendingIntent);

        // Create the Voice input
        Notification notification = new WearableNotifications.Builder(
                notificationBuilder)
                .setMinPriority()
                .addRemoteInputForContentIntent(
                        new RemoteInput.Builder(Constants.EXTRA_REPLY).setLabel(
                                getString(R.string.reply)).build()).build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat
                .from(this);
        notificationManager.notify(1, notification);
    }

    private void processTweet(Intent intent) {
        String text = intent.getStringExtra(Constants.EXTRA_REPLY);

        if (!TextUtils.isEmpty(text) && Constants.REPLY_MESSAGE.equalsIgnoreCase(text)) {
            // TODO Tweet処理
            Log.d("DebugLog", "OK,Tweet");
        }
    }
}
