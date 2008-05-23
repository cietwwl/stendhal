package games.stendhal.server.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;

import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;

public class OutfitActionTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testOnWrongAction() {
		OutfitAction oa = new OutfitAction();
		Player player = PlayerTestHelper.createPlayer("player");
		RPAction action = new RPAction();
		oa.onAction(player, action);
		assertTrue("no exception thrown", true);
	}

	@Test(expected = NumberFormatException.class)
	public void testOnActionWrongValue() {
		OutfitAction oa = new OutfitAction();
		Player player = PlayerTestHelper.createPlayer("player");
		RPAction action = new RPAction();
		action.put("value", "schnick");
		oa.onAction(player, action);
	}

	@Test
	public void testOnAction() {
		OutfitAction oa = new OutfitAction();
		Player player = PlayerTestHelper.createPlayer("player");
		RPAction action = new RPAction();
		assertNull(player.get("outfit"));
		action.put("value", 1);
		oa.onAction(player, action);
		assertTrue(player.has("outfit"));
		assertEquals("1", player.get("outfit"));

		action.put("value", 51515151);
		oa.onAction(player, action);
		assertTrue(player.has("outfit"));
		assertEquals("invalid player outfit", "1", player.get("outfit"));
	}

}
