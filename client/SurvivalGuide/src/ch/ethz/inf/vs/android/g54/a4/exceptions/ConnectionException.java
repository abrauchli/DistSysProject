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
 * This exception is thrown when the connection with the server failed for some reason.
 * 
 * e.g. we have no Internet connection or the server we wanted to connect to, does not exist.
 * 
 * @see ServerException
 * @see UnrecognizedResponseException
 */
public class ConnectionException extends Exception {

	/**
	 * from implementing Serializable
	 */
	private static final long serialVersionUID = -8712962248765421328L;

	public ConnectionException() {
		super();
	}

	public ConnectionException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ConnectionException(String detailMessage) {
		super(detailMessage);
	}

	public ConnectionException(Throwable throwable) {
		super(throwable);
	}

}
