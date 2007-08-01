/* $Id$ */
/***************************************************************************
 *		      (C) Copyright 2003 - Marauroa		      *
 ***************************************************************************
 ***************************************************************************
 *									 *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.				   *
 *									 *
 ***************************************************************************/
package games.stendhal.client.entity;

import games.stendhal.client.NotificationType;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import marauroa.common.game.RPObject;

/** A Player entity */
public class Player extends RPEntity {
	/**
	 * Away property.
	 */
	public final static Object	PROP_AWAY	= new Object();

	/**
	 * The away message of this player.
	 */
	private String	away;


	/**
	 * Create a player entity.
	 */
	public Player() {
		away = null;
	}

	
	/**
       *  The path to the player's outfit (eg. if they want to use something that isn't in /data/sprites/outfits...). Can be a URL to a web address.
       */
        private String outfitPath = "data/sprites/outfit/player_base_0.png"; //default
	
	/**
	 * The height of the player
	 */
	private double height = 0;
	
	/**
	 * The width of the player
	 */
	private double width = 0;

	//
	// Player
	//

	/**
	 * Determine if the player is away.
	 *
	 * @return	<code>true</code> if the player is away.
	 */
	public boolean isAway() {
		return (getAway() != null);
	}


	/**
	 * Get the away message.
	 *
	 * @return	The away text, or <code>null</code> if not away.
	 */
	public String getAway() {
		return away;
	}


	/**
	 * An away message was set/cleared.
	 *
	 * @param	message		The away message, or <code>null</code>
	 *				if no-longer away.
	 */
	protected void onAway(final String message) {
		addTextIndicator(((message != null) ? "Away" : "Back"), NotificationType.INFORMATION);
	}
	
	/**
        * Gets the outfit path
        * @return  The path to the outfit the player is using
        */
        public String getOutfitPath() {
            return outfitPath;
        }


	//
	// Entity
	//
	
	/**
        * Gets the player's height. 
        * @return height The height of the player
        */
        protected double getHeight() {
            return height;
        }
	
        
        /**
       * Gets the width of the player
       * @return the width of the player
       */
        protected double getWidth() {
            return width;
        }

	/**
	 * Get the area the entity occupies.
	 *
	 * @return	A rectange (in world coordinate units).
	 */
	@Override
	public Rectangle2D getArea() {
		return new Rectangle.Double(getX(), getY() + 1, getWidth(), getHeight());
	}


	//
	// RPObjectChangeListener
	//

	/**
	 * The object added/changed attribute(s).
	 *
	 * @param	object		The base object.
	 * @param	changes		The changes.
	 */
	@Override
	public void onChangedAdded(final RPObject object, final RPObject changes) {
		super.onChangedAdded(object, changes);

		if (changes.has("away")) {
			/*
			 * Filter out a player "changing" to the same message
			 */
			if (!object.has("away") || !object.get("away").equals(changes.get("away"))) {
				away = changes.get("away");
				fireChange(PROP_AWAY);
				onAway(away);
			}
		}
		
		if (changes.has("outfit_path")) {
                	if (!object.has("outfit_path") || !object.get("outfit_path").equals(changes.get("outfit_path"))) {
				outfitPath = changes.get("outfit_path");
			}
                }
		
		if (changes.has("height") && !object.has("height")|| changes.has("height") && object.has("height") && !object.get("height").equals(changes.get("height"))) {
			height = changes.getInt("height");
                }
                
                if (changes.has("width") && !object.has("width") || changes.has("width") && object.has("width") && !object.get("width").equals(changes.get("width"))) {
			width = changes.getInt("width");
                }
	}

	/**
	 * The object removed attribute(s).
	 *
	 * @param	object		The base object.
	 * @param	changes		The changes.
	 */
	@Override
	public void onChangedRemoved(final RPObject object, final RPObject changes) {
		super.onChangedRemoved(object, changes);

		if (changes.has("away")) {
			away = null;
			fireChange(PROP_AWAY);
			onAway(null);
		}
		
		if (changes.has("height")) {
                    if (!object.has("height")) {
                        height = 0;
                    }
                }
                
                if (changes.has("width")) {
                    if (!object.has("width")) {
                        width = 0;
                    }
                }
		
		if (changes.has("outfit_path")) {
			outfitPath = null;
		}
			
	}
}
