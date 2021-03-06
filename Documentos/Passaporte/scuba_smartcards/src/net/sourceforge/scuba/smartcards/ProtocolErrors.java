/**
 * IdemixErrors.java
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (C) Pim Vullers, Radboud University Nijmegen, September 2012.
 */

package net.sourceforge.scuba.smartcards;

import java.util.HashMap;

/**
 * Simple type declaration for a Map containing error messages for status words.
 * 
 * @author Pim Vullers
 */
public class ProtocolErrors extends HashMap<Integer, String> {
	// TODO: automatically add a list of well-known errors

    /**
     * Universal version identifier to match versions during deserialisation.
     */
	private static final long serialVersionUID = -5991735413227319349L;

	/**
	 * Convenience constructor to easily add a single error.
	 * 
	 * @param status word from the smart card.
	 * @param message describing the corresponding error.
	 */
	public ProtocolErrors(Integer status, String message) {
		super();
		put(status, message);
	}
}
