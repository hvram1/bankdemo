/*
 * @(#)DispatchProcessor.java
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
 *    $Id: DispatchProcessor.java,v 1.7 2005/05/01 23:55:54 xrath Exp $
 */
package rath.msnm;

import java.io.IOException;

import rath.msnm.entity.Callback;
import rath.msnm.entity.ServerInfo;
import rath.msnm.msg.IncomingMessage;
import rath.msnm.msg.OutgoingMessage;

public class DispatchProcessor extends AbstractProcessor
{
	private String sp = null;

	public DispatchProcessor( MSNMessenger msn, ServerInfo info )
	{
		super( msn, info, 0 );

		setServerName( "DS" );
	}

	
	public void init() throws IOException
	{
		OutgoingMessage msg = new OutgoingMessage("VER");
		markTransactionId( msg );
		msg.add( "MSNP10" );
		msg.add( "MSNP9" );
//		msg.add( "MSNP8" );
		msg.add( "CVRO" );
		msg.setBackProcess( Callback.getInstance("processVER", this.getClass()) );

		sendMessage( msg );
	}

	public void processVER( IncomingMessage msg ) throws Exception
	{
		OutgoingMessage out = new OutgoingMessage("CVR");
		markTransactionId( out );
		out.setBackProcess( Callback.getInstance("processCVR", this.getClass()) );

		sendMessage( out );
	}

	public void processCVR( IncomingMessage msg ) throws Exception
	{
		OutgoingMessage out = new OutgoingMessage("USR");
		markTransactionId( out );
		out.add( msg.get(0) );
		out.add( "I" );
		out.add( msn.getLoginName() );
		out.setBackProcess( Callback.getInstance("processXFR", this.getClass()) );

		sendMessage( out );
	}

	public void processXFR( IncomingMessage msg ) throws Exception
	{
		isLive = false;
		ServerInfo ns = msg.getServerInfo(1);
		//msn.login( ns, getCurrentTransactionId() );
	}

	public void cleanUp()
	{

	}
};
