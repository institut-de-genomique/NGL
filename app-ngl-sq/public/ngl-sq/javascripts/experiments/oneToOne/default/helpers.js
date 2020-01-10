angular.module('toolsHelpers', [])
.factory('helpers', ['$parse',
    function($parse){

	var constructor =  {
			
			computeQuantity : function(experiment){
				experiment.atomicTransfertMethods.forEach(function(atm){
	
					var getter = $parse("outputContainerUseds[0].quantity");
					var outputQuantity = getter(atm);
	
	
					var compute = {
							inputConc : $parse("inputContainerUseds[0].concentration")(atm),
							outputConc : $parse("outputContainerUseds[0].concentration")(atm),
							outputVol : $parse("outputContainerUseds[0].volume")(atm),
	
							isReady:function(){
								return (this.inputConc && this.inputConc.value
										&& this.outputConc && this.outputConc.value
										&& this.outputVol && this.outputVol.value);
							}
					};
					if(compute.isReady()){
						if($parse("(outputConc.unit ===  inputConc.unit)")(compute)){
							var result = $parse("outputVol.value  * outputConc.value ")(compute);
							console.log("result = "+result);
							if(angular.isNumber(result) && !isNaN(result)){
								outputQuantity = {};
								outputQuantity.value = Math.round(result*10)/10;
								if($parse("outputConc.unit")(compute) == "nM"){
									outputQuantity.unit = "fmol";	
								}else if ($parse("outputConc.unit")(compute) == "ng/µl"){
									outputQuantity.unit = "ng";
								}else{
									console.log("Unité "+outputQuantity.unit+" non gérée!");
								}
							}else{
								outputQuantity = undefined;					
							}    
							getter.assign(atm, outputQuantity);
						}else{
							console.log("not ready to compute outputQuantity"+outputQtty.value);
							outputQuantity.value = undefined;
							outputQuantity.unit = undefined;
							getter.assign(atm,outputQuantity);    
						}
	
					}else{
						outputQuantity = undefined;
						getter.assign(atm,outputQuantity);
						console.log("not ready to compute outputQuantity");
					}
				});
			}
		};
	
	return constructor;

}]);