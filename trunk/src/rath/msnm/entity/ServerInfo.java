/*
 * @(#)ServerInfo.java
 *
 * Copyright (c) 2001-2002, JangHo Hwang
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 	1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 	2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 	3. Neither the name of the JangHo Hwang nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *    $Id: ServerInfo.java,v 1.7 2004/12/24 22:05:52 xrath Exp $
 */
package rath.msnm.entity;

public class ServerInfo
{
	/**
	 * Default port number for <b>DS</b>(Dispatch server) and
	 * <b>SS</b>(Switchboard server)
	 */
	public static final int DEFAULT_PORT = 1863;

	private String host = null;
	private int port;

	
	public ServerInfo( String host )
	{
		this.host = host;
		this.port = DEFAULT_PORT;
	}

	
	public ServerInfo( String host, int port )
	{
		this.host = host;
		this.port = port;
	}

	public String getHostAddress()
	{
		return this.host;
	}

	public int getPort()
	{
		return this.port;
	}

	
	public static ServerInfo getDefaultDispatchServerInfo()
	{
		return new ServerInfo( "64.4.13.58", DEFAULT_PORT );
	}

	
	public static ServerInfo getDefaultServerInfo()
	{
        return new ServerInfo( "messenger.hotmail.com", DEFAULT_PORT );
	}

	public String toString()
	{
		return "Host: " + host + ", Port: " + port;
	}
};
