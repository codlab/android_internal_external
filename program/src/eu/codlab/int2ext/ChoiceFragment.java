package eu.codlab.int2ext;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
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
public class ChoiceFragment extends Fragment {
	@Override
	public void onCreate(Bundle s){
		super.onCreate(s);
        setHasOptionsMenu(true);

	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View mainView = inflater.inflate(R.layout.choicefragment, container, false);
		
		SharedPreferences _sh = getActivity().getSharedPreferences("INTEXT_PREFS", 0);
		if(!_sh.getBoolean("VIEWED", false)){
			_sh.edit().putBoolean("VIEWED", true).commit();
		}
		
		return mainView;

	}

	@Override
	public void onViewCreated(View view, Bundle saveInstanceState){

		// External 2 Internal
		Button b = (Button)view.findViewById(R.id.int2ext_go);
		b.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				CopyProgram c = new CopyProgram(getActivity());
				if(CopyProgram.APK_SYS_SUCCESS == c.copyProgramSoft()){
                    Toast.makeText(getActivity(), R.string.swap_ok, Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getActivity(), R.string.swap_ko, Toast.LENGTH_LONG).show();
                }
			}

		});

		//External 2 Internal from init.d
		/*b = (Button)view.findViewById(R.id.int2ext_goinit);
		b.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				CopyProgram c = new CopyProgram(getActivity());
				c.copyProgram();
			}

		});*/

		//External 2 Internal per BOOT COMPLETED RECEIVER
		CheckBox c = (CheckBox)view.findViewById(R.id.int2ext_goboot);
		c.setChecked(CopyProgram.existsProgramBootReceiver());
		c.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				CopyProgram c = new CopyProgram(getActivity());
				if(arg1){
					c.copyProgramBootReceiver();
				}else{
					c.removeProgramBootReceiver();
				}
			}

		});

	}

	//create the menu
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.activity_main, menu);
	}
}
