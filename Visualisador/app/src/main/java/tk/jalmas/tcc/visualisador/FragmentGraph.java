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
    private TextView cursorTypeText;
    private TextView cursor1Text;
    private TextView cursor2Text;
    private TextView cursorsDataText1;
    private TextView cursorsDataText2;
    private TextView timeScaleTextSmall;
    private TextView triggerTextSmall;

    private View cursorsPanel;
    private View holdOffSelector;
    private View timeScaleSelector;
    private View triggerPanel;

    private int rxTextCount;

    private boolean onBulkMode;

    private boolean isGraphStopped;
    private boolean updateRequest;

    private int[] lastData;
    private int lastPosition;

    private Handler refreshHandler = null;
    Runnable refresherRunnable = new Runnable() {
        @Override
        public void run() {
            if (receiver != null) {
                updateTimeScaleOnDevice();
                updateTriggerSettingsOnDevice();
                updateHoldOffSettingOnDevice();
            }

            refreshHandler.postDelayed(refresherRunnable, 2000);
        }
    };

    public FragmentGraph() {
        // Required empty public constructor
    }

    private void updateGraph(int[] data, int lastPosition){
        this.lastData = data;
        this.lastPosition = lastPosition;

        if (!updateRequest && isGraphStopped)
            return;

        int dataLen = 0;
        if (onBulkMode)
            dataLen = data.length / 2;
        else
            dataLen = data.length;

        MySimpleGraph.DataPoint[] points = new MySimpleGraph.DataPoint[dataLen];
        for (int i = 0; i < dataLen; i++) {
            int index = 0;

            if (onBulkMode || updateRequest)
                index = dataLen/2 + i + (int) (dataLen * Settings.getCurrentTimeOffsetFactor());
            else
                index = i;

            if (i==0) System.out.println("min " + index);
            if (i==dataLen-1) System.out.println("max " + index);

            points[i] = new MySimpleGraph.DataPoint((float) i,
                    (data[index] * Settings.deviceMaxVoltage / 256));
        }
        if (lastPosition == -1)
            graph.updateData(points, Settings.getTriggerValuePercent());
        else
            graph.updateData(points, lastPosition, Settings.getTriggerValuePercent());

        updateRequest = false;
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

        holdOffSelector = v.findViewById(R.id.holdOffSelector);
        timeScaleSelector = v.findViewById(R.id.timeScaleSelector);
        cursorsPanel = v.findViewById(R.id.cursorsPanel);
        triggerPanel = v.findViewById(R.id.triggerPanel);

        bluetoothIcon = (ImageView) v.findViewById(R.id.bt_icon);

        triggerText = (TextView) v.findViewById(R.id.triggerText);
        timeScaleText = (TextView) v.findViewById(R.id.timeScaleText);
        holdOffText = (TextView) v.findViewById(R.id.holdOffText);
        timeOffsetText = (TextView) v.findViewById(R.id.offsetText);
        rxText = (TextView) v.findViewById(R.id.rxText);
        cursorTypeText = (TextView) v.findViewById(R.id.cursorTypeText);
        cursor1Text = (TextView) v.findViewById(R.id.cursor1Text);
        cursor2Text = (TextView) v.findViewById(R.id.cursor2Text);
        cursorsDataText1 = (TextView) v.findViewById(R.id.cursorsDataText1);
        cursorsDataText2 = (TextView) v.findViewById(R.id.cursorsDataText2);
        timeScaleTextSmall = (TextView) v.findViewById(R.id.timeScaleTextSmall);
        triggerTextSmall = (TextView) v.findViewById(R.id.triggerTextSmall);

        graph.setMaxYValue(Settings.voltageScaleMax);

        holdOffSelector.setVisibility(View.VISIBLE);
        timeScaleSelector.setVisibility(View.VISIBLE);
        cursorsPanel.setVisibility(View.INVISIBLE);
        triggerPanel.setVisibility(View.VISIBLE);

        setUpGraphOnCursorMovedListener(graph);

        updateTriggerText();
        updateTimeScaleText();
        updateHoldOffText();
        updateTimeOffsetText();
        updateCursorTexts();

        updateScalesMinMaxValues(v);

        isGraphStopped = false;

        onBulkMode = false;

        timeScaleText.setText(Settings.getCurrentTimeScaleCompleteLabel());

        receiver = new Receiver(getActivity());
        setUpReceiver(v);

        setUpButtonListeners(v);

        refreshHandler = new Handler();
        refresherRunnable.run();

        receiver.start();

        return v;
    }

    private void setUpGraphOnCursorMovedListener(MySimpleGraph graph) {
        graph.setOnCursorMovedListener(new OnCursorMovedListener() {
            @Override
            public void onVoltageCursorMoved(float cursor1, float cursor2) {
                updateCursorTexts();
            }

            @Override
            public void onTimeCursorMoved(float cursor1, float cursor2) {
                updateCursorTexts();
            }
        });
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
                Settings.decreaseTimeOffset();
                updateTimeOffsetText();

                updateRequest = true;
                updateGraph(lastData);
            }
        });

        v.findViewById(R.id.offsetSelectorUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.increaseTimeOffset();
                updateTimeOffsetText();

                updateRequest = true;
                updateGraph(lastData);
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
                    startStopButton.setText("RUN");
                    pauseIcon.setVisibility(View.VISIBLE);
                    holdOffSelector.setVisibility(View.INVISIBLE);
                    timeScaleSelector.setVisibility(View.INVISIBLE);
                    cursorsPanel.setVisibility(View.VISIBLE);
                    triggerPanel.setVisibility(View.INVISIBLE);
                    graph.setGraphPaused(true);
                    updateCursorTexts();
                } else {
                    startStopButton.setText("STOP");
                    pauseIcon.setVisibility(View.INVISIBLE);
                    holdOffSelector.setVisibility(View.VISIBLE);
                    timeScaleSelector.setVisibility(View.VISIBLE);
                    cursorsPanel.setVisibility(View.INVISIBLE);
                    triggerPanel.setVisibility(View.VISIBLE);
                    graph.setGraphPaused(false);
                }
            }
        });

        v.findViewById(R.id.cursorToggleButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                graph.toggleCurrentCursorsState();

                updateCursorTexts();
            }
        });
    }

    private void updateCursorTexts() {
        switch (graph.getCurrentCursorsState()) {
            case VOLTAGE:
                float[] voltCursors = graph.getVoltageCursors();
                float[] voltCursorsValue = {
                        voltCursors[0] * Settings.voltageScaleMax,
                        voltCursors[1] * Settings.voltageScaleMax
                };
                cursorTypeText.setText("Voltage");
                cursor1Text.setText(String.format("1: %.2fV", voltCursorsValue[0]));
                cursor2Text.setText(String.format("2: %.2fV",voltCursorsValue[1]));
                cursorsDataText1.setText(String.format("\u0394Y: %.2fV",Math.abs(voltCursorsValue[1] - voltCursorsValue[0])));
                cursorsDataText2.setText("");
                break;

            case TIME:
                float[] timeCursors = graph.getTimeCursors();
                float[] timeCursorsValue = {
                        timeCursors[0] * graph.GRID_X_COUNT * Settings.getCurrentTimeScaleLabelValue(),
                        timeCursors[1] * graph.GRID_X_COUNT * Settings.getCurrentTimeScaleLabelValue()
                };
                cursorTypeText.setText("Time");
                cursor1Text.setText(String.format("1: %.1f%s", timeCursorsValue[0], Settings.getCurrentTimeScaleLabelUnit()));
                cursor2Text.setText(String.format("2: %.1f%s", timeCursorsValue[1], Settings.getCurrentTimeScaleLabelUnit()));
                cursorsDataText1.setText(String.format("\u0394Y: %.1f%s",
                        Math.abs(timeCursorsValue[1] - timeCursorsValue[0]),
                        Settings.getCurrentTimeScaleLabelUnit()));

                String frequencyScale = "";
                switch (Settings.getCurrentTimeScaleLabelUnit()) {
                    case "us":
                        frequencyScale = "kHz";
                        break;
                    case "ms":
                        frequencyScale = "Hz";
                        break;
                }
                cursorsDataText2.setText(String.format("Freq: %.1f%s",
                        1000f/Math.abs(timeCursorsValue[1] - timeCursorsValue[0]),
                        frequencyScale));

                break;

            case OFF:
                cursorTypeText.setText("Cursors off");
                cursor1Text.setText("");
                cursor2Text.setText("");
                cursorsDataText1.setText("");
                cursorsDataText2.setText("");

                break;
        }
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
                onBulkMode = true;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        triggerStatus.setImageResource(R.drawable.trigger_rise);
                    }
                });
            }

            @Override
            public void onChangeToContinuous() {
                onBulkMode = false;

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
        triggerTextSmall.setText("Trigger: " + triggerText.getText());

    }

    private void updateTimeScaleText() {
        timeScaleText.setText(Settings.getCurrentTimeScaleCompleteLabel());
        timeScaleTextSmall.setText(timeScaleText.getText() + "/div");
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

    private void updateTriggerSettingsOnDevice() {
        if (receiver != null)
            receiver.sendCommand(Settings.composeTriggerCommand());
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
