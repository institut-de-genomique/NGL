/*******************************************************************************
 * Copyright CEA, DSV/IG/GEN/LABGEM, 91000 Evry, France. contributor(s) : Francois LE FEVRE (Jul 12, 2011)
 * e-mail of the contributor(s) flefevre at/@ genoscope.cns.fr
 * Commercial use prohibited without an agreement with CEA
 * Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 ******************************************************************************/
package fr.cea.ig.auto.submission;

import org.kohsuke.args4j.Option;

import fr.genoscope.lis.devsi.birds.api.client.BirdsLineCommandsOptions;


public class BirdsCommandsOptions extends BirdsLineCommandsOptions{
	
	@Option(name="-n", aliases = {"--new"}, usage="new for test")
    private boolean isNew;
    
    public String display(){
    	String display = super.display();
    	display = display.concat("new="+isNew+"\n");
    	return display;
    }

    public boolean isNew() {
		return isNew;
	}

	public void setIsNew(boolean isNew) {
		this.isNew = isNew;
	}
}
