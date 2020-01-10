package fr.cea.ig.ngl.test.dao.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import fr.cea.ig.ngl.dao.readsets.FilesAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import fr.cea.ig.ngl.test.dao.api.factory.TestReadsetFactory;
import fr.cea.ig.ngl.test.resource.RApplication;
import fr.cea.ig.ngl.test.resource.RConstant;
import fr.cea.ig.ngl.test.resource.RReadSet;
import fr.cea.ig.test.APIRef;
import fr.cea.ig.test.TestContext;
import fr.cea.ig.util.function.CC3;
import fr.cea.ig.util.function.T;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.File;
import models.laboratory.run.instance.ReadSet;

public class ReadSetFilesAPITest {

    //Tested API
    private static APIRef<FilesAPI> api = APIRef.readsetFile;
    private static APIRef<ReadSetsAPI> readSetApi = APIRef.readset;

    public static final CC3<TestContext, ReadSet, ReadSet> illuminaData = 
//    		RReadSet.createIlluminaRunAndReadSetRWC
    		RApplication.contextResource
    		.nest5(RReadSet::createIlluminaRunAndReadSet)
            .cc3((context, exp, refRun, run, refReadSet, readSet) -> T.t3(context, refReadSet, readSet));

    public static final CC3<TestContext, ReadSet, File> illuminaDataWithFile  = illuminaData
            .cc3((context, refReadSet, readSet) -> {
                File input = TestReadsetFactory.rawFile();
                context.apis().readsetFile().save(readSet, input, RConstant.USER);
                return T.t3(context, context.apis().readset().get(readSet.code), input);   
            });
    
    @Test
    public void illumina_checkObjectExistTest() throws Exception {
        illuminaData.accept((context, refReadSet, readSet) -> {
            assertFalse(api.get().checkObjectExist(readSet.code, ""));
            File input = TestReadsetFactory.rawFile();
            api.get().save(readSet, input, RConstant.USER);
            assertTrue(api.get().checkObjectExist(readSet.code, input.fullname));
        });
    }

    @Test
    public void deleteTest() throws Exception {
        illuminaDataWithFile.accept((context, readSet, file) -> {
            api.get().delete(readSet, file.fullname, RConstant.USER);
            readSet = readSetApi.get().get(readSet.code);
            assertNull(api.get().getSubObject(readSet, file.fullname));
        });
    }

    @Test
    public void deleteByReadSetCodeTest() throws Exception {
        illuminaData.accept((context, refReadSet, readSet) -> {
            api.get().deleteByReadSetCode(readSet);
            assertNull(readSetApi.get().get(readSet.code).files);
        });
    }

    @Test
    public void deleteByRunCodeTest() throws Exception {
        RApplication.contextResource
        .nest5(RReadSet::createIlluminaRunAndReadSet)
        .accept((__, ___, ____, run, refReadSet, readSet) -> {
            api.get().deleteByRunCode(run);
            assertNull(readSetApi.get().get(readSet.code).files);
        });
    }

    @Test
    public void getSubObjectTest() throws Exception {
        illuminaDataWithFile.accept((context, readSet, file) -> {
            File f = api.get().getSubObject(readSet, file.fullname);
            assertNotNull(f);
            assertEquals(file.extension, f.extension);
            assertEquals(file.fullname, f.fullname);
        });
    }

    @Test
    public void getSubObjectsTest() throws Exception {
        illuminaData.accept((context, refReadSet, readSet) -> {
            Collection<File> files = api.get().getSubObjects(readSet);
            assertNull(files);
        });
        illuminaDataWithFile.accept((context, readSet, __) -> {
            Collection<File> files = api.get().getSubObjects(readSet);
            assertNotNull(files);
            assertEquals(1, files.size());
        });
    }

    @Test
    public void saveTest() throws Exception {
        illuminaData.accept((context, refReadSet, readSet) -> {
            assertNull(readSet.files);
            File input = TestReadsetFactory.rawFile();
            File file = api.get().save(readSet, input, RConstant.USER);
            readSet = readSetApi.get().get(readSet.code);
            assertNotNull(readSet.files);
            assertEquals(1, readSet.files.size());
            assertEquals(file.fullname, readSet.files.get(0).fullname);
        });
    }

    @Test
    public void updateTest() throws Exception {
        illuminaDataWithFile.accept((context, readSet, file) -> {
            assertEquals(2, file.properties.keySet().size());
            
            String val = "value";
            String key = "md5";
            file.properties.put(key, new PropertySingleValue(val));
            File newFile = api.get().update(readSet, file, RConstant.USER);
            
            assertEquals(3,   newFile.properties.keySet().size());
//            assertEquals(val, (String)newFile.properties.get(key).value);
            assertEquals(val, newFile.properties.get(key).value);
        });
    }

    @Test
    public void updateFieldsTest() throws Exception {
        illuminaDataWithFile.accept((context, readSet, file) -> {
            String name = "newname.fastq";
            File input       = new File();
            input.fullname   = name;
            input.extension  = file.extension;
            input.properties = file.properties;
            input.typeCode   = file.typeCode;
            File newFile = api.get().update(readSet, input, RConstant.USER, Arrays.asList("fullname"), file.fullname);
            assertEquals(name, newFile.fullname);
            assertNotEquals(file.fullname, newFile.fullname);
            
            readSet = readSetApi.get().get(readSet.code);
            assertEquals(newFile.fullname, readSet.files.get(0).fullname);
        });
    }


}
