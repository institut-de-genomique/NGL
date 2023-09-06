package controllers;

import java.util.ArrayList;
import java.util.List;

import models.datatable.Tube;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.components.datatable.DatatableResponse;

public class Datatable extends Controller {
	
	public /*static*/ Result getExamples(){
		List<Tube> tubes = new ArrayList<>();
		tubes.add(new Tube("test1", "20", "N",   "AX_32"));
		tubes.add(new Tube("test2", "40", "IWP", "AP_67"));
		tubes.add(new Tube("test3", "60", "IWP", "KI_98"));
		return ok(Json.toJson(new DatatableResponse<>(tubes, tubes.size())));
	}
	
	public /*static*/ Result getExamplesPagination(){
		List<Tube> tubes = new ArrayList<>();
		for (int i=0; i<100; i++) {
			tubes.add(new Tube("test"+i, ""+(i*i+i+4), "N",   "AX_"+i));
			tubes.add(new Tube("test"+i, ""+(i*i+i+6), "IWP", "AP_"+i));
			tubes.add(new Tube("test"+i, ""+(i*i+i+2), "IWP", "KI_"+i*10));
		}
		return ok(Json.toJson(new DatatableResponse<>(tubes, tubes.size())));
	}
	
}
