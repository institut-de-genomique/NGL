package fr.cea.ig.ngl.dao.api.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.protocol.instance.Protocol;

/**
 * Factory pour l'entité "Protocol".
 * 
 * @author asaadi
 */
public class ProtocolFactory {
    /**
	 * Méthode permettant de générer un objet "Protocol" aléatoire.
	 * @param withid
	 * @return Un objet "Protocol" aléatoire.
	 */
     public static Protocol getRandomProtocol(Boolean withid){
       Protocol protocol = new Protocol();
       List<String> experimentTypeCodes = new ArrayList<String>();
      Map<String, PropertyValue> properties =  new HashMap<String, PropertyValue>();
       if(withid == true){
        protocol._id="1234";
       }

       experimentTypeCodes.add("aliquoting");
       protocol.code = UUID.randomUUID().toString();
       protocol.name = UUID.randomUUID().toString();
       protocol.filePath = UUID.randomUUID().toString();
       protocol.categoryCode = "production";
       protocol.active = true;
       protocol.version = UUID.randomUUID().toString();
       protocol.experimentTypeCodes = experimentTypeCodes;
       protocol.properties = null;
       properties.put("cDNAsynthesisType", new PropertySingleValue(Boolean.TRUE));
       protocol.properties = properties; 

       return protocol;
     }


     public static Protocol geProtocolWithParameter (Boolean codeValide, Boolean experimentTypeCodesValid,Boolean categoryCodeValid,Boolean nameValid, Boolean propertieValid){
       Protocol protocol = new Protocol();

      getRandomProtocol(false);

      if(codeValide == false){
        protocol.code = "TestRegex!!@";
      }

      if(experimentTypeCodesValid == false){
        List<String> maListeVide = new ArrayList<>();
        protocol.experimentTypeCodes = maListeVide;
      }

      if(categoryCodeValid == false){
        protocol.categoryCode = "prod";

      }

      if(nameValid == false){
        protocol.name = null;
      }

      /* if(propertieValid == false){
        protocol.properties.put("inexistante", new PropertySingleValue(Boolean.TRUE));
      
      } */

      return protocol;

    }

}
