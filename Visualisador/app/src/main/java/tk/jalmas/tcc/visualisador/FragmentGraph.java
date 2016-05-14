package tk.jalmas.tcc.visualisador;


import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentGraph extends Fragment {
    private static final String TAG = FragmentGraph.class.getName();

    private MySimpleGraph graph;
    private Receiver receiver;

    private ImageView bluetoothIcon;

    private TextView triggerText;
    private TextView timeScaleText;
    private TextView holdOffText;
    private TextView timeOffsetText;
    private TextView rxText;

    private int rxTextCount;

    private boolean isGraphStopped;

    private Handler refreshHandler = null;
    Runnable refresherRunnable = new Runnable() {
        @Override
        public void run() {
            if (receiver != null) {
                updateTimeScaleOnDevice();
                updateTriggerSettingsOnDevice();
                updateHoldOffSettingOnDevice();
                updateTimeOffsetSettingOnDevice();
            }

            refreshHandler.postDelayed(refresherRunnable, 2000);
        }
    };

    public FragmentGraph() {
        // Required empty public constructor
    }

    private void updateGraph(int[] data, int lastPosition){
        if (isGraphStopped)
            return;

        MySimpleGraph.DataPoint[] points = new MySimpleGraph.DataPoint[data.length];
        for (int i = 0; i < data.length; i++)
            points [i] = new MySimpleGraph.DataPoint((float) i,
                    (float) (data[i] * Settings.deviceMaxVoltage / 256));

        if (lastPosition == -1)
            graph.updateData(points, Settings.getTriggerValuePercent());
        else
            graph.updateData(points, lastPosition, Settings.getTriggerValuePercent());

    }

    private void updateGraph(int[] data){
        updateGraph(data, -1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View v = inflater.inflate(R.layout.fragment_graph, container, false);

        graph = (MySimpleGraph) v.findViewById(R.id.graph);
        graph.setMaxYValue(Settings.voltageScaleMax);

        bluetoothIcon = (ImageView) v.findViewById(R.id.bt_icon);

        triggerText = (TextView) v.findViewById(R.id.triggerText);
        timeScaleText = (TextView) v.findViewById(R.id.timeScaleText);
        holdOffText = (TextView) v.findViewById(R.id.holdOffText);
        timeOffsetText = (TextView) v.findViewById(R.id.offsetText);
        rxText = (TextView) v.findViewById(R.id.rxText);

        updateTriggerText();
        updateTimeScaleText();
        updateHoldOffText();
        updateTimeOffsetText();

        updateScalesMinMaxValues(v);

        isGraphStopped = false;

        timeScaleText.setText(Settings.getCurrentTimeScaleLabel());

        receiver = new Receiver(getActivity());
        setUpReceiver(v);

        setUpButtonListeners(v);

        refreshHandler = new Handler();
        refresherRunnable.run();

        int i = 1;
        receiver.start();

        return v;
    }

    private void setUpButtonListeners(View v) {
        final Button triggerButton = (Button) v.findViewById(R.id.triggerButton);
        triggerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isGraphStopped) {
                    Settings.toggleTriggerState();
                    updateTriggerSettingsOnDevice();

                    switch (Settings.getTriggerState()) {
                        case OFF:
                            triggerButton.setText("TRIG: OFF");
                            break;
                        case RISE:
                            triggerButton.setText("TRIG: RISE");
                            break;
                        case FALL:
                            triggerButton.setText("TRIG: FALL");
                            break;
                    }
                }
            }
        });

        v.findViewById(R.id.triggerSelectorDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isGraphStopped) {
                    Settings.decreaseTriggerValue();
                    updateTriggerSettingsOnDevice();
                    updateTriggerText();
                }
            }
        });

        v.findViewById(R.id.triggerSelectorUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isGraphStopped) {
                    Settings.increaseTriggerValue();
                    updateTriggerSettingsOnDevice();
                    updateTriggerText();
                }
            }
        });

        v.findViewById(R.id.timeScaleSelectorDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isGraphStopped) {
                    Settings.decreaseTimeScale();
                    updateTimeScaleOnDevice();
                    updateTimeScaleText();
                }
            }
        });

        v.findViewById(R.id.timeScaleSelectorUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isGraphStopped) {
                    Settings.increaseTimeScale();
                    updateTimeScaleOnDevice();
                    updateTimeScaleText();
                }
            }
        });

        v.findViewById(R.id.holdOffSelectorDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isGraphStopped) {
                    Settings.decreaseHoldOff();
                    updateHoldOffSettingOnDevice();
                    updateHoldOffText();
                }
            }
        });

        v.findViewById(R.id.holdOffSelectorUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isGraphStopped) {
                    Settings.increaseHoldOff();
                    updateHoldOffSettingOnDevice();
                    updateHoldOffText();
                }
            }
        });

        v.findViewById(R.id.offsetSelectorDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isGraphStopped) {
                    Settings.decreaseTimeOffset();
                    updateTimeOffsetSettingOnDevice();
                    updateTimeOffsetText();
                }
            }
        });

        v.findViewById(R.id.offsetSelectorUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isGraphStopped) {
                    Settings.increaseTimeOffset();
                    updateTimeOffsetSettingOnDevice();
                    updateTimeOffsetText();
                }
            }
        });

        final ImageView pauseIcon = (ImageView) v.findViewById(R.id.pause_icon);
        pauseIcon.setVisibility(View.INVISIBLE);

        final Button startStopButton = (Button) v.findViewById(R.id.startStopButton);
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGraphStopped = !isGraphStopped;
                if (isGraphStopped) {
                    startStopButton.setText("START");
                    pauseIcon.setVisibility(View.VISIBLE);
                } else {
                    startStopButton.setText("STOP");
                    pauseIcon.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void updateScalesMinMaxValues(View v) {
        ((TextView) v.findViewById(R.id.holdOffLimitMin)).setText(Settings.getMinHoldOffLabel());
        ((TextView) v.findViewById(R.id.holdOffLimitMax)).setText(Settings.getMaxHoldOffLabel());
        ((TextView) v.findViewById(R.id.timeScaleLimitMin)).setText(Settings.getMinTimeScaleLabel());
        ((TextView) v.findViewById(R.id.timeScaleLimitMax)).setText(Settings.getMaxTimeScaleLabel());
        ((TextView) v.findViewById(R.id.offsetLimitMin)).setText(Settings.getMinTimeOffsetLabel());
        ((TextView) v.findViewById(R.id.offsetLimitMax)).setText(Settings.getMaxTimeOffsetLabel());
    }

    private void setUpReceiver(View v) {
        receiver.setUpdater(new Updater() {
            @Override
            public void onUpdate(final int[] data) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateGraph(data);
                    }
                });
            }

            @Override
            public void onUpdate(final int[] data, final int lastPosition) {
                //System.out.println("onUpdate received " + lastPosition);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateGraph(data, lastPosition);
                    }
                });
            }
        });

        rxTextCount = 0;
        rxText.setVisibility(View.INVISIBLE);
        receiver.setStateListener(new StateListener() {

            @Override
            public void onCharacterReceived() {
                rxTextCount++;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (rxTextCount < 250)
                            rxText.setVisibility(View.VISIBLE);
                        else
                            rxText.setVisibility(View.INVISIBLE);
                    }
                });

                if (rxTextCount == 500)
                    rxTextCount = 0;
            }

            @Override
            public void onBluetoothStateChanged(final boolean connected) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (connected)
                            bluetoothIcon.setImageResource(R.drawable.bluetooth);
                        else
                            bluetoothIcon.setImageResource(R.drawable.bluetooth_fade);
                    }
                });
            }

            @Override
            public void onErrorOccurred() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showErrorDialog();
                    }
                });
            }
        });

        final ImageView triggerStatus = (ImageView) v.findViewById(R.id.trigger_status);
        receiver.setOnChangeSendingModeListener(new OnSendingModeChangeListener() {
            @Override
            public void onChangeToBulk() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        triggerStatus.setImageResource(R.drawable.trigger_rise);
                    }
                });
            }

            @Override
            public void onChangeToContinuous() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        triggerStatus.setImageResource(R.drawable.trigger_rise_faded);
                    }
                });
            }
        });
    }

    private void updateTriggerText() {
        int triggerPercent = Settings.getTriggerValuePercent();

        triggerText.setText(String.format("%.1fV", (float) (((double) triggerPercent) * 4.0 / 100.0)));

    }

    private void updateTimeScaleText() {
        timeScaleText.setText(Settings.getCurrentTimeScaleLabel());
    }

    private void updateHoldOffText() {
        holdOffText.setText(Settings.getCurrentHoldOffLabel());
    }

    private void updateTimeOffsetText() {
        timeOffsetText.setText(Settings.getCurrentTimeOffsetLabel());
    }

    private void updateTimeScaleOnDevice() {
        if (receiver != null)
            receiver.sendCommand(Settings.composeTimeScaleCommand());
    }

    private void updateHoldOffSettingOnDevice() {
        if (receiver != null)
            receiver.sendCommand(Settings.composeHoldOffCommand());
    }

    private void updateTimeOffsetSettingOnDevice() {
        if (receiver != null)
            receiver.sendCommand(Settings.composeTimeOffsetCommand());
    }

    private void updateTriggerSettingsOnDevice() {
        if (receiver != null)
            receiver.sendCommand(Settings.composeTriggerCommand());
    }

    private void updateSettingOnDevice(char command) {
        if (receiver != null)
            receiver.sendCommand(command);
    }

    private void showErrorDialog() {
        Toast.makeText(getActivity(), "Error on connecting", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        if (receiver != null) {
            receiver.cleanup();
        }

        super.onDestroy();
    }
}
