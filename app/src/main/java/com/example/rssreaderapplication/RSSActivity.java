package com.example.rssreaderapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

public class RSSActivity extends AppCompatActivity {

    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    private final static String default_notification_channel_id = "default";

    ListView listView;
    ArrayList<String> title;
    ArrayList<String> links;
    ArrayList<String> description;
    ArrayList<String> date;

    FirebaseAuth mAuth;
    FirebaseDatabase db;
    DatabaseReference ref;

    BottomNavigationView bottomNav;

    ArrayList<String> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rss);

        listView = (ListView) findViewById(R.id.feed);

        title = new ArrayList<String>();
        links = new ArrayList<String>();
        description = new ArrayList<String>();
        date = new ArrayList<String>();



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String dbTitle = title.get(i);
                String dbDesc = description.get(i);

                FeedItems feedItems = new FeedItems(dbTitle, dbDesc);

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if(user != null){
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = database.getReference();
                Log.d("UID: ", user.getUid());
                databaseReference.child("users").child(user.getUid()).child("items").push().setValue(feedItems);

                Uri uri = Uri.parse(links.get(i));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                }
            }
        });

        new ProcessInBackground().execute();


        bottomNav = (BottomNavigationView) findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.home);
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId())
                {
                    case R.id.subscribe:
                        startActivity(new Intent(getApplicationContext(), SubscribeActivity.class));
                        overridePendingTransition(0, 0);

                        return true;

                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), RSSActivity.class));
                        overridePendingTransition(0, 0);

                        return true;

                    case R.id.logout:
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);

                        return true;
                }
                return false;
            }
        });

    }


    public InputStream getInputStream(URL url)
    {
        try {
            return url.openConnection().getInputStream();
        }
        catch (IOException exception)
        {
            return null;
        }
    }


    public class ProcessInBackground extends AsyncTask<Integer, Void, Exception>
    {
        ProgressDialog progDialog = new ProgressDialog(RSSActivity.this);
        Exception exc = null;

        URL url1;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progDialog.setMessage("RSS Feed Loading");
            progDialog.show();
        }

        @Override
        protected Exception doInBackground(Integer... integers) {

            try {

                final  FirebaseDatabase database = FirebaseDatabase.getInstance();
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();


                String id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                DatabaseReference refURL = database.getReference().child("users").child(id).child("rss");

                refURL.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        URLClass urlClass = snapshot.getValue(URLClass.class);

                        assert urlClass != null;
                        String newURL = urlClass.url + "";

                        try {
                            url1 = new URL(newURL);

                            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                            factory.setNamespaceAware(false);
                            XmlPullParser xmlPullParser = factory.newPullParser();

                            xmlPullParser.setInput(getInputStream(url1), "utf-8");

                            boolean item = false;
                            int evenType = xmlPullParser.getEventType();

                            while (evenType != XmlPullParser.END_DOCUMENT)
                            {
                                if(evenType == XmlPullParser.START_TAG)
                                {
                                    if(xmlPullParser.getName().equalsIgnoreCase("item"))
                                    {
                                        item = true;
                                    }

                                    else if(xmlPullParser.getName().equalsIgnoreCase("title"))
                                    {
                                        if(item)
                                        {
                                            title.add(xmlPullParser.nextText());
                                        }
                                    }
                                    else if(xmlPullParser.getName().equalsIgnoreCase("link"))
                                    {
                                        if(item)
                                        {
                                            links.add(xmlPullParser.nextText());
                                        }
                                    }
                                    else if(xmlPullParser.getName().equalsIgnoreCase("description"))
                                    {
                                        if(item)
                                        {
                                            description.add(xmlPullParser.nextText());
                                        }
                                    }
                                    else if(xmlPullParser.getName().equalsIgnoreCase("date"))
                                    {
                                        if (item)
                                        {
                                            date.add(xmlPullParser.nextText());
                                        }
                                    }
                                }
                                else if(evenType == XmlPullParser.END_DOCUMENT &&
                                        xmlPullParser.getName().equalsIgnoreCase("item"))
                                {
                                    item = false;
                                }
                                evenType = xmlPullParser.next();
                            }
                        } catch (MalformedURLException e) {
                        exc = e;
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                URL url = new URL("https://www.rte.ie/news/rss/news-headlines.xml");

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xmlPullParser = factory.newPullParser();

                xmlPullParser.setInput(getInputStream(url), "utf-8");

                boolean item = false;
                int evenType = xmlPullParser.getEventType();

                while (evenType != XmlPullParser.END_DOCUMENT)
                {
                    if(evenType == XmlPullParser.START_TAG)
                    {
                        if(xmlPullParser.getName().equalsIgnoreCase("item"))
                        {
                            item = true;
                        }

                        else if(xmlPullParser.getName().equalsIgnoreCase("title"))
                        {
                            if(item)
                            {
                                title.add(xmlPullParser.nextText());
                            }
                        }
                        else if(xmlPullParser.getName().equalsIgnoreCase("link"))
                        {
                            if(item)
                            {
                                links.add(xmlPullParser.nextText());
                            }
                        }
                        else if(xmlPullParser.getName().equalsIgnoreCase("description"))
                        {
                            if(item)
                            {
                                description.add(xmlPullParser.nextText());
                            }
                        }
                        else if(xmlPullParser.getName().equalsIgnoreCase("date"))
                        {
                            if (item)
                            {
                                date.add(xmlPullParser.nextText());
                            }
                        }
                    }
                    else if(evenType == XmlPullParser.END_DOCUMENT &&
                            xmlPullParser.getName().equalsIgnoreCase("item"))
                    {
                        item = false;
                    }
                    evenType = xmlPullParser.next();
                }

                Uri sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(RSSActivity.this,
                        default_notification_channel_id)
                        .setSmallIcon(R.drawable.ic_baseline_rss_feed_24)
                        .setContentTitle("New Story: " + title.add(xmlPullParser.nextText()))
                        .setContentText("" + description.add(xmlPullParser.nextText()));

                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    AudioAttributes attributes = new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build();

                    int importanceHigh = NotificationManager.IMPORTANCE_HIGH;
                    NotificationChannel notificationChannel = new
                            NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importanceHigh);

                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(Color.YELLOW);
                    notificationChannel.enableVibration(true);
                    notificationChannel.setVibrationPattern(new long[]{100 , 200 , 300 , 400 , 500 , 400 , 300 , 200 , 400 });
                    notificationChannel.setSound(sound, attributes);

                    builder.setChannelId(NOTIFICATION_CHANNEL_ID);
                    assert  manager != null;

                    manager.createNotificationChannel(notificationChannel);
                }
                assert manager != null;
                manager.notify((int) System.currentTimeMillis(), builder.build());
            }
            catch (MalformedURLException | XmlPullParserException e)
            {
                exc = e;
            } catch (IOException e)
            {
                exc = e;
            }

            return exc;
        }

        @Override
        protected void onPostExecute(Exception e) {
            super.onPostExecute(e);

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(RSSActivity.this,
                    android.R.layout.simple_list_item_1, title);

            listView.setAdapter(arrayAdapter);
            progDialog.dismiss();
        }
    }
}
