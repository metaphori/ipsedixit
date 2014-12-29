package asw1022.servlets;

import asw1022.model.User;
import asw1022.model.dixit.Card;
import asw1022.model.dixit.GameAction;
import asw1022.model.dixit.GameException;
import asw1022.model.dixit.GameExecution;
import asw1022.model.dixit.GamePhase;
import asw1022.model.dixit.Match;
import asw1022.model.dixit.Player;
import asw1022.repositories.CardRepository;
import asw1022.repositories.IRepository;
import asw1022.repositories.MatchRepository;
import asw1022.repositories.UserRepository;
import asw1022.util.xml.ManageXML;
import com.sun.org.apache.xerces.internal.impl.xs.opti.ElementImpl;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sound.sampled.AudioFormat;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.w3c.dom.*;

/**
 *
 * @author Roberto Casadei <roberto.casadei12@studio.unibo.it>
 */
@WebServlet(urlPatterns = {"/chat"}, asyncSupported = true)
public class Dixit extends HttpServlet {

    private HashMap<String, GameExecution> matches = new HashMap<String, GameExecution>(); 
    private HashMap<String, Object> contexts = new HashMap<String, Object>();
    
    private IRepository<Match> mrepo;
    private IRepository<User> urepo;
    private IRepository<Card> crepo;
    
    private List<Card> cards;
    
    private ManageXML mngXML;
    
    Logger logger = Logger.getLogger(Dixit.class.getName());
    
    @Override
    public void init() throws ServletException {
        try {
            ServletContext app = getServletContext();
            String matchDB = app.getRealPath(app.getInitParameter("matchDB"));
            this.mrepo = new MatchRepository(matchDB);
            String userDB = app.getRealPath(app.getInitParameter("userDB"));
            this.urepo = new UserRepository(userDB);
            String cardDB = app.getRealPath(app.getInitParameter("cardDB"));
            this.crepo = new CardRepository(cardDB);     
            
            // We can read all the cards during initialization
            this.cards = crepo.readAll();
            
            this.mngXML = new ManageXML();
            
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error during Dixit Servlet initialization", ex);
        } 
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        InputStream is = request.getInputStream();
        response.setContentType("text/xml; charset=UTF-8");

        try {
            Document data = null;
            synchronized(this){
                 data = mngXML.parse(is);
            }
            operations(data, request, response, mngXML);
        } catch (Exception ex) { 
            logger.severe("Eccezione: " + ex.getMessage() + " \n");
        }
    }

    private void operations(Document data, 
            HttpServletRequest request, 
            HttpServletResponse response, 
            ManageXML mngXML) throws Exception {
        
        request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
        
        Element root = data.getDocumentElement(); //name of operation is message root
        GameAction operation = getOperationFromString(root.getTagName());
        logger.info("Received post msg " + root.getTagName());
        Document answer = null;
        OutputStream os;
        switch (operation) {
            case JoinGame:
                answer = JoinGame(root, request);
                break;
            case GivePhrase:
                answer = SetPhrase(root, request);
                break;
            case SelectCard:
                answer = SelectCard(root, request);
                break;
            case VoteCard:
                answer = VoteCard(root, request);
                break;
            case Ok:
                answer = Proceed(root, request);
                break;
            case None:
                answer = GetUpdate(root, request);
                break;
        }
        
        if(answer!=null){
            os = response.getOutputStream();
            mngXML.transform(os, answer);
            os.close();      
        }
    }
    
    /*******************************************/
    /* Operation: JoinGame */
    /*******************************************/
    public Document JoinGame(Element root, HttpServletRequest request) throws Exception{
        HttpSession session = request.getSession();
        
        // 0) Get data from XML message
        NodeList children = root.getChildNodes();
        Node usernode = ((Node) children.item(0));
        Node matchnamenode = ((Node) children.item(1));
        String user = usernode.getTextContent();
        String matchname = matchnamenode.getTextContent();
        List<Player> mates = null;
        
        // Check if provided username matches with the one on the session
        if(!user.equals(session.getAttribute("username"))){
            throw new Exception("Provided user is different from session.");
        }
        
        synchronized (this) {
            // 1) The first player that joins also creates the match
            GameExecution ge = matches.get(matchname);
            if(ge==null){
                // A match context has to be created
                Match match = mrepo.getByName(matchname);
                GameExecution newge = new GameExecution(match, this.cards);
                matches.put(matchname, newge);
                ge = newge;
                logger.info("Created a new game execution for match " + matchname);
            }
            
            // 2) Create a new context for the user
            contexts.put(user, new LinkedList<Document>());             
            
            // 3) Check if the player has already joined the match
            // If so, provide him the current state of the game, AND RETURN.
            Player player = ge.getPlayerByName(user);
            if(player!=null){
                logger.info("Player " + user + " has already joined the match " + matchname);
                Document answer = mngXML.newDocument("Ok");
                if(!ge.waitingForPlayers()){
                    logger.info("Passing current game status");
                    this.AddGameInfoSubtree(answer, ge, user);
                }
                return answer;
            }
            
            // 4a) The player must be added to the game
            logger.info("Adding new player " + user + " to the match " + matchname);             
            
            // 4b) Let's check first if more players can join the game
            // I.e., if we need more players, let's add the player to the match
            if(ge.waitingForPlayers()){
                // 4c) Add the player to the game
                Player newplayer = new Player();
                newplayer.setName(user);                
                ge.addPlayer(newplayer);
                
                // 4d) Let's check if, after this addition, we are done
                // If so, let's notify all the players and provide them the game status.
                boolean waitingForPlayers = ge.waitingForPlayers();
                Document answer = mngXML.newDocument("Ok");
                if(!waitingForPlayers){
                    // Let's include the game status info to this very response...
                    //this.AddGameInfoSubtree(answer, ge, user);
                
                    // .. and also to all the other players
                    // (they will be notified asynchronously as soon as they wants update)
                    for (String destUser : contexts.keySet()) {
                        /*if (destUser.equals(user)) {
                            continue;
                        }*/
                        Document data = mngXML.newDocument("Update");
                        this.AddGameInfoSubtree(data, ge, destUser);
                        
                        Object value = contexts.get(destUser);
                        logger.info("Communicate " + destUser + " that " + user + 
                                " has joined and we are done. The game can start.");
                        if (value instanceof AsyncContext) {
                            OutputStream aos = ((AsyncContext) value).getResponse().getOutputStream();
                            mngXML.transform(aos, data);
                            aos.close();
                            ((AsyncContext) value).complete();
                            contexts.put(destUser, new LinkedList<Document>());
                        } else {
                            ((LinkedList<Document>) value).addLast(data);
                        }
                    } // end for(contexts)
                }

                return answer;
            } // end if(waitingforplayers)
            else {
                return null;
            }
        } // end synchronized(this)
        
    }
    
    /*******************************************/
    /* Operation: JoinGame */
    /*******************************************/
    public Document SetPhrase(Element root, HttpServletRequest request) throws Exception{
        HttpSession session = request.getSession();
        
        // 0) Get data from XML message
        NodeList children = root.getChildNodes();
        Node usernode = ((Node) children.item(0));
        Node matchnamenode = ((Node) children.item(1));
        Node phrasenode = ((Node) children.item(2));
        String user = usernode.getTextContent();
        String matchname = matchnamenode.getTextContent();
        String phrasetxt = phrasenode.getTextContent();
        
        // Check if provided username matches with the one on the session
        if(!user.equals(session.getAttribute("username"))){
            throw new Exception("Provided user is different from session.");
        }
        
        synchronized (this) {
            // Get match
            GameExecution ge = matches.get(matchname);
            
            // Get user
            Player player = ge.getPlayerByName(user);
            
            // Set phrase
            ge.setPhrase(player, phrasetxt);
 
            Document answer = mngXML.newDocument("Ok");

            // Let's include the game status info to this very response...
            //this.AddGameInfoSubtree(answer, ge, user);

            // .. and also to all the other players
            // (they will be notified asynchronously as soon as they wants update)

            for (String destUser : contexts.keySet()) {
                /*
                if (destUser.equals(user)) {
                    continue;
                }
                */
                
                Document data = mngXML.newDocument("Update");
                this.AddGameInfoSubtree(data, ge, destUser);
            
                Object value = contexts.get(destUser);
                logger.info("Communicate " + destUser + " that " + user
                        + " has set the phrase and the game can proceed.");
                if (value instanceof AsyncContext) {
                    OutputStream aos = ((AsyncContext) value).getResponse().getOutputStream();
                    mngXML.transform(aos, data);
                    aos.close();
                    ((AsyncContext) value).complete();
                        List<Document> docsQueue = new LinkedList<Document>();
                        docsQueue.add(data);
                        contexts.put(destUser, docsQueue);
                } else {
                    ((LinkedList<Document>) value).addLast(data);
                }
            } // end for(contexts)

            return answer;
        } // end synchronized(this)
        
    }    
    
    /*******************************************/
    /* Operation: SelectCard */
    /*******************************************/
    public Document SelectCard(Element root, HttpServletRequest request) throws Exception{
        HttpSession session = request.getSession();
        
        // 0) Get data from XML message
        NodeList children = root.getChildNodes();
        Node usernode = ((Node) children.item(0));
        Node matchnamenode = ((Node) children.item(1));
        Node cardnode = ((Node) children.item(2));
        String user = usernode.getTextContent();
        String matchname = matchnamenode.getTextContent();
        String cardName = cardnode.getTextContent();

        // Check if provided username matches with the one on the session
        if (!user.equals(session.getAttribute("username"))) {
            throw new Exception("Provided user is different from session.");
        }

        synchronized (this) {
            GameExecution ge = matches.get(matchname);
            Player player = ge.getPlayerByName(user);

            ge.selectCard(player, cardName);

            Document answer = mngXML.newDocument("Ok");
            
            // When all the players have selected a card, the game switch phase to "vote"
            if (ge.getPhase() == GamePhase.Vote) {
                // Let's include the game status info to this very response...
                //this.AddGameInfoSubtree(answer, ge, user);

                    // .. and also to all the other players
                // (they will be notified asynchronously as soon as they wants update)

                for (String destUser : contexts.keySet()) {
                    /*if (destUser.equals(user)) {
                        continue;
                    }*/
                    
                    Document data = mngXML.newDocument("Update");
                    this.AddGameInfoSubtree(data, ge, destUser); 
                
                    Object value = contexts.get(destUser);
                    logger.info("Communicate " + destUser + " that " + user
                            + " has selected a card and we are done. We can vote.");
                    if (value instanceof AsyncContext) {
                        OutputStream aos = ((AsyncContext) value).getResponse().getOutputStream();
                        mngXML.transform(aos, data);
                        aos.close();
                        ((AsyncContext) value).complete();
                        contexts.put(destUser, new LinkedList<Document>());
                    } else {
                        ((LinkedList<Document>) value).addLast(data);
                    }
                } // end for(contexts)
            } // end if(phase="vote")
            return answer;
        } // end synchronized(this)
        
    }

    /*******************************************/
    /* Operation: VoteCard */
    /*******************************************/
    public Document VoteCard(Element root, HttpServletRequest request) throws Exception{
        HttpSession session = request.getSession();
        
        // 0) Get data from XML message
        NodeList children = root.getChildNodes();
        Node usernode = ((Node) children.item(0));
        Node matchnamenode = ((Node) children.item(1));
        Node cardnode = ((Node) children.item(2));
        String user = usernode.getTextContent();
        String matchname = matchnamenode.getTextContent();
        String cardName = cardnode.getTextContent();

        // Check if provided username matches with the one on the session
        if (!user.equals(session.getAttribute("username"))) {
            throw new Exception("Provided user is different from session.");
        }

        synchronized (this) {
            GameExecution ge = matches.get(matchname);
            Player player = ge.getPlayerByName(user);

            ge.vote(player, cardName);

            Document answer = mngXML.newDocument("Ok");
            
            // When all the players have voted a card, the game switch phase to "results"
            if (ge.getPhase() == GamePhase.Results
                    || ge.getPhase() == GamePhase.End) {
                // Let's include the game status info to this very response...
                //this.AddGameInfoSubtree(answer, ge, user);

                    // .. and also to all the other players
                // (they will be notified asynchronously as soon as they wants update)

                for (String destUser : contexts.keySet()) {
                    /*if (destUser.equals(user)) {
                        continue;
                    }*/
                    
                    Document data = mngXML.newDocument("Update");
                    this.AddGameInfoSubtree(data, ge, destUser); 
                
                    Object value = contexts.get(destUser);
                    logger.info("Communicate " + destUser + " that " + user
                            + " has voted a card and we are done. We can vote.");
                    if (value instanceof AsyncContext) {
                        OutputStream aos = ((AsyncContext) value).getResponse().getOutputStream();
                        mngXML.transform(aos, data);
                        aos.close();
                        ((AsyncContext) value).complete();
                        contexts.put(destUser, new LinkedList<Document>());
                    } else {
                        ((LinkedList<Document>) value).addLast(data);
                    }
                } // end for(contexts)
            } // end if(phase="vote")
            return answer;
        } // end synchronized(this)
        
    }    
    
    /*******************************************/
    /* Operation: Proceed */
    /*******************************************/
    public Document Proceed(Element root, HttpServletRequest request) throws Exception{
        HttpSession session = request.getSession();
        
        // 0) Get data from XML message
        NodeList children = root.getChildNodes();
        Node usernode = ((Node) children.item(0));
        Node matchnamenode = ((Node) children.item(1));
        String user = usernode.getTextContent();
        String matchname = matchnamenode.getTextContent();

        // Check if provided username matches with the one on the session
        if (!user.equals(session.getAttribute("username"))) {
            throw new Exception("Provided user is different from session.");
        }

        synchronized (this) {
            GameExecution ge = matches.get(matchname);
            Player player = ge.getPlayerByName(user);

            logger.info(player.getName() + " is done and wants to proceed.");
            
            ge.proceed(player);

            Document answer = mngXML.newDocument("Ok");
            
            // When all the players have voted a card, the game switch phase to "results"
            if (ge.getPhase() == GamePhase.SetPhrase) {
                // Let's include the game status info to this very response...
                //this.AddGameInfoSubtree(answer, ge, user);

                    // .. and also to all the other players
                // (they will be notified asynchronously as soon as they wants update)

                for (String destUser : contexts.keySet()) {
                    /*if (destUser.equals(user)) {
                        continue;
                    }*/
                    
                    logger.info("Keeping a note for " + destUser + " that we can proceed.");
                    
                    Document data = mngXML.newDocument("Update");
                    this.AddGameInfoSubtree(data, ge, destUser); 
                
                    Object value = contexts.get(destUser);
                    if (value instanceof AsyncContext) {
                        OutputStream aos = ((AsyncContext) value).getResponse().getOutputStream();
                        mngXML.transform(aos, data);
                        aos.close();
                        ((AsyncContext) value).complete();
                        List<Document> docsQueue = new LinkedList<Document>();
                        docsQueue.add(data);
                        contexts.put(destUser, docsQueue);
                    } else {
                        ((LinkedList<Document>) value).addLast(data);
                    }
                } // end for(contexts)
            } // end if(phase="vote")
            return answer;
        } // end synchronized(this)
        
    }    
        
    
    /*******************************************/
    /* Operation: GetUpdate */
    /*******************************************/    
    public Document GetUpdate(Element root, HttpServletRequest request){
        String user = (String) request.getSession().getAttribute("username");
        logger.info("User " + user + " wants to receive updates");

        boolean async;
        synchronized (this) {
            Object obj = contexts.get(user);

            LinkedList<Document> list = (LinkedList<Document>)contexts.get(user);
            if (async = list.isEmpty()) {
                AsyncContext asyncContext = request.startAsync();
                asyncContext.setTimeout(10 * 1000);
                asyncContext.addListener(new AsyncAdapter() {
                    @Override
                    public void onTimeout(AsyncEvent e) {
                        try {
                            ManageXML mngXML = new ManageXML();

                            AsyncContext asyncContext = e.getAsyncContext();
                            HttpServletRequest reqAsync = (HttpServletRequest) asyncContext.getRequest();
                            String user = (String) reqAsync.getSession().getAttribute("username");
                            System.out.println("timeout event launched for: " + user);

                            Document answer = mngXML.newDocument("Timeout");
                            boolean confirm;
                            synchronized (Dixit.this) {
                                if (confirm = (contexts.get(user) instanceof AsyncContext)) {
                                    contexts.put(user, new LinkedList<Document>());
                                }
                            }
                            if (confirm) {
                                OutputStream tos = asyncContext.getResponse().getOutputStream();
                                mngXML.transform(tos, answer);
                                tos.close();
                                asyncContext.complete();
                            }
                        } catch (Exception ex) {
                            System.out.println(ex);
                        }
                    }
                });
                contexts.put(user, asyncContext);
            } else {
                return list.removeFirst();
            }
        }    
        
        return null;
    }
    
    private synchronized void AddGameInfoSubtree(Document doc, GameExecution ge, String currUser) throws Exception {
        Element root = doc.getDocumentElement();
        
        // Add cards to the response
        Element cards = doc.createElement("YourCards");
        for (Card c : ge.getCardsForPlayer(currUser)) {
            Element card = doc.createElement("Card");
            card.setAttribute("cardId", c.getName());
            card.setAttribute("cardTitle", c.getTitle());
            card.setAttribute("cardUrl", c.getUrl());
            cards.appendChild(card);
        }    
        
        Element gameInfo = doc.createElement("GameInfo");
        gameInfo.setAttribute("phase", ge.getPhase().toString());
        gameInfo.setAttribute("turn", ge.getTurn().getName());
        Element playersElem = doc.createElement("Players");
        for(Player p : ge.getPlayers()){
            Element playerElem = doc.createElement("Player");
            playerElem.setAttribute("points", ""+ge.getPointsPerPlayer(p.getName()));
            playerElem.appendChild(doc.createTextNode(p.getName()));
            playersElem.appendChild(playerElem);
        }
        gameInfo.appendChild(playersElem);
        gameInfo.appendChild(cards); 
        
        String phrase = ge.getPhrase();
        if(phrase!=null){
            Element phraseElem = doc.createElement("Phrase");
            phraseElem.appendChild(doc.createTextNode(phrase));
            gameInfo.appendChild(phraseElem);
        }
        
        if(ge.getPhase()==GamePhase.Vote){
            logger.info("Returning table cards");
            List<Card> selcards = ge.getSelectedCardsRandomly();
            if(selcards!=null && selcards.size()>0){
                logger.info("Table cards retrieved");
                Element selcardsElem = doc.createElement("CardsOnTable");
                for(Card c : selcards){
                    Element card = doc.createElement("Card");
                    card.setAttribute("cardId", c.getName());
                    card.setAttribute("cardTitle", c.getTitle());
                    card.setAttribute("cardUrl", c.getUrl());
                    selcardsElem.appendChild(card);
                }
                gameInfo.appendChild(selcardsElem);
            }
        }
        
        if(ge.getPhase()==GamePhase.Results 
                || ge.getPhase()==GamePhase.End){
            Element votesElem = doc.createElement("Votes");
            Element selectionElem = doc.createElement("UncoveredCards");
            
            Map<Player,Card> selection = ge.getCardSelection();
            Map<Player,Card> votes = ge.getVotes();
            
            for(Player chooser : selection.keySet()){
                Card selectedCard = selection.get(chooser);
                Element selElem = doc.createElement("Selection");
                selElem.setAttribute("by", chooser.getName());
                selElem.setAttribute("card", selectedCard.getName());
                selectionElem.appendChild(selElem);                
            }
            gameInfo.appendChild(selectionElem);
            
            for(Player voter : votes.keySet()){
                Card votedCard = votes.get(voter);
                Element voteElem = doc.createElement("Vote");
                voteElem.setAttribute("by", voter.getName());
                voteElem.setAttribute("toCard", votedCard.getName());
                votesElem.appendChild(voteElem);
            }
            gameInfo.appendChild(votesElem);
        }
        
        if(ge.getPhase()==GamePhase.End){
            gameInfo.setAttribute("winner", ge.getWinner().getName());
        }
        
        root.appendChild(gameInfo);
    }    
    
    public GameAction getOperationFromString(String str){
        if(str.equals("join")) return GameAction.JoinGame;
        else if(str.equals("setPhrase")) return GameAction.GivePhrase;
        else if(str.equals("selectCard")) return GameAction.SelectCard;
        else if(str.equals("voteCard")) return GameAction.VoteCard;
        else if(str.equals("ok")) return GameAction.Ok;        
        else return GameAction.None;
    }
}
