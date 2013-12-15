package eu.codlab.int2ext;

import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;

public class Prefs extends PreferenceFragment implements OnSharedPreferenceChangeListener{
	ArrayList<RomConfig> _roms;
	PreferenceCategory	_custom;
	
	private void populateList(){
		_roms.clear();
		CopyProgram c = new CopyProgram(getActivity());
		String json_string = c.getJSONString();
		try {
			JSONObject json_formatted = new JSONObject(json_string);
			if(json_formatted != null && json_formatted.has("c")){
				JSONArray array = json_formatted.getJSONArray("c");
				for(int i=0;i<array.length();i++){
					JSONObject o = array.getJSONObject(i);
					if(o.has("t") && o.has("s") && o.has("k") && o.has("i") && o.has("e") && o.has("d")){
						_roms.add(new RomConfig(o.getString("t"), o.getString("s"), o.getString("k"), o.getString("i"),
								o.getString("e"), o.getString("d")));
					}
				}
			}
		} catch (Exception e) {
			_roms.add(new RomConfig("Default SGS3 Roms", "This conf will work with many roms", "rom1", "/mnt/sdcard",
					"/mnt/extSdCard", "/dev/block/vold/179:49"));
		}
	}
	
	private void populateConfList(){
		PreferenceCategory targetCategory = (PreferenceCategory)findPreference("rom");
		targetCategory.removeAll();
		CheckBoxPreference _check = null;

		String actualkey = this.getPreferenceManager().getSharedPreferences().getString("romchoose", "--");
		for(RomConfig rom : _roms){
			_check=new CheckBoxPreference(getActivity());
			_check.setDefaultValue(actualkey.equals(rom.getId()));
			_check.setKey(rom.getId());
			_check.setSummary(rom.getSummary());
			_check.setTitle(rom.getName());
			targetCategory.addPreference(_check);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		/*_roms.add(new RomConfig("DLilol", "Test", "rom2", "/mnt/sd",
				"/mnt/extSd", "nothing"));*/
		super.onCreate(savedInstanceState);
		_roms = new ArrayList<RomConfig>();
		populateList();
		setHasOptionsMenu(true);
		addPreferencesFromResource(R.xml.preferences);
		//CopyProgram.setPref(this.getPreferenceManager().getSharedPreferencesName());
		_custom = (PreferenceCategory)findPreference("custompreferences");
		PreferenceCategory mounts = (PreferenceCategory)findPreference("mounts");
		createMountsViewClick(mounts);

		populateConfList();

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.activity_prefs, menu);
	}

	private void createMountsViewClick(PreferenceCategory cat){
		Preference mnt_click = new Preference(getActivity());
		mnt_click.setTitle(R.string.prefclicktomount);
		mnt_click.setSummary(R.string.prefclicktomountexplain);
		mnt_click.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {
				initMountsView();
				return false;
			}

		});
		cat.addPreference(mnt_click);
	}

	private void initMountsView(){
		final PreferenceCategory cat = (PreferenceCategory)findPreference("mounts");
		getActivity().runOnUiThread(new Runnable(){
			@Override
			public void run(){
				if(cat != null){
					cat.removeAll();
					createMountsViewClick(cat);
					CopyProgram c = new CopyProgram(getActivity());
					final String _build = c.getBuild(); 
					final String _mount = c.getMounts();
					if(_mount == null){
						getActivity().runOnUiThread(new Runnable(){
							@Override
							public void run(){
								Toast.makeText(getActivity(), R.string.errormount, Toast.LENGTH_SHORT).show();
							}
						});
						return;
					}
					String [] lines = _mount.split("\n");
					Preference mnt = null;
					boolean _trouve;

					mnt = new Preference(getActivity());
					mnt.setTitle(R.string.sendmailtitle);
					mnt.setSummary(R.string.sendmailsummary);
					mnt.setOnPreferenceClickListener(new OnPreferenceClickListener(){
						@Override
						public boolean onPreferenceClick(Preference preference) {
							String emailList[] = {"pokeke100@gmail.com"};
							Intent intent = new Intent(Intent.ACTION_SEND);  
							intent.setType("plain/text");
							intent.putExtra(Intent.EXTRA_EMAIL, emailList);   
							intent.putExtra(Intent.EXTRA_SUBJECT, "[INT/EXT] Mount points");  
							intent.putExtra(Intent.EXTRA_TEXT, "Hi, "+_mount);
							startActivity(Intent.createChooser(intent, "Send me a mail!"));
							return false;
						}
					});
					cat.addPreference(mnt);

					mnt = new Preference(getActivity());
					mnt.setTitle(R.string.sendmailtitlebuild);
					mnt.setSummary(R.string.sendmailbuildsummary);
					mnt.setOnPreferenceClickListener(new OnPreferenceClickListener(){
						@Override
						public boolean onPreferenceClick(Preference preference) {
							String emailList[] = {"pokeke100@gmail.com"};
							Intent intent = new Intent(Intent.ACTION_SEND);  
							intent.setType("plain/text");
							intent.putExtra(Intent.EXTRA_EMAIL, emailList);   
							intent.putExtra(Intent.EXTRA_SUBJECT, "[INT/EXT] Mount points and build.prop");  
							intent.putExtra(Intent.EXTRA_TEXT, "Hi, "+_mount+" \n \n"+_build);
							startActivity(Intent.createChooser(intent, "Send me a mail!"));
							return false;
						}
					});
					cat.addPreference(mnt);
					for(String line : lines){
						String [] split = line.split(" ");
						if(split.length > 1){
							_trouve=false;
							mnt = new Preference(getActivity());
							mnt.setTitle(split[0]);
							final String dev = split[0];
							for(int i=0;i<split.length-1 && !_trouve;i++){
								if("type".equals(split[i])){
									mnt.setSummary("FileSystem : "+split[i+1]);
									_trouve = true;
								}
							}
							if(!_trouve && split.length>2){
								mnt.setSummary("fs : "+split[2]+" (result from mount)");
							}else if(!_trouve){
								mnt.setSummary("FS Error avoid this one");
							}
							mnt.setOnPreferenceClickListener(new OnPreferenceClickListener(){
								@Override
								public boolean onPreferenceClick(Preference preference) {
									getPreferenceManager().getSharedPreferences().edit().putString("customblock", dev).commit();
									EditTextPreference device_preference = (EditTextPreference)_custom.findPreference("customblock");
									device_preference.getEditText().setText(dev);
									return false;
								}
							});
							cat.addPreference(mnt);
						}
					}
				}
			}
		});
	}
	@Override
	public void onViewCreated(View view, Bundle d){
		super.onViewCreated(view, d);
	}

	public void register(){
		this.getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	public void unregister(){
		this.getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onResume(){
		super.onResume();
		register();
	}

	@Override
	public void onPause(){
		unregister();
		super.onPause();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, final String arg1) {
		RomConfig _find = null;

		for(RomConfig conf : _roms){
			if(arg1.equals(conf.getId())){
				_find = conf;
			}
		}

		final RomConfig find = _find;
		if(find != null){
			unregister();


			PreferenceCategory targetCategory = (PreferenceCategory)findPreference("rom");
			CheckBoxPreference _romconf;
			for(int i=0;i<targetCategory.getPreferenceCount();i++){
				if(targetCategory.getPreference(i) instanceof CheckBoxPreference){
					_romconf = (CheckBoxPreference) targetCategory.getPreference(i);
					_romconf.setChecked(arg1.equals(_roms.get(i).getId()));

				}
			}

			if(getPreferenceManager().getSharedPreferences().getBoolean(arg1, true) != false){

				Editor edit = this.getPreferenceManager().getSharedPreferences().edit();
				for(RomConfig conf : _roms){
					if(!arg1.equals(conf.getId())){
						edit.putBoolean(conf.getId(), false);
					}
				}
				edit.putString("roomchoose", arg1);
				edit.putString("choosedinternal", find.getInternal());
				edit.putString("choosedexternal", find.getExternal());
				edit.putString("choosedblock", find.getDevice());
				edit.commit();
				getActivity().runOnUiThread(new Runnable(){
					@Override
					public void run(){
						CopyProgram c = new CopyProgram(getActivity());
						String type = c.getDeviceType(find.getDevice());
						if(type != null){
							Toast.makeText(getActivity(),"Config set to "+find.getDevice()+" with type "+type+". The internal and external are "+find.getExternal()+" "+find.getInternal(), Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(getActivity(),find.getDevice()+" could not have been found ! It has not been disable but you should avoid use this configuration with this phone!", Toast.LENGTH_SHORT).show();
						}
					}
				});
			}else{
				arg0.edit().putBoolean(arg1, true).commit();
				getActivity().runOnUiThread(new Runnable(){
					@Override
					public void run(){
						Toast.makeText(getActivity(), R.string.cantuncheck, Toast.LENGTH_SHORT).show();
					}
				});
			}
			register();
		}else{
			if(arg1.equals("customblock")){
				CopyProgram c = new CopyProgram(getActivity());
				final String type = c.getDeviceType(getPreferenceManager().getSharedPreferences().getString("customblock", ""));
				if(type == null){
					getActivity().runOnUiThread(new Runnable(){
						@Override
						public void run(){
							Toast.makeText(getActivity(), "Warning, "+getPreferenceManager().getSharedPreferences().getString("customblock", "")+" seems to be not a valid device block for the sdcard", Toast.LENGTH_SHORT).show();
						}
					});
				}else{
					getActivity().runOnUiThread(new Runnable(){
						@Override
						public void run(){
							Toast.makeText(getActivity(), "It seems that "+getPreferenceManager().getSharedPreferences().getString("customblock", "")+"  has a "+type+" type", Toast.LENGTH_SHORT).show();
						}
					});
				}
			}

			getActivity().runOnUiThread(new Runnable(){
				@Override
				public void run(){
					Toast.makeText(getActivity(), R.string.warningchanged, Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
	
	public void downloadUpdate(){
		AsyncDownloadJSON a = new AsyncDownloadJSON(this);
		URL [] r = new URL[1];
		try {
			r[0] = new URL("http://www.codlab.eu/int2ext.json");
			a.execute(r);
		} catch (Exception e) {
			jsonDownloadFailure();
			e.printStackTrace();
		}
	}

	public void jsonDownloadFailure(){
		getActivity().runOnUiThread(new Runnable(){
			@Override
			public void run(){
				Toast.makeText(getActivity(), R.string.downloaderror, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void jsonDownloadOk(){
		populateList();
		
		getActivity().runOnUiThread(new Runnable(){
			@Override
			public void run(){
				populateConfList();
			}
		});
	}

}
