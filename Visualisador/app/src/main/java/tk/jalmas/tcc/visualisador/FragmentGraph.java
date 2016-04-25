package tk.jalmas.tcc.visualisador;


import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import tk.jalmas.tcc.visualisador.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentGraph extends Fragment {
    private static final String TAG = FragmentGraph.class.getName();

    private GraphView graph;

    final int nSamples = 400;
    final int offset = 200;
    final DataPoint[] dataPoints = new DataPoint[nSamples];
    static int periodical = 0;

    private Handler refreshHandler = null;
    Runnable refresherRunnable = new Runnable() {
        @Override
        public void run() {
            updateGraph();
            refreshHandler.postDelayed(refresherRunnable, 1500);
            Log.d(TAG, "last update tag updated");
        }
    };
    public FragmentGraph() {
        // Required empty public constructor
    }

    private void updateGraph(){
        DataPoint[] dataPoints = new DataPoint[nSamples];

        for (int i = -offset; i < nSamples - offset; i++)
            dataPoints[i + offset] = new DataPoint(i * 2 * Math.PI/nSamples, Math.sin(i * 2 * Math.PI/nSamples + periodical * Math.PI/2) );
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
        graph.removeAllSeries();
        graph.addSeries(series);
        periodical++;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_graph, container, false);



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

        refreshHandler = new Handler();
        refresherRunnable.run();
        return v;
    }


}
