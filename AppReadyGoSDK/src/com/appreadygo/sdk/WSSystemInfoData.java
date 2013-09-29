package com.appreadygo.sdk;

import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.telephony.TelephonyManager;

final class WSSystemInfoData implements IWSJsonConvert {
	private final static String TAG = WSSystemInfoData.class.getSimpleName();

	public WSSystemInfoData(Context ctx) {
		TelephonyManager tm = (TelephonyManager) ctx
				.getSystemService(Context.TELEPHONY_SERVICE);
		brn = Build.BRAND;
		den = Build.DEVICE;
		din = Build.DISPLAY;
		fin = Build.FINGERPRINT;
		han = Build.ID;
		man = Build.MANUFACTURER;
		mon = Build.MODEL;
		opn = tm.getNetworkOperator();
		prn = Build.PRODUCT;
		con = Build.VERSION.CODENAME;
		inc = Build.VERSION.INCREMENTAL;
		rel = Build.VERSION.RELEASE;
		sdki = Build.VERSION.SDK_INT;
		try {
			PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0 );
			applicationVersion = packageInfo.versionName;
			codeVersion = Integer.toString(packageInfo.versionCode);
		} catch (NameNotFoundException e) {
			applicationVersion = "na";
			codeVersion = "na";	
		}
		jarVersion = "1-9.26";

	}

	@Override
	public JSONObject ConvertToJson() {

		JSONObject json = new JSONObject();

		try {
			json.put("brn", this.brn);
			json.put("den", this.den); // string - device name
			json.put("din", this.din); // string - display name
			json.put("fin", this.fin); // string - fingerprint name (not our;
										// this is android property)
			json.put("han", this.han); // string - hardware name
			json.put("man", this.man);
			json.put("mon", this.mon); // string - model name
			json.put("opn", this.opn); // string - operator name
			json.put("prn", this.prn); // string - product name
			json.put("con", this.con);
			json.put("inc", this.inc); // json.put( - The internal value used by
										// the underlying source control to
										// represent this build.
			json.put("rel", this.rel); // string - The user-visible version
										// string.
			json.put("sdki", this.sdki);
			json.put("jar", this.jarVersion);
			json.put("app", this.applicationVersion);
		} catch (Exception e) {
			InternalLog.d(TAG, "Excpetion in JSON");
		}
		return json;
	}

	String brn; // string - brand name
	String den; // string - device name
	String din; // string - display name
	String fin; // string - fingerprint name (not our; this is android property)
	String han; // string - hardware name
	String man; // string - manufacturer name
	String mon; // string - model name
	String opn; // string - operator name
	String prn; // string - product name
	String con;// string - The current development codename, or the string "REL"
				// if this is a release build.
	String inc; // string - The internal value used by the underlying source
				// control to represent this build.
	String rel; // string - The user-visible version string.
	int sdki; // integer - The user-visible SDK version of the framework; its
				// possible values are defined in //Build.VERSION_CODES.
	String applicationVersion; //from Manifest
	String codeVersion; //from Manifest
	String jarVersion;

}
