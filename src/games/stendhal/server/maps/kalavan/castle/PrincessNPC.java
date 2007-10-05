package games.stendhal.server.maps.kalavan.castle;

import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.config.ZoneConfigurator;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.pathfinder.FixedPath;
import games.stendhal.server.pathfinder.Node;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds the princess in Kalavan castle
 *
 * @author kymara
 */
public class PrincessNPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone, attributes);
	}

	private void buildNPC(StendhalRPZone zone, Map<String, String> attributes) {
		SpeakerNPC princessNPC = new SpeakerNPC("Princess Ylflia") {

			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(19, 21));
				nodes.add(new Node(19, 41));
				nodes.add(new Node(22, 41));
				nodes.add(new Node(14, 41));
				nodes.add(new Node(14, 48));
				nodes.add(new Node(18, 48));
				nodes.add(new Node(19, 48));
				nodes.add(new Node(19, 41));
				nodes.add(new Node(22, 41));
				nodes.add(new Node(20, 41));
				nodes.add(new Node(20, 21));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
			        addGreeting("How do you do?");
				addReply("good","Good! Can I help you?");
				addReply("bad","Oh dear ... Can I help you?");
				addReply("well","Wonderful! Can I help you?");
				addJob("I am the princess of this kingdom. To become one of my citizens, speak to Barrett Holmes in the city. He may be able to sell you a house.");
				addHelp("Watch out for mad scientists. My father allowed them liberty to do some work in the basement and I am afraid things have got rather out of hand.");
				addQuest("I don't need any favour just now, thank you.");
				addOffer("Sorry, but I do not have anything to offer you.");				
				addGoodbye("Goodbye, and good luck.");
			}
		};

		princessNPC.setEntityClass("princess2npc");
		princessNPC.setPosition(19, 21);
		princessNPC.initHP(100);
		zone.add(princessNPC);
	}
}
