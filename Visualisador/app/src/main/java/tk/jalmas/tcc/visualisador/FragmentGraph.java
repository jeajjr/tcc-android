package tk.jalmas.tcc.visualisador;


import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import tk.jalmas.tcc.visualisador.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentGraph extends Fragment {
    private static final String TAG = FragmentGraph.class.getName();

    //private GraphView graph;

    static int periodical = 0;

    private MySimpleGraph graph;
    private TextView timeScaleText;

    private Handler refreshHandler = null;
    Runnable refresherRunnable = new Runnable() {
        @Override
        public void run() {
            //updateGraph();

            refreshHandler.postDelayed(refresherRunnable, 100);
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
        graph.updateData(points, lastPosition);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_graph, container, false);

        graph = (MySimpleGraph) v.findViewById(R.id.graph);
        timeScaleText = (TextView) v.findViewById(R.id.timeScaleText);

        v.findViewById(R.id.toggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "toggle button");
            }
        });

        refreshHandler = new Handler();
        //refresherRunnable.run();

        Receiver receiver = new Receiver(getActivity());
        receiver.setUpdater(new Updater() {
            @Override
            public void onUpdate(int[] data) {

            }

            @Override
            public void onUpdate(final int[] data, final int lastPosition) {
                //System.out.println("onUpdate received " + lastPosition);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timeScaleText.setText("" + lastPosition);

                        updateGraph(data, lastPosition);
                    }
                });
            }
        });

        receiver.start();

/*
        for (int i = -offset; i < nSamples - offset; i++)
            dataPoints[i + offset] = new DataPoint(i * 2 * Math.PI/nSamples, Math.sin(i * 2 * Math.PI/nSamples));

        graph = (GraphView) v.findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
        graph.addSeries(series);
        graph.onDataChanged(false, false);

        v.findViewById(R.id.toggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "toggle button");

            }
        });

        v.findViewById(R.id.voltageScaleSelectorUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "voltageScaleSelectorUp");

                //graph.setScrollY((int) (graph.getScaleY() / 2.0f));
            }
        });

        v.findViewById(R.id.voltageScaleSelectorDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "voltageScaleSelectorDown");
            }
        });

        */

        return v;
    }
}
