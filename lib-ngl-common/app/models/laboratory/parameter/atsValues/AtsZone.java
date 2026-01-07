package models.laboratory.parameter.atsValues;

import java.util.List;

import models.laboratory.parameter.Parameter;

public class AtsZone extends Parameter {

    public List<String> values;

    protected AtsZone() {
        super("value-atlasea-zone");
        
    }
    
}