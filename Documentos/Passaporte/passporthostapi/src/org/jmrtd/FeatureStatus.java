/*
 * JMRTD - A Java API for accessing machine readable travel documents.
 *
 * Copyright (C) 2006 - 2013  The JMRTD team
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * $Id: FeatureStatus.java 1504 2013-05-31 21:04:32Z martijno $
 */

package org.jmrtd;

/**
 * Security features of this identity document.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 *
 * @version $Revision: 1504 $
 */
public class FeatureStatus {

	/**
	 * Outcome of a feature presence check.
	 * 
	 * @author The JMRTD team (info@jmrtd.org)
	 *
	 * @version $Revision: 1504 $
	 */
	public enum Verdict {
		UNKNOWN,		/* Presence unknown */
		PRESENT,		/* Present */
		NOT_PRESENT;	/* Not present */
	};

	private Verdict hasBAC, hasAA, hasEAC;

	public FeatureStatus() {
		this.hasBAC = Verdict.UNKNOWN;
		this.hasAA = Verdict.UNKNOWN;
		this.hasEAC = Verdict.UNKNOWN;
	}
	
	public void setBAC(Verdict hasBAC) {
		this.hasBAC = hasBAC;
	}
	
	public Verdict hasBAC() {
		return hasBAC;
	}
	
	public void setAA(Verdict hasAA) {
		this.hasAA = hasAA;
	}
	
	public Verdict hasAA() {
		return hasAA;
	}
	
	public void setEAC(Verdict hasEAC) {
		this.hasEAC = hasEAC;
	}
	
	public Verdict hasEAC() {
		return hasEAC;
	}
}
