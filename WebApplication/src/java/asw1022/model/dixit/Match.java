package asw1022.model.dixit;

import asw1022.model.BasicObject;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 *
 * @author Roberto Casadei <roberto.casadei12@studio.unibo.it>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Match extends BasicObject {
    
    protected String owner;
    protected int numPlayers;
    
    protected MatchVisibility visibility;
    
    public enum MatchVisibility {
        All, SecretUrl
    }    
    
    public String getOwner() { return this.owner; }
    public void setOwner(String owner) { this.owner = owner; }    
    
    public void setVisiblity(MatchVisibility visibility){ this.visibility = visibility; }
    public MatchVisibility getVisibility(){ return this.visibility; }

    public int getNumPlayers() { return this.numPlayers; }
    public void setNumPlayers(int numPlayers) { this.numPlayers = numPlayers; }    
    
}
