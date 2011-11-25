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
package ch.ethz.inf.vs.android.g54.a4.exceptions;

/**
 * This exception is thrown when parsing the message from the server does not work.
 * 
 * This could be, because the JSONObjects and JSONArrays are not parsed correctly.
 * 
 * @see ConnectionException
 * @see ServerException
 */
public class UnrecognizedResponseException extends Exception {

	/**
	 * from implementing Serializable
	 */
	private static final long serialVersionUID = 7159957197467533748L;

	public UnrecognizedResponseException() {
		super();
	}

	public UnrecognizedResponseException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public UnrecognizedResponseException(String detailMessage) {
		super(detailMessage);
	}

	public UnrecognizedResponseException(Throwable throwable) {
		super(throwable);
	}

}
