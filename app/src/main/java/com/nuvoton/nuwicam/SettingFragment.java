package com.nuvoton.nuwicam;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.preference.Preference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.longevitysoft.android.xml.plist.domain.PListObject;
import com.longevitysoft.android.xml.plist.domain.sString;
import com.nuvoton.socketmanager.ReadConfigure;
import com.nuvoton.socketmanager.SocketInterface;
import com.nuvoton.socketmanager.SocketManager;

import java.util.ArrayList;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, SocketInterface{
    private ReadConfigure configure;
    private SocketManager socketManager;
    private String key; 
    private static String platform, cameraSerial, preferenceName;
    private String TAG = "SettingFragment";
    private ArrayList<Preference> settingArrayList;
    public static SettingFragment newInstance(Bundle bundle){
        platform = bundle.getString("Platform");
        cameraSerial = bundle.getString("CameraSerial");
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    public SettingFragment(){
        Log.d(TAG, "SettingFragment: " + platform);
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        preferenceName = "Setup Camera " + String.valueOf(cameraSerial);

//        getActivity().getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        getPreferenceManager().setSharedPreferencesName(preferenceName);
//        getPreferenceManager().getSharedPreferences();
        Log.d(TAG, "onCreate: " + preferenceName + " pref name: " + getPreferenceManager().getSharedPreferencesName());
        // Inflate the layout for this fragment

        if (platform.equals("NuWicam")) {
            addPreferencesFromResource(R.xml.settings_nuwicam);
        }

        configure = ReadConfigure.getInstance(getActivity().getApplicationContext());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "onSharedPreferenceChanged: " + key);
        determineSettings(key, sharedPreferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        getActivity().getApplicationContext().getSharedPreferences(preferenceName, Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);
        updateSetting();

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        getActivity().getApplicationContext().getSharedPreferences(preferenceName, Context.MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(this);
    }

    private void determineSettings(String key, SharedPreferences sharedPreference){
        socketManager = new SocketManager();
        socketManager.setSocketInterface(this);
        boolean callSend = true, plugin = false;
        String command = getDeviceURL();
        sString baseCommand, subCommand;
        String pipe="&pipe=0", type="&type=h264", value, commandType = "";
        ArrayList<String> commandList = new ArrayList<>();
        String pluginCommand = "param.cgi?action=update&group=plugin";
        String finalCommand = "";
        String index = "0";
        switch (key){
            case "Resolution":
                ArrayList<Map> videoCommandSet = configure.videoCommandSet;
                Map<String, PListObject> targetCommand = videoCommandSet.get(1);
                baseCommand = (sString) targetCommand.get("Base Command");
                subCommand = (sString) targetCommand.get("Sub Command");
                value = sharedPreference.getString(key, "0");
                command = command + baseCommand.getValue() + "?command=" + subCommand.getValue() + pipe + type + "&value=" + value;
                commandType = SocketManager.CMDSET_RESOLUTION;
                break;
            case "Adaptive":
                index = sharedPreference.getString("Adaptive", "0");
                commandList = new ArrayList<>();

                String adaptiveTemp = "Pipe0_Quality";
                finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + adaptiveTemp + "&value=0";
                commandList.add(finalCommand);

                adaptiveTemp = "Pipe0_Min_Quality";
                finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + adaptiveTemp + "&value=20";
                commandList.add(finalCommand);

                adaptiveTemp = "Pipe0_Max_Quality";
                finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + adaptiveTemp + "&value=52";
                commandList.add(finalCommand);

                if (index.equals("0")){
                    adaptiveTemp = "Pipe0_Min_Bitrate";
                    finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + adaptiveTemp + "&value=512";
                    commandList.add(finalCommand);

                    adaptiveTemp = "Pipe0_Max_Bitrate";
                    finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + adaptiveTemp + "&value=2000";
                    commandList.add(finalCommand);
                }else if (index.equals("1")){
                    adaptiveTemp = "Pipe0_Min_Bitrate";
                    finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + adaptiveTemp + "&value=512";
                    commandList.add(finalCommand);

                    adaptiveTemp = "Pipe0_Max_Bitrate";
                    finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + adaptiveTemp + "&value=5000";
                    commandList.add(finalCommand);
                }else{
                    adaptiveTemp = "Pipe0_Min_Bitrate";
                    finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + adaptiveTemp + "&value=2000";
                    commandList.add(finalCommand);

                    adaptiveTemp = "Pipe0_Max_Bitrate";
                    finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + adaptiveTemp + "&value=5000";
                    commandList.add(finalCommand);
                }
                plugin = true;
                socketManager.setCommandList(commandList);
                commandType = SocketManager.CMDSET_ADAPTIVE;
                sharedPreference.edit().putString("Fixed Quality", "4");
                sharedPreference.edit().putString("Fixed Bit Rate", "4");
                break;
            case "Fixed Bit Rate":
                index = sharedPreference.getString("Fixed Bit Rate", "0");
                commandList = new ArrayList<>();

                String fixedBitRateTemp = "Pipe0_Quality";
                finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + fixedBitRateTemp + "&value=0";
                commandList.add(finalCommand);

                fixedBitRateTemp = "Pipe0_Min_Quality";
                finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + fixedBitRateTemp + "&value=1";
                commandList.add(finalCommand);

                fixedBitRateTemp = "Pipe0_Max_Quality";
                finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + fixedBitRateTemp + "&value=52";
                commandList.add(finalCommand);

                fixedBitRateTemp = "Pipe0_Max_Bitrate";
                finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + fixedBitRateTemp + "&value=0";
                commandList.add(finalCommand);

                fixedBitRateTemp = "Pipe0_Bitrate";

                if (index.equals("0")){
                    finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + fixedBitRateTemp + "&value=2000";
                }else if (index.equals("1")){
                    finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + fixedBitRateTemp + "&value=3000";
                }else {
                    finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + fixedBitRateTemp + "&value=5000";
                }
                plugin = true;
                commandList.add(finalCommand);
                socketManager.setCommandList(commandList);
                commandType = SocketManager.CMDSET_FIXED_BITRATE;
                sharedPreference.edit().putString("Adaptive", "4");
                sharedPreference.edit().putString("Fixed Quality", "4");

                break;
            case "Fixed Quality":
                index = sharedPreference.getString("Fixed Bit Rate", "0");
                commandList = new ArrayList<>();

                String fixedQualityTemp = "Pipe0_Min_Bitrate";
                finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + fixedQualityTemp + "&value=512";
                commandList.add(finalCommand);

                fixedQualityTemp = "Pipe0_Max_Bitrate";
                finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + fixedQualityTemp + "&value=4096";
                commandList.add(finalCommand);

                fixedQualityTemp = "Pipe0_Quality";

                if (index.equals("0")){
                    finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + fixedQualityTemp + "&value=50";
                }else if (index.equals("1")){
                    finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + fixedQualityTemp + "&value=40";
                }else {
                    finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + fixedQualityTemp + "&value=25";
                }
                plugin = true;
                commandList.add(finalCommand);
                socketManager.setCommandList(commandList);
                commandType = SocketManager.CMDSET_FIXED_QUALITY;
                sharedPreference.edit().putString("Adaptive", "4");
                sharedPreference.edit().putString("Fixed Bit Rate", "4");
                break;
            case "FPS":
                videoCommandSet = configure.videoCommandSet;
                targetCommand = videoCommandSet.get(7);
                baseCommand = (sString) targetCommand.get("Base Command");
                subCommand = (sString) targetCommand.get("Sub Command");
                value = sharedPreference.getString(key, "30");
                command = command + baseCommand.getValue() + "?command=" + subCommand.getValue() + pipe + type + "&value=" + value;
                commandType = SocketManager.CMDSET_FPS;
                break;
            case "Device Mic":
                videoCommandSet = configure.audioCommandSet;
                targetCommand = videoCommandSet.get(0);
                baseCommand = (sString) targetCommand.get("Base Command");
                subCommand = (sString) targetCommand.get("Sub Command");
                boolean mute = sharedPreference.getBoolean("key", false);
                value = (mute == true) ? "1" : "0";
                command = command + baseCommand.getValue() + "?command=" + subCommand.getValue() + pipe + type + "&value=" + value;
                commandType = SocketManager.CMDSET_MUTE;

                break;
            case "Transmission":
                callSend = false;
                break;
            case "Reboot":
                String reboot = sharedPreference.getString(key, "1");
                if (reboot.equals("0")){
                    videoCommandSet = configure.systemCommandSet;
                    targetCommand = videoCommandSet.get(0);
                    baseCommand = (sString) targetCommand.get("Base Command");
                    command = command + baseCommand.getValue();
                    commandType = SocketManager.CMDSET_REBOOT;
                }
                sharedPreference.edit().putString(key, "1");
                break;
//            case "Reset Data":
//                callSend = false;
//                String reset = sharedPreference.getString(key, "1");
//                if (reset.equals("0")){
//                    configure.initSharedPreference(Integer.valueOf(cameraSerial), true);
//                    sharedPreference.edit().putString(key, "0");
//                }
//                break;
            case "Recorder Status":
                ArrayList<Map> recordCommandSet = configure.recordCommandSet;
                String recorderStatus = sharedPreference.getString(key, "0");
                if (recorderStatus.equals("0")){
                    targetCommand = recordCommandSet.get(3);
                }else {
                    targetCommand = recordCommandSet.get(2);
                }
                baseCommand = (sString) targetCommand.get("Base Command");
                subCommand = (sString) targetCommand.get("Sub Command");
                value = sharedPreference.getString(key, "30");
                command = command + baseCommand.getValue() + "?command=" + subCommand.getValue() + pipe + type + "&value=" + value;
                commandType = SocketManager.CMDSET_RECORD;
                break;
        }
        Log.d(TAG, "determineSettings: " + command);
        if (socketManager != null && callSend == true && plugin == false){
            socketManager.executeSendGetTask(command, commandType);
        }else if (socketManager != null && callSend == true && plugin == true){
            socketManager.executeSendGetTaskList(commandList, commandType);
        }
        sharedPreference.edit().commit();
    }

    private String getDeviceURL(){
        String cameraName = "Setup Camera " + cameraSerial;
        SharedPreferences preference = getActivity().getApplicationContext().getSharedPreferences(cameraName, Context.MODE_PRIVATE);
        String urlString = preference.getString("URL", "DEFAULT");
        String [] ipCut = urlString.split("/");
        String ip = ipCut[2];
        String url = "http://" + ip + ":80/";
        return url;
    }

    @Override
    public void showToastMessage(String message) {
        Log.d(TAG, "showToastMessage: ");
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void updateFileList(ArrayList<FileContent> fileList) {
        Log.d(TAG, "updateFileList: ");
    }

    @Override
    public void deviceIsAlive() {
        Log.d(TAG, "deviceIsAlive: ");
    }

    @Override
    public void updateSettingContent(String category, String value) {
        String cameraName = "Setup Camera " + cameraSerial;
        SharedPreferences preference = getActivity().getApplicationContext().getSharedPreferences(cameraName, Context.MODE_PRIVATE);
        preference.edit().putString(category, value);
        preference.edit().commit();
        if (category.equals("Recorder Status")){
            Preference pref = (Preference)getPreferenceManager().findPreference(category);
            if (value.equals("1"))
                pref.setSummary("Recorder is recording");
            else
                pref.setSummary("Recorder is stopped");
        }else if(category.equals("Available Storage")){
            Preference pref = (Preference)getPreferenceManager().findPreference(category);
            if (value.equals("1"))
                pref.setSummary("Storage available on device.");
            else
                pref.setSummary("No storage available on device.");
        }else {
            ListPreference pref = (ListPreference) getPreferenceManager().findPreference(category);
            pref.setValue(value);
        }
    }

    private void updateSetting(){
        socketManager = new SocketManager();
        socketManager.setSocketInterface(this);
        boolean callSend = true, plugin = false;
        sString baseCommand, subCommand;
        String pipe="&pipe=0", type="&type=h264", commandType = "";
        ArrayList<String> commandList = new ArrayList<>();
        String command = getDeviceURL();
// get resolution
        ArrayList<Map> videoCommandSet = configure.videoCommandSet;
        Map<String, PListObject> targetCommand = videoCommandSet.get(2);
        baseCommand = (sString) targetCommand.get("Base Command");
        subCommand = (sString) targetCommand.get("Sub Command");
        command = command + baseCommand.getValue() + "?command=" + subCommand.getValue() + pipe + type;
        commandList.add(command);
// get fps
        command = getDeviceURL();

        targetCommand = videoCommandSet.get(8);
        baseCommand = (sString) targetCommand.get("Base Command");
        subCommand = (sString) targetCommand.get("Sub Command");
        command = command + baseCommand.getValue() + "?command=" + subCommand.getValue() + pipe + type;
        commandList.add(command);
// get available storage
        command = getDeviceURL();

        ArrayList<Map> infoCommandSet = configure.infoCommandSet;
        targetCommand = infoCommandSet.get(0);
        baseCommand = (sString) targetCommand.get("Base Command");
        subCommand = (sString) targetCommand.get("Sub Command");
        command = command + baseCommand.getValue() + "?command=" + subCommand.getValue() + pipe + type;
        commandList.add(command);
// get recorder status
        command = getDeviceURL();

        ArrayList<Map> recordCommandSet = configure.recordCommandSet;
        targetCommand = recordCommandSet.get(1);
        baseCommand = (sString) targetCommand.get("Base Command");
        subCommand = (sString) targetCommand.get("Sub Command");
        command = command + baseCommand.getValue() + "?command=" + subCommand.getValue() + pipe + type;
        commandList.add(command);

        commandType = SocketManager.CMDGET_ALL;
        socketManager.setCommandList(commandList);
        socketManager.executeSendGetTaskList(commandList, commandType);
    }
}
