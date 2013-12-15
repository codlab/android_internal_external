package eu.codlab.int2ext;

import java.io.File;

import eu.codlab.int2ext.ShellCommand.CommandResult;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class CopyProgram {
	private final static String PREFERENCES = "eu.codlab.int2ext_preferences";
	public final static int APK_DOESNOTEXIST=1<<1;
	public final static int APK_SYS_DOESNOTEXIST=1<<2;
	public final static int APK_SYS_COULDNOTCREATE=1<<3;
	public final static int APK_SYS_EXIST=1<<4;
	public final static int APK_SYS_COULDNOTDELETE = 1<<5;
	public final static int APK_COULDNOTCREATE=1<<6;
	public final static int APK_SYS_SUCCESS=0;
	public final static int CANTSU=1<<7;
	private static ShellCommand cmd;

	private String _internal_mount_point;
	private String _external_mount_point;
	private String _device_block;

	private Context _context;

	private CopyProgram(){
		_internal_mount_point="";
		_external_mount_point="";
		_device_block="";
	}

	public CopyProgram(Context context){
		this();
		_context = context;
		if(cmd == null)
			cmd = new ShellCommand();
		SharedPreferences _sh = context.getSharedPreferences(PREFERENCES, 0);
		if(_sh.getBoolean("customdefault", false)){
			_internal_mount_point=_sh.getString("custominternal", "/mnt/sdcard");
			_external_mount_point=_sh.getString("customexternal", "/mnt/extSdCard");
			_device_block=_sh.getString("customblock", "/dev/block/vold/179:49");
		}else{
			_internal_mount_point=_sh.getString("choosedinternal", "/mnt/sdcard");
			_external_mount_point=_sh.getString("choosedexternal", "/mnt/extSdCard");
			_device_block=_sh.getString("choosedblock", "/dev/block/vold/179:49");
		}
	}


	private String getRemountWrite(String device){
		return "mount -o remount,rw -t yaffs2 "+device+" /system";
	}

	private String getRemountRead(String device){
		return "mount -o remount,ro -t yaffs2 "+device+" /system";
	}


	public boolean isMask(int val, int mask){
		return (val & mask) == mask;
	}

	public String executeSuMount(){

		CommandResult r;// = cmd.su.runWaitFor("mount > /sdcard/mount_list");
		r = cmd.su.runWaitFor("mount");
		String res = r.stdout;
		if(r == null || res == null)
			return null;

		//String res =  executeCommand(getSu()+" -c "+getMount()+" root",null);
		String [] split = res.split("\n");
		String device = null;
		int esp=0;
		for(int i =0;i<split.length && device == null;i++){
			if(split[i].indexOf(" /system") >= 0){
				esp = split[i].indexOf(" ");
				if( esp > 0)
					device = split[i].substring(0,esp);
			}
		}
		return device;
	}

	public boolean saveJSON(String ori){
		CommandResult r= cmd.su.runWaitFor("cat "+ori+" > /data/codlab_int2ext");
		if(r.success() == false)
			return false;
		r =cmd.su.runWaitFor("chmod 744 /data/codlab_int2ext");
		return r.success();
	}

	public String getJSONString(){
		CommandResult r= cmd.sh.runWaitFor("cat /data/codlab_int2ext");
		if(r == null || r.success() == false)
			return null;

		return r.stdout;
	}

	public String getDeviceType(String device){

		CommandResult r= cmd.su.runWaitFor("mount");
		String res = r.stdout;
		if(r == null || res == null)
			return null;

		//String res =  executeCommand(getSu()+" -c "+getMount()+" root",null);
		String [] split = res.split("\n");
		for(int i =0;i<split.length && device != null;i++){
			if(split[i].indexOf(device) >= 0){
				String [] _split = split[i].split(" type ");
				if(_split.length>1){
					//type exists
					_split = _split[1].split(" ");
					return _split[0];
				}else{
					return getAlternateFS(split[i]);
				}

			}
		}
		return null;
	}

	private String getAlternateFS(String line){
		String [] r = line.split(" ");
		if(r.length>3){
			return r[2];
		}
		return null;
	}

	public String getBuild(){
		CommandResult r= cmd.su.runWaitFor("cat /system/build.prop");
		String res = r.stdout;
		if(r == null || res == null)
			return null;
		return res;
	}

	public String getMounts(){

		CommandResult r= cmd.su.runWaitFor("mount");
		String res = r.stdout;
		if(r == null || res == null)
			return null;
		return res;
	}


	public int runProgram(){
		cmd.su.runWaitFor("/system/etc/init.d/11extsd2internalsd");
		return 0;
	}

	public int copyProgram(){
		String device = executeSuMount();
		if(device != null){
			if(cmd.su.runWaitFor(getRemountWrite(device)).success() != true)
				return CopyProgram.APK_SYS_COULDNOTCREATE;
			//CommandResult  r = cmd.su.runWaitFor("ls /data/app/eu.codlab.airplane*");
			cmd.su.runWaitFor("mkdir /system/etc/init.d");
			cmd.su.runWaitFor("echo 'sleep 2'> /system/etc/init.d/11extsd2internalsd");
			cmd.su.runWaitFor("chmod 755 /system/etc/init.d/11extsd2internalsd");
			cmd.su.runWaitFor("echo 'mount -o remount,rw /'>> /system/etc/init.d/11extsd2internalsd");
			cmd.su.runWaitFor("echo 'mkdir -p /data/internal_sd'>> /system/etc/init.d/11extsd2internalsd");
			cmd.su.runWaitFor("echo 'mount -o bind "+_internal_mount_point+" /data/internal_sd'>> /system/etc/init.d/11extsd2internalsd");
			cmd.su.runWaitFor("echo 'mount -t "+getDeviceType(_device_block)+" -o umask=0000 "+_device_block+" "+_internal_mount_point+"'>> /system/etc/init.d/11extsd2internalsd");
			cmd.su.runWaitFor("echo 'mount -o bind /data/internal_sd "+_external_mount_point+"'>> /system/etc/init.d/11extsd2internalsd");
			cmd.su.runWaitFor(getRemountRead(device));
			return CopyProgram.APK_SYS_SUCCESS;
		}else{
			return CopyProgram.APK_SYS_COULDNOTCREATE;
		}
	}

	public static boolean existsProgramBootReceiver(){
		if(cmd == null)
			cmd = new ShellCommand();
		CommandResult t = cmd.su.runWaitFor("/system/etc/11extsd2internalsd");
		return !(t.stderr == null || t.stderr.indexOf("such") >=0);
	}
	public int copyProgramBootReceiver(){
		String c = "mount -o rw,remount /system";
		cmd.su.runWaitFor(c);

		cmd.su.runWaitFor("echo 'sleep 2'> /system/etc/11extsd2internalsd");
		cmd.su.runWaitFor("chmod 755 /system/etc/11extsd2internalsd");
		cmd.su.runWaitFor("echo 'mount -o remount,rw /'>> /system/etc/11extsd2internalsd");
		cmd.su.runWaitFor("echo 'mkdir -p /data/internal_sd'>> /system/etc/11extsd2internalsd");
		cmd.su.runWaitFor("echo 'mount -o bind "+_internal_mount_point+" /data/internal_sd'>> /system/etc/11extsd2internalsd");
		cmd.su.runWaitFor("echo 'mount -t "+getDeviceType(_device_block)+" -o umask=0000 "+_device_block+" "+_internal_mount_point+"'>> /system/etc/11extsd2internalsd");
		cmd.su.runWaitFor("echo 'mount -o bind /data/internal_sd "+_external_mount_point+"'>> /system/etc/11extsd2internalsd");
		return CopyProgram.APK_SYS_SUCCESS;
	}

	public int removeProgram(){
		String device = executeSuMount();
		if(device != null){
			if(cmd.su.runWaitFor(getRemountWrite(device)).success() != true)
				return CopyProgram.APK_SYS_COULDNOTCREATE;
			//CommandResult  r = cmd.su.runWaitFor("ls /data/app/eu.codlab.airplane*");
			cmd.su.runWaitFor("rm /system/etc/init.d/11extsd2internalsd;"+getRemountRead(device));
			return CopyProgram.APK_SYS_SUCCESS;
		}else{
			return CopyProgram.APK_SYS_COULDNOTCREATE;
		}
	}
	public int removeProgramBootReceiver(){
		String device = executeSuMount();
		if(device != null){
			if(cmd.su.runWaitFor(getRemountWrite(device)).success() != true)
				return CopyProgram.APK_SYS_COULDNOTCREATE;
			//CommandResult  r = cmd.su.runWaitFor("ls /data/app/eu.codlab.airplane*");
			cmd.su.runWaitFor("rm /system/etc/11extsd2internalsd;"+getRemountRead(device));
			return CopyProgram.APK_SYS_SUCCESS;
		}else{
			return CopyProgram.APK_SYS_COULDNOTCREATE;
		}
	}

	public void copyProgramBoot(){
		cmd.su.runWaitFor("/system/etc/11extsd2internalsd");
		try{
			_context.sendBroadcast(new Intent("android.intent.action.MEDIA_MOUNTED", Uri.parse("file://"+Environment.getExternalStorageDirectory())));
		}catch(Exception e){

		}
	}

	public int copyProgramSoft(){
		String device = executeSuMount();
		if(device != null){
			cmd.su.runWaitFor("mount -o rw,remount /");
			cmd.su.runWaitFor("mkdir -p /data/internal_sd");
			cmd.su.runWaitFor("mount -o bind "+_internal_mount_point+" /data/internal_sd");
			cmd.su.runWaitFor("mount -t "+getDeviceType(_device_block)+" -o umask=0000 "+_device_block+" "+_internal_mount_point+"");
			cmd.su.runWaitFor("mount -o bind /data/internal_sd "+_external_mount_point+"");
			cmd.su.runWaitFor(getRemountRead(device));
			checkMediaRescan();
			return CopyProgram.APK_SYS_SUCCESS;
		}else{
			return CopyProgram.APK_SYS_COULDNOTCREATE;
		}
	}

	private void checkMediaRescan(){
		try{
			if(_context != null){
				SharedPreferences _sh = _context.getSharedPreferences(PREFERENCES, 0);
				if(_sh != null && _sh.getBoolean("sendrescan", false))
					_context.sendBroadcast(new Intent("android.intent.action.MEDIA_MOUNTED", Uri.parse("file://"+Environment.getExternalStorageDirectory())));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}