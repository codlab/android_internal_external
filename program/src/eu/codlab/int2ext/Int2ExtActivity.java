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
import android.widget.Toast;

import com.android.vending.util.IabHelper;
import com.android.vending.util.IabResult;
import com.android.vending.util.Inventory;
import com.android.vending.util.Purchase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Int2ExtActivity extends Activity
        implements IabHelper.QueryInventoryFinishedListener,
        IabHelper.OnIabPurchaseFinishedListener, IabHelper.OnConsumeFinishedListener {
    private Random _random;
    private IabHelper mHelper;

    private ChoiceFragment _main;
	private Prefs _fragment;

    public void createDonationDialog(boolean don1_purchased, boolean don2_purchased){
        if(don1_purchased == true && don2_purchased == true){
            return;
        }
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
                    mHelper.launchPurchaseFlow(Int2ExtActivity.this, "don1", 01,
                            Int2ExtActivity.this, _random.nextInt(1353676232)+"");
                    arg0.dismiss();
                }
            });
        }
        if(don2_purchased == false){
            alertDiaLog.setButton3(getString(R.string.dialog_donation_max), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    mHelper.launchPurchaseFlow(Int2ExtActivity.this, "don2", 02,
                            Int2ExtActivity.this, _random.nextInt(1353676232)+"");
                    arg0.dismiss();
                }
            });
        }
        alertDiaLog.show();
    }

    public void onPlaystoreOK(){
        try{
            List additionalSkuList = new ArrayList();
            additionalSkuList.add("don1");
            additionalSkuList.add("don2");
            mHelper.queryInventoryAsync(true, additionalSkuList,
                    this);
        }catch(Exception e){

        }
    }


    @Override
	public void onCreate(Bundle savedInstanceState) {
        _random = new Random();
        super.onCreate(savedInstanceState);

        String base64EncodedPublicKey="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoBrxvOEVfZwjKXu7GTZJoMAIF9C3TkTwryrYuar1+z2Hkn5l5YIE49exfArUjL65xBuOFrhQ2zUZLxnMfbTixuXxR2g1JFTktVdCvl96eGWU+jYkLTDuovLO2JMtTFT/niHrWUaZ7OiuziqSY5HgERYVzt+CA2j0mmr8F2T+G88T5sBHY9gz0lSHN5ErU+mMjmv9vfYuGdLBvoRhXAY8XYfT9YtH+2+cfbV9UqwnhHWmvz70eZQLt9wU7gGiCCMv2itjdbbdk4T+4gXXd8v6Yb07Hl0upe/7BTbB9iTbmpXo8CAaVdX5VJaLYM7FzOj0jFGmtB7dXw4ctMVcmhcepwIDAQAB";

        // compute your public key and store it in base64EncodedPublicKey
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(final IabResult result) {
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

		_fragment = null;

		setContentView(R.layout.activity_int2_ext_acitivty);


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
			xact.replace(R.id.int2ext_fragment, _fragment);

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
			fragmentTransaction.replace(R.id.int2ext_fragment, _main);
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

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }
    @Override
    public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
        if (result.isFailure()) {
            //Log.d("ERROR",result.getMessage());
            return;
        }

        String don1 =
                inventory.getSkuDetails("don1").getPrice();
        String don2 =
                inventory.getSkuDetails("don2").getPrice();
        //Log.d("DON",don1+" "+don2);

        if(inventory.hasPurchase("don1")){
            mHelper.consumeAsync(inventory.getPurchase("don1"),
                    this);
        }
        if(inventory.hasPurchase("don2")){
            mHelper.consumeAsync(inventory.getPurchase("don2"),
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


}
