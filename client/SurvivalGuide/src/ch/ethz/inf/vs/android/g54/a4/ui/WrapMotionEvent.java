/*
 * This file is part of SurvivalGuide
 * Copyleft 2011 The SurvivalGuide Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.ethz.inf.vs.android.g54.a4.ui;

import android.view.MotionEvent;

public class WrapMotionEvent {
protected MotionEvent event;

	protected WrapMotionEvent(MotionEvent event) {
		this.event = event;
	}

    static public WrapMotionEvent wrap(MotionEvent event) {
    	try {
            return new EclairMotionEvent(event);
        } catch (VerifyError e) {
            return new WrapMotionEvent(event);
        }
    }

    public int getAction() {
        return event.getAction();
    }

    public float getX() {
        return event.getX();
    }

    public float getX(int pointerIndex) {
        verifyPointerIndex(pointerIndex);
        return getX();
    }

    public float getY() {
        return event.getY();
    }

    public float getY(int pointerIndex) {
        verifyPointerIndex(pointerIndex);
        return getY();
    }

    public int getPointerCount() {
        return 1;
    }

    public int getPointerId(int pointerIndex) {
        verifyPointerIndex(pointerIndex);
        return 0;
    }

    private void verifyPointerIndex(int pointerIndex) {
        if (pointerIndex > 0) {
            throw new IllegalArgumentException(
                "Invalid pointer index for Donut/Cupcake");
        }
    }

}