package controllers.instruments.io.cns.firefly;

import java.util.List;
import java.util.stream.Collectors;

import controllers.instruments.io.utils.AbstractOutput;
import controllers.instruments.io.utils.File;
import models.laboratory.experiment.instance.AtomicTransfertMethod;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import validation.ContextValidation;

public class Output extends AbstractOutput {

    private static final play.Logger.ALogger logger = play.Logger.of(Output.class);

    

    @Override
    public File generateFile(Experiment experiment, ContextValidation contextValidation) throws Exception {
        if ("plates-to-plate".equals(experiment.typeCode)) {
            return null;
        }
        List<PlateSampleSheetLine> lines = getPlateSampleSheetLines(experiment, "tube");

        String[][] grid = new String[8][12];
        String plateID = null;

        for (PlateSampleSheetLine line : lines) {
            if (line.rows != null && line.columns != null
                    && line.rows >= 1 && line.rows <= 8
                    && line.columns >= 1 && line.columns <= 12) {

                grid[line.rows - 1][line.columns - 1] = line.bufferVolume != null ? line.bufferVolume.toString() : "";

                if (plateID == null && line.plateID != null) {
                    plateID = line.plateID;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("PlateID,").append(plateID != null ? plateID : "Plate 1").append("\n");
        sb.append("Rows,8\n");
        sb.append("Columns,12\n");

        sb.append(",");
        for (int col = 1; col <= 12; col++) {
            sb.append(col).append(col < 12 ? "," : "\n");
        }

        for (int row = 0; row < 8; row++) {
            sb.append((char) ('A' + row));
            for (int col = 0; col < 12; col++) {
                sb.append(",");
                sb.append(grid[row][col] != null ? grid[row][col] : "");
            }
            sb.append("\n");
        }

        String content = sb.toString();
        logger.debug("Contenu CSV généré :\n" + content);

        return new File(getFileName(experiment) +"_norm"+ ".csv", content);
    }

    private String getFileName(Experiment experiment) {
        return experiment.code.toUpperCase();
    }

    private List<PlateSampleSheetLine> getPlateSampleSheetLines(Experiment experiment, String inputContainerCategory) {
        return experiment.atomicTransfertMethods
                .stream()
                .map(atm -> getPlateSampleSheetLine(atm, inputContainerCategory, experiment))
                .collect(Collectors.toList());
    }

    private PlateSampleSheetLine getPlateSampleSheetLine(AtomicTransfertMethod atm, String inputContainerCategory, Experiment experiment) {
        PlateSampleSheetLine pssl = new PlateSampleSheetLine();

        // Position
        if (atm.line != null && atm.column != null) {
            pssl.rows = convertRowLetterToNumber(atm.line);
            try {
                pssl.columns = Integer.parseInt(atm.column);
            } catch (NumberFormatException e) {
                pssl.columns = null;
            }
        }

        // bufferVolume + plateID
        if (atm.inputContainerUseds != null && !atm.inputContainerUseds.isEmpty()) {
            InputContainerUsed icu = atm.inputContainerUseds.get(0);

            if (icu.experimentProperties != null && icu.experimentProperties.containsKey("bufferVolume")) {
                pssl.bufferVolume = (Double) icu.experimentProperties.get("bufferVolume").value;
            }

            if (icu.locationOnContainerSupport != null) {
                pssl.plateID = icu.locationOnContainerSupport.code;
            }
        }

        return pssl;
    }

    private int convertRowLetterToNumber(String row) {
        return row.toUpperCase().charAt(0) - 'A' + 1; // A=1, B=2, ...
    }
}
