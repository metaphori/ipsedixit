package asw1022.util;

import java.util.*;

/**
 *
 * @author Roberto Casadei <roberto.casadei12@studio.unibo.it>
 */
public class Utils {
    
    public static <T> List<T> Shuffle(Collection<T> coll){
        List<T> result = new LinkedList<>();
        List<T> input = new ArrayList<>(coll);
        Random rand = new Random();
        for(int k=input.size(); k>0; k--){
           T elem = input.remove(rand.nextInt(k));
           result.add(elem);
        }
        return result;
    }
    
}
