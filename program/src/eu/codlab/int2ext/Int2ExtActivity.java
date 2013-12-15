package eu.codlab.int2ext;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.MenuItem;

public class Int2ExtActivity extends Activity {
	private ChoiceFragment _main;
	private Prefs _fragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_fragment = null;

		setContentView(R.layout.activity_int2_ext_acitivty);


	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.paypal:
			Uri uri = Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=2EPFUE8UY99F4");
			startActivity(new Intent(Intent.ACTION_VIEW,uri));
			return true;
		case android.R.id.home:
			if(_fragment != null){
				pop();
				return true;
			}
			break;
		case R.id.menu_download:
			if(_fragment != null){
				_fragment.downloadUpdate();
				return true;
			}
		case R.id.menu_settings:
			if(_fragment == null){
				create();
				return true;
			}
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void create(){
		if(_fragment == null){
			_fragment = new Prefs();
			FragmentManager fm = getFragmentManager();
			FragmentTransaction xact = fm.beginTransaction();
			xact.setCustomAnimations(R.anim.slidein_fromtop, R.anim.slideout_totop,R.anim.slidein_fromtop,R.anim.slideout_totop);
			xact.replace(R.int2ext.fragment, _fragment);

			xact.addToBackStack(null);

			xact.commit();
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	public void onBackPressed(){
		if(_fragment != null){
			pop();
		}else{
			if(getFragmentManager().getBackStackEntryCount() >=0){
				while(getFragmentManager().getBackStackEntryCount()>0)
					getFragmentManager().popBackStackImmediate();
			}
			super.onBackPressed();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle save){
		if(_main != null)
			getFragmentManager().putFragment(save, "MAIN", _main);
		if(_fragment != null)
			getFragmentManager().putFragment(save, "PREFS", _fragment);
		super.onSaveInstanceState(save);
	}

	@Override
	public void onRestoreInstanceState(Bundle restore){
		super.onRestoreInstanceState(restore);

		if(restore.containsKey("MAIN"))
			_main =  (ChoiceFragment) getFragmentManager().getFragment(restore, "MAIN");

		if(restore.containsKey("PREFS"))
			_fragment =  (Prefs) getFragmentManager().getFragment(restore, "PREFS");
	}

	@Override
	public void onResume(){
		super.onResume();
		FragmentManager fragmentManager = getFragmentManager();
		if(_main == null){
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

			_main = new ChoiceFragment();
			fragmentTransaction.replace(R.int2ext.fragment, _main);
			fragmentTransaction.commit();
		}

		SharedPreferences _sh = getSharedPreferences("INTEXT_PREFS", 0);
		if(!_sh.getBoolean("VIEWED", false)){

			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle(R.string.warning);
			alertDialog.setMessage(this.getText(R.string.firsttime));
			alertDialog.setButton(this.getText(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					create();
				} }); 
			alertDialog.setButton2(this.getText(R.string.cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}}); 
			alertDialog.show();
		}

		if(_fragment != null)
			getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	public void pop(){
		if(_fragment != null){
			FragmentManager fm = getFragmentManager();
			FragmentTransaction xact = fm.beginTransaction();
			xact.setCustomAnimations(R.anim.slidein_fromtop, R.anim.slideout_totop,R.anim.slidein_fromtop,R.anim.slideout_totop);
			xact.remove(_fragment);
			xact.commit();
			fm.popBackStackImmediate();
			_fragment = null;
			//fm.popBackStackImmediate();
			getActionBar().setDisplayHomeAsUpEnabled(false);
		}
	}


}
