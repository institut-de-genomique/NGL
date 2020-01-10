package workflows;

import static ngl.refactoring.state.ContainerStateNames.A_PF;
import static ngl.refactoring.state.ContainerStateNames.A_QC;
import static ngl.refactoring.state.ContainerStateNames.A_TF;
import static ngl.refactoring.state.ContainerStateNames.A_TM;
import static ngl.refactoring.state.ContainerStateNames.IS;
import static ngl.refactoring.state.ContainerStateNames.IU;
import static ngl.refactoring.state.ContainerStateNames.IW_D;
import static ngl.refactoring.state.ContainerStateNames.IW_E;
import static ngl.refactoring.state.ContainerStateNames.IW_P;
import static ngl.refactoring.state.ContainerStateNames.N;
import static ngl.refactoring.state.ContainerStateNames.UA;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import fr.cea.ig.util.function.CC1;
import fr.cea.ig.util.function.T1;
import models.laboratory.common.instance.State;
import ngl.common.Global;
import workflows.container.ContSupportWorkflows;

/**
 * This belong to some subproject and the test should actually define rules.key
 * and rules.kbasename that should be set on a per test basis. The simpler way
 * is to have this test moved to the proper subproject.
 * 
 * @author vrd
 *
 */
public class ContainerSupportWorkflowTests {

    private CC1<ContSupportWorkflows> af = Global.afSq.cc1()
            .cc1(app -> new T1<>(app.injector().instanceOf(ContSupportWorkflows.class)));

    @Test
    public void validateGetNextStateFromContainersIWD() throws Exception {
        af.accept(worflows -> {
            Set<String> containerStates = new TreeSet<>(Arrays.asList(IU,
                                                                      IW_E,
                                                                      IW_P,
                                                                      A_TM,
                                                                      A_TF,
                                                                      A_PF,
                                                                      A_QC,
                                                                      UA,
                                                                      IS,
                                                                      N,
                                                                      IW_D));
            State s = worflows.getNextStateFromContainerStates("ngl-test", containerStates);
            Assert.assertEquals(IW_D, s.code);
        });
    }

    @Test
    public void validateGetNextStateFromContainersIU() throws Exception {
        af.accept(worflows -> {
            Set<String> containerStates = new TreeSet<>(Arrays.asList(IU,
                                                                      IW_E,
                                                                      IW_P,
                                                                      A_TM,
                                                                      A_TF,
                                                                      A_PF,
                                                                      A_QC,
                                                                      UA,
                                                                      IS));
            State s = worflows.getNextStateFromContainerStates("ngl-test", containerStates);
            Assert.assertEquals(IU, s.code);
        });
    }

    @Test
    public void validateGetNextStateFromContainersIWE() throws Exception {
        af.accept(worflows -> {
            Set<String> containerStates = new TreeSet<>(Arrays.asList(IW_E,
                                                                      IW_P,
                                                                      A_TM,
                                                                      A_TF,
                                                                      A_PF,
                                                                      A_QC,
                                                                      UA,
                                                                      IS));
            State s = worflows.getNextStateFromContainerStates("ngl-test", containerStates);
            Assert.assertEquals(IW_E, s.code);
        });
    }

    @Test
    public void validateGetNextStateFromContainersA() throws Exception {
        af.accept(worflows -> {
            Set<String> containerStates = new TreeSet<>(Arrays.asList(A_TM,
                                                                      A_TF,
                                                                      A_PF,
                                                                      A_QC,
                                                                      UA,
                                                                      IS));
            State s = worflows.getNextStateFromContainerStates("ngl-test", containerStates);
            Assert.assertEquals("A", s.code);
        });
    }

    @Test
    public void validateGetNextStateFromContainersATM() throws Exception {
        af.accept(worflows -> {
            Set<String> containerStates = new TreeSet<>(Arrays.asList(IW_P,
                                                                      A_TM,
                                                                      UA,
                                                                      IS));
            State s = worflows.getNextStateFromContainerStates("ngl-test", containerStates);
            Assert.assertEquals(A_TM, s.code);
        });
    }

    @Test
    public void validateGetNextStateFromContainersAQC() throws Exception {
        af.accept(workflows -> {
            Set<String> containerStates = new TreeSet<>(Arrays.asList(IW_P,
                                                                      A_QC,
                                                                      UA,
                                                                      IS));
            State s = workflows.getNextStateFromContainerStates("ngl-test", containerStates);
            Assert.assertEquals(A_QC, s.code);
        });
    }

    // FIXME : failed: expected:<[A]-P> but was:<[IW]-P>
    // @Test
    public void validateGetNextStateFromContainersAP() throws Exception {
        af.accept(worflows -> {
            Set<String> containerStates = new TreeSet<>(Arrays.asList(IW_P, 
                                                                      A_PF, 
                                                                      UA, 
                                                                      IS));
            State s = worflows.getNextStateFromContainerStates("ngl-test", containerStates);
            Assert.assertEquals(A_PF, s.code);
        });
    }

    @Test
    public void validateGetNextStateFromContainersATF() throws Exception {
        af.accept(workflows -> {
            Set<String> containerStates = new TreeSet<>(Arrays.asList(IW_P,
                                                                      A_TF,
                                                                      UA,
                                                                      IS));
            State s = workflows.getNextStateFromContainerStates("ngl-test", containerStates);
            Assert.assertEquals(A_TF, s.code);
        });
    }

    @Test
    public void validateGetNextStateFromContainersIWP() throws Exception {
        af.accept(workflows -> {
            Set<String> containerStates = new TreeSet<>(Arrays.asList(IW_P, 
                                                                      UA, 
                                                                      IS));
            State s = workflows.getNextStateFromContainerStates("ngl-test", containerStates);
            Assert.assertEquals(IW_P, s.code);
        });
    }

    @Test
    public void validateGetNextStateFromContainersIS() throws Exception {
        af.accept(workflows -> {
            Set<String> containerStates = new TreeSet<>(Arrays.asList(UA, 
                                                                      IS));
            State s = workflows.getNextStateFromContainerStates("ngl-test", containerStates);
            Assert.assertEquals(IS, s.code);
        });
    }

    @Test
    public void validateGetNextStateFromContainersUA() throws Exception {
        af.accept(workflows -> {
            Set<String> containerStates = new TreeSet<>(Arrays.asList(UA));
            State s = workflows.getNextStateFromContainerStates("ngl-test", containerStates);
            Assert.assertEquals(UA, s.code);
        });
    }

}
