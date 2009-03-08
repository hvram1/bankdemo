/*
 * @(#)MsnFriend.java
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
 *    $Id: MsnFriend.java,v 1.13 2005/05/06 17:03:50 xrath Exp $
 */
package rath.msnm.entity;

import java.awt.Image;
import java.io.UnsupportedEncodingException;

import rath.msnm.UserStatus;
import rath.msnm.msg.MimeUtility;
/**
 * 夏昭----------->bluempire@hotmail.com: 夏昭 (FLN)
 * friendlyName是夏昭
 * formatFriendlyName是夏昭
 * 只是做了一些URLEncoder的操作。
 * 
 * @author sheng.liuzs@alipay.com
 * @version $Id: MsnFriend.java, v 0.1 2008-10-4 下午02:17:41 sheng.liuzs@alipay.com Exp $
 */
public class MsnFriend implements UserStatus
{
	private String groupIndex = "0";
	private String status = null;
	private String oldStatus = null;
	private String loginName = null;
	private String friendlyName = null;
	private String formatFriendlyName = null;
	private String code = null;
	private int accessValue = 0;

	private String photoContext;
	private boolean photoUpdated = true;
	private Image photo;

	public MsnFriend( String loginName )
	{
		this( loginName, null );
	}

	public MsnFriend( String loginName, String friendlyName )
	{
		this.loginName = loginName;
		this.friendlyName = friendlyName;

		setStatus( UserStatus.OFFLINE );
	}

	public void setAccessValue( int access )
	{
		this.accessValue = access;
	}

	public int getAccessValue()
	{
		return this.accessValue;
	}

	public void setGroupIndex( String index )
	{
		this.groupIndex = index;
	}

	public String getGroupIndex()
	{
		return this.groupIndex;
	}

	public void setCode( String code )
	{
		this.code = code;
	}

	public String getCode()
	{
		return this.code;
	}

	public void setLoginName( String loginName )
	{
		this.loginName = loginName;
	}

	public String getLoginName()
	{
		return this.loginName;
	}

	public void setFriendlyName( String frName )
	{
		this.friendlyName = frName;
		this.formatFriendlyName = null;
	}

	public String getFriendlyName()
	{
		return this.friendlyName;
	}

	
	public String getFormattedFriendlyName()
	{
		if( formatFriendlyName!=null )
			return formatFriendlyName;
		if( this.friendlyName==null )
			return null;

		try
		{
			this.formatFriendlyName = MimeUtility.getURLDecodedString(
				this.friendlyName, "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
		    this.formatFriendlyName = friendlyName;
		}
		return formatFriendlyName;
	}

	public void setStatus( String st )
	{
		if( st==null )
			st = UserStatus.OFFLINE;

		oldStatus = this.status;
		this.status = st;
	}

	
	public String getOldStatus()
	{
		return this.oldStatus;
	}

	public String getStatus()
	{
		return this.status;
	}

	/**
	 * @deprecated This method is deprecated for internationalization problem.
	 */
	public String getFormattedStatus() throws IllegalAccessException
	{
		throw new IllegalAccessException("Deprecated");
	}

	public String toString()
	{
		return loginName + ":" + friendlyName + " (" + status + ")";
	}

	public boolean equals( Object o )
	{
		if( this==o )
			return true;
		if( o!=null && o instanceof MsnFriend )
			return loginName.equals( ((MsnFriend)o).loginName );
		return false;
	}

	public int hashCode()
	{
		return loginName.hashCode();
	}

	public String toFormattedString()
	{
		return loginName + ": " + getFormattedFriendlyName() + " (" + getStatus() + ")";
	}
};
