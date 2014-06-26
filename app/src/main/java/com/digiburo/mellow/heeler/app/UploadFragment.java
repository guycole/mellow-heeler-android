package com.digiburo.mellow.heeler.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.digiburo.mellow.heeler.R;
import com.digiburo.mellow.heeler.lib.Constant;
import com.digiburo.mellow.heeler.lib.database.DataBaseFacade;
import com.digiburo.mellow.heeler.lib.database.LocationModelList;
import com.digiburo.mellow.heeler.lib.database.ObservationModelList;
import com.digiburo.mellow.heeler.lib.database.SortieModel;
import com.digiburo.mellow.heeler.lib.database.SortieModelList;
import com.digiburo.mellow.heeler.lib.service.UploadService;
import com.digiburo.mellow.heeler.lib.utility.LegalJsonMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * upload fragment
 */
public class UploadFragment extends Fragment {
  private static final Logger LOG = LoggerFactory.getLogger(UploadFragment.class);

  private TextView textLocationCurrent;
  private TextView textLocationOriginal;

  private TextView textObservationCurrent;
  private TextView textObservationOriginal;

  private TextView textSortieCurrent;
  private TextView textSortieOriginal;

  int locationPopulation;
  int observationPopulation;
  int sortiePopulation;

  /**
   * update display for fresh location or WiFi detection event
   */
  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      LOG.info("onReceive noted");

      boolean authFlag = false;

      if (intent.hasExtra(Constant.INTENT_AUTH_FLAG)) {
        authFlag = intent.getBooleanExtra(Constant.INTENT_AUTH_FLAG, false);
        if (authFlag) {
          Toast.makeText(getActivity(), "authentication success", Toast.LENGTH_LONG).show();
        } else {
          Toast.makeText(getActivity(), "authentication failure", Toast.LENGTH_LONG).show();
        }
      } else if (intent.hasExtra(Constant.INTENT_UPLOAD_TYPE)) {
        int uploadCount = intent.getIntExtra(Constant.INTENT_UPLOAD_COUNT, 0);
        String uploadType = intent.getStringExtra(Constant.INTENT_UPLOAD_TYPE);

        LegalJsonMessage messageType = LegalJsonMessage.discoverMatchingEnum(uploadType);
        switch(messageType) {
          case GEOLOCATION:
            locationPopulation -= uploadCount;
 //           Toast.makeText(getActivity(), "location success:" + uploadCount, Toast.LENGTH_LONG).show();
            break;
          case OBSERVATION:
            observationPopulation -= uploadCount;
//            Toast.makeText(getActivity(), "observation success:" + uploadCount, Toast.LENGTH_LONG).show();
            break;
          case SORTIE:
            sortiePopulation -= uploadCount;
//            Toast.makeText(getActivity(), "sortie success:" + uploadCount, Toast.LENGTH_LONG).show();
            break;
          default:
            LOG.error("unsupported message type:" + uploadType);
        }

        updatePopulation();
      } else if (intent.hasExtra(Constant.INTENT_COMPLETE_FLAG)) {
        Toast.makeText(getActivity(), "upload complete", Toast.LENGTH_LONG).show();
        getActivity().finish();
      }
    }
  };

  /**
   * mandatory empty ctor
   */
  public UploadFragment() {
    //empty
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    View view = inflater.inflate(R.layout.fragment_upload, container, false);

    textLocationCurrent = (TextView) view.findViewById(R.id.textLocationCurrent);
    textLocationCurrent.setText(Constant.UNKNOWN);

    textLocationOriginal = (TextView) view.findViewById(R.id.textLocationOriginal);
    textLocationOriginal.setText(Constant.UNKNOWN);

    textObservationCurrent = (TextView) view.findViewById(R.id.textObservationCurrent);
    textObservationCurrent.setText(Constant.UNKNOWN);

    textObservationOriginal = (TextView) view.findViewById(R.id.textObservationOriginal);
    textObservationOriginal.setText(Constant.UNKNOWN);

    textSortieCurrent = (TextView) view.findViewById(R.id.textSortieCurrent);
    textSortieCurrent.setText(Constant.UNKNOWN);

    textSortieOriginal = (TextView) view.findViewById(R.id.textSortieOriginal);
    textSortieOriginal.setText(Constant.UNKNOWN);

    discoverStartingPopulation();
    updatePopulation();

    return view;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    UploadService.startUploadService(getActivity());
  }

  @Override
  public void onResume() {
    super.onResume();
    getActivity().registerReceiver(broadcastReceiver, new IntentFilter(Constant.UPLOAD_EVENT));
  }

  @Override
  public void onPause() {
    super.onPause();
    getActivity().unregisterReceiver(broadcastReceiver);
  }

  private void updatePopulation() {
    textLocationCurrent.setText(Integer.toString(locationPopulation));
    textObservationCurrent.setText(Integer.toString(observationPopulation));
    textSortieCurrent.setText(Integer.toString(sortiePopulation));
  }

  private void discoverStartingPopulation() {
    DataBaseFacade dataBaseFacade = new DataBaseFacade(getActivity());

    SortieModelList sortieModelList = dataBaseFacade.selectAllSorties(false, getActivity());
    sortiePopulation = sortieModelList.size();

    for (SortieModel sortieModel:sortieModelList) {
      locationPopulation += dataBaseFacade.countLocationRows(false, sortieModel.getSortieUuid(), getActivity());
      observationPopulation += dataBaseFacade.countLocationRows(false, sortieModel.getSortieUuid(), getActivity());
    }

    textLocationOriginal.setText(Integer.toString(locationPopulation));
    textObservationOriginal.setText(Integer.toString(observationPopulation));
    textSortieOriginal.setText(Integer.toString(sortiePopulation));
  }
}
/*
 * Copyright 2014 Digital Burro, INC
 * Created on May 21, 2014 by gsc
 */