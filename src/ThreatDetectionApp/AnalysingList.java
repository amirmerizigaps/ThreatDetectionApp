package ThreatDetectionApp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnalysingList {

    private final List<input> inputs;

    public AnalysingList() {
        this.inputs = new ArrayList<>();
    }

    
    public void addInput(input input) {

      if (input == null) {
           
        	throw new IllegalArgumentException("input can't be null");
        }

        
          for (input existing : inputs) {
        	  
       if (existing.getID() == input.getID()) {  //no idea repeat
            	
                  throw new IllegalArgumentException("Repeatd input ID");
            }
        }

       inputs.add(input);
    }

                                         // supprimer  input par son ID
    public boolean removeInput(int id) {

        return inputs.removeIf(input -> input.getID() == id);
    }

                                     // get all inputs
    public List<input> getInputs() {

        return Collections.unmodifiableList(inputs);
    }

                                   
    public boolean isEmpty() {

        return inputs.isEmpty();
    }

    //total inputs
    public int getSize() {

        return inputs.size();
    }
}