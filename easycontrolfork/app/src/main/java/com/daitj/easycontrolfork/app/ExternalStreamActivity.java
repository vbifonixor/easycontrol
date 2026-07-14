package com.daitj.easycontrolfork.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.daitj.easycontrolfork.app.helper.ExternalStream;

public class ExternalStreamActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent mainIntent = new Intent(this, MainActivity.class);
    mainIntent.setAction(getIntent().getAction());
    mainIntent.setData(getIntent().getData());
    mainIntent.putExtras(getIntent());
    mainIntent.putExtra(ExternalStream.EXTRA_LAUNCH, true);
    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    startActivity(mainIntent);
    finish();
  }
}
