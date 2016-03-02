package tk.jalmas.visualisador;


import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentGraph extends Fragment {
    private static final String TAG = FragmentGraph.class.getName();

    private GraphView graph;

    public FragmentGraph() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_graph, container, false);

        int nSamples = 400;
        int offset = 200;
        DataPoint[] dataPoints = new DataPoint[nSamples];

        for (int i = -offset; i < nSamples - offset; i++)
            dataPoints[i + offset] = new DataPoint(i * 2 * Math.PI/nSamples, Math.sin(i * 2 * Math.PI/nSamples));

        graph = (GraphView) v.findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
        graph.addSeries(series);

        v.findViewById(R.id.voltageScaleSelectorUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "voltageScaleSelectorUp");

                graph.setScrollY(graph.getScaleY() / 2.0f);
            }
        });

        v.findViewById(R.id.voltageScaleSelectorDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "voltageScaleSelectorDown");

                graph.setScaleY(graph.getScaleY() * 2.0f);
            }
        });

        return v;
    }


}
