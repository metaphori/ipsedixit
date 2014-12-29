/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package asw1022.db;

import java.util.List;

/**
 *
 * @author Roberto Casadei <roberto.casadei12@studio.unibo.it>
 */
public interface IDB<T> {
    
    List<T> getItems();
    
    void setItems(List<T> items);
    
}
