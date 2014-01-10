package eu.codlab.int2ext;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

/**
 * This fragment is the first one which can be seen
 * @author kevin le perf
 *
 */
public class HelpFragment extends Fragment {
	@Override
	public void onCreate(Bundle s){
		super.onCreate(s);
        setHasOptionsMenu(false);

	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View mainView = inflater.inflate(R.layout.helpfragment, container, false);
		
		return mainView;

	}

	@Override
	public void onViewCreated(View view, Bundle saveInstanceState){

	}

	//create the menu
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		//inflater.inflate(R.menu.activity_int2_ext_activity, menu);
	}
}
