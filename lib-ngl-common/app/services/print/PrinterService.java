package services.print;

import java.io.BufferedOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.parameter.printer.BBP11;
import models.laboratory.parameter.printer.BarcodePosition;
import models.laboratory.printing.Tag;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.utils.ValidationConstants;

@Service
public class PrinterService {

	private static final play.Logger.ALogger logger = play.Logger.of(PrinterService.class);
	
	public void printTags(String printerCode, String barcodePosition, List<Tag> tags, ContextValidation ctxVal){
		logger.debug("print tag");
		BBP11 bbp11 = MongoDBDAO.findByCode(InstanceConstants.PARAMETER_COLL_NAME, BBP11.class, printerCode);
		if (bbp11 != null) {
			BarcodePosition position = findBarcodePosition(bbp11, barcodePosition);
			printTags(bbp11, position, tags, ctxVal);
			
		} else {
			ctxVal.addError("printer", ValidationConstants.ERROR_NOTEXISTS_MSG, printerCode);
		}		
	}

	private BarcodePosition findBarcodePosition(BBP11 bbp11, String barcodePosition) {
		Optional<BarcodePosition> option = bbp11.barcodePositions
					.stream()
					.filter(b -> b.id.equals(barcodePosition)).findFirst();		
		return option.get();
	}
	
	private void printTags(BBP11 bbp11, BarcodePosition position, List<Tag> tags, ContextValidation ctxVal) {
		StringBuffer commands = new StringBuffer();
		commands.append('q').append(position.labelWidth).append("\n");

		if (bbp11.inverseList)
			Collections.reverse(tags) ;
		
		tags.forEach(tag ->{
			addBarCodeCommands(tag, commands, position);
			commands.append("P1\n");
		});
		
		try {
			sendCommands(bbp11, commands.toString());
		} catch (PrintServicesException e) {
			logger.error("error when try to print with "+bbp11.name, e);
			ctxVal.addError("printer", "Error when try to print with "+bbp11.name);
		}
	}

	private void addBarCodeCommands(Tag tag, StringBuffer commandsBuffer, BarcodePosition configuration) {		
		commandsBuffer.append("N\n");		
		commandsBuffer.append(getPrintCommand(configuration, tag)).append('\n');
	}

	private String getPrintCommand(BarcodePosition configuration, Tag label) {
	    StringBuffer printCommand = new StringBuffer();
	    String labelCommand = configuration.labelCommand;
	    String barcodeCommand = configuration.barcodeCommand;
	    //logger.debug("Impression de deux lignes : " + lignes[0] + ", " + lignes[1]);
	   
	    if (configuration.twoDimension) {
		    if (label.barcode.equals(label.label)) label.label = "";
	    	printCommand.append(labelCommand).append(",\"").append(label.label).append("\"\n");
		    printCommand.append(labelCommand.replace("3", "7")).append(",\"").append(label.barcode).append("\"\n");
		    printCommand.append(barcodeCommand).append(",\"").append(label.barcode).append('\"'); 
	    } else {
	    	printCommand.append(labelCommand).append(",\"").append(label.label).append("\"\n");
	    	printCommand.append(barcodeCommand).append(",\"").append(label.barcode).append('\"').toString();
	    }
	    return printCommand.toString();	   	   
	}
	
//	private void sendCommands(BBP11 printer, String commands) throws PrintServicesException {
//
//		if (Logger.isInfoEnabled())
//			Logger.info("Sending\n" + commands + "\n to " + printer.name);
//
//		Socket printerSocket = new Socket();
//
//		try {
//			printerSocket.connect(new InetSocketAddress(printer.ipAdress, printer.port), 1000);
//
//			BufferedOutputStream output = new BufferedOutputStream(printerSocket.getOutputStream());
//			output.write(commands.getBytes());
//			output.write('\n');
//			output.close();
//
//			if (Logger.isInfoEnabled())
//				Logger.info("Done sending commands to "  + printer.name);
//
//		} catch (Exception e) {
//			throw new PrintServicesException("While sending \n" + commands + "\n to "  + printer.name, e);
//		} finally {
//			try {
//				printerSocket.close();
//			} catch (IOException e) {
//				if (Logger.isDebugEnabled())
//					Logger.debug("While closing socket to "  + printer.name, e);
//			}
//		}
//
//	}
	
	private void sendCommands(BBP11 printer, String commands) throws PrintServicesException {
		logger.info("sending\n" + commands + "\n to " + printer.name);
		try (Socket printerSocket = new Socket()) {
			printerSocket.connect(new InetSocketAddress(printer.ipAdress, printer.port), 1000);
			try (BufferedOutputStream output = new BufferedOutputStream(printerSocket.getOutputStream())) {
				output.write(commands.getBytes());
				output.write('\n');
			}
			logger.info("done sending commands to "  + printer.name);
		} catch (Exception e) {
			throw new PrintServicesException("while sending \n" + commands + "\n to "  + printer.name, e);
		}
	}

}
