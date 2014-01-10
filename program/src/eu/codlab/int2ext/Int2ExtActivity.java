package eu.codlab.int2ext;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.vending.util.IabHelper;
import com.android.vending.util.IabResult;
import com.android.vending.util.Inventory;
import com.android.vending.util.Purchase;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Int2ExtActivity extends FragmentActivity
        implements IabHelper.QueryInventoryFinishedListener,
        IabHelper.OnIabPurchaseFinishedListener, IabHelper.OnConsumeFinishedListener {
    private InternalExternalPagerAdapter _adapter;

    private ViewPager _pager;
    private TabPageIndicator _titleIndicator;

    class InternalExternalPagerAdapter extends FragmentPagerAdapter {
        private ChoiceFragment _main;
        private HelpFragment _help;
        private Prefs _fragment;
        public InternalExternalPagerAdapter(FragmentManager fm) {
            super(fm);
            _main = null;
            _fragment = null;
            _help = null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position){
            if(position == 0){
                return getString(R.string.main_title);
            }else if(position == 1){
                return getString(R.string.help_title);
            }else if(position == 2){
                return getString(R.string.settings_title);
            }
            return "";
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0){
                if(_main == null)_main = new ChoiceFragment();
                return _main;
            }else if(position == 1){
                if(_help == null)_help = new HelpFragment();
                return _help;
            }else if(position == 2){
                if(_fragment == null)_fragment = new Prefs();
                return _fragment;
            }
            return null;
        }

        public void downloadUpdate(){
            if(_fragment == null)_fragment = new Prefs();
            _fragment.downloadUpdate();
        }

        public boolean isPopable(){
            return _pager != null && _pager.getCurrentItem() > 0;
        }


    }
    private boolean _playstore_ok;


    private Random _random;
    private IabHelper mHelper;


    private boolean _was_don_1 = false;
    private boolean _was_don_2 = false;
    private String base64EncodedPublicKey="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoBrxvOEVfZwjKXu7GTZJoMAIF9C3TkTwryrYuar1+z2Hkn5l5YIE49exfArUjL65xBuOFrhQ2zUZLxnMfbTixuXxR2g1JFTktVdCvl96eGWU+jYkLTDuovLO2JMtTFT/niHrWUaZ7OiuziqSY5HgERYVzt+CA2j0mmr8F2T+G88T5sBHY9gz0lSHN5ErU+mMjmv9vfYuGdLBvoRhXAY8XYfT9YtH+2+cfbV9UqwnhHWmvz70eZQLt9wU7gGiCCMv2itjdbbdk4T+4gXXd8v6Yb07Hl0upe/7BTbB9iTbmpXo8CAaVdX5VJaLYM7FzOj0jFGmtB7dXw4ctMVcmhcepwIDAQAB";
    private IabHelper getHelper(){
        if(mHelper == null)mHelper = new IabHelper(this, base64EncodedPublicKey);
        return mHelper;
    }
    public void initHelper(){
        getHelper().startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(final IabResult result) {
                _playstore_ok = true;
                runOnUiThread(new Runnable(){
                    public void run(){
                        if (!result.isSuccess()) {
                            //Toast.makeText(Int2ExtActivity.this, "Problem setting up inapp " + result, Toast.LENGTH_LONG);
                            // Oh noes, there was a problem.
                        }else{
                            onPlaystoreOK();
                        }
                    }
                });
                // Hooray, IAB is fully set up!
            }
        });
    }
    public void createDonationDialog(boolean don1_purchased, boolean don2_purchased){
        if(don1_purchased == true && don2_purchased == true){
            return;
        }

        if(!_playstore_ok){
            try{
                _was_don_1 = true;
                initHelper();
            }catch(Exception e){

            }
        }else{
            AlertDialog alertDiaLog = new AlertDialog.Builder(this).create();
            alertDiaLog.setTitle(R.string.dialog_donation_title);
            alertDiaLog.setMessage(getString(R.string.dialog_donation_message));
            alertDiaLog.setButton(getString(R.string.dialog_donation_no_thx), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    arg0.dismiss();
                }
            });
            if(don1_purchased == false){
                alertDiaLog.setButton2(getString(R.string.dialog_donation_mini), new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        try{
                            getHelper().launchPurchaseFlow(Int2ExtActivity.this, "don1", 01,
                                Int2ExtActivity.this, _random.nextInt(1353676232)+"");
                        }catch(Exception e){

                        }
                        arg0.dismiss();
                    }
                });
            }
            if(don2_purchased == false){
                alertDiaLog.setButton3(getString(R.string.dialog_donation_max), new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        try{
                            getHelper().launchPurchaseFlow(Int2ExtActivity.this, "don2", 02,
                                Int2ExtActivity.this, _random.nextInt(1353676232)+"");
                        }catch(Exception e){

                        }
                        arg0.dismiss();
                    }
                });
            }
            alertDiaLog.show();

        }

    }

    public void onPlaystoreOK(){
        try{
            List additionalSkuList = new ArrayList();
            additionalSkuList.add("don1");
            additionalSkuList.add("don2");
            getHelper().queryInventoryAsync(true, additionalSkuList,
                    this);
        }catch(Exception e){

        }
    }


    @Override
	public void onCreate(Bundle savedInstanceState) {
        _random = new Random();
        super.onCreate(savedInstanceState);

        _playstore_ok = false;
        _was_don_1 = false;
        _was_don_2 = false;


        initHelper();

		//_fragment = null;
        setContentView(R.layout.activity_int2_ext_acitivty);

        _adapter = new InternalExternalPagerAdapter(getSupportFragmentManager());

        _pager = (ViewPager)findViewById(R.id.pager);
        _pager.setAdapter(_adapter);
        setTop(false);

        _titleIndicator = (TabPageIndicator)findViewById(R.id.titles);
        _titleIndicator.setViewPager(_pager);

        _pager.setCurrentItem(0);



	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
            case R.id.main_donation:
                this.createDonationDialog(false, false);
                return true;
		case R.id.paypal:
			Uri uri = Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=2EPFUE8UY99F4");
			startActivity(new Intent(Intent.ACTION_VIEW,uri));
			return true;
		case android.R.id.home:
			if(_adapter != null && _pager != null){
				pop();
				return true;
			}
			break;
		/*case R.id.menu_download:
			if(_pager != null && _adapter != null){
				_adapter.downloadUpdate();
				return true;
			}*/
		case R.id.menu_settings:
			if(_pager != null && _adapter != null){
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
        if(_pager != null){
            _pager.setCurrentItem(2);
            setTop(true);
        }

    }

	public void onBackPressed(){
		if(_adapter != null && _pager != null && _adapter.isPopable()){
			pop();
		}else{
			super.onBackPressed();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle save){
		super.onSaveInstanceState(save);
	}

	@Override
	public void onRestoreInstanceState(Bundle restore){
		super.onRestoreInstanceState(restore);
	}

	@Override
	public void onResume(){
		super.onResume();

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

        if(_pager != null && _adapter != null && _adapter.isPopable())
            setTop(true);

	}

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setTop(boolean home_enabled){
        if(Build.VERSION.SDK_INT > 11)
            getActionBar().setDisplayHomeAsUpEnabled(home_enabled);
    }

	public void pop(){
        if(_adapter != null && _pager != null && _adapter.isPopable()){
            _pager.setCurrentItem(0);
            setTop(false);
        }
	}

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (getHelper() != null) getHelper().dispose();
        mHelper = null;
    }
    @Override
    public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
        if (result.isFailure()) {
            return;
        }

        String don1 =
                inventory.getSkuDetails("don1").getPrice();
        String don2 =
                inventory.getSkuDetails("don2").getPrice();

        if(inventory.hasPurchase("don1")){
            getHelper().consumeAsync(inventory.getPurchase("don1"),
                    this);
        }
        if(inventory.hasPurchase("don2")){
            getHelper().consumeAsync(inventory.getPurchase("don2"),
                    this);
        }

        if(_random.nextInt(100) < 20)
            createDonationDialog(inventory.hasPurchase("don1"),inventory.hasPurchase("don2"));

        // update the UI
    }
    @Override
    public void onIabPurchaseFinished(final IabResult result, final Purchase info) {
        runOnUiThread(new Runnable(){
            public void run(){
                if (result.isFailure()) {
                    return;
                }else if (info.getSku().equals("don1")) {
                    Toast.makeText(Int2ExtActivity.this, R.string.purchased_don1, Toast.LENGTH_LONG);
                }else if (info.getSku().equals("don2")) {
                    Toast.makeText(Int2ExtActivity.this, R.string.purchased_don2, Toast.LENGTH_LONG);
                }
            }
        });
    }
    @Override
    public void onConsumeFinished(Purchase purchase, IabResult result) {
    }

    //create the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.activity_int2_ext_activity, menu);
        return true;
    }

}
