package asw1022.model.dixit;

import asw1022.util.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This class keeps the state of a single game execution.
 * @author Roberto Casadei <roberto.casadei12@studio.unibo.it>
 */
public class GameExecution {
    
    protected List<Card> allcards;
    protected LinkedList<Card> deck;
    protected List<Player> players;
    protected Map<Player, List<Card>> cardsPerPlayer;
    protected Map<Player, Integer> pointsPerPlayer;
    protected Map<Player, Card> selectedCardForPlayer;
    protected Map<Player, Player> votes;
    protected Match match;
    protected GamePhase phase;
    protected Player turn;
    protected int numCardsPerPlayer;
    protected String phrase;
    protected int pointLimit = 3;
    protected Player winner = null;
    
    public GameExecution(Match match, List<Card> allcards){
        this.match = match;
        this.allcards = allcards;
        this.deck = new LinkedList<Card>(Utils.Shuffle(allcards));
        this.players = new ArrayList<Player>();
        this.phase = GamePhase.Setup;
        this.numCardsPerPlayer = 4;
        this.cardsPerPlayer = new HashMap<Player, List<Card>>();
        this.pointsPerPlayer = new HashMap<Player, Integer>();
        this.selectedCardForPlayer = new HashMap<Player, Card>();
        this.votes = new HashMap<Player, Player>();
        this.phrase = null;
    } 
    
    /************************************/
    /* PHASE: Setup */
    /************************************/     
    
    public synchronized void addPlayer(Player p) throws GameException {
        if(this.phase != GamePhase.Setup)
            throw new GameException("Wrong phase. Trying to add player but phase is " + this.phase);
        
        if(!waitingForPlayers())
            throw new GameException("This match cannot accept any more players.");
        players.add(p);
        
        // then, assign cards
        Random rand = new Random();
        List<Card> cards = new ArrayList<Card>();
        for(int i=0; i<this.numCardsPerPlayer; i++){
            cards.add(deck.removeFirst());
        }
        cardsPerPlayer.put(p, cards);
        pointsPerPlayer.put(p, 0);
        
        if(!waitingForPlayers()){
            this.phase = GamePhase.SetPhrase;
            this.turn = players.get(0);
        }
    }
    

    /************************************/
    /* PHASE: Set Phrase */
    /************************************/      
    
    public synchronized void setPhrase(Player p, String phrase) throws GameException{
        if(this.phase != GamePhase.SetPhrase)
            throw new GameException("Wrong phase. Trying to set phrase but phase is " + this.phase);        
        if(!p.equals(this.turn))
            throw new GameException("The phrase can be set only by the player that owns the turn.");
        this.phrase = phrase;
        this.phase = GamePhase.SelectCard;
    }
    
    /************************************/
    /* PHASE: Select Card */
    /************************************/     
    
    public synchronized void selectCard(Player p, String cardName) throws GameException{
        if(this.phase != GamePhase.SelectCard)
            throw new GameException("Wrong phase. Trying to select card but phase is " + this.phase);
        
        Card previous = selectedCardForPlayer.get(p);
        if(previous!=null)
            throw new GameException("The player " + p.getName() + " has already chosen a card.");
        
        List<Card> playerCards = this.cardsPerPlayer.get(p);
        
        Card selected = null;
        for(Card c : playerCards){
            if(c.getName().equals(cardName)){
                selected = c;
                break;
            }
        }
        
        if(selected==null)
            throw new GameException("The selected card does not belong to the player.");
        
        // Let's remove the card
        playerCards.remove(selected);
        deck.addLast(selected);
        pickCardFromDeck(p);
        
        this.selectedCardForPlayer.put(p, selected);
        
        System.out.println(this.selectedCardForPlayer.size() + " out of " + this.players.size());
        
        // If all the players have chosen a card, advance the game
        if(this.selectedCardForPlayer.size() == this.players.size()){
            this.phase = GamePhase.Vote;
        }
    }    

    public synchronized void pickCardFromDeck(Player p){
        Card c = deck.removeFirst();
        this.cardsPerPlayer.get(p).add(c);
    }
    
    /************************************/
    /* PHASE: Vote */
    /************************************/     
    
    public synchronized void vote(Player voter, String cardName) throws GameException{
        if(this.phase != GamePhase.Vote)
            throw new GameException("Wrong phase. Trying to vote but phase is " + this.phase);
        
        if(this.votes.containsKey(voter))
            throw new GameException("Player " + voter.getName() + " has already voted.");
        
        Player votedByCard = null;
        for(Player p : this.selectedCardForPlayer.keySet()){
            Card c = this.selectedCardForPlayer.get(p);
            if(c.getName().equals(cardName)){
                votedByCard = p;
                break;
            }
        }
        
        if(votedByCard == null)
            throw new GameException("Player " + voter.getName() + 
                    " is voting a card which is not in the table.");
        
        this.votes.put(voter, votedByCard);
        
        if(this.votes.size() == (this.players.size()-1)){
            // 1) Calculate points
            CalculatePoints();
            
            // 2) Check if end
            for(Player p : pointsPerPlayer.keySet()){
                if(pointsPerPlayer.get(p) >= getPointLimit()){
                    this.phase = GamePhase.End;
                    this.winner = p;
                    return;
                }
            }
            
            // 3) Next phase
            this.phase = GamePhase.Results;
        }
    }
    
    protected synchronized void CalculatePoints(){
        int nVotesTurn = 0;
        List<Player> votersOfTurn = new ArrayList<Player>();
        for(Player voter : votes.keySet()){
            Player voted = votes.get(voter);
            
            if(voted==turn){
                nVotesTurn++;
                votersOfTurn.add(voter);
            }
        }
        
        if(nVotesTurn>0 && nVotesTurn<(this.players.size()-1)){
            // The player of current turn takes 3 points as well as those who voted him
            int npTurn = this.pointsPerPlayer.get(turn);
            this.pointsPerPlayer.put(turn, npTurn+3);
            for(Player p : votersOfTurn){
                int npVoter = this.pointsPerPlayer.get(p);
                this.pointsPerPlayer.put(p, npVoter+3);
            }
            
            // Moreover, each players get a point for each vote
            for(Player voter : votes.keySet()){
                Player voted = votes.get(voter);
                if(voted==voter) continue; // one cannot vote himself
                
                int np = this.pointsPerPlayer.get(voted);
                this.pointsPerPlayer.put(voted, np+1);
            }            
        } else{
            // The player of current turn takes 0 points, all the other players 2 points
            for(Player p : this.players){
                if(p==turn) continue;
                int npVoter = this.pointsPerPlayer.get(p);
                this.pointsPerPlayer.put(p, npVoter+2);                
            }
        }
    }
        
    /************************************/
    /* PHASE: Results */
    /************************************/    
    
    protected List<Player> pIsOk = new ArrayList<Player>();
    public synchronized void proceed(Player player) throws GameException {
        if(this.phase != GamePhase.Results)
            throw new GameException("Wrong phase. You are ok but phase is " + this.phase);
        if(pIsOk.contains(player))
            throw new GameException("You should not say you are ok twice.");
        
        pIsOk.add(player);
        if(pIsOk.size()==this.players.size()){
            this.nextTurn();
        }
    }
    
    public synchronized Player nextTurn() throws GameException {
        if(this.phase != GamePhase.Results)
            throw new GameException("Wrong phase. Trying to next turn but phase is " + this.phase);        
        
        int index = players.indexOf(this.turn);
        if(index==(players.size()-1))
            index = 0;
        else
            index++;
        
        this.turn = players.get(index);
        this.phase = GamePhase.SetPhrase;
        
        // Cleanup
        this.phrase = "";
        this.selectedCardForPlayer.clear();
        this.votes.clear();
        this.pIsOk.clear();
        
        return turn;
    }
    
    /************************************/
    /* Accessors */
    /************************************/
    
    public synchronized Player getWinner(){
        return this.winner;
    }
    
    public synchronized int getPointLimit(){
        return this.pointLimit;
    }
    
    public synchronized GamePhase getPhase(){
        return this.phase;
    }
    
    public synchronized String getPhrase(){
        return this.phrase;
    }
    
    public synchronized Player getTurn(){
        return turn;
    }    
    
    public synchronized List<Card> getCardsForPlayer(String user){
        Player p = getPlayerByName(user);
        return cardsPerPlayer.get(p);
    }
    
    public synchronized int getPointsPerPlayer(String user){
        Player p = getPlayerByName(user);
        return pointsPerPlayer.get(p);        
    }

    public synchronized Player getPlayerByName(String user) {
        for(Player p : players){
            if(p.getName().equals(user))
                return p;
        }
        return null;
    }
    
    public synchronized int getMatchPlayerNum(){
        return match.getNumPlayers();
    }
    
    public synchronized List<Player> getPlayers(){
        // let's return a copy of the list
        return new ArrayList<Player>(this.players);
    }    
    
    public synchronized Card getCardByName(String cname){
        for(Card c : this.allcards){
            if(c.getName().equals(cname))
                return c;
        }
        return null;
    }
    
    public synchronized List<Card> getSelectedCardsRandomly() throws GameException{
        List<Card> result = new ArrayList<Card>();
        if(this.phase!=GamePhase.Vote)
            throw new GameException("It's not the right time to get all the cards on the table");
        
        List<Player> ps = new ArrayList<Player>(this.players);
        Random rand = new Random();
        while(ps.size()>0){
            int sz = ps.size();
            Player p = ps.remove(rand.nextInt(sz));
            Card c = selectedCardForPlayer.get(p);
            result.add(c);
        }
        
        return result;
    }
    
    public synchronized Map<Player,Card> getCardSelection() {
        return this.selectedCardForPlayer;
    }
    
    public synchronized Map<Player,Card> getVotes() throws GameException {
        Map<Player,Card> result = new HashMap<Player,Card> ();
        if(this.phase!=GamePhase.Results && this.phase!=GamePhase.End)
            throw new GameException("It's not the right time to get all votes");
                
        for(Player voter : votes.keySet()){
            Player voted = votes.get(voter);
            Card cardVoted = selectedCardForPlayer.get(voted);
            result.put(voter, cardVoted);
        }
        
        return result;
    }
    
    /************************************/
    /* Utilities */
    /************************************/
    
    public synchronized boolean waitingForPlayers(){
        return players.size() < match.getNumPlayers();
    }    
    
}
