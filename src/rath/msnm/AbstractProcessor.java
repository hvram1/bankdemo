/*
 * @(#)AbstractProcessor.java
 * 
 * Copyright (c) 2001-2002, JangHo Hwang All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the JangHo Hwang nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * $Id: AbstractProcessor.java,v 1.17 2005/05/11 19:49:15 xrath Exp $
 */
package rath.msnm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

import rath.msnm.entity.Callback;
import rath.msnm.entity.ServerInfo;
import rath.msnm.msg.IncomingMessage;
import rath.msnm.msg.MimeMessage;
import rath.msnm.msg.OutgoingMessage;


public abstract class AbstractProcessor extends Thread
{
	public final MSNMessenger msn;
	private static final Class[] PARAM_TYPE = new Class[] { IncomingMessage.class };
	
	private ServerInfo info = null;
	private boolean autoOut = false;
	private int trId;
	private String name = "";

	protected HashMap callbackMap = new HashMap();
	protected volatile boolean isLive;
	protected Socket socket = null;
	protected InputStream in = null;
	protected OutputStream out = null;

	
	protected AbstractProcessor(MSNMessenger msn)
	{
		this(msn, null, 1);
	}

	
	protected AbstractProcessor(MSNMessenger msn, ServerInfo info)
	{
		this(msn, info, 1);
	}

	
	protected AbstractProcessor(MSNMessenger msn, ServerInfo info, int trId)
	{
		this.msn = msn;
		this.info = info;
		this.trId = trId;
	}

	public void setServerName(String name)
	{
		this.name = name;
		setName("MSN Channel(" + name + ")");
	}

	public String getServerName()
	{
		return this.name;
	}

	
	public void setAutoOutSend(boolean autoOut)
	{
		this.autoOut = autoOut;
	}

	
	public boolean isAutoOutSend()
	{
		return this.autoOut;
	}

	protected void setServerInfo(ServerInfo info)
	{
		this.info = info;
	}

	protected ServerInfo getServerInfo()
	{
		return this.info;
	}

	
	protected void makeConnection() throws IOException
	{
		closeConnection();

		this.socket = new Socket(info.getHostAddress(), info.getPort());
		this.in = socket.getInputStream(); 
		this.out = socket.getOutputStream();
	}

	protected void closeConnection() throws IOException
	{
		if( in != null )
			in.close();
		if( out != null )
			out.close();
		if( socket != null )
			socket.close();
	}

	
	public abstract void init() throws IOException;

	
	public synchronized void sendMessage(OutgoingMessage msg)
		throws IOException
	{
		if( out == null )
			return;

		if( trId != -1 && msg.getBackProcess() != null )
		{
			callbackMap.put(new Integer(trId), msg.getBackProcess());
		}

		if( Debug.printOutput )
			System.out.println("=> " + msg.toString());

		System.err.println("发送的消息是"+msg.toString() );
		println( msg.toString() );
		trId++;
	}
	
	public void println( String str ) throws IOException
	{
		out.write( (str+"\r\n").getBytes("UTF-8") );
		out.flush();
	}

	private ByteArrayOutputStream inbuf = new ByteArrayOutputStream();

	public String readLine() throws IOException
	{
		inbuf.reset();
		while(true)
		{
			int v = in.read();
			if( v==-1 )
				return null;
			if( v==13 )
				continue;
			if( v==10 )
				break;
			inbuf.write(v);
		}

		return new String(inbuf.toByteArray(), "UTF-8");
	}

	
	public synchronized void sendMimeMessage(OutgoingMessage msg,
		MimeMessage mime) throws IOException
	{
		if( trId != -1 && msg.getBackProcess() != null )
		{
			callbackMap.put(new Integer(trId), msg.getBackProcess());
		}

		byte[] raw = mime.getBytes();
		int len = raw.length;

		msg.add(len);
		println( msg.toString() );
		out.write( raw );

		if( Debug.printMime )
		{
			System.out.println(new String(raw, "UTF-8"));
		}

		out.flush();
		trId++;
	}

	public synchronized void sendCHLResponse(OutgoingMessage msg, String hash)
		throws IOException
	{
		out.write( (msg.toString().concat("\r\n").concat(hash)).getBytes("UTF-8") );
		out.flush();
		trId++;
	}

	
	public void markTransactionId(OutgoingMessage msg)
	{
		msg.setTransactionId(this.trId);
	}

	
	public int getCurrentTransactionId()
	{
		return this.trId;
	}

	protected Method lookupMethod(Callback cb) throws NoSuchMethodException,
		SecurityException
	{
		return cb.getClassRef().getMethod(cb.getMethodName(), PARAM_TYPE);
	}

	
	protected void registerCallback(Integer trId, Callback cb)
	{
		callbackMap.put(trId, cb);
	}

	
	public void processMessage(IncomingMessage msg) throws Exception
	{

	}

	
	public void processNotifyMessage(IncomingMessage msg) throws Exception
	{
		if( msg.getHeader().equals("MSG") )
		{
			/*
			 * MSG UserHandle FriendlyName Length\r\nMessage
			 */			
			int len = msg.getInt(2);
			int offset = 0, readlen = 0;
			byte[] b = new byte[len];
			while( offset < len )
			{
				readlen = in.read(b, offset, len-offset);
				if( readlen==-1 )
					break;
				offset += readlen;
			}

			MimeMessage mime = MimeMessage.parse(b);
			filterMimeMessage(mime);
			if( Debug.printMime )
			{
				System.out.println(new String(b, "UTF-8"));
			}
		}
	}

	/**
	 * All Mime Messages ('MSG' header) filter in this method.
	 */
	protected void filterMimeMessage(MimeMessage msg)
	{

	}

	
	public void removeInfiniteTransactionId(int trId)
	{
		callbackMap.remove(new Integer(trId));
	}

	
	public final void run()
	{
		this.isLive = true;
		try
		{
			makeConnection();
			init();

			while (isLive)
			{
				String line = readLine();
					System.out.println("<= " + line);
				if( line == null )
					break;

				IncomingMessage msg = null;
				try
				{
					msg = IncomingMessage.getInstance(line);
				}
				catch (Exception e)
				{
				}
				if( msg == null )
					continue;

				try
				{
					if( !msg.isNotify() )
					{
						Integer trId = new Integer(msg.getTransactionId());
						Callback cb = (Callback) callbackMap.get(trId);
						if( cb != null )
						{
							if( !cb.isInfinite() )
								callbackMap.remove(trId);
	
							lookupMethod(cb).invoke(this, new Object[] { msg });
						}
						else
							processMessage(msg);
					}
					else
					{
						processNotifyMessage(msg);
					}
				}
				catch( IOException e )
				{
					throw e;
				}
				catch( Exception e )
				{
					processError( e );
				}
			}
		}
		catch( SocketException e ) {}
		catch (Throwable e)
		{
			processError(e);
		}
		finally
		{
			cleanUp();

			if( in != null )
			{
				try
				{
					in.close();
				}
				catch (Exception e)
				{
				}
				in = null;
			}
			if( out != null )
			{
				try
				{
					if( autoOut )
						sendMessage(new OutgoingMessage("OUT"));
					out.close();
				}
				catch (Exception e)
				{
				}
				out = null;
			}
			if( socket != null )
			{
				try
				{
					socket.close();
				}
				catch (Exception e)
				{
				}
				socket = null;
			}
		}
	}

	
	public abstract void cleanUp();

	
	public void processError(Throwable e)
	{
		System.err.println(this.getClass().getName() + ":");
		e.printStackTrace();
	}
	/**
	 * @return Returns the msn.
	 */
	public MSNMessenger getMsn() {
		return msn;
	}
};
