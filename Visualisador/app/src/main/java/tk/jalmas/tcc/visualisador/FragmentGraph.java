package tk.jalmas.tcc.visualisador;


import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentGraph extends Fragment {
    private static final String TAG = FragmentGraph.class.getName();

    private MySimpleGraph graph;
    private Receiver receiver;

    private TextView timeScaleText;
    private TextView holdOffText;
    private TextView timeOffsetText;

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
            points [i] = new MySimpleGraph.DataPoint((float) i, (float) data[i]);

        if (lastPosition == -1)
            graph.updateData(points);
        else
            graph.updateData(points, lastPosition);

    }

    private void updateGraph(int[] data){
        updateGraph(data, -1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_graph, container, false);

        graph = (MySimpleGraph) v.findViewById(R.id.graph);

        timeScaleText = (TextView) v.findViewById(R.id.timeScaleText);
        holdOffText = (TextView) v.findViewById(R.id.holdOffText);
        timeOffsetText = (TextView) v.findViewById(R.id.offsetText);

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

        receiver.start();

        return v;
    }

    private void setUpButtonListeners(View v) {
        final Button triggerButton = (Button) v.findViewById(R.id.triggerButton);
        triggerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Settings.setIsTriggerEnabled(!Settings.isTriggerEnabled());
                updateTriggerSettingsOnDevice();

                if (Settings.isTriggerEnabled())
                    triggerButton.setText("TRIG: ON");
                else
                    triggerButton.setText("TRIG: OFF");
            }
        });

        v.findViewById(R.id.timeScaleSelectorDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.decreaseTimeScale();
                updateTimeScaleOnDevice();
                updateTimeScaleText();
            }
        });

        v.findViewById(R.id.timeScaleSelectorUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.increaseTimeScale();
                updateTimeScaleOnDevice();
                updateTimeScaleText();
            }
        });

        v.findViewById(R.id.holdOffSelectorDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.decreaseHoldOff();
                updateHoldOffSettingOnDevice();
                updateHoldOffText();
            }
        });

        v.findViewById(R.id.holdOffSelectorUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.increaseHoldOff();
                updateHoldOffSettingOnDevice();
                updateHoldOffText();
            }
        });

        v.findViewById(R.id.offsetSelectorDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.decreaseTimeOffset();
                updateTimeOffsetSettingOnDevice();
                updateTimeOffsetText();
            }
        });

        v.findViewById(R.id.offsetSelectorUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.increaseTimeOffset();
                updateTimeOffsetSettingOnDevice();
                updateTimeOffsetText();
            }
        });

        final Button startStopButton = (Button) v.findViewById(R.id.startStopButton);
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGraphStopped = !isGraphStopped;
                if (isGraphStopped)
                    startStopButton.setText("START");
                else
                    startStopButton.setText("STOP");

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

        receiver.setErrorHandler(new ErrorHandler() {
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
        super.onDestroy();

        if (receiver != null)
            receiver.cleanup();
    }
}
