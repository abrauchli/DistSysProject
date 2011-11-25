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
 * This exception is thrown when we receive an error from the server.
 * 
 * This can be either an HTTP error message, or an ok=false in the json-response.
 * 
 * @see ConnectionException
 * @see UnrecognizedResponseException
 */
public class ServerException extends Exception {

	/**
	 * from implementing Serializable
	 */
	private static final long serialVersionUID = -5187236119505320432L;

	public ServerException() {
		super();
	}

	public ServerException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ServerException(String detailMessage) {
		super(detailMessage);
	}

	public ServerException(Throwable throwable) {
		super(throwable);
	}

}
