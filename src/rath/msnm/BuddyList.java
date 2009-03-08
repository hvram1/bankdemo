/*
 * @(#)BuddyList.java
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
 *    $Id: BuddyList.java,v 1.9 2005/05/08 20:51:48 xrath Exp $
 */
package rath.msnm;

import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Collections;

import rath.msnm.entity.MsnFriend;

public class BuddyList
{
	private final String name;
	private List list = null;
	private Map map = null;
	private Map codeMap = null;

	
	public BuddyList( String name )
	{
		this.name = name;

		this.list = Collections.synchronizedList( new ArrayList() );
		this.map = Collections.synchronizedMap( new HashMap() );
		this.codeMap = Collections.synchronizedMap( new HashMap() );
	}

	public String getName()
	{
		return this.name;
	}

	
	public void add( MsnFriend friend )
	{
		String loginName = friend.getLoginName();
		Object o = null;
		if( (o=map.get(loginName))!=null )
		{
			MsnFriend old = (MsnFriend)o;
			old.setFriendlyName( friend.getFriendlyName() );
			old.setStatus( friend.getStatus() );
		}
		else
		{
			list.add( friend );
			map.put( loginName, friend );
			codeMap.put( friend.getCode(), friend );
		}
	}

	public void remove( MsnFriend friend )
	{
		list.remove( friend );
		map.remove( friend.getLoginName() );
		codeMap.remove( friend.getCode() );
	}

	public void remove( String loginName )
	{
		Object o = map.remove(loginName);
		if( o!=null )
		{
			list.remove( o );
			codeMap.remove( ((MsnFriend)o).getCode() );
		}
	}

	public void removeAsCode( String code )
	{
		Object o = codeMap.remove( code );
		if( o!=null )
		{
			list.remove( o );
			map.remove( ((MsnFriend)o).getLoginName() );
		}
	}

	public MsnFriend get(int index)
	{
		return (MsnFriend)list.get(index);
	}

	public MsnFriend getAsCode( String code )
	{
		return (MsnFriend)codeMap.get(code);
	}

	public MsnFriend get( String loginName )
	{
		return (MsnFriend)map.get( loginName );
	}

	
	public void set( MsnFriend friend )
	{
		MsnFriend mf = get(friend.getLoginName());
		if( mf!=null )
		{
			mf.setFriendlyName( friend.getFriendlyName() );
			mf.setStatus( friend.getStatus() );
		}
		else
			throw new IllegalArgumentException( friend.getLoginName() + " not found on " + getName() );
	}

	
	public void setOffline( String loginName )
	{
		MsnFriend mf = get(loginName);
		if( mf!=null )
			mf.setStatus( UserStatus.OFFLINE );
		/*
		else
			throw new IllegalArgumentException( loginName + " not found on " + getName() );
		*/
	}

	public Iterator iterator()
	{
		return list.iterator();
	}

	public int size()
	{
		return list.size();
	}

	public synchronized void sort( Comparator comp )
	{
	    Collections.sort( list, comp );
	}

	public void clear()
	{
		list.clear();
		map.clear();
		codeMap.clear();
	}

}
