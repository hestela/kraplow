package com.chriscarr.bang.cards;

import java.util.List;

import com.chriscarr.bang.CancelPlayer;
import com.chriscarr.bang.Deck;
import com.chriscarr.bang.Discard;
import com.chriscarr.bang.Player;
import com.chriscarr.bang.Turn;
import com.chriscarr.bang.Hand;
import com.chriscarr.bang.Figure;
import com.chriscarr.bang.userinterface.UserInterface;

public class Springfield extends Card implements Playable {
	public Springfield(String name, int suit, int value, int type) {
		super(name, suit, value, type);
	}

	/* (non-Javadoc)
	 * @see com.chriscarr.bang.Playable#canPlay(com.chriscarr.bang.Player, java.util.List, int)
	 */
	public boolean canPlay(Player player, List<Player> players, int bangsPlayed){			
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.chriscarr.bang.Playable#targets(com.chriscarr.bang.Player, java.util.List)
	 */
	public List<Player> targets(Player player, List<Player> players){
		return players;
	}
	
	/* (non-Javadoc)
	 * @see com.chriscarr.bang.Playable#play(com.chriscarr.bang.Player, java.util.List, com.chriscarr.bang.UserInterface, com.chriscarr.bang.Deck, com.chriscarr.bang.Discard)
	 */
	public boolean play(Player currentPlayer, List<Player> players, UserInterface userInterface, Deck deck, Discard discard, Turn turn){
		//Choose card to discard
		int cardDiscard = userInterface.askDiscard(currentPlayer);
		if(cardDiscard == -1){
			return false;
		}
		//Choose player to give a draw to
		Player targetPlayer = Turn.getValidChosenPlayer(currentPlayer, players, userInterface);
		//discard the card
		Hand currentHand = currentPlayer.getHand();
		Object card = currentHand.remove(cardDiscard);
		discard.add(card);
		discard.add(this);
		//shoot player
		List<Player> others = Turn.others(currentPlayer, players);
		Player otherPlayer = Turn.getValidChosenPlayer(currentPlayer, others, userInterface);
		if(!(otherPlayer instanceof CancelPlayer)){
			userInterface.printInfo(currentPlayer.getName() + " Shoots " + otherPlayer.getName());
			if(Figure.APACHEKID.equals(otherPlayer.getAbility()) && this.getSuit() == Card.DIAMONDS){
				userInterface.printInfo(otherPlayer.getName() + " is unaffected by diamond Shoot");
				return true;
			}
			int missesRequired = 1;
			int barrelMisses = Turn.isBarrelSave(otherPlayer, deck, discard, userInterface, missesRequired, currentPlayer);
			missesRequired = missesRequired - barrelMisses;
			if(missesRequired <= 0){
				return true;
			} else if(missesRequired == 1){
				int missPlayed = Turn.validPlayMiss(otherPlayer, userInterface); 
				if(missPlayed == -1){
					turn.damagePlayer(otherPlayer, players, currentPlayer, 1, currentPlayer, deck, discard, userInterface);
					userInterface.printInfo(otherPlayer.getName() + " is loses a health.");
				} else {
					for(int i = 0; i < missesRequired; i++){
						Card missCard = (Card)otherPlayer.getHand().remove(missPlayed);
						discard.add(missCard);
						if(missCard.getName().equals(CARDDODGE)){
							Hand otherHand = otherPlayer.getHand();
							otherHand.add(deck.pull());
							userInterface.printInfo(otherPlayer.getName() + " dodged " + currentPlayer.getName() + "'s " + Card.CARDBANG + " and draws a card");
						} else {
							userInterface.printInfo(otherPlayer.getName() + " plays a Missed!");
						}
						if(Figure.MOLLYSTARK.equals(otherPlayer.getAbility())){
							Hand otherHand = otherPlayer.getHand();
							otherHand.add(deck.pull());
							userInterface.printInfo(otherPlayer.getName() + " draws a card");
						}
					}
				}
			}
			return true;
		} else {
			currentPlayer.getHand().add(this);
			return false;
		}
	}
}