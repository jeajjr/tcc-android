package tk.jalmas.visualisador;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by jeajjr on 15/09/2015.
 */
public class ActivityMain extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        getFragmentManager().beginTransaction()
                .add(R.id.container, new FragmentGraph())
                .commit();
    }
}
