package fr.cea.ig.ngl.dao.readsets;

import java.util.List;

import org.mongojack.DBUpdate.Builder;

import models.laboratory.run.instance.File;

public class FilesDao extends ReadSetsDAO {

    public Builder getBuilder(File value, List<String> fields, String prefix) {
        Builder builder = new Builder();
        try {
            for (String field: fields) {
                String fieldName = (null != prefix)?prefix+"."+field:field;
                builder.set(fieldName, File.class.getField(field).get(value));
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return builder;
    }
}
