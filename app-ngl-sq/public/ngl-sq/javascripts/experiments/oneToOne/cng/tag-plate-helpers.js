//creation 25/06/2018 pour gerer les plaques d'index CNRGH.
//03/12/2018 essai de remonter au niveau experiments pour etre communn CNG/CNS... 
"use strict"; // ajout et du coup necessite var tags=[]  au lieu de tags[]   !!! 
              // ajout [] dans module()  sinon message erreur module is not available..
              //  marche toujours pas=> Error: [$injector:unpr] Unknown provider: tagPlatesProvider <- tagPlates <- BisSeqLibPrepCtrl
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
				
				// IDT-ILMN TruSeq DNA UD Indexes (24 couples d'indexes DUAL répétés 4 fois !!!)
				// remplacer par 3 boucles for imbriquées ??? 
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
				// remplacer par 2 boucles for imbriquées ???
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
				},
				// NGL-3176 23/11/2020
				// QiaSeq miRNA NGS 96 index IL
				// les codes sont identiques aux noms=> contiennent des majuscules (pas très cohérent  avec QiaSeq48IL ci dessus !!)
				// remplacer par 2 boucles for imbriquées ???
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
				},
				
				// Agilent SureSelect [bleue] (96 SINGLE)
				// NGL-1741 correction erreur, decalage sur  H6->H12
				// remplacer par 1 boucle ??
				populateIndex_AglSur96:function(){
					var tags=[];
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
				
				// Agilent(96 SINGLE) ajoutee 03/07/2018; pourquoi ? appele par aucun controleur!!
				// remplacer par 1 boucle ??
				populateIndex_Agl96:function(){
					var tags=[];
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
				
				// QMPSeq (48 DUAL)  ajout NGL-2896
				// remplacer par 1 boucle ??
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
				},
				/* 04/08/2020 nouvelle version
				 * Fonction qui dispose les index contenus dans une plaque => plaque output a indexer
				 * faite pour fonctionner avec des robots qui travaillenr en mode colonne (colonne plaque index=>colonne plaque output)
				 * il est possible de dire qu'on commense a utiliser la plaque d'index en colonne N et pas la 1ere
				 * 07/05/2020 ajout un parametre optionnel "auto" : dans les cas ou l'utilisateur n'a pas le choix de la colonne de départ 
				 * (exemple: pcr-indexing-and-purification-qmp-seq_ctrl.js=> colonne 1 est imposée) 
				 * les warnings concernant cette colonne ne sont pas pertinents => ne pas les afficher
				 * NGL-2944 11/05/2020 version sans le unset des tags qui est déporté dans une fonction dédiée  unsetTags
				 * 
				 * SUPSQCNG-866: dans le cas de l'administrateur il faut pouvoir écraser un index déjà présent
				 */
				setTags:function(plate, plateColumn, atmService, messages, auto){
					console.log("SETTING INDEX..."); 
					
					var showColWarnings=true;
					if (auto != undefined){ showColWarnings=false;}
					
					console.log(">>selected plate :"+ plate.name + "; start column: "+plateColumn.name+ ";start position :"+ plateColumn.position  );	
					messages.clear();
					
					// attention à certaines colonnes de départ
					// le controle doit porter sur la valeur maximale de colonne trouvee sur la plaque a indexer
					// =>dernier puit si on a trié  dans l'ordre "colonne d'abord"
					var dataMain = atmService.data.getData();
					// trier dans l'ordre "colonne d'abord"
					var dataMain = $filter('orderBy')(dataMain, ['atomicTransfertMethod.column*1','atomicTransfertMethod.line']); 
					var last=dataMain.slice(-1)[0];
					var lastInputCol=last.atomicTransfertMethod.column*1;
					console.log("last col in input plate="+ lastInputCol);
						
					var lastTagCol=plate.tags.length / 8;  // ce sont des colonnes de 8
					console.log("last col in tag plate="+ lastTagCol);
						
					// Même en prennant tous les index, il n'y en a pas assez pour indexer tout les puits de la plaque!! 
					// (NB il existe des plaques de 48 index seulement)
					// 17/07/2018 l'utilisateur à la possibilité de compléter manuellement => warning et pas danger
					// 07/05/2020 ajout showColWarnings
					if ( (lastTagCol < lastInputCol) && showColWarnings ){
						messages.clazz="alert alert-warning";
						messages.text='Remarque: '+ Messages('select.msg.error.notEnoughTags.tagPlate',plate.name);
						messages.showDetails = false;
						messages.open();
						
						//return;     NON, NE PAS BLOQUER
					}
					
					// la liste des colonnes proposée est fixe et ne dépend pas du nbre de colonnes dans les plaque d'index...
					// choisir la colonne 7 pour une plaque qui ne continent que 48 index (max=colonne 6) est une erreur !
					// 07/05/2020 cette erreur doit etre tracée meme si le choix n'est pas due à l'utilisateur
					if ( plateColumn.name*1 > lastTagCol ){	
						messages.clazz="alert alert-danger";
						messages.text=Messages('select.msg.error.emptyStartColumn.tagPlate', plateColumn.name, plate.name );
						messages.showDetails = false;
						messages.open();
						
						return;
					}
					
					// la colonne choisie ne permet pas a tous les puits de la plaque input de recevoir un index
					// 17/07/2018 l'utilisateur à la possibilité de compléter manuellement => warning et pas danger
					// 07/05/2020 ajout showColWarnings
					if ( ((lastTagCol - plateColumn.name*1  +1) < lastInputCol ) && showColWarnings ) { 
						messages.clazz="alert alert-warning";
						messages.text='Remarque: '+ Messages('select.msg.error.wrongStartColumn.tagPlate', plateColumn.name);
						messages.showDetails = false;
						messages.open();
						
						//return;      NON, NE PAS BLOQUER, 
					}
					
					// utiliser displayResult au lieu de dataMain
					var wells = atmService.data.displayResult;
					angular.forEach(wells, function(well){
						var ocu = well.data.outputContainerUsed;
						// 02/04/2020 ne pas écraser un index déjà présent dans le puit!!!!
						/* => 18/08/2020 NGL-2972 JG demande que l'écrasement soit toujours fait !!!!! mise en commentaire du code ajouté 02/04/2020
						var curentTagValue = $parse("experimentProperties.tag.value")(ocu);
						console.log("current tag="+ curentTagValue);
						// SUPSQCNG-866: 04/08/2020 l'administrateur doit pouvoir corriger des erreurs sur une experience terminée => autoriser l'écrasement!!! 
						if ( curentTagValue == undefined || curentTagValue == null || Permissions.check("admin")) {
						*/
							// 02/04/2020 si cette fonction est appellee alors que la position du puit n'est pas encore définie!!! on ne peut rien calculer
							if ( ocu.locationOnContainerSupport.line && ocu.locationOnContainerSupport.column ){
								//calculer la position sur la plaque:   pos= (col -1)*8 + line      (line est le code ascii - 65)
								var libPos=(ocu.locationOnContainerSupport.column  -1 )*8 + (ocu.locationOnContainerSupport.line.charCodeAt(0) -65);
								var indexPos= libPos + plateColumn.position;
								// Si la position calculee ne correspond pas a celle d'une plaque d'index (plaque d'index ne couvre pas toute la plaque de sortie)
								// => ne rien faire
								if ( plate.tags[indexPos]) {
									console.log("==> setting index "+indexPos+ ": "+ plate.tags[indexPos] +" in well "+ocu.locationOnContainerSupport.line+ ocu.locationOnContainerSupport.column);
									$parse("experimentProperties.tag.value").assign(ocu, plate.tags[indexPos]);
									$parse("experimentProperties.tagCategory.value").assign(ocu, plate.tagCategory);
								}
							}
						//} 
					});
				},
				/* NGL-2944 11/05/2020 trop tordu de faire le unset dans la fonction setTags !!!=> création nouvelle fonction dédiée
				 *            => permet aussi de faire unset plus propre avec valeurs de plate et plateColumn mémorisées
				 *            => !! reprendre toutes les expériences qui faisait appel a setTags
				 *            18/08/2020 avec l'écrasement des index meme pour un utilisateur normal, le unset propre ne sert plus a rien !!!!
				 */
				unsetTags:function(plate, plateColumn, atmService, messages){
					console.log("UNSETTING INDEX..."); 
					
					//utilisation de displayResult => besoin de setData(dataMain)...plus rapide ????
					var wells = atmService.data.displayResult;
					angular.forEach(wells, function(well){
						//  NOTE: les puits deja remplis et sautés lors de l'affectation n'ont pas été mémorisés => on les vide aussi !!!!
						var ocu = well.data.outputContainerUsed;
						if ( ocu.locationOnContainerSupport.line && ocu.locationOnContainerSupport.column ){
							//calculer la position sur la plaque:   pos= (col -1)*8 + line      (line est le code ascii - 65)
							var libPos=(ocu.locationOnContainerSupport.column  -1 )*8 + (ocu.locationOnContainerSupport.line.charCodeAt(0) -65);
							var indexPos= libPos + plateColumn.position;
							// Si la position calculee ne correspond pas a celle d'une plaque d'index (plaque d'index ne couvre pas toute la plaque de sortie)
							// =>ne rien faire
							if ( plate.tags[indexPos]) {
								// la position correspond bien a une plaque...
								// 12/05/2020 MAIS vérifer qu'il ne s'agit pas d'un index positionné manuellement
								/*  => 18/08/2020 nettoyer sans verifier !!! mise en commentaire du code ajouté 12/05/2020
								var curentTagValue = $parse("experimentProperties.tag.value")(ocu);
								//console.log("==> index "+indexPos+ ": "+ plate.tags[indexPos] +" in well "+ocu.locationOnContainerSupport.line+ ocu.locationOnContainerSupport.column);
								//console.log("current tag="+ curentTagValue);
								if ( curentTagValue === plate.tags[indexPos]){
								*/
									//console.log("OK suppression");
									$parse("experimentProperties.tag.value").assign(ocu,null);
									$parse("experimentProperties.tagCategory.value").assign(ocu,null);
								//}
							}
						}
					});
				},
				// affecter automatiquement la categorie de tag sur modifiction d'un tag
				computeTagCategory:function(udtData){
					var getter = $parse("outputContainerUsed.experimentProperties.tagCategory.value");
					var compute = {
							tagValue : $parse("outputContainerUsed.experimentProperties.tag.value")(udtData),
							// le filtrage au niveau du name des index est fait dans chaque experience =>   typeahead="v.code as v.name for v in getTags()
							tag : $filter("filter")(factory.allTags,{code:$parse("outputContainerUsed.experimentProperties.tag.value")(udtData)},true),
							isReady:function(){
								return (this.tagValue && this.tag && this.tag.length === 1);
							}
					};
					
					if(compute.isReady()){
					    var tagCategory = undefined;
						var result = compute.tag[0].categoryCode;
						//console.log("result ==== "+result);
						if(result){
							tagCategory = result;				
						}
						getter.assign(udtData, tagCategory);
					}else{
						getter.assign(udtData, undefined);
					}
				},
				// NGL-1350: recuperer les tags et les groupe des tags
				getAllTags: function(){	 
					return this.allTags;
				},
				
				getAllTagGroups: function(){
			        return this.allGroupTags;
				},
				// FDS 29/11/2018  ajouter un paramètre pour filtrer sur les types
				//      => appel:  initTags() ou initTags('index-illumina-sequencing') ou  initTags('index-nanopore-sequencing')
				// FDS 08/01/2021  ajouter un paramètre pour filtrer en plus sur la catégorie si nécessaire 
				//                 ( peut on avoir besoin de ne filtrer QUE sur category ???)
				initTags: function(types, categories){
					factory.allGroupTags=[{'name':'---', 'value':undefined}]; //initialisé ici pour faire patienter pendant l'execution de la promise
					var filters={};
					// if(angular.isUndefined(types)){          pourquoi cette écriture ??
					if ( types === undefined ) {
					    filters = {typeCodes:['index-illumina-sequencing','index-nanopore-sequencing']};
					} else {
						if ( categories !== undefined) {
							console.log('types ET categories');
							filters= {typeCodes: types, categoryCodes: categories};
						} else {
							filters= {typeCodes: types};
						}
					}
					
					$http.get(jsRoutes.controllers.commons.api.Parameters.list().url,{params: filters })
						.success(function(data, status, headers, config) {
							console.log('index Illumina récupérés depuis Mongo...')
							// attention certains tags appartiennent a plusieurs groupes, faire une Map pour obtenir une liste sans doublons
							var groupsMap = new Map();
							data.forEach (function(tag){
								if ( tag.groupNames != null) { 	
									tag.groupNames.forEach (function(group){
										//console.log('group ...'+group);
										//console.log('tag...'+tag.name);
										groupsMap.set(group,"group");
									});
								}
							});
							
							var grps=Array.from(groupsMap.keys()); // convertir la map en tableau
							// creer un tableau d'objet pour les groupes car il y a un cas spécial: il faut un element 'undefined' pour pouvoir saisir 
							// des tags qui n'ont aucun groupName defini...ou pour lesquel l'utilisateur ne le connait pas...
							grps.forEach( function (grp){
								factory.allGroupTags.push ({'name':grp,'value':grp});
							});
							
							// trier le tableau d'objets avec orderBy angular
							factory.allGroupTags = $filter('orderBy')(factory.allGroupTags,'name');
							// 04/10/2018 trier aussi les tags
							factory.allTags=$filter('orderBy')(data,'name');
							//console.log('tri fini...') pas de probleme de perf ici
					});
				},
		}
		return factory;
}]);