package edu.elon.cs.collection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import edu.elon.cs.collection.R;


public class CompassView extends ImageView {

	private float azimuth;
	private float cx, cy;
	
	public CompassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		azimuth = 0.0f;
		Drawable image = getResources().getDrawable(R.drawable.compass);
		cx = image.getIntrinsicWidth() / 2;
		cy = image.getIntrinsicHeight() / 2;
	}

	public void setAzimuth(float azimuth){
		this.azimuth = (azimuth - 90.0f)%360;
	}
	
	@Override
	protected void onDraw(Canvas canvas){
		canvas.rotate(azimuth, cx, cy);
		super.onDraw(canvas);
	}
}