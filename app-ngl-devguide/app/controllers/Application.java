package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.datatableDoc;
import views.html.index;
import views.html.modules;
import views.html.namingRules;
import views.html.services;
import views.html.technologies;

public class Application extends Controller {

	public /*static*/ Result index() {
		return ok(index.render("home"));	       
	}

	public /*static*/ Result namingRules(String submenu) {
		return ok(namingRules.render("namingrules"));	       
	}

	public /*static*/ Result technologies() {
		return ok(technologies.render("technologies"));	       
	}

	public /*static*/ Result modules(String name) {
		return ok(modules.render(name));	       
	}

	public /*static*/ Result services(String name) {
		return ok(services.render(name));	       
	}

	public /*static*/ Result datatable() {
		return ok(datatableDoc.render());	       
	}

	public /*static*/ Result datatableDemo() {
		return ok(datatableDoc.render());	       
	}
}
