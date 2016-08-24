package com.evontech.VideoPlugin;

import android.content.Context;

import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

public class MySubscriber extends Subscriber {

    private String userId;
    private String name;

//  Created by amit rai on 3/7/2016

    public MySubscriber(Context context, Stream stream) {
        super(context, stream);
        // With the userId we can query our own database
        // to extract player information
        setName("User" + ((int) (Math.random() * 1000)));
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String name) {
        this.userId = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
