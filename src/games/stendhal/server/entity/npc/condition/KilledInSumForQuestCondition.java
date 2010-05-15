package games.stendhal.server.entity.npc.condition;

import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.parser.Sentence;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

/**
 * Checking sum of quest creatures kills in player's quest and kills slots..
 * Quest string should have in proper quest index string like "creature1,w,x,y,z,creature2,a,b,c,d,creature3,..."
 * Where creature1, creature2 - names of creatures to kill;
 *       w,x and a,b - number of creatures to kill, solo and shared;
 *       y,z and c,d - number of creatures killed by player before starting this quest, both solo and shared.
 * 
 * @author yoriy
 */
public class KilledInSumForQuestCondition implements ChatCondition {
	private static Logger logger = Logger.getLogger(KilledForQuestCondition.class);
	private final String QUEST_SLOT;
	private final int questIndex;
	private final int killsSum;
	

	/**
	 * Creates a new KilledInSumForQuestCondition.
	 * 
	 * @param quest - the quest slot
	 * @param index - quest slot index where information stored 
	 * @param killsSum - required sum of creatures kills
	 */
	public KilledInSumForQuestCondition(String quest, int index, int killsSum) {
		this.QUEST_SLOT=quest;
		this.questIndex=index;
		this.killsSum=killsSum;
	}

	/**
	 * return true if player have killed proper sum of creatures.
	 */
	public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
		final String temp = player.getQuest(QUEST_SLOT, questIndex);
		if(temp==null) {
			return false;
		}
		final List<String> tokens = Arrays.asList(temp.split(","));
		// check for size - it should be able to divide by 5 without reminder.
		if((tokens.size() % 5)!=0) {
			logger.error("Wrong record in player's "+player.getName()+
					" quest slot ("+QUEST_SLOT+") : ["+player.getQuest(QUEST_SLOT)+"]");
			//npc.say("something wrong with you, i dont see how much monsters you killed.");
			return false;
		};
		int sum=0;
		for(int i=0; i<tokens.size()/5; i++) {
			final String creatureName=tokens.get(i*5);
			int killedSolo;
			int killedShared;
			try {
				killedSolo=Integer.parseInt(tokens.get(i*5+3));
				killedShared=Integer.parseInt(tokens.get(i*5+4));				
			} catch (NumberFormatException npe) {
				logger.error("NumberFormatException while parsing numbers in quest slot "+QUEST_SLOT+
						" of player "+player.getName()
						+" , creature " + i*5);
				return false;
			};
			final int diffSolo = player.getSoloKill(creatureName) - killedSolo;
			final int diffShared = player.getSharedKill(creatureName) - killedShared;
			sum=sum+diffSolo+diffShared;
		}
		if(sum<killsSum) {
			return false;
		};
		return true;
	}

	@Override
	public String toString() {
		return "KilledForQuestCondition";
	}

	@Override
	public boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, false,
				KilledInSumForQuestCondition.class);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

}
