/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package asw1022.model;

/**
 *
 * @author Roberto Casadei <roberto.casadei12@studio.unibo.it>
 */
public abstract class BasicObject {
    
    protected String name;
    
    public String getName(){ return this.name; }
    public void setName(String name){ this.name = name; }
 
    @Override
    public boolean equals(Object another){
        if(!(another instanceof BasicObject)) return false;
        BasicObject other = (BasicObject)another;
        return this.getName().equals(other.getName());
    }
    
}
