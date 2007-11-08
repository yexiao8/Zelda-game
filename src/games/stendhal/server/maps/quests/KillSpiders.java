package games.stendhal.server.maps.quests;

import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

/**
 * QUEST: Kill Spiders
 *
 * PARTICIPANTS: - Morgrin
 *
 * STEPS: - Groundskeeper Morgrin ask you to clean up the school basement
 *        - You go kill the spiders in the basement and you get the reward from Morgrin
 *
 * REWARD: - magical egg - 5000 XP
 *
 * REPETITIONS: - None.
 */

public class KillSpiders extends AbstractQuest {

	private static final String QUEST_SLOT = "kill_all_spiders";


	private void step_1() {
		SpeakerNPC npc = npcs.get("Morgrin");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, null,
				ConversationStates.QUEST_OFFERED, null,
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text,
							SpeakerNPC engine) {
						if (!player.hasQuest(QUEST_SLOT)
								|| player.getQuest(QUEST_SLOT).equals(
										"rejected")) {
							engine
									.say("Have you ever been to the basement of the school? The room is full of spiders and some could be dangerous, since the students do experiments! Would you like to help me with this 'little' problem?");
						}  else if (player.isQuestCompleted(QUEST_SLOT)) {
							engine
									.say("I already asked you to kill all creatures in the basement!");
							engine
									.setCurrentState(ConversationStates.ATTENDING);
						}  else if (player.getQuest(QUEST_SLOT).startsWith(	"killed;")) {
							String[] tokens = player.getQuest(QUEST_SLOT).split(";");
							long delay = 7 * 24 * 60 *  10 * 60 * 1000;
							long timeRemaining = (Long.parseLong(tokens[1]) + delay) - System.currentTimeMillis();
							if (timeRemaining > 0) {
								engine.say("Sorry there is nothing to do for you yet. But maybe you could come back later. I have to clean the school once a week.");
								return;
							}
							engine.say("Would you like to help me again?");
							engine.setCurrentState(ConversationStates.QUEST_OFFERED);
						} else {
							engine
									.say("Thanks for your help. Now I'm sleeping well again.");
							engine
									.setCurrentState(ConversationStates.ATTENDING);
						}
					}
				});

		npc
				.add(
						ConversationStates.QUEST_OFFERED,
						ConversationPhrases.YES_MESSAGES,
						null,
						ConversationStates.ATTENDING,
						"Fine. Go down to the basement and kill all the creatures there!",
						new SpeakerNPC.ChatAction() {
							@Override
							public void fire(Player player, String text,
									SpeakerNPC engine) {
								player.addKarma(5.0);
								player.setQuest(QUEST_SLOT, "start");
								player.removeKill("spider");
								player.removeKill("poisonous_spider");
								player.removeKill("giant_spider");
							}
						});

		npc.add(ConversationStates.QUEST_OFFERED, "no", null,
				ConversationStates.ATTENDING,
				"Ok, i have to find someone else to do this 'little' job!",
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text,
							SpeakerNPC engine) {
						player.addKarma(-5.0);
						player.setQuest(QUEST_SLOT, "rejected");
					}
				});
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		SpeakerNPC npc = npcs.get("Morgrin");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new SpeakerNPC.ChatCondition() {
					@Override
					public boolean fire(Player player, String text,
							SpeakerNPC engine) {
						return player.hasQuest(QUEST_SLOT)
								&& player.getQuest(QUEST_SLOT).equals(
										"start");
					}
				}, ConversationStates.ATTENDING, null,
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text,
							SpeakerNPC engine) {
						if (player.hasKilled("spider")
								&& player.hasKilled("poisonous_spider")
								&& player.hasKilled("giant_spider")) {
							engine
										.say("Oh thank you my friend. Here you have something special, I got it from a Magican. Who he was I do not know. What the egg's good for, I do not know. I only know, it could be useful for you.");
								Item mythegg = StendhalRPWorld.get()
										.getRuleManager().getEntityManager()
										.getItem("mythical_egg");
								mythegg.setBoundTo(player.getName());
								player.equip(mythegg, true);
								player.addKarma(5.0);
								player.addXP(5000);
								player.setQuest(QUEST_SLOT, "killed;" + System.currentTimeMillis());
						} else {
							engine
									.say("Go down and kill the creatures, no time left.");
						}
		 			}
				});
	}

	@Override
	public void addToWorld() {
		super.addToWorld();

		step_1();
		step_2();
		step_3();
	}
}
