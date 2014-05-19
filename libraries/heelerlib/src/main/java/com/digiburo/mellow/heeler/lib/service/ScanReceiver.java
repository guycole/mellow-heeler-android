package com.digiburo.mellow.heeler.lib.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.digiburo.mellow.heeler.lib.Constant;
import com.digiburo.mellow.heeler.lib.Personality;
import com.digiburo.mellow.heeler.lib.content.DataBaseFacade;
import com.digiburo.mellow.heeler.lib.content.LocationModel;
import com.digiburo.mellow.heeler.lib.content.ObservationModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * invoked to process WiFi scan results (start scanning or collect results)
 * @author gsc
 */
public class ScanReceiver extends BroadcastReceiver {
  private static final Logger LOG = LoggerFactory.getLogger(ScanReceiver.class);

  /**
   *
   * @param context
   * @param intent
   */
  public void onReceive(Context context, Intent intent) {
    LOG.debug("xxx xxx onReceive xxx xxx:" + intent.getAction());

    if (Personality.getCurrentSortie() == null) {
      LOG.debug("no sortie - ignoring intent");
      return;
    }

    if (Constant.INTENT_ACTION_ALARM.equals(intent.getAction())) {
      LOG.debug("start wifi scan");
      WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
      wifiManager.startScan();
      return;
    }

    LocationModel locationModel = Personality.getCurrentLocation();
    if (locationModel == null) {
      LOG.error("unknown location skipping observation");
      return;
    }

    DataBaseFacade dataBaseFacade = new DataBaseFacade();

    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    List<ScanResult> scanList = wifiManager.getScanResults();
    for (ScanResult scanResult:scanList) {
      ObservationModel observationModel = new ObservationModel();
      observationModel.setDefault(context);
      observationModel.setScanResult(scanResult);
      dataBaseFacade.newObservation(observationModel, context);
    }
  }
}
/*
 * Copyright 2014 Digital Burro, INC
 * Created on May 10, 2014 by gsc
 */
