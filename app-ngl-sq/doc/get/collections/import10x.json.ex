{
  "code": "RC-201704030930",
  "name": "Import Fichier 10x",
  "fileType": "excel",
  "action": "save",
  "traceInformation": {
    "createUser": "ngsrg",
    "creationDate": new Date(),
    "modifyUser": "ngsrg",
    "modifyDate": new Date()
  },
  "container": {
    "importTypeCode": {
      "_type": "default",
      "required": false,
      "value": "import10x"
    },
    "state": {
      "_type": "object",
      "required": false,
      "code": {
        "_type": "default",
        "required": false,
        "value": "IW-P"
      }
    },
    "support": {
      "_type": "object",
      "required": false,
      "code": {
        "_type": "excel",
        "required": false,
        "cellPosition": 2
      },
      "line": {
        "_type": "excel",
        "required": true,
        "cellPosition": 3,
        "defaultValue": "1"
      },
      "column": {
        "_type": "excel",
        "required": true,
        "cellPosition": 4,
        "defaultValue": "1"
      },
      "categoryCode": {
        "_type": "excel",
        "required": true,
        "cellPosition": 1
      }
    },
    "contents": {
      "_type": "contents",
      "required": true,
      "pool": false,
      "projectCode": {
        "_type": "excel",
        "required": true,
        "cellPosition": 5
      },
      "sampleCode": {
        "_type": "excel",
        "required": true,
        "cellPosition": 6
      },
      "properties": {
        "_type": "properties",
        "required": true,
        "tag": {
          "_type": "propertyValue",
          "required": true,
          "className": "models.laboratory.common.instance.property.PropertySingleValue",
          "value": {
            "_type": "excel",
            "required": true,
            "cellPosition": 9
          }
        },
        "tagCategory": {
          "_type": "propertyValue",
          "required": true,
          "className": "models.laboratory.common.instance.property.PropertySingleValue",
          "value": {
            "_type": "default",
            "required": true,
            "value": "DUAL-INDEX"
          }
        },
        "Type_librairie": {
          "_type": "propertyValue",
          "required": true,
          "className": "models.laboratory.common.instance.property.PropertySingleValue",
          "value": {
            "_type": "default",
            "required": true,
            "value": "10x"
          }
        },
        "Concentration_Librairie": {
          "_type": "propertyValue",
          "required": false,
          "className": "models.laboratory.common.instance.property.PropertySingleValue",
          "value": {
            "_type": "excel",
            "required": true,
            "cellPosition": 16
          },
          "unit": {
            "_type": "default",
            "required": false,
            "value": "nM"
          }
        }
      }
    },
    "concentration": {
      "_type": "propertyValue",
      "required": true,
      "className": "models.laboratory.common.instance.property.PropertySingleValue",
      "value": {
        "_type": "excel",
        "required": true,
        "cellPosition": 16
      },
      "unit": {
        "_type": "default",
        "required": false,
        "value": "nM"
      }
    },
    "comments": {
      "_type": "comments",
      "required": false,
      "comment": {
        "_type": "excel",
        "required": false,
        "cellPosition": 14
      }
    },
    "properties": {
      "_type": "properties",
      "required": true,
      "receptionDate": {
        "_type": "propertyValue",
        "required": true,
        "className": "models.laboratory.common.instance.property.PropertySingleValue",
        "value": {
          "_type": "excel",
          "required": true,
          "cellPosition": 0
        }
      }
    },
    "qualityControlResults": {
      "_type": "qualityControlResults",
      "required": true,
      "properties": {
        "_type": "properties",
        "required": true,
        "providedConcentration": {
          "_type": "propertyValue",
          "required": false,
          "className": "models.laboratory.common.instance.property.PropertySingleValue",
          "value": {
            "_type": "excel",
            "required": false,
            "cellPosition": 12
          },
          "unit": {
            "_type": "default",
            "required": false,
            "value": "ng/µl"
          }
        },
        "comment": {
          "_type": "propertyValue",
          "required": false,
          "className": "models.laboratory.common.instance.property.PropertySingleValue",
          "value": {
            "_type": "excel",
            "required": false,
            "cellPosition": 14
          }
        },
        "providedQuantity": {
          "_type": "propertyValue",
          "required": false,
          "className": "models.laboratory.common.instance.property.PropertySingleValue",
          "value": {
            "_type": "excel",
            "required": false,
            "cellPosition": 13
          },
          "unit": {
            "_type": "default",
            "required": false,
            "value": "ng"
          }
        },
        "providedVolume": {
          "_type": "propertyValue",
          "required": false,
          "className": "models.laboratory.common.instance.property.PropertySingleValue",
          "value": {
            "_type": "excel",
            "required": false,
            "cellPosition": 11
          },
          "unit": {
            "_type": "default",
            "required": false,
            "value": "µL"
          }
        }
      },
      "typeCode": {
        "_type": "default",
        "required": true,
        "value": "external-qc"
      }
    }
  },
  "sample": {
    "code": {
      "_type": "excel",
      "required": true,
      "cellPosition": 6
    },
    "typeCode": {
      "_type": "excel",
      "required": true,
      "cellPosition": 8
    },
    "categoryCode": {
      "_type": "excel",
      "required": true,
      "cellPosition": 8
    },
    "importTypeCode": {
      "_type": "default",
      "required": false,
      "value": "import10x"
    },
    "referenceCollab": {
      "_type": "excel",
      "required": true,
      "cellPosition": 7
    },
    "projectCodes": {
      "_type": "excel",
      "required": true,
      "cellPosition": 5
    },
    "properties": {
      "_type": "properties",
      "required": true,
      "theoricalGCPercent": {
        "_type": "propertyValue",
        "required": false,
        "className": "models.laboratory.common.instance.property.PropertySingleValue",
        "value": {
          "_type": "excel",
          "required": false,
          "cellPosition": 10
        }
      }
    }
  },
  "support": {
    "code": {
      "_type": "excel",
      "required": false,
      "cellPosition": 2
    },
    "state": {
      "_type": "object",
      "required": false,
      "code": {
        "_type": "default",
        "required": false,
        "value": "IW-P"
      }
    }
  }
}