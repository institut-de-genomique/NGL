//creation 25/06/2018 pour gerer les plaques d'index CNRGH.
"use strict"; // ajout et du coup necessite var tags=[]  au lieu de tags[]   !!! 
// 28/01/2022 code générique déplacé vers services.js/tagService. Ne restent ici que les populateIndex qui sont spécifiques CNG (le CNS a les siennes)
angular.module('tools',[]).factory('tagPlates', ['$parse', '$filter','$http',
                                         function($parse,   $filter,  $http){
		/* l'indice dans le tableau tags[] correspond a l'ordre "colonne d'abord" dans la plaque
		   la disposition physique d'une plaque 96 est:
		                 1  2  3  4  5  6  7  8  9  10  11  12
		              -----------------------------------------
		            A | 01 09 17       41 49                89
		            B | 02 10 ..       42 50                90
		            C | .. ..          .. ..                ..
		            D | 
		            E |
		            F |
		            G |
		            H | 08 16          48  56                96
		            
		 c'est le code des index qu'il faut mettre ici exemple:  AglSSXT-01(name) / aglSSXT-01(code) 
		 TODO ?? dans les fonctions populate_XXX: remplacer quand c'est possible par des  boucles for imbriquées => voir CNS/dna-illumina-indexed-library-prep
		      l'affichage exhaustif sans passer par un algo est visuellement plus parlant...
        */
	
		var factory = {
				// ajoutée pour NGL-3515
				// A utiliser quand les index contiennent la position dans leur nom
				populateIndex_PlateNbcol:function(prefix, nbcol){
				// pour une plaque complete 96: nbcol=12; pour une demi plaque (48 index) nbcol=6
					var tags=[];
					var lines = ["A","B","C","D","E","F","G","H"];

					for(var i = 1 ; i <= nbcol; i++){
						for (var j=0; j < lines.length; j++){
							var line = lines[j];
							var tagCode = null;
							if (i < 10){ tagCode = prefix+line+"0"+i;} 
							else {       tagCode = prefix+line+i;}
							console.log("adding tagcode "+tagCode);
							tags.push(tagCode);
						}
					}
					return tags;
				},
				// 14/04/2022
				// A utiliser pour plaques avec indexes dans l'ordre, par colonne, sans exceptions, sans trous !!!!!!!!
				// attention certains prefixes nécessitent 00 en padding  ex UDP001=> UDP383.... maxpad="00"
				//                    alors que d'autres uniquement 0     ex Agl-01=> Agl-96.... maxpax="0"
				populateIndex_PlateRange:function(prefix, startIndex, endIndex, maxpad){
					var tags=[];
					for(var i = startIndex ; i <= endIndex; i++){
						var tagCode = null;
						if      (i < 10) { if (maxpad="00") {tagCode = prefix+"00"+i;} else {tagCode = prefix+"0"+i;} } 
						else if (i < 100){ if (maxpad="00") {tagCode = prefix+"0"+i;}  else {tagCode = prefix+i;} }
						else {       tagCode = prefix+i;}
						//console.log("adding tagcode "+tagCode);
						tags.push(tagCode);
					}
					return tags;
				},
				// DAP TruSeq HT == RAP TruSeq HT  (96 couple d'index DUAL)
				// remplacer par 2 boucles for imbriquées ???
				// NOTE: pour des raisons historiques les codes sont en majuscules...
				populateIndex_XapTruSeqHT:function(){
					var tags=[];
					//           A              B            C            D            E            F            G            H
					tags.push("D701-D501", "D701-D502", "D701-D503", "D701-D504", "D701-D505", "D701-D506", "D701-D507", "D701-D508"); //colonne 1
					tags.push("D702-D501", "D702-D502", "D702-D503", "D702-D504", "D702-D505", "D702-D506", "D702-D507", "D702-D508"); //colonne 2
					tags.push("D703-D501", "D703-D502", "D703-D503", "D703-D504", "D703-D505", "D703-D506", "D703-D507", "D703-D508"); //colonne 3
					tags.push("D704-D501", "D704-D502", "D704-D503", "D704-D504", "D704-D505", "D704-D506", "D704-D507", "D704-D508"); //colonne 4
					tags.push("D705-D501", "D705-D502", "D705-D503", "D705-D504", "D705-D505", "D705-D506", "D705-D507", "D705-D508"); //colonne 5
					tags.push("D706-D501", "D706-D502", "D706-D503", "D706-D504", "D706-D505", "D706-D506", "D706-D507", "D706-D508"); //colonne 6
					tags.push("D707-D501", "D707-D502", "D707-D503", "D707-D504", "D707-D505", "D707-D506", "D707-D507", "D707-D508"); //colonne 7
					tags.push("D708-D501", "D708-D502", "D708-D503", "D708-D504", "D708-D505", "D708-D506", "D708-D507", "D708-D508"); //colonne 8
					tags.push("D709-D501", "D709-D502", "D709-D503", "D709-D504", "D709-D505", "D709-D506", "D709-D507", "D709-D508"); //colonne 9
					tags.push("D710-D501", "D710-D502", "D710-D503", "D710-D504", "D710-D505", "D710-D506", "D710-D507", "D710-D508"); //colonne 10
					tags.push("D711-D501", "D711-D502", "D711-D503", "D711-D504", "D711-D505", "D711-D506", "D711-D507", "D711-D508"); //colonne 11
					tags.push("D712-D501", "D712-D502", "D712-D503", "D712-D504", "D712-D505", "D712-D506", "D712-D507", "D712-D508"); //colonne 12
					return tags;
				},
				
				// IDT-ILMN TruSeq DNA UD Indexes (96 couples d'index DUAL)
				// remplacer par 2 boucles for imbriquées ???
				populateIndex_IdtTruSeq96:function(){
					var tags=[];
					//	                 A                       B                       C                       D                       E                       F                       G                       H
					tags.push("udi0001_i7-udi0001_i5","udi0002_i7-udi0002_i5","udi0003_i7-udi0003_i5","udi0004_i7-udi0004_i5","udi0005_i7-udi0005_i5","udi0006_i7-udi0006_i5","udi0007_i7-udi0007_i5","udi0008_i7-udi0008_i5"); //colonne 1
					tags.push("udi0009_i7-udi0009_i5","udi0010_i7-udi0010_i5","udi0011_i7-udi0011_i5","udi0012_i7-udi0012_i5","udi0013_i7-udi0013_i5","udi0014_i7-udi0014_i5","udi0015_i7-udi0015_i5","udi0016_i7-udi0016_i5"); //colonne 2
					tags.push("udi0017_i7-udi0017_i5","udi0018_i7-udi0018_i5","udi0019_i7-udi0019_i5","udi0020_i7-udi0020_i5","udi0021_i7-udi0021_i5","udi0022_i7-udi0022_i5","udi0023_i7-udi0023_i5","udi0024_i7-udi0024_i5"); //colonne 3
					tags.push("udi0025_i7-udi0025_i5","udi0026_i7-udi0026_i5","udi0027_i7-udi0027_i5","udi0028_i7-udi0028_i5","udi0029_i7-udi0029_i5","udi0030_i7-udi0030_i5","udi0031_i7-udi0031_i5","udi0032_i7-udi0032_i5"); //colonne 4
					tags.push("udi0033_i7-udi0033_i5","udi0034_i7-udi0034_i5","udi0035_i7-udi0035_i5","udi0036_i7-udi0036_i5","udi0037_i7-udi0037_i5","udi0038_i7-udi0038_i5","udi0039_i7-udi0039_i5","udi0040_i7-udi0040_i5"); //colonne 5
					tags.push("udi0041_i7-udi0041_i5","udi0042_i7-udi0042_i5","udi0043_i7-udi0043_i5","udi0044_i7-udi0044_i5","udi0045_i7-udi0045_i5","udi0046_i7-udi0046_i5","udi0047_i7-udi0047_i5","udi0048_i7-udi0048_i5"); //colonne 6
					tags.push("udi0049_i7-udi0049_i5","udi0050_i7-udi0050_i5","udi0051_i7-udi0051_i5","udi0052_i7-udi0052_i5","udi0053_i7-udi0053_i5","udi0054_i7-udi0054_i5","udi0055_i7-udi0055_i5","udi0056_i7-udi0056_i5"); //colonne 7
					tags.push("udi0057_i7-udi0057_i5","udi0058_i7-udi0058_i5","udi0059_i7-udi0059_i5","udi0060_i7-udi0060_i5","udi0061_i7-udi0061_i5","udi0062_i7-udi0062_i5","udi0063_i7-udi0063_i5","udi0064_i7-udi0064_i5"); //colonne 8
				    tags.push("udi0065_i7-udi0065_i5","udi0066_i7-udi0066_i5","udi0067_i7-udi0067_i5","udi0068_i7-udi0068_i5","udi0069_i7-udi0069_i5","udi0070_i7-udi0070_i5","udi0071_i7-udi0071_i5","udi0072_i7-udi0072_i5"); //colonne 9
					tags.push("udi0073_i7-udi0073_i5","udi0074_i7-udi0074_i5","udi0075_i7-udi0075_i5","udi0076_i7-udi0076_i5","udi0077_i7-udi0077_i5","udi0078_i7-udi0078_i5","udi0079_i7-udi0079_i5","udi0080_i7-udi0080_i5"); //colonne 10
					tags.push("udi0081_i7-udi0081_i5","udi0082_i7-udi0082_i5","udi0083_i7-udi0083_i5","udi0084_i7-udi0084_i5","udi0085_i7-udi0085_i5","udi0086_i7-udi0086_i5","udi0087_i7-udi0087_i5","udi0088_i7-udi0088_i5"); //colonne 11
					tags.push("udi0089_i7-udi0089_i5","udi0090_i7-udi0090_i5","udi0091_i7-udi0091_i5","udi0092_i7-udi0092_i5","udi0093_i7-udi0093_i5","udi0094_i7-udi0094_i5","udi0095_i7-udi0095_i5","udi0096_i7-udi0096_i5"); //colonne 12
					return tags;
				},
				// IDT-ILMN TruSeq DNA UD Indexes (96 couples d'index DUAL) !! NGL-4038 Il y a 4 index V2, les 2 types de plaques vont cohabiter; il faut proposer les 2
				// remplacer par 2 boucles for imbriquées ???   !! Dans ce cas il faudra une prévoir une map de remplacement pour les v2 (voir CNS)
				populateIndex_IdtTruSeq96V2:function(){
					var tags=[];
					//	                 A                       B                       C                       D                       E                       F                       G                       H
					tags.push("udi0001_i7-udi0001_i5","udi0002_i7-udi0002_i5","udi0003_i7-udi0003_i5","udi0004_i7-udi0004_i5","udi0005_i7-udi0005_i5","udi0006_i7-udi0006_i5","udi0007_i7-udi0007_i5","udi0008_i7-udi0008_i5"); //colonne 1
					tags.push("udi0009_i7-udi0009_i5","udi0010_i7-udi0010_i5","udi0011_i7-udi0011_i5","udi0012_i7-udi0012_i5","udi0013_i7-udi0013_i5","udi0014_i7-udi0014_i5","udi0015v2_i7-udi0015v2_i5","udi0016v2_i7-udi0016v2_i5"); //colonne 2
					tags.push("udi0017_i7-udi0017_i5","udi0018_i7-udi0018_i5","udi0019_i7-udi0019_i5","udi0020_i7-udi0020_i5","udi0021_i7-udi0021_i5","udi0022_i7-udi0022_i5","udi0023_i7-udi0023_i5","udi0024_i7-udi0024_i5"); //colonne 3
					tags.push("udi0025_i7-udi0025_i5","udi0026_i7-udi0026_i5","udi0027_i7-udi0027_i5","udi0028_i7-udi0028_i5","udi0029_i7-udi0029_i5","udi0030_i7-udi0030_i5","udi0031_i7-udi0031_i5","udi0032_i7-udi0032_i5"); //colonne 4
					tags.push("udi0033_i7-udi0033_i5","udi0034_i7-udi0034_i5","udi0035_i7-udi0035_i5","udi0036_i7-udi0036_i5","udi0037_i7-udi0037_i5","udi0038_i7-udi0038_i5","udi0039_i7-udi0039_i5","udi0040_i7-udi0040_i5"); //colonne 5
					tags.push("udi0041_i7-udi0041_i5","udi0042_i7-udi0042_i5","udi0043_i7-udi0043_i5","udi0044_i7-udi0044_i5","udi0045_i7-udi0045_i5","udi0046_i7-udi0046_i5","udi0047_i7-udi0047_i5","udi0048_i7-udi0048_i5"); //colonne 6
					tags.push("udi0049_i7-udi0049_i5","udi0050_i7-udi0050_i5","udi0051_i7-udi0051_i5","udi0052_i7-udi0052_i5","udi0053_i7-udi0053_i5","udi0054_i7-udi0054_i5","udi0055v2_i7-udi0055v2_i5","udi0056v2_i7-udi0056v2_i5"); //colonne 7
					tags.push("udi0057_i7-udi0057_i5","udi0058_i7-udi0058_i5","udi0059_i7-udi0059_i5","udi0060_i7-udi0060_i5","udi0061_i7-udi0061_i5","udi0062_i7-udi0062_i5","udi0063_i7-udi0063_i5","udi0064_i7-udi0064_i5"); //colonne 8
				    tags.push("udi0065_i7-udi0065_i5","udi0066_i7-udi0066_i5","udi0067_i7-udi0067_i5","udi0068_i7-udi0068_i5","udi0069_i7-udi0069_i5","udi0070_i7-udi0070_i5","udi0071_i7-udi0071_i5","udi0072_i7-udi0072_i5"); //colonne 9
					tags.push("udi0073_i7-udi0073_i5","udi0074_i7-udi0074_i5","udi0075_i7-udi0075_i5","udi0076_i7-udi0076_i5","udi0077_i7-udi0077_i5","udi0078_i7-udi0078_i5","udi0079_i7-udi0079_i5","udi0080_i7-udi0080_i5"); //colonne 10
					tags.push("udi0081_i7-udi0081_i5","udi0082_i7-udi0082_i5","udi0083_i7-udi0083_i5","udi0084_i7-udi0084_i5","udi0085_i7-udi0085_i5","udi0086_i7-udi0086_i5","udi0087_i7-udi0087_i5","udi0088_i7-udi0088_i5"); //colonne 11
					tags.push("udi0089_i7-udi0089_i5","udi0090_i7-udi0090_i5","udi0091_i7-udi0091_i5","udi0092_i7-udi0092_i5","udi0093_i7-udi0093_i5","udi0094_i7-udi0094_i5","udi0095_i7-udi0095_i5","udi0096_i7-udi0096_i5"); //colonne 12
					return tags;
				},
				
				// IDT-ILMN TruSeq DNA UD Indexes (24 couples d'indexes DUAL répétés 4 fois !!!)
				populateIndex_IdtTruSeq24x4:function(){
					var tags=[];
					//	                 A                       B                       C                       D                       E                       F                       G                       H
					tags.push("udi0001_i7-udi0001_i5","udi0002_i7-udi0002_i5","udi0003_i7-udi0003_i5","udi0004_i7-udi0004_i5","udi0005_i7-udi0005_i5","udi0006_i7-udi0006_i5","udi0007_i7-udi0007_i5","udi0008_i7-udi0008_i5"); //colonne 1
					tags.push("udi0009_i7-udi0009_i5","udi0010_i7-udi0010_i5","udi0011_i7-udi0011_i5","udi0012_i7-udi0012_i5","udi0013_i7-udi0013_i5","udi0014_i7-udi0014_i5","udi0015_i7-udi0015_i5","udi0016_i7-udi0016_i5"); //colonne 2
					tags.push("udi0017_i7-udi0017_i5","udi0018_i7-udi0018_i5","udi0019_i7-udi0019_i5","udi0020_i7-udi0020_i5","udi0021_i7-udi0021_i5","udi0022_i7-udi0022_i5","udi0023_i7-udi0023_i5","udi0024_i7-udi0024_i5"); //colonne 3
					tags.push("udi0001_i7-udi0001_i5","udi0002_i7-udi0002_i5","udi0003_i7-udi0003_i5","udi0004_i7-udi0004_i5","udi0005_i7-udi0005_i5","udi0006_i7-udi0006_i5","udi0007_i7-udi0007_i5","udi0008_i7-udi0008_i5"); //colonne 4
					tags.push("udi0009_i7-udi0009_i5","udi0010_i7-udi0010_i5","udi0011_i7-udi0011_i5","udi0012_i7-udi0012_i5","udi0013_i7-udi0013_i5","udi0014_i7-udi0014_i5","udi0015_i7-udi0015_i5","udi0016_i7-udi0016_i5"); //colonne 5
					tags.push("udi0017_i7-udi0017_i5","udi0018_i7-udi0018_i5","udi0019_i7-udi0019_i5","udi0020_i7-udi0020_i5","udi0021_i7-udi0021_i5","udi0022_i7-udi0022_i5","udi0023_i7-udi0023_i5","udi0024_i7-udi0024_i5"); //colonne 6
					tags.push("udi0001_i7-udi0001_i5","udi0002_i7-udi0002_i5","udi0003_i7-udi0003_i5","udi0004_i7-udi0004_i5","udi0005_i7-udi0005_i5","udi0006_i7-udi0006_i5","udi0007_i7-udi0007_i5","udi0008_i7-udi0008_i5"); //colonne 7
					tags.push("udi0009_i7-udi0009_i5","udi0010_i7-udi0010_i5","udi0011_i7-udi0011_i5","udi0012_i7-udi0012_i5","udi0013_i7-udi0013_i5","udi0014_i7-udi0014_i5","udi0015_i7-udi0015_i5","udi0016_i7-udi0016_i5"); //colonne 8
					tags.push("udi0017_i7-udi0017_i5","udi0018_i7-udi0018_i5","udi0019_i7-udi0019_i5","udi0020_i7-udi0020_i5","udi0021_i7-udi0021_i5","udi0022_i7-udi0022_i5","udi0023_i7-udi0023_i5","udi0024_i7-udi0024_i5"); //colonne 9
					tags.push("udi0001_i7-udi0001_i5","udi0002_i7-udi0002_i5","udi0003_i7-udi0003_i5","udi0004_i7-udi0004_i5","udi0005_i7-udi0005_i5","udi0006_i7-udi0006_i5","udi0007_i7-udi0007_i5","udi0008_i7-udi0008_i5"); //colonne 10
					tags.push("udi0009_i7-udi0009_i5","udi0010_i7-udi0010_i5","udi0011_i7-udi0011_i5","udi0012_i7-udi0012_i5","udi0013_i7-udi0013_i5","udi0014_i7-udi0014_i5","udi0015_i7-udi0015_i5","udi0016_i7-udi0016_i5"); //colonne 11
					tags.push("udi0017_i7-udi0017_i5","udi0018_i7-udi0018_i5","udi0019_i7-udi0019_i5","udi0020_i7-udi0020_i5","udi0021_i7-udi0021_i5","udi0022_i7-udi0022_i5","udi0023_i7-udi0023_i5","udi0024_i7-udi0024_i5"); //colonne 12
					return tags;
				},
				
				// NEBNext small RNA plaque 48 (48 index NEB SINGLE)
				// ATTENTION répartition spéciale, pas d'algorithme utilisable !!!!!
				populateIndex_NebNext48:function(){
					var tags=[];
					//            A         B         C         D         E         F         G         H
					tags.push("neb-01", "neb-02", "neb-03", "neb-04", "neb-05", "neb-06", "neb-07", "neb-08"); //colonne 1
					tags.push("neb-09", "neb-10", "neb-11", "neb-12", "neb-02", "neb-03", "neb-04", "neb-05"); //colonne 2
					tags.push("neb-06", "neb-07", "neb-08", "neb-09", "neb-10", "neb-11", "neb-12", "neb-01"); //colonne 3
					tags.push("neb-03", "neb-04", "neb-05", "neb-06", "neb-07", "neb-08", "neb-09", "neb-10"); //colonne 4
					tags.push("neb-11", "neb-12", "neb-01", "neb-02", "neb-04", "neb-05", "neb-06", "neb-07"); //colonne 5
					tags.push("neb-08", "neb-09", "neb-10", "neb-11", "neb-12", "neb-01", "neb-02", "neb-03"); //colonne 6
					return tags;
				},
				
				// QiaSeq miRNA NGS 48 index IL (48 index Qiagen SINGLE)
				populateIndex_QiaSeq48IL:function(){
					var tags=[];
					//			  A         B         C         D         E         F         G         H
					tags.push("qmr-01", "qmr-02", "qmr-03", "qmr-04", "qmr-05", "qmr-06", "qmr-07", "qmr-08"); //colonne 1
					tags.push("qmr-09", "qmr-10", "qmr-11", "qmr-12", "qmr-13", "qmr-14", "qmr-15", "qmr-16"); //colonne 2
					tags.push("qmr-17", "qmr-18", "qmr-19", "qmr-20", "qmr-21", "qmr-22", "qmr-23", "qmr-24"); //colonne 3
					tags.push("qmr-25", "qmr-26", "qmr-27", "qmr-28", "qmr-29", "qmr-30", "qmr-31", "qmr-32"); //colonne 4
					tags.push("qmr-33", "qmr-34", "qmr-35", "qmr-36", "qmr-37", "qmr-38", "qmr-39", "qmr-40"); //colonne 5
					tags.push("qmr-41", "qmr-42", "qmr-43", "qmr-44", "qmr-45", "qmr-46", "qmr-47", "qmr-48"); //colonne 6
					return tags;
					// return this.populateIndex_PlateRange("qmr-",1,48,"0") a tester...
				},
				// NGL-3176 23/11/2020
				// QiaSeq miRNA NGS 96 index IL
				// les codes sont identiques aux noms=> contiennent des majuscules (pas très cohérent  avec QiaSeq48IL ci dessus !!)
				populateIndex_QiaSeq96IL:function(){
					var tags=[];
					//			  A             B            C            D            E            F            G            H
					tags.push("QmiRHT-01", "QmiRHT-02", "QmiRHT-03", "QmiRHT-04", "QmiRHT-05", "QmiRHT-06", "QmiRHT-07", "QmiRHT-08"); //colonne 1
					tags.push("QmiRHT-09", "QmiRHT-10", "QmiRHT-11", "QmiRHT-12", "QmiRHT-13", "QmiRHT-14", "QmiRHT-15", "QmiRHT-16"); //colonne 2
					tags.push("QmiRHT-17", "QmiRHT-18", "QmiRHT-19", "QmiRHT-20", "QmiRHT-21", "QmiRHT-22", "QmiRHT-23", "QmiRHT-24"); //colonne 3
					tags.push("QmiRHT-25", "QmiRHT-26", "QmiRHT-27", "QmiRHT-28", "QmiRHT-29", "QmiRHT-30", "QmiRHT-31", "QmiRHT-32"); //colonne 4
					tags.push("QmiRHT-33", "QmiRHT-34", "QmiRHT-35", "QmiRHT-36", "QmiRHT-37", "QmiRHT-38", "QmiRHT-39", "QmiRHT-40"); //colonne 5
					tags.push("QmiRHT-41", "QmiRHT-42", "QmiRHT-43", "QmiRHT-44", "QmiRHT-45", "QmiRHT-46", "QmiRHT-47", "QmiRHT-48"); //colonne 6
					tags.push("QmiRHT-49", "QmiRHT-50", "QmiRHT-51", "QmiRHT-52", "QmiRHT-53", "QmiRHT-54", "QmiRHT-55", "QmiRHT-56"); //colonne 7
					tags.push("QmiRHT-57", "QmiRHT-58", "QmiRHT-59", "QmiRHT-60", "QmiRHT-61", "QmiRHT-62", "QmiRHT-63", "QmiRHT-64"); //colonne 8
					tags.push("QmiRHT-65", "QmiRHT-66", "QmiRHT-67", "QmiRHT-68", "QmiRHT-69", "QmiRHT-70", "QmiRHT-71", "QmiRHT-72"); //colonne 9
					tags.push("QmiRHT-73", "QmiRHT-74", "QmiRHT-75", "QmiRHT-76", "QmiRHT-77", "QmiRHT-78", "QmiRHT-79", "QmiRHT-80"); //colonne 10
					tags.push("QmiRHT-81", "QmiRHT-82", "QmiRHT-83", "QmiRHT-84", "QmiRHT-85", "QmiRHT-86", "QmiRHT-87", "QmiRHT-88"); //colonne 11
					tags.push("QmiRHT-89", "QmiRHT-90", "QmiRHT-91", "QmiRHT-92", "QmiRHT-93", "QmiRHT-94", "QmiRHT-95", "QmiRHT-96"); //colonne 12
					return tags;
					// return this.populateIndex_PlateRange("QmiRHT-",1,96,"0") a tester...
				},
				
				// NUGEN Ovation Ultralow Methyl-Seq System 1-96  SINGLE
				// remplacer par 2 boucles for imbriquées ???
				populateIndex_Nuo96:function(){
					var tags=[];
					//            A         B         C         D         E         F         G         H
					tags.push("nuo-01", "nuo-02", "nuo-03", "nuo-04", "nuo-05", "nuo-06", "nuo-07", "nuo-08"); //colonne 1
					tags.push("nuo-09", "nuo-10", "nuo-11", "nuo-12", "nuo-13", "nuo-14", "nuo-15", "nuo-16"); //colonne 2
					tags.push("nuo-17", "nuo-18", "nuo-19", "nuo-20", "nuo-21", "nuo-22", "nuo-23", "nuo-24"); //colonne 3
					tags.push("nuo-25", "nuo-26", "nuo-27", "nuo-28", "nuo-29", "nuo-30", "nuo-31", "nuo-32"); //colonne 4
					tags.push("nuo-33", "nuo-34", "nuo-35", "nuo-36", "nuo-37", "nuo-38", "nuo-39", "nuo-40"); //colonne 5
					tags.push("nuo-41", "nuo-42", "nuo-43", "nuo-44", "nuo-45", "nuo-46", "nuo-47", "nuo-48"); //colonne 6
					tags.push("nuo-49", "nuo-50", "nuo-51", "nuo-52", "nuo-53", "nuo-54", "nuo-55", "nuo-56"); //colonne 7
					tags.push("nuo-57", "nuo-58", "nuo-59", "nuo-60", "nuo-61", "nuo-62", "nuo-63", "nuo-64"); //colonne 8
					tags.push("nuo-65", "nuo-66", "nuo-67", "nuo-68", "nuo-69", "nuo-70", "nuo-71", "nuo-72"); //colonne 9
					tags.push("nuo-73", "nuo-74", "nuo-75", "nuo-76", "nuo-77", "nuo-78", "nuo-79", "nuo-80"); //colonne 10
					tags.push("nuo-81", "nuo-82", "nuo-83", "nuo-84", "nuo-85", "nuo-86", "nuo-87", "nuo-88"); //colonne 11
					tags.push("nuo-89", "nuo-90", "nuo-91", "nuo-92", "nuo-93", "nuo-94", "nuo-95", "nuo-96"); //colonne 12
					return tags;
					// return this.populateIndex_PlateRange("nuo-",1,96,"0") a tester..
				},
				
				// Agilent SureSelect [bleue] (96 SINGLE)
				// NGL-1741 correction erreur, decalage sur  H6->H12
				populateIndex_AglSur96:function(){
					var tags=[];
					// !!! au sens de repartition des index !!!! ils sont en ligne et pas en colonne
					//            A             B             C             D             E             F              G            H
					tags.push("aglSSXT-01", "aglSSXT-13", "aglSSXT-25", "aglSSXT-37", "aglSSXT-49", "aglSSXT-61", "aglSSXT-73", "aglSSXT-85"); //colonne 1
					tags.push("aglSSXT-02", "aglSSXT-14", "aglSSXT-26", "aglSSXT-38", "aglSSXT-50", "aglSSXT-62", "aglSSXT-74", "aglSSXT-86"); //colonne 2
					tags.push("aglSSXT-03", "aglSSXT-15", "aglSSXT-27", "aglSSXT-39", "aglSSXT-51", "aglSSXT-63", "aglSSXT-75", "aglSSXT-87"); //colonne 3
					tags.push("aglSSXT-04", "aglSSXT-16", "aglSSXT-28", "aglSSXT-40", "aglSSXT-52", "aglSSXT-64", "aglSSXT-76", "aglSSXT-88"); //colonne 4
					tags.push("aglSSXT-05", "aglSSXT-17", "aglSSXT-29", "aglSSXT-41", "aglSSXT-53", "aglSSXT-65", "aglSSXT-77", "aglSSXT-89"); //colonne 5
					tags.push("aglSSXT-06", "aglSSXT-18", "aglSSXT-30", "aglSSXT-42", "aglSSXT-54", "aglSSXT-66", "aglSSXT-78", "aglSSXT-90"); //colonne 6
					tags.push("aglSSXT-07", "aglSSXT-19", "aglSSXT-31", "aglSSXT-43", "aglSSXT-55", "aglSSXT-67", "aglSSXT-79", "aglSSXT-91"); //colonne 7
					tags.push("aglSSXT-08", "aglSSXT-20", "aglSSXT-32", "aglSSXT-44", "aglSSXT-56", "aglSSXT-68", "aglSSXT-80", "aglSSXT-92"); //colonne 8
					tags.push("aglSSXT-09", "aglSSXT-21", "aglSSXT-33", "aglSSXT-45", "aglSSXT-57", "aglSSXT-69", "aglSSXT-81", "aglSSXT-93"); //colonne 9
					tags.push("aglSSXT-10", "aglSSXT-22", "aglSSXT-34", "aglSSXT-46", "aglSSXT-58", "aglSSXT-70", "aglSSXT-82", "aglSSXT-94"); //colonne 10
					tags.push("aglSSXT-11", "aglSSXT-23", "aglSSXT-35", "aglSSXT-47", "aglSSXT-59", "aglSSXT-71", "aglSSXT-83", "aglSSXT-95"); //colonne 11
					tags.push("aglSSXT-12", "aglSSXT-24", "aglSSXT-36", "aglSSXT-48", "aglSSXT-60", "aglSSXT-72", "aglSSXT-84", "aglSSXT-96"); //colonne 12
					return tags;
				},
				/* 14/06/2023 ...obsoletes et en plus SetA et SetB contenaient des erreurs !!!!!
				populateIndex_IdtNexteraSetA:function(){
					var tags=[];
					//	          A          B          C          D          E           F         G          H
					tags.push("UDP0001", "UDP0002", "UDP0003", "UDP0004", "UDP0005", "UDP0006", "UDP0007", "UDP0008"); //colonne 1
					tags.push("UDP0009", "UDP0010", "UDP0011", "UDP0012", "UDP0013", "UDP0014", "UDP0015", "UDP0016"); //colonne 2
					tags.push("UDP0017", "UDP0018", "UDP0019", "UDP0020", "UDP0021", "UDP0022", "UDP0023", "UDP0024"); //colonne 3
					tags.push("UDP0025", "UDP0026", "UDP0027", "UDP0028", "UDP0029", "UDP0030", "UDP0031", "UDP0032"); //colonne 4
					tags.push("UDP0033", "UDP0034", "UDP0035", "UDP0036", "UDP0037", "UDP0038", "UDP0039", "UDP0040"); //colonne 5
					// BUG vu 14/06/2023: colonne 6 contient 3 erreurs !!!!
					//tags.push("UDP0041", "UDP0042", "UDP0043", "UDP0004", "UDP0047", "UDP0048", "UDP0047", "UDP0048"); //colonne 6
					  tags.push("UDP0041", "UDP0042", "UDP0043", "UDP0044", "UDP0045", "UDP0046", "UDP0047", "UDP0048"); //colonne 6
					tags.push("UDP0049", "UDP0050", "UDP0051", "UDP0004", "UDP0053", "UDP0054", "UDP0055", "UDP0056"); //colonne 7
					tags.push("UDP0057", "UDP0058", "UDP0059", "UDP0060", "UDP0068", "UDP0062", "UDP0063", "UDP0064"); //colonne 8
					tags.push("UDP0065", "UDP0066", "UDP0067", "UDP0068", "UDP0069", "UDP0070", "UDP0071", "UDP0072"); //colonne 9
					// BUG vu 14/06/2023: colonne 10 contient 1 erreur !!!!
					//tags.push("UDP0073", "UDP0074", "UDP0075", "UDP0076", "UDP0077", "UDP0078", "UDP0007", "UDP0080"); //colonne 10
					  tags.push("UDP0073", "UDP0074", "UDP0075", "UDP0076", "UDP0077", "UDP0078", "UDP0079", "UDP0080"); //colonne 10
					tags.push("UDP0081", "UDP0082", "UDP0083", "UDP0084", "UDP0085", "UDP0086", "UDP0087", "UDP0088"); //colonne 11
					tags.push("UDP0089", "UDP0090", "UDP0091", "UDP0092", "UDP0093", "UDP0094", "UDP0095", "UDP0096"); //colonne 12
					return tags;
				},
				populateIndex_IdtNexteraSetB:function(){
					var tags=[];
					//	          A          B          C          D          E           F         G          H
					tags.push("UDP0097", "UDP0098", "UDP0099", "UDP0100", "UDP0101", "UDP0102", "UDP0103", "UDP0104"); //colonne 1
					tags.push("UDP0105", "UDP0106", "UDP0107", "UDP0108", "UDP0109", "UDP0110", "UDP0111", "UDP0112"); //colonne 2
					tags.push("UDP0113", "UDP0114", "UDP0115", "UDP0116", "UDP0117", "UDP0118", "UDP0119", "UDP0120"); //colonne 3
					tags.push("UDP0121", "UDP0122", "UDP0123", "UDP0124", "UDP0125", "UDP0126", "UDP0127", "UDP0128"); //colonne 4
					tags.push("UDP0129", "UDP0130", "UDP0131", "UDP0132", "UDP0133", "UDP0134", "UDP0135", "UDP0136"); //colonne 5
					tags.push("UDP0137", "UDP0138", "UDP0139", "UDP0140", "UDP0141", "UDP0142", "UDP0143", "UDP0144"); //colonne 6
					tags.push("UDP0145", "UDP0146", "UDP0147", "UDP0148", "UDP0149", "UDP0150", "UDP0151", "UDP0152"); //colonne 7
					tags.push("UDP0153", "UDP0154", "UDP0155", "UDP0156", "UDP0157", "UDP0158", "UDP0159", "UDP0160"); //colonne 8
					tags.push("UDP0161", "UDP0162", "UDP0163", "UDP0164", "UDP0165", "UDP0166", "UDP0167", "UDP0168"); //colonne 9
					// BUG vu 14/06/2023: colonne 10 contient 5 erreurs !!!!
					//tags.push("UDP0169", "UDP0170", "UDP0170", "UDP0171", "UDP0172", "UDP0173", "UDP0174", "UDP0176"); //colonne 10
					  tags.push("UDP0169", "UDP0170", "UDP0171", "UDP0172", "UDP0173", "UDP0174", "UDP0175", "UDP0176"); //colonne 10
					tags.push("UDP0177", "UDP0178", "UDP0179", "UDP0180", "UDP0181", "UDP0182", "UDP0183", "UDP0184"); //colonne 11
					tags.push("UDP0185", "UDP0186", "UDP0187", "UDP0188", "UDP0189", "UDP0190", "UDP0191", "UDP0192"); //colonne 12
					return tags;
				},
				populateIndex_IdtNexteraSetC:function(){
					var tags=[];
					//	          A          B          C          D          E           F         G          H
					tags.push("UDP0193", "UDP0194", "UDP0195", "UDP0196", "UDP0197", "UDP0198", "UDP0199", "UDP0200"); //colonne 1
					tags.push("UDP0201", "UDP0202", "UDP0203", "UDP0204", "UDP0205", "UDP0206", "UDP0207", "UDP0208"); //colonne 2
					tags.push("UDP0209", "UDP0210", "UDP0211", "UDP0212", "UDP0213", "UDP0214", "UDP0215", "UDP0216"); //colonne 3
					tags.push("UDP0217", "UDP0218", "UDP0219", "UDP0220", "UDP0221", "UDP0222", "UDP0223", "UDP0224"); //colonne 4
					tags.push("UDP0225", "UDP0226", "UDP0227", "UDP0228", "UDP0229", "UDP0230", "UDP0231", "UDP0232"); //colonne 5
					tags.push("UDP0233", "UDP0234", "UDP0235", "UDP0236", "UDP0237", "UDP0238", "UDP0239", "UDP0240"); //colonne 6
					tags.push("UDP0241", "UDP0242", "UDP0243", "UDP0244", "UDP0245", "UDP0246", "UDP0247", "UDP0248"); //colonne 7
					tags.push("UDP0249", "UDP0250", "UDP0251", "UDP0252V2", "UDP0253", "UDP0254", "UDP0255", "UDP0256"); //colonne 8
					tags.push("UDP0257", "UDP0258V2", "UDP0259", "UDP0260", "UDP0261", "UDP0262", "UDP0263", "UDP0264"); //colonne 9
					tags.push("UDP0265", "UDP0266", "UDP0267", "UDP0268", "UDP0269", "UDP0270", "UDP0271", "UDP0272"); //colonne 10
					tags.push("UDP0273", "UDP0274", "UDP0275", "UDP0276", "UDP0277", "UDP0278", "UDP0279", "UDP0280"); //colonne 11
					tags.push("UDP0281", "UDP0282", "UDP0283", "UDP0284", "UDP0285", "UDP0286", "UDP0287", "UDP0288"); //colonne 12
					return tags;
				},
				populateIndex_IdtNexteraSetD:function(){
					var tags=[];
					//	          A          B          C          D          E           F         G          H
					tags.push("UDP0289V2", "UDP0290V2", "UDP0291V2", "UDP0292", "UDP0293", "UDP0294", "UDP0295", "UDP0296"); //colonne 1
					tags.push("UDP0297", "UDP0298", "UDP0299", "UDP0300", "UDP0301V2", "UDP0302", "UDP0303", "UDP0304"); //colonne 2
					tags.push("UDP0305", "UDP0306", "UDP0307", "UDP0308", "UDP0309", "UDP0310", "UDP0311", "UDP0312"); //colonne 3
					tags.push("UDP0313", "UDP0314", "UDP0315", "UDP0316", "UDP0317", "UDP0318", "UDP0319", "UDP0320"); //colonne 4
					tags.push("UDP0321", "UDP0322", "UDP0323", "UDP0324", "UDP0325", "UDP0326", "UDP0327", "UDP0328"); //colonne 5
					tags.push("UDP0329", "UDP0330", "UDP0331", "UDP0332", "UDP0333", "UDP0334", "UDP0335", "UDP0336"); //colonne 6
					tags.push("UDP0337", "UDP0338", "UDP0339", "UDP0340", "UDP0341", "UDP0342", "UDP0343", "UDP0344"); //colonne 7
					tags.push("UDP0345", "UDP0346", "UDP0347", "UDP0348", "UDP0349", "UDP0350", "UDP0351", "UDP0352"); //colonne 8
					tags.push("UDP0353", "UDP0354", "UDP0355", "UDP0356", "UDP0357", "UDP0358", "UDP0359", "UDP0360"); //colonne 9
					tags.push("UDP0361", "UDP0362", "UDP0363", "UDP0364", "UDP0365", "UDP0366", "UDP0367", "UDP0368"); //colonne 10
					tags.push("UDP0369", "UDP0370", "UDP0371", "UDP0372", "UDP0373", "UDP0374", "UDP0375", "UDP0376"); //colonne 11
					tags.push("UDP0377", "UDP0378", "UDP0379", "UDP0380", "UDP0381", "UDP0382", "UDP0383", "UDP0384"); //colonne 12
					return tags;
				},
				*/
				// Agilent(96 SINGLE) ajoutés 03/07/2018; pourquoi ? appelé par aucun controleur!!!!!
				populateIndex_Agl96:function(){
					var tags=[];
					// !!! au sens de repartition des index !!!! ils sont en ligne et pas en colonne
					//            A         B         C         D         E         F         G         H
					tags.push("agl-01", "agl-13", "agl-25", "agl-37", "agl-49", "agl-61", "agl-73", "agl-85"); //colonne 1
					tags.push("agl-02", "agl-14", "agl-26", "agl-38", "agl-50", "agl-62", "agl-74", "agl-86"); //colonne 2
					tags.push("agl-03", "agl-15", "agl-27", "agl-39", "agl-51", "agl-63", "agl-75", "agl-87"); //colonne 3
					tags.push("agl-04", "agl-16", "agl-28", "agl-40", "agl-52", "agl-64", "agl-76", "agl-88"); //colonne 4
					tags.push("agl-05", "agl-17", "agl-29", "agl-41", "agl-53", "agl-65", "agl-77", "agl-89"); //colonne 5
					tags.push("agl-06", "agl-18", "agl-30", "agl-42", "agl-54", "agl-66", "agl-78", "agl-90"); //colonne 6
					tags.push("agl-07", "agl-19", "agl-31", "agl-43", "agl-55", "agl-67", "agl-79", "agl-91"); //colonne 7
					tags.push("agl-08", "agl-20", "agl-32", "agl-44", "agl-56", "agl-68", "agl-80", "agl-92"); //colonne 8
					tags.push("agl-09", "agl-21", "agl-33", "agl-45", "agl-57", "agl-69", "agl-81", "agl-93"); //colonne 9
					tags.push("agl-10", "agl-22", "agl-34", "agl-46", "agl-58", "agl-70", "agl-82", "agl-94"); //colonne 10
					tags.push("agl-11", "agl-23", "agl-35", "agl-47", "agl-59", "agl-71", "agl-83", "agl-95"); //colonne 11
					tags.push("agl-12", "agl-24", "agl-36", "agl-48", "agl-60", "agl-72", "agl-84", "agl-96"); //colonne 12
					return tags;
				},
				// preparation pour NGL-3869/ NGL-3812 
				// plaques Agilent SureSelect XT HS2 (DUAL) les index sont dans NGL depuis NGL-3196/ SUPSQCNG-929 
				// les index sont dans les sens des colonnes cette fois !!
				// Pas de "-" autorisé dans les noms de fonction!!
				populateIndex_AglSureSelect_XTHS2_Kit_A:function(){	
					return this.populateIndex_PlateRange("aglxths2-",1,96,"00");
				},
				populateIndex_AglSureSelect_XTHS2_Kit_B:function(){
					return this.populateIndex_PlateRange("aglxths2-",97,192,"00");
				},
				populateIndex_AglSureSelect_XTHS2_Kit_C:function(){
					return this.populateIndex_PlateRange("aglxths2-",193,288,"00");
				},
				populateIndex_AglSureSelect_XTHS2_Kit_D:function(){
					return this.populateIndex_PlateRange("aglxths2-",289,384,"00");
				},
				// QMPSeq (48 DUAL)  ajout NGL-2896
				populateIndex_QMPSeq48:function(){
					var tags=[];
					//            A         B         C         D         E         F         G         H
					tags.push("QMP001", "QMP002", "QMP003", "QMP004", "QMP005", "QMP006", "QMP007", "QMP008"); //colonne 1
					tags.push("QMP009", "QMP010", "QMP011", "QMP012", "QMP013", "QMP014", "QMP015", "QMP016"); //colonne 2
					tags.push("QMP017", "QMP018", "QMP019", "QMP020", "QMP021", "QMP022", "QMP023", "QMP024"); //colonne 3
					tags.push("QMP025", "QMP026", "QMP027", "QMP028", "QMP029", "QMP030", "QMP031", "QMP032"); //colonne 4
					tags.push("QMP033", "QMP034", "QMP035", "QMP036", "QMP037", "QMP038", "QMP039", "QMP040"); //colonne 5
					tags.push("QMP041", "QMP042", "QMP043", "QMP044", "QMP045", "QMP046", "QMP047", "QMP048"); //colonne 6
					return tags;
					// return this.populateIndex_PlateRange("QMP0",1,48,"0") a tester..
				},
				populateIndex_RUDI_plate_A:function(){
					return this.populateIndex_PlateNbcol("RUDI-A_",12);
				},
				populateIndex_RUDI_plate_B:function(){
					return this.populateIndex_PlateNbcol("RUDI-B_",12);
				},
				populateIndex_RUDI_plate_C:function(){
					return this.populateIndex_PlateNbcol("RUDI-C_",12);
				},
				populateIndex_RUDI_plate_D:function(){
					return this.populateIndex_PlateNbcol("RUDI-D_",12);
				},
				// NGL-4198 ajout plaques E,F,G,H
				populateIndex_RUDI_plate_E:function(){
					return this.populateIndex_PlateNbcol("RUDI-E_",12);
				},
				populateIndex_RUDI_plate_F:function(){
					return this.populateIndex_PlateNbcol("RUDI-F_",12);
				},
				populateIndex_RUDI_plate_G:function(){
					return this.populateIndex_PlateNbcol("RUDI-G_",12);
				},
				populateIndex_RUDI_plate_H:function(){
					return this.populateIndex_PlateNbcol("RUDI-H_",12);
				},
				// NGL-3811 
				// Note il y a eu des plaques set_C et set_D avec des index notés V2, et il y a eu des run/readset avec ces index
				//      ces plaques on du être creées avant l'ajout de cette méthode...'
				populateIndex_IDT_Nextera_Set_A:function(){
					return this.populateIndex_PlateRange("UDP0",1,96,"00");
				},
				populateIndex_IDT_Nextera_Set_B:function(){
					return this.populateIndex_PlateRange("UDP0",97,192,"00");
				},
				/* 07/04/2023 NGL-4191 remplacerles 2 plaques C et D par les versions V2
				populateIndex_IDT_Nextera_Set_C:function(){
					return this.populateIndex_PlateRange("UDP0",193,288,"00");
				},
				populateIndex_IDT_Nextera_Set_D:function(){
					return this.populateIndex_PlateRange("UDP0",289,384,"00");
				},
				*/
				populateIndex_IDT_Nextera_Set_CV2:function(){
					var tags=this.populateIndex_PlateRange("UDP0",193,288,"00");
					// UDP0252V2 ; UDP0258V2 
					// surcharger le tableau !!!
					tags[59]="UDP0252V2";
					tags[65]="UDP0258V2";
					return tags;
				},
				populateIndex_IDT_Nextera_Set_DV2:function(){
					var tags=this.populateIndex_PlateRange("UDP0",289,384,"00");
					// UDP0289V2; UDP0290V2; UDP0291V2; UDP0301V2
					// surcharger le tableau !!!
					tags[0]="UDP0289V2";
					tags[1]="UDP0290V2";
					tags[2]="UDP0291V2";
					tags[12]="UDP0301V2";
					
					return tags;
				},
				
				
				// NGL-3960 ajout: ce sont les memes plaques que Nextera !!!
				populateIndex_IDT_Anchor_Set_A:function(){
					return this.populateIndex_PlateRange("UDP0",1,96,"00");
				},
				populateIndex_IDT_Anchor_Set_B:function(){
					return this.populateIndex_PlateRange("UDP0",97,192,"00");
				},
				populateIndex_IDT_Anchor_Set_C:function(){
					return this.populateIndex_PlateRange("UDP0",193,288,"00");
				},
				populateIndex_IDT_Anchor_Set_D:function(){
					return this.populateIndex_PlateRange("UDP0",289,384,"00");
				},
				// plaque pour tests de developpement equipe NGL/ 1 colonne
				populateIndex_NGLDEBUG8:function(){
					var tags=[];
					//            A         B         C         D         E         F         G         H
					tags.push("QMP001", "QMP002", "QMP003", "QMP004", "QMP005", "QMP006", "QMP007", "QMP008"); //colonne 1
					return tags;
				},
				// plaque pour tests de developpement equipe NGL/ 2 colonnes
				populateIndex_NGLDEBUG16:function(){
					var tags=[];
					//            A         B         C         D         E         F         G         H
					tags.push("QMP001", "QMP002", "QMP003", "QMP004", "QMP005", "QMP006", "QMP007", "QMP008"); //colonne 1
					tags.push("QMP009", "QMP010", "QMP011", "QMP012", "QMP013", "QMP014", "QMP015", "QMP016"); //colonne 2
					return tags;
				}
		}
		return factory;
}]);