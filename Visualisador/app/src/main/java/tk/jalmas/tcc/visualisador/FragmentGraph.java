package tk.jalmas.tcc.visualisador;


import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentGraph extends Fragment {
    private static final String TAG = FragmentGraph.class.getName();

    private MySimpleGraph graph;
    private TextView timeScaleText;
    private Receiver receiver;

    private Handler refreshHandler = null;
    Runnable refresherRunnable = new Runnable() {
        @Override
        public void run() {
            if (receiver != null) {
                receiver.sendCommand(Settings.composeTimeScaleCommand());
                receiver.sendCommand(Settings.composeTriggerCommand());
            }

            refreshHandler.postDelayed(refresherRunnable, 2000);
        }
    };
    public FragmentGraph() {
        // Required empty public constructor
    }

    private void updateGraph(int[] data, int lastPosition){
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

        refreshHandler = new Handler();
        refresherRunnable.run();

        receiver = new Receiver(getActivity());
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

        receiver.setOnChangeSendingModeListener(new OnSendingModeChangeListener() {
            @Override
            public void onChangeToBulk() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "onChangeToBulk", Toast.LENGTH_SHORT).show();

                        Settings.setCurrentTimeScaleAsLastContinuous();
                        updateSettingOnDevice(Settings.getSetLastTimeScaleBulkCommand());
                        timeScaleText.setText(Settings.getCurrentTimeScaleLabel());
                    }
                });
            }

            @Override
            public void onChangeToContinuous() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "onChangeToContinuous", Toast.LENGTH_SHORT).show();

                        Settings.setCurrentTimeScaleAsLastBulk();
                        updateSettingOnDevice(Settings.getSetLastTimeScaleContinuousCommand());
                        timeScaleText.setText(Settings.getCurrentTimeScaleLabel());
                    }
                });
            }
        });

        receiver.start();

        v.findViewById(R.id.toggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Settings.setIsTriggerEnabled(!Settings.isTriggerEnabled());
                updateTriggerSettingsOnDevice();
            }
        });

        timeScaleText.setText(Settings.getCurrentTimeScaleLabel());

        v.findViewById(R.id.timeScaleSelectorDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.decreaseTimeScale();
                updateTimeScaleText();
                if (receiver != null && receiver.getCurrentState() == Receiver.STATES.CONTINUOUS)
                    Settings.setCurrentTimeScaleAsLastContinuous();
                else
                    Settings.setCurrentTimeScaleAsLastBulk();
                updateTimeScaleOnDevice();
            }
        });

        v.findViewById(R.id.timeScaleSelectorUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.increaseTimeScale();
                updateTimeScaleText();
                if (receiver != null && receiver.getCurrentState() == Receiver.STATES.CONTINUOUS)
                    Settings.setCurrentTimeScaleAsLastContinuous();
                else
                    Settings.setCurrentTimeScaleAsLastBulk();
                updateTimeScaleOnDevice();
            }
        });

        return v;
    }

    private void updateTimeScaleText() {
        timeScaleText.setText(Settings.getCurrentTimeScaleLabel());
    }

    private void updateTimeScaleOnDevice() {
        if (receiver != null)
            receiver.sendCommand(Settings.composeTriggerCommand());
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
