package com.digiburo.mellow.heeler.lib.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.digiburo.mellow.heeler.lib.Constant;
import com.digiburo.mellow.heeler.lib.Personality;
import com.digiburo.mellow.heeler.lib.database.DataBaseFacade;
import com.digiburo.mellow.heeler.lib.database.LocationModel;
import com.digiburo.mellow.heeler.lib.database.ObservationModel;
import com.digiburo.mellow.heeler.lib.database.ObservationModelList;
import com.digiburo.mellow.heeler.lib.database.SortieModel;

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

    LocationModel locationModel = Personality.getCurrentLocation();
    if (locationModel == null) {
      LOG.debug("no location - ignoring intent");
      return;
    }

    SortieModel sortieModel = Personality.getCurrentSortie();
    if (sortieModel == null) {
      LOG.debug("no sortie - ignoring intent");
      return;
    }

    if (Constant.INTENT_ACTION_ALARM.equals(intent.getAction())) {
      LOG.debug("start wifi scan");
      WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
      wifiManager.startScan();
      return;
    }

    freshScan(locationModel, sortieModel, context);
  }

  private void freshScan(final LocationModel locationModel, final SortieModel sortieModel, final Context context) {
    Thread thread = new Thread(new FreshScan(locationModel, sortieModel, context));
    thread.start();
  }

  class FreshScan implements Runnable {
    private final LocationModel locationModel;
    private final SortieModel sortieModel;
    private final Context context;

    public FreshScan(final LocationModel locationModel, final SortieModel sortieModel, final Context context) {
      this.locationModel = locationModel;
      this.sortieModel = sortieModel;
      this.context = context;
    }

    @Override
    public void run() {
      ObservationModelList observationModelList = getScanResults(context);
      saveScanResults(observationModelList, context);

      int population = observationModelList.size();
      LOG.debug("WiFi scan population:" + population);

      if (population > 0) {
        ObservationModel lastObservation = observationModelList.get(population-1);

        Intent intent = new Intent(Constant.CONSOLE_UPDATE);
        intent.putExtra(Constant.INTENT_OBSERVATION_UPDATE, lastObservation.getId());
        context.sendBroadcast(intent);
      }

      SpeechService.sayThis(Integer.toString(population), context);
    }

    private ObservationModelList getScanResults(final Context context) {
      ObservationModelList observationModelList = new ObservationModelList();

      WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
      List<ScanResult> scanList = wifiManager.getScanResults();
      for (ScanResult scanResult:scanList) {
        ObservationModel observationModel = new ObservationModel();
        observationModel.setDefault();
        observationModel.setScanResult(scanResult, locationModel.getLocationUuid(), sortieModel.getSortieUuid());
        observationModelList.add(observationModel);
      }

      return observationModelList;
    }

    private void saveScanResults(final ObservationModelList observationModelList, final Context context) {
      DataBaseFacade dataBaseFacade = new DataBaseFacade(context);
      for (ObservationModel observationModel:observationModelList) {
        dataBaseFacade.insert(observationModel, context);
      }
    }
  }
}
/*
 * Copyright 2014 Digital Burro, INC
 * Created on May 10, 2014 by gsc
 */
