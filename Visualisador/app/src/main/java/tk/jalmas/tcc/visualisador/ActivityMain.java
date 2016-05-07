package tk.jalmas.tcc.visualisador;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

public class ActivityMain extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();



        setContentView(R.layout.activity_main);

        getFragmentManager().beginTransaction()
                .add(R.id.container, new FragmentGraph())
                .commit();
    }
}
