models.sra.submit.common.instance :
 	Contient les objets utils communs aux differents type de soumission : 
 	sample et study et submission. Ces objets ont des validations distinctes en fonction 
 	du type de soumission : SRA ou WGS. La validation est differente en fonction 
 	du type de soumission indiquee dans le contexte de validation.
 	L'objet study ou sample ou submission est soumis dans une collection 
 	differente en fonction du type de soumission.
 	
models.sra.submit.sra.instance :
 	Contient les objets specifiques d'une soumission SRA : 
 	experiment et ses run et rawData 
 	
models.sra.submit.wgs.instance :
	Contient les objets specifiques d'une soumission WGS : 
	analysis 
	
	
Attention à l'unicite des noms des study qui sont dans 2 types de tables. Idem pour les samples et
et submission.
	
 	
 	
  