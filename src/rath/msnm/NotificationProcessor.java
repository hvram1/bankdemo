/*
 * @(#)NotificationProcessor.java
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
 *    $Id: NotificationProcessor.java,v 1.35 2005/05/20 06:15:03 xrath Exp $
 */
package rath.msnm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import rath.msnm.entity.Callback;
import rath.msnm.entity.Group;
import rath.msnm.entity.MsnFriend;
import rath.msnm.entity.ServerInfo;
import rath.msnm.msg.IncomingMessage;
import rath.msnm.msg.MimeMessage;
import rath.msnm.msg.MimeUtility;
import rath.msnm.msg.OutgoingMessage;
import rath.msnm.util.StringUtil;
import rath.msnm.util.TWN;

public class NotificationProcessor extends AbstractProcessor implements UserStatus
{
	public static final String NO_GROUP_NAME = "-- No Group --";
	public static final String NO_GROUP_IDX = "XXXX-XXXX";

	String lastFrom = "0";
	String lastTo = "0";
	String lastFN = "";
	private String securityPackage = null;
	private String status = null;

	private Thread callbackCleaner = null;
	private Hashtable lockMap = new Hashtable();
	private Hashtable sessionMap = new Hashtable();
	private HashMap callIdMap = new HashMap();
	private HashMap callingMap = new HashMap(); 

	private boolean isInitialRush = false;

	public NotificationProcessor( MSNMessenger msn, ServerInfo info )
	{
		this( msn, info, 0 );
	}

	public NotificationProcessor( MSNMessenger msn, ServerInfo info, int trId )
	{
		super( msn, info, trId );

		setServerName( "NS" );
	}

	
	public void init() throws IOException
	{
		OutgoingMessage msg = new OutgoingMessage("VER");
		markTransactionId( msg );
		msg.add( "MSNP10" );
		msg.add( "MSNP9" );
		msg.add( "CVRO" );
		msg.setBackProcess( Callback.getInstance("processVER", this.getClass()) );

		sendMessage( msg );
	}

	
	public void processMessage( IncomingMessage msg ) throws Exception
	{
		String header = msg.getHeader();

		if( header.equals("ILN") )
		{
			String status = msg.get(0);
			String login = msg.get(1);
			String friendly = msg.get(2);

			MsnFriend friend = msn.getBuddyGroup().getForwardList().get( login );
			if( friend==null )
			    friend = new MsnFriend(login, friendly);
			else
				friend.setFriendlyName( friendly );
			friend.setStatus( status );

			
/*
			if( msg.size()>4 )
			{
				String sn = msg.get(3);
				String ctx = MimeUtility.getURLDecodedString(msg.get(4), "UTF-8");
				String ctx_old = friend.getPhotoContext();
				friend.setPhotoContext( ctx );

				if( !ctx.equals(ctx_old) )
				{
					msn.firePhotoContextUpdatedEvent( friend, ctx );
				}
			}
*/
			msn.fireListOnlineEvent( friend );
		}
		else
		if( header.equals("ADC") )
		{
			String group = msg.get(0);
			if( group.equals("RL") && msg.getTransactionId()==0 )
			{
				String login = msg.get(1);
				String friendly = msg.get(2);

				if( login.startsWith("N=") )
					login = login.substring(2);
				if( friendly.startsWith("F=") )
					friendly = friendly.substring(2);

				MsnFriend friend = new MsnFriend(login, friendly);
				BuddyList reverse = msn.getBuddyGroup().getReverseList();
				reverse.add( friend );

				storeLocalCopy();
				if( msn.getBuddyGroup().getForwardList().get(login)==null )
					msn.fireWhoAddedMeEvent( friend );
			}
		}
		else
		if( header.equals("REM") && msg.getTransactionId()==0 )
		{
			String group = msg.get(0);
			if( group.equals("RL") )
			{
				String login = msg.get(1);

				BuddyList reverse = msn.getBuddyGroup().getReverseList();
				reverse.remove( login );

				storeLocalCopy();
				msn.fireWhoRemovedMeEvent( new MsnFriend(login, login) );
			}
		}
		else
		if( header.equals("CHL") )
		{
			String code = StringUtil.md5(msg.get(0) + "Q1P7W2E4J9R8U3S5");

			OutgoingMessage out = new OutgoingMessage("QRY");
			markTransactionId( out );
			out.add( "msmsgs@msnmsgr.com" );
			out.add( 32 );

			sendCHLResponse( out, code );
		}
	}

	public void processNotifyMessage( IncomingMessage msg ) throws Exception
	{
		super.processNotifyMessage( msg );

		String header = msg.getHeader();

		if( header.equals("NLN") )
		{
			String status = msg.get(0);
			String login = msg.get(1);
			String friendly = msg.get(2);

			BuddyList fl = msn.getBuddyGroup().getForwardList();
			MsnFriend friend = fl.get(login);
			if( friend!=null )
			{
				friend.setStatus( status );
				friend.setFriendlyName( friendly );

/*
				if( msg.size() > 4 )
				{
					String sn = msg.get(3);
					String ctx = MimeUtility.getURLDecodedString(msg.get(4), "UTF-8");
					String ctx_old = friend.getPhotoContext();
					friend.setPhotoContext( ctx );

					if( !ctx.equals(ctx_old) )
					{
						msn.firePhotoContextUpdatedEvent( friend, ctx );
					}
				}
*/

				msn.fireUserOnlineEvent( friend );
			}
		}
		else
		if( header.equals("FLN") )
		{
			String login = msg.get(0);
			msn.fireUserOfflineEvent( login );
		}
		else
		if( header.equals("RNG") )
		{
			acceptRinging( msg );
		}
		else
		if( header.equals("LSG") )
		{
			doCollectGroup( msg );
		}
		else
		if( header.equals("LST") )
		{
			doCollectList( msg );
		}
		else
		if( header.equals("PRP") )
		{
			processProfile( msg );
		}
	}

	protected void processProfile( IncomingMessage msg ) throws IOException
	{
		String head = msg.get(0);
		if( head.equals("MFN") )
		{
			String fn = msg.get(1);
			msn.getOwner().setFriendlyName( fn );
			msn.fireRenameNotifyEvent( msn.getOwner() );
		}
	}

	protected void filterMimeMessage( MimeMessage msg ) 
	{
		if( msg.getKind()==MimeMessage.KIND_MAIL_NOTIFY )
		{
			if( msg.hasProperty("Inbox-Unread") )
				msn.fireNotifyUnreadMail( msg.getProperties(), 
					Integer.parseInt(msg.getProperty("Inbox-Unread")) );
		}
	}

	
	protected void acceptRinging( IncomingMessage msg ) throws IOException
	{
		String sessionId = msg.get(0);
		ServerInfo serv = msg.getServerInfo(1);
		String securityPackage = msg.get(2);
		String cookie = msg.get(3);
		String destinLoginName = msg.get(4);
		String destinFriendlyName = msg.get(5);

		// Throw calling friend information
		// I need not :)
		SwitchboardSession ss = new SwitchboardSession( msn, serv, sessionId );
		ss.setCookie( cookie );
		ss.start();
	}

	public void processVER( IncomingMessage msg ) throws Exception
	{
		OutgoingMessage out = new OutgoingMessage("CVR");
		markTransactionId( out );
		out.add( "0x0412" ); // What is it?
		out.add( System.getProperty("os.name").replace(' ', '_') ); // OS name
		out.add( System.getProperty("os.version") ); // OS version
		out.add( System.getProperty("os.arch") ); // OS architecture
		out.add( "MSNMSGR" );
		out.add( "6.2.0605" );
		out.add( "MSMSGS" );
		out.add( msn.getLoginName() );
		out.setBackProcess( Callback.getInstance("processCVR", this.getClass()) );

		sendMessage( out );
	}

	public void processCVR( IncomingMessage msg ) throws Exception
	{
		// Microsoft doesn't provide security package.
		// this.securityPackage = msg.get(0);
		this.securityPackage = "TWN"; // SSL based

		OutgoingMessage out = new OutgoingMessage("USR");
		markTransactionId( out );
		out.add( this.securityPackage );
		out.add( "I" );
		out.add( msn.getLoginName() );
		out.setBackProcess( Callback.getInstance("processAuth", this.getClass()) );

		sendMessage( out );
	}

	public void processAuth( IncomingMessage msg ) throws Exception
	{
		if( msg.getHeader().equals("XFR") &&
		    msg.get(0).equals("NS") )
		{
		    ServerInfo info = msg.getServerInfo(1);
			this.setServerInfo(info);
			makeConnection();
			init();
			return;
		}

		OutgoingMessage out = new OutgoingMessage("USR");
		markTransactionId( out );
		out.add( this.securityPackage );
		out.add( "S" );
		if( securityPackage.equals("MD5") )
		{
			out.add( StringUtil.md5(msg.get(2)+msn.getPassword()) );
		}
		else
		if( securityPackage.equals("TWN") )
		{
			out.add( TWN.getTNP(msn.getLoginName(), msn.getPassword(), msg.get(2)) );
		}

		out.setBackProcess( Callback.getInstance("processLogon", this.getClass()) );

		sendMessage( out );
	}

	public void processLogon( IncomingMessage msg ) throws Exception
	{
		// Authentication failed.
		if( !msg.getHeader().equals("USR") )
		{
			msn.fireLoginErrorEvent( msg.getHeader() );
			return;
		}

		Callback cb = Callback.getInstance("judgeSerial", this.getClass());

		OutgoingMessage out = new OutgoingMessage("SYN");
		markTransactionId( out );
		out.add( lastFrom );
		out.add( lastTo );
		out.setBackProcess( cb );

		sendMessage( out );

		msn.isLogged = true;
		msn.fireLoginCompleteEvent( new MsnFriend(msg.get(1),lastFN) );
	}

	
	public void judgeSerial( IncomingMessage msg ) throws IOException
	{
		String serverFrom = msg.get(0);
		String serverTo = msg.get(1);
		if( !(serverFrom.equals(lastFrom) && serverTo.equals(lastTo)) )
		{
			this.lastFrom = serverFrom;
			this.lastTo = serverTo;

			
			BuddyGroup bg = msn.getBuddyGroup();
			bg.getForwardList().clear();
			bg.getAllowList().clear();
			bg.getBlockList().clear();
			bg.getReverseList().clear();	
			bg.getAllList().clear();
			
			GroupList gl = msn.getBuddyGroup().getGroupList();
			gl.addGroup( new Group(NO_GROUP_NAME, NO_GROUP_IDX) );
		}

		this.isInitialRush = true;
		setMyStatus( msn.getInitialStatus() );
	}

	/**
	 * Collect LSG Message
	 */
	public void doCollectGroup( IncomingMessage msg ) throws IOException
	{
		GroupList gl = msn.getBuddyGroup().getGroupList();

		String gName = MimeUtility.getURLDecodedString( msg.get(0), "UTF-8" );
		String gIndex = msg.get(1);

		Group group = new Group(gName, gIndex);
		gl.addGroup(group);
	}

	public void doCollectList( IncomingMessage msg ) throws IOException
	{
		String hisLoginName = msg.get(0);
		if( hisLoginName.startsWith("N=") )
			hisLoginName = hisLoginName.substring(2);

		String hisFriendlyName = null;
		String hisCode = null;
		String hisGroup = NO_GROUP_IDX;
		int accessId = 0;
		if( msg.size() < 3 )
		{
			accessId = msg.getInt(1);
			hisFriendlyName = hisLoginName;
		}
		else
		{
			hisFriendlyName = msg.get(1);
			hisCode = msg.get(2);
			if( hisCode.startsWith("C=") )
			{
				accessId = msg.getInt(3);
				if( msg.size() >= 5 )
					hisGroup = msg.get(4);

				if( hisFriendlyName.startsWith("F=") )
					hisFriendlyName = hisFriendlyName.substring(2);
				if( hisCode.startsWith("C=") )
					hisCode = hisCode.substring(2);
			}
			else
			{
				accessId = Integer.parseInt(hisCode);
			}
		}

		BuddyGroup bg = msn.getBuddyGroup();

		MsnFriend friend = new MsnFriend( hisLoginName );
		friend.setFriendlyName( hisFriendlyName );
		friend.setGroupIndex( hisGroup );
		friend.setCode( hisCode );
		friend.setAccessValue( accessId );

		bg.getAllList().add( friend );

		
		if( bg.isListForward(accessId) )
			fixAdd( bg.getForwardList(), hisLoginName, friend, hisGroup );
		if( bg.isListAllow(accessId) )
			fixAdd( bg.getAllowList(), hisLoginName, friend, hisGroup );
		if( bg.isListBlock(accessId) )
			fixAdd( bg.getBlockList(), hisLoginName, friend, hisGroup );
		if( bg.isListReverse(accessId) )
			fixAdd( bg.getReverseList(), hisLoginName, friend, hisGroup );
		if( bg.isNewbie(accessId) )
		{
			requestRemoveAsList( hisLoginName, "PL" );
			if( !bg.isListReverse(accessId) )
			{
				requestAddAsList( hisLoginName, "RL" );
			}
			msn.fireWhoAddedMeEvent( friend );
		}

		if( !isInitialRush )
			msn.fireListAdd( friend );	
	}

	private void fixAdd( BuddyList list, String loginName, MsnFriend friend, String groupIndex )
	{
		MsnFriend old = list.get( loginName );
		if( old!=null )
			friend.setStatus( old.getStatus() );
		list.add( friend );
		if( old!=null && groupIndex!=null )
			old.setGroupIndex( groupIndex );
	}

	private void collectComplete( IncomingMessage msg ) throws IOException
	{		
		msn.storeLocalCopy( lastFrom, lastTo );
		msn.fireAllListUpdatedEvent();		
	}

	
	public void setMyStatus( String code ) throws IOException
	{
		this.status = code;

		Callback cb = Callback.getInstance("processCHG", this.getClass());
		OutgoingMessage out = new OutgoingMessage("CHG");
		markTransactionId( out );
		out.add( code );
		out.setBackProcess( cb );


		sendMessage( out );
	}

	public void processCHG( IncomingMessage msg ) throws IOException
	{
		if( isInitialRush )
		{
			isInitialRush = false;
			collectComplete(msg);

			Callback cb = Callback.getInstance("processCHG", this.getClass());
			OutgoingMessage out = new OutgoingMessage("CHG");
			markTransactionId( out );
			out.add( this.status );
			out.add( "0" );
			out.setBackProcess( cb );

			sendMessage( out );
		}
	}

	public String getMyStatus()
	{
		return this.status;
	}

	public void setMyFriendlyName( String newName ) throws IOException
	{
		Callback cb = Callback.getInstance("processRename", this.getClass());

		OutgoingMessage out = new OutgoingMessage("PRP");
		markTransactionId( out );
		out.add( "MFN" );
		newName = StringUtil.replaceString(newName, "%", "%25");
		newName = StringUtil.replaceString(newName, " ", "%20");
		out.add( newName );

		out.setBackProcess( cb );

		sendMessage( out );
	}

	public void processRename( IncomingMessage msg ) throws IOException
	{
		if( !msg.getHeader().equals("PRP") )
		{
			// Error, maybe 209? (Invalid friendly name)
			// For instance, request name include 'MSN', you'll fail.
			msn.fireRenameNotifyEvent( null );
			return;
		}

		String head = msg.get(0);
		String fn = msg.get(1);
		
		msn.getOwner().setFriendlyName( fn );
		msn.fireRenameNotifyEvent( msn.getOwner() );

		storeLocalCopy();
	}

	
	public void start()
	{
		if( callbackCleaner==null )
			startCallbackCleaner();
		super.start();
	}

	/**
	 * Remove garbage callback per 30 minutes.
	 * If callback's delay time over 3 minute, that callback should be removed.
	 */
	private void startCallbackCleaner()
	{
		callbackCleaner = new Thread( new Runnable() {
			public final void run()
			{
				try
				{
					while(true)
					{
						Thread.sleep( 1000*60*30 );
						long limit = System.currentTimeMillis() - (long)(1000*60*5);
						synchronized( callbackMap )
						{
							for(Iterator i=callbackMap.values().iterator(); i.hasNext(); )
							{
								Callback cb = (Callback)i.next();
								if( cb.getCreationTime() < limit )
									i.remove();
							}
						}
					}
				}
				catch( InterruptedException e ) {}
				catch( Exception e )
				{
					processError( e );
				}
			}
		});
		callbackCleaner.setPriority( Thread.MIN_PRIORITY );
		callbackCleaner.start();
	}

	
	public void doCallFriend( String loginName ) throws IOException
	{
		if( callingMap.containsKey(loginName) )
			return;

		Callback cb = Callback.getInstance("connectToSwitchboard", this.getClass());

		OutgoingMessage out = new OutgoingMessage("XFR");
		markTransactionId( out );
		out.add( "SB" );
		out.setBackProcess( cb );

		callIdMap.put( new Integer(out.getTransactionId()), loginName );
		callingMap.put( loginName, loginName );

		sendMessage( out );
	}	

	public void requestAdd( String loginName ) throws IOException
	{
		requestAddAsList(loginName, "AL");
		requestAddAsList(loginName, "FL");
	}

	public void requestAddAsList( String loginName, String listKind ) 
		throws IOException, IllegalArgumentException
	{
		if( !listKind.equals("AL") && !listKind.equals("BL") && 
			!listKind.equals("FL") && !listKind.equals("RL") )
			throw new IllegalArgumentException("not supported listName (AL/BL/FL/RL");
			
		OutgoingMessage out = new OutgoingMessage("ADC");
		markTransactionId( out );
		out.add( listKind );
		out.add( "N=" + loginName );
		if( listKind.equals("FL") )
		{
			out.add( "F=" + loginName );
		}
		out.setBackProcess(Callback.getInstance("responseAdd", this.getClass()));
		sendMessage( out );
	}

	public void responseAdd( IncomingMessage msg ) throws IOException
	{
		String header = msg.getHeader();
		if( header.equals("ADC") )
		{
			String code = msg.get(0);
			String loginName = msg.get(1);
			if( loginName.startsWith("N=") )
				loginName = loginName.substring(2);
			String friendlyName = loginName;
			String loginCode = null;
			if( code.equals("FL") )
			{
				friendlyName = msg.get(2);
				if( friendlyName.startsWith("F=") )
					friendlyName = friendlyName.substring(2);
				loginCode = msg.get(3);
				if( loginCode.startsWith("C=") )
					loginCode = loginCode.substring(2);
			}

			MsnFriend friend = new MsnFriend(loginName, friendlyName);
			friend.setGroupIndex( NO_GROUP_IDX );
			friend.setCode( loginCode );
			BuddyList bl = msn.getBuddyGroup().getListAsCode(code);
			if( bl!=null )
				bl.add( friend );

			storeLocalCopy();
		}
		else
		{
			try {
				int errorCode = Integer.parseInt( header );
				msn.fireAddFailedEvent( errorCode );
			} catch( NumberFormatException e ) {}
		}
	}

	public void requestRemove( String loginName ) throws IOException
	{
		requestRemoveAsList( loginName, "FL" );
		requestRemoveAsList( loginName, "AL" );
	}

	/**
	 *
	 * @param listKind Must in 'FL', 'AL', 'BL', 'RL'. 
	 */
	public void requestRemoveAsList( String loginName, String listKind ) 
		throws IOException, IllegalArgumentException
	{
		requestRemoveAsList( loginName, listKind, false );
	}

	public void requestRemoveAsList( String loginName, String listKind, boolean isCode ) 
		throws IOException, IllegalArgumentException
	{
		if( !listKind.equals("AL") && !listKind.equals("BL") && 
			!listKind.equals("FL") && !listKind.equals("RL") &&
			!listKind.equals("PL") )
			throw new IllegalArgumentException("not supported listName (AL/BL/FL/RL/PL");

		OutgoingMessage out = new OutgoingMessage("REM");
		markTransactionId( out );
		out.add( listKind );
		if( !isCode && listKind.equals("FL") )
		{
			BuddyList bl = msn.getBuddyGroup().getListAsCode("FL");
			if( bl.get(loginName)!=null )
				out.add( bl.get(loginName).getCode() );
		}
		else
		{
			out.add( loginName );
		}

		out.setBackProcess(Callback.getInstance("responseRemove", this.getClass()));
		sendMessage( out );
	}

	public void responseRemove( IncomingMessage msg ) throws IOException
	{
		if( msg.getHeader().equals("REM") )
		{
			String code = msg.get(0);
			String loginName = msg.get(1);

			BuddyList bl = msn.getBuddyGroup().getListAsCode(code);
			if( bl!=null )
			{
				if( code.equals("FL") )
				{
					bl.removeAsCode(loginName);
				}
				else
				{
					bl.remove( loginName );
				}
			}
			storeLocalCopy();
		}
	}

	/**
	 * Block/Unblock the specified user.
	 */
	public void requestBlock( String loginName, boolean isUnblock ) 
		throws IOException
	{
		BuddyList fl = msn.getBuddyGroup().getForwardList();
		MsnFriend friend = fl.get( loginName );
		if( friend==null )
			return;

		Callback cb = Callback.getInstance("responseBlock", this.getClass());

		OutgoingMessage out = new OutgoingMessage("REM");
		markTransactionId( out );
		out.add( isUnblock ? "BL" : "AL" );
		out.add( loginName );
		out.setBackProcess( cb );

		sendMessage( out );

		OutgoingMessage out2 = new OutgoingMessage("ADC");
		markTransactionId( out2 );
		out2.add( isUnblock ? "AL" : "BL" );
		out2.add( "N=" + loginName );
		out2.setBackProcess( cb );

		sendMessage( out2 );
	}

	/**
	 * Callback to unblock user.
	 */
	public void responseBlock( IncomingMessage msg ) throws IOException
	{
		String header = msg.getHeader();
		
		if( Character.isDigit(header.charAt(0)) )
			return;

		String code = msg.get(0);
		String loginName = msg.get(1);
		if( loginName.startsWith("N=") )
			loginName = loginName.substring(2);
		
		BuddyList bl = msn.getBuddyGroup().getListAsCode(code);
		if( bl!=null )
		{
			if( header.equals("REM") )
			{
				bl.remove( loginName );
				storeLocalCopy();
			}
			else
			if( header.equals("ADC") )
			{
				bl.add( new MsnFriend(loginName, loginName) );
				storeLocalCopy();
			}
		}		
	}

	
	public void requestMoveGroup( MsnFriend friend, String oldIndex, String newIndex )
		throws IOException
	{
		Callback cb = null;	
		OutgoingMessage out = null;

		if( !newIndex.equals(NO_GROUP_IDX) )
		{
			cb = Callback.getInstance("responseGroupAdd", this.getClass());
			out = new OutgoingMessage("ADC");
			markTransactionId( out );
			out.add( "FL" );
			out.add( "C=" + friend.getCode() );
			out.add( newIndex );
			out.setBackProcess( cb );

			sendMessage( out );
		}

		if( !oldIndex.equals(NO_GROUP_IDX) )
		{
			cb = Callback.getInstance("responseGroupRemove", this.getClass());
			out = new OutgoingMessage("REM");
			markTransactionId( out );
			out.add( "FL" );
			out.add( friend.getCode() );
			out.add( oldIndex );
			out.setBackProcess( cb );

			sendMessage( out );
		}
	}

	public void responseGroupAdd( IncomingMessage msg ) throws IOException
	{
		if( msg.size() < 3 )
			return;

		String code = msg.get(0);
		String userCode = msg.get(1);
		String groupCode = msg.get(2);

		BuddyList bl = msn.getBuddyGroup().getListAsCode(code);
		if( bl!=null )
		{
			if( userCode.startsWith("C=") )
				userCode = userCode.substring(2);
			MsnFriend f = bl.getAsCode( userCode );
			f.setGroupIndex( groupCode );
		}

		storeLocalCopy();
	}

	public void responseGroupRemove( IncomingMessage msg ) throws IOException
	{
		if( msg.size() < 3 )
			return;

		String code = msg.get(0);
		String userCode = msg.get(1);
		String groupCode = msg.get(2);

		storeLocalCopy();
	}

	
	public void requestCreateGroup( String groupName ) throws IOException
	{
		Callback cb = Callback.getInstance("responseCreateGroup", this.getClass());
		OutgoingMessage out = new OutgoingMessage("ADG");
		markTransactionId( out );
		out.add( MimeUtility.getURLEncodedString(groupName, "UTF-8") );
		out.add( 0 );
		out.setBackProcess( cb );

		sendMessage( out );
	}

	public void responseCreateGroup( IncomingMessage msg ) throws IOException
	{
		if( msg.getHeader().equals("ADG") )
		{
		    String newName = MimeUtility.getURLDecodedString(msg.get(0), "UTF-8");
			String index = msg.get(1);

			Group group = new Group( newName, index );
			GroupList gl = msn.getBuddyGroup().getGroupList();
			gl.addGroup( group );

			storeLocalCopy();
		}
	}

	private void storeLocalCopy()
	{
		msn.storeLocalCopy(lastFrom, lastTo);
	}

	public void requestRemoveGroup( String groupIndex ) throws IOException
	{
		Callback cb = Callback.getInstance("responseRemoveGroup", this.getClass());
		OutgoingMessage out = new OutgoingMessage("RMG");
		markTransactionId( out );
		out.add( groupIndex );
		out.setBackProcess( cb );

		sendMessage( out );
	}

	public void responseRemoveGroup( IncomingMessage msg ) throws IOException
	{
		if( msg.getHeader().equals("RMG") )
		{
			String index = msg.get(0);

			GroupList gl = msn.getBuddyGroup().getGroupList();
			gl.removeGroup(index);

			storeLocalCopy();
		}
	}

	public void requestRenameGroup( String groupIndex, String newName ) throws IOException
	{
		Callback cb = Callback.getInstance("responseRenameGroup", this.getClass());
		OutgoingMessage out = new OutgoingMessage("REG");
		markTransactionId( out );
		out.add( groupIndex );
		out.add( MimeUtility.getURLEncodedString(newName, "UTF-8") );
		out.setBackProcess( cb );

		sendMessage( out );
	}

	public void responseRenameGroup( IncomingMessage msg ) throws IOException
	{
		if( msg.getHeader().equals("REG") )
		{
		    GroupList gl = msn.getBuddyGroup().getGroupList();
		    String index = msg.get(0);
			String newName = MimeUtility.getURLDecodedString(msg.get(1), "UTF-8");

			Group g = gl.getGroup(index);
			g.setName( newName );

			storeLocalCopy();
		}
	}

	public SwitchboardSession doCallFriendWait( String loginName )
		throws IOException, InterruptedException
	{
		Callback cb = Callback.getInstance("connectToSwitchboard", this.getClass());

		OutgoingMessage out = new OutgoingMessage("XFR");
		markTransactionId( out );
		out.add( "SB" );
		out.setBackProcess( cb );

		Integer tr = new Integer(out.getTransactionId());
		callIdMap.put( tr, loginName );

		sendMessage( out );

		// For wait, create temporary lock object and push lock map.
		// It will notify by callback process

		Object lock = new Object();
		lockMap.put( tr, lock );

		synchronized( lock )
		{
			lock.wait(10000);
		}

		return (SwitchboardSession)sessionMap.get(tr);
	}

	
	public void connectToSwitchboard( IncomingMessage msg ) throws IOException
	{
		if( callIdMap.size()==0 )
			return;

		ServerInfo serv = msg.getServerInfo(1);
		if( serv==null )
			return;

		final Integer tr = new Integer(msg.getTransactionId());
		final String cookie = msg.get(3);
		final String toCallLoginName = (String)callIdMap.get(new Integer(msg.getTransactionId()));

		SwitchboardSession ss = new SwitchboardSession( msn, serv, (String)null ) {

			private String firstCallName = null;
			private boolean isFirstJoin = true;

			public void init() throws IOException
			{
				this.firstCallName = toCallLoginName;
				Callback cb = Callback.getInstance("processUserCall", this.getClass());

				OutgoingMessage out = new OutgoingMessage("USR");
				markTransactionId( out );
				out.add( msn.getLoginName() );
				out.add( cookie );
				out.setBackProcess( cb );

				sendMessage( out );
			}

			public void processUserCall( IncomingMessage msg ) throws IOException
			{
				// USR trId OK MyLoginName MyFriendlyName
				// I need not yet. :)

				Callback cb = Callback.getInstance("processCallResult",this.getClass());

				OutgoingMessage out = new OutgoingMessage("CAL");
				markTransactionId( out );
				out.add( firstCallName );
				out.setBackProcess( cb );

				sendMessage( out );
			}

			protected void processWhoJoined( IncomingMessage msg ) throws Exception
			{
				callingMap.remove( toCallLoginName );
				super.processWhoJoined( msg );

				if( isFirstJoin )
				{
					isFirstJoin = false;
					msn.fireSwitchboardSessionStartedEvent( this );

					if( lockMap.containsKey(tr) )
					{
						sessionMap.put( tr, this );
						Object lock = lockMap.remove(tr);
						if( lock!=null )
						{
							synchronized(lock)
							{
								lock.notify();
							}
						}
					}

					sessionOpened();
				}
			}

			public void processCallResult( IncomingMessage msg ) throws IOException
			{
				callingMap.remove( toCallLoginName );
				String sessionId = msg.get(1);
				setSessionId( sessionId );				
			}

			public void cleanUp()
			{
			    super.cleanUp();
				callingMap.remove( toCallLoginName );

				if( getSessionId()==null )
				    msn.fireSwitchboardSessionAbandonEvent( this, firstCallName );
			}
		};
		ss.setTarget( toCallLoginName );
		ss.start();
	}

	public void cleanUp()
	{
		if( msn.isLogged )
		{
			if( callbackCleaner!=null )
			{
				callbackCleaner.interrupt();
				callbackCleaner = null;
			}
			msn.isLogged = false;
			msn.fireLogoutNotifyEvent();
		}
	}

	
	public void logout() throws IOException
	{
		cleanUp();

		isLive = false;
		setAutoOutSend( false );

		OutgoingMessage out = new OutgoingMessage("OUT");
		sendMessage( out );

		interrupt();
	}
};
