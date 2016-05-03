package tk.jalmas.tcc.visualisador;


import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


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
                //receiver.sendCommand(Settings.composeTriggerCommand());
            }

            refreshHandler.postDelayed(refresherRunnable, 2000);
        }
    };
    public FragmentGraph() {
        // Required empty public constructor
    }
/*
    private void updateGraph(){
        if (periodical++ != 0) {
            int size = 40;
            MySimpleGraph.DataPoint[] data = new MySimpleGraph.DataPoint[size];
            for (int i = 0; i < size; i++) {
                float sin = (float) (128.0 * Math.sin(2.0 * Math.PI * i / (size / 1) + ((double) periodical * 0.001) ) + 128.0);
                data[i] = new MySimpleGraph.DataPoint((float) i, sin);
            }
            graph.updateData(data);
        }
    }
*/
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

        receiver.start();

        v.findViewById(R.id.toggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Settings.setIsTriggerEnabled(!Settings.isTriggerEnabled());

            }
        });

        timeScaleText.setText(Settings.getCurrentTimeScaleLabel());

        v.findViewById(R.id.timeScaleSelectorDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.decreaseTimeScale();
                timeScaleText.setText(Settings.getCurrentTimeScaleLabel());
            }
        });

        v.findViewById(R.id.timeScaleSelectorUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.increaseTimeScale();
                timeScaleText.setText(Settings.getCurrentTimeScaleLabel());
            }
        });

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (receiver != null)
            receiver.cleanup();
    }
}
