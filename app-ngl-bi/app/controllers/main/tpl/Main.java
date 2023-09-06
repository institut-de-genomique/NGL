package controllers.main.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.lfw.utils.JavascriptGeneration.Codes;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.Executor;
import fr.cea.ig.ngl.support.NGLJavascript;
import fr.cea.ig.ngl.support.api.CodeLabelAPIHolder;
import fr.cea.ig.ngl.support.api.ProjectAPIHolder;
import fr.cea.ig.ngl.support.api.ResolutionConfigurationAPIHolder;
import fr.cea.ig.ngl.support.api.ValuationCriteriaAPIHolder;
import play.mvc.Result;
import views.html.home ;

public class Main extends NGLController
                implements NGLJavascript,
                           CodeLabelAPIHolder,
                           ProjectAPIHolder,
                           ResolutionConfigurationAPIHolder, 
                           ValuationCriteriaAPIHolder,
                           Executor {


	private final home home;

	@Inject
	public Main(NGLApplication app, home home) {
		super(app);
		this.home = home;
	}

	@Authenticated
	@Historized
	@Authorized.Read
	public Result home() {
		return ok(home.render());
	}
	
	public Result jsCodes() {
		return result(() -> {
			Codes codes = new Codes()
					.add(getCodeLabelAPI().all(),         x -> x.tableName,          x -> x.code, x -> x.label)
					.add(getProjectAPI().all(),   x -> "project",  x -> x.code, x -> x.name)
					.add(getValuationCriteriaAPI().all(), x -> "valuation_criteria", x -> x.code, x -> x.name)
					.add(getResolutionConfigurationAPI().all(), 
							rc -> rc.resolutions,
							x -> "resolution" , x -> x.code , x -> x.name);
			if (getConfig().isCNSInstitute())
				patchTara(codes);
			return codes.asCodeFunction();
		},
		"error while generating codes");
	}
	
	private static void patchTara(Codes codes) {
		String tfc = "taraFilterCode";
		String tdc = "taraDepthCode";
		codes.add(tfc, "0-0.2",    "AACC")
			 .add(tfc, "0-inf",    "AAZZ")
			 .add(tfc, "0.1-0.2",  "BBCC")
			 .add(tfc, "0.2-0.45", "CCEE")
			 .add(tfc, "0.2-1.6",  "CCII")
			 .add(tfc, "0.22-3",   "CCKK")
			 .add(tfc, "0.45-0.8", "EEGG")
			 .add(tfc, "0.45-8",   "EEOO")
			 .add(tfc, "0.8-3",    "GGKK")
			 .add(tfc, "0.8-5",    "GGMM")
			 .add(tfc, "0.8-20",   "GGQQ")
			 .add(tfc, "0.8-180",  "GGSS")
			 .add(tfc, "0.8-200",  "GGRR")
			 .add(tfc, "0.8-inf",  "GGZZ")
			 .add(tfc, "1.6-20",   "IIQQ")
			 .add(tfc, "3-20",     "KKQQ")
			 .add(tfc, "3-inf",    "KKZZ")
			 .add(tfc, "5-20",     "MMQQ")
			 .add(tfc, "20-200",   "QQRR")
			 .add(tfc, "20-180",   "QQSS")
			 .add(tfc, "180-2000", "SSUU")
			 .add(tfc, "180-inf",  "SSZZ")
			 .add(tfc, "300-inf",  "TTZZ")
			 .add(tfc, "pool",     "YYYY")
			 .add(tfc, "inf-inf",  "ZZZZ")

			 .add(tdc, "CTL",                      "CTL")
			 .add(tdc, "Deep Chlorophyl Maximum",  "DCM")
			 .add(tdc, "DCM and OMZ Pool",         "DOP")
			 .add(tdc, "DCM and Surface Pool",     "DSP")
			 .add(tdc, "Meso",                     "MES")
			 .add(tdc, "MixedLayer",               "MXL")
			 .add(tdc, "NightSampling@25mt0",      "NSI")
			 .add(tdc, "NightSampling@25mt24",     "NSJ")
			 .add(tdc, "NightSampling@25mt48",     "NSK")
			 .add(tdc, "OBLIQUE",                  "OBL")
			 .add(tdc, "Oxygen Minimum Zone",      "OMZ")
			 .add(tdc, "PF1",                      "PFA")
			 .add(tdc, "PF2",                      "PFB")
			 .add(tdc, "PF3",                      "PFC")
			 .add(tdc, "PF4",                      "PFD")
			 .add(tdc, "PF5",                      "PFE")
			 .add(tdc, "PF6",                      "PFF")
			 .add(tdc, "P1a",                      "PFG")
			 .add(tdc, "P1b",                      "PFH")
			 .add(tdc, "B2B1",                     "PFI")
			 .add(tdc, "B4B3",                     "PFJ")
			 .add(tdc, "B6B5",                     "PFK")
			 .add(tdc, "B8B7",                     "PFL")
			 .add(tdc, "B10B9",                    "PFM")
			 .add(tdc, "Surface OMZ and DCM Pool", "SOD")
			 .add(tdc, "Surface and OMZ Pool",     "SOP")
			 .add(tdc, "Surface",                  "SUR")
			 .add(tdc, "Sub-MixedLayer@100m",      "SXL")
			 .add(tdc, "Other",                    "OTH")
			 .add(tdc, "DiscreteDepth",            "ZZZ")
			 .add(tdc, "IntegratedDepth",          "IZZ");
	}
	
}
