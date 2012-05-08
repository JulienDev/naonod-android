package fr.naonod.tools;

import android.content.Context;

public class Tools {

	public static float getDensity(Context context) {
		return context.getResources().getDisplayMetrics().density;
	}
}
