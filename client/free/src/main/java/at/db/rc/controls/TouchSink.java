/*
 * Copyright 2013 Daniel Baumann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package at.db.rc.controls;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import at.db.rc.Client;
import at.db.rc.A;
import at.db.rc.interfaces.IRegionProvider;

public class TouchSink extends View implements IRegionProvider {

	public static final String	TAG											= TouchSink.class.getName();
	private RectF								emptyRect								= new RectF();
	private RectF								touchPad;
	private RectF								pointerStick;
	private RectF								scrollPad;
	private RectF								leftButton;
	private RectF								rightButton;

	public static final int			TOUCH_PAD								= 1 << 0;
	public static final int			POINTER_STICK						= 1 << 1;
	public static final int			SCROLL_PAD							= 1 << 2;
	public static final int			BUTTONS									= 1 << 3;

	private int									touchSinkModes					= TOUCH_PAD | SCROLL_PAD | BUTTONS;

	// all values in inches
	private static final float	INNER_CIRCLE_RADIUS			= 0.1F;
	private static final float	OUTER_CIRCLE_RADIUS			= 0.6F;
	private static final float	SCROLL_BAR_WIDTH				= 0.20F;
	private static final float	SCROLL_BAR_ITEM_HEIGHT	= 0.03F;
	private static final float	BUTTON_HEIGHT						= 0.2F;
	private static final float	BORDER									= 0.05F;
	private Client							client;
	private int									xScrollBar;
	private int									yButton;
	private int									xBorder;
	private int									yBorder;
	private RectF								pointerStickCenter;
	private boolean							isInitialized						= false;
	private Point								pointerStickCenterPoint;
	private float								xdpi;
	private float								ydpi;
	private int									yScrollBarItem;

	private Drawable						connected								= null;
	private Drawable						disconnected						= null;

	public TouchSink(Context context) {
		super(context);
	}

	public TouchSink(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TouchSink(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public int getTouchSinkModes() {
		return touchSinkModes;
	}

	@Override
	public void setTouchSinkModes(int touchSinkModes) {
		this.touchSinkModes = touchSinkModes;
		calculateRegions();
		postInvalidate();
	}

	public boolean isTouchPadActive() {
		return ((touchSinkModes & TOUCH_PAD) == TOUCH_PAD);
	}

	public boolean isPointerStickActive() {
		return ((touchSinkModes & POINTER_STICK) == POINTER_STICK);
	}

	public boolean isScrollPadActive() {
		return ((touchSinkModes & SCROLL_PAD) == SCROLL_PAD);
	}

	public boolean isButtonsActive() {
		return ((touchSinkModes & BUTTONS) == BUTTONS);
	}

	@Override
	public void draw(Canvas canvas) {
		if (!isInitialized) {
			calculateRegions();
			connected = getResources().getDrawable(A.drawable.remote_control_connected);
			disconnected = getResources().getDrawable(A.drawable.remote_control_disconnected);
			isInitialized = true;
		}
		int width = this.getWidth();
		int height = this.getHeight();

		Paint paint = new Paint();
		paint.setColor(Color.LTGRAY);
		paint.setStyle(Style.STROKE);

		if (isScrollPadActive()) {
			int sbLeft = width - (xScrollBar * 2 / 3);
			int sbRight = width - (xScrollBar / 3);
			Path path = new Path();
			path.setFillType(FillType.EVEN_ODD);
			path.moveTo((sbLeft + sbRight) / 2, scrollPad.top);
			path.lineTo(sbLeft, scrollPad.top + yBorder / 2);
			path.lineTo(sbRight, scrollPad.top + yBorder / 2);
			path.lineTo((sbLeft + sbRight) / 2, scrollPad.top);
			path.close();
			canvas.drawPath(path, paint);

			path = new Path();
			path.setFillType(FillType.EVEN_ODD);
			path.moveTo((sbLeft + sbRight) / 2, scrollPad.bottom);
			path.lineTo(sbLeft, scrollPad.bottom - yBorder / 2);
			path.lineTo(sbRight, scrollPad.bottom - yBorder / 2);
			path.lineTo((sbLeft + sbRight) / 2, scrollPad.bottom);
			path.close();
			canvas.drawPath(path, paint);

			for (int i = (2 * yBorder); i < height - (2 * yBorder); i += yScrollBarItem) {
				canvas.drawRect(
						new Rect(sbLeft, i, sbRight, Math.min((i + (yScrollBarItem / 2)), (height - (2 * yScrollBarItem)))), paint);
			}
		}

		if (isButtonsActive()) {
			canvas.drawRoundRect(leftButton, 5, 5, paint);
			canvas.drawRoundRect(rightButton, 5, 5, paint);
		}

		if (isTouchPadActive()) {
			canvas.drawLines(new float[] { touchPad.left, touchPad.top + yBorder, touchPad.left, touchPad.bottom - yBorder,
					touchPad.right, touchPad.top + yBorder, touchPad.right, touchPad.bottom - yBorder, touchPad.left + xBorder,
					touchPad.top, touchPad.right - xBorder, touchPad.top, touchPad.left + xBorder, touchPad.bottom,
					touchPad.right - xBorder, touchPad.bottom }, paint);
		}

		if (isPointerStickActive()) {
			canvas.drawOval(pointerStick, paint);
			canvas.drawOval(pointerStickCenter, paint);
		}

		Drawable image = ((client != null) && client.isConnected()) ? connected : disconnected;
		if (image instanceof BitmapDrawable) {
			Bitmap bitmap = ((BitmapDrawable) image).getBitmap();
			Matrix matrix = new Matrix();
			matrix.setScale(0.5F, 0.5F);
			canvas.drawBitmap(bitmap, matrix, new Paint());
		}
		
		invalidate();
	}

	private void calculateRegions() {
		int width = this.getWidth();
		int height = this.getHeight();
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		xdpi = metrics.xdpi;
		ydpi = metrics.ydpi;

		xBorder = (int) (xdpi * BORDER);
		yBorder = (int) (ydpi * BORDER);
		yScrollBarItem = (int) (ydpi * SCROLL_BAR_ITEM_HEIGHT);

		if (isScrollPadActive()) {
			xScrollBar = (int) (xdpi * SCROLL_BAR_WIDTH);
			scrollPad = new RectF(width - 20, xBorder, width, height - xBorder);
		} else {
			xScrollBar = 0;
			scrollPad = emptyRect;
		}

		if (isButtonsActive()) {
			yButton = (int) (ydpi * BUTTON_HEIGHT);
			int w = width - (2 * xBorder) - xScrollBar;
			leftButton = new RectF(xBorder, height - yButton - yBorder, xBorder + ((w - xBorder) / 2), height - yBorder);
			rightButton = new RectF(xBorder + ((w + xBorder) / 2), height - yButton - yBorder, xBorder + w, height - yBorder);
		} else {
			yButton = 0;
			leftButton = emptyRect;
			rightButton = emptyRect;
		}

		if (isTouchPadActive()) {
			touchPad = new RectF(xBorder, yBorder, width - xBorder - xScrollBar, height - yBorder - yBorder - yButton);
		} else {
			touchPad = emptyRect;
		}

		if (isPointerStickActive()) {
			int xCenter = ((width - xScrollBar) / 2);
			int yCenter = ((height - yButton) / 2);
			pointerStickCenterPoint = new Point(xCenter, yCenter);
			int xInnerRadius = (int) (xdpi * INNER_CIRCLE_RADIUS);
			int yInnerRadius = (int) (ydpi * INNER_CIRCLE_RADIUS);
			int xOuterRadius = (int) (xdpi * OUTER_CIRCLE_RADIUS);
			int yOuterRadius = (int) (ydpi * OUTER_CIRCLE_RADIUS);
			pointerStick = new RectF(xCenter - xOuterRadius, yCenter - yOuterRadius, xCenter + xOuterRadius, yCenter
					+ yOuterRadius);
			pointerStickCenter = new RectF(xCenter - xInnerRadius, yCenter - yInnerRadius, xCenter + xInnerRadius, yCenter
					+ yInnerRadius);
		} else {
			pointerStick = emptyRect;
			pointerStickCenter = emptyRect;
		}
	}

	@Override
	public void setOnTouchListener(OnTouchListener listener) {
		super.setOnTouchListener(listener);
		if (listener instanceof Client) {
			client = (Client) listener;
			client.setRegionProvider(this);
		}
	}

	@Override
	public Region getRegion(float x, float y) {
		if (touchPad.contains(x, y)) {
			return Region.TOUCH_PAD;
		} else if (pointerStick.contains(x, y)) {
			return Region.POINTER_STICK;
		} else if (scrollPad.contains(x, y)) {
			return Region.SCROLL_PAD;
		} else if (leftButton.contains(x, y)) {
			return Region.LEFT_BUTTON;
		} else if (rightButton.contains(x, y)) {
			return Region.RIGHT_BUTTON;
		}
		return null;
	}

	@Override
	public PointF getPointerStickPoint(float eventX, float eventY) {
		float dx = (eventX - pointerStickCenterPoint.x) / xdpi;
		float dy = (eventY - pointerStickCenterPoint.y) / ydpi;
		// double distance = Math.sqrt((dx * dx) + (dy * dy));
		// if (distance < INNER_CIRCLE_RADIUS) {
		// return new PointF(0, 0);
		// } else {
		// float meanDpi = ((xdpi + ydpi) / 2);
		// double scale = (INNER_CIRCLE_RADIUS / distance);
		// return new PointF((float) (dx - (scale * dx)) * meanDpi, (float) (dy -
		// (scale * dy)) * meanDpi);
		// }
		return new PointF(dx, dy);
	}

}
